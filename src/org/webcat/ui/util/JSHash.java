/*==========================================================================*\
 |  $Id: JSHash.java,v 1.4 2010/11/01 17:04:05 aallowat Exp $
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

package org.webcat.ui.util;

import java.util.HashSet;
import com.webobjects.foundation.NSMutableDictionary;

//--------------------------------------------------------------------------
/**
 * A class that encapsulates a Javascript hash concept, with the added ability
 * that values can be either scalar values (which, if strings, are quoted as
 * necessary), direct JS code (which will not be quoted so it can be evaluated
 * on the client), or nested hashes.
 *
 * We can't just shove the keys and values into a JSONObject and call toString
 * on that since it will quote all the keys and values.
 *
 * @author Tony Allevato
 * @version $Id: JSHash.java,v 1.4 2010/11/01 17:04:05 aallowat Exp $
 */
public class JSHash
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes an empty option set.
     */
    public JSHash()
    {
        options = new NSMutableDictionary<String, Object>();
    }


    // ----------------------------------------------------------
    /**
     * Initializes a new option set with the specified keys and values.
     *
     * @param keysAndValues a varargs list of alternating keys and values; the
     *     keys should be strings
     */
    public JSHash(Object... keysAndValues)
    {
        this();

        if (keysAndValues.length % 2 != 0)
        {
            throw new IllegalArgumentException("There should a value " +
                "corresponding to every key that was passed.");
        }

        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];

            if (!(key instanceof String))
            {
                throw new IllegalArgumentException("Keys should be strings.");
            }

            options.setObjectForKey(value, key);
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets an object that represents the specified Javascript code as an
     * "expression". If you wish to put literal JS code into a hash, you must
     * insert it with <tt>put(JSHash.code("code"))</tt> so that it does
     * not get quoted in the string representation of the hash.
     *
     * @param code the Javascript code to convert
     * @return an object that can be inserted into a JSHash
     */
    public static Object code(String code)
    {
        return new Code(code);
    }


    // ----------------------------------------------------------
    /**
     * Creates a copy of this JSHash object. The result is a JSHash that is
     * distinct from the original in that changes to the keys and values in one
     * do not affect the other, but any values that are references to objects
     * will refer to the same objects.
     *
     * @return a copy of this DojoOptions object
     */
    public JSHash clone()
    {
        JSHash copy = new JSHash();
        copy.merge(this);
        return copy;
    }


    // ----------------------------------------------------------
    /**
     * Returns a JavaScript hash string containing the key-value pairs in the
     * options object.
     *
     * @return a String representation of the JavaScript hash containing the
     *         values in the dictionary
     */
    @Override
    public String toString()
    {
        StringBuffer buffer = new StringBuffer(256);
        buffer.append('{');

        String[] keys = options.keySet().toArray(new String[options.size()]);

        for (int i = 0; i < keys.length; i++)
        {
            String key = keys[i];

            buffer.append(key);
            buffer.append(':');

            Object value = options.objectForKey(key);
            buffer.append(stringRepresentationOfValue(value));

            if (i != keys.length - 1)
            {
                buffer.append(", ");
            }
        }

        buffer.append('}');
        return buffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether or not the options set is empty.
     *
     * @return true if the options set is empty; otherwise false
     */
    public boolean isEmpty()
    {
        return options.isEmpty();
    }


    // ----------------------------------------------------------
    /**
     * Inserts or sets a value in the hash.
     *
     * @param key the option key
     * @param value the option value
     */
    public void put(String key, Object value)
    {
        options.setObjectForKey(value, stringAsValidKey(key));
    }


    // ----------------------------------------------------------
    /**
     * Merges the specified options set into this options set.
     *
     * @param opts the options set that will be merged into this one
     */
    public void merge(JSHash opts)
    {
        if (opts != null)
        {
            for (String key : opts.options.keySet())
            {
                options.setObjectForKey(opts.options.objectForKey(key), key);
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Removes the value with the specified key, if it exists.
     *
     * @param key the key to remove
     */
    public void remove(String key)
    {
        options.removeObjectForKey(key);
    }


    // ----------------------------------------------------------
    /**
     * Gets the value with the specified key, if it exists.
     *
     * @param key the key to remove
     * @return the value retrieved, or null if the value does not exist or is
     *     not of the specified type
     */
    public Object get(String key)
    {
        return get(key, Object.class);
    }


    // ----------------------------------------------------------
    /**
     * Gets the value with the specified key, if it exists.
     *
     * @param <T> the type of the value to retrieve
     * @param key the key to remove
     * @param klass the type of the value to retrieve
     * @return the value retrieved, or null if the value does not exist or is
     *     not of the specified type
     */
    public <T> T get(String key, Class<? extends T> klass)
    {
        Object value = options.objectForKey(key);

        if (klass.isInstance(value))
        {
            return klass.cast(value);
        }

        return null;
    }


    // ----------------------------------------------------------
    /**
     * Returns a string based on the specified string that can be legally used
     * as a key in a JavaScript hash. That is, if the string is a valid
     * JavaScript identifier, it will be returned as is; otherwise, it will be
     * returned in single quotes.
     *
     * @param str the string to be made into a key
     *
     * @return the string converted into a form that can be used as a key
     */
    private static String stringAsValidKey(String str)
    {
        boolean isIdentifier = true;

        if (str.length() > 0)
        {
            if (javascriptKeywords.contains(str)
                    || !Character.isJavaIdentifierStart(str.charAt(0)))
            {
                isIdentifier = false;
            }
            else
            {
                for (int i = 1; i < str.length(); i++)
                {
                    if (!Character.isJavaIdentifierPart(str.charAt(i)))
                    {
                        isIdentifier = false;
                        break;
                    }
                }
            }
        }
        else
        {
            isIdentifier = false;
        }

        if (isIdentifier)
        {
            return str;
        }
        else
        {
            return singleQuote(str);
        }
    }


    // ----------------------------------------------------------
    /**
     * Single-quotes the specified string, escaping any existing single quotes
     * and backslashes that may be present inside it.
     *
     * @param str the string to be single-quoted
     *
     * @return the single-quoted string
     */
    private static String singleQuote(String str)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append('\'');

        for (int i = 0; i < str.length(); i++)
        {
            char ch = str.charAt(i);

            if (ch == '\'')
            {
                buffer.append("\\'");
            }
            else if (ch == '\\')
            {
                buffer.append("\\\\");
            }
            else
            {
                buffer.append(ch);
            }
        }

        buffer.append('\'');
        return buffer.toString();
    }


    // ----------------------------------------------------------
    private String stringRepresentationOfValue(Object value)
    {
        if (value == null)
        {
            return "null";
        }
        else if (value instanceof JSHash || value instanceof Code ||
                value instanceof Number || value instanceof Boolean)
        {
            return value.toString();
        }
        else
        {
            return singleQuote(value.toString());
        }
    }


    //~ Private classes .......................................................

    // ----------------------------------------------------------
    private static class Code
    {
        // ----------------------------------------------------------
        public Code(String code)
        {
            this.code = code;
        }


        // ----------------------------------------------------------
        @Override
        public String toString()
        {
            if (code == null)
            {
                return "null";
            }
            else
            {
                return code;
            }
        }


        //~ Static/instance variables .........................................

        private String code;
    }


    //~ Static/instance variables .............................................

    private NSMutableDictionary<String, Object> options;

    private static final String[] javascriptKeywordsArray = {
        "break", "else", "new", "var", "case", "finally", "return", "void",
        "catch", "for", "switch", "while", "continue", "function", "this",
        "with", "default", "if", "throw", "delete", "in", "try", "do",
        "instanceof", "typeof", "abstract", "enum", "int", "short", "boolean",
        "export", "interface", "static", "byte", "extends", "long", "super",
        "char", "final", "native", "synchronized", "class", "float", "package",
        "throws", "const", "goto", "private", "transient", "debugger",
        "implements", "protected", "volatile", "double", "import", "public",
        "null", "true", "false"
    };

    private static final HashSet<String> javascriptKeywords;

    static
    {
        javascriptKeywords = new HashSet<String>();
        for (String keyword : javascriptKeywordsArray)
        {
            javascriptKeywords.add(keyword);
        }
    }
}
