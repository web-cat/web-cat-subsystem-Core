/*==========================================================================*\
 |  $Id: KVCAttributeFinder.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
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

import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.eof.ERXGenericRecord;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.webcat.core.KVCAttributeInfo;

//-------------------------------------------------------------------------
/**
 * Provides a static utility method (and an internal cache) for looking
 * up the KVC-accessible attributes for a class.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class KVCAttributeFinder
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * This class provides only static utility methods, and so no instance
     * should ever be created.  Should it be a singleton instead?
     */
    private KVCAttributeFinder()
    {
        // Nothing to do
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public static NSArray<KVCAttributeInfo> attributesForClass(
        Class<?> klass, String prefix)
    {
        NSArray<KVCAttributeInfo> unfiltered;

        if (cache.containsKey(klass))
        {
            unfiltered = cache.objectForKey(klass);
        }
        else
        {
            NSMutableArray<KVCAttributeInfo> attrs =
                new NSMutableArray<KVCAttributeInfo>();

            if (NSKeyValueCoding.class.isAssignableFrom(klass))
            {
                addMethods(klass, klass.getMethods(), attrs);
                addFields(klass.getFields(), attrs);
            }

            try
            {
                attrs.sortUsingComparator(
                    NSComparator.AscendingCaseInsensitiveStringComparator);
            }
            catch (Exception e)
            {
                // ???
            }

            cache.setObjectForKey(attrs, klass);
            unfiltered = attrs;
        }

        NSMutableArray<KVCAttributeInfo> filtered =
            new NSMutableArray<KVCAttributeInfo>();

        for (KVCAttributeInfo attr : unfiltered)
        {
            String key = attr.name();

            if (key.toLowerCase().startsWith(prefix.toLowerCase()))
            {
                filtered.addObject(attr);
            }
        }

        return filtered;
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private static boolean isMethodFromSuperclass(Class<?> klass, Method method)
    {
        if ("toString".equals(method.getName()))
        {
            // Special dispensation for the toString() method, since it returns
            // a useful string representation of most objects that some report
            // designers might find useful.

            return false;
        }

        Class<?> methodClass = method.getDeclaringClass();

        boolean isInGenericRecord =
            methodClass.isAssignableFrom(ERXGenericRecord.class) ||
            methodClass.isAssignableFrom(EOGenericRecord.class);

        if (!isInGenericRecord)
        {
            // The method might have been overridden, causing its declaring
            // class to be the class itself. Check this by explicitly trying
            // to find the method in a superclass.

            try
            {
                ERXGenericRecord.class.getMethod(
                    method.getName(), method.getParameterTypes());

                isInGenericRecord = true;
            }
            catch (SecurityException e)
            {
                // ignore it
            }
            catch (NoSuchMethodException e)
            {
                // ignore it
            }

            try
            {
                EOGenericRecord.class.getMethod(
                    method.getName(), method.getParameterTypes());

                isInGenericRecord = true;
            }
            catch (SecurityException e)
            {
                // ignore it
            }
            catch (NoSuchMethodException e)
            {
                // ignore it
            }
        }

        return isInGenericRecord;
    }


    // ----------------------------------------------------------
    private static boolean methodHasSpecialName(Method method)
    {
        boolean isSpecial = false;

        String methodName = method.getName();

        if (methodName.startsWith("create")
            && methodName.endsWith("Relationship"))
        {
            isSpecial = true;
        }
        else if (methodName.equals("changedProperties"))
        {
            isSpecial = true;
        }
        else if (methodName.startsWith("updateMutableFields"))
        {
            isSpecial = true;
        }

        return isSpecial;
    }


    // ----------------------------------------------------------
    private static void addMethods(Class<?> klass, Method[]         methods,
                                   NSMutableArray<KVCAttributeInfo> attrs)
    {
        for (Method method : methods)
        {
            String name = method.getName();
            int modifiers = method.getModifiers();

            boolean isFromSuperclass = isMethodFromSuperclass(klass, method);
            boolean isSpecialName = methodHasSpecialName(method);

            if ((modifiers & Modifier.PUBLIC) != 0
                 && (modifiers & Modifier.STATIC) == 0
                 && method.getParameterTypes().length == 0
                 && !isFromSuperclass
                 && !isSpecialName
                 && isTypeAcceptable(method.getReturnType()))
            {
                if (name.startsWith("_get"))
                {
                    name = initialLower(name.substring("_get".length()));
                }
                else if (name.startsWith("get"))
                {
                    name = initialLower(name.substring("get".length()));
                }
                else if (name.startsWith("_is"))
                {
                    name = initialLower(name.substring("_is".length()));
                }
                else if (name.startsWith("is"))
                {
                    name = initialLower(name.substring("is".length()));
                }
                else if (name.startsWith("_"))
                {
                    name = name.substring("_".length());
                }

                if (!attrs.containsObject(name))
                {
                    KVCAttributeInfo attr = new KVCAttributeInfo(
                        name, method.getReturnType().getSimpleName(), method);
                    attrs.addObject(attr);
                }
            }
        }
    }


    // ----------------------------------------------------------
    private static void addFields(
        Field[] fields, NSMutableArray<KVCAttributeInfo> attrs)
    {
        for (Field field : fields)
        {
            String name = field.getName();
            int modifiers = field.getModifiers();

            if ((modifiers & Modifier.PUBLIC) != 0
                && (modifiers & Modifier.STATIC) == 0
                && isTypeAcceptable(field.getType()))
            {
                if (name.startsWith("_is"))
                {
                    name = initialLower(name.substring("_is".length()));
                }
                else if (name.startsWith("is"))
                {
                    name = initialLower(name.substring("is".length()));
                }
                else if (name.startsWith("_"))
                {
                    name = name.substring("_".length());
                }

                if (!attrs.containsObject(name))
                {
                    KVCAttributeInfo attr = new KVCAttributeInfo(
                        name, field.getType().getSimpleName(), field);
                    attrs.addObject(attr);
                }
            }
        }
    }


    // ----------------------------------------------------------
    private static boolean isTypeAcceptable(Class<?> klass)
    {
        return klass != Void.class && klass != Void.TYPE;
    }


    // ----------------------------------------------------------
    private static String initialLower(String str)
    {
        if (str != null && str.length() > 0)
        {
            return "" + Character.toLowerCase(str.charAt(0))
                + str.substring(1);
        }
        else
        {
            return str;
        }
    }


    //~ Instance/static variables .............................................

    private static final NSMutableDictionary<Class<?>,
        NSArray<KVCAttributeInfo>> cache =
            new NSMutableDictionary<Class<?>, NSArray<KVCAttributeInfo>>();
}
