/*==========================================================================*\
 |  $Id: RequestUtils.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
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

package org.webcat.core.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.util.HttpSupport;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;

//-------------------------------------------------------------------------
/**
 * Helper methods for working with HTTP requests and responses.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class RequestUtils
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Prevent instantiation.
     */
    private RequestUtils()
    {
        // Do nothing.
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the specified request can accept a
     * response that is GZIP encoded.
     *
     * @param request the request
     * @return true if the request accepts a GZIP-encoded response; otherwise,
     *     false
     */
    public static boolean acceptsGZIPEncoding(WORequest request)
    {
        String accepts = request.headerForKey(HttpSupport.HDR_ACCEPT_ENCODING);
        return accepts != null && accepts.contains(HttpSupport.ENCODING_GZIP);
    }


    // ----------------------------------------------------------
    /**
     * Gets an input stream to read the content from the specified request,
     * expanded it from GZIP format if necessary.
     *
     * @param request the request
     * @return an input stream to read the content from the request
     * @throws IOException if an I/O error occurs
     */
    public static InputStream inputStreamForRequest(WORequest request)
        throws IOException
    {
        InputStream input = request.content().stream();
        String encoding = request.headerForKey(
                HttpSupport.HDR_CONTENT_ENCODING);

        if (HttpSupport.ENCODING_GZIP.equals(encoding)
                || "x-gzip".equals(encoding))
        {
            input = new GZIPInputStream(input);
        }
        else if (encoding != null)
        {
            throw new IOException("Content encoding \"" + encoding + "\" is "
                    + "not supported.");
        }

        return input;
    }


    // ----------------------------------------------------------
    /**
     * Appends a plain text string to a response, compressing it with GZIP if
     * the request supports that.
     *
     * @param content the content string
     * @param request the request
     * @param response the response
     * @throws IOException if an I/O error occurs
     */
    public static void sendPlainText(String content, WORequest request,
            WOResponse response) throws IOException
    {
        byte[] raw = content.getBytes(Constants.CHARACTER_ENCODING);
        response.setHeader("text/plain;charset=" + Constants.CHARACTER_ENCODING,
                HttpSupport.HDR_CONTENT_TYPE);
        sendBytes(raw, request, response);
    }


    // ----------------------------------------------------------
    /**
     * Appends a byte array to a response, compressing it with GZIP if the
     * request supports that.
     *
     * @param content the content array
     * @param request the request
     * @param response the response
     * @throws IOException if an I/O error occurs
     */
    public static void sendBytes(byte[] content, WORequest request,
            WOResponse response) throws IOException
    {
        content = preSend(content, request, response);
        response.appendContentData(new NSData(content));
    }


    // ----------------------------------------------------------
    /**
     * Prepares a byte array to be appended to a response, by compressing it
     * (if the request supports that) as well as setting its etag header.
     *
     * @param content the raw content to send
     * @param request the request
     * @param response the response
     * @return the byte array that should be sent (which may be a compressed
     *     version of the one sent in)
     * @throws IOException if an I/O error occurs
     */
    private static byte[] preSend(byte[] content, WORequest request,
            WOResponse response) throws IOException
    {
        response.setHeader(etag(content), HttpSupport.HDR_ETAG);

        if (content.length > 256 && acceptsGZIPEncoding(request))
        {
            content = compressBytes(content);
            response.setHeader(HttpSupport.ENCODING_GZIP,
                    HttpSupport.HDR_CONTENT_ENCODING);
        }

        return content;
    }


    // ----------------------------------------------------------
    /**
     * Compresses a byte array using GZIP.
     *
     * @param raw the raw data to compress
     * @return the compressed data
     * @throws IOException if an I/O error occurs
     */
    private static byte[] compressBytes(byte[] raw) throws IOException
    {
        int maxLen = raw.length + 32;
        ByteArrayOutputStream out = new ByteArrayOutputStream(maxLen);
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(raw);
        gzip.finish();
        gzip.flush();
        return out.toByteArray();
    }


    // ----------------------------------------------------------
    /**
     * Computes the etag (SHA-1 hash) for the specified content.
     *
     * @param content the content
     * @return SHA-1 hash for the specified content
     */
    private static String etag(byte[] content)
    {
        MessageDigest md = Constants.newMessageDigest();
        md.update(content);
        return ObjectId.fromRaw(md.digest()).getName();
    }
}
