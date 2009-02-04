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

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import er.ajax.AjaxUtils;

//------------------------------------------------------------------------
/**
 * An expandable/collapsable panel that displays a clickable title. The content
 * of the pane is loaded on-demand; that is, if the pane is closed on page-load,
 * then its children will not be rendered until it is opened for the first time.
 * 
 * <h2>Bindings</h2>
 * <table>
 * <tr>
 * <td>{@code title}</td>
 * <td>The title that will be displayed in the pane.</td>
 * </tr>
 * <tr>
 * <td>{@code open}</td>
 * <td>A boolean value indicating whether the title pane should be initially
 * open or not. Defaults to true.</td>
 * </tr>
 * <tr>
 * <td>{@code duration}</td>
 * <td>An integer specifying the number of milliseconds for the expand/collapse
 * effect of the pane. Defaults to 250.</td>
 * </tr>
 * </table>
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class WCTitlePane extends WCContentPane
{
    //~ Constructors ..........................................................
    
    // ----------------------------------------------------------
    public WCTitlePane(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement element)
    {
        super("div", someAssociations, element);
        
        _open = _associations.removeObjectForKey("open");
    }
    
    
    //~ KVC attributes (must be public) .......................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return "dijit.TitlePane";
    }

    
    // ----------------------------------------------------------
    @Override
    public void appendAttributesToResponse(WOResponse response,
            WOContext context)
    {
        super.appendAttributesToResponse(response, context);
        
        appendOpenAttributeToResponse(response, context);
    }


    // ----------------------------------------------------------
    protected boolean openInContext(WOContext context)
    {
        if (_open != null)
        {
            return _open.booleanValueInComponent(context.component());
        }
        else
        {
            return true;
        }
    }


    // ----------------------------------------------------------
    protected void appendOpenAttributeToResponse(WOResponse response,
            WOContext context)
    {
        if (_open != null)
        {
            appendTagAttributeToResponse(response, "open",
                    openInContext(context));
        }
    }
    

    // ----------------------------------------------------------
    @Override
    public void appendChildrenToResponse(WOResponse response, WOContext context)
    {
        WORequest request = context.request();

        // If the title pane is set to be closed on page-load, then any future
        // attempt to render its contents will occur as an Ajax request. So, we
        // only want to render its children if the pane is open on page-load or
        // on any Ajax request.

        if (openInContext(context) || (AjaxUtils.isAjaxRequest(request) &&
                AjaxUtils.shouldHandleRequest(request, context, null)))
        {
            super.appendChildrenToResponse(response, context);
        }
    }


    //~ Static/instance variables .............................................

    protected WOAssociation _open;
}
