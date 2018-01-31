/*==========================================================================*\
 |  $Id: AjaxUpdateContainerTagProcessor.java,v 1.1 2011/10/25 12:51:37 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2011 Virginia Tech
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

package org.webcat.woextensions;

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
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.1 $, $Date: 2011/10/25 12:51:37 $
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
