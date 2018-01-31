/*==========================================================================*\
 |  $Id: EOBase.java,v 1.2 2012/06/22 16:23:18 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2012 Virginia Tech
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

import java.lang.reflect.Method;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

// -------------------------------------------------------------------------
/**
 * A custom base class for all EOs that captures common features we want
 * them all to inherit.
 *
 * @author  Stephen Edwards
 * @author  Last changed by: $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2012/06/22 16:23:18 $
 */
public class EOBase
    extends er.extensions.eof.ERXGenericRecord
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     */
    public EOBase()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Determine if a given user can access this object.  The default
     * implementation simply returns true, and should be overridden by
     * subclasses that wish to limit access to a subset of users.
     * @param user The user to check.
     * @return True if the user can access this object.
     */
    public boolean accessibleByUser(User user)
    {
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Determine if a given user can access this object.  The default
     * implementation simply returns true, and should be overridden by
     * subclasses that wish to limit access to a subset of users.
     * @param user The user to check.
     * @return True if the user can access this object.
     */
    public static WCAccessibleQualifier accessibleBy(User user)
    {
        return new WCAccessibleQualifier(user);
    }


    // ----------------------------------------------------------
    /**
     * Gets the database ID of the object. This method is defined here so that
     * it can be called from {@link #apiId()} without needing reflection, since
     * every EO defines it anyway.
     *
     * @return the database ID of the object
     */
    public Number id()
    {
        throw new UnsupportedOperationException("This EO does not override "
                + "the id() method. This is probably a problem in the "
                + "generated template.");
    }


    // ----------------------------------------------------------
    /**
     * Gets the external web API identifier for the receiving object. This ID
     * will be used in URLs and in data retrieved from the web API. The default
     * behavior is to simply return the numeric database ID ({@link #id()}) as
     * a string, but subclasses can override this method to provide a more
     * human-readable unique name.
     *
     * @return the external web API identifier of the object
     */
    public String apiId()
    {
        return id().toString();
    }


    // ----------------------------------------------------------
    /**
     * Retrieves the object that has the specified external web API identifier.
     *
     * Since static methods cannot be overridden, this method takes the
     * {@code Class} of the actual object that should be retrieved, along with
     * the identifier. The search process then works as follows:
     *
     * 1. If the identifier can be parsed as a number, then it is treated as
     *    the database ID of the object.
     * 2. Otherwise, we look for a static method on the specified {@code Class}
     *    named {@code findObjectWithApiId(String)} and call it, passing the
     *    API identifier that was given. This method should return the one
     *    object that it finds, or null if there was no object with that ID.
     *
     * @param <T> the type of the object to find
     * @param ec the editing context
     * @param type a {@code Class} object matching {@code <T>}
     * @param apiId the external web API identifier of the object, or the
     *     string representation of the database identifier
     *
     * @return the object that was found, or null if there was no object with
     *     that identifier
     */
    public static <T extends EOBase> T objectWithApiId(
            EOEditingContext ec, Class<? extends T> type, String apiId)
    {
        Long idAsNumber;
        T result = null;

        try
        {
            idAsNumber = Long.parseLong(apiId);
        }
        catch (NumberFormatException e)
        {
            idAsNumber = null;
        }

        if (idAsNumber != null)
        {
            try
            {
                Method forId = type.getMethod("forId",
                        EOEditingContext.class, int.class);
                result = (T) forId.invoke(null, ec, idAsNumber.intValue());
            }
            catch (Exception e)
            {
                result = null;
            }
        }

        if (result == null)
        {
            Method findObjectWithApiId = null;

            try
            {
                findObjectWithApiId =
                    type.getMethod("findObjectWithApiId",
                            EOEditingContext.class, String.class);

                result = (T) findObjectWithApiId.invoke(null, ec, apiId);
            }
            catch (Exception e)
            {
                result = null;
            }
        }

        return result;
    }


    // ----------------------------------------------------------
    /**
     * A variant of {@link #objectWithApiId(EOEditingContext, Class, String)}
     * that instead takes the entity name string instead of the class.
     *
     * @param ec the editing context
     * @param entityName the entity name
     * @param apiId the external web API identifier of the object, or the
     *     string representation of the database identifier
     *
     * @return the object that was found, or null if there was no object with
     *     that identifier
     */
    public static EOBase objectWithApiId(
            EOEditingContext ec, String entityName, String apiId)
    {
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);
        String className = entity.className();

        try
        {
            Class<? extends EOBase> klass =
                (Class<? extends EOBase>) Class.forName(className);

            return objectWithApiId(ec, klass, apiId);
        }
        catch (ClassNotFoundException e)
        {
            log.error("Could not find class for entity " + entity, e);
            return null;
        }
    }
}
