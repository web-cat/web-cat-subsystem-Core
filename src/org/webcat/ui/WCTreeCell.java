/*==========================================================================*\
 |  $Id: WCTreeCell.java,v 1.3 2014/06/16 15:57:54 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011 Virginia Tech
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

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import org.webcat.ui._base.WCTreeSubcomponent;
import org.webcat.ui.generators.JavascriptGenerator;

//-------------------------------------------------------------------------
/**
 * A cell in a {@link WCTree}.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.3 $, $Date: 2014/06/16 15:57:54 $
 */
public class WCTreeCell extends WCTreeSubcomponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTreeCell(WOContext context)
    {
        super(context);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public Object itemFromParent()
    {
        return WCTreeItems.currentTreeItems().item;
    }


    // ----------------------------------------------------------
    public WCIndexPath indexPathFromParent()
    {
        return WCTreeItems.currentTreeItems().indexPath;
    }


    // ----------------------------------------------------------
    public String cellStyle()
    {
        return "padding-left: "
            + (indexPathFromParent().length() - 1) * 16 + "px";
    }


    // ----------------------------------------------------------
    public boolean isExpandable()
    {
        //NSArray roots = treeModel().arrangedChildrenOfObject(itemFromParent());
        //return (roots != null && roots.count() > 0);
        return treeModel().objectHasArrangedChildren(itemFromParent());
    }


    // ----------------------------------------------------------
    public boolean isExpanded()
    {
        return tree().isItemExpanded(itemFromParent());
    }


    // ----------------------------------------------------------
    public WOActionResults toggleExpanded()
    {
        tree().toggleItemExpanded(itemFromParent());
        return tree().refreshTable();
    }


    // ----------------------------------------------------------
    public String toggleControlClass()
    {
        return "WCTreeControl WCTreeControl"
            + (isExpanded() ? "Expanded" : "Collapsed");
    }


    // ----------------------------------------------------------
    public String toggleControlTitle()
    {
        return isExpanded() ? "Expand this item" : "Collapse this item";
    }


    // ----------------------------------------------------------
    public String startSpinnerScript()
    {
        JavascriptGenerator js = new JavascriptGenerator();
        js.dijit(idFor("toggleSpinner")).call("start");
        js.style(idFor("toggleControl"), "display", "none");
        js.style(idFor("toggleSpinner"), "display", "inline-block");
        return js.toString(true);
    }


    // ----------------------------------------------------------
    public String idFor(String tag)
    {
        return tree().idFor.get(tag + "_")
            + WCTreeItems.currentTreeItems().indexPath.toString().replace(
                    '.', '_');
    }
}
