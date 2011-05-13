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

package org.webcat.core;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

//-------------------------------------------------------------------------
/**
 * A test page for the {@link WCDropDownList} component.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class WCDropDownListTestPage extends WOComponent
{
    // ----------------------------------------------------------
    /**
     * Creates a new {@code WCDropDownListTestPage} object.
     *
     * @param context the context
     */
    public WCDropDownListTestPage(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public NSMutableArray<String> list1;
    public String item1;
    public String selection1;
    public int index1;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse(WOResponse response, WOContext context)
    {
        list1 = new NSMutableArray<String>();

        for (int i = 1; i <= 10; i++)
        {
            list1.addObject("Item " + i);
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public String displayStringForItem1()
    {
        return "((" + item1 + "))";
    }
}
