/*==========================================================================*\
 |  Copyright (C) 2018 Virginia Tech
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.webobjects.eoaccess.*;
import org.apache.log4j.Logger;

// --------------------------------------------------------------------------
/**
 *  A concrete implementation of <code>UserAuthenticator</code> that
 *  uses CAS for authentication.
 *
 *  @author  Stephen Edwards
 */
public class CasAuthenticator
    extends LdapAuthenticator
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new object.
     */
    public CasAuthenticator()
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
    public boolean configure(String baseName, WCProperties properties)
    {
        boolean result = super.configure(baseName, properties);
        baseUrl = properties.getProperty( baseName + ".cas.baseUrl" );
        if (baseUrl == null || baseUrl.equals(""))
        {
            log.error("a required property is not set: "
                + baseName + ".cas.baseUrl");
            result = false;
        }
        else
        {
            if (!baseUrl.endsWith("/"))
            {
                baseUrl += "/";
            }
        }
        loginUrl = baseUrl + "login";
        validationUrl = baseUrl + "serviceValidate";
        log.debug(baseName + ": baseUrl = " + baseUrl);

        return result;
    }


    // ----------------------------------------------------------
    /**
     * Validate a CAS user by validating a single-use service ticket.
     * Although this method uses the same name as for other authenticators,
     * it is actually a ticket validator instead of a password validator.
     * The `username' should be the service URL against which the ticket was
     * generated--the actual username is retrieved from the ticket
     * validation response if validation is successful.
     *
     * Should not be called until the authenticator has been configured.
     *
     * @param returnUrl Provide the Web-CAT service URL used to generate the
     *                  service ticket
     * @param ticket    The CAS service ticket to validate
     * @param domain    The authentication domain associated with this check
     * @param ec        The editing context to use
     * @param ls        An existing login session associated with this
     *                  user, if one exists (or null, if not)
     * @return The current user object, or null if invalid login
     */
    public User authenticate(
        String               returnUrl,
        String               ticket,
        AuthenticationDomain domain,
        com.webobjects.eocontrol.EOEditingContext ec,
        LoginSession ls)
    {
        if (returnUrl != null && !returnUrl.startsWith("https://"))
        {
            return super.authenticate(returnUrl, ticket, domain, ec, ls);
        }
        User user = null;
        log.debug("authenticate(), ticket = " + ticket + ", session = " + ls);
        String username = validateTicket(ticket, returnUrl);
        if (username != null)
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
                    if (domain.defaultEmailDomain() != null)
                    {
                        user.setEmail(
                            username + "@" + domain.defaultEmailDomain());
                        ec.saveChanges();
                    }
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
            log.info("ticket " + ticket+ "(" + domain.displayableName()
                + "): validation failed");
        }

        return user;
    }


    // ----------------------------------------------------------
    /**
     * Get the login URL for the associated CAS server where users should
     * be redirected for login.
     *
     * @param returnUrl The application (Web-CAT) service URL where the
     *                  CAS server will redirect the user after authentication
     *                  (usually the login page URL).
     *
     * @return The URL for CAS login where the user should be redirected.
     */
    public String casLoginUrl(String returnUrl)
    {
        try
        {
            return loginUrl + "?service="
                + URLEncoder.encode(returnUrl, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            log.error("Cannot encode in UTF-8!", e);
            return loginUrl;
        }
    }


    // ----------------------------------------------------------
    private String validateTicket(String ticket, String returnUrl)
    {
        String response = null;
        try
        {
            // given a url open a connection
            URLConnection c = new URL(validationUrl
                + "?ticket="
                + URLEncoder.encode(ticket, "UTF-8")
                + "&service="
                + URLEncoder.encode(returnUrl, "UTF-8")).openConnection();

            // set connection timeout to 5 sec and read timeout to 10 sec
            c.setConnectTimeout(5000);
            c.setReadTimeout(10000);

            // get a stream to read data from
            BufferedReader in = new BufferedReader(
                new InputStreamReader(c.getInputStream()));
            StringBuilder content = new StringBuilder(1024);
            String line = in.readLine();
            while (line != null)
            {
                content.append(line);
                content.append('\n');
                line = in.readLine();
            }

            response = content.toString();

            in.close();
        }
        catch (IOException e)
        {
            log.error(e);
        }
        log.debug("validation response: " + response);
        if (response != null
            && response.contains("cas:authenticationSuccess"))
        {
            // <cas:user>stedwar2</cas:user>
            Matcher m = USER_PATTERN.matcher(response);
            if (m.find())
            {
                return m.group(1).trim();
            }
        }
        return null;
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger(CasAuthenticator.class);
    private String baseUrl;
    private String loginUrl;
    private String validationUrl;
    private static Pattern USER_PATTERN = Pattern.compile(
        "<cas:user>([^<>\\s]+)</cas:user>");
}
