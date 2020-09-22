/*==========================================================================*\
 |  Copyright (C) 2006-2021 Virginia Tech
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

import org.webcat.core.AuthenticationDomain;
import org.webcat.core.User;
import org.webcat.core.WCProperties;
import com.webobjects.eocontrol.EOEditingContext;

// -------------------------------------------------------------------------
/**
 *  An interface for all concrete user name/password authentication
 *  techniques.
 *
 *  @author Stephen Edwards
 */
public interface UserAuthenticator
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Initialize and configure the authenticator, reading subclass-specific
     * settings from properties.  The authenticator should read any
     * instance-specific settings from properties named
     * "baseName.<property>".  This operation should only be called once,
     * before any authenticate requests.
     *
     * @param baseName   The base property name for this authenticator object
     * @param properties The property collection from which the object
     *                   should read its configuration settings
     * @return true If configuration was successful and authenticator is
     *              ready for service
     */
    public boolean configure(String baseName, WCProperties properties);


    // ----------------------------------------------------------
    /**
     * Validate the user `username' with the password `password'.
     * Should not be called until the authenticator has been configured.
     *
     * @param userName The user id to validate
     * @param password The password to check
     * @param domain   The authentication domain associated with this check
     * @param ec       The editing context to use
     * @param ls       An existing login session associated with this
     *                 user, if one exists (or null, if not)
     * @return The current user object, or null if invalid login
     */
    public User authenticate(
        String userName,
        String password,
        AuthenticationDomain domain,
        EOEditingContext ec,
        LoginSession ls);
}
