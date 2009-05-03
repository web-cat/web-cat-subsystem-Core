/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU Affero General Public License as published
 |  by the Free Software Foundation; either version 3 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU Affero General Public License
 |  along with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
\*==========================================================================*/

package net.sf.webcat.core;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 * This implementation of EOManager provides a "write through" cache of
 * an EO's state, where every change to every attribute or relationship is
 * immediately committed to the database, and all attributes/relationships
 * are locally cached as well.
 *
 * @author stedwar2
 * @author  latest changes by: $Author$
 * @version $Revision$ $Date$
 */
public class CachingEOManager
    implements EOManager,
               Cloneable
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new manager for the given EO.  The EO must exist and be
     * within an existing editing context, already stored in the database.
     * After giving control of an EO to this manager, no other code should
     * directly modify the EO's state.
     * @param eo the object to manage
     * @param manager the (probably shared) editing context manager to use
     * for independent saving of the given eo
     */
    @SuppressWarnings("unchecked")
    public CachingEOManager(EOEnterpriseObject eo, ECManager manager)
    {
        ecm = manager;
        snapshot = eo.snapshot().mutableClone();

        // Now convert all NSArrays in snapshot so they are mutable
        for (String key : eo.toManyRelationshipKeys())
        {
            snapshot.takeValueForKey(
                ((NSArray<?>)snapshot.valueForKey(key)).mutableClone(),
                key);
        }
        NSArray<String> attributeKeyArray = eo.attributeKeys();

        attributeKeys = new NSDictionary<String, String>(
            attributeKeyArray, attributeKeyArray);

        // Now create a mirror in a new EC
        ecm.lock();
        try
        {
            mirror = ecm.localize(eo);
        }
        finally
        {
            ecm.unlock();
        }
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Object clone()
    {
        try
        {
            CachingEOManager result = (CachingEOManager)super.clone();

            result.snapshot =
                (NSMutableDictionary<String, Object>)snapshot.clone();

            return result;
        }
        catch (CloneNotSupportedException e)
        {
            // never happens
            return null;
        }
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        Object result = snapshot.valueForKey(key);
        if (result == NullValue)
        {
            result = null;
        }
        return result;
    }


    // ----------------------------------------------------------
    public void takeValueForKey( Object value, String key )
    {
        Object current = snapshot.valueForKey(key);
        if (current != null && current != NullValue && current instanceof Null)
        {
            log.error("non-unique KVC.Null found in snapshot for key " + key);
            log.error("snapshot = " + snapshot);
        }
        if (attributeKeys.valueForKey(key) == null)
        {
            // Then this is a relationship, not a plain attribute
            if (value == null)
            {
                if (current == null || current == NullValue)
                {
                    return;
                }
                if (current instanceof NSArray)
                {
                    NSArray<?> currents = (NSArray<?>)current;
                    if (currents.count() == 1)
                    {
                        current = currents.objectAtIndex(0);
                    }
                    else
                    {
                        throw new IllegalArgumentException("takeValueForKey("
                            + value + ", " + key + ") called on to-many "
                            + "relationship with current value of "
                            + currents);
                    }
                }
                removeObjectFromBothSidesOfRelationshipWithKey(
                    (EORelationshipManipulation)current, key);
            }
            else if (value instanceof NSArray)
            {
                NSArray<?> currents = (NSArray<?>)current;
                for (int i = 0; i < currents.count(); i++)
                {
                    removeObjectFromBothSidesOfRelationshipWithKey(
                        (EORelationshipManipulation)currents.objectAtIndex(i),
                        key);
                }
                NSArray<?> newOnes = (NSArray<?>)value;
                for (int i = 0; i < newOnes.count(); i++)
                {
                    addObjectToBothSidesOfRelationshipWithKey(
                        (EORelationshipManipulation)newOnes.objectAtIndex(i),
                        key);
                }
            }
            else
            {
                if (current != null && current != NullValue)
                {
                    removeObjectFromBothSidesOfRelationshipWithKey(
                        (EORelationshipManipulation)current, key);
                }
                addObjectToBothSidesOfRelationshipWithKey(
                    (EORelationshipManipulation)value, key);
            }
            return;
        }
        if (current == value
            || (value == null && current == NullValue))
        {
            return;
        }

        ecm.lock();
        try
        {
            Object newValue = value;
            if (value == null)
            {
                snapshot.takeValueForKey(NullValue, key);
            }
            else if (value instanceof NSArray)
            {
                snapshot.takeValueForKey(
                    ((NSArray<?>)value).mutableClone(), key);
                newValue = ecm.localize(value);
            }
            else
            {
                snapshot.takeValueForKey(value, key);
                newValue = ecm.localize(value);
            }
            mirror.takeValueForKey(newValue, key);
            EOEnterpriseObject oldMirror = mirror;
            mirror = ecm.saveChanges(mirror);
            if (mirror != oldMirror)
            {
                // retry it once if the save forced an abort and a new
                // EC was created instead
                mirror.takeValueForKey(ecm.localize(newValue), key);
                mirror = ecm.saveChanges(mirror);
            }
        }
        finally
        {
            ecm.unlock();
        }
    }


    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void addObjectToBothSidesOfRelationshipWithKey(
        EORelationshipManipulation eo, String key)
    {
        Object current = snapshot.valueForKey(key);
        if (current != null
            && current != NullValue
            && current instanceof NSArray)
        {
            NSMutableArray<Object> currentTargets =
                (NSMutableArray<Object>)current;
            if (currentTargets.contains(eo))
            {
                return;
            }
            currentTargets.add(eo);
        }
        else
        {
            snapshot.takeValueForKey(eo, key);
        }
        ecm.lock();
        try
        {
            mirror.addObjectToBothSidesOfRelationshipWithKey(
                ecm.localize(eo), key);
            EOEnterpriseObject oldMirror = mirror;
            mirror = ecm.saveChanges(mirror);
            if (mirror != oldMirror)
            {
                // retry it once if the save forced an abort and a new
                // EC was created instead
                mirror.addObjectToBothSidesOfRelationshipWithKey(
                    ecm.localize(eo), key);
                mirror = ecm.saveChanges(mirror);
            }
        }
        finally
        {
            ecm.unlock();
        }
    }


    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void addObjectToPropertyWithKey(Object eo, String key)
    {
        Object current = snapshot.valueForKey(key);
        if (current != null
            && current != NullValue
            && current instanceof NSArray)
        {
            NSMutableArray<Object> currentTargets =
                (NSMutableArray<Object>)current;
            if (currentTargets.contains(eo))
            {
                return;
            }
            currentTargets.add(eo);
        }
        else
        {
            snapshot.takeValueForKey(eo, key);
        }
        ecm.lock();
        try
        {
            mirror.addObjectToPropertyWithKey(ecm.localize(eo), key);
            EOEnterpriseObject oldMirror = mirror;
            mirror = ecm.saveChanges(mirror);
            if (mirror != oldMirror)
            {
                // retry it once if the save forced an abort and a new
                // EC was created instead
                mirror.addObjectToPropertyWithKey(ecm.localize(eo), key);
                mirror = ecm.saveChanges(mirror);
            }
        }
        finally
        {
            ecm.unlock();
        }
    }


    // ----------------------------------------------------------
    public void removeObjectFromBothSidesOfRelationshipWithKey(
        EORelationshipManipulation eo, String key)
    {
        Object current = snapshot.valueForKey(key);
        if (current != null
            && current != NullValue
            && current instanceof NSArray)
        {
            NSMutableArray<?> currentTargets =
                (NSMutableArray<?>)current;
            currentTargets.remove(eo);
        }
        else if (current == eo)
        {
            snapshot.takeValueForKey(NullValue, key);
        }
        ecm.lock();
        try
        {
            mirror.removeObjectFromBothSidesOfRelationshipWithKey(
                ecm.localize(eo), key);
            EOEnterpriseObject oldMirror = mirror;
            mirror = ecm.saveChanges(mirror);
            if (mirror != oldMirror)
            {
                // retry it once if the save forced an abort and a new
                // EC was created instead
                mirror.removeObjectFromBothSidesOfRelationshipWithKey(
                    ecm.localize(eo), key);
                mirror = ecm.saveChanges(mirror);
            }
        }
        finally
        {
            ecm.unlock();
        }
    }


    // ----------------------------------------------------------
    public void removeObjectFromPropertyWithKey(Object eo, String key)
    {
        Object current = snapshot.valueForKey(key);
        if (current != null
            && current != NullValue
            && current instanceof NSArray)
        {
            NSMutableArray<?> currentTargets = (NSMutableArray<?>)current;
            currentTargets.remove(eo);
        }
        else if (current == eo)
        {
            snapshot.takeValueForKey(NullValue, key);
        }
        ecm.lock();
        try
        {
            mirror.removeObjectFromPropertyWithKey(ecm.localize(eo), key);
            EOEnterpriseObject oldMirror = mirror;
            mirror = ecm.saveChanges(mirror);
            if (mirror != oldMirror)
            {
                // retry it once if the save forced an abort and a new
                // EC was created instead
                mirror.removeObjectFromPropertyWithKey(ecm.localize(eo), key);
                mirror = ecm.saveChanges(mirror);
            }
        }
        finally
        {
            ecm.unlock();
        }
    }


    //~ Instance/static variables .............................................

    private ECManager           ecm;

    // copy of EO internal ec context
    private EOEnterpriseObject  mirror;

    // snapshot of managed EO's state
    private NSMutableDictionary<String, Object> snapshot;

    private NSDictionary<String, String>        attributeKeys;

    static Logger log = Logger.getLogger(CachingEOManager.class);
}
