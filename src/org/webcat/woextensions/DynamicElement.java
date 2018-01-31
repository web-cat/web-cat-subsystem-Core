/*==========================================================================*\
 |  $Id: DynamicElement.java,v 1.1 2011/10/25 12:51:37 stedwar2 Exp $
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

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

// -------------------------------------------------------------------------
/**
 * This is a custom subclass of WODynamicElement that adds a few utility
 * methods to make writing custom dynamic elements a bit easier.  It is
 * inspired by some comments on the WODev wiki at:
 *
 * http://wodev.spearway.com/cgi-bin/WebObjects/WODev.woa/wa/Main?wikiPage=CreatingADynamicElement
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.1 $, $Date: 2011/10/25 12:51:37 $
 */
public class DynamicElement
    extends WODynamicElement
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Construct a new object.
     * @param name         the component instance's name
     * @param associations this instance's parameter bindings
     * @param children     this instance's child elements
     */
    public DynamicElement(
        String name,
        NSDictionary<String, WOAssociation> associations,
        WOElement children)
    {
        super(name, associations, children);
        this.associations = associations;
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Get this element's associations.
     * @return this element's associations
     */
    public NSDictionary<String, WOAssociation> associations()
    {
        return associations;
    }


    // ----------------------------------------------------------
    /**
     * Get the association for a given key.
     * @param key the key to look up
     * @return this key's association, or null if there is none
     */
    public boolean hasAssociationForKey(Object key)
    {
        return associationForKey(key) != null;
    }


    //~ Protected Methods .....................................................

    // ----------------------------------------------------------
    /**
     * Get the association for a given key.
     * @param key the key to look up
     * @return this key's association, or null if there is none
     */
    protected WOAssociation associationForKey(Object key)
    {
        return (key != null)
            ? (WOAssociation)associations().objectForKey(key)
            : null;
    }


    // ----------------------------------------------------------
    /**
     * Extract a value from an association.
     * @param anAssociation the association to inspect
     * @param aComponent the current component
     * @return this association's value
     */
    protected Object valueInAssociation(WOAssociation anAssociation,
                                        WOComponent   aComponent)
    {
        return (anAssociation != null)
            ? anAssociation.valueInComponent(aComponent)
            : null;
    }


    // ----------------------------------------------------------
    /**
     * Get the value for a given key.
     * @param key the key to look up
     * @param aComponent the current component
     * @return this key's value, or null if there is none
     */
    protected Object associationValueForKey(Object key,
                                            WOComponent aComponent)
    {
        return (key != null)
            ? valueInAssociation(associationForKey(key), aComponent)
            : null;
    }


    // ----------------------------------------------------------
    /**
     * Get the string value for a given key.
     * @param key the key to look up
     * @param aComponent the current component
     * @return this key's value, or null if there is none
     */
    protected String associationStringValueForKey(Object key,
                                                  WOComponent aComponent)
    {
        return associationStringValueForKey(key, aComponent, null);
    }



    // ----------------------------------------------------------
    /**
     * Get the string value for a given key.
     * @param key the key to look up
     * @param aComponent the current component
     * @param defaultVal the default value if the association is missing
     * @return this key's value, or null if there is none
     */
    protected String associationStringValueForKey(Object      key,
                                                  WOComponent aComponent,
                                                  String      defaultVal)
    {
        Object aValue = associationValueForKey(key, aComponent);
        return (aValue != null)
            ? aValue.toString()
            : defaultVal;
    }


    // ----------------------------------------------------------
    /**
     * Get the boolean value for a given key.
     * @param key the key to look up
     * @param aComponent the current component
     * @return this key's value, or null if there is none
     */
    protected boolean associationBooleanValueForKey(Object key,
                                                    WOComponent aComponent)
    {
        return associationBooleanValueForKey(key, aComponent, false);
    }


    // ----------------------------------------------------------
    /**
     * Get the boolean value for a given key.
     * @param key the key to look up
     * @param aComponent the current component
     * @param defaultVal the default value if the association is missing
     * @return this key's value, or null if there is none
     */
    protected boolean associationBooleanValueForKey(Object key,
                                                    WOComponent aComponent,
                                                    boolean     defaultVal)
    {
        boolean result = defaultVal;
        Object  aValue = this.associationValueForKey(key, aComponent);
        if (aValue != null)
        {
            if (aValue instanceof Number)
            {
                result = ((Number)aValue).intValue() != 0;
            }
            else if (aValue instanceof Boolean)
            {
                result = ((Boolean)aValue).booleanValue();
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Get the int value for a given key.
     * @param key the key to look up
     * @param aComponent the current component
     * @return this key's value, or null if there is none
     */
    protected int associationIntegerValueForKey(Object key,
                                                WOComponent aComponent)
    {
        return associationIntegerValueForKey(key, aComponent, 0);
    }


    // ----------------------------------------------------------
    /**
     * Get the int value for a given key.
     * @param key the key to look up
     * @param aComponent the current component
     * @param defaultVal the default value if the association is missing
     * @return this key's value, or null if there is none
     */
    protected int associationIntegerValueForKey(Object key,
                                                WOComponent aComponent,
                                                int         defaultVal)
    {
        int result = defaultVal;
        Object  aValue = this.associationValueForKey(key, aComponent);
        if (aValue instanceof Number)
        {
            result = ((Number)aValue).intValue();
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Get the float value for a given key.
     * @param key the key to look up
     * @param aComponent the current component
     * @return this key's value, or null if there is none
     */
    protected float associationFloatValueForKey(Object key,
                                                WOComponent aComponent)
    {
        return associationFloatValueForKey(key, aComponent, 0.0f);
    }


    // ----------------------------------------------------------
    /**
     * Get the float value for a given key.
     * @param key the key to look up
     * @param aComponent the current component
     * @param defaultVal the default value if the association is missing
     * @return this key's value, or null if there is none
     */
    protected float associationFloatValueForKey(Object key,
                                                WOComponent aComponent,
                                                float       defaultVal)
    {
        float result = defaultVal;
        Object  aValue = this.associationValueForKey(key, aComponent);
        if (aValue instanceof Number)
        {
            result = ((Number)aValue).floatValue();
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Get the double value for a given key.
     * @param key the key to look up
     * @param aComponent the current component
     * @return this key's value, or null if there is none
     */
    protected double associationDoubleValueForKey(Object key,
                                                  WOComponent aComponent)
    {
        return associationDoubleValueForKey(key, aComponent, 0.0);
    }


    // ----------------------------------------------------------
    /**
     * Get the double value for a given key.
     * @param key the key to look up
     * @param aComponent the current component
     * @param defaultVal the default value if the association is missing
     * @return this key's value, or null if there is none
     */
    protected double associationDoubleValueForKey(Object key,
                                                  WOComponent aComponent,
                                                  double      defaultVal)
    {
        double result = defaultVal;
        Object  aValue = this.associationValueForKey(key, aComponent);
        if (aValue instanceof Number)
        {
            result = ((Number)aValue).doubleValue();
        }
        return result;
    }


    //~ Instance/static variables .............................................

    private NSDictionary<String, WOAssociation> associations;
}
