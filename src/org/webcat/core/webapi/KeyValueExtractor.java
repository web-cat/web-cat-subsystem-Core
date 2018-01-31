package org.webcat.core.webapi;

import org.webcat.core.EOBase;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

//-------------------------------------------------------------------------
/**
 * A convenience class to extract properties from an enterprise object into a
 * dictionary (or an array of objects into an array of dictionaries). Useful
 * for web API actions that need to return properties about an object in their
 * response.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2012/06/22 16:23:17 $
 */
public class KeyValueExtractor
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new key-value extractor.
     */
    public KeyValueExtractor()
    {
        mapping = new NSMutableDictionary<String, String>();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Adds the specified key path to the extractor, using the key path itself
     * as the key in the resulting dictionary.
     *
     * @param keypath the key path to extract
     * @return the receiver, for method chaining
     */
    public KeyValueExtractor with(String keypath)
    {
        return with(keypath, keypath);
    }


    // ----------------------------------------------------------
    /**
     * Adds the specified key path to the extractor, using a different name as
     * the key in the resulting dictionary.
     *
     * @param keypath the key path to extract
     * @param newName the name of the key in the resulting dictionary
     * @return the receiver, for method chaining
     */
    public KeyValueExtractor with(String keypath, String newName)
    {
        mapping.setObjectForKey(newName, keypath);
        return this;
    }


    // ----------------------------------------------------------
    /**
     * Extracts the properties of the specified object into a dictionary.
     *
     * @param object the object whose properties should be extracted
     * @return the dictionary containing the properties
     */
    public NSDictionary<String, Object> extract(EOBase object)
    {
        NSMutableDictionary<String, Object> result =
            new NSMutableDictionary<String, Object>();

        for (String keypath : mapping.allKeys())
        {
            String newName = mapping.objectForKey(keypath);

            result.setObjectForKey(
                    object.valueForKeyPath(keypath), newName);
        }

        return result;
    }


    // ----------------------------------------------------------
    /**
     * Extracts the properties of the specified array of objects into an array
     * of dictionaries.
     *
     * @param objects the array of objects whose properties should be extracted
     * @return the array of dictionaries containing the properties
     */
    public NSArray<NSDictionary<String, Object>> extract(
            NSArray<? extends EOBase> objects)
    {
        NSMutableArray<NSDictionary<String, Object>> results =
            new NSMutableArray<NSDictionary<String, Object>>();

        for (EOBase object : objects)
        {
            results.addObject(extract(object));
        }

        return results;
    }


    //~ Static/instance variables .............................................

    /**
     * The mapping from keypaths (keys) to their new name in the extracted
     * dictionary (values).
     */
    private NSMutableDictionary<String, String> mapping;
}
