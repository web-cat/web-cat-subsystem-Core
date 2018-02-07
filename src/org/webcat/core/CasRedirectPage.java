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


package org.webcat.core;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOCookie;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * A no-frills page used to redirect the browser to a CAS login service,
 * while saving cookies needed to track state on return.
 *
 * @author  Stephen Edwards
 */
public class CasRedirectPage
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new page object.
     *
     * @param context The context to use
     */
    public CasRedirectPage(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public String contextId;
    public String loginUrl;
    public String authId;
    public boolean hasCookies;
    public String nextUrl;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        hasCookies = context.request().cookies().size() > 0;

        String myUrl = context().urlWithRequestHandlerKey(null, null, null);
        response.addCookie(new WOCookie(
            AuthenticationDomain.COOKIE_LAST_USED_INSTITUTION,
            authId, myUrl, null, DirectAction.ONE_YEAR, false));
        response.addCookie(new WOCookie(
            DirectAction.CONTEXT_ID_KEY,
            contextId, myUrl,null, DirectAction.TEN_MINUTES, false));

        nextUrl = Application.completeURLWithRequestHandlerKey(context,
            null, null, null, true, 0);
        if (contextId != null)
        {
            nextUrl += "?" + DirectAction.CONTEXT_ID_KEY + "=" + contextId;
            if (authId != null)
            {
                nextUrl += "&d=" + authId;
            }
        }

        super.appendToResponse(response, context);
    }


    //~ Instance/static fields ................................................

}
