/*==========================================================================*\
 |  $Id: RequestFilter.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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
 * A filter that is called before the actual handler for a request, which can
 * manipulate the request/response in some way, or prevent further processing
 * entirely.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public interface RequestFilter
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Called to filter the request. The
     * {@link RequestFilterChain#filterRequest(WORequest, WOResponse)} method
     * can be called on the {@code filterChain} parameter in order to continue
     * processing, or the call can be omitted to prevent processing from moving
     * on to the next filter or handler.
     *
     * @param request the request
     * @param response the response
     * @param filterChain the filter chain, used to forward the request to the
     *     next filter or handler in the chain
     * @throws Exception if an error occurs
     */
    public void filterRequest(WORequest request, WOResponse response,
            RequestFilterChain filterChain) throws Exception;
}
