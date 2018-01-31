/*==========================================================================*\
 |  $Id: AdvancedQueryUtils.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
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

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;
import java.util.TimeZone;
import org.apache.log4j.Logger;
import org.webcat.core.KeyPathParser;

//-------------------------------------------------------------------------
/**
 * A collection of static utility methods.
 *
 * @author aallowat
 * @version $Id: AdvancedQueryUtils.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
 */
public class AdvancedQueryUtils
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * This class provides only static utility methods, so no instance
     * should ever be created.
     */
    private AdvancedQueryUtils()
    {
        // Nothing to do
    }


    //~ Public Constants ......................................................

    public static final int TYPE_STRING = 0;
    public static final int TYPE_INTEGER = 1;
    public static final int TYPE_DOUBLE = 2;
    public static final int TYPE_BOOLEAN = 3;
    public static final int TYPE_TIMESTAMP = 4;
    public static final int TYPE_ENTITY = 5;
    public static final int TYPE_ARRAY = 6;


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public static int typeOfKeyPath(String entityType, String keypath)
    {
        KeyPathParser parser = new KeyPathParser(entityType, keypath);
        Class<?> klass = parser.theClass();

        return typeOfClass(klass);
    }


    // ----------------------------------------------------------
    public static int typeOfClass(Class<?> klass)
    {
        if (String.class.isAssignableFrom(klass))
        {
            return TYPE_STRING;
        }
        else if (Integer.class.isAssignableFrom(klass)
                 || klass == Integer.TYPE)
        {
            return TYPE_INTEGER;
        }
        else if (Double.class.isAssignableFrom(klass)
                 || klass == Double.TYPE)
        {
            return TYPE_DOUBLE;
        }
        else if (Boolean.class.isAssignableFrom(klass)
                 || klass == Boolean.TYPE)
        {
            return TYPE_BOOLEAN;
        }
        else if (java.util.Date.class.isAssignableFrom(klass))
        {
            return TYPE_TIMESTAMP;
        }
        else if (EOEnterpriseObject.class.isAssignableFrom(klass))
        {
            return TYPE_ENTITY;
        }
        else if (NSArray.class.isAssignableFrom(klass))
        {
            return TYPE_ARRAY;
        }
        else
        {
            return TYPE_STRING;
        }
    }


    // ----------------------------------------------------------
    public static Object valueRangeForPreviewRepresentation(
        int type, String rep, EOEditingContext ec)
    {
        NSMutableDictionary<String, Object> dict =
            new NSMutableDictionary<String, Object>();

        String[] parts = rep.split(",");
        dict.setObjectForKey(
            singleValueForPreviewRepresentation(type, parts[0], ec),
            "minimumValue");
        dict.setObjectForKey(
            singleValueForPreviewRepresentation(type, parts[1], ec),
            "maximumValue");

        return dict;
    }


    // ----------------------------------------------------------
    public static Object singleValueForPreviewRepresentation(
        int type, String rep, EOEditingContext ec)
    {
        switch (type)
        {
            case TYPE_STRING:
                return rep;

            case TYPE_INTEGER:
                try
                {
                    return Integer.parseInt(rep.trim());
                }
                catch (NumberFormatException e)
                {
                    return null;
                }

            case TYPE_DOUBLE:
                try
                {
                    return Double.parseDouble(rep.trim());
                }
                catch (NumberFormatException e)
                {
                    return null;
                }

            case TYPE_BOOLEAN:
                return Boolean.parseBoolean(rep);

            case TYPE_TIMESTAMP:
                return timestampFromRepresentation(rep);

            case TYPE_ENTITY:
                try
                {
                    String[] parts = rep.split(":");
                    String entity = parts[0];
                    int id = Integer.parseInt(parts[1]);

                    return objectWithId(id, entity, ec);
                }
                catch (Exception e)
                {
                    log.warn("Exception trying to find object " + rep, e);
                    return null;
                }

            default:
                return null;
        }
    }


    // ----------------------------------------------------------
    public static Object multipleValuesForPreviewRepresentation(
        int type, String rep, EOEditingContext ec)
    {
        String[] values = rep.split(",");
        NSMutableArray array = new NSMutableArray();
        switch (type)
        {
            case TYPE_STRING:
            {
                for (String item : values)
                {
                    array.addObject(item.trim());
                }
                return array;
            }

            case TYPE_INTEGER:
            {
                for (String item : values)
                {
                    try
                    {
                        array.addObject(Integer.parseInt(item.trim()));
                    }
                    catch (NumberFormatException e)
                    {
                        // Ignore the erroneous value
                    }
                }
                return array;
            }

            case TYPE_DOUBLE:
            {
                for (String item : values)
                {
                    try
                    {
                        array.addObject(Double.parseDouble(item.trim()));
                    }
                    catch (NumberFormatException e)
                    {
                        // Ignore the erroneous value
                    }
                }
                return array;
            }

            case TYPE_ENTITY:
            {
                for (String itemRep : values)
                {
                    try
                    {
                        String[] parts = itemRep.split(":");
                        String entity = parts[0];
                        int id = Integer.parseInt(parts[1]);

                        EOEnterpriseObject object =
                            objectWithId(id, entity, ec);

                        if (object != null)
                        {
                            array.addObject(object);
                        }
                        else
                        {
                            log.warn("No object found for entity "
                                + entity + " with id " + id + "!");
                        }
                    }
                    catch (Exception e)
                    {
                        log.warn(
                            "Exception trying to find object " + itemRep, e);
                    }
                }
                return array;
            }

            default:
                return null;
        }
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private static EOEnterpriseObject objectWithId(
        int id, String entity, EOEditingContext ec)
    {
        if (id > 0)
        {
            NSArray<EOEnterpriseObject> results =
                EOUtilities.objectsMatchingKeyAndValue(
                    ec, entity, "id", new Integer(id));

            if (results != null && results.count() > 0)
            {
                return results.objectAtIndex(0);
            }
        }

        return null;
    }


    // ----------------------------------------------------------
    private static NSTimestamp timestampFromRepresentation(String rep)
    {
        try
        {
            String[] parts = rep.split(" ");
            return new NSTimestamp(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]) + 1,
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]),
                Integer.parseInt(parts[4]),
                0,
                TimeZone.getDefault());
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }


    //~ Instance/static variables .............................................

    private static final Logger log =
        Logger.getLogger(AdvancedQueryUtils.class);
}
