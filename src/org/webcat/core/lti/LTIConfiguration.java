/*==========================================================================*\
 |  Copyright (C) 2018 Virginia Tech
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


package org.webcat.core.lti;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.webcat.core.Application;
import org.webcat.core.WCComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * A simple message page for use in LTI responses, when the response is
 * just an error/diagnostic message.
 *
 * @author  Stephen Edwards
 */
public class LTIConfiguration
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The context to use
     */
    public LTIConfiguration(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public String ltiLaunchUrl;
    public String hostName;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        response.setHeader("application/xml", "content-type");
        if (ltiLaunchUrl == null)
        {
            ltiLaunchUrl = Application.completeURLWithRequestHandlerKey(
                context,
                Application.application().directActionRequestHandlerKey(),
                "ltiLaunch", null, true, 0);
        }
        if (hostName == null)
        {
            Matcher matcher = Pattern.compile("^http(s)?://([^/:]*)",
                Pattern.CASE_INSENSITIVE).matcher(ltiLaunchUrl);
            if (matcher.find())
            {
                hostName = matcher.group(2);
            }
            else
            {
                hostName = "localhost";
            }
        }
        super.appendToResponse(response, context);
    }


    //~ Instance/static fields ................................................

    static Logger log = Logger.getLogger(LTIConfiguration.class);
}
