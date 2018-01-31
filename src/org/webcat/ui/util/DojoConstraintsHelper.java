/*==========================================================================*\
 |  $Id: DojoConstraintsHelper.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSDictionaryUtilities;

//------------------------------------------------------------------------
/**
 * A class that more easily allows a Dojo-based element to manage the
 * associations that represent constraints.
 *
 * This object should be created in the element's constructor. Any bindings
 * that represent constraints that are specified are removed from the element's
 * associations map, so that they are not output as attributes on the tag. Then,
 * in the elements appendAttributesToResponse method, these bindings can be
 * queried and their values returned in a string representing a JavaScript hash
 * that is appropriate for use as the "constraints" attribute.
 *
 * @author Tony Allevato
 * @version $Id: DojoConstraintsHelper.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class DojoConstraintsHelper
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the DojoConstraintsHelper class.
     *
     * @param associations the associations from the element
     */
    @SuppressWarnings("unchecked")
    public DojoConstraintsHelper(
            NSMutableDictionary<String, WOAssociation> associations)
    {
        _constraintAssociations =
            _NSDictionaryUtilities.extractObjectsForKeysWithPrefix(
                    associations, "constraints.", true);

        if (_constraintAssociations == null ||
                _constraintAssociations.count() <= 0)
        {
            _constraintAssociations = null;
        }

    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * @param context the context under which to access the binding values
     * @param additionalConstraints additional constraints that should be added
     *     to (overriding) the user-supplied bindings
     *
     * @return a string representing the constraints hash
     */
    public String constraintsFromBindingValues(WOContext context,
            JSHash additionalConstraints)
    {
        JSHash constraints = new JSHash();

        if (_constraintAssociations != null)
        {
            for (String constraint : _constraintAssociations.keySet())
            {
                WOAssociation assoc =
                    _constraintAssociations.objectForKey(constraint);

                if (assoc != null)
                {
                    Object value = assoc.valueInComponent(context.component());

                    // Literal constants specified in the WOD file (or inline
                    // bindings) will come through as strings, but if the string
                    // is actually a number, we should treat it as one.

                    if (value instanceof String)
                    {
                        String stringValue = (String) value;

                        try
                        {
                            Integer intValue = Integer.parseInt(stringValue);
                            value = intValue;
                        }
                        catch (NumberFormatException e)
                        {
                            try
                            {
                                Double doubleValue =
                                    Double.parseDouble(stringValue);
                                value = doubleValue;
                            }
                            catch (NumberFormatException e2)
                            {
                                // Do nothing; old value will pass through.
                            }
                        }
                    }

                    constraints.put(constraint, value);
                }
            }
        }

        if (additionalConstraints != null)
        {
            constraints.merge(additionalConstraints);
        }

        if (!constraints.isEmpty())
        {
            return constraints.toString();
        }
        else
        {
            return null;
        }
    }


    //~ Static/instance variables .............................................

    private NSDictionary<String, WOAssociation> _constraintAssociations;
}
