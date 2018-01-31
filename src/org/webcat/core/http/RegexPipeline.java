/*==========================================================================*\
 |  $Id: RegexPipeline.java,v 1.3 2012/06/22 16:23:18 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011-2012 Virginia Tech
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
import java.util.regex.Pattern;
import com.webobjects.appserver.WODynamicURL;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.3 $, $Date: 2012/06/22 16:23:18 $
 */
public class RegexPipeline
    extends UrlPipeline
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public RegexPipeline(RequestFilter[] filters,
            RequestHandlerWithResponse requestHandler, Pattern pattern)
    {
        super(filters, requestHandler);

        this.pattern = pattern;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public boolean matches(WORequest request)
    {
        String path = request.requestHandlerPath();
        return pattern.matcher(path).matches();
    }


    // ----------------------------------------------------------
    @Override
    public void handleRequest(WORequest request, WOResponse response)
        throws Exception
    {
        String path = request.requestHandlerPath();

        if (path == null)
        {
            response.setStatus(WOMessage.HTTP_STATUS_NOT_FOUND);
            return;
        }

        Matcher matcher = pattern.matcher(path);
        if (!matcher.matches())
        {
            response.setStatus(WOMessage.HTTP_STATUS_NOT_FOUND);
        }

        Object oldText = request.userInfoForKey(REGEX_TEXT);
        Object oldMatcher = request.userInfoForKey(REGEX_MATCHER);

        try
        {
            request.setUserInfoForKey(path, REGEX_TEXT);
            request.setUserInfoForKey(matcher, REGEX_MATCHER);

            if (matcher.groupCount() >= 1)
            {
                WODynamicURL url = request._uriDecomposed();
                String oldPath = url.requestHandlerPath();

                int start = matcher.start(1);
                String newPath = matcher.group(1);

                Object oldFilterPath = request.userInfoForKey(
                        MetaRequestHandler.REGEX_FILTER_PATH_KEY);

                NSMutableArray<String> groups = new NSMutableArray<String>();

                for (int i = 0; i <= matcher.groupCount(); i++)
                {
                    if (matcher.group(i) == null)
                    {
                        groups.add("");
                    }
                    else
                    {
                        groups.add(matcher.group(i));
                    }
                }

                request.setUserInfoForKey(groups,
                        MetaRequestHandler.REGEX_CAPTURE_GROUPS_KEY);

                try
                {
                    url.setRequestHandlerPath(newPath);
                    request.setUserInfoForKey(path.substring(0, start),
                            MetaRequestHandler.REGEX_FILTER_PATH_KEY);
                    super.handleRequest(request, response);
                }
                finally
                {
                    request.setUserInfoForKey(oldFilterPath,
                            MetaRequestHandler.REGEX_FILTER_PATH_KEY);
                    url.setRequestHandlerPath(oldPath);
                }
            }
            else
            {
                // No capture groups were used in the regex, so service the
                // whole request.

                super.handleRequest(request, response);
            }
        }
        finally
        {
            request.setUserInfoForKey(oldText, REGEX_TEXT);
            request.setUserInfoForKey(oldMatcher, REGEX_MATCHER);
        }
    }


    // ----------------------------------------------------------
    public static class Binder extends RequestHandlerBinderImpl
    {
        // ----------------------------------------------------------
        public Binder(String regex)
        {
            this.pattern = Pattern.compile(regex);
        }


        // ----------------------------------------------------------
        public UrlPipeline create()
        {
            return new RegexPipeline(filters(), requestHandler(), pattern);
        }


        private Pattern pattern;
    }


    //~ Static/instance variables .............................................

    public static final String REGEX_TEXT =
        "org.webcat.core.http.RegexPipeline.text";

    public static final String REGEX_MATCHER =
        "org.webcat.core.http.RegexPipeline.matcher";

    private Pattern pattern;
}
