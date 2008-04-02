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

import com.webobjects.eoaccess.*;
import com.webobjects.foundation.NSDictionary;
import edu.vt.middleware.eddo.*;
import org.apache.log4j.Logger;

// --------------------------------------------------------------------------
/**
 *  A concrete implementation of <code>UserAuthenticator</code> that
 *  tests user ids/passwords against the Virginia Tech ED-Auth service
 *  using LDAP.
 *
 *  @author edwards
 *  @version $Id$
 */
public class EdAuthAuthenticator
    implements UserAuthenticator
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new EdAuthAuthenticator object.
     */
    public EdAuthAuthenticator()
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
                              WCProperties properties
                            )
    {
        try
        {
            dm = new DirectoryManager();
            pm = dm.createPersonManager();
        }
        catch ( Exception e )
        {
            log.error( "failure connecting to EdAuth service", e );
        }

//        Provider[] providers = Security.getProviders();
//        for (int i = 0; i < providers.length; i++) {
//            Provider provider = providers[i];
//            log.warn("Provider name: " + provider.getName());
//            log.warn("Provider information: " + provider.getInfo());
//            log.warn("Provider version: " + provider.getVersion());
//            Set entries = provider.entrySet();
//            Iterator iterator = entries.iterator();
//            while ( iterator.hasNext() ) {
//                log.warn("Property entry: " + iterator.next());
//            }
//        }
        return true;
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
     * @return The current user object, or null if invalid login
     */
    public User authenticate( String               username,
                              String               password,
                              AuthenticationDomain domain,
                              com.webobjects.eocontrol.EOEditingContext ec
                            )
    {
        User user = null;
        if ( authenticate( username, password ) )
        {
            log.debug( "user " + username + " validated" );
            try
            {
                user = (User)EOUtilities.objectMatchingValues(
                    ec, User.ENTITY_NAME,
                    new NSDictionary(
                        new Object[]{ username , domain              },
                        new Object[]{ User.USER_NAME_KEY,
                                      User.AUTHENTICATION_DOMAIN_KEY }
                    ) );
                if ( user.authenticationDomain() != domain )
                {
                    if ( user.authenticationDomain() == null )
                    {
                        user.setAuthenticationDomainRelationship( domain );
                    }
                    else
                    {
                        log.warn(
                                "user " + username
                                + " successfully validated in '"
                                + domain.displayableName()
                                + "' but bound to '"
                                + user.authenticationDomain().displayableName()
                                + "'"
                                );
                        user = null;
                    }
                }
            }
            catch ( EOObjectNotAvailableException e )
            {
                user = User.createUser(
                         username,
                         null,  // DO NOT MIRROR PASSWORD IN DATABASE
                                // for security reasons
                         domain,
                         User.STUDENT_PRIVILEGES,
                         ec
                    );
                log.info( "new user '"
                          + username
                          + "' ("
                          + domain.displayableName()
                          + ") created"
                        );
            }
            catch ( EOUtilities.MoreThanOneException e )
            {
                log.error( "user '"
                           + username
                           + "' ("
                           + domain.displayableName()
                           + "):",
                           e
                         );
            }
        }
        else
        {
            log.info( "user " + username + "(" + domain.displayableName()
                      + "): login validation failed" );
        }

        return user;
    }

    private boolean authenticate( String username, String password )
    {
        boolean result = false;
        try
        {
            result = pm.authenticatePerson( username, password );
        }
        catch ( Exception e )
        {
            log.error( "authentication failure: ", e );
        }
        log.debug( "result = " + result );
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

    static Logger log = Logger.getLogger( EdAuthAuthenticator.class );

    private DirectoryManager dm;
    private PersonManager    pm;
}
