/*==========================================================================*\
 |  $Id: InfoRefsRequestHandler.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011-2012 Virginia Tech
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.webcat.core.NSMutableDataOutputStream;
import org.webcat.core.http.RequestHandlerWithResponse;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * Handles requests for the {@code info/refs} file in the repository.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class InfoRefsRequestHandler
    implements RequestHandlerWithResponse
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Looks up the requested repository and appends its info/refs to the
     * response.
     *
     * @param request the request
     * @param response the response
     * @throws Exception if an error occurs
     */
    public void handleRequest(WORequest request, WOResponse response)
            throws Exception
    {
        response.setHeader("text/plain", "Content-Type");

        NSMutableDataOutputStream outputStream = new NSMutableDataOutputStream();

        Repository repo = RepositoryRequestUtils.repositoryFromRequest(request);
        //RevWalk walk = new RevWalk(repo);

        //try
        //{
            //final RevFlag ADVERTISED = walk.newFlag("ADVERTISED");

            final OutputStreamWriter out =
                new OutputStreamWriter(outputStream, Constants.CHARSET);

            final RefAdvertiser adv = new RefAdvertiser() {
                protected void writeOne(CharSequence line) throws IOException
                {
                    out.append(line.toString().replace(' ', '\t'));
                }

                protected void end()
                {
                    // Do nothing.
                }
            };

            adv.init(repo);
            //adv.init(walk, ADVERTISED);
            adv.setDerefTags(true);

            Map<String, Ref> refs = repo.getAllRefs();
            refs.remove(Constants.HEAD);
            adv.send(refs);
        //}
        //finally
        //{
            //walk.release();
        //}

        outputStream.close();
        response.appendContentData(outputStream.data());
    }
}
