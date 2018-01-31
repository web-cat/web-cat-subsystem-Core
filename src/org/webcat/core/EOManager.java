/*==========================================================================*\
 |  $Id: EOManager.java,v 1.6 2014/06/16 16:01:32 stedwar2 Exp $
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
import org.webcat.woextensions.WCEC;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EORelationshipManipulation;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.eof.ERXEC;

//-------------------------------------------------------------------------
/**
 * This interface defines common features for classes that encapsulate an EO
 * that is managed in its own editing context so that it can be saved/managed
 * independently of the client editing context from which it is being
 * accessed.  The intent is to allow a single object (managed by this
 * container) to be independently saved to the database, separate from all
 * the other objects related to it.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.6 $, $Date: 2014/06/16 16:01:32 $
 */
public interface EOManager
    extends NSKeyValueCoding,
            EORelationshipManipulation
{
    // ----------------------------------------------------------
    /**
     * This inner class encapsulates an internal editing context that
     * might need to change over time, if the old editing context accumulates
     * errors that need to be dumped.
     */
    public static class ECManager
    {
        // ----------------------------------------------------------
        /**
         * Create a new ECManager with a new internal editing context.
         */
        public ECManager()
        {
            ec = WCEC.newAutoLockingEditingContext();
        }


        // ----------------------------------------------------------
        /**
         * Get a copy of the given object in the given editing context.
         * @param <T> The type of the object to be localized, which could
         *  be a concrete EO class
         * @param context The editing context to localize to
         * @param object The EO to transfer into the EC.  If the given
         * object is not an EOEnterpriseObject, it is returned unchanged.
         * If the object is an NSArray or NSDictionary, a copy with
         * localized internal values is returned.
         * @return A local instance of the given object
         */
        public static <T> T localize(EOEditingContext context, T object)
        {
            if (object == null)
            {
                return object;
            }
            else if (object instanceof EOEnterpriseObject)
            {
                if (((EOEnterpriseObject)object).editingContext() == context)
                {
                    return object;
                }
                else
                {
                    @SuppressWarnings("unchecked")
                    T resultAsT = (T)EOUtilities.localInstanceOfObject(
                        context, (EOEnterpriseObject)object);
                    return resultAsT;
                }
            }
            else if (object instanceof NSDictionary)
            {
                NSMutableDictionary<?, ?> result =
                    ((NSDictionary<?, ?>)object).mutableClone();
                for (Object key : result.allKeys())
                {
                    result.takeValueForKey(
                        localize(context, result.valueForKey((String)key)),
                        (String)key);
                }
                @SuppressWarnings("unchecked")
                T resultAsT = (T)result;
                return resultAsT;
            }
            else if (object instanceof NSArray)
            {
                @SuppressWarnings("unchecked")
                NSMutableArray<Object> result =
                    ((NSArray<Object>)object).mutableClone();
                for (int i = 0; i < result.count(); i++)
                {
                    result.set(i, localize(context, result.objectAtIndex(i)));
                }
                @SuppressWarnings("unchecked")
                T resultAsT = (T)result;
                return resultAsT;
            }
            else
            {
                return object;
            }
        }


        // ----------------------------------------------------------
        /**
         * Get a copy of the given object in the internal editing context.
         * @param <T> The type of the object to be localized, which could
         *  be a concrete EO class
         * @param object The EO to transfer into the EC.  If the given
         * object is not an EOEnterpriseObject, it is returned unchanged.
         * If the object is an NSArray or NSDictionary, a copy with
         * localized internal values is returned.
         * @return A local instance of the given object
         */
        protected <T> T localize(T object)
        {
            return localize(ec, object);
        }


        // ----------------------------------------------------------
        /**
         * Calls saveChanges() on the internal editing context, returning
         * null on success or an appropriate Exception object on failure.
         * This method allows the caller to decide what to do when saving
         * fails.
         * @return The exception that occurred, if saving fails, or null
         * on success.
         */
        public Exception tryToSaveChanges()
        {
            Exception result = null;
            try
            {
                ec.saveChanges();
            }
            catch (Exception e)
            {
                result = e;
            }
            return result;
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
            Exception err = tryToSaveChanges();
            if (err != null)
            {
                log.info("Exception in saveChanges(); throwing away old EC",
                    err);

                // Something happened, so try replacing the old context
                // with a new one.
                ERXEC newContext = WCEC.newAutoLockingEditingContext();
                eo = EOUtilities.localInstanceOfObject(newContext, eo);
                newContext.refreshObject(eo);

                // Now try to clean up the old one
                try
                {
                    // Try to clean up the broken editing context, if possible
                    ec.dispose();
                }
                catch (Exception ee)
                {
                    // if there is an error, ignore it since we're not going
                    // to use this ec any more anyway
                }

                // Finally do the replacement
                ec = newContext;
            }
            return eo;
        }


        // ----------------------------------------------------------
        /**
         * Revert the internal editing context, throwing away any
         * pending changes.
         */
        public void revert()
        {
            ec.revert();
        }


        // ----------------------------------------------------------
        /**
         * Refresh all objects in the internal editing context.
         */
        public void refreshAllObjects()
        {
            ec.refreshAllObjects();
        }


        // ----------------------------------------------------------
        /**
         * Releases the inner EOEditContext.
         */
        public void dispose()
        {
            ec.dispose();
        }


        //~ Instance/static variables .........................................
        private ERXEC ec;

        static Logger log = Logger.getLogger(ECManager.class);
    }
}
