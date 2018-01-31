/*==========================================================================*\
 |  $Id: MiltonRequestWrapper.java,v 1.3 2011/06/10 00:31:45 aallowat Exp $
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

package org.webcat.core.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.webcat.core.FileUtilities;
import com.bradmcevoy.http.AbstractRequest;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Cookie;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.RequestParseException;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.Request.Header;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Response.ContentType;
import com.webobjects.appserver.WOCookie;
import com.webobjects.appserver.WODynamicURL;
import com.webobjects.appserver.WORequest;

//-------------------------------------------------------------------------
/**
 * Wraps a WebObjects {@code WORequest} so that the Milton framework can access
 * its headers and content.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.3 $, $Date: 2011/06/10 00:31:45 $
 */
public class MiltonRequestWrapper extends AbstractRequest
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public MiltonRequestWrapper(WORequest request)
    {
        this.request = request;
        this.inputStream = request.content().stream();

        threadLocalRequest.set(request);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public static WORequest currentRequest()
    {
        return threadLocalRequest.get();
    }


    // ----------------------------------------------------------
    @Override
    public String getRequestHeader(Header header)
    {
        return request.headerForKey(header.code);
    }


    // ----------------------------------------------------------
    public String getAbsoluteUrl()
    {
        return WebDAVRequestHandler.completeURLForPath(
                request.context(),
                request.requestHandlerPath().replace(" ", "%20"));
    }


    // ----------------------------------------------------------
    public Auth getAuthorization()
    {
        if (auth == null)
        {
            auth = new Auth("");
        }

        return auth;
    }


    // ----------------------------------------------------------
    public Cookie getCookie(String name)
    {
        for (WOCookie cookie : request.cookies())
        {
            if (cookie.name().equals(name))
            {
                return new MiltonCookieWrapper(cookie);
            }
        }

        return null;
    }


    // ----------------------------------------------------------
    public List<Cookie> getCookies()
    {
        ArrayList<Cookie> cookies = new ArrayList<Cookie>();

        for (WOCookie cookie : request.cookies())
        {
            cookies.add(new MiltonCookieWrapper(cookie));
        }

        return cookies;
    }


    // ----------------------------------------------------------
    public String getFromAddress()
    {
        return request._remoteAddress();
    }


    // ----------------------------------------------------------
    public Map<String, String> getHeaders()
    {
        HashMap<String, String> headers = new HashMap<String, String>();

        for (String header : request.headerKeys())
        {
            headers.put(header, request.headerForKey(header));
        }

        return headers;
    }


    // ----------------------------------------------------------
    public InputStream getInputStream() throws IOException
    {
        return inputStream;
    }


    // ----------------------------------------------------------
    public Method getMethod()
    {
        return Method.valueOf(request.method());
    }


    // ----------------------------------------------------------
    public String getRemoteAddr()
    {
        return request._remoteAddress();
    }


    // ----------------------------------------------------------
    protected Response.ContentType getRequestContentType()
    {
        String s = getRequestHeader(Header.CONTENT_TYPE);

        if (s == null)
        {
            return null;
        }
        else if (s.contains(Response.MULTIPART))
        {
            return ContentType.MULTIPART;
        }
        else
        {
            return typeContents.get(s);
        }
    }


    // ----------------------------------------------------------
    public void parseRequestParameters(Map<String, String> params,
            Map<String, FileItem> files) throws RequestParseException
    {
        try
        {
            if (request.isMultipartFormData())
            {
                // TODO
                System.out.println("Multpart");
            }
            else
            {
                for (String param : request.formValueKeys())
                {
                    params.put(param, (String) request.formValueForKey(param));
                }
            }
        }
        catch (Throwable t)
        {
            throw new RequestParseException(t.getMessage(), t);
        }
    }


    // ----------------------------------------------------------
    public void setAuthorization(Auth auth)
    {
        this.auth = auth;
    }


    //~ Static/instance variables .............................................

    private WORequest request;
    private InputStream inputStream;
    private Auth auth;

    private static ThreadLocal<WORequest> threadLocalRequest =
        new ThreadLocal<WORequest>();

    private static final Map<ContentType, String> contentTypes =
        new EnumMap<ContentType, String>(ContentType.class);
    private static final Map<String, ContentType> typeContents =
        new HashMap<String, ContentType>();

    static
    {
        contentTypes.put(ContentType.HTTP, Response.HTTP);
        contentTypes.put(ContentType.MULTIPART, Response.MULTIPART);
        contentTypes.put(ContentType.XML, Response.XML);

        for (ContentType key : contentTypes.keySet())
        {
            typeContents.put(contentTypes.get(key), key);
        }
    }
}
