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

package org.webcat.core.webdav;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.webcat.core.git.http.NoCacheRequestFilter;
import org.webcat.core.http.MetaRequestHandler;
import org.webcat.core.http.RequestHandlerBinder;
import com.bradmcevoy.http.StandardFilter;

//-------------------------------------------------------------------------
/**
 * The main request handler for WebDAV requests.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class WebDAVRequestHandler extends MetaRequestHandler
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new {@code WebDAVRequestHandler}.
     */
    public WebDAVRequestHandler()
    {
        // Apparently the only way to let DAV know that a file/folder is read-
        // only is to throw a NotAuthorizedException in the appropriate
        // resource methods, but the Milton framework generates log messages at
        // the WARN level when this happens. Instead of requiring all users to
        // address this in their log4j configuration file, we just shut it up
        // at runtime.

        Logger.getLogger(StandardFilter.class).setLevel(Level.ERROR);

        // Handle any request via the WebDAVHandler, which will serve files
        // using the Milton WebDAV library.

        serve("*").with(new WebDAVHandler());
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Inserts an authentication filter into the filter chain for any request
     * to this handler.
     */
    protected RequestHandlerBinder register(RequestHandlerBinder binder)
    {
        return binder.through(new NoCacheRequestFilter())
                     .through(new WebDAVAuthenticationFilter());
    }


    //~ Static/instance variables .............................................

    /** The request handler key. */
    public static final String REQUEST_HANDLER_KEY = "dav";
}
