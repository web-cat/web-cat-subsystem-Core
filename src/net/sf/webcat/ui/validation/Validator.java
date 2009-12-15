/*==========================================================================*\
 |  $Id$
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

package net.sf.webcat.ui.validation;

import com.webobjects.appserver.WOComponent;

//-------------------------------------------------------------------------
/**
 * The abstract class that all validators used by {@link ValidatingAction} must
 * extend. Typically there is one validator per field in the form.
 *
 * @author  Tony Allevato
 * @version $Id$
 */
public abstract class Validator
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the Validator class.
     *
     * @param fieldId the identifier of the field being validated
     */
    public Validator(String fieldId)
    {
        this.fieldId = fieldId;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Implementors must provide logic to perform the validation of a value
     * submitted in an action.
     *
     * @param component the component on which the value is bound
     * @return an error message if validation failed; null if validation
     *     succeeded
     */
    public abstract String validate(WOComponent component);


    // ----------------------------------------------------------
    /**
     * Gets the DOM identifier of the field being validated.
     *
     * @return the identifier of the field being validated
     */
    public String fieldId()
    {
        return fieldId;
    }


    //~ Static/instance variables .............................................

    private String fieldId;
}
