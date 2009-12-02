/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2008 Virginia Tech
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
import com.webobjects.foundation.NSKeyValueCoding.Null;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 * This implementation of EOManager provides an independently saveable
 * view of an EO's state, where changes can be saved independently of the
 * EO's editing context.  Make changes as usual, then commit them using
 * the {@link #saveChanges()} method.  Note that if there are optimistic
 * locking conflicts, this class uses a "last write wins" strategy to
 * resolve them automatically.
 *
 * @author stedwar2
 * @version $Id$
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
        try
        {
            ecm.lock();
            return ECManager.localize(clientContext, mirror.valueForKey(key));
        }
        finally
        {
            ecm.unlock();
        }
    }


    // ----------------------------------------------------------
    public void takeValueForKey( Object value, String key )
    {
        try
        {
            ecm.lock();
            mirror.takeValueForKey(ecm.localize(value), key);
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
        try
        {
            ecm.lock();
            mirror.addObjectToBothSidesOfRelationshipWithKey(
                ecm.localize(eo), key);
        }
        finally
        {
            ecm.unlock();
        }
    }


    // ----------------------------------------------------------
    public void addObjectToPropertyWithKey(Object eo, String key)
    {
        try
        {
            ecm.lock();
            mirror.addObjectToPropertyWithKey(ecm.localize(eo), key);
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
        try
        {
            ecm.lock();
            mirror.removeObjectFromBothSidesOfRelationshipWithKey(
                ecm.localize(eo), key);
        }
        finally
        {
            ecm.unlock();
        }
    }


    // ----------------------------------------------------------
    public void removeObjectFromPropertyWithKey(Object eo, String key)
    {
        try
        {
            ecm.lock();
            mirror.removeObjectFromPropertyWithKey(ecm.localize(eo), key);
        }
        finally
        {
            ecm.unlock();
        }
    }


    // ----------------------------------------------------------
    public void saveChanges()
    {
        try
        {
            ecm.lock();

            // grab the changes, in case there is trouble saving them
            NSDictionary snapshot =
                mirror.editingContext().committedSnapshotForObject(mirror);
            NSDictionary changes = mirror.changesFromSnapshot(snapshot);

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
        finally
        {
            ecm.unlock();
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

    static Logger log = Logger.getLogger( IndependentEOManager.class );
}
