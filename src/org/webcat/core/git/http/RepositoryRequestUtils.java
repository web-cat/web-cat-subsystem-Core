/*==========================================================================*\
 |  $Id: RepositoryRequestUtils.java,v 1.2 2012/06/22 16:23:17 aallowat Exp $
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

package org.webcat.core.git.http;

import org.eclipse.jgit.lib.Repository;
import org.webcat.core.EOBase;
import org.webcat.core.EntityRequestInfo;
import org.webcat.core.Session;
import org.webcat.core.git.GitRepository;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

//-------------------------------------------------------------------------
/**
 * Helper methods for working with repository-related requests.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2012/06/22 16:23:17 $
 */
public class RepositoryRequestUtils
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Prevent instantiation.
     */
    private RepositoryRequestUtils()
    {
        // Do nothing.
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Looks up the repository associated with the specified request and stores
     * it in the request's user-info dictionary.
     *
     * @param request the request
     * @return the repository associated with the request, or null if one was
     *     not found
     */
    public static Repository cacheRepositoryInRequest(WORequest request)
    {
        String path = request.requestHandlerPath();
        EntityRequestInfo info = EntityRequestInfo.fromRequestHandlerPath(path);

        Repository repo = null;

        if (info != null)
        {
            Session session = (Session) request.context().session();
            EOEditingContext ec = session.defaultEditingContext();

            EOBase object = info.requestedObject(ec);
            repo = GitRepository.repositoryForObject(object).repository();
        }

        request.setUserInfoForKey(repo, KEY_REPOSITORY);
        return repo;
    }


    // ----------------------------------------------------------
    /**
     * Gets the cached repository from the request's user-info dictionary.
     *
     * @param request the request
     * @return the cached repository for the request, or null if there was none
     */
    public static Repository repositoryFromRequest(WORequest request)
    {
        return (Repository) request.userInfoForKey(KEY_REPOSITORY);
    }


    // ----------------------------------------------------------
    /**
     * Removes the cached repository from the request's user-info dictionary.
     *
     * @param request the request
     */
    public static void clearRepositoryInRequest(WORequest request)
    {
        request.setUserInfoForKey(null, KEY_REPOSITORY);
    }


    //~ Static/instance variables .............................................

    public static final String KEY_REPOSITORY =
        "org.webcat.core.git.RequestRepository";
}
