/*==========================================================================*\
 |  Copyright (C) 2023 Virginia Tech
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


package org.webcat.woextensions;

import org.webcat.core.Application;
import com.webobjects.appserver.WOCookie;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * Customized version of WOCookie that sets SameSite and isSecure to better
 * defaults.
 *
 * @author  edwards
 */
public class WCCookie
    extends WOCookie
{

    // ----------------------------------------------------------
    /**
     * Create a new WCCookie object.
     * @param name
     * @param value
     */
    public WCCookie(String name, String value)
    {
        this(name, value, null, null, -1);
    }


    // ----------------------------------------------------------
    /**
     * Create a new WCCookie object.
     * @param name
     * @param value
     * @param path
     * @param domain
     * @param expires
     */
    public WCCookie(
        String name,
        String value,
        String path,
        String domain,
        NSTimestamp expires)
    {
        this(name, value, path, domain, expires,
            Application.useSecureConnectionsByDefault(),
            false);
    }


    // ----------------------------------------------------------
    /**
     * Create a new WCCookie object.
     * @param name
     * @param value
     * @param path
     * @param domain
     * @param expires
     * @param isSecure
     */
    public WCCookie(
        String name,
        String value,
        String path,
        String domain,
        NSTimestamp expires,
        boolean isSecure)
    {
        this(name, value, path, domain, expires, isSecure, false);
    }


    // ----------------------------------------------------------
    /**
     * Create a new WCCookie object.
     * @param name
     * @param value
     * @param path
     * @param domain
     * @param timeout
     */
    public WCCookie(
        String name,
        String value,
        String path,
        String domain,
        int timeout)
    {
        this(name, value, path, domain, timeout,
            Application.useSecureConnectionsByDefault(),
            false);
    }


    // ----------------------------------------------------------
    /**
     * Create a new WCCookie object.
     * @param name
     * @param value
     * @param path
     * @param domain
     * @param timeout
     * @param isSecure
     */
    public WCCookie(
        String name,
        String value,
        String path,
        String domain,
        int timeout,
        boolean isSecure)
    {
        this(name, value, path, domain, timeout, isSecure, false);
    }


    // ----------------------------------------------------------
    /**
     * Create a new WCCookie object.
     * @param name
     * @param value
     * @param path
     * @param domain
     * @param expires
     * @param isSecure
     * @param httpOnly
     */
    public WCCookie(
        String name,
        String value,
        String path,
        String domain,
        NSTimestamp expires,
        boolean isSecure,
        boolean httpOnly)
    {
        super(name, value, path, domain, expires, isSecure, httpOnly);
        if (isSecure)
        {
            setSameSite(SameSite.NONE);
        }
    }


    // ----------------------------------------------------------
    /**
     * Create a new WCCookie object.
     * @param name
     * @param value
     * @param path
     * @param domain
     * @param timeout
     * @param isSecure
     * @param httpOnly
     */
    public WCCookie(
        String name,
        String value,
        String path,
        String domain,
        int timeout,
        boolean isSecure,
        boolean httpOnly)
    {
        super(name, value, path, domain, timeout, isSecure, httpOnly);
        if (isSecure)
        {
            setSameSite(SameSite.NONE);
        }
    }

}
