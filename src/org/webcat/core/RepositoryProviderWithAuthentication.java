/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2015 Virginia Tech
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

import com.webobjects.eocontrol.EOEditingContext;

//-------------------------------------------------------------------------
/**
 * <p>
 * An interface implemented by EOs that want to provide their own file
 * repositories, and that also want to manage authentication for
 * access to them.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public interface RepositoryProviderWithAuthentication
    extends RepositoryProvider
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Allows the object to perform its own custom user authentication,
     * instead of using Web-CAT's built-in authentication
     * @param ec The editing context to use for the request.
     * @param altUsername The user name to look up.
     * @param altPassword The password to authenticate for the user.
     * @return The user object, if the credentials are authenticated, or
     * null if authentication fails.
     */
    public User authorizedUserForRepository(
        EOEditingContext ec, String altUsername, String altPassword);
}
