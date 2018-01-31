/*==========================================================================*\
 |  $Id: WCConnectAction.java,v 1.2 2010/10/13 20:35:40 aallowat Exp $
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
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.components.ERXComponentUtilities;

//--------------------------------------------------------------------------
/**
 * Generates a script tag of type "dojo/connect" that can be nested inside a
 * Dijit element to execute a server-side action (via an Ajax request) in
 * response to a widget event. Use the "event" binding to specify the event
 * (such as "onChange"), and "args" to specify the argument list, as one
 * normally would in a dojo/connect script tag. The bindings that specify which
 * action to execute are similar to those offered by DojoFormActionElement and
 * DojoRemoteHelper.
 *
 * @author Tony Allevato
 * @version $Id: WCConnectAction.java,v 1.2 2010/10/13 20:35:40 aallowat Exp $
 */
public class WCConnectAction extends DojoActionFormElement
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCConnectAction(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("script", someAssociations, template);
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
    @Override
    public void appendAttributesToResponse(WOResponse response,
            WOContext context)
    {
        _appendTagAttributeAndValueToResponse(response, "type", "dojo/connect",
                false);

        super.appendAttributesToResponse(response, context);
    }


    // ----------------------------------------------------------
    @Override
    public void appendChildrenToResponse(WOResponse response, WOContext context)
    {
        super.appendChildrenToResponse(response, context);

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

        script.append("\n");

        if (_remoteHelper.isRemoteInContext(context))
        {
            JSHash requestOptions = new JSHash();
            requestOptions.put("url", actionUrl);
            requestOptions.put("sender", context.elementID());

            script.append(_remoteHelper.remoteSubmitCall(
                    "this", requestOptions, context));
        }
        else
        {
            script.append(WCForm.scriptToPerformFullSubmit(
                    context, nameInContext(context)));
        }

        script.append("\n");

        response.appendContentString(script.toString());
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
    protected void appendNameAttributeToResponse(WOResponse response,
            WOContext context)
    {
        // Do nothing.
    }
}
