/*==========================================================================*\
 |  $Id: DojoSingleSelectionListFormElement.java,v 1.1 2010/05/11 14:52:00 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2008 Virginia Tech
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

import java.util.Collections;
import java.util.List;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

// ------------------------------------------------------------------------
/**
 * A base class for Dojo widgets that act as a single-selection list, such as
 * ComboBox and FilteringSelect.
 *
 * @author Tony Allevato
 * @version $Id: DojoSingleSelectionListFormElement.java,v 1.1 2010/05/11 14:52:00 aallowat Exp $
 */
public abstract class DojoSingleSelectionListFormElement extends DojoFormElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public DojoSingleSelectionListFormElement(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);

        _list = _associations.removeObjectForKey("list");
        _item = _associations.removeObjectForKey("item");
        _index = _associations.removeObjectForKey("index");
        _displayString = _associations.removeObjectForKey("displayString");
        _searchString = _associations.removeObjectForKey("searchString");
        _selection = _associations.removeObjectForKey("selection");
        _noSelectionString = _associations.removeObjectForKey(
                "noSelectionString");
        _selectedValue = _associations.removeObjectForKey("selectedValue");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public boolean hasContent()
    {
        return true;
    }


    // ----------------------------------------------------------
    @Override
    protected boolean defaultEscapeHTML()
    {
        return false;
    }


    // ----------------------------------------------------------
    protected List<?> listInContext(WOContext context)
    {
        List<?> list = Collections.EMPTY_LIST;

        if(_list != null)
        {
            Object value = _list.valueInComponent(context.component());

            if(value instanceof NSArray)
            {
                list = ((NSArray<?>)value).vector();
            }
            else if(value instanceof List)
            {
                list = (List<?>)value;
            }
        }

        return list;
    }


    // ----------------------------------------------------------
    protected String noSelectionStringInContext(WOContext context)
    {
        Object value = null;

        if(_noSelectionString != null)
        {
            value = _noSelectionString.valueInComponent(context.component());
        }

        if(value == null)
        {
            return "";
        }
        else
        {
            return value.toString();
        }
    }


    // ----------------------------------------------------------
    protected Object selectionInContext(WOContext context)
    {
        Object selection = null;

        if(_selection != null)
            selection = _selection.valueInComponent(context.component());

        if(selection == null)
        {
            if(_selectedValue != null)
            {
                selection =
                    _selectedValue.valueInComponent(context.component());
            }
        }

        return selection;
    }


    // ----------------------------------------------------------
    protected String optionTagName()
    {
        if ("select".equalsIgnoreCase(elementName()))
        {
            return "option";
        }
        else
        {
            return "span";
        }
    }


    // ----------------------------------------------------------
    public void appendOptionTagToResponse(WOResponse response,
            WOContext context, String value, boolean isSelected,
            String content, String searchString)
    {
        response.appendContentCharacter('<');
        response.appendContentString(optionTagName());

        _appendTagAttributeAndValueToResponse(
                response, "value", value, _value != null);

        if(isSelected)
        {
            _appendTagAttributeAndValueToResponse(
                    response, "selected", "selected", false);
        }

        if (searchString != null)
        {
            _appendTagAttributeAndValueToResponse(
                    response, "searchKey", searchString, true);
        }

        response.appendContentCharacter('>');

        if(escapeHTMLInContext(context))
        {
            response.appendContentHTMLString(content);
        }
        else
        {
            response.appendContentString(content);
        }

        response.appendContentString("</");
        response.appendContentString(optionTagName());
        response.appendContentCharacter('>');
    }


    // ----------------------------------------------------------
    protected String displayStringInContext(WOContext context)
    {
        Object value = null;

        if (_displayString != null)
        {
            value = _displayString.valueInComponent(context.component());
        }

        return (value != null) ? value.toString() : "";
    }


    // ----------------------------------------------------------
    protected String searchStringInContext(WOContext context)
    {
        Object value = null;

        if (_searchString != null)
        {
            value = _searchString.valueInComponent(context.component());
        }

        return (value != null) ? value.toString() : null;
    }


    // ----------------------------------------------------------
    protected String optionStringInContext(WOContext context, Object value,
            int index)
    {
        if(_item != null && _item.isValueSettable())
        {
            _item.setValue(value, context.component());
        }

        if(_index != null && _index.isValueSettable())
        {
            _index.setValue(Integer.valueOf(index), context.component());
        }

        String optionString = displayStringInContext(context);
        if(optionString.length() == 0)
        {
            optionString = valueStringInContext(context);
        }

        return optionString;
    }


    // ----------------------------------------------------------
    @Override
    public void appendAttributesToResponse(
            WOResponse response, WOContext context)
    {
        super.appendAttributesToResponse(response, context);

        _appendTagAttributeAndValueToResponse(
                response, "labelType", "html", false);
        _appendTagAttributeAndValueToResponse(
                response, "labelAttr", "label", false);
    }


    // ----------------------------------------------------------
    @Override
    public void appendChildrenToResponse(WOResponse response, WOContext context)
    {
        String noSelectionString = noSelectionStringInContext(context);

        if(noSelectionString.length() > 0)
        {
            appendOptionTagToResponse(response, context,
                    NO_SELECTION_PLACEHOLDER, false, noSelectionString,
                    noSelectionString);
        }

        List<?> optionList = listInContext(context);

        if(optionList.size() > 0)
        {
            Object selection = selectionInContext(context);

            for(int i = 0; i < optionList.size(); i++)
            {
                Object option = optionList.get(i);

                String optionString = optionStringInContext(context, option, i);

                if(optionString.length() == 0)
                    optionString = option.toString();

                String searchString = searchStringInContext(context);
                String valueString = valueStringInContext(context);

                if(valueString.length() == 0)
                    valueString = Integer.toString(i);

                boolean selected = false;

                if(selection != null)
                {
                    if(_selection == null)
                    {
                        selected = selection.equals(valueString);
                    }
                    else
                    {
                        selected = selection.equals(option);
                    }
                }

                appendOptionTagToResponse(response, context,
                        valueString, selected, optionString, searchString);
            }
        }

        super.appendChildrenToResponse(response, context);
    }


    //~ Static/instance variables .............................................

    protected static final String NO_SELECTION_PLACEHOLDER =
        "dijit.form.internal.DijitSingleListForm.noSelection";

    protected WOAssociation _list;
    protected WOAssociation _item;
    protected WOAssociation _index;
    protected WOAssociation _displayString;
    protected WOAssociation _searchString;
    protected WOAssociation _selection;
    protected WOAssociation _noSelectionString;
    protected WOAssociation _selectedValue;
}
