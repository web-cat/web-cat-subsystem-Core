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
 |
 *==========================================================================*
 |
 | Parts of this implementation are adapted from:
 |
 | Password Hashing With PBKDF2 (http://crackstation.net/hashing-security.htm).
 | Copyright (c) 2013, Taylor Hornby
 | All rights reserved.
 |
 | Redistribution and use in source and binary forms, with or without
 | modification, are permitted provided that the following conditions are met:
 |
 | 1. Redistributions of source code must retain the above copyright notice,
 | this list of conditions and the following disclaimer.
 |
 | 2. Redistributions in binary form must reproduce the above copyright notice,
 | this list of conditions and the following disclaimer in the documentation
 | and/or other materials provided with the distribution.
 |
 | THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 | AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 | IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 | ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 | LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 | CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 | SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 | INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 | CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 | ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 | POSSIBILITY OF SUCH DAMAGE.
\*==========================================================================*/

package org.webcat.core;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.*;
import org.webcat.woextensions.ECActionWithResult;
import static org.webcat.woextensions.ECActionWithResult.call;
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
 */
public class DatabaseAuthenticator
    implements PasswordManagingUserAuthenticator
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
        log.debug("DatabaseAuthenticator(): " + hashCode());
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
    public boolean configure(String       baseName,
                             WCProperties properties)
    {
        log.debug("DatabaseAuthenticator.configure(" + baseName + "): "
            + hashCode());
        addIfNotFound = properties.booleanForKey(baseName + ".addIfNotFound");
        skipPasswordChecks =
            properties.booleanForKey(baseName + ".skipPasswordChecks");
        log.debug(baseName + ".addIfNotFound = " + addIfNotFound);
        if (skipPasswordChecks)
        {
            log.warn(
                baseName + ".skipPasswordChecks = " + skipPasswordChecks);
        }
        else
        {
            log.debug(
                baseName + ".skipPasswordChecks = " + skipPasswordChecks);
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
     * @param ls       An existing login session associated with this
     *                 user, if one exists (or null, if not)
     * @return The current user object, or null if invalid login
     */
    public User authenticate(String userName,
                             String password,
                             AuthenticationDomain domain,
                             EOEditingContext ec,
                             LoginSession ls)
    {
        if (log.isDebugEnabled())
        {
            log.debug("DatabaseAuthenticator.authenticate(" + userName
                + ", " + password + ", " + domain.subdirName() + "): "
                + hashCode());
        }
        User user = null;
        try
        {
            User u = (User)EOUtilities.objectMatchingValues(
                ec, User.ENTITY_NAME,
                new NSDictionary<String, Object>(
                    new Object[]{ userName , domain              },
                    new String[]{ User.USER_NAME_KEY,
                                  User.AUTHENTICATION_DOMAIN_KEY }
                    ));
            if (skipPasswordChecks
                || u.checkPassword(password))
            {
                log.debug("user " + userName + " validated");
                user = u;
                if (user.authenticationDomain() != domain)
                {
                    if (user.authenticationDomain() == null)
                    {
                        user.setAuthenticationDomainRelationship(domain);
                        log.info("user " + userName + " added to domain ("
                            + domain.displayableName() + ")");
                    }
                    else
                    {
                        log.warn("user " + userName
                            + " successfully validated in '"
                            + domain.displayableName() + "' but bound to '"
                            + user.authenticationDomain().displayableName()
                            + "'");
                        user = null;
                    }
                }
            }
            else
            {
                log.info("user " + userName + ": login validation failed");
            }
        }
        catch (EOObjectNotAvailableException e)
        {
            if (addIfNotFound)
            {
                user = User.createUser(
                        userName,
                        password,
                        domain,
                        User.STUDENT_PRIVILEGES,
                        ec);
                log.info("DatabaseAuthenticator: new user '"
                    + userName + "' created");
            }
            else
            {
                log.debug("no user found matching username/domain");
            }
        }
        catch (EOUtilities.MoreThanOneException e)
        {
            log.error("DatabaseAuthenticator: user '" + userName + "':",
                e);
        }

        return user;
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
    public boolean changePassword(final User   user,
                                  final String newPassword)
    {
        return call(new ECActionWithResult<Boolean>() {
            @Override
            public Boolean action()
            {
                try
                {
                    User localUser = user.localInstance(ec);
                    localUser.setPassword(newPassword);
                    ec.saveChanges();
                    return true;
                }
                catch (Exception e)
                {
                    return false;
                }
            }
        });
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
        for (int i = 0; i < DEFAULT_GENERATED_LENGTH; i++)
        {
            // now generate a random alpha-numeric for each position
            // in the password
            int index = randGen.nextInt(availChars.length());
            password.append(availChars.charAt(index));
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
    public boolean newRandomPassword(User user)
    {
        String newPass = generatePassword();
        if (changePassword(user, newPass))
        {
            WCProperties properties =
                new WCProperties(Application.configurationProperties());
            user.addPropertiesTo(properties);
            if (properties.getProperty("login.url") == null)
            {
                String dest = Application.application().servletConnectURL();
                properties.setProperty("login.url", dest);
            }
            String institutionMsg = "";
            if (AuthenticationDomain.authDomainStubs().count() > 1)
            {
                institutionMsg = "When logging in, be sure to select "
                    + "\""
                    + user.authenticationDomain().displayableName()
                    + "\"\nas your institution.\n\n";
            }
            Application.sendSimpleEmail(
                user.email(),
                properties.stringForKeyWithDefault(
                    "DatabaseAuthenticator.new.user.email.title",
                    "New Web-CAT Password"),
                properties.stringForKeyWithDefault(
                    "DatabaseAuthenticator.new.user.email.message",
                    "Your Web-CAT user name is   : ${user.userName}\n"
                    + "Your new Web-CAT password is: "
                    + newPass
                    + "\n\n"
                    + "You login to Web-CAT at:\n\n"
                    + "${login.url}\n\n"
                    + institutionMsg
                    + "You can change your password by logging into Web-CAT "
                    + "and visiting\nthe Home->My Profile page."));
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
