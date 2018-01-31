/*==========================================================================*\
 |  $Id: WCActionFunction.java,v 1.2 2010/10/11 14:25:35 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2008 Virginia Tech
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

package org.webcat.ui;

import org.apache.log4j.Logger;
import org.webcat.core.Application;
import org.webcat.ui._base.DojoActionFormElement;
import org.webcat.ui.util.DojoRemoteHelper;
import org.webcat.ui.util.JSHash;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.appserver.ERXWOContext;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.components.ERXComponentUtilities;

//--------------------------------------------------------------------------
/**
 * Generates a JavaScript function that can be called to execute an action,
 * either as a synchronous (page-load) request or a remote Ajax request.
 *
 * As with other Dojo action elements, the "remoteness" of the request is
 * determined by the presence of any of the "remote.*" bindings. If they are
 * omitted, the request is synchronous.
 *
 * @binding jsId the Javascript name of the function
 * @binding waitForOnLoad if true, the function will be generated in a block
 *     inside a dojo.addOnLoad handler
 *
 * @author Tony ALlevato
 * @version $Id: WCActionFunction.java,v 1.2 2010/10/11 14:25:35 aallowat Exp $
 */
public class WCActionFunction extends DojoActionFormElement
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCActionFunction(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);

        _jsId = _associations.removeObjectForKey("jsId");
        _waitForOnLoad = _associations.removeObjectForKey("waitForOnLoad");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return null;
    }


    // ----------------------------------------------------------
    @Override
    protected boolean usesFakeFullSubmit()
    {
        return true;
    }


    // ----------------------------------------------------------
    protected String jsIdInContext(WOContext context)
    {
        if (_jsId != null)
        {
            return _jsId.valueInComponent(context.component()).toString();
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    protected boolean waitForOnLoadInContext(WOContext context)
    {
        if (_waitForOnLoad != null)
        {
            return _waitForOnLoad.booleanValueInComponent(context.component());
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        super.appendToResponse(response, context);

        String id = jsIdInContext(context);

        WOComponent component = context.component();

        String actionUrl = null;

        if (_directActionName != null)
        {
            actionUrl = context.directActionURLForActionNamed(
                    (String) _directActionName.valueInComponent(component),
                    ERXComponentUtilities.queryParametersInComponent(
                            _associations, component)).replaceAll("&amp;", "&");
        }
        else
        {
            actionUrl = AjaxUtils.ajaxComponentActionUrl(context);
        }

        StringBuffer script = new StringBuffer();

        boolean waitForOnLoad = waitForOnLoadInContext(context);

        if (waitForOnLoad)
        {
            script.append("dojo.addOnLoad(function() {\n");
        }

        script.append(id);
        script.append(" = function(widget) {\n");

        if (_remoteHelper.isRemoteInContext(context))
        {
            JSHash requestOptions = new JSHash();
            requestOptions.put("url", actionUrl);
            requestOptions.put("sender", context.elementID());

            script.append(_remoteHelper.remoteSubmitCall(
                    "widget", requestOptions, context));
        }
        else
        {
            script.append(WCForm.scriptToPerformFullSubmit(
                    context, nameInContext(context)));
        }

        script.append("}");

        if (waitForOnLoad)
        {
            script.append("\n});\n");
        }

        if (Application.isAjaxRequest(context.request()))
        {
            response.appendContentString("<script type=\"text/javascript\">");
            response.appendContentString(script.toString());
            response.appendContentString("</script>");
        }
        else
        {
            ERXResponseRewriter.addScriptCodeInHead(response, context,
                    script.toString());
        }
    }


    // ----------------------------------------------------------
    @Override
    protected void appendOnClickScriptToResponse(WOResponse response,
            WOContext context)
    {
        // Do nothing.
    }


    // ----------------------------------------------------------
    @Override
    protected void _appendOpenTagToResponse(WOResponse response,
            WOContext context)
    {
        // Do nothing.
    }


    // ----------------------------------------------------------
    @Override
    protected void _appendCloseTagToResponse(WOResponse response,
            WOContext context)
    {
        // Do nothing.
    }


    //~ Static/instance variables .............................................

    protected WOAssociation _jsId;
    protected WOAssociation _waitForOnLoad;
}
