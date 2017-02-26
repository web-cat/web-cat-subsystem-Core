/*==========================================================================*\
 |  $Id$
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
import java.io.InputStream;
import java.util.Collection;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PostReceiveHook;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser;
import org.eclipse.jgit.util.HttpSupport;
import org.webcat.core.git.GitUtilities;
import org.webcat.core.http.RequestHandlerWithResponse;
import org.webcat.core.http.RequestUtils;
import org.webcat.core.http.SmartGZIPOutputStream;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * Handles Git Smart HTTP requests for the {@code git-receive-pack} service.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class ReceivePackRequestHandler
    implements RequestHandlerWithResponse
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Handles the request and generates the ReceivePack response.
     *
     * @param request the request
     * @param response the response
     * @throws Exception if an error occurs
     */
    public void handleRequest(WORequest request, WOResponse response)
        throws Exception
    {
        long start = System.currentTimeMillis();
        String contentType = request.headerForKey(HttpSupport.HDR_CONTENT_TYPE);

        if (RECEIVE_PACK_REQUEST_TYPE.equals(contentType))
        {
            final Repository repo =
                RepositoryRequestUtils.repositoryFromRequest(request);

            ReceivePack rp = new ReceivePack(repo);
            rp.setBiDirectionalPipe(false);
            rp.setPostReceiveHook(new PostReceiveHook() {
                public void onPostReceive(ReceivePack pack,
                        Collection<ReceiveCommand> commands)
                {
                    // Once the pack is received, force a pull of the working
                    // copy so that anyone using Web-DAV will get synched.
                    GitUtilities.workingCopyForRepository(repo, true);
                }
            });

            response.setHeader(RECEIVE_PACK_RESULT_TYPE,
                    HttpSupport.HDR_CONTENT_TYPE);

            InputStream input = RequestUtils.inputStreamForRequest(request);
            SmartGZIPOutputStream output =
                new SmartGZIPOutputStream(request, response);

            rp.receive(input, output, null);

            output.close();
        }
        else
        {
            response.setStatus(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
        }
        long end = System.currentTimeMillis();
        if (log.isDebugEnabled())
        {
            double time = (end - start)/1000.0;
            log.debug("handleRequest(): " + time + "s\n    url = "
                + request.uri());
        }
    }


    //~ Inner classes .........................................................

    // ----------------------------------------------------------
    /**
     * Prepends info/refs advertisements for the {@code git-receive-pack}
     * service to a response.
     */
    public static class InfoRefs extends SmartHttpInfoRefsFilter
    {
        //~ Constructors ......................................................

        // ----------------------------------------------------------
        /**
         * Creates a new filter for the {@code git-receive-pack} service.
         */
        public InfoRefs()
        {
            super("git-receive-pack");
        }


        //~ Methods ...........................................................

        // ----------------------------------------------------------
        @Override
        protected void advertise(WORequest request, Repository repository,
                PacketLineOutRefAdvertiser advertiser) throws IOException
        {
            ReceivePack rp = new ReceivePack(repository);

            try
            {
                rp.sendAdvertisedRefs(advertiser);
            }
            finally
            {
                rp.getRevWalk().release();
            }
        }
    }


    //~ Static/instance variables .............................................

    private static final String RECEIVE_PACK_REQUEST_TYPE =
        "application/x-git-receive-pack-request";

    private static final String RECEIVE_PACK_RESULT_TYPE =
        "application/x-git-receive-pack-result";

    private static final Logger log =
        Logger.getLogger(ReceivePackRequestHandler.class);
}
