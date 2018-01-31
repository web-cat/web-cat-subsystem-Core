/*==========================================================================*\
 |  $Id: ComponentIDGenerator.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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

package org.webcat.ui.util;

import com.webobjects.appserver.WOComponent;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

//--------------------------------------------------------------------------
/**
 * A simple class that lets a component easily generate CSS identifiers for its
 * sub-elements without being required to create a special method for each one.
 * Usage is as follows:
 * <p>
 * In your component class, declare an field that is an instance of this class:
 * <pre>
 *     ComponentIDGenerator idFor;
 * </pre>
 * <p>
 * In your component's <tt>appendToResponse</tt> method, initialize the field
 * by passing it the current component:
 * <pre>
 *     public void appendToResponse(WOResponse response, WOContext context)
 *     {
 *         idFor = new ComponentIDGenerator(this);
 *
 *         // other processing...
 *
 *         super.appendToResponse(response, context);
 *     }
 * </pre>
 * <p>
 * In your component's WOD file, use keys (or keypaths) that branch off this
 * "idFor" object:
 * <pre>
 *     SomeElement: WOSomeElement {
 *         id = idFor.aUniqueIdentifierString;
 *     }
 * </pre>
 * <p>
 * If the WebObjects element ID of SomeElement was "0.5.3", then the identifier
 * generated above would be "_0_5_3_aUniqueIdentifierString".
 *
 * @author Tony Allevato
 * @version $Id: ComponentIDGenerator.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class ComponentIDGenerator implements NSKeyValueCodingAdditions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public ComponentIDGenerator(WOComponent component)
    {
        idBase = "_" + component.context().elementID().replace('.', '_');
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String get()
    {
        return idBase;
    }


    // ----------------------------------------------------------
    public String get(String keypath)
    {
        if (COMPONENT_ID_PREFIX.equals(keypath))
        {
            return idBase;
        }
        else
        {
            String suffix = keypath.replace('.', '_');
            return idBase + "_" + suffix;
        }
    }


    // ----------------------------------------------------------
    public void takeValueForKey(Object object, String key)
    {
        // Do nothing.
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        return get(key);
    }


    // ----------------------------------------------------------
    public void takeValueForKeyPath(Object object, String keypath)
    {
        // Do nothing.
    }


    // ----------------------------------------------------------
    public Object valueForKeyPath(String keypath)
    {
        return get(keypath);
    }


    //~ Static/instance variables .............................................

    private static final String COMPONENT_ID_PREFIX = "ComponentIDPrefix";

    private String idBase;
}
