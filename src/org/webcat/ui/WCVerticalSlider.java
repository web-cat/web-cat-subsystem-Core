/*==========================================================================*\
 |  $Id: WCVerticalSlider.java,v 1.1 2010/05/11 14:51:57 aallowat Exp $
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

import org.webcat.ui._base.DojoNumericFormElement;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

//------------------------------------------------------------------------
/**
 * A form widget that allows one to select a value with a vertically draggable
 * image.
 * <p>
 * Corresponds to Dojo type {@code dijit.form.VerticalSlider}.
 * 
 * <h2>Bindings</h2>
 * <ul>
 * <li>{@code clickSelect}: A Boolean value that indicates whether clicking the
 * slider bar changes its value directly to the clicked location. By default,
 * this is {@code true}.
 * <li>{@code maximum}: the maximum value of the slider.
 * <li>{@code minimum}: the minimum value of the slider.
 * <li>{@code pageIncrement}: the amount of change with Shift+arrow key.
 * <li>{@code showButtons}: A Boolean value indicating whether the increment and
 * decrement buttons should be shown at the ends of the slider. By default,
 * this is {@code true}.
 * <li>{@code value}: The current value of the slider.
 * </ul>
 * 
 * @author Tony Allevato
 * @version $Id: WCVerticalSlider.java,v 1.1 2010/05/11 14:51:57 aallowat Exp $
 */
public class WCVerticalSlider extends DojoNumericFormElement
{
    //~ Constructor ...........................................................
    
	// ----------------------------------------------------------
    /**
     * Creates a new vertical slider.
     * 
     * @param name
     * @param someAssociations
     * @param template
     */
	public WCVerticalSlider(String name,
			NSDictionary<String, WOAssociation> someAssociations,
			WOElement template)
	{
		super("div", someAssociations, template);
	}
	

	//~ Methods ...............................................................
	
	// ----------------------------------------------------------
	@Override
	public String dojoType()
	{
		return "dijit.form.VerticalSlider"; 
	}
}
