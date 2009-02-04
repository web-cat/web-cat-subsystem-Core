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

package net.sf.webcat.ui._base;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

// ------------------------------------------------------------------------
/**
 * A Dojo element that lets the user specify an arbitrary {@code dojoType} in
 * the WOD file. Use this (actually, one of the more conveniently named
 * subclasses WCDiv or WCSpan) instead of {@code WOGenericElement} or straight
 * &lt;div&gt; tags to create generic Dojo elements that properly generate the
 * end-tag when there is no content.
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class DojoGenericElement extends DojoElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public DojoGenericElement(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);
        
        _dojoType = _associations.removeObjectForKey("dojoType");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        // Unused; the dynamic type is computed in
        // _appendDojoTypeAttributeToResponse.

        return null;
    }
    
    
    // ----------------------------------------------------------
    /**
     * Appends the dojoType attribute and value to the response.
     */
    @Override
    protected void _appendDojoTypeAttributeToResponse(WOResponse response,
            WOContext context)
    {
        String dojoType = (String) _dojoType.valueInComponent(
                context.component());

        if (dojoType != null && dojoType.length() > 0)
        {
            _appendTagAttributeAndValueToResponse(response, "dojoType",
                    dojoType, false);
        }
    }
    

    //~ Static/instance variables .............................................

    protected WOAssociation _dojoType;
}
