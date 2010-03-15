/*==========================================================================*\
 |  $Id$
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

package net.sf.webcat.ui;

import org.apache.log4j.Logger;
import net.sf.webcat.ui.util.JSHash;
import net.sf.webcat.ui.util.DojoRemoteHelper;
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
 * @version $Id$
 */
public class WCConnectAction extends WOHTMLDynamicElement
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCConnectAction(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("script", someAssociations, template);

        _action = _associations.removeObjectForKey("action");
        _actionClass = _associations.removeObjectForKey("actionClass");
        _directActionName =
            _associations.removeObjectForKey("directActionName");
        _remoteHelper = new DojoRemoteHelper(_associations);
    }


    //~ Methods ...............................................................

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

        String elemName = nameInContext(context);
        JSHash requestOptions = new JSHash();
        requestOptions.put("sender", elemName);

        response.appendContentString("\n");
        response.appendContentString(_remoteHelper.remoteSubmitCall(
                "this", requestOptions, context));
        response.appendContentString("\n");
    }


    // ----------------------------------------------------------
    protected String nameInContext(WOContext context)
    {
        if (_associations != null && _associations.objectForKey("name") != null)
        {
            return _associations.objectForKey("name").valueInComponent(
                    context.component()).toString();
        }
        else
        {
            return context.elementID();
        }
    }


    // ----------------------------------------------------------
    @Override
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        if (!AjaxUtils.isAjaxRequest(request) ||
                !AjaxUtils.shouldHandleRequest(request, context, null))
        {
            return null;
        }

        WOActionResults result = null;

        WOComponent component = context.component();

        AjaxUtils.createResponse(request, context);
        AjaxUtils.mutableUserInfo(request);

        context.setActionInvoked(true);
        result = (WOActionResults) _action.valueInComponent(component);

        AjaxUtils.updateMutableUserInfoWithAjaxInfo(context);

        if (result == context.page())
        {
            log.warn("An Ajax request attempted to return the page, which "
                    + "is almost certainly an error.");

            result = null;
        }

        if (result == null)
        {
            result = AjaxUtils.createResponse(request, context);
        }

        return result;
    }


    //~ Static/instance variables .............................................

    protected WOAssociation _jsId;
    protected WOAssociation _action;
    protected WOAssociation _actionClass;
    protected WOAssociation _directActionName;

    protected DojoRemoteHelper _remoteHelper;

    private static final Logger log = Logger.getLogger(WCConnectAction.class);
}
