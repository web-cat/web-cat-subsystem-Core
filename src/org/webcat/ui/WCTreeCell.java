/*==========================================================================*\
 |  $Id$
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
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSet;
import org.webcat.core.WCComponent;
import org.webcat.ui._base.WCTreeSubcomponent;
import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.ComponentIDGenerator;

//-------------------------------------------------------------------------
/**
 * A cell in a {@link WCTree}.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class WCTreeCell extends WCTreeSubcomponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTreeCell(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public ComponentIDGenerator idFor = new ComponentIDGenerator(this);


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
        NSArray roots = treeModel().arrangedChildrenOfObject(itemFromParent());
        return (roots != null && roots.count() > 0);
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
        js.dijit(idFor.get("toggleSpinner")).call("start");
        js.style(idFor.get("toggleControl"), "display", "none");
        js.style(idFor.get("toggleSpinner"), "display", "inline-block");
        return js.toString(true);
    }
}
