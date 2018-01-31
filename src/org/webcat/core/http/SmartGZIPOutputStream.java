/*==========================================================================*\
 |  $Id: SmartGZIPOutputStream.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import org.eclipse.jgit.util.HttpSupport;
import org.eclipse.jgit.util.TemporaryBuffer;
import org.webcat.core.NSMutableDataOutputStream;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * An output stream that automatically GZIPs its content before appending it
 * to a response if the request supports GZIP encoding.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class SmartGZIPOutputStream extends TemporaryBuffer
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new {@code SmartGZIPOutputStream} for the specified request
     * and response.
     *
     * @param request the request
     * @param response the response
     */
    public SmartGZIPOutputStream(WORequest request, WOResponse response)
    {
        super(LIMIT);

        this.request = request;
        this.response = response;
        this.outputStream = new NSMutableDataOutputStream();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    protected OutputStream overflow() throws IOException
    {
        startedOutput = true;
        return outputStream;
    }


    // ----------------------------------------------------------
    @Override
    public void close() throws IOException
    {
        super.close();

        if (!startedOutput)
        {
            TemporaryBuffer out = this;

            if (out.length() > 256 && RequestUtils.acceptsGZIPEncoding(request))
            {
                TemporaryBuffer gzbuf = new TemporaryBuffer.Heap(LIMIT);

                try
                {
                    GZIPOutputStream gzip = new GZIPOutputStream(gzbuf);
                    out.writeTo(gzip, null);
                    gzip.close();

                    if (gzbuf.length() < out.length())
                    {
                        out = gzbuf;
                        response.setHeader(HttpSupport.ENCODING_GZIP,
                                HttpSupport.HDR_CONTENT_ENCODING);
                    }
                }
                catch (IOException e)
                {
                    // Likely caused by overflowing the buffer, meaning the
                    // data would be larger if compressed. Discard compressed
                    // copy and use the original.
                }
            }

            out.writeTo(outputStream, null);
            outputStream.flush();
        }

        // Finally, append whatever was dumped into our stream to the response
        // content.

        outputStream.close();
        response.appendContentData(outputStream.data());
    }


    //~ Static/instance variables .............................................

    private static final int LIMIT = 32 * 1024;

    private WORequest request;
    private WOResponse response;
    private NSMutableDataOutputStream outputStream;
    private boolean startedOutput;
}
