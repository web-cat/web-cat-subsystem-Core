/*==========================================================================*\
 |  $Id: WCTableRows.java,v 1.1 2010/10/28 00:37:30 aallowat Exp $
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
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import er.extensions.appserver.ERXWOContext;

//-------------------------------------------------------------------------
/**
 * <p>
 * Provides the iteration over the items in the current batch of the display
 * group. This element should contain a single {@link WCTableRow} component
 * that defines the cells for a row of the table.
 * </p>
 * Bindings
 * <dl>
 * <dt>item</dt>
 * <dd>The value bound to this key will be updated every time through the set
 * of displayed objects in the table with the current item in the display group
 * (like a <code>WORepetition</code>).</dd>
 * <dt>noRowsMessage</dt>
 * <dd>A message to display when there are no rows to display in the table.
 * The content of this message should make sense both in the situation where
 * the display group has no objects at all, as well as in the situation where
 * the user has done a search that returned no results.</dd>
 * </dl>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2010/10/28 00:37:30 $
 */
public class WCTableRows extends WCTableSubcomponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTableRows(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public Object item;
    public int rowIndex;
    public String noRowsMessage;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        WCTableRows oldTableRows = setCurrentTableRows(this);

        super.appendToResponse(response, context);

        setCurrentTableRows(oldTableRows);
    }


    // ----------------------------------------------------------
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        WCTableRows oldTableRows = setCurrentTableRows(this);

        super.takeValuesFromRequest(request, context);

        setCurrentTableRows(oldTableRows);
    }


    // ----------------------------------------------------------
    @Override
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        WCTableRows oldTableRows = setCurrentTableRows(this);

        WOActionResults result = super.invokeAction(request, context);

        setCurrentTableRows(oldTableRows);

        return result;
    }


    // ----------------------------------------------------------
    public void setItem(Object object)
    {
        item = object;
        pushValuesToParent();
    }


    // ----------------------------------------------------------
    public static WCTableRows currentTableRows()
    {
        return (WCTableRows) ERXWOContext.contextDictionary().objectForKey(
                CURRENT_TABLE_ROWS_KEY);
    }


    // ----------------------------------------------------------
    public static WCTableRows setCurrentTableRows(WCTableRows tableRows)
    {
        WCTableRows oldTableRows =
            (WCTableRows) ERXWOContext.contextDictionary().objectForKey(
                CURRENT_TABLE_ROWS_KEY);

        if (tableRows == null)
        {
            ERXWOContext.contextDictionary().removeObjectForKey(
                    CURRENT_TABLE_ROWS_KEY);
        }
        else
        {
            ERXWOContext.contextDictionary().setObjectForKey(tableRows,
                    CURRENT_TABLE_ROWS_KEY);
        }

        return oldTableRows;
    }


    //~ Static/instance variables .............................................

    private static final String CURRENT_TABLE_ROWS_KEY =
        "org.webcat.ui.WCTableRows.currentTableRows";
}
