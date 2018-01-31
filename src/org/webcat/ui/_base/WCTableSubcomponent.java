/*==========================================================================*\
 |  $Id: WCTableSubcomponent.java,v 1.1 2010/10/28 00:37:30 aallowat Exp $
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

package org.webcat.ui._base;

import org.webcat.ui.WCTable;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import er.extensions.appserver.ERXDisplayGroup;

//-------------------------------------------------------------------------
/**
 * A class that can be extended by components that reside inside a WCTable,
 * and provides convenience methods for referring to that table and its
 * display group.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2010/10/28 00:37:30 $
 */
public class WCTableSubcomponent extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the WCTableSubComponent class.
     *
     * @param context the context
     */
    public WCTableSubcomponent(WOContext context)
    {
        super(context);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the table that this subcomponent is nested inside.
     *
     * @return the table that contains this subcomponent
     */
    public WCTable table()
    {
        return WCTable.currentTable();
    }


    // ----------------------------------------------------------
    /**
     * A convenience method to return the display group that is bound to the
     * table that contains this subcomponent.
     *
     * @return the display group that is bound to the table that contains this
     *     subcomponent
     */
    public ERXDisplayGroup<?> displayGroup()
    {
        return table().displayGroup;
    }
}
