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

import org.webcat.core.Application;
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
import com.webobjects.eocontrol.EOEnterpriseObject;

//-------------------------------------------------------------------------
/**
 * A request filter that performs HTTP basic authentication on a Git repository
 * URL, to validate the user against the Web-CAT user database.
 *
 * @author  Tony Allevato
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
        request.setUserInfoForKey(info, KEY_REQUEST_INFO);

        try
        {
            super.filterRequest(request, response, filterChain);
        }
        finally
        {
            currentRequestInfo.remove();
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
        EntityRequestInfo info = currentRequestInfo.get();
        if (info != null)
        {
            EOEnterpriseObject object = info.requestedObject(ec);

            if (object instanceof RepositoryProviderWithAuthentication)
            {
                User u = ((RepositoryProviderWithAuthentication)object)
                    .authorizedUserForRepository(ec, username, password);
                if (u != null)
                {
                    return u;
                }
            }
        }
        return super.validateUser(username, password, ec);
    }


    // ----------------------------------------------------------
    @Override
    protected boolean alsoCheckDbPassword() { return true; }


    //~ Static/instance variables .............................................

    private static final String KEY_REQUEST_INFO =
        "org.webcat.core.git.http.GitAuthenticationFilter.requestInfo";

    private static final ThreadLocal<EntityRequestInfo> currentRequestInfo =
        new ThreadLocal<EntityRequestInfo>();
}
