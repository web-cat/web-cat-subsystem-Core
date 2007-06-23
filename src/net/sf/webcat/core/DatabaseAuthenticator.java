/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License
 |  along with Web-CAT; if not, write to the Free Software
 |  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 |
 |  Project manager: Stephen Edwards <edwards@cs.vt.edu>
 |  Virginia Tech CS Dept, 660 McBryde Hall (0106), Blacksburg, VA 24061 USA
\*==========================================================================*/

package net.sf.webcat.core;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  A concrete implementation of <code>UserAuthenticator</code> that
 *  tests user ids/passwords against information stored in the database.
 *  This implementation tests the application property
 *  <code>authenticator.addIfNotFound</code>.  If this property is
 *  true, user names that are not already in the database will be added
 *  as new users with the given password (i.e., automatic
 *  account creation for unknown user names).  If the property is
 *  false or unset, then only users already in the database will be
 *  admitted.
 *
 *  @author Stephen Edwards
 *  @version $Id$
 */
public class DatabaseAuthenticator
    implements UserAuthenticator
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new <code>DatabaseAuthenticator</code> object.
     *
     */
    public DatabaseAuthenticator()
    {
        // Private data are initialized in their declarations
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
        addIfNotFound = properties.booleanForKey( baseName + ".addIfNotFound" );
        skipPasswordChecks =
            properties.booleanForKey( baseName + ".skipPasswordChecks" );
        log.debug( baseName + ".addIfNotFound = " + addIfNotFound );
        if ( skipPasswordChecks )
        {
            log.warn(
                baseName + ".skipPasswordChecks = " + skipPasswordChecks );
        }
        else
        {
            log.debug(
                baseName + ".skipPasswordChecks = " + skipPasswordChecks );
        }
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Validate the user `username' with the password `password'.
     * Should not be called until the authenticator has been configured.
     *
     * @param userName The user id to validate
     * @param password The password to check
     * @param domain   The authentication domain associated with this check
     * @param ec       The editing context to use
     * @return The current user object, or null if invalid login
     */
    public User authenticate( String userName,
                              String password,
                              AuthenticationDomain domain,
                              com.webobjects.eocontrol.EOEditingContext ec
                            )
    {
        User user = null;
        try
        {
            User u = (User)EOUtilities.objectMatchingValues(
                ec, User.ENTITY_NAME,
                new NSDictionary(
                    new Object[]{ userName , domain              },
                    new Object[]{ User.USER_NAME_KEY,
                                  User.AUTHENTICATION_DOMAIN_KEY }
                ) );
            if ( skipPasswordChecks || password.equals( u.password() ) )
            {
                log.debug( "user " + userName + " validated" );
                user = u;
                if ( user.authenticationDomain() != domain )
                {
                    if ( user.authenticationDomain() == null )
                    {
                        user.setAuthenticationDomainRelationship( domain );
                        log.info( "user " + userName
                                  + " added to domain ("
                                  + domain.displayableName()
                                  + ")" );
                    }
                    else
                    {
                        log.warn(
                                "user " + userName
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
            else
            {
                log.info( "user " + userName + ": login validation failed" );
            }
        }
        catch ( EOObjectNotAvailableException e )
        {
            if ( addIfNotFound )
            {
                user = User.createUser(
                        userName,
                        password,
                        domain,
                        User.STUDENT_PRIVILEGES,
                        ec
                    );
                log.info( "DatabaseAuthenticator: new user '"
                          + userName
                          + "' created"
                        );
            }
        }
        catch ( EOUtilities.MoreThanOneException e )
        {
            log.error( "DatabaseAuthenticator: user '"
                       + userName
                       + "':",
                       e
                     );
        }

        return user;
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
        return true;
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
        boolean result = false;
        EOEditingContext ec = Application.newPeerEditingContext();
        try
        {
            ec.lock();
            User localUser =
                (User)EOUtilities.localInstanceOfObject( ec, user );
            localUser.setPassword( newPassword );
            ec.saveChanges();
            result = true;
        }
        finally
        {
            ec.unlock();
            Application.releasePeerEditingContext( ec );
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Generate a new random password.
     * @return The new password
     */
    public static String generatePassword()
    {
        StringBuffer password = new StringBuffer();

        // generate a random number
        for(int i = 0; i < DEFAULT_GENERATED_LENGTH; i++)
        {
            // now generate a random alpha-numeric for each position
            // in the password
            int index = randGen.nextInt( availChars.length() );
            password.append( availChars.charAt( index ) );
        }
        return password.toString();
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
        String newPass = generatePassword();
        if ( changePassword( user, newPass ) )
        {
            WCProperties properties =
                new WCProperties( Application.configurationProperties() );
            user.addPropertiesTo( properties );
            if ( properties.getProperty( "login.url" ) == null )
            {
                String dest = Application.application().servletConnectURL();
                properties.setProperty( "login.url", dest );
            }
            Application.sendSimpleEmail(
                user.email() ,
                properties.stringForKeyWithDefault(
                    "DatabaseAuthenticator.new.user.email.title",
                    "New Web-CAT Password" ),
                properties.stringForKeyWithDefault(
                    "DatabaseAuthenticator.new.user.email.message",
                    "Your Web-CAT user name is   : ${user.userName}\n"
                    + "Your new Web-CAT password is: ${user.password}\n\n"
                    + "You login to Web-CAT at:\n\n"
                    + "${login.url}\n\n"
                    + "You can change your password by logging into Web-CAT "
                    + "and visiting\nthe Home->My Profile page."
                    )
                );
            return true;
        }
        else
        {
            return false;
        }
    }


    //~ Instance/static variables .............................................

    private boolean addIfNotFound = false;
    private boolean skipPasswordChecks = false;

    private static final java.util.Random randGen = new java.util.Random();
    private static final int DEFAULT_GENERATED_LENGTH = 8;
    private static final String availChars =
        "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghikmnopqrstuvwxyz23456789!@#$%^&*";

    static Logger log = Logger.getLogger( DatabaseAuthenticator.class );
}
