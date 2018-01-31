/*==========================================================================*\
 |  $Id: MiltonCookieWrapper.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

import com.bradmcevoy.http.Cookie;
import com.webobjects.appserver.WOCookie;

//-------------------------------------------------------------------------
/**
 * Wraps a WebObjects {@code WOCookie} so that the Milton framework can access
 * its properties.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class MiltonCookieWrapper implements Cookie
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public MiltonCookieWrapper(WOCookie cookie)
    {
        this.cookie = cookie;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the {@code WOCookie} that this object wraps.
     *
     * @return the {@code WOCookie} that this object wraps
     */
    public WOCookie wrappedCookie()
    {
        return cookie;
    }


    // ----------------------------------------------------------
    public int getVersion()
    {
        return 1;
    }


    // ----------------------------------------------------------
    public void setVersion(int version)
    {
        // Do nothing; WOCookie does not support setting the version.
    }


    // ----------------------------------------------------------
    public String getName()
    {
        return cookie.name();
    }


    // ----------------------------------------------------------
    public String getValue()
    {
        return cookie.value();
    }


    // ----------------------------------------------------------
    public void setValue(String value)
    {
        cookie.setValue(value);
    }


    // ----------------------------------------------------------
    public boolean getSecure()
    {
        return cookie.isSecure();
    }


    // ----------------------------------------------------------
    public void setSecure(boolean secure)
    {
        cookie.setIsSecure(secure);
    }


    // ----------------------------------------------------------
    public int getExpiry()
    {
        return cookie.timeOut();
    }


    // ----------------------------------------------------------
    public void setExpiry(int expiry)
    {
        cookie.setTimeOut(expiry);
    }


    // ----------------------------------------------------------
    public String getPath()
    {
        return cookie.path();
    }


    // ----------------------------------------------------------
    public void setPath(String path)
    {
        cookie.setPath(path);
    }


    // ----------------------------------------------------------
    public String getDomain()
    {
        return cookie.domain();
    }


    // ----------------------------------------------------------
    public void setDomain(String domain)
    {
        cookie.setDomain(domain);
    }


    //~ Static/instance variables .............................................

    private WOCookie cookie;
}
