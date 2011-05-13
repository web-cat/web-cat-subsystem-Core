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

package org.webcat.core.git.http;

import org.eclipse.jgit.lib.ObjectDatabase;
import org.eclipse.jgit.storage.file.ObjectDirectory;
import org.eclipse.jgit.storage.file.PackFile;
import org.webcat.core.http.RequestHandlerWithResponse;
import org.webcat.core.http.RequestUtils;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * Handles requests for the {@code info/packs} file in the repository.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class InfoPacksRequestHandler implements RequestHandlerWithResponse
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Looks up the requested repository and appends its info/packs to the
     * response.
     *
     * @param request the request
     * @param response the response
     * @throws Exception if an error occurs
     */
    public void handleRequest(WORequest request, WOResponse response)
        throws Exception
    {
        StringBuilder out = new StringBuilder();

        ObjectDatabase db = RepositoryRequestUtils.repositoryFromRequest(
                request).getObjectDatabase();
        if (db instanceof ObjectDirectory)
        {
            for (PackFile pack : ((ObjectDirectory) db).getPacks())
            {
                out.append("P ");
                out.append(pack.getPackFile().getName());
                out.append('\n');
            }
        }

        out.append('\n');

        RequestUtils.sendPlainText(out.toString(), request, response);
    }
}
