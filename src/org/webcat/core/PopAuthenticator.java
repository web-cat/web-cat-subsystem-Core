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

import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;
import java.io.*;
import java.net.*;
import javax.net.*;
import javax.net.ssl.*;
import org.webcat.core.AuthenticationDomain;
import org.webcat.core.PopAuthenticator;
import org.webcat.core.User;
import org.webcat.core.UserAuthenticator;
import org.webcat.core.WCProperties;
import org.webcat.woextensions.WCEC;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  A concrete implementation of <code>UserAuthenticator</code> that
 *  tests user ids/passwords against a POP3 server.
 *  This implementation uses the application properties
 *  <code>authenticator.POPserver.name</code> and
 *  <code>authenticator.POPserver.port</code> to determine the
 *  POP server to use for authentication.
 *
 *  @author  Stephen Edwards
 */
public class PopAuthenticator
    implements UserAuthenticator
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new PopAuthenticator object.
     */
    public PopAuthenticator()
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
    public boolean configure(String baseName, WCProperties properties)
    {
        boolean result = true;

        server = properties.getProperty(baseName + ".POPserver.name");
        if (server == null || server.equals(""))
        {
            log.error("a required property is not set: "
                + baseName + ".POPserver.name");
            result = false;
        }

        port = properties.intForKey(baseName + ".POPserver.port");
        if (port == 0)
        {
            log.error("a required property is not set: "
                       + baseName + ".POPserver.port\n");
            result = false;
        }

        boolean secure = properties.booleanForKey(
            baseName + ".POPserver.useSSL");
        socketFactory = secure
            ? SSLSocketFactory.getDefault()
            : SocketFactory.getDefault();

        addIfNotFound = properties.booleanForKeyWithDefault(
            baseName + ".addIfNotFound", true);

        return result;
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
    public User authenticate(
        String userName,
        String password,
        AuthenticationDomain domain,
        WCEC ec,
        LoginSession ls)
    {
        User user = null;
        if (authenticate(userName, password))
        {
            log.debug("user " + userName + " validated");
            try
            {
                user = (User)EOUtilities.objectMatchingValues(
                    ec, User.ENTITY_NAME,
                    new NSDictionary<String, Object>(
                        new Object[]{ userName , domain              },
                        new String[]{ User.USER_NAME_KEY,
                                      User.AUTHENTICATION_DOMAIN_KEY }
                    ) );
                if (user.authenticationDomain() != domain)
                {
                    if (user.authenticationDomain() == null)
                    {
                        user.setAuthenticationDomainRelationship(domain);
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
            catch (EOObjectNotAvailableException e)
            {
                if (addIfNotFound)
                {
                    user = User.createUser(
                        userName,
                        null,  // DO NOT MIRROR PASSWORD IN DATABASE
                               // for security reasons
                        domain,
                        User.STUDENT_PRIVILEGES,
                        ec);
                    log.info("new user '" + userName + "' ("
                        + domain.displayableName() + ") created");
                }
                else
                {
                    log.info("user " + userName
                        + " successfully validated in '"
                        + domain.displayableName()
                        + "' but does not exist (not created)");
                }
            }
            catch (EOUtilities.MoreThanOneException e)
            {
                log.error("user '" + userName + "' ("
                    + domain.displayableName() + "):", e);
            }
        }
        else
        {
            log.info("user " + userName + "(" + domain.displayableName()
                + "): login validation failed");
        }

        return user;
    }


    // ----------------------------------------------------------
    /**
     * Connects to POP3 server via a socket and tries to validate the
     * username/password pair.
     * <pre>
     * protocol used : telnet
     * server type   : mail server
     * security      : none -- clear text transmissions
     *
     * RETURN CODES:
     * -------------------------------------------+------------
     * Event                                        Exit Code
     * -------------------------------------------+------------
     * Abnormal Termination of function           |    -1
     * PID,PSWD OK                                |    1
     * PID bad                                    |    2
     * PSWD bad                                   |    3
     * PSWD expired                               |    4
     * incorrect number of command line parameters|    5
     * auth server is down                        |    6
     * -------------------------------------------+------------
     * </pre>
     *
     * @param pid      The user name to use
     * @param password The password to check
     * @return         true if result code == rPidPswdGood, see table above
     */
    private boolean authenticate(String pid, String password)
    {
        // exit code
        int result = rAbnormalTermination;

        if (server == null) return result == rPidPswdGood;

        Socket socket = null;

        try
        {
            // create socket connection to pop server
            socket = socketFactory.createSocket(server, port);
            // stream which comes from server
            BufferedReader serverIn = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            //stream which goes out to server
            PrintStream meOut =
                new PrintStream(socket.getOutputStream());

            // converse with server... doing authentication

            // first -- read server's greeting line and check to see
            // if we made a connection
            String serverSaid = serverIn.readLine();
            if (serverSaid.indexOf( "+OK" ) != -1)
            {
                // send the user name
                meOut.println("user " + pid);

                // ignore the server's message to send password
                serverIn.readLine();

                // send the password
                meOut.println("pass " + password);

                // read the server's response
                serverSaid = serverIn.readLine();

                // close the connection
                meOut.println("quit");
                result = rPswdBad;

                if (serverSaid.indexOf( "+OK" ) != -1)
                {
                    result = rPidPswdGood;
                }

                if (serverSaid.indexOf( "Can't find your" ) != -1)
                {
                    result = rPidBad;
                }
            }
            serverIn.close();    //close connection to server
        }
        catch (IOException ioe)
        {
            log.error("Error talking to pop server", ioe);
            result = rServerError;
        }
        finally
        {
            if (socket != null)
            {
                try
                {
                    socket.close();
                }
                catch (IOException ioe)
                {
                    log.info("Error closing pop server socket", ioe);
                }
            }
        }
        return result == rPidPswdGood;
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger(PopAuthenticator.class);

    private int           port;
    private String        server;
    private SocketFactory socketFactory;
    private boolean       addIfNotFound;

    // return codes
    static final int rAbnormalTermination  = -1;
    static final int rPidPswdGood          = 1;
    static final int rPidBad               = 2;
    static final int rPswdBad              = 3;
    static final int rPswdExpired          = 4;
    static final int rInvalidCmdLineParam  = 5;
    static final int rServerError          = 6;
    static final int rCantUnderstandServer = 7;
}
