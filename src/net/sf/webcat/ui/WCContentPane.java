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

import net.sf.webcat.ui._base.DojoElement;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXWOContext;

//------------------------------------------------------------------------
/**
 * A Dojo content pane that provides an Ajax interface. After assigning the
 * pane an element identifier, you can refresh the pane's content by calling
 * its refresh method:  <code>dijit.byId("paneId").refresh()</code>
 *
 * <h2>Bindings</h2>
 * <table>
 * <tr>
 * <td>{@code refreshOnShow}</td>
 * <td>A boolean value indicating whether the pane should refresh (redownload)
 * its content when it goes from hidden to shown. Defaults to false.</td>
 * </tr>
 * </table>
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class WCContentPane extends DojoElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public WCContentPane(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("div", someAssociations, template);

    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return "webcat.ContentPane";
    }


    // ----------------------------------------------------------
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        Object result = null;

        if (AjaxUtils.shouldHandleRequest(request, context,
                _containerID(context)))
        {
            result = handleRequest(request, context);
            AjaxUtils.updateMutableUserInfoWithAjaxInfo(context);

            if (result == context.page())
            {
                result = null;
            }

            if (result == null)
            {
                result = AjaxUtils.createResponse(request, context);
            }
        }
        else if (hasChildrenElements())
        {
            result = super.invokeAction(request, context);
        }

        return (WOActionResults) result;
    }


    // ----------------------------------------------------------
    public void appendAttributesToResponse(WOResponse response,
            WOContext context)
    {
        super.appendAttributesToResponse(response, context);

        appendTagAttributeToResponse(response, "href",
                        AjaxUtils.ajaxComponentActionUrl(context));
    }


    // ----------------------------------------------------------
    public WOActionResults handleRequest(WORequest request, WOContext context)
    {
        String id = _containerID(context);

        WOResponse response = AjaxUtils.createResponse(request, context);
        AjaxUtils.setPageReplacementCacheKey(context, id);

        if (hasChildrenElements())
        {
            appendChildrenToResponse(response, context);
        }

        return null;
    }


    // ----------------------------------------------------------
    protected String _containerID(WOContext context)
    {
        String id = (String) valueForBinding("id", context.component());

        if (id == null)
        {
            id = ERXWOContext.safeIdentifierName(context, false);
        }

        return id;
    }
}
