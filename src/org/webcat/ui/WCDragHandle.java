/*==========================================================================*\
 |  $Id: WCDragHandle.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.foundation.NSDictionary;

//------------------------------------------------------------------------
/**
 * A drag handle that can be placed on an item in a drag-and-drop source, such
 * as a row in a WCStyledTable.
 *
 * @author Tony Allevato
 * @version $Id: WCDragHandle.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class WCDragHandle extends WOHTMLDynamicElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public WCDragHandle(String aName,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("div", someAssociations, template);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    protected void _appendCloseTagToResponse(WOResponse response,
            WOContext context)
    {
        // There appear to be some Dojo issues if an element without content
        // uses <div /> instead of <div></div>, so we fix that here.

        response.appendContentString("</");
        response.appendContentString(elementName());
        response.appendContentCharacter('>');
    }


    // ----------------------------------------------------------
    @Override
    public void appendAttributesToResponse(
            WOResponse response, WOContext context)
    {
        super.appendAttributesToResponse(response, context);

        _appendTagAttributeAndValueToResponse(response, "class",
                "dojoDndHandle", false);
    }
}
