/*==========================================================================*\
 |  $Id: WCContentPane.java,v 1.2 2010/10/28 00:37:30 aallowat Exp $
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

import org.webcat.ui._base.DojoElement;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.ERXWOContext;

//------------------------------------------------------------------------
/**
 * A Dojo content pane that provides an Ajax interface. After assigning the
 * pane an element identifier, you can refresh the pane's content by calling
 * its refresh method:  <code>dijit.byId("paneId").refresh()</code>
 *
 * <h2>Bindings</h2>
 *
 * <dl>
 * <dt>refreshOnShow</dt>
 * <dd>A boolean value indicating whether the pane should refresh (redownload)
 * its content when it goes from hidden to shown. Defaults to false.</dd>
 *
 * <dt>alwaysDynamic</dt>
 * <dd>A boolean value indicating whether the pane's content should
 * <b>always</b> be retrieved with an Ajax request, as opposed to being
 * rendered during the normal page-load cycle. Defaults to false.</dd>
 *
 * </dl>
 *
 * @author Tony Allevato
 * @version $Id: WCContentPane.java,v 1.2 2010/10/28 00:37:30 aallowat Exp $
 */
public class WCContentPane extends DojoElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public WCContentPane(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);

        _alwaysDynamic = _associations.removeObjectForKey("alwaysDynamic");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return "webcat.ContentPane";
    }


    // ----------------------------------------------------------
    @Override
    public String elementName()
    {
        return "div";
    }


    // ----------------------------------------------------------
    protected boolean alwaysDynamicInContext(WOContext context)
    {
        if (_alwaysDynamic != null)
        {
            return _alwaysDynamic.booleanValueInComponent(context.component());
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    protected String containerIDOrName(WOContext context)
    {
        String id = (String) valueForBinding("id", context.component());

        if (id == null)
        {
            id = ERXWOContext.safeIdentifierName(context, false);
        }

        return id;
    }


    // ----------------------------------------------------------
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        //if (haveChildrenBeenAppended)
        {
            // If the request is not associated with this content pane, then we
            // only try to invoke the action on its children if they have
            // already been rendered on the page. This prevents potentially
            // slow-running operations inside closed title panes from being
            // executed until they are opened for the first time.

            super.takeValuesFromRequest(request, context);
        }
    }


    // ----------------------------------------------------------
    @Override
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        WOActionResults result = null;
        boolean isAjax = ERXApplication.isAjaxRequest(request);

        if (isAjax && AjaxUtils.shouldHandleRequest(request, context, null))
        {
            // If the request is associated with the element ID of this content
            // pane, then it is coming from a get request on the href
            // associated with the pane. We need to create a new response and
            // append this element's children to it.

            String id = containerIDOrName(context);

            WOResponse response = AjaxUtils.createResponse(request, context);
            AjaxUtils.setPageReplacementCacheKey(context, id);

            appendChildrenToResponse(response, context);

            AjaxUtils.updateMutableUserInfoWithAjaxInfo(context);

            result = response;
        }
        else
        {
            result = super.invokeAction(request, context);
        }

        return result;
    }


    // ----------------------------------------------------------
    @Override
    public void appendChildrenToResponse(WOResponse response, WOContext context)
    {
        WORequest request = context.request();

        boolean isAjax = ERXApplication.isAjaxRequest(request);
        boolean shouldHandleAjax =
            isAjax && context.elementID().startsWith(context.senderID());

        if (shouldHandleAjax || (!isAjax && !alwaysDynamicInContext(context)))
        {
            super.appendChildrenToResponse(response, context);
        }
    }


    // ----------------------------------------------------------
    @Override
    public void appendAttributesToResponse(WOResponse response,
            WOContext context)
    {
        super.appendAttributesToResponse(response, context);

        appendTagAttributeToResponse(response, "href",
                        AjaxUtils.ajaxComponentActionUrl(context));

        if (alwaysDynamicInContext(context))
        {
            appendTagAttributeToResponse(response, "alwaysDynamic", "true");
        }
    }


    //~ Static/instance variables .............................................

    protected WOAssociation _alwaysDynamic;
}
