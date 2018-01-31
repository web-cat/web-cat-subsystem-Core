/*==========================================================================*\
 |  $Id: KeyPathParser.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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

import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.webcat.core.WCProperties;

//-------------------------------------------------------------------------
/**
 * A KVC parser for use in queries.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class KeyPathParser
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create an object.
     * @param entity The entity from which the path originates
     * @param keyPath The KVC path
     */
    public KeyPathParser(String entity, String keyPath)
    {
        this(entity, keyPath, 0);
    }


    // ----------------------------------------------------------
    /**
     * Create an object.
     * @param entity The entity from which the path originates
     * @param keyPath The KVC path
     * @param skipEnd If non-zero, indicates how many of the final segments
     * of the key path to ignore
     */
    public KeyPathParser(String entity, String keyPath, int skipEnd)
    {
        this.keyPath = keyPath;

        Class<?> klass = null;

        EOClassDescription classDesc =
            EOClassDescription.classDescriptionForEntityName(entity);

        if (classDesc instanceof EOEntityClassDescription)
        {
            EOEntityClassDescription entDesc =
                (EOEntityClassDescription)classDesc;

            try
            {
                klass = Class.forName(entDesc.entity().className());
            }
            catch (Exception e)
            {
                // Leave klass null
            }
        }

        String[] components = splitKeyPath(keyPath);
        int i;

        for (i = 0; i < components.length - skipEnd; i++)
        {
            String component = components[i];

            NSArray<String> toManyKeys = classDesc.toManyRelationshipKeys();

            if (toManyKeys.containsObject(component))
            {
                if (i == components.length - 1)
                {
                    klass = NSArray.class;
                }
                else
                {
                    klass = null;
                }

                break;
            }
            EOClassDescription newClassDesc =
                classDesc.classDescriptionForDestinationKey(component);

            if (newClassDesc == null)
            {
                // If the key isn't a relationship, try to access it as an
                // attribute.
                klass = classDesc.classForAttributeKey(component);

                if (klass == null)
                {
                    // Last resort -- try to access it as a getter method.
                    klass = classForGetter(classDesc, component);
                }

                if (klass != null
                    && (NSDictionary.class.isAssignableFrom(klass)
                        || WCProperties.class.isAssignableFrom(klass)))
                {
                    if (i < components.length - 1)
                    {
                        klass = Object.class;
                    }
                    else
                    {
                        klass = null;
                    }
                }
/*				else if (!isPrimitive(klass))
                {
                    klass = null;
                }*/

                break;
            }
            else if (newClassDesc instanceof EOEntityClassDescription)
            {
                classDesc = newClassDesc;

                try
                {
                    EOEntityClassDescription entDesc =
                        (EOEntityClassDescription)classDesc;
                    klass = Class.forName(entDesc.entity().className());
                }
                catch (Exception e)
                {
                    klass = null;
                    break;
                }
            }
         }

        theClass = klass;
        remainingKeyPath = joinStrings(components, i);
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public Class<?> theClass()
    {
        return theClass;
    }


    // ----------------------------------------------------------
    public String remainingKeyPath()
    {
        return remainingKeyPath;
    }


    // ----------------------------------------------------------
    public String keyPath()
    {
        return keyPath;
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    /* private boolean isPrimitive(Class<?> klass)
    {
        return(klass == String.class ||
                klass == Float.class || klass == Float.class ||
                klass == Double.class || klass == Double.TYPE ||
                klass == Byte.class || klass == Byte.TYPE ||
                klass == Short.class || klass == Short.TYPE ||
                klass == Integer.class || klass == Integer.TYPE ||
                klass == Long.class || klass == Long.TYPE ||
                klass == Boolean.class || klass == Boolean.TYPE ||
                klass == Character.class || klass == Character.TYPE ||
                klass == NSTimestamp.class);
    } */


    // ----------------------------------------------------------
    private Class<?> classForGetter(EOClassDescription classDesc, String key)
    {
        Class<?> klass = null;

        if (classDesc instanceof EOEntityClassDescription)
        {
            try
            {
                EOEntityClassDescription entDesc =
                    (EOEntityClassDescription)classDesc;
                klass = Class.forName(entDesc.entity().className());

                if (klass == null)
                {
                    return null;
                }
            }
            catch (Exception e)
            {
                return null;
            }
        }
        else
        {
            return null;
        }

        Class<?> returnType = null;

        // method getFoo()
        returnType = getterHelper(klass, "get" + initialCap(key));
        if (returnType != null)
        {
            return returnType;
        }

        // method foo()
        returnType = getterHelper(klass, key);
        if (returnType != null)
        {
            return returnType;
        }

        // method isFoo()
        returnType = getterHelper(klass, "is" + initialCap(key));
        if (returnType != null)
        {
            return returnType;
        }

        // method _getFoo()
        returnType = getterHelper(klass, "_get" + initialCap(key));
        if (returnType != null)
        {
            return returnType;
        }

        // method _foo()
        returnType = getterHelper(klass, "_" + key);
        if (returnType != null)
        {
            return returnType;
        }

        // method _isFoo()
        returnType = getterHelper(klass, "_is" + initialCap(key));
        if (returnType != null)
        {
            return returnType;
        }

        // field _foo
        returnType = fieldHelper(klass, "_" + key);
        if (returnType != null)
        {
            return returnType;
        }

        // field _isFoo
        returnType = fieldHelper(klass, "_is" + initialCap(key));
        if (returnType != null)
        {
            return returnType;
        }

        // field foo
        returnType = fieldHelper(klass, key);
        if (returnType != null)
        {
            return returnType;
        }

        // field isFoo
        returnType = fieldHelper(klass, "is" + initialCap(key));
        if (returnType != null)
        {
            return returnType;
        }

        // no getter found
        return null;
    }


    // ----------------------------------------------------------
    private Class<?> getterHelper(Class<?> klass, String name)
    {
        try
        {
            Method method = klass.getMethod(name);
            return method.getReturnType();
        }
        catch (Exception e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    private Class<?> fieldHelper(Class<?> klass, String name)
    {
        try
        {
            Field field = klass.getField(name);
            return field.getType();
        }
        catch (Exception e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    private String initialCap(String key)
    {
        if (key != null && key.length() > 0)
        {
            return "" + Character.toUpperCase(key.charAt(0)) +
                key.substring(1);
        }
        else
        {
            return key;
        }
    }


    // ----------------------------------------------------------
    private String[] splitKeyPath(String keypath)
    {
        ArrayList<String> list = new ArrayList<String>();

        while (keypath.indexOf('.') != -1)
        {
            int index = keypath.indexOf('.');
            list.add(keypath.substring(0, index));
            keypath = keypath.substring(index + 1);
        }

        list.add(keypath);

        return list.toArray(new String[list.size()]);
    }


    // ----------------------------------------------------------
    private String joinStrings(String[] parts, int start)
    {
        StringBuffer buffer = new StringBuffer();

        if (start < parts.length)
        {
            buffer.append(parts[start]);

            for (int i = start + 1; i < parts.length; i++)
            {
                buffer.append('.');
                buffer.append(parts[i]);
            }
        }

        return buffer.toString();
    }


    //~ Instance/static variables .............................................

    private String keyPath;
    private String remainingKeyPath;
    private Class<?> theClass;
}