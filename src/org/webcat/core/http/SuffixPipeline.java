/*==========================================================================*\
 |  $Id: SuffixPipeline.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

import com.webobjects.appserver.WODynamicURL;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * A URL pipeline that handles requests for registered path suffixes.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class SuffixPipeline extends UrlPipeline
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public SuffixPipeline(RequestFilter[] filters,
            RequestHandlerWithResponse requestHandler, String suffix)
    {
        super(filters, requestHandler);

        this.suffix = suffix;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public boolean matches(WORequest request)
    {
        String path = request.requestHandlerPath();
        return path.endsWith(suffix);
    }


    // ----------------------------------------------------------
    public void handleRequest(WORequest request, WOResponse response)
        throws Exception
    {
        WODynamicURL url = request._uriDecomposed();

        String oldPath = url.requestHandlerPath();
        String newPath = oldPath.substring(0,
                oldPath.length() - suffix.length());

        url.setRequestHandlerPath(newPath);

        try
        {
            super.handleRequest(request, response);
        }
        finally
        {
            url.setRequestHandlerPath(oldPath);
        }
    }


    //~ Inner classes .........................................................

    // ----------------------------------------------------------
    public static class Binder extends RequestHandlerBinderImpl
    {
        //~ Constructors ......................................................

        // ----------------------------------------------------------
        public Binder(String suffix)
        {
            this.suffix = suffix;
        }


        //~ Methods ...........................................................

        // ----------------------------------------------------------
        public UrlPipeline create()
        {
            return new SuffixPipeline(filters(), requestHandler(), suffix);
        }


        //~ Static/instance variables .........................................

        private String suffix;
    }


    //~ Static/instance variables .............................................

    private String suffix;
}
