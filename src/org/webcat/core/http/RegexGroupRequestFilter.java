/*==========================================================================*\
 |  $Id: RegexGroupRequestFilter.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

import java.util.regex.Matcher;
import com.webobjects.appserver.WODynamicURL;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

public class RegexGroupRequestFilter implements RequestFilter
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public RegexGroupRequestFilter(int groupIndex)
    {
        this.groupIndex = groupIndex;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void filterRequest(WORequest request, WOResponse response,
            RequestFilterChain filterChain) throws Exception
    {
        Matcher matcher = (Matcher) request.userInfoForKey(
                RegexPipeline.REGEX_MATCHER);

        WODynamicURL url = request._uriDecomposed();
        String oldPath = (String) request.userInfoForKey(
                RegexPipeline.REGEX_TEXT);

        int start = matcher.start(groupIndex);
        String newPath = matcher.group(groupIndex);

        Object oldFilterPath = request.userInfoForKey(
                MetaRequestHandler.REGEX_FILTER_PATH_KEY);

        try
        {
            url.setRequestHandlerPath(newPath);
            request.setUserInfoForKey(oldPath.substring(0, start),
                    MetaRequestHandler.REGEX_FILTER_PATH_KEY);
            filterChain.filterRequest(request, response);
        }
        finally
        {
            request.setUserInfoForKey(oldFilterPath,
                    MetaRequestHandler.REGEX_FILTER_PATH_KEY);
            url.setRequestHandlerPath(oldPath);
        }
    }


    //~ Static/instance variables .............................................

    private int groupIndex;
}
