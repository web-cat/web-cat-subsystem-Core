/*==========================================================================*\
 |  $Id: WCTreeHeadings.java,v 1.1 2011/05/13 19:43:46 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2009 Virginia Tech
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

import org.webcat.ui._base.WCTableSubcomponent;
import org.webcat.ui._base.WCTreeSubcomponent;
import org.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOComponentReference;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.appserver._private.WOHTMLBareString;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import er.extensions.appserver.ERXWOContext;

//-------------------------------------------------------------------------
/**
 * A component that contains {@link WCTableHeading} instances. This component
 * also provides the paging controls for the table.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:43:46 $
 */
public class WCTreeHeadings extends WCTreeSubcomponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTreeHeadings(WOContext context)
    {
        super(context);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (tree() == null)
        {
            throw new IllegalStateException("WCTreeHeadings must be an "
                    + "immediate child of WCTree.");
        }

        cachedNumberOfColumns = null;

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    /**
     * Returns the number of columns in the table, computed by counting the
     * number of {@link WCTableHeading} elements contained as immediate children
     * of this element. This is used to determine the "colspan" attribute of
     * the header cell that contains the toolbar controls.
     *
     * @return the number of columns in the table
     */
    public Integer numberOfColumns()
    {
        if (cachedNumberOfColumns == null)
        {
            // This is a big hack. We ask the children of WCTableHeadings to
            // append themselves to a temporary response, and then we count the
            // number of occurrences of "<th" that are in it.

            int count = 0;

            WOElement childTemplate = _childTemplate();
            WOComponent component = context().component();

            ERXWOContext.contextDictionary().setObjectForKey(true,
                    WCTreeHeading.COUNTING_HEADINGS_KEY);
            context()._setCurrentComponent(component.parent());

            WOResponse fakeResponse = new WOResponse();
            childTemplate.appendToResponse(fakeResponse, context());
            String headings = fakeResponse.contentString().toLowerCase();

            context()._setCurrentComponent(component);
            ERXWOContext.contextDictionary().removeObjectForKey(
                    WCTreeHeading.COUNTING_HEADINGS_KEY);

            int index = -1;
            while ((index = headings.indexOf("<th", index + 1)) != -1)
            {
                count++;
            }

            if (tree().canSelectItems)
            {
                count++;
            }

            cachedNumberOfColumns = count;
            tree().numberOfColumns = count;
        }

        return cachedNumberOfColumns;
    }


    //~ Static/instance variables .............................................

    private Integer cachedNumberOfColumns;
}
