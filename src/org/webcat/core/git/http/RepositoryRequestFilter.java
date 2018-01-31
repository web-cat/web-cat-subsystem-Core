/*==========================================================================*\
 |  $Id: RepositoryRequestFilter.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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
import org.webcat.core.http.RequestFilter;
import org.webcat.core.http.RequestFilterChain;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * A request filter that looks up the requested repository and puts it into the
 * request's user-info dictionary for the duration of the request, for
 * convenience in other request handlers.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class RepositoryRequestFilter implements RequestFilter
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Looks up the requested repository and puts it into the request's
     * user-info dictionary.
     *
     * @param request the request
     * @param response the response
     * @param filterChain the filter chain
     * @throws Exception if an error occurs
     */
    public void filterRequest(WORequest request, WOResponse response,
            RequestFilterChain filterChain) throws Exception
    {
        Repository repo = RepositoryRequestUtils.repositoryFromRequest(request);

        if (repo != null)
        {
            throw new IllegalStateException("Repository userInfo key was "
                    + "already set.");
        }
        else
        {
            repo = RepositoryRequestUtils.cacheRepositoryInRequest(request);

            try
            {
                filterChain.filterRequest(request, response);
            }
            finally
            {
                RepositoryRequestUtils.clearRepositoryInRequest(request);
                repo.close();
            }
        }
    }
}
