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

package net.sf.webcat.ui.util;

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
 * @version $Id$
 */
public class DojoOptions
{
    //~ Constructors ..........................................................
    
    // ----------------------------------------------------------
    /**
     * Initializes an empty option set.
     */
    public DojoOptions()
    {
        options = new NSMutableDictionary<String, Object>();
    }


    //~ Methods ...............................................................

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
            buffer.append(value.toString());

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
     * Puts a simple value into the options set. If the value is a number, the
     * literal number will be used. If the value is a boolean, then a literal
     * true or false value will be used. Otherwise, the value will be treated
     * as a string (by calling toString) and it will be put into the options
     * set as a single-quoted string literal.
     * 
     * @param key the option key
     * @param value the option value
     */
    public void putValue(String key, Object value)
    {
        options.setObjectForKey(new ValueOption(value),
                stringAsValidKey(key));
    }


    // ----------------------------------------------------------
    /**
     * Puts a JavaScript expression into the options set. That is, the string
     * passed in as the expression will be put into the options set without
     * modification or quoting so that it will be executed when the hash is
     * evaluated on the client. No syntax validation is performed on the
     * expression.
     * 
     * @param key the option key
     * @param expression the expression that will be evaluated to obtain the
     *     value of the option
     */
    public void putExpression(String key, String expression)
    {
        options.setObjectForKey(new ExpressionOption(expression),
                stringAsValidKey(key));
    }
    

    // ----------------------------------------------------------
    /**
     * Puts the specified options set as the value of a key in this option set.
     * This allows nested hashes to be constructed.
     * 
     * @param key the option key
     * @param value the options set that will be used as the value of the
     *     option
     */
    public void putOptions(String key, DojoOptions value)
    {
        options.setObjectForKey(new OptionsOption(value),
                stringAsValidKey(key));
    }
    
    
    // ----------------------------------------------------------
    /**
     * Merges the specified options set into this options set.
     * 
     * @param opts the options set that will be merged into this one
     */
    public void putAll(DojoOptions opts)
    {
        for (String key : opts.options.keySet())
        {
            options.setObjectForKey(opts.options.objectForKey(key), key);
        }
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
            if (!Character.isJavaIdentifierStart(str.charAt(0)))
            {
                isIdentifier = false;
            }
            else
            {
                for (int i = 1; i < str.length(); i++)
                {
                    if (!Character.isJavaIdentifierPart(str.charAt(1)))
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


    //~ Private classes .......................................................

    // ----------------------------------------------------------
    private static class ValueOption
    {
        // ----------------------------------------------------------
        public ValueOption(Object value)
        {
            this.value = value;
        }
        

        // ----------------------------------------------------------
        @Override
        public String toString()
        {
            if (value instanceof Number || value instanceof Boolean)
            {
                return value.toString();
            }
            else
            {
                return singleQuote(value.toString());
            }
        }


        //~ Static/instance variables .........................................

        private Object value;
    }


    // ----------------------------------------------------------
    private static class ExpressionOption
    {
        // ----------------------------------------------------------
        public ExpressionOption(String expression)
        {
            this.expression = expression;
        }
        

        // ----------------------------------------------------------
        @Override
        public String toString()
        {
            return expression;
        }

        
        //~ Static/instance variables .........................................

        private String expression;
    }


    // ----------------------------------------------------------
    private static class OptionsOption
    {
        // ----------------------------------------------------------
        public OptionsOption(DojoOptions options)
        {
            this.options = options;
        }
        

        // ----------------------------------------------------------
        @Override
        public String toString()
        {
            return options.toString();
        }

        
        //~ Static/instance variables .........................................

        private DojoOptions options;
    }


    //~ Static/instance variables .............................................

    private NSMutableDictionary<String, Object> options;
}
