/*==========================================================================*\
 |  $Id: LdapAuthenticator.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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

import com.webobjects.eoaccess.*;
import edu.vt.middleware.ldap.*;
import org.webcat.core.AuthenticationDomain;
import org.webcat.core.LdapAuthenticator;
import org.webcat.core.User;
import org.webcat.core.UserAuthenticator;
import org.webcat.core.WCProperties;
import org.apache.log4j.Logger;

// --------------------------------------------------------------------------
/**
 *  A concrete implementation of <code>UserAuthenticator</code> that
 *  tests user ids/passwords using LDAP.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class LdapAuthenticator
    implements UserAuthenticator
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new object.
     */
    public LdapAuthenticator()
    {
        // Initialization happens in configure()
    }


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
    public boolean configure( String       baseName,
                              WCProperties properties )
    {
        boolean result = true;
        String host = properties.getProperty( baseName + ".ldap.hostUrl" );
        if ( host == null || host.equals("") )
        {
            log.error( "a required property is not set: "
                       + baseName + ".ldap.hostUrl" );
            result = false;
        }

        String base = properties.getProperty( baseName + ".ldap.context" );
        if ( base == null || base.equals("") )
        {
            log.error( "a required property is not set: "
                       + baseName + ".ldap.context" );
            result = false;
        }

        if (!result) return result;


        String userField =
            properties.getProperty( baseName + ".ldap.userField" );
        if ( userField == null || userField.equals("") )
        {
            userField = "cn";
        }

        log.debug(baseName + ": host = " + host + ", context = " + base
            + ", user field = " + userField);

        AuthenticatorConfig config = new AuthenticatorConfig(host, base);

        String bindDN =  properties.getProperty( baseName + ".ldap.bindDN" );
        String bindPassword =
            properties.getProperty( baseName + ".ldap.bindPassword" );
        if ("".equals(bindDN)) { bindDN = null; }
        if ("".equals(bindPassword)) { bindPassword = null; }
        if (bindDN == null && bindPassword != null)
        {
            log.error( baseName + ".ldap.bindPassword was set without"
                + "setting " + baseName + ".ldap.bindDN" );
            result = false;
        }
        else if (bindPassword == null && bindDN != null)
        {
            log.error( baseName + ".ldap.bindDN was set without"
                + "setting " + baseName + ".ldap.bindPassword" );
            result = false;
        }
        if (bindDN != null)
        {
            config.setServiceUser(bindDN);
            log.debug(baseName + ": bindDN = " + bindDN);
        }
        if (bindPassword != null)
        {
            config.setServiceCredential(bindPassword);
            log.debug(baseName + ": bindPassword = " + bindPassword);
        }

        config.setSubtreeSearch(
            properties.booleanForKey( baseName + ".ldap.searchSubtrees" ) );

        String authFilter =
            properties.getProperty( baseName + ".ldap.authFilter" );
        if (authFilter != null && !authFilter.equals(""))
        {
            config.setAuthorizationFilter(authFilter);
        }

        config.setUserField(new String[] {userField});
        authenticator = new Authenticator(config);

        try
        {
            if (properties.booleanForKey(baseName + ".useTLS"))
            {
                log.debug(baseName + ": turning TLS on");
                authenticator.useTls(true);
            }
            else
            {
                log.debug(baseName + ": turning TLS off");
                authenticator.useTls(false);
            }
        }
        catch (Exception e)
        {
            log.error("Cannot use TLS:", e);
        }

        this.cacheInSession =
            properties.booleanForKey(baseName + ".cacheInSession");

        return result;
    }


    // ----------------------------------------------------------
    /**
     * Validate the user `username' with the password `password'.
     * Should not be called until the authenticator has been configured.
     *
     * @param username The user id to validate
     * @param password The password to check
     * @param domain   The authentication domain associated with this check
     * @param ec       The editing context to use
     * @param ls       An existing login session associated with this
     *                 user, if one exists (or null, if not)
     * @return The current user object, or null if invalid login
     */
    public User authenticate( String               username,
                              String               password,
                              AuthenticationDomain domain,
                              com.webobjects.eocontrol.EOEditingContext ec,
                              LoginSession ls)
    {
        User user = null;
        log.debug("authenticate(), user = " + username + ", session = " + ls);
        if (authenticate(username, password, ls))
        {
            log.debug("user " + username + " validated");
            try
            {
                user = User.uniqueObjectMatchingQualifier(
                    ec,
                    User.userName.is(username).and(
                        User.authenticationDomain.is(domain)));
                if (user == null)
                {
                    user = User.createUser(
                        username,
                        null,  // DO NOT MIRROR PASSWORD IN DATABASE
                               // for security reasons
                        domain,
                        User.STUDENT_PRIVILEGES,
                        ec
                    );
                    log.info("new user '"
                        + username
                        + "' ("
                        + domain.displayableName()
                        + ") created");
                }
                else if (user.authenticationDomain() != domain)
                {
                    if (user.authenticationDomain() == null)
                    {
                        user.setAuthenticationDomainRelationship(domain);
                    }
                    else
                    {
                        log.warn("user " + username
                            + " successfully validated in '"
                            + domain.displayableName()
                            + "' but bound to '"
                            + user.authenticationDomain().displayableName()
                            + "'");
                        user = null;
                    }
                }
            }
            catch (EOUtilities.MoreThanOneException e)
            {
                log.error("user '"
                    + username
                    + "' ("
                    + domain.displayableName()
                    + "):",
                    e);
            }
        }
        else
        {
            log.info("user " + username + "(" + domain.displayableName()
                + "): login validation failed");
        }
        if (user != null && ls == null && this.cacheInSession)
        {
            log.info("user " + username + "(" + domain.displayableName() +
                "): ldap caching");
            user.setPassword(password);
            ec.saveChanges();
        }

        return user;
    }

    private boolean authenticate(
        String username, String password, LoginSession s)
    {
        boolean result = false;
        if ((s != null) && (this.cacheInSession))
        {
            result = (username.equals(s.user().userName())) &&
                (s.user().checkPassword(password));
            log.info("user " + username + ": ldap cache check = " + result);
        }
        if (!result) {
            try
            {
                result = authenticator.authenticate(username, password);
                log.info("user " + username + ": ldap forced auth, result = "
                    + result);
            }
            catch (Exception e)
            {
                log.error("authentication failure: ", e);
            }
        }
        log.debug("result = " + result);
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Check whether users validated with this authenticator can
     * change their password.  For authentication mechanisms using
     * external databases or servers where no changes are allowed, the
     * authenticator should return false.
     *
     * @return True if users associated with this authenticator can
     *         change their password
     */
    public boolean canChangePassword()
    {
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Change the user's password.  For authentication mechanisms using
     * external databases or servers where no changes are allowed, an
     * authenticator may simply return false for all requests.
     *
     * @param user        The user
     * @param newPassword The password to change to
     * @return True if the password change was successful
     */
    public boolean changePassword( User   user,
                                   String newPassword )
    {
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Change the user's password to a new random password, and e-mail's
     * the user their new password.  For authentication mechanisms using
     * external databases or servers where no changes are allowed, an
     * authenticator may simply return false for all requests.
     *
     * @param user        The user
     * @return True if the password change was successful
     */
    public boolean newRandomPassword( User user )
    {
        return false;
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( LdapAuthenticator.class );
    private Authenticator authenticator;
    private boolean cacheInSession = false;
}
