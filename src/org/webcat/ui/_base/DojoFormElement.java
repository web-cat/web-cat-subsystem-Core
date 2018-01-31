/*==========================================================================*\
 |  $Id: DojoFormElement.java,v 1.1 2010/05/11 14:52:00 aallowat Exp $
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

import java.util.List;
import org.webcat.ui.WCButton;
import org.webcat.ui.WCDateTextBox;
import org.webcat.ui.util.DojoConstraintsHelper;
import org.webcat.ui.util.JSHash;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

// ------------------------------------------------------------------------
/**
 * A base class for any Dojo elements that contain a form field that should be
 * submitted as part of a form submit operation on a page.
 *
 * @author Tony Allevato
 * @version $Id: DojoFormElement.java,v 1.1 2010/05/11 14:52:00 aallowat Exp $
 */
public abstract class DojoFormElement extends DojoElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Dojo form element.
     *
     * @param name
     * @param someAssociations
     * @param template
     */
    public DojoFormElement(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);

        _name = _associations.removeObjectForKey("name");
        _value = _associations.removeObjectForKey("value");
        _escapeHTML = _associations.objectForKey("escapeHTML");
        _hidden = _associations.objectForKey("hidden");
        _disabled = _associations.removeObjectForKey("disabled");

        _constraintsHelper = new DojoConstraintsHelper(_associations);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the value of the "type" attribute that should be written along with
     * the tag, if it is an &lt;input&gt; element.
     * @param context TODO
     *
     * @return the value of the element's "type" attribute
     */
    protected String inputTypeInContext(WOContext context)
    {
        return "";
    }


    // ----------------------------------------------------------
    /**
     * Can be overridden by subclasses to return a value indicating whether the
     * element can contain content or not.
     */
    @Override
    protected boolean hasContent()
    {
        return hasChildrenElements();
    }


    // ----------------------------------------------------------
    protected boolean defaultEscapeHTML()
    {
        return true;
    }


    // ----------------------------------------------------------
    protected NSMutableDictionary<String, String> nameMappingFromContext(
            WOContext context)
    {
        NSMutableDictionary<String, String> mapping =
            (NSMutableDictionary<String, String>) context.userInfoForKey(
                    NAME_MAPPING_USER_INFO_KEY);

        if (mapping == null)
        {
            mapping = new NSMutableDictionary<String, String>();
            context.setUserInfoForKey(mapping, NAME_MAPPING_USER_INFO_KEY);
        }

        return mapping;
    }


    // ----------------------------------------------------------
    protected void insertNameMappingIntoContext(WOContext context)
    {
        String name = nameInContext(context);
        String elementID = context.elementID();

        if (name != null && !name.equals(elementID))
        {
            nameMappingFromContext(context).setObjectForKey(elementID, name);
        }
    }


    // ----------------------------------------------------------
    protected void removeNameMappingFromContext(WOContext context)
    {
        String name = nameInContext(context);

        if (name != null)
        {
            nameMappingFromContext(context).removeObjectForKey(name);
        }
    }


    // ----------------------------------------------------------
    /**
     * If a subclass overrides this and does not call the super implementation,
     * it must call {@link #insertNameMappingIntoContext} before calling
     * <tt>context.wasFormSubmitted</tt> and then call
     * {@link #removeNameMappingFromContext} before returning if it wants to
     * participate successfully in Ajax partial submits.
     */
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        insertNameMappingIntoContext(context);

        if (!isDisabledInContext(context) && context.wasFormSubmitted())
        {
            String name = nameInContext(context);

            if (name != null && name.length() > 0 && _value != null)
            {
                String stringValue = request.stringFormValueForKey(name);
                Object value = objectForStringValue(stringValue, context);
                _value.setValue(value, context.component());
            }
        }

        super.takeValuesFromRequest(request, context);

        removeNameMappingFromContext(context);
    }


    // ----------------------------------------------------------
    /**
     * Gets the name of the element in the specified context.
     *
     * @param context
     *            the context
     * @return the name of the element
     */
    public String nameInContext(WOContext context)
    {
        if (_name != null)
        {
            Object value = _name.valueInComponent(context.component());

            if (value != null)
            {
                return value.toString();
            }
        }

        Object elementID = context.elementID();

        if (elementID != null)
        {
            return elementID.toString();
        }
        else
        {
            throw new IllegalStateException("<" + getClass().getName() + "> "
                    + "Cannot evaluate 'name' attribute, and context element "
                    + "ID is null.");
        }
    }


    // ----------------------------------------------------------
    /**
     * Given the specified string value of the "value" attribute of the form
     * field for this element from the request, transforms it into an object
     * that is set in the "value" binding of the hosting component. Can be
     * overridden by subclasses if special handling of the value is required.
     *
     * The default implementation merely returns the same string.
     *
     * @param stringValue
     *            the string representation of the value of the field
     * @param context
     *            the context of the request
     * @return an object to set into the "value" binding of the hosting
     *         component
     */
    protected Object objectForStringValue(String stringValue, WOContext context)
    {
        return stringValue;
    }


    // ----------------------------------------------------------
    /**
     * Gets the string that will be written as the value of the "value"
     * attribute for this element, based on the specified object that was
     * retrieved from the component binding.
     *
     * Subclasses can override this to alter the way that the value is converted
     * to a string. The default implementation merely calls toString on the
     * object.
     *
     * @param value
     *            the object obtained by querying the "value" binding on the
     *            hosting component
     * @param context
     *            the context of the request
     * @return the string representation of the element, to be written into the
     *         "value" attribute of the HTML
     */
    protected String stringValueForObject(Object value, WOContext context)
    {
        return value.toString();
    }


    // ----------------------------------------------------------
    protected boolean escapeHTMLInContext(WOContext context)
    {
        if (_escapeHTML == null)
            return defaultEscapeHTML();
        else
            return _escapeHTML.booleanValueInComponent(context.component());
    }


    // ----------------------------------------------------------
    protected boolean isDisabledInContext(WOContext context)
    {
        if (_disabled != null)
            return _disabled.booleanValueInComponent(context.component());
        else
            return false;
    }


    // ----------------------------------------------------------
    protected boolean isValueInInputValues(Object input, Object formValues)
    {
        boolean result = false;

        if(input != null && formValues != null)
        {
            if(formValues instanceof List)
            {
                result = ((List<?>) formValues).contains(input.toString());
            }
            else
            {
                result = input.toString().equals(formValues);
            }
        }

        return result;
    }


    // ----------------------------------------------------------
    protected void appendInputTypeAttributeToResponse(WOResponse response,
            WOContext context)
    {
        boolean hidden = false;

        if (_hidden != null)
            hidden = _hidden.booleanValueInComponent(context.component());

        String inputType = hidden ? "hidden" : inputTypeInContext(context);

        if (inputType != null && inputType.length() > 0)
        {
            _appendTagAttributeAndValueToResponse(response, "type", inputType,
                    false);
        }
    }


    // ----------------------------------------------------------
    protected void appendNameAttributeToResponse(WOResponse response,
            WOContext context)
    {
        String name = nameInContext(context);

        if (name != null && name.length() > 0)
        {
            _appendTagAttributeAndValueToResponse(response, "name", name,
                    escapeHTMLInContext(context));
        }
    }


    // ----------------------------------------------------------
    protected String valueStringInContext(WOContext context)
    {
        if (_value == null)
        {
            return "";
        }
        else
        {
            Object object = _value.valueInComponent(context.component());

            if (object != null)
            {
                String value = stringValueForObject(object, context);

                if (value == null)
                    return null;
                else
                    return value;
            }
            else
            {
                return "";
            }
        }
    }


    // ----------------------------------------------------------
    protected void appendValueAttributeToResponse(WOResponse response,
            WOContext context)
    {
        String value = valueStringInContext(context);

        if (value.length() > 0)
        {
            _appendTagAttributeAndValueToResponse(response, "value", value,
                    escapeHTMLInContext(context));
        }
    }


    // ----------------------------------------------------------
    protected void appendDisabledAttributeToResponse(WOResponse response,
            WOContext context)
    {
        if (isDisabledInContext(context))
        {
            _appendTagAttributeAndValueToResponse(response, "disabled",
                    "disabled", false);
        }
    }


    // ----------------------------------------------------------
    /**
     * Subclasses can override this method to provide a dictionary of
     * additional constraints that should be appended to any constraints
     * specified in the WOD file. This is useful mainly if an element binding
     * is intended to map to a constraint but is not named as such (for
     * example, the dateformat binding on WCDateTextBox).
     *
     * @param context
     * @return a dictionary containing additional constraints that should be
     *     merged with any user-supplied constraints
     */
    protected JSHash additionalConstraints(
            WOContext context)
    {
        return null;
    }


    // ----------------------------------------------------------
    protected void appendConstraintsAttributeToResponse(WOResponse response,
            WOContext context)
    {
        String constraints = _constraintsHelper.constraintsFromBindingValues(
                context, additionalConstraints(context));

        if (constraints != null)
        {
            _appendTagAttributeAndValueToResponse(response, "constraints",
                    constraints, true);
        }
    }


    // ----------------------------------------------------------
    @Override
    public void appendAttributesToResponse(WOResponse response,
            WOContext context)
    {
        super.appendAttributesToResponse(response, context);

        appendInputTypeAttributeToResponse(response, context);
        appendDisabledAttributeToResponse(response, context);
        appendNameAttributeToResponse(response, context);
        appendValueAttributeToResponse(response, context);

        appendConstraintsAttributeToResponse(response, context);
    }


    //~ Static/instance variables .............................................

    protected WOAssociation _disabled;
    protected WOAssociation _name;
    protected WOAssociation _value;
    protected WOAssociation _escapeHTML;
    protected WOAssociation _hidden;

    private DojoConstraintsHelper _constraintsHelper;

    /*package*/ static final String NAME_MAPPING_USER_INFO_KEY =
        "webcat.ui._base.DojoFormElement.nameMappingKey";
}
