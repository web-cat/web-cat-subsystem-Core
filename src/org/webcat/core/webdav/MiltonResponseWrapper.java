/*==========================================================================*\
 |  $Id: MiltonResponseWrapper.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.webcat.core.NSMutableDataOutputStream;
import com.bradmcevoy.http.AbstractResponse;
import com.bradmcevoy.http.Cookie;
import com.bradmcevoy.http.Response.Status;
import com.webobjects.appserver.WOCookie;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;

//-------------------------------------------------------------------------
/**
 * Wraps a WebObjects {@code WOResponse} so that the Milton framework can
 * access its headers and content.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class MiltonResponseWrapper extends AbstractResponse
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public MiltonResponseWrapper(WOResponse response)
    {
        this.response = response;
        this.headers = new HashMap<String, String>();
        this.outputStream = new NSMutableDataOutputStream();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void close()
    {
        try
        {
            outputStream.flush();
            response.appendContentData(outputStream.data());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


    // ----------------------------------------------------------
    public Map<String, String> getHeaders()
    {
        return Collections.unmodifiableMap(headers);
    }


    // ----------------------------------------------------------
    public String getNonStandardHeader(String code)
    {
        return headers.get(code);
    }


    // ----------------------------------------------------------
    public OutputStream getOutputStream()
    {
        return outputStream;
    }


    // ----------------------------------------------------------
    public Status getStatus()
    {
        return status;
    }


    // ----------------------------------------------------------
    public void setAuthenticateHeader(List<String> challenges)
    {
        for (String challenge : challenges)
        {
            response.setHeader(challenge, Header.WWW_AUTHENTICATE.code);
        }
    }


    // ----------------------------------------------------------
    public Cookie setCookie(Cookie cookie)
    {
        if (cookie instanceof MiltonCookieWrapper)
        {
            MiltonCookieWrapper mc = (MiltonCookieWrapper) cookie;
            response.addCookie(mc.wrappedCookie());
            return cookie;
        }
        else
        {
            WOCookie c = new WOCookie(cookie.getName(), cookie.getValue());
            c.setDomain(cookie.getDomain());
            c.setTimeOut(cookie.getExpiry());
            c.setPath(cookie.getPath());
            c.setIsSecure(cookie.getSecure());

            response.addCookie(c);
            return new MiltonCookieWrapper(c);
        }
    }


    // ----------------------------------------------------------
    public Cookie setCookie(String name, String value)
    {
        WOCookie cookie = new WOCookie(name, value);
        response.addCookie(cookie);
        return new MiltonCookieWrapper(cookie);
    }


    // ----------------------------------------------------------
    public void setNonStandardHeader(String code, String value)
    {
        response.setHeader(value, code);
        headers.put(code, value);
    }


    // ----------------------------------------------------------
    public void setStatus(Status status)
    {
        response.setStatus(status.code);
        this.status = status;
    }


    //~ Static/instance variables .............................................

    private WOResponse response;
    private HashMap<String, String> headers;
    private Status status;
    private NSMutableDataOutputStream outputStream;
}
