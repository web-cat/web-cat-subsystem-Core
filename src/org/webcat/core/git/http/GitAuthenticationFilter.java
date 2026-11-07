/*==========================================================================*\
 |  Copyright (C) 2011-2021 Virginia Tech
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

package org.webcat.core.git.http;

import org.apache.log4j.Logger;
import org.webcat.core.AccessToken;
import org.webcat.core.Application;
import org.webcat.core.AuthenticationDomain;
import org.webcat.core.EntityRequestInfo;
import org.webcat.core.RepositoryProvider;
import org.webcat.core.RepositoryProviderWithAuthentication;
import org.webcat.core.User;
import org.webcat.core.http.BasicAuthenticationFilter;
import org.webcat.core.http.RequestFilterChain;
import org.webcat.woextensions.WCEC;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

//-------------------------------------------------------------------------
/**
 * A request filter that performs HTTP basic authentication on a Git repository
 * URL, to validate the user against the Web-CAT user database.
 *
 * <p>Supports authentication via personal access tokens with scope enforcement
 * ({@code git:read} for clone/fetch/read-only operations, {@code git:write} for
 * receive-pack/push operations). Double-checks that the provided username matches
 * the token owner by email, username, or legacy institution abbreviation + username.</p>
 *
 * @author  Tony Allevato
 * @author  Stephen Edwards
 */
public class GitAuthenticationFilter extends BasicAuthenticationFilter
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
    @Override
    public void filterRequest(WORequest request, WOResponse response,
            RequestFilterChain filterChain) throws Exception
    {
        String path = request.requestHandlerPath();
        EntityRequestInfo info = EntityRequestInfo.fromRequestHandlerPath(path);
        currentRequestInfo.set(info);
        currentRequest.set(request);
        request.setUserInfoForKey(info, KEY_REQUEST_INFO);

        try
        {
            super.filterRequest(request, response, filterChain);
        }
        finally
        {
            currentRequestInfo.remove();
            currentRequest.remove();
            request.setUserInfoForKey(null, KEY_REQUEST_INFO);
        }
    }


    // ----------------------------------------------------------
    private EntityRequestInfo getRequestInfo(WORequest request)
    {
        EntityRequestInfo info = null;
        if (request != null)
        {
            info = (EntityRequestInfo) request.userInfoForKey(KEY_REQUEST_INFO);
        }
        if (info == null)
        {
            info = currentRequestInfo.get();
        }
        return info;
    }


    // ----------------------------------------------------------
    @Override
    protected boolean isRequestValid(WORequest request)
    {
        return getRequestInfo(request) != null;
    }


    // ----------------------------------------------------------
    @Override
    protected String realmForContext(WOContext context)
    {
        EntityRequestInfo info = null;
        if (context != null)
        {
            info = getRequestInfo(context.request());
        }
        else
        {
            info = currentRequestInfo.get();
        }

        if (info != null)
        {
            return "Web-CAT Git repository "
                + info.entityName() + "/" + info.objectID()
                + " on " + Application.wcApplication().host();
        }
        else
        {
            return "Web-CAT Git repositories on "
                + Application.wcApplication().host();
        }
    }


    // ----------------------------------------------------------
    @Override
    protected boolean userHasAccess(User user)
    {
        EntityRequestInfo info = currentRequestInfo.get();
        if (info == null)
        {
            return false;
        }

        EOEnterpriseObject object = info.requestedObject(
                user.editingContext());

        if (object instanceof RepositoryProvider)
        {
            RepositoryProvider provider = (RepositoryProvider)object;

            return (user.hasAdminPrivileges() ||
                    provider.userCanAccessRepository(user));
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    @Override
    protected User validateUser(String username, String password, WCEC ec)
    {
        if (password == null || password.trim().isEmpty())
        {
            return null;
        }

        WORequest req = currentRequest.get();

        // 1. Check if the password provided is a personal access token
        AccessToken token = AccessToken.findToken(ec, password);
        if (token != null)
        {
            if (!token.isValid())
            {
                log.warn("Git auth failed: access token is expired or revoked ("
                    + token.maskedToken() + ")");
                return null;
            }

            // Check that token grants the required Git scope for this operation
            String requiredScope = requiredScopeForRequest(req);
            boolean hasScope = token.hasScope(requiredScope)
                || (AccessToken.SCOPE_GIT_READ.equals(requiredScope)
                    && token.hasScope(AccessToken.SCOPE_GIT_WRITE));

            if (!hasScope)
            {
                log.warn("Git auth failed: access token (" + token.maskedToken()
                    + ") lacks required scope " + requiredScope);
                return null;
            }

            // Double-check the user name
            User tokenUser = token.user();
            if (tokenUser == null || !matchesUser(tokenUser, username, ec))
            {
                log.warn("Git auth failed: provided username '" + username
                    + "' does not match access token owner ("
                    + (tokenUser != null ? tokenUser.userName() : "null") + ")");
                return null;
            }

            // Record audit usage for the token
            token.recordUse(clientIpAddress(req));
            try
            {
                ec.saveChanges();
            }
            catch (Exception e)
            {
                log.error("Failed to save access token audit usage", e);
            }

            return tokenUser;
        }

        // 2. Check RepositoryProviderWithAuthentication if custom repo auth is present
        EntityRequestInfo info = currentRequestInfo.get();
        if (info != null)
        {
            EOEnterpriseObject object = info.requestedObject(ec);

            if (object instanceof RepositoryProviderWithAuthentication)
            {
                User u = ((RepositoryProviderWithAuthentication) object)
                    .authorizedUserForRepository(ec, username, password);
                if (u != null)
                {
                    return u;
                }
            }
        }

        // 3. Password fallback for internal authentication domains only
        User user = findUserByIdentifier(ec, username);
        if (user != null && user.canChangePassword())
        {
            return User.validate(user.userName(), password,
                user.authenticationDomain(), ec);
        }

        return null;
    }


    // ----------------------------------------------------------
    /**
     * Determines whether the given Git request requires {@code git:write}
     * or {@code git:read} scope.
     *
     * @param request The current WORequest
     * @return {@link AccessToken#SCOPE_GIT_WRITE} for receive-pack/push requests;
     *         {@link AccessToken#SCOPE_GIT_READ} otherwise
     */
    public static String requiredScopeForRequest(WORequest request)
    {
        if (request != null)
        {
            String service = (String)request.formValueForKey("service");
            String path = request.requestHandlerPath();

            if ("git-receive-pack".equals(service)
                || (path != null && path.endsWith("git-receive-pack")))
            {
                return AccessToken.SCOPE_GIT_WRITE;
            }
        }
        return AccessToken.SCOPE_GIT_READ;
    }


    // ----------------------------------------------------------
    /**
     * Double-checks that the provided username identifier matches the given User.
     * The identifier may be any of:
     * <ul>
     *   <li>User email address (e.g. "edwards@cs.vt.edu")</li>
     *   <li>Username (e.g. "edwards")</li>
     *   <li>Legacy institution abbreviation + username (e.g. "VT.edwards")</li>
     * </ul>
     *
     * @param user The User entity associated with the access token
     * @param usernameInput The username string supplied by the client
     * @param ec The editing context
     * @return true if the identifier matches the user
     */
    public static boolean matchesUser(User user, String usernameInput,
            EOEditingContext ec)
    {
        if (user == null || usernameInput == null)
        {
            return false;
        }
        String trimmed = usernameInput.trim();
        if (trimmed.isEmpty())
        {
            return false;
        }

        // 1. Check user email (e.g. "edwards@cs.vt.edu")
        if (user.email() != null && user.email().equalsIgnoreCase(trimmed))
        {
            return true;
        }

        // 2. Check plain username (e.g. "edwards")
        if (user.userName() != null && user.userName().equalsIgnoreCase(trimmed))
        {
            return true;
        }

        // 3. Check legacy institution abbreviation + user name (e.g. "VT.edwards")
        if (user.apiId() != null && user.apiId().equalsIgnoreCase(trimmed))
        {
            return true;
        }

        int dot = trimmed.indexOf('.');
        if (dot > 0 && dot < trimmed.length() - 1)
        {
            String prefix = trimmed.substring(0, dot);
            String suffix = trimmed.substring(dot + 1);

            if (suffix.equalsIgnoreCase(user.userName()))
            {
                AuthenticationDomain domain = user.authenticationDomain();
                if (domain != null)
                {
                    if (prefix.equalsIgnoreCase(domain.name())
                        || prefix.equalsIgnoreCase(domain.subdirName()))
                    {
                        return true;
                    }
                }

                AuthenticationDomain resolved =
                    AuthenticationDomain.authDomainBySubdirName(ec, prefix);
                if (resolved == null)
                {
                    resolved = AuthenticationDomain.authDomainByName(ec, prefix);
                }
                if (resolved != null && resolved.equals(domain))
                {
                    return true;
                }
            }
        }

        return false;
    }


    // ----------------------------------------------------------
    /**
     * Looks up a user matching any of: email, legacy institution.username, or plain username.
     *
     * @param ec The editing context
     * @param usernameInput The input string
     * @return The matching User or null
     */
    public static User findUserByIdentifier(EOEditingContext ec, String usernameInput)
    {
        if (usernameInput == null || usernameInput.trim().isEmpty())
        {
            return null;
        }
        String trimmed = usernameInput.trim();

        // 1. Try email lookup
        if (trimmed.contains("@"))
        {
            NSArray<User> users = User.objectsMatchingValues(ec, User.EMAIL_KEY, trimmed);
            if (users != null && users.count() == 1)
            {
                return users.objectAtIndex(0);
            }
        }

        // 2. Try legacy institution.username or standard apiId lookup
        User user = User.findObjectWithApiId(ec, trimmed);
        if (user != null)
        {
            return user;
        }

        // 3. Try plain username lookup
        try
        {
            user = User.userWithName(ec, trimmed);
            if (user != null)
            {
                return user;
            }
        }
        catch (Exception e)
        {
            log.warn("Multiple users match username '" + trimmed + "'");
        }

        return null;
    }


    // ----------------------------------------------------------
    private static String clientIpAddress(WORequest request)
    {
        if (request == null)
        {
            return null;
        }
        String xff = request.headerForKey("x-forwarded-for");
        if (xff != null && !xff.trim().isEmpty())
        {
            int comma = xff.indexOf(',');
            return (comma > 0) ? xff.substring(0, comma).trim() : xff.trim();
        }
        return request._remoteAddress();
    }


    //~ Static/instance variables .............................................

    private static final Logger log =
        Logger.getLogger(GitAuthenticationFilter.class);

    private static final String KEY_REQUEST_INFO =
        "org.webcat.core.git.http.GitAuthenticationFilter.requestInfo";

    private static final ThreadLocal<EntityRequestInfo> currentRequestInfo =
        new ThreadLocal<EntityRequestInfo>();

    private static final ThreadLocal<WORequest> currentRequest =
        new ThreadLocal<WORequest>();
}
