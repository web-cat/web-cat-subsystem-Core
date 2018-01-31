/*==========================================================================*\
 |  $Id: IndependentEOManager.java,v 1.5 2011/12/25 02:24:54 stedwar2 Exp $
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

import org.apache.log4j.Logger;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EORelationshipManipulation;
import com.webobjects.foundation.NSDictionary;

//-------------------------------------------------------------------------
/**
 * This implementation of EOManager provides an independently saveable
 * view of an EO's state, where changes can be saved independently of the
 * EO's editing context.  Make changes as usual, then commit them using
 * the {@link #saveChanges()} method.  Note that if there are optimistic
 * locking conflicts, this class uses a "last write wins" strategy to
 * resolve them automatically.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.5 $, $Date: 2011/12/25 02:24:54 $
 */
public class IndependentEOManager
    implements EOManager
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new manager for the given EO.  The EO must exist and be
     * within an existing editing context, already stored in the database.
     * After giving control of an EO to this manager, no other code should
     * directly modify the EO's state.
     * @param eo the object to manage
     */
    public IndependentEOManager(EOEnterpriseObject eo)
    {
        this(eo.editingContext(), eo, new ECManager());
    }


    // ----------------------------------------------------------
    /**
     * Creates a new manager for the given EO.  The EO must exist and be
     * within an existing editing context, already stored in the database.
     * After giving control of an EO to this manager, no other code should
     * directly modify the EO's state.
     * @param context The editing context used by this manager's client(s)
     * @param eo the object to manage
     * @param manager the (probably shared) editing context manager to use
     * for independent saving of the given eo
     */
    public IndependentEOManager(
        EOEditingContext context, EOEnterpriseObject eo, ECManager manager)
    {
        ecm = manager;
        setClientContext(context);
        mirror = ecm.localize(eo);
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Retrieve this object's <code>id</code> value.
     * @return the value of the attribute
     */
    public Number id()
    {
        try
        {
            return (Number)EOUtilities.primaryKeyForObject(
                mirror.editingContext(), mirror).objectForKey( "id" );
        }
        catch (Exception e)
        {
            return er.extensions.eof.ERXConstant.ZeroInteger;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets a local instance of the managed object in the specified editing
     * context.
     *
     * @param ec the editing context
     * @return a local instance of the object
     */
    public EOEnterpriseObject localInstanceIn(EOEditingContext ec)
    {
        return EOUtilities.localInstanceOfObject(ec, mirror);
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        return ECManager.localize(clientContext, mirror.valueForKey(key));
    }


    // ----------------------------------------------------------
    public void takeValueForKey(Object value, String key)
    {
        mirror.takeValueForKey(ecm.localize(value), key);
    }


    // ----------------------------------------------------------
    public void addObjectToBothSidesOfRelationshipWithKey(
        EORelationshipManipulation eo, String key)
    {
        mirror.addObjectToBothSidesOfRelationshipWithKey(
            ecm.localize(eo), key);
    }


    // ----------------------------------------------------------
    public void addObjectToPropertyWithKey(Object eo, String key)
    {
        mirror.addObjectToPropertyWithKey(ecm.localize(eo), key);
    }


    // ----------------------------------------------------------
    public void removeObjectFromBothSidesOfRelationshipWithKey(
        EORelationshipManipulation eo, String key)
    {
        mirror.removeObjectFromBothSidesOfRelationshipWithKey(
            ecm.localize(eo), key);
    }


    // ----------------------------------------------------------
    public void removeObjectFromPropertyWithKey(Object eo, String key)
    {
        mirror.removeObjectFromPropertyWithKey(ecm.localize(eo), key);
    }


    // ----------------------------------------------------------
    public void refresh()
    {
        ecm.revert();
        ecm.refreshAllObjects();
    }


    // ----------------------------------------------------------
    public void revert()
    {
        ecm.revert();
    }


    // ----------------------------------------------------------
    /**
     * Tries to save any pending changes in the managed EO, returning
     * null on success or an appropriate Exception object on failure.
     * This method allows the caller to decide what to do when saving
     * fails.
     * @return The exception that occurred, if saving fails, or null
     * on success.
     */
    public Exception tryToSaveChanges()
    {
        return ecm.tryToSaveChanges();
    }


    // ----------------------------------------------------------
    public void saveChanges()
    {
        // grab the changes, in case there is trouble saving them
        @SuppressWarnings("unchecked")
        NSDictionary<String, Object> snapshot =
            mirror.editingContext().committedSnapshotForObject(mirror);
        @SuppressWarnings("unchecked")
        NSDictionary<String, Object> changes =
            mirror.changesFromSnapshot(snapshot);

        boolean changesSaved = false;
        // Try ten times
        for (int i = 0; !changesSaved && i< 10; i++)
        {
            EOEnterpriseObject newMirror = ecm.saveChanges(mirror);
            changesSaved = (newMirror == mirror);
            if (!changesSaved)
            {
                // then the changes may have failed
                mirror = newMirror;
                changes = ecm.localize(changes);
                mirror.reapplyChangesFromDictionary(changes);
            }
        }
        if (!changesSaved)
        {
            log.error("Unable to save changes to eo " + mirror);
            log.error("Unsaved changes = " + changes,
                new Exception("here"));
        }
    }


    // ----------------------------------------------------------
    public EOEditingContext clientContext()
    {
        return clientContext;
    }


    // ----------------------------------------------------------
    public void setClientContext(EOEditingContext newClientContext)
    {
        clientContext = newClientContext;
    }


    //~ Instance/static variables .............................................

    private ECManager           ecm;
    private EOEnterpriseObject  mirror;   // copy of EO in ecm context
    private EOEditingContext    clientContext;

    static Logger log = Logger.getLogger(IndependentEOManager.class);
}
