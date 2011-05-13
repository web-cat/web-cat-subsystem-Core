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

import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.JSHash;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

//-------------------------------------------------------------------------
/**
 * An item in a drop-down list.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class WCDropDownItem extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCDropDownItem(WOContext context)
    {
        super(context);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public boolean synchronizesVariablesWithBindings()
    {
        return false;
    }


    // ----------------------------------------------------------
    public boolean isSelectable()
    {
        return this.valueForBooleanBinding("isSelectable", true);
    }


    // ----------------------------------------------------------
    public boolean hasAction()
    {
        return hasBinding("action");
    }


    // ----------------------------------------------------------
    public WOActionResults action()
    {
        return (WOActionResults) valueForBinding("action");
    }


    // ----------------------------------------------------------
    public String href()
    {
        return (String) valueForBinding("href");
    }


    // ----------------------------------------------------------
    public boolean remote()
    {
        return valueForBooleanBinding("remote", false);
    }


    // ----------------------------------------------------------
    public JavascriptGenerator selectItem()
    {
        return WCDropDownList.currentDropDownList().selectCurrentItem();
    }


    // ----------------------------------------------------------
    public WOActionResults clickItem()
    {
        WCDropDownList.currentDropDownList().selectCurrentItem();
        return action();
    }


    // ----------------------------------------------------------
    public boolean isRenderingTitleArea()
    {
        return WCDropDownList.currentDropDownList().isRenderingTitleArea();
    }


    // ----------------------------------------------------------
    public String hideMenuScript()
    {
        WCDropDownList list = WCDropDownList.currentDropDownList();

        JavascriptGenerator js = new JavascriptGenerator();
        js.append("var event=window.event;");
        js.append(list.hideMenu(true));

        return js.toString(true);
    }


    // ----------------------------------------------------------
    public String hideMenuAndUpdateSelectionScript()
    {
        WCDropDownList list = WCDropDownList.currentDropDownList();

        JavascriptGenerator js = new JavascriptGenerator();
        js.append("var event=window.event;");
        js.append(list.hideMenu(true));

        if (list.title == null)
        {
            String selectionId = list.selectionId();

            js.call("webcat.dropDownList.updateSelection", selectionId,
                    JSHash.code("this"));
        }

        return js.toString(true);
    }
}
