/*==========================================================================*\
 |  $Id: WCRadioButton.java,v 1.2 2011/05/13 19:43:18 aallowat Exp $
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

package org.webcat.ui;

import org.webcat.ui._base.DojoFormElement;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

//--------------------------------------------------------------------------
/**
 * A radio button.
 *
 * @author Tony Allevato
 * @version $Id: WCRadioButton.java,v 1.2 2011/05/13 19:43:18 aallowat Exp $
 */
public class WCRadioButton extends DojoFormElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public WCRadioButton(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("span", someAssociations, template);

        _checked = _associations.removeObjectForKey("checked");
        _selection = _associations.removeObjectForKey("selection");

        if(_checked == null && null == _value
                || _checked != null && _value != null
                || _checked != null && !_checked.isValueSettable()
                || _value != null && _selection != null && !_selection.isValueSettable())
        {
            throw new WODynamicElementCreationException(
                    "<" + getClass().getName()+ "> : Bad attribute list.");
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return "dijit.form.RadioButton";
    }


    // ----------------------------------------------------------
    @Override
    public String inputTypeInContext(WOContext context)
    {
        return "radio";
    }


    // ----------------------------------------------------------
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        insertNameMappingIntoContext(context);

        WOComponent component = context.component();

        if (!isDisabledInContext(context) && context.wasFormSubmitted())
        {
            String name = nameInContext(context);

            if (name != null)
            {
                NSArray<Object> formValues = request.formValuesForKey(
                        nameInContext(context));

                if (formValues != null)
                {
                    Object value;
                    if (_value == null)
                    {
                        value = context.elementID();
                    }
                    else
                    {
                        value = _value.valueInComponent(component);
                    }

                    boolean selected = isValueInInputValues(value, formValues);

                    if (_value != null && _selection != null && selected)
                    {
                        _selection.setValue(value, component);
                    }

                    if (_checked != null)
                    {
                        _checked.setValue(selected, component);
                    }
                }
            }
        }

        removeNameMappingFromContext(context);
    }

    // ----------------------------------------------------------
    public void appendAttributesToResponse(WOResponse response,
            WOContext context)
    {
        super.appendAttributesToResponse(response, context);

        WOComponent component = context.component();

        if(_value != null)
        {
            Object value = _value.valueInComponent(component);
            if(value != null && _selection != null)
            {
                Object selection = _selection.valueInComponent(component);

                if(selection != null &&
                        selection.toString().equals(value.toString()))
                {
                    _appendTagAttributeAndValueToResponse(response,
                            "checked", "checked", false);
                }
            }
        }
        else
        {
            _appendTagAttributeAndValueToResponse(response, "value",
                    context.elementID(), false);
        }

        if(checkedInContext(context))
        {
            _appendTagAttributeAndValueToResponse(response,
                    "checked", "checked", false);
        }
    }


    // ----------------------------------------------------------
    protected boolean checkedInContext(WOContext context)
    {
        if (_checked == null)
        {
            return false;
        }
        else
        {
            return _checked.booleanValueInComponent(context.component());
        }
    }


    //~ Static/instance variables .............................................

    protected WOAssociation _checked;
    protected WOAssociation _selection;
}
