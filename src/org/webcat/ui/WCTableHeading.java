/*==========================================================================*\
 |  $Id$
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
import org.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * <p>
 * Represents the heading of a single column in the table. Provides the title
 * of the column and the keypaths used to sort on it.
 * </p><p>
 * This component may only be nested inside WCTableHeadings.
 * </p>
 * Bindings
 * <dl>
 * <dt>title (<code>String</code>)</dt>
 * <dd>The title that will be displayed in the heading.</dd>
 * <dt>sortOnKeyPaths (<code>String</code>)</dt>
 * <dd>One or more comma-separated keypaths that define the sort orderings to
 * use for this column. If this binding is omitted, the column will not be
 * sortable.</dd>
 * </dl>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class WCTableHeading extends WCTableSubcomponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTableHeading(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public String title;
    public String sortOnKeyPaths;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (sortOnKeyPaths != null && table().needsInitialSort)
        {
            table().sortDisplayGroup(sortOnKeyPaths, true);
            table().needsInitialSort = false;
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public boolean isCurrentSortOrder()
    {
        return sortOnKeyPaths.equals(
                table().sortOrderingKeyPathsFromDisplayGroup());
    }


    // ----------------------------------------------------------
    public boolean isAscending()
    {
        return table().isDisplayGroupSortOrderingAscending();
    }


    // ----------------------------------------------------------
    public WOActionResults sort()
    {
        boolean ascending;

        if (isCurrentSortOrder())
        {
            ascending = !table().isDisplayGroupSortOrderingAscending();
        }
        else
        {
            ascending = true;
        }

        return table().sortUsingKeyPaths(sortOnKeyPaths, ascending);
    }


    //~ Static/instance variables .............................................
}
