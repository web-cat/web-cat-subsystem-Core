/*==========================================================================*\
 |  $Id: KVCAttributeInfo.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

import java.lang.reflect.AccessibleObject;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

//-------------------------------------------------------------------------
/**
 * Describes one KVC-accessible attribute of a class, including its name
 * and type.
 *
 * @author aallowat
 * @version $Id: KVCAttributeInfo.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public class KVCAttributeInfo
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Create an object.
     * @param name The name of this attribute
     * @param type The type of this attribute
     * @param accObject The reflection object representing the attribute, either
     *     a Field or Method, used to provide annotation access for extended
     *     properties
     */
	public KVCAttributeInfo(String name, String type,
	        AccessibleObject accObject)
	{
		this.name = name;
		this.type = type;
		
		properties = new NSMutableDictionary<String, Object>();

		Deprecated annDeprecated = accObject.getAnnotation(Deprecated.class);
		if (annDeprecated != null)
		{
		    properties.setObjectForKey(Boolean.TRUE, PROP_DEPRECATED);
		}
	}


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
	public String name()
	{
		return name;
	}


    // ----------------------------------------------------------
	public String type()
	{
		return type;
	}

	
    // ----------------------------------------------------------
	public Object valueForProperty(String property)
	{
	    return properties.valueForKey(property);
	}
	

    // ----------------------------------------------------------
    public NSDictionary<String, Object> allPropertyValues()
    {
        return properties;
    }

    
	//~ Instance/static variables .............................................

	public static final String PROP_DEPRECATED = "deprecated";

	private String name;
	private String type;
	private NSMutableDictionary<String, Object> properties;
}
