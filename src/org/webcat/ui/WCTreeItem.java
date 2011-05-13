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

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import er.extensions.components.ERXComponentUtilities;
import org.webcat.core.WCComponent;
import org.webcat.ui._base.WCTableSubcomponent;
import org.webcat.ui._base.WCTreeSubcomponent;

//-------------------------------------------------------------------------
/**
 * <p>
 * Represents a row in a {@link WCTree}. This component automatically handles
 * the visual banding of rows in the tree, and also provides the affordances
 * for selecting and deselecting rows if the tree instance supports it.
 * </p><p>
 * This component can only be placed inside a {@link WCTreeItems} component.
 * </p>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class WCTreeItem extends WCTreeSubcomponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTreeItem(WOContext context)
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
        return WCTreeItems.currentTreeItems().rowIndex;
    }


    // ----------------------------------------------------------
    public Object itemFromParent()
    {
        return WCTreeItems.currentTreeItems().item;
    }


    // ----------------------------------------------------------
    public String componentNameForSelectionControl()
    {
        return tree().multipleSelection ? "WCCheckBox" : "WCRadioButton";
    }


    // ----------------------------------------------------------
    public String selectionControlGroupName()
    {
        return tree().idFor.get("selectionControlGroup");
    }


    // ----------------------------------------------------------
    public boolean canSelectItem()
    {
        return treeModel().canSelectObject(itemFromParent());
    }


    // ----------------------------------------------------------
    public boolean isItemSelected()
    {
        return treeModel().selectedObjects().containsObject(itemFromParent());
    }


    // ----------------------------------------------------------
    public void setIsItemSelected(boolean selected)
    {
        Object item = itemFromParent();

        if (selected)
        {
            treeModel().selectObject(item);
        }
        else
        {
            treeModel().deselectObject(item);
        }
    }


    // ----------------------------------------------------------
    public String rowId()
    {
        return tree().idFor.get("path_")
            + WCTreeItems.currentTreeItems().indexPath.toString().replace(
                    '.', '_');
    }
}
