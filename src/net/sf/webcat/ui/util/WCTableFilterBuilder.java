package net.sf.webcat.ui.util;

import net.sf.webcat.ui.util.WCTableLayoutBuilder.Column;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class WCTableFilterBuilder
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the WCDataTableLayout class.
     */
    public WCTableFilterBuilder()
    {
        filters = new NSMutableArray<NSDictionary<String, Object>>();
    }


    // ----------------------------------------------------------
    /**
     * Gets the table columns as an array of dictionaries.
     * 
     * @return the array of dictionaries that describe the table columns
     */
    public NSArray<NSDictionary<String, Object>> asArray()
    {
        return filters;
    }
    
    
    // ----------------------------------------------------------
    /**
     * Adds a new column to the table layout.
     * 
     * @param name the name of the column that will be displayed in the table
     *     header
     * @param keyPath the key path, evaluated on each row's object, from which
     *     to obtain the value for the cells in this column
     * @param type
     */
    public void add(String name, String keyPath, String type)
    {
        NSMutableDictionary<String, Object> dict =
            new NSMutableDictionary<String, Object>();

        dict.setObjectForKey(name, "name");
        dict.setObjectForKey(keyPath, "keyPath");
        dict.setObjectForKey(type, "type");

        filters.addObject(dict);
    }


    private NSMutableArray<NSDictionary<String, Object>> filters;

}
