/*==========================================================================*\
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.webcat.core.lti.LTILaunchRequest;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.signature.OAuthSignatureMethod;

//-------------------------------------------------------------------------
/**
 * A simple development diagnostic page for testing callbacks (such as
 * CAS login callbacks).
 *
 * @author  Stephen Edwards
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


    // ----------------------------------------------------------
    public String oauthSignature()
    {
        WORequest request = context().request();
//        String consumerKey = "QwertAsdfg";
//        String consumerSecret = "GfdsaTrewq";
//        String apiUrl = "https://web-cat2.cs.vt.edu/Web-CAT/WebObjects/"
//            + "Web-CAT.woa/wa/ltiLaunch";
//        String consumerSig = request.stringFormValueForKey("oauth_signature");
//        List<OAuth.Parameter> params = new ArrayList<OAuth.Parameter>();
        log.debug("begin request parameters {");
        for (String key : request.formValueKeys())
        {
            String value = request.stringFormValueForKey(key);
            log.debug("    " + key + " : " + value);
//            params.add(new OAuth.Parameter(key, value));
        }
        log.debug("} end request parameters");
//        OAuthMessage message = new OAuthMessage("POST", apiUrl, params);
//        OAuthConsumer consumer =
//            new OAuthConsumer(null, consumerKey, consumerSecret, null);
//        OAuthAccessor accessor = new OAuthAccessor(consumer);
//
        String signature = null;
//        try
//        {
//            log.debug("request parameters: " + params);
//            log.debug("Signature base string: "
//                + OAuthSignatureMethod.getBaseString(message));
//            log.debug("consumer sig: " + consumerSig);
//            log.debug("checking validity");
//            try
//            {
//                VALIDATOR.validateMessage(message, accessor);
//                log.debug("request is valid!");
//            }
//            catch (Exception e)
//            {
//                log.error("request is invalid: " + e.getClass().getSimpleName()
//                    + ": " + e.getMessage());
//            }
//        }
//        catch (Exception e)
//        {
//            log.error(e);
//        }
        LTILaunchRequest lti = new LTILaunchRequest(
            context(), ((Session)session()).defaultEditingContext());
        log.debug("LTI request is valid = " + lti.isValid());
        log.debug("LMS instance = " + lti.lmsInstance());
        log.debug("course name = " + lti.courseName());
        log.debug("LTI user id = " + lti.userId());
        log.debug("user = " + lti.user());
        return signature;
    }


    //~ Static/instance variables .............................................

    private static OAuthValidator VALIDATOR = new SimpleOAuthValidator();
    static Logger log = Logger.getLogger(CallbackDiagnosticPage.class);
}
