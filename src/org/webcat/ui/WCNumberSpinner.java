/*==========================================================================*\
 |  $Id: WCNumberSpinner.java,v 1.1 2010/05/11 14:51:57 aallowat Exp $
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

// ------------------------------------------------------------------------
/**
 * A numerical input widget with up/down buttons that let the user adjust the
 * value incrementally.
 * 
 * @author Tony Allevato
 * @version $Id: WCNumberSpinner.java,v 1.1 2010/05/11 14:51:57 aallowat Exp $
 */
public class WCNumberSpinner extends DojoNumericFormElement
{
    //~ Constructor ...........................................................
    
	// ----------------------------------------------------------
    /**
     * Creates a new number spinner.
     * 
     * @param name
     * @param someAssociations
     * @param template
     */
	public WCNumberSpinner(String name,
			NSDictionary<String, WOAssociation> someAssociations,
			WOElement template)
	{
		super("input", someAssociations, template);
	}


    //~ Methods ...............................................................

	// ----------------------------------------------------------
	@Override
	public String dojoType()
	{
		return "dijit.form.NumberSpinner";
	}
}
