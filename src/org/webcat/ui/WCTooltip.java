/*==========================================================================*\
 |  $Id: WCTooltip.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

//------------------------------------------------------------------------
/**
 * A tooltip that can be attached to one or more DOM elements.
 * 
 * <h2>Bindings</h2>
 * <table>
 * <tr>
 * <td>{@code label}</td>
 * <td>The label that will be displayed in the tooltip. If not specified,
 * component content will be used instead.</td>
 * </tr>
 * <tr>
 * <td>{@code connectId}</td>
 * <td>The identifier(s) of the DOM element(s) that the tooltip should be
 * attached to. To connect the tooltip to multiple elements, use a comma-
 * separated list of identifiers.</td>
 * </tr>
 * <tr>
 * <td>{@code position}</td>
 * <td>The position of the tooltip relative to the element it is connected to.
 * Valid options are "above", "below", "left", and "right".</td>
 * </tr>
 * <tr>
 * <td>{@code showDelay}</td>
 * <td>An integer specifying the number of milliseconds that will elapse before
 * the tooltip appears. Defaults to 400.</td>
 * </tr>
 * </table>
 * 
 * @author Tony Allevato
 * @version $Id: WCTooltip.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class WCTooltip extends DojoElement
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTooltip(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("span", someAssociations, template);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return "dijit.Tooltip";
    }
    
    
    // ----------------------------------------------------------
    @Override
    public void appendAttributesToResponse(WOResponse response,
            WOContext context)
    {
        super.appendAttributesToResponse(response, context);

        // Ensure that the tooltip content is hidden as the page loads.
        _appendTagAttributeAndValueToResponse(response, "style",
                "display: none", false);
    }
}
