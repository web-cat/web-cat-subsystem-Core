/*==========================================================================*\
 |  $Id$
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

package net.sf.webcat.ui.table;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

//------------------------------------------------------------------------
/**
 * A helper class to ease the description of the column set of an ObjectTable.
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class ObjectTableColumns
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instalce of the ObjectTableColumns class.
     */
    public ObjectTableColumns()
    {
        columns = new NSMutableArray<NSDictionary<String, Object>>();
    }


    // ----------------------------------------------------------
    /**
     * Gets the table columns as an array of dictionaries.
     * 
     * @return the array of dictionaries that describe the table columns
     */
    public NSArray<NSDictionary<String, Object>> asArray()
    {
        return columns;
    }
    
    
    // ----------------------------------------------------------
    /**
     * Adds a new column to the table layout.
     * 
     * @param name the name of the column that will be displayed in the table
     *     header
     * @param componentName the name of the component that will be used to
     *     render the cells in this column
     * @param keyPath the key path, evaluated on each row's object, from which
     *     to obtain the value for the cells in this column
     * @param sortingKeyPath the key path upon which to sort this column, if
     *     different than keyPath.  If null, keyPath will be used
     * @param properties a dictionary of initial cell-specific properties for
     *     the cells in this column (can be null)
     * 
     * @return an ObjectTableColumn object that represents the newly added
     *     column, which can be used to conveniently chain additional
     *     addProperty methods
     */
    public ObjectTableColumn add(String name, String componentName,
            String keyPath, String sortingKeyPath,
            NSDictionary<String, Object> properties)
    {
        NSMutableDictionary<String, Object> dict =
            new NSMutableDictionary<String, Object>();
        
        dict.setObjectForKey(name, "name");
        dict.setObjectForKey(componentName, "componentName");

        if (keyPath != null)
            dict.setObjectForKey(keyPath, "keyPath");
        
        if (sortingKeyPath != null)
            dict.setObjectForKey(sortingKeyPath, "sortingKeyPath");

        if (properties != null)
            dict.setObjectForKey(
                    new NSMutableDictionary<String, Object>(properties),
                    "properties");
        else
            dict.setObjectForKey(new NSMutableDictionary<String, Object>(),
                    "properties");

        columns.addObject(dict);
        
        return new ObjectTableColumn(dict);
    }
    
    
    // ----------------------------------------------------------
    /**
     * Represents a column in the table layout. These are returned by the
     * {@link ObjectTable#add} method to provide convenient helper methods for
     * constructing the layout.
     */
    public class ObjectTableColumn
    {
        //~ Constructors ......................................................
        
        // ----------------------------------------------------------
        private ObjectTableColumn(NSMutableDictionary<String, Object> dict)
        {
            dictionary = dict;
        }
        

        // ----------------------------------------------------------
        /**
         * Adds a cell-specific property to this column.
         * 
         * @param key the name of the property to add
         * @param value the value of the property
         * 
         * @return this column, so that multiple calls can be chained
         */
        @SuppressWarnings("unchecked")
        public ObjectTableColumn addProperty(String key, Object value)
        {
            NSMutableDictionary<String, Object> props =
                (NSMutableDictionary<String, Object>)
                dictionary.objectForKey("properties");
            
            props.setObjectForKey(value, key);

            return this;
        }
        
        
        //~ Static/instance variables .........................................
        
        private NSMutableDictionary<String, Object> dictionary;
    }


    //~ Static/instance variables .........................................

    private NSMutableArray<NSDictionary<String, Object>> columns;
}
