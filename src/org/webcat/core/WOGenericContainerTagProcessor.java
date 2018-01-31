/*==========================================================================*\
 |  $Id: WOGenericContainerTagProcessor.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2009 Virginia Tech
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
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import ognl.helperfunction.WOTagProcessor;

//-------------------------------------------------------------------------
/**
 * A simple tag processor for creating shortcut tags that are based on
 * WOGenericContainer--just use the intended element name as the shortcut
 * name.
 *
 * @author Stephen Edwards
 * @author Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2010/05/11 14:51:55 $
 */
public class WOGenericContainerTagProcessor
    extends WOTagProcessor
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.  The shortcut name will be used as the
     * element name for the generated WOGenericContainer.
     */
    public WOGenericContainerTagProcessor()
    {
        this(null, null);
    }


    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param elementName If non-null, use this name rather than the
     * shortcut name as the element name for the WOGenericContainer.
     * @param defaultBindings If non-null, this parameter provides
     * default bindings to set on the WOGenericContainer.  Any bindings
     * provided on the actual tag take precedence over these defaults,
     * of course.
     */
    public WOGenericContainerTagProcessor(
        String elementName,
        NSDictionary<String, String> defaultBindings)
    {
        defaultElementName = elementName;
        this.defaultBindings = defaultBindings;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public WODeclaration createDeclaration(
        String elementName,
        String elementType,
        NSMutableDictionary associations)
    {
        if (!associations.containsKey("elementName"))
        {
            associations.setObjectForKey(
                new WOConstantValueAssociation(
                    (defaultElementName != null)
                        ? defaultElementName
                        : elementType), "elementName");
        }
        if (defaultBindings != null)
        {
            for (String key : defaultBindings.keySet())
            {
                if (!associations.containsKey(key))
                {
                    associations.setObjectForKey(
                        new WOConstantValueAssociation(
                            defaultBindings.get(key)),
                        key);
                }
            }
        }
        return super.createDeclaration(
            elementName, "WOGenericContainer", associations);
    }


    //~ Instance/static variables .............................................
    private String defaultElementName;
    private NSDictionary<String, String> defaultBindings;
}
