/*==========================================================================*\
 |  $Id: DojoElement.java,v 1.1 2010/05/11 14:52:00 aallowat Exp $
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

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.foundation.NSDictionary;

// ------------------------------------------------------------------------
/**
 * A general base class for Dojo elements (that is, anything that needs to be
 * tagged with a dojoType attribute in HTML).
 *
 * @author Tony Allevato
 * @version $Id: DojoElement.java,v 1.1 2010/05/11 14:52:00 aallowat Exp $
 */
public abstract class DojoElement extends WOHTMLDynamicElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public DojoElement(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);

        _dojoType = _associations.removeObjectForKey("dojoType");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the value of the dojoType attribute that should be attached to this
     * element.
     *
     * @return the value of the dojoType attribute that should be attached to
     *         this element.
     */
    public abstract String dojoType();


    // ----------------------------------------------------------
    /**
     * Subclasses should override this if they need to provide a context-
     * sensitive element name (that is, one tag type for a particular binding
     * value, another tag type for a different value).
     *
     * @param context
     *
     * @return the element's tag name
     */
    protected String elementNameInContext(WOContext context)
    {
        return elementName();
    }


    // ----------------------------------------------------------
    public Object valueForBinding(String name, Object defaultValue,
            WOComponent component)
    {
        Object result = component.valueForBinding(name);

        if (result == null)
        {
            return defaultValue;
        }
        else
        {
            return result;
        }
    }


    // ----------------------------------------------------------
    public Object valueForBinding(String name, WOComponent component)
    {
        return valueForBinding(name, null, component);
    }


    // ----------------------------------------------------------
    /**
     * Appends the dojoType attribute and value to the response.
     *
     * @param response
     * @param context
     */
    protected void _appendDojoTypeAttributeToResponse(WOResponse response,
            WOContext context)
    {
        String dojoType = null;

        if (_dojoType != null)
        {
            dojoType = _dojoType.valueInComponent(
                    context.component()).toString();
        }
        else
        {
            dojoType = dojoType();
        }

        if (dojoType != null && dojoType.length() > 0)
        {
            _appendTagAttributeAndValueToResponse(response, "dojoType",
                    dojoType, false);
        }
    }


    // ----------------------------------------------------------
    protected void addRequiredWebResources(WOResponse response,
            WOContext context)
    {
        // Default implementation does nothing.
    }


    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        super.appendToResponse(response, context);

        addRequiredWebResources(response, context);
    }


    // ----------------------------------------------------------
    @Override
    public void appendAttributesToResponse(WOResponse response,
            WOContext context)
    {
        _appendDojoTypeAttributeToResponse(response, context);

        super.appendAttributesToResponse(response, context);
    }


    // ----------------------------------------------------------
    @Override
    protected void _appendOpenTagToResponse(WOResponse response,
            WOContext context)
    {
        // There appear to be some Dojo issues if an element without content
        // uses <div /> instead of <div></div>, so we fix that here.

        String elementName = elementNameInContext(context);

        response.appendContentCharacter('<');
        response.appendContentString(elementName);
        appendAttributesToResponse(response, context);

        if (!hasContent() && !shouldElementHaveEndTag(elementName))
        {
            response.appendContentString(" /");
        }

        response.appendContentCharacter('>');
    }


    // ----------------------------------------------------------
    @Override
    protected void _appendCloseTagToResponse(WOResponse response,
            WOContext context)
    {
        // There appear to be some Dojo issues if an element without content
        // uses <div /> instead of <div></div>, so we fix that here.

        String elementName = elementNameInContext(context);

        if (hasContent() || shouldElementHaveEndTag(elementName))
        {
            response.appendContentString("</");
            response.appendContentString(elementName);
            response.appendContentCharacter('>');
        }
    }


    // ----------------------------------------------------------
    protected boolean shouldElementHaveEndTag(String elementName)
    {
        return "div".equalsIgnoreCase(elementName)
            || "span".equalsIgnoreCase(elementName);
    }


    // ----------------------------------------------------------
    protected void appendTagAttributeToResponse(WOResponse response,
            String name, Object object)
    {
        if (object != null)
        {
            response._appendTagAttributeAndValue(name, object.toString(), true);
        }
    }


    //~ Static/instance variables .............................................

    protected WOAssociation _dojoType;
}
