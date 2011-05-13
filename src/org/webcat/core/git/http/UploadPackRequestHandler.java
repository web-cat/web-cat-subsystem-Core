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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser;
import org.eclipse.jgit.util.HttpSupport;
import org.webcat.core.NSMutableDataOutputStream;
import org.webcat.core.http.RequestHandlerWithResponse;
import org.webcat.core.http.RequestUtils;
import org.webcat.core.http.SmartGZIPOutputStream;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * Handles Git Smart HTTP requests for the {@code git-upload-pack} service.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class UploadPackRequestHandler implements RequestHandlerWithResponse
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Handles the request and generates the UploadPack response.
     *
     * @param request the request
     * @param response the response
     * @throws Exception if an error occurs
     */
    public void handleRequest(WORequest request, WOResponse response)
        throws Exception
    {
        String contentType = request.headerForKey(HttpSupport.HDR_CONTENT_TYPE);

        if (UPLOAD_PACK_REQUEST_TYPE.equals(contentType))
        {
            Repository repo =
                RepositoryRequestUtils.repositoryFromRequest(request);

            UploadPack up = new UploadPack(repo);
            up.setBiDirectionalPipe(false);

            response.setHeader(UPLOAD_PACK_RESULT_TYPE,
                    HttpSupport.HDR_CONTENT_TYPE);

            InputStream input = RequestUtils.inputStreamForRequest(request);
            SmartGZIPOutputStream output =
                new SmartGZIPOutputStream(request, response);

            up.upload(input, output, null);

            output.close();
        }
        else
        {
            response.setStatus(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
        }
    }


    //~ Inner classes .........................................................

    // ----------------------------------------------------------
    /**
     * Prepends info/refs advertisements for the {@code git-upload-pack}
     * service to a response.
     */
    public static class InfoRefs extends SmartHttpInfoRefsFilter
    {
        //~ Constructors ......................................................

        // ----------------------------------------------------------
        /**
         * Creates a new filter for the {@code git-upload-pack} service.
         */
        public InfoRefs()
        {
            super("git-upload-pack");
        }


        //~ Methods ...........................................................

        // ----------------------------------------------------------
        @Override
        protected void advertise(WORequest request, Repository repository,
                PacketLineOutRefAdvertiser advertiser) throws IOException
        {
            UploadPack up = new UploadPack(repository);

            try
            {
                up.sendAdvertisedRefs(advertiser);
            }
            finally
            {
                up.getRevWalk().release();
            }
        }
    }


    //~ Static/instance variables .............................................

    private static final String UPLOAD_PACK_REQUEST_TYPE =
        "application/x-git-upload-pack-request";

    private static final String UPLOAD_PACK_RESULT_TYPE =
        "application/x-git-upload-pack-result";
}
