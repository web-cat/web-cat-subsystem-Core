/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2014 Virginia Tech
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
import com.webobjects.foundation.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import org.webcat.core.Application;
import org.webcat.core.AuthenticationDomain;
import org.webcat.core.DatabaseAuthenticator;
import org.webcat.core.User;
import org.webcat.core.UserAuthenticator;
import org.webcat.core.WCProperties;
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
 *  @author  Last changed by $Author$
 *  @version $Revision$, $Date$
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
    public boolean configure(String       baseName,
                             WCProperties properties)
    {
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
     * @return The current user object, or null if invalid login
     */
    public User authenticate(String userName,
                             String password,
                             AuthenticationDomain domain,
                             com.webobjects.eocontrol.EOEditingContext ec)
    {
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
            boolean passwordMatches = skipPasswordChecks;
            if (!skipPasswordChecks)
            {
                if (u.salt() == null)
                {
                    // Need to migrate user to encrypted password
                    changePassword(u, u.password());
                }
                if (password == null)
                {
                    password = "";
                }
                byte[] salt = fromHex(u.salt());
                byte[] hash = fromHex(u.password());
                // Compute the hash of the provided password, using the
                // same salt, iteration count, and hash length
                byte[] testHash = pbkdf2(
                    password.toCharArray(), salt, u.iterations(), hash.length);
                // Compare the hashes in constant time. The password is
                // correct if both hashes match.
                passwordMatches = slowEquals(hash, testHash);
            }
            if (passwordMatches)
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
    public boolean changePassword(final User   user,
                                  final String newPassword)
    {
        final String password = newPassword == null
            ? ""
            : newPassword;
        return call(new ECActionWithResult<Boolean>() {
            @Override
            public Boolean action()
            {
                try
                {
                    User localUser = user.localInstance(ec);

                    // Generate a random salt
                    SecureRandom random = new SecureRandom();
                    byte[] salt = new byte[SALT_BYTE_SIZE];
                    random.nextBytes(salt);

                    // Hash the password
                    byte[] hash = pbkdf2(password.toCharArray(), salt,
                        PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
                    localUser.setIterations(PBKDF2_ITERATIONS);
                    localUser.setSalt(toHex(salt));
                    localUser.setPassword(toHex(hash));
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
            Application.sendSimpleEmail(
                user.email(),
                properties.stringForKeyWithDefault(
                    "DatabaseAuthenticator.new.user.email.title",
                    "New Web-CAT Password"),
                properties.stringForKeyWithDefault(
                    "DatabaseAuthenticator.new.user.email.message",
                    "Your Web-CAT user name is   : ${user.userName}\n"
                    + "Your new Web-CAT password is: ${user.password}\n\n"
                    + "You login to Web-CAT at:\n\n"
                    + "${login.url}\n\n"
                    + "You can change your password by logging into Web-CAT "
                    + "and visiting\nthe Home->My Profile page."));
            return true;
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param   array  the byte array to convert
     * @return         A length*2 character string encoding the byte array
     */
    private static String toHex(byte[] array)
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0)
        {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        }
        else
        {
            return hex;
        }
    }


    // ----------------------------------------------------------
    /**
     * Converts a string of hexadecimal characters into a byte array.
     *
     * @param   hex  the hex string
     * @return       the hex string decoded into a byte array
     */
    private static byte[] fromHex(String hex)
    {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++)
        {
            binary[i] = (byte)Integer.parseInt(
                hex.substring(2 * i, 2 * i + 2), 16);
        }
        return binary;
    }


    // ----------------------------------------------------------
    /**
     * Compares two byte arrays in length-constant time. This comparison
     * method is used so that password hashes cannot be extracted from
     * an on-line system using a timing attack and then attacked off-line.
     *
     * @param   a  the first byte array
     * @param   b  the second byte array
     * @return     true if both byte arrays are the same, false if not
     */
    private static boolean slowEquals(byte[] a, byte[] b)
    {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++)
        {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }


    // ----------------------------------------------------------
    /**
     *  Computes the PBKDF2 hash of a password.
     *
     * @param   password    the password to hash.
     * @param   salt        the salt
     * @param   iterations  the iteration count (slowness factor)
     * @param   bytes       the length of the hash to compute in bytes
     * @return              the PBDKF2 hash of the password
     */
    private static byte[] pbkdf2(
        char[] password, byte[] salt, int iterations, int bytes)
    {
        try
        {
            PBEKeySpec spec =
                new PBEKeySpec(password, salt, iterations, bytes * 8);
            SecretKeyFactory skf =
                SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        }
        catch (Exception e)
        {
            log.error("Unexpected exception computing pbkdf2 hash:", e);
            throw new RuntimeException(e);
        }
    }


    //~ Instance/static variables .............................................

    private boolean addIfNotFound = false;
    private boolean skipPasswordChecks = false;

    private static final java.util.Random randGen = new java.util.Random();
    private static final int DEFAULT_GENERATED_LENGTH = 8;
    private static final String availChars =
        "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghikmnopqrstuvwxyz23456789!@#$%^&*";

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    // The following constants may be changed without breaking existing hashes.
    private static final int SALT_BYTE_SIZE = 24;
    private static final int HASH_BYTE_SIZE = 24;
    private static final int PBKDF2_ITERATIONS = 1000;

    static Logger log = Logger.getLogger( DatabaseAuthenticator.class );
}
