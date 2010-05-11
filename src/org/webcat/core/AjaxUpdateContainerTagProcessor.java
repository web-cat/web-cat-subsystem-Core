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

package org.webcat.core;

import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODeclaration;
import com.webobjects.foundation.NSMutableDictionary;
import ognl.helperfunction.WOTagProcessor;

//-------------------------------------------------------------------------
/**
 * Adds support for <wo:adiv> and <wo:aspan> tag shortcuts.  Both tags
 * represent an AjaxUpdateContainer; adiv adds no special attributes, but
 * aspan is a shortcut for adding elementName="span" to the tag.
 *
 * @author Tony Allevato
 * @version $Id$
 */
public class AjaxUpdateContainerTagProcessor
    extends WOTagProcessor
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
	@SuppressWarnings("unchecked")
	public WODeclaration createDeclaration(
        String elementName,
        String elementType,
        NSMutableDictionary associations)
	{
		if (associations.objectForKey("elementName") != null)
		{
			throw new IllegalArgumentException(
                "The " + elementType + " tag implies the appropriate "
                + "elementName attribute; do not specify one.");
		}

		if (elementType.equals("aspan"))
		{
			associations.setObjectForKey(
			    new WOConstantValueAssociation("span"), "elementName");
		}

		return super.createDeclaration(elementName, "AjaxUpdateContainer",
		    associations);
	}
}
