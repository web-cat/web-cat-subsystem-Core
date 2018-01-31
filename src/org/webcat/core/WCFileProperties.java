/*==========================================================================*\
 |  $Id: WCFileProperties.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

import java.util.Properties;
import org.webcat.core.WCProperties;

// -------------------------------------------------------------------------
/**
 *  A subclass of WCProperties that adds built-in inheritance searching
 *  for file properties.
 *
 *  @author  Stephen Edwards
 *  @version $Id: WCFileProperties.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public class WCFileProperties
    extends WCProperties
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new WCFileProperties object, initializing its contents from
     * a property file.
     *
     * @param filename The file to load from
     * @param defaults The defaults
     */
    public WCFileProperties( String filename, Properties defaults )
    {
        super( filename, defaults );
    }


    // ----------------------------------------------------------
    /**
     * Creates an empty property list with the specified defaults.
     *
     * @param defaults The defaults
     */
    public WCFileProperties( Properties defaults )
    {
        super( defaults );
    }


    // ----------------------------------------------------------
    /**
     * Creates an empty property list with no default values.
     */
    public WCFileProperties()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Get a file type property value, as a string.  The key that this
     * method searches for is based on a file extension.  For the
     * extension "gif" and the property "mimeType", this method will
     * look for a property called "filetype.gif.mimeType".  If not found,
     * It will instead look for a property called
     * "filetype.<extension>.inherit".  If found, this second key will
     * be interpreted as the name of a "parent" file type, and a recursive
     * search for the specified subproperty under the parent type will
     * be instituted.  If no property is found in the recursive search,
     * then the value will be taken from the "filetype.default.<subProperty>"
     * key.  If this final property does not exist, the default value will be
     * returned.
     *
     * @param extension     The file extension to look up (no dot at the
     *                      beginning)
     * @param subProperty   The file extension property to look up
     * @param defaultValue  The default to return if no value is found
     * @return              The property's value as a string
     */
    public String getFileProperty( String extension,
                                   String subProperty,
                                   String defaultValue /* = null */ )
    {
        String target = "filetype." + extension + "." + subProperty;
        String result = getProperty( target );
        if ( result == null )
        {
            String parent = getProperty( "filetype." + extension + ".inherit" );
            if ( parent != null )
            {
                result = getFileProperty( parent, subProperty, defaultValue );
            }
            else
            {
                result = getProperty( "filetype.default." + subProperty,
                                      defaultValue );
            }
        }
        return ( result == null ) ? defaultValue : result;
    }


    // ----------------------------------------------------------
    /**
     * Get a file type property value, as a boolean.  The search is exactly
     * as for getFileProperty().
     *
     * @param extension     The file extension to look up (no dot at the
     *                      beginning)
     * @param subProperty   The file extension property to look up
     * @param defaultValue  The default to return if no value is found
     * @return              The property's value as a boolean
     */
    public boolean getFileFlag( String extension,
                                String subProperty,
                                boolean defaultValue /* = false */ )
    {
        String target = "filetype." + extension + "." + subProperty;
        String resultAsString = getProperty( target );
        boolean result = defaultValue;
        if ( resultAsString == null )
        {
            String parent = getProperty( "filetype." + extension + ".inherit" );
            if ( parent != null )
            {
                result = getFileFlag( parent, subProperty, defaultValue );
            }
            else
            {
                String propName = "filetype.default." + subProperty;
                if ( getProperty( propName ) != null )
                {
                    result = booleanForKeyWithDefault( propName, defaultValue );
                }
            }
        }
        else
        {
            result = booleanForKeyWithDefault( target, defaultValue );
        }
        return result;
    }
}
