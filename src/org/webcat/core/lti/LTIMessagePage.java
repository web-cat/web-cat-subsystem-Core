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

import org.apache.log4j.Logger;
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
public class LTIMessagePage
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The context to use
     */
    public LTIMessagePage(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public LTILaunchRequest ltiRequest;
    public String title;
    public String message = "There was an error processing your request.s";


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (title == null || title.isEmpty())
        {
            String cms = null;
            if (ltiRequest != null)
            {
                cms = ltiRequest.get(
                    LTILaunchRequest.TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE);
            }
            if (cms == null || cms.isEmpty())
            {
                cms = "Course Management System";
            }
            else
            {
                // attempt to capitalize?
                if (cms.length() > 0)
                {
                    cms = cms.substring(0, 1).toUpperCase() + cms.substring(1);
                }
            }
            title = cms + " Response";
        }
        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    @Override
    public String title()
    {
        return title;
    }


    //~ Instance/static fields ................................................

    static Logger log = Logger.getLogger(LTIMessagePage.class);
}
