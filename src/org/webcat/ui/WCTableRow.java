/*==========================================================================*\
 |  $Id: WCTableRow.java,v 1.2 2010/10/29 20:36:15 aallowat Exp $
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

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import er.extensions.components.ERXComponentUtilities;
import org.webcat.core.WCComponent;
import org.webcat.ui._base.WCTableSubcomponent;

//-------------------------------------------------------------------------
/**
 * <p>
 * Represents a row in a {@link WCTable}. This component automatically handles
 * the visual banding of rows in the table, and also provides the affordances
 * for selecting and deselecting rows if the table instance supports it.
 * </p><p>
 * This component can only be placed inside a {@link WCTableRows} component.
 * </p>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2010/10/29 20:36:15 $
 */
public class WCTableRow extends WCTableSubcomponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTableRow(WOContext context)
    {
        super(context);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public boolean showError()
    {
        return ERXComponentUtilities.booleanValueForBinding(
                this, "showError", false);
    }


    // ----------------------------------------------------------
    public boolean showCaution()
    {
        return ERXComponentUtilities.booleanValueForBinding(
                this, "showCaution", false);
    }


    // ----------------------------------------------------------
    public int indexFromParent()
    {
        return WCTableRows.currentTableRows().rowIndex;
    }


    // ----------------------------------------------------------
    public String selectionControlGroupName()
    {
        return table().idFor.get("selectionControlGroup");
    }


    // ----------------------------------------------------------
    public boolean isRowSelected()
    {
        int index = indexFromParent() +
            displayGroup().indexOfFirstDisplayedObject() - 1;

        return displayGroup().selectionIndexes().containsObject(index);
    }


    // ----------------------------------------------------------
    public void setIsRowSelected(boolean selected)
    {
        int index = indexFromParent() +
            displayGroup().indexOfFirstDisplayedObject() - 1;

        NSMutableArray<Integer> selection =
            new NSMutableArray<Integer>(displayGroup().selectionIndexes());

        if (selected)
        {
            if (!selection.containsObject(index))
            {
                selection.addObject(index);
            }
        }
        else
        {
            selection.removeObject(index);
        }

        displayGroup().setSelectionIndexes(selection);
    }
}
