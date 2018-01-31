/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2017-2018 Virginia Tech
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import org.apache.log4j.Logger;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * A simple development diagnostic page for testing callbacks (such as
 * CAS login callbacks).
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class CallbackDiagnosticPage
    extends WOComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public CallbackDiagnosticPage(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public WORequest incomingRequest;
    public String response;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse aResponse, WOContext aContext)
    {
        String ticket = aContext.request().stringFormValueForKey("ticket");
        if (ticket != null)
        {
            log.debug("received ticket: " + ticket);

            String validationUrl = Application.configurationProperties()
              .getProperty(CallbackDiagnosticPage.class.getName()
              + ".validationUrl",
              "https://login-dev.middleware.vt.edu/profile/cas/"
              + "serviceValidate");
            String returnUrl = Application.configurationProperties()
              .getProperty(CallbackDiagnosticPage.class.getName()
              + ".returnUrl",
              "https://web-cat.cs.vt.edu/Web-CAT/WebObjects/Web-CAT.woa/"
              + "wa/casCallback");

            try
            {
                // given a url open a connection
                URLConnection c = new URL(validationUrl
                    + "?ticket="
                    + URLEncoder.encode(ticket, "UTF-8")
                    + "&service="
                    + URLEncoder.encode(returnUrl, "UTF-8")).openConnection();

                // set connection timeout to 5 sec and read timeout to 10 sec
                c.setConnectTimeout(5000);
                c.setReadTimeout(10000);

                // get a stream to read data from
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(c.getInputStream()));
                StringBuilder content = new StringBuilder(1024);
                String line = in.readLine();
                while (line != null)
                {
                    content.append(line);
                    line = in.readLine();
                }

                response = content.toString();

                in.close();
            }
            catch (IOException e)
            {
                log.error(e);
            }
        }

        super.appendToResponse(aResponse, aContext);
    }


    // ----------------------------------------------------------
    public String loginUrl()
    {
        try
        {
            String loginUrl = Application.configurationProperties()
                .getProperty(CallbackDiagnosticPage.class.getName()
                + ".loginUrl",
                "https://login-dev.middleware.vt.edu/profile/cas/login");
            String returnUrl = Application.configurationProperties()
                .getProperty(CallbackDiagnosticPage.class.getName()
                + ".returnUrl",
                "https://web-cat.cs.vt.edu/Web-CAT/WebObjects/Web-CAT.woa/"
                + "wa/casCallback");
            return loginUrl + "?service="
                + URLEncoder.encode(returnUrl, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            log.error("Cannot encode in UTF-8", e);
        }
        return null;
    }


    //~ Static/instance variables .............................................

    static Logger log = Logger.getLogger(CallbackDiagnosticPage.class);
}
