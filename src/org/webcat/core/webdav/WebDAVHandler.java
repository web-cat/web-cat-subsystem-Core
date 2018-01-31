/*==========================================================================*\
 |  $Id: WebDAVHandler.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

import org.webcat.core.http.RequestHandlerWithResponse;
import org.webcat.core.webdav.MiltonRequestWrapper;
import org.webcat.core.webdav.MiltonResponseWrapper;
import org.webcat.core.webdav.WorkingCopyResourceFactory;
import com.bradmcevoy.http.HttpManager;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * A simple request handler that wraps the request and response in
 * Milton-compatible objects and then passes control to the Milton HTTP
 * manager.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class WebDAVHandler implements RequestHandlerWithResponse
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WebDAVHandler()
    {
        resourceFactory = new WorkingCopyResourceFactory();
        httpManager = new HttpManager(resourceFactory);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void handleRequest(WORequest request, WOResponse response)
    {
        MiltonRequestWrapper miltonRequest =
            new MiltonRequestWrapper(request);
        MiltonResponseWrapper miltonResponse =
            new MiltonResponseWrapper(response);

        httpManager.process(miltonRequest, miltonResponse);
    }


    //~ Static/instance variables .............................................

    private WorkingCopyResourceFactory resourceFactory;
    private HttpManager httpManager;
}
