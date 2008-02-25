/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License
 |  along with Web-CAT; if not, write to the Free Software
 |  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 |
 |  Project manager: Stephen Edwards <edwards@cs.vt.edu>
 |  Virginia Tech CS Dept, 660 McBryde Hall (0106), Blacksburg, VA 24061 USA
\*==========================================================================*/

package net.sf.webcat.core;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;

import org.apache.log4j.*;

//-------------------------------------------------------------------------
/**
 * This class encapsulates an EO that is managed in its own editing context
 * so that it can be saved/managed independently of the client editing context
 * from which it is being accessed.  The intent is to allow a single
 * object (managed by this container) to be independently saved to the
 * database, separate from all the other objects related to it.
 *
 * @author stedwar2
 * @version $Id$
 */
public class IndependentEOManager
    implements NSKeyValueCoding,
               EORelationshipManipulation,
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
    public IndependentEOManager(EOEnterpriseObject eo, ECManager manager)
    {
        ecm = manager;
        snapshot = eo.snapshot().mutableClone();

        // Now convert all NSArrays in snapshot so they are mutable
        NSArray toManyKeys = eo.toManyRelationshipKeys();
        for (int i = 0; i < toManyKeys.count(); i++)
        {
            String key = (String)toManyKeys.objectAtIndex(i);
            snapshot.takeValueForKey(
                ((NSArray)snapshot.valueForKey(key)).mutableClone(),
                key);
        }
        NSArray attributeKeyArray = eo.attributeKeys();
        attributeKeys = new NSDictionary(attributeKeyArray, attributeKeyArray);

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
    public Object clone()
    {
        try
        {
            IndependentEOManager result = (IndependentEOManager)super.clone();

            result.snapshot = (NSMutableDictionary)snapshot.clone();

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
        if (result == NSKeyValueCoding.NullValue)
        {
            result = null;
        }
        return result;
    }


    // ----------------------------------------------------------
    public void takeValueForKey( Object value, String key )
    {
        Object current = snapshot.valueForKey(key);
        if (attributeKeys.valueForKey(key) == null)
        {
            // Then this is a relationship, not a plain attribute
            if (value == null)
            {
                if (current == null || current == NSKeyValueCoding.NullValue)
                {
                    return;
                }
                if (current instanceof NSArray)
                {
                    NSArray currents = (NSArray)current;
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
                NSArray currents = (NSArray)current;
                for (int i = 0; i < currents.count(); i++)
                {
                    removeObjectFromBothSidesOfRelationshipWithKey(
                        (EORelationshipManipulation)currents.objectAtIndex(i),
                        key);
                }
                NSArray newOnes = (NSArray)value;
                for (int i = 0; i < newOnes.count(); i++)
                {
                    addObjectToBothSidesOfRelationshipWithKey(
                        (EORelationshipManipulation)newOnes.objectAtIndex(i),
                        key);
                }
            }
            else
            {
                if (current != null)
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
            || (value == null && current == NSKeyValueCoding.NullValue))
        {
            return;
        }

        ecm.lock();
        try
        {
            Object newValue = value;
            if (value == null)
            {
                snapshot.takeValueForKey(NSKeyValueCoding.NullValue, key);
            }
            else if (value instanceof NSArray)
            {
                snapshot.takeValueForKey(((NSArray)value).mutableClone(), key);
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
    public void addObjectToBothSidesOfRelationshipWithKey(
        EORelationshipManipulation eo, String key)
    {
        Object current = snapshot.valueForKey(key);
        if (current != null && current instanceof NSArray)
        {
            NSMutableArray currentTargets = (NSMutableArray)current;
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
                (EORelationshipManipulation)ecm.localize(eo), key);
            EOEnterpriseObject oldMirror = mirror;
            mirror = ecm.saveChanges(mirror);
            if (mirror != oldMirror)
            {
                // retry it once if the save forced an abort and a new
                // EC was created instead
                mirror.addObjectToBothSidesOfRelationshipWithKey(
                    (EORelationshipManipulation)ecm.localize(eo), key);
                mirror = ecm.saveChanges(mirror);
            }
        }
        finally
        {
            ecm.unlock();
        }
    }


    // ----------------------------------------------------------
    public void addObjectToPropertyWithKey(Object eo, String key)
    {
        Object current = snapshot.valueForKey(key);
        if (current != null && current instanceof NSArray)
        {
            NSMutableArray currentTargets = (NSMutableArray)current;
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
        if (current != null && current instanceof NSArray)
        {
            NSMutableArray currentTargets = (NSMutableArray)current;
            currentTargets.remove(eo);
        }
        else if (current == eo)
        {
            snapshot.takeValueForKey(NSKeyValueCoding.NullValue, key);
        }
        ecm.lock();
        try
        {
            mirror.removeObjectFromBothSidesOfRelationshipWithKey(
                (EORelationshipManipulation)ecm.localize(eo), key);
            EOEnterpriseObject oldMirror = mirror;
            mirror = ecm.saveChanges(mirror);
            if (mirror != oldMirror)
            {
                // retry it once if the save forced an abort and a new
                // EC was created instead
                mirror.removeObjectFromBothSidesOfRelationshipWithKey(
                    (EORelationshipManipulation)ecm.localize(eo), key);
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
        if (current != null && current instanceof NSArray)
        {
            NSMutableArray currentTargets = (NSMutableArray)current;
            currentTargets.remove(eo);
        }
        else if (current == eo)
        {
            snapshot.takeValueForKey(NSKeyValueCoding.NullValue, key);
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


    // ----------------------------------------------------------
    public static class ECManager
    {
        // ----------------------------------------------------------
        /**
         * Create a new ECManager with a new internal editing context.
         */
        public ECManager()
        {
            ec = Application.newPeerEditingContext();
        }


        // ----------------------------------------------------------
        /**
         * Get a copy of the given EO in the internal editing context.
         * @param eo The EO to transfer into the internal EC
         * @return A local instance of the given EO
         */
        protected EOEnterpriseObject localize(EOEnterpriseObject eo)
        {
            if (eo == null || eo.editingContext() == ec)
            {
                return eo;
            }
            else
            {
                return EOUtilities.localInstanceOfObject(ec, eo);
            }
        }


        // ----------------------------------------------------------
        /**
         * Get a copy of the given EO in the internal editing context.
         * @param object The EO to transfer into the internal EC.  If the given
         * object is not an EOEnterpriseObject, it is returned unchanged.
         * @return A local instance of the given EO
         */
        protected Object localize(Object object)
        {
            if (object == null)
            {
                return object;
            }
            else if (object instanceof EOEnterpriseObject)
            {
                return localize((EOEnterpriseObject)object);
            }
            else if (object instanceof NSArray)
            {
                NSMutableArray result = ((NSArray)object).mutableClone();
                for (int i = 0; i < result.count(); i++)
                {
                    result.set(i, localize(result.objectAtIndex(i)));
                }
                return result;
            }
            else
            {
                return object;
            }
        }


        // ----------------------------------------------------------
        /**
         * Calls saveChanges() on the internal editing context.  If any
         * error occurs, it throws away the old editing context and
         * creates a new one.  Assumes that the editing context is currently
         * locked by the caller, and leaves it locked after completion.
         * If a new editing context is created, the given EO (which presumably
         * already exists in the current EC) is imported into the new
         * context, and then returned.  Otherwise, the EO is returned
         * unchanged.
         * @param eo The primary EO of interest
         * @return eo in the current editing context (imported into a
         * new editing context if the old one is thrown away).
         */
        public EOEnterpriseObject saveChanges(EOEnterpriseObject eo)
        {
            try
            {
                ec.saveChanges();
            }
            catch (Exception e)
            {
                log.info("Exception in saveChanges(); throwing away old EC",
                    e);

                // Something happened, so try replacing the old context
                // with a new one.
                EOEditingContext newContext =
                    Application.newPeerEditingContext();
                newContext.lock();
                eo = EOUtilities.localInstanceOfObject(newContext, eo);

                // Now try to clean up the old one
                try
                {
                    // Try to unlock first, if possible
                    try
                    {
                        ec.unlock();
                    }
                    catch (Exception eee)
                    {
                        // nothing
                    }
                    // Try to clean up the broken editing context, if possible
                    Application.releasePeerEditingContext(ec);
                }
                catch (Exception ee)
                {
                    // if there is an error, ignore it since we're not going to
                    // use this ec any more anyway
                }

                // Finally do the replacement
                ec = newContext;
            }
            return eo;
        }


        // ----------------------------------------------------------
        /**
         * Lock the internal editing context.
         */
        public void lock()
        {
            ec.lock();
        }


        // ----------------------------------------------------------
        /**
         * Unlock the internal editing context.
         */
        public void unlock()
        {
            ec.unlock();
        }


        // ----------------------------------------------------------
        /**
         * Releases the inner EOEditContext.
         */
        public void dispose()
        {
            Application.releasePeerEditingContext(ec);
        }


        //~ Instance/static variables .........................................
        private EOEditingContext ec;
    }


    //~ Instance/static variables .............................................

    private ECManager           ecm;
    private EOEnterpriseObject  mirror;   // copy of EO internal ec context
    private NSMutableDictionary snapshot; // snapshot of managed EO's state
    private NSDictionary        attributeKeys;

    static Logger log = Logger.getLogger( IndependentEOManager.class );
}
