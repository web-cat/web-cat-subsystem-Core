/*==========================================================================*\
 |  $Id: EOBasedKeyGenerator.java,v 1.2 2011/03/23 15:06:14 aallowat Exp $
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

import java.io.Serializable;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import er.extensions.eof.ERXGenericRecord;

//--------------------------------------------------------------------------
/**
 * <p>
 * A class that is embedded in every Web-CAT enterprise object, which allows
 * for the creation of EO-dependent keypath strings for use in situations such
 * as persisting UI information about an EO in a particular context. For
 * example, on StudentsForAssignmentPage, you may wish to persist whether a
 * page module for an assignment offering is expanded or collapsed. Define a
 * unique key affix for this state, like "StudentsForAssignmentPage_open", and
 * use the following keypath from the AssignmentOffering:
 * </p><p>
 * <pre>
 *     theAsmtOffering.generateKey.user.preferences.StudentsForAssignmentPage_open
 *      -----------------------------   ---------------------------------------------</pre>
 * </p><p>
 * This will return the following keypath:
 * </p><p>
 * <pre>
 *     user.preferences.StudentsForAssignmentPage_open__AssignmentOffering_##</pre>
 * </p><p>
 * where ## is replaced by the unique database ID of the enterprise object.
 * This keypath can then be bound to the "open" attribute of the WCPageModule:
 * </p><p>
 * <pre>
 *     &lt;wo:WCPageModule open="$theAsmtOffering.preferencesKeys.user.preferences.StudentsForAssignmentPage_open"&gt;</pre>
 * </p>
 *
 * @author Tony Allevato
 * @version $Id: EOBasedKeyGenerator.java,v 1.2 2011/03/23 15:06:14 aallowat Exp $
 */
public class EOBasedKeyGenerator
    implements NSKeyValueCodingAdditions, Serializable
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the EOPrefsKeyGenerator with the
     * specified enterprise object.
     *
     * @param object the enterprise object to generate preference keys from
     */
    public EOBasedKeyGenerator(ERXGenericRecord object)
    {
        this.object = object;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String get()
    {
        return object.entityName() + "_" + object.valueForKey("id");
    }


    // ----------------------------------------------------------
    public String get(String keyPath)
    {
        return keyPath + "__" + get();
    }


    // ----------------------------------------------------------
    public Object valueForKeyPath(String keyPath)
    {
        return get(keyPath);
    }


    // ----------------------------------------------------------
    public void takeValueForKeyPath(Object value, String keyPath)
    {
        // Do nothing.
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        return get(key);
    }


    // ----------------------------------------------------------
    public void takeValueForKey(Object value, String key)
    {
        // Do nothing.
    }


    //~ Static/instance variables .............................................

    /* The enterprise object for which keypaths are being constructed. */
    private transient ERXGenericRecord object;
}
