/*==========================================================================*\
 |  $Id: WCDropDownList.java,v 1.2 2011/05/13 19:43:18 aallowat Exp $
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
import org.webcat.ui.util.ComponentIDGenerator;
import org.webcat.ui.util.JSHash;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSArray;
import er.extensions.appserver.ERXWOContext;

//-------------------------------------------------------------------------
/**
 * A menu-like component that drops down a list when the mouse hovers over it.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2011/05/13 19:43:18 $
 */
public class WCDropDownList extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCDropDownList(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public String id;
    public String menuId;
    public String title;
    public String noSelectionString;
    public String maximumSize;

    public ComponentIDGenerator idFor = new ComponentIDGenerator(this);


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (id == null)
        {
            id = idFor.get();
        }

        if (menuId == null)
        {
            menuId = idFor.get("menu");
        }

        WCDropDownList oldList = setCurrentDropDownList(this);

        super.appendToResponse(response, context);

        setCurrentDropDownList(oldList);
    }


    // ----------------------------------------------------------
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        WCDropDownList oldList = setCurrentDropDownList(this);

        super.takeValuesFromRequest(request, context);

        setCurrentDropDownList(oldList);
    }


    // ----------------------------------------------------------
    @Override
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        WCDropDownList oldList = setCurrentDropDownList(this);

        WOActionResults result = super.invokeAction(request, context);

        setCurrentDropDownList(oldList);

        return result;
    }


    // ----------------------------------------------------------
    public String triggerSetItemToSelection()
    {
        setItem(selection());
        isRenderingTitleArea = true;
        return null;
    }


    // ----------------------------------------------------------
    public String triggerClearItem()
    {
        setItem(null);
        isRenderingTitleArea = false;
        return null;
    }


    // ----------------------------------------------------------
    protected boolean isRenderingTitleArea()
    {
        return isRenderingTitleArea;
    }


    // ----------------------------------------------------------
    public NSArray<?> list()
    {
        return valueForNSArrayBindings("list", null);
    }


    // ----------------------------------------------------------
    public void setList(NSArray<?> list)
    {
        // Do nothing; keep KVC quiet.
    }


    // ----------------------------------------------------------
    public Object item()
    {
        return valueForBinding("item");
    }


    // ----------------------------------------------------------
    public void setItem(Object item)
    {
        setValueForBinding(item, "item");
    }


    // ----------------------------------------------------------
    public int index()
    {
        return valueForIntegerBinding("index", 0);
    }


    // ----------------------------------------------------------
    public void setIndex(int index)
    {
        if (canSetValueForBinding("index"))
        {
            setValueForBinding(index, "index");
        }
    }


    // ----------------------------------------------------------
    public Object selection()
    {
        return valueForBinding("selection");
    }


    // ----------------------------------------------------------
    public void setSelection(Object selection)
    {
        if (canSetValueForBinding("selection"))
        {
            setValueForBinding(selection, "selection");
        }
    }


    // ----------------------------------------------------------
    public boolean hasDisplayString()
    {
        return hasBinding("displayString");
    }


    // ----------------------------------------------------------
    public String displayString()
    {
        return valueForStringBinding("displayString", null);
    }


    // ----------------------------------------------------------
    public String displayStringForItem()
    {
        if (hasDisplayString())
        {
            return displayString();
        }
        else
        {
            return item().toString();
        }
    }


    // ----------------------------------------------------------
    public boolean hasContent()
    {
        if (_childTemplate() instanceof WODynamicGroup)
        {
            WODynamicGroup group = (WODynamicGroup) _childTemplate();
            return group.hasChildrenElements();
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    public void setDisplayString(String value)
    {
        // Do nothing; keep KVC quiet.
    }


    // ----------------------------------------------------------
    public String cssListStyle()
    {
        if (maximumSize != null)
        {
            StringBuilder buffer = new StringBuilder();

            String[] parts = maximumSize.split(",");

            if (parts[0].length() > 0)
            {
                buffer.append("max-width: ");
                buffer.append(parts[0]);
            }

            if (parts.length > 1 && parts[1].length() > 0)
            {
                buffer.append("; max-height: " + parts[1]);
            }

            return buffer.toString();
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public String selectionId()
    {
        return idFor.get("selection");
    }


    // ----------------------------------------------------------
    public String displayStringForSelection()
    {
        if (title != null)
        {
            return title;
        }
        else
        {
            setItem(selection());
            return displayString();
        }
    }


    // ----------------------------------------------------------
    protected JavascriptGenerator selectCurrentItem()
    {
        setSelection(item());
        return null;
    }


    // ----------------------------------------------------------
    public String showMenuScript()
    {
        JavascriptGenerator js = new JavascriptGenerator();
        js.call("webcat.dropDownList.showDropDown",
                JSHash.code("event"),
                id, menuId);
        return js.toString(true);
    }


    // ----------------------------------------------------------
    public String hideMenuScript()
    {
        return hideMenu(false).toString(true);
    }


    // ----------------------------------------------------------
    protected JavascriptGenerator hideMenu(boolean force)
    {
        JavascriptGenerator js = new JavascriptGenerator();
        js.call("webcat.dropDownList.hideDropDown",
                JSHash.code("event"),
                id, menuId, force);
        return js;
    }


    // ----------------------------------------------------------
    public static WCDropDownList currentDropDownList()
    {
        return (WCDropDownList) ERXWOContext.contextDictionary().objectForKey(
                CURRENT_DROP_DOWN_LIST_KEY);
    }


    // ----------------------------------------------------------
    public static WCDropDownList setCurrentDropDownList(WCDropDownList list)
    {
        WCDropDownList oldList =
            (WCDropDownList) ERXWOContext.contextDictionary().objectForKey(
                    CURRENT_DROP_DOWN_LIST_KEY);

        if (list == null)
        {
            ERXWOContext.contextDictionary().removeObjectForKey(
                    CURRENT_DROP_DOWN_LIST_KEY);
        }
        else
        {
            ERXWOContext.contextDictionary().setObjectForKey(list,
                    CURRENT_DROP_DOWN_LIST_KEY);
        }

        return oldList;
    }


    //~ Static/instance variables .............................................

    private static final String CURRENT_DROP_DOWN_LIST_KEY =
        "org.webcat.ui.WCDropDownList.currentDropDownList";

    private boolean isRenderingTitleArea;
}
