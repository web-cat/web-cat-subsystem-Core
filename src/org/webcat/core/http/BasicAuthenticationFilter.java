/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011 Virginia Tech
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

package org.webcat.core.http;

import org.apache.http.HttpStatus;
import org.eclipse.jgit.util.HttpSupport;
import org.webcat.core.Application;
import org.webcat.core.EOBase;
import org.webcat.core.LoginSession;
import org.webcat.core.Session;
import org.webcat.core.User;
import org.webcat.woextensions.ECActionWithResult;
import static org.webcat.woextensions.ECActionWithResult.call;
import com.Ostermiller.util.Base64;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;

//-------------------------------------------------------------------------
/**
 * <p>
 * A basic authentication filter that uses the Web-CAT user database to
 * authenticate users. The following authentication attempts are made:
 * </p>
 * <ol>
 * <li>The request's "X-Session-Id" header is examined to see if it contains a
 * valid Web-CAT login session identifier. If so, that session is used. (This
 * supports the external web API.)</li>
 * <li>If that header does not exist, the standard WebObjects session
 * restoration is attempted (lookup the session ID in the "wosid" cookie). If
 * found, that session is used. (This supports browser-based navigation.)</li>
 * <li>If the cookie does not exist, authentication is attempted using HTTP
 * basic authentication. If authentication succeeds, the user is logged in and
 * a session is created. If there are no credentials associated with the
 * request, a 401 status code is returned.</li>
 * </ol>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public abstract class BasicAuthenticationFilter
     implements RequestFilter
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Filters the request, only passing it further down the chain if
     * authentication was successful.
     *
     * @param request the request
     * @param response the response
     * @param filterChain the filter chain
     * @throws Exception if an error occurs
     */
    public void filterRequest(WORequest request, WOResponse response,
            RequestFilterChain filterChain) throws Exception
    {
        existingSessionId = null;

        Session session = sessionFromContext(request.context());

        try
        {
            if (session != null
                    && isRequestValid(request)
                    && userHasAccess(session.primeUser()))
            {
                filterChain.filterRequest(request, response);
            }
            else if (response.status() != HttpStatus.SC_UNAUTHORIZED)
            {
                generateAuthFailedResponse(response);
            }
        }
        finally
        {
            if (session != null)
            {
                Application.wcApplication().saveSessionForContext(
                        request.context());
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the request is valid (usually based on
     * the part of the path that follows the request handler key). If the path
     * is invalid, the authentication filter will return a 403 Forbidden status
     * code.
     *
     * The default behavior is to return true for all requests; subclasses can
     * override this to more intelligently handle malformed paths.
     *
     * @param request the request
     * @return true if the request is valid, otherwise false
     */
    protected boolean isRequestValid(WORequest request)
    {
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Gets the realm that should be displayed in the authentication dialog
     * used by the browser or client. This should be an informative string such
     * as "Web-CAT Git repository for [user]".
     *
     * @param context the request/response context
     * @return the realm
     */
    protected abstract String realmForContext(WOContext context);


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the specified user has access to the
     * path being requested.
     *
     * @param user the user
     * @return if the user has access to the path, otherwise false
     */
    protected abstract boolean userHasAccess(User user);


    // ----------------------------------------------------------
    /**
     * Generates a response indicating that authentication failed.
     *
     * @param response the response to perform generation in
     */
    private void generateAuthFailedResponse(WOResponse response)
    {
        response.appendContentString(
                "<html><body><h1>Authentication failed</h1></body></html>");
        response.setStatus(WOMessage.HTTP_STATUS_FORBIDDEN);
    }


    // ----------------------------------------------------------
    /**
     * Gets the session for the user making the request, creating it if it does
     * not yet exist (for example, if the user is logging in directly through
     * the Git URL).
     *
     * @param info the entity request info
     */
    private Session sessionFromContext(final WOContext context)
    {
        final WORequest request = context.request();
        final WOResponse response = context.response();

        String sessionId = request.headerForKey(SESSION_ID_HEADER);

        if (sessionId == null)
        {
            sessionId = context._requestSessionID();
        }

        Session session = (sessionId != null)
            ? // Use an existing session if we have one.
            (Session) Application.wcApplication()
                .restoreSessionWithID(sessionId, request.context())
            : null;

        if (session == null)
        {
            String authorization = request.headerForKey(
                    HttpSupport.HDR_AUTHORIZATION);

            if (authorization == null)
            {
                String realm = realmForContext(context);

                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                response.setHeader("Basic realm=\"" + realm + "\"",
                        HttpSupport.HDR_WWW_AUTHENTICATE);
            }
            else
            {
                authorization = Base64.decode(authorization.substring(6));
                String[] parts = authorization.split(":");

                if (parts != null && parts.length >= 2)
                {
                    final String username = parts[0];
                    final String password =
                        (parts.length > 1) ? parts[1] : null;

                    final Session oldSession = session;
                    session = call(new ECActionWithResult<Session>()
                    {
                        public Session action()
                        {
                            Session result = oldSession;
                            User user = validateUser(username, password, ec);

                            if (user == null)
                            {
                                Application.wcApplication()
                                    .saveSessionForContext(context);
                            }
                            else
                            {
                                context._setRequestSessionID(existingSessionId);
                                result = (Session)context.session();
                                result.setUser(user.localInstance(
                                    result.defaultEditingContext()));
                                result._appendCookieToResponse(response);
                            }
                            return result;
                        }
                    });
                }
            }
        }

        return session;
    }


    // ----------------------------------------------------------
    /**
     * Looks up a user with the specified username (in the format "username" or
     * "domain.username") and, if found, validates the user with the specified
     * password.
     *
     * @param username the username
     * @param password the password
     * @param session the session to associate with the user
     * @return true if the user was successfully authenticated, otherwise false
     */
    private User validateUser(String username, String password,
            EOEditingContext ec)
    {
        // Look up the user based on the repository ID.
        User user = EOBase.objectWithApiId(ec, User.class, username);

        // Validate the user's password.
        if (user != null)
        {
            user = User.validate(user.userName(), password,
                    user.authenticationDomain(), ec);
        }

        if (user != null)
        {
            LoginSession ls = LoginSession.getLoginSessionForUser(ec, user);

            if (ls != null)
            {
                // Remember the existing session id for restoration.
                existingSessionId = ls.sessionId();
            }

            return user;
        }
        else
        {
            return null;
        }
    }


    //~ Static/instance variables .............................................

    private static final String SESSION_ID_HEADER = "X-Session-Id";

    private String existingSessionId;
}
