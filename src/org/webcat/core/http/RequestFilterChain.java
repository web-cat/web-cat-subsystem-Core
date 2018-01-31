/*==========================================================================*\
 |  $Id: RequestFilterChain.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * Represents a filter chain that is passed to a {@link RequestFilter} so that
 * request handling can be passed to the next filter or handler in the chain.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public interface RequestFilterChain
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Filters should call this method to allow request handling to continue
     * with the next filter or handler in the chain.
     *
     * @param request the request
     * @param response the response
     * @throws Exception if an error occurred
     */
    public void filterRequest(WORequest request, WOResponse response)
        throws Exception;
}
