/*==========================================================================*\
 |  $Id: CachingEOManager.java,v 1.2 2011/12/25 02:24:54 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2011 Virginia Tech
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

package org.webcat.core;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import org.webcat.core.CachingEOManager;
import org.webcat.core.EOManager;
import org.webcat.woextensions.ECAction;
import org.webcat.woextensions.ECActionWithResult;
import org.webcat.woextensions.WCEC;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 * This implementation of EOManager provides a "write through" cache of
 * an EO's state, where every change to every attribute or relationship is
 * immediately committed to the database, and all attributes/relationships
 * are locally cached as well.
 *
 * @author stedwar2
 * @author  latest changes by: $Author: stedwar2 $
 * @version $Revision: 1.2 $ $Date: 2011/12/25 02:24:54 $
 */
public class CachingEOManager
    implements EOManager,
               NSKeyValueCoding.ErrorHandling,
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
    public CachingEOManager(EOBase eo, WCEC context)
    {
//        ecm = manager;
//        snapshot = eo.snapshot().mutableClone();

        // Now convert all NSArrays in snapshot so they are mutable
//        for (String key : eo.toManyRelationshipKeys())
//        {
//            snapshot.takeValueForKey(
//                ((NSArray<?>)snapshot.valueForKey(key)).mutableClone(),
//                key);
//        }
        NSArray<String> attributeKeyArray = eo.attributeKeys();

        attributeKeys = new NSDictionary<String, String>(
            attributeKeyArray, attributeKeyArray);

        // Now create a mirror in a new EC
//        mirror = ecm.localize(eo);
        original = eo;
        this.context = context;
        if (context != original.editingContext())
        {
            original = (EOBase)original.localInstanceIn(context);
        }
        globalId = original.globalId();
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public Object clone()
    {
        try
        {
            CachingEOManager result = (CachingEOManager)super.clone();

//            result.snapshot =
//                (NSMutableDictionary<String, Object>)snapshot.clone();
            result.original = original;
            result.globalId = globalId;

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
//        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
        Object result = original.valueForKey(key);
        log.debug(me() + ": valueForKey(" + key + ") => " + result);
        return result;
    }


    // ----------------------------------------------------------
    public void takeValueForKey( Object value, String key )
    {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(
            this, value, key);
    }


    // ----------------------------------------------------------
    public void addObjectToBothSidesOfRelationshipWithKey(
        final EORelationshipManipulation eo, final String key)
    {
        EOGlobalID id = null;
        if (eo != null)
        {
            EOBase obj = (EOBase)eo;
            id = obj.globalId();
        }
        final EOGlobalID finalId = id;
        new ECAction() { public void action() {
            EOEnterpriseObject clone = ec.faultForGlobalID(globalId, ec);
            EOEnterpriseObject other = finalId == null
                ? null
                : ec.faultForGlobalID(finalId, ec);

            log.debug(me() + ": addObjectToBothSidesOfRelationshipWithKey(), "
                + "key = " + key + " => " + other);
            clone.addObjectToBothSidesOfRelationshipWithKey(other, key);
            ec.saveChanges();
            log.debug(me() + ": addObjectToBothSidesOfRelationshipWithKey(), "
                + "after save = " + key + " => " + clone.valueForKey(key));
        }}.run();
        refreshMaster(key);
//        Object current = snapshot.valueForKey(key);
//        if (current != null
//            && current != NullValue
//            && current instanceof NSArray)
//        {
//            NSMutableArray<Object> currentTargets =
//                (NSMutableArray<Object>)current;
//            if (currentTargets.contains(eo))
//            {
//                return;
//            }
//            currentTargets.add(eo);
//        }
//        else
//        {
//            snapshot.takeValueForKey(eo, key);
//        }
//        log.debug("setting " + key + ": " + current + " <= from/to => " + eo);
//        mirror.addObjectToBothSidesOfRelationshipWithKey(
//            ecm.localize(eo), key);
//        EOEnterpriseObject oldMirror = mirror;
//        log.debug("attempting to save");
//        mirror = ecm.saveChanges(mirror);
//        if (mirror != oldMirror)
//        {
//            log.debug("generated new mirror ... retrying");
//            // retry it once if the save forced an abort and a new
//            // EC was created instead
//            mirror.addObjectToBothSidesOfRelationshipWithKey(
//                ecm.localize(eo), key);
//            mirror = ecm.saveChanges(mirror);
//            log.debug("after retrying save, new " + key + " => "
//                + mirror.valueForKey(key));
//        }
//        else
//        {
//            log.debug("after save, new " + key + " => "
//                + mirror.valueForKey(key));
//        }
    }


    // ----------------------------------------------------------
    public void addObjectToPropertyWithKey(final Object eo, final String key)
    {
        new ECAction() { public void action() {
            EOEnterpriseObject clone = ec.faultForGlobalID(globalId, ec);
            log.debug(me() + ": addObjectToPropertyWithKey(), key = "
                + key + " => " + eo);
            clone.addObjectToPropertyWithKey(eo, key);
            ec.saveChanges();
            log.debug(me() + ": addObjectToPropertyWithKey(), after save = "
                + key + " => " + clone.valueForKey(key));
        }}.run();
        refreshMaster(key);
//        Object current = snapshot.valueForKey(key);
//        if (current != null
//            && current != NullValue
//            && current instanceof NSArray)
//        {
//            NSMutableArray<Object> currentTargets =
//                (NSMutableArray<Object>)current;
//            if (currentTargets.contains(eo))
//            {
//                return;
//            }
//            currentTargets.add(eo);
//        }
//        else
//        {
//            snapshot.takeValueForKey(eo, key);
//        }
//        mirror.addObjectToPropertyWithKey(ecm.localize(eo), key);
//        EOEnterpriseObject oldMirror = mirror;
//        mirror = ecm.saveChanges(mirror);
//        if (mirror != oldMirror)
//        {
//            // retry it once if the save forced an abort and a new
//            // EC was created instead
//            mirror.addObjectToPropertyWithKey(ecm.localize(eo), key);
//            mirror = ecm.saveChanges(mirror);
//        }
    }


    // ----------------------------------------------------------
    public void removeObjectFromBothSidesOfRelationshipWithKey(
        EORelationshipManipulation eo, final String key)
    {
        EOGlobalID id = null;
        if (eo != null)
        {
            EOBase obj = (EOBase)eo;
            id = obj.globalId();
        }
        final EOGlobalID finalId = id;
        new ECAction() { public void action() {
            EOEnterpriseObject clone = ec.faultForGlobalID(globalId, ec);
            EOEnterpriseObject other = finalId == null
                ? null
                : ec.faultForGlobalID(finalId, ec);

            log.debug(me() + ": removeObjectFromBothSidesOfRelationship"
                + "WithKey(), key = " + key + " - " + other);
            clone.removeObjectFromBothSidesOfRelationshipWithKey(other, key);
            ec.saveChanges();
            log.debug(me() + ": removeObjectFromBothSidesOfRelationship"
                + "WithKey(), after save = " + key + " - "
                + clone.valueForKey(key));
        }}.run();
        refreshMaster(key);
//        Object current = snapshot.valueForKey(key);
//        if (current != null
//            && current != NullValue
//            && current instanceof NSArray)
//        {
//            NSMutableArray<?> currentTargets =
//                (NSMutableArray<?>)current;
//            currentTargets.remove(eo);
//        }
//        else if (current == eo)
//        {
//            snapshot.takeValueForKey(NullValue, key);
//        }
//        mirror.removeObjectFromBothSidesOfRelationshipWithKey(
//            ecm.localize(eo), key);
//        EOEnterpriseObject oldMirror = mirror;
//        mirror = ecm.saveChanges(mirror);
//        if (mirror != oldMirror)
//        {
//            // retry it once if the save forced an abort and a new
//            // EC was created instead
//            mirror.removeObjectFromBothSidesOfRelationshipWithKey(
//                ecm.localize(eo), key);
//            mirror = ecm.saveChanges(mirror);
//        }
    }


    // ----------------------------------------------------------
    public void removeObjectFromPropertyWithKey(
        final Object eo, final String key)
    {
        new ECAction() { public void action() {
            EOEnterpriseObject clone = ec.faultForGlobalID(globalId, ec);
            log.debug(me() + ": removeObjectFromPropertyWithKey(), key = "
                + key + " - " + eo);
            clone.removeObjectFromPropertyWithKey(eo, key);
            ec.saveChanges();
            log.debug(me() + ": removeObjectFromPropertyWithKey(), after "
                + "save = " + key + " - " + clone.valueForKey(key));
        }}.run();
        refreshMaster(key);
//        Object current = snapshot.valueForKey(key);
//        if (current != null
//            && current != NullValue
//            && current instanceof NSArray)
//        {
//            NSMutableArray<?> currentTargets = (NSMutableArray<?>)current;
//            currentTargets.remove(eo);
//        }
//        else if (current == eo)
//        {
//            snapshot.takeValueForKey(NullValue, key);
//        }
//        mirror.removeObjectFromPropertyWithKey(ecm.localize(eo), key);
//        EOEnterpriseObject oldMirror = mirror;
//        mirror = ecm.saveChanges(mirror);
//        if (mirror != oldMirror)
//        {
//            // retry it once if the save forced an abort and a new
//            // EC was created instead
//            mirror.removeObjectFromPropertyWithKey(ecm.localize(eo), key);
//            mirror = ecm.saveChanges(mirror);
//        }
    }


    // ----------------------------------------------------------
    public Object handleQueryWithUnboundKey(final String key)
    {
        Object result = original.valueForKey(key);
//        log.error("handleQueryWithUnboundKey("
//            + key + ") should never be called",
//            new Exception("incorrectly called from here"));
//        Object result = new ECActionWithResult<Object>() {
//            public Object action() {
//                EOEnterpriseObject clone = ec.faultForGlobalID(globalId, ec);
//                Object out = clone.valueForKey(key);
//                if (out instanceof EOCustomObject)
//                {
//                    EOCustomObject custom = (EOCustomObject)out;
//                    if (custom.isFault())
//                    {
//                        ec.refreshObject(custom);
//                    }
//                    out = ec.globalIDForObject(custom);
//                }
//                return out;
//            }}.call();

//        Object result = snapshot.valueForKey(key);
//        if (result == NullValue)
//        {
//            result = null;
//        }
//        result = localize(result);
        log.debug(me() + ": valueForKey(" + key + ") => " + result);
        return result;
    }


    // ----------------------------------------------------------
    public void handleTakeValueForUnboundKey(
        final Object value, final String key)
    {
//        Object current = snapshot.valueForKey(key);
//        if (current != null && current != NullValue && current instanceof Null)
//        {
//            log.error("non-unique KVC.Null found in snapshot for key " + key);
//            log.error("snapshot = " + snapshot);
//        }
        if (attributeKeys.valueForKey(key) == null)
        {
            // Then this is a relationship, not a plain attribute
            if (value == null)
            {
//                if (current == null
//                    || current == NullValue
//                    || current instanceof Null)
//                {
//                    return;
//                }
//                if (current instanceof NSArray)
//                {
//                    NSArray<?> currents = (NSArray<?>)current;
//                    if (currents.count() == 1)
//                    {
//                        current = currents.objectAtIndex(0);
//                    }
//                    else
//                    {
//                        throw new IllegalArgumentException("takeValueForKey("
//                            + value + ", " + key + ") called on to-many "
//                            + "relationship with current value of "
//                            + currents);
//                    }
//                }
                new ECAction() { public void action() {
                    EOEnterpriseObject clone =
                        ec.faultForGlobalID(globalId, ec);
                    Object current = clone.valueForKey(key);
                    if (current != null)
                    {
                        if (current instanceof NSArray)
                        {
                            NSArray<?> currents = (NSArray<?>)current;
                            if (currents.count() == 1)
                            {
                                current = currents.objectAtIndex(0);
                            }
                            else
                            {
                                throw new IllegalArgumentException(
                                    "takeValueForKey(" + value + ", " + key
                                    + ") called on to-many "
                                    + "relationship with current value of "
                                    + currents);
                            }
                        }
                        log.debug(me() + ": handleTakeValueForUnboundKey("
                            + key + ") null => removing " + current);
                        clone.removeObjectFromBothSidesOfRelationshipWithKey(
                            (EORelationshipManipulation)current, key);
                        ec.saveChanges();
                        log.debug(me() + ": handleTakeValueForUnboundKey("
                            + key + ") after save => " + key + " => "
                            + clone.valueForKey(key));
                    }
                }}.run();

//                removeObjectFromBothSidesOfRelationshipWithKey(
//                    (EORelationshipManipulation)current, key);
            }
            else if (value instanceof NSArray)
            {
                final NSArray<?> newOnes = (NSArray<?>)value;
                new ECAction() { public void action() {
                    EOEnterpriseObject clone =
                        ec.faultForGlobalID(globalId, ec);
                    NSArray<?> currents = (NSArray<?>)clone.valueForKey(key);
                    for (int i = 0; i < currents.count(); i++)
                    {
                        EOBase other =
                            (EOBase)currents.objectAtIndex(i);
                        log.debug(me() + ": handleTakeValueForUnboundKey("
                            + key + ") null => removing " + other);
                        clone.removeObjectFromBothSidesOfRelationshipWithKey(
                            ec.faultForGlobalID(
                                other.globalId(), ec),
                            key);
                    }
                    for (int i = 0; i < newOnes.count(); i++)
                    {
                        EORelationshipManipulation other =
                            (EORelationshipManipulation)newOnes
                            .objectAtIndex(i);
                        log.debug(me() + ": handleTakeValueForUnboundKey("
                            + key + ") => adding " + other);
                        clone.addObjectToBothSidesOfRelationshipWithKey(
                            other, key);
                    }
                    ec.saveChanges();
                    log.debug(me() + ": handleTakeValueForUnboundKey("
                        + key + ") after save => " + clone.valueForKey(key));
                }}.run();
            }
            else if (value instanceof EOCustomObject)
            {
                new ECAction() { public void action() {
                    EOEnterpriseObject clone =
                        ec.faultForGlobalID(globalId, ec);
                    Object current = clone.valueForKey(key);
                    if (current != null)
                    {
                        if (current instanceof NSArray)
                        {
                            NSArray<?> currents = (NSArray<?>)current;
                            if (currents.count() == 1)
                            {
                                current = currents.objectAtIndex(0);
                            }
                            else
                            {
                                throw new IllegalArgumentException(
                                    "takeValueForKey(" + value + ", " + key
                                    + ") called on to-many "
                                    + "relationship with current value of "
                                    + currents);
                            }
                        }
                        log.debug(me() + "handleTakeValueForUnboundKey(" + key
                            + ") => removing current: " + current);
                        clone.removeObjectFromBothSidesOfRelationshipWithKey(
                            (EORelationshipManipulation)current, key);
                    }
                    EOEnterpriseObject other = ec.faultForGlobalID(
                        ((EOBase)value).globalId(), ec);
                    log.debug(me() + ": handleTakeValueForUnboundKey(" + key
                        + ") => adding " + other);
                    clone.addObjectToBothSidesOfRelationshipWithKey(
                        other, key);
                    ec.saveChanges();
                    log.debug(me() + ": handleTakeValueForUnboundKey(" + key
                        + ") after save => " + clone.valueForKey(key));
                }}.run();
//                if (current != null
//                    && current != NullValue
//                    && !(current instanceof Null))
//                {
//                    removeObjectFromBothSidesOfRelationshipWithKey(
//                        (EORelationshipManipulation)current, key);
//                }
//                addObjectToBothSidesOfRelationshipWithKey(
//                    (EORelationshipManipulation)value, key);
            }
            else
            {
                throw new IllegalArgumentException(
                    "takeValueForKey(" + value + ", " + key
                    + ") called on to-many "
                    + "relationship, but don't know how to handle value");
            }
            refreshMaster(key);
            return;
        }

        new ECAction() { public void action() {
            EOEnterpriseObject clone = ec.faultForGlobalID(globalId, ec);
            log.debug(me() + ": handleTakeValueForUnboundKey(" + key
                + ") => takeValueForKey(): " + value);
            clone.takeValueForKey(value, key);
            ec.saveChanges();
            log.debug(me() + ": handleTakeValueForUnboundKey(" + key
                + ") => after takeValueForKey(): " + clone.valueForKey(key));
        }}.run();
        refreshMaster(key);
//        if (current == value
//            || (value == null && current == NullValue))
//        {
//            return;
//        }
//
//        Object newValue = value;
//        if (value == null)
//        {
//            snapshot.takeValueForKey(NullValue, key);
//        }
//        else if (value instanceof NSArray)
//        {
//            snapshot.takeValueForKey(
//                ((NSArray<?>)value).mutableClone(), key);
//            newValue = ecm.localize(value);
//        }
//        else
//        {
//            snapshot.takeValueForKey(value, key);
//            newValue = ecm.localize(value);
//        }
//        log.debug("setting " + key + ": " + current + " <= from/to => "
//            + newValue);
//        mirror.takeValueForKey(newValue, key);
//        EOEnterpriseObject oldMirror = mirror;
//        log.debug("attempting to save");
//        mirror = ecm.saveChanges(mirror);
//        if (mirror != oldMirror)
//        {
//            log.debug("generated new mirror ... retrying");
//            // retry it once if the save forced an abort and a new
//            // EC was created instead
//            mirror.takeValueForKey(ecm.localize(newValue), key);
//            mirror = ecm.saveChanges(mirror);
//            log.debug("after retrying save, new " + key + " => "
//                + mirror.valueForKey(key));
//        }
//        else
//        {
//            log.debug("after save, new " + key + " => "
//                + mirror.valueForKey(key));
//        }
    }


    // ----------------------------------------------------------
    public void unableToSetNullForKey(String key)
    {
        NSKeyValueCoding.DefaultImplementation.unableToSetNullForKey(this, key);
    }


    // ----------------------------------------------------------
    public void dispose()
    {
//        if (ownsEcm && ecm != null)
//        {
//            ecm.dispose();
//        }
    }


    // ----------------------------------------------------------
    private Object localize(final Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        else if (obj == NullValue || obj instanceof Null)
        {
            return null;
        }
        else if (obj instanceof EOGlobalID)
        {
            return new ECActionWithResult<Object>(context) {
                public Object action() {
                    return ec.faultForGlobalID((EOGlobalID)obj, ec);
                }}.call();
        }
        else if (obj instanceof EOCustomObject)
        {
            final EOBase eo = (EOBase)obj;
            log.warn(me() + ": localize() received EO = " + eo.globalId(),
                new Exception("localize() called from here"));
            return new ECActionWithResult<Object>(context) {
                public Object action()
                {
                    return ec.faultForGlobalID(eo.globalId(), ec);
                }}.call();
        }
        else
        {
            return obj;
        }
    }


    // ----------------------------------------------------------
    private String me()
    {
        return this.getClass().getSimpleName() + "@"
            + System.identityHashCode(this);
    }


    // ----------------------------------------------------------
    private void refreshMaster(final String key)
    {
        if (original != null)
        {
            if (original.editingContext() != context)
            {
                log.error("Mismatched editing contexts",
                    new Exception("refreshMaster() called here"));
            }
            context.refaultObject(original);
//                new ECAction(original.wcEditingContext()) {
//                    public void action() {
//                        ec.refreshObject(original);
//                        if (key != null)
//                        {
//                            log.debug(me() + ": refreshing master, " + key
//                                + " => " + original.valueForKey(key));
//                        }
//                    }}.run();
//            }
        }
    }


    //~ Instance/static variables .............................................

//    private ECManager ecm;
//    private boolean ownsEcm = false;

    // Original eo passed in, just for refreshing purposes
    private EOBase     original;
    private WCEC       context;
    private EOGlobalID globalId;
    // copy of EO internal ec context
//    private EOEnterpriseObject  mirror;

    // snapshot of managed EO's state
//    private NSMutableDictionary<String, Object> snapshot;

    private NSDictionary<String, String> attributeKeys;

    static Logger log = Logger.getLogger(CachingEOManager.class);
}
