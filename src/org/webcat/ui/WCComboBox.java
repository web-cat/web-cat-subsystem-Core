/*==========================================================================*\
 |  $Id: WCComboBox.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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

import org.webcat.ui._base.DojoSingleSelectionListFormElement;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSDictionary;

// ------------------------------------------------------------------------
/**
 * Implements the dijit.form.ComboBox class, which provides an auto-completing
 * text box with a drop-down list (from which the completion proposals are
 * obtained).
 * <p>
 * The standard bindings of list, item, and selection from WOPopUpButton are
 * used in the same way here. Since a combo box also allows the user to enter
 * arbitrary strings, {@code selection} must be bound to a
 * {@link java.lang.String}. The remaining list-type bindings &ndash;
 * {@code list}, {@code item}, and {@code displayString} &ndash; work as they
 * do for the standard WebObjects list dynamic elements and can be bound to
 * arbitrary objects.
 * <p>
 * @binding autoComplete A boolean value indicating that, if the user enters
 * 		a partial string and then tabs out of the field, the first entry
 * 		displayed in the drop down will be automatically copied into the field
 * @binding hasDownArrow A boolean value indicating whether the control
 * 		displays a down arrow or if it is a standard text field
 * @binding ignoreCase A boolean value indicating whether the field should
 * 		ignore case when matching possible items
 * @binding intermediateChanges A boolean value indicating whether the field
 * 		should fire onChange events for each value change or only on demand
 * @binding lowercase A boolean value indicating whether all characters in the
 * 		field should be converted to lowercase
 * @binding readOnly A boolean value indicating whether this widget should
 * 		respond to user input. Similar to disabled, but readOnly fields are
 * 		submitted while disabled ones are not
 * @binding uppercase A boolean value indicating whether all characters in the
 * 		field should be converted to uppercase
 * 
 * @author Tony Allevato
 */
public class WCComboBox extends DojoSingleSelectionListFormElement
{
	//~ Constructor ...........................................................
	
	// ----------------------------------------------------------
	/**
	 * Creates a new ComboBox element.
	 * 
	 * @param name
	 * @param someAssociations
	 * @param template
	 */
    public WCComboBox(String name,
			NSDictionary<String, WOAssociation> someAssociations,
			WOElement template)
    {
		super("select", someAssociations, template);
	}


	// ----------------------------------------------------------
    @Override
    public String dojoType()
    {
    	return "dijit.form.ComboBox";
    }


	// ----------------------------------------------------------
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
    	// Since a dijit.form.ComboBox lets the user enter an arbitrary string,
    	// we simply take the string value of the field and set the appropriate
    	// component binding to it, rather than searching for the item in the
    	// list first.

        if(_selection != null && !isDisabledInContext(context) &&
        		context.wasFormSubmitted())
        {
            Object selection = null;

            String valueString =
            	request.stringFormValueForKey(nameInContext(context));

            if(valueString != null)
            {
            	selection = valueString;
            }

            _selection.setValue(selection, context.component());
        }
    }
}
