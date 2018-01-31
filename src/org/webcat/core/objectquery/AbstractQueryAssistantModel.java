/*==========================================================================*\
 |  $Id: AbstractQueryAssistantModel.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
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

package org.webcat.core.objectquery;

import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSKeyValueCoding;

//-------------------------------------------------------------------------
/**
 * This interface defines the two methods that any query assistant model must
 * implement, in order to translate the model's internal state to and from
 * EOModel qualifiers that are stored in the query database.
 *
 * @author aallowat
 * @version $Id: AbstractQueryAssistantModel.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
 */
public abstract class AbstractQueryAssistantModel
    implements NSKeyValueCoding
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * This method converts the specified qualifier into whatever internal
     * state model the query assistant model requires. If the conversion is not
     * possible (i.e., the qualifier came from a different query assistant and
     * this one does not know how to handle it), then the model should either
     * try to interpret as much of the qualifier as it can, or simply
     * initialize itself to defaults.
     *
     * The model should NOT try to preserve qualifiers that it does not
     * understand and reconstitute them in the qualifierFromValues method. This
     * would cause user confusion as the user interface for a particular
     * query assistant would have no way of displaying or modifying these
     * "hidden" qualifiers.
     *
     * @param qualifier the qualifier to convert to the model
     */
    public abstract void takeValuesFromQualifier(EOQualifier qualifier);


    // ----------------------------------------------------------
    /**
     * This method converts the internal state of the query assistant model
     * into a qualifier that can be stored in the database.
     *
     * @return a qualifier that represents the internal state of this query
     *     assistant model
     */
    public abstract EOQualifier qualifierFromValues();


    // ----------------------------------------------------------
    /**
     * This method converts the internal state of the query assistant model
     * into a human-readable string that describes the nature of the objects
     * that will be selected by this query.
     *
     * @return a human-readable description of the internal state of the model
     */
    public abstract String humanReadableDescription();


    // ----------------------------------------------------------
    public void takeValueForKey(Object value, String key)
    {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(
            this, value, key);
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }
}
