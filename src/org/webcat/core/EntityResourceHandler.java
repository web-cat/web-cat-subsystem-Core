/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2010 Virginia Tech
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

import java.io.File;
import org.webcat.core.EntityResourceRequestHandler;
import com.webobjects.eocontrol.EOEnterpriseObject;

//-------------------------------------------------------------------------
/**
 * Subsystems can register entity resource handlers with the
 * {@link EntityResourceRequestHandler} to provide direct URL hooks to access
 * resources on the file-system that are associated with those objects.
 *
 * @param <T> the actual type of the EO used by this resource handler
 *
 * @author  Tony Allevato
 * @version $Id$
 */
public abstract class EntityResourceHandler<T extends EOEnterpriseObject>
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the absolute file-system path to a resource associated with an EO.
     *
     * @param object the object whose associated resources are being accessed
     * @param relativePath the path to the resource, relative to whatever
     *     root is appropriate for the object
     * @return a File object representing the absolute path of the file on the
     *     file system where the resource is located
     */
    public abstract File pathForResource(T object, String relativePath);


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the resource requires a logged in user.
     * By default, resources do not need a log-in.
     *
     * @return true if the resource handler requires a user to be logged in,
     *     or false if the resource can be loaded anonymously
     */
    public boolean requiresLogin()
    {
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether a user has access to view resources for
     * the specified object. By default, any user can access resources for the
     * object; override this to provide object-specific checks.
     *
     * Users with admin privileges can always access any resource, regardless
     * of the return value of this method.
     *
     * @param object the object
     * @param user the user
     * @return true if the user can access the resource, otherwise false
     */
    public boolean userCanAccess(T object, User user)
    {
        return true;
    }
}
