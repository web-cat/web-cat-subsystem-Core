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
import net.sf.webcat.core.Application;
import net.sf.webcat.ui.util.DojoOptions;
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
 * Generates a JavaScript function that can be called to execute an action via
 * an Ajax request.
 * 
 * TODO does not currently support form submits (uses webcat.invokeRemoteAction
 * instead of webcat.partialSubmit)
 * 
 * @author Tony ALlevato
 * @version $Id$
 */
public class WCRemoteFunction extends WOHTMLDynamicElement
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCRemoteFunction(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);

        _jsId = _associations.removeObjectForKey("jsId");
        _action = _associations.removeObjectForKey("action");
        _actionClass = _associations.removeObjectForKey("actionClass");
        _directActionName =
            _associations.removeObjectForKey("directActionName");
        _remoteHelper = new DojoRemoteHelper(_associations);
    }


    //~ Methods ...............................................................
    
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
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
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
        script.append(id);
        script.append(" = function(widget) {\n");
        script.append(_remoteHelper.invokeRemoteActionCall(
                "widget", actionUrl, null, context));
        script.append("}");

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
    protected String nameInContext(WOContext context)
    {
        if (_associations.objectForKey("name") != null)
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

        if (_action != null)
        {
            result = (WOActionResults) _action.valueInComponent(component);
        }
        
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

    private static final Logger log = Logger.getLogger(WCRemoteFunction.class);
}
