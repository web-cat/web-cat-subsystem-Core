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
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import er.extensions.components._private.ERXWOForm;

//--------------------------------------------------------------------------
/**
 * A small hack to ERXWOForm that inserts some comment tags after the form open
 * tag so that "shadow buttons" (i.e., hidden buttons that exist to allow div
 * tags like menu items to invoke actions) can be added inside the form. This
 * is necessary since, for example, menu items upon parsing get moved to the
 * end of the document outside of the form, so the buttons cannot be inlined
 * there and still correctly submit the form.
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class WCForm extends ERXWOForm
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCForm(String name,
            NSDictionary<String, WOAssociation> associations,
            WOElement element)
    {
        super(name, associations, element);
    }

    
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    protected void _appendOpenTagToResponse(WOResponse response,
            WOContext context)
    {
        super._appendOpenTagToResponse(response, context);
        
        response.appendContentString(SHADOW_BUTTON_REGION_START);
        response.appendContentString("\n");
        response.appendContentString(SHADOW_BUTTON_REGION_END);
    }
    

    //~ Static/instance variables .............................................

    public static final String SHADOW_BUTTON_REGION_START =
        "<!-- WC SHADOW BUTTON REGION START -->";
    public static final String SHADOW_BUTTON_REGION_END =
        "<!-- WC SHADOW BUTTON REGION END -->";
}
