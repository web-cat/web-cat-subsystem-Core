/*==========================================================================*\
 |  $Id$
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

package net.sf.webcat.ui._base;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSDictionary;

// --------------------------------------------------------------
/**
 * A base class for Dojo widgets that are bound to numerical values. The
 * {@link #takeValuesFromRequest(WORequest, WOContext)} method automatically
 * takes the string value of the {@code value} binding and parses it either as
 * an integer or floating-point value, depending on the value returned by a
 * subclass in {@link #supportsIntegralValuesOnly()}.
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public abstract class DojoNumericFormElement extends DojoFormElement
{
    //~ Constructor ...........................................................
    
	// ----------------------------------------------------------
	/**
	 * Creates a new numeric form value element.
	 * 
	 * @param name
	 * @param someAssociations
	 * @param template
	 */
	public DojoNumericFormElement(String name,
			NSDictionary<String, WOAssociation> someAssociations,
			WOElement template)
	{
		super(name, someAssociations, template);
	}


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	/**
	 * Gets a value indicating whether the element should support integral
	 * values or floating point values.
	 * 
	 * @return true if the element should only support integral values; false
	 * if floating point values are also permitted
	 */
	protected boolean supportsIntegralValuesOnly()
	{
		return false;
	}


	// ----------------------------------------------------------
	@Override
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
	    // Don't set the binding if the form control was disabled on the page.
	    
        if(!isDisabledInContext(context) && context.wasFormSubmitted())
        {
            String name = nameInContext(context);

            if(name != null && name.length() > 0)
            {
                String valueString = request.stringFormValueForKey(name);
                Object value;

                // Parse the string into an appropriate numerical type that
                // will be set to the binding.

                if(supportsIntegralValuesOnly())
                {
                	value = Integer.valueOf(valueString);
                }
                else
                {
                	value = Double.valueOf(valueString);
                }

                _value.setValue(value, context.component());
            }
        }
        
        super.takeValuesFromRequest(request, context);
    }
}
