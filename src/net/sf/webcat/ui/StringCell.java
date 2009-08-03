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

package net.sf.webcat.ui;

import java.text.Format;
import ognl.webobjects.WOOgnl;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver._private.WOFormatterRepository;
import com.webobjects.foundation.NSDictionary;

//------------------------------------------------------------------------
/**
 * A cell that contains a simple or formatted string value.
 *
 * <p><b>Cell Properties</b></p>
 * <ul>
 * <li><b>cssClass:</b> one or more CSS class names (space-separated) that will
 * be applied to the &lt;span&gt; tag containing the cell's value.
 * <li><b>style:</b> a CSS style string that will be applied to the contents of
 * the cell.
 * <li><b>formatter:</b> a {@link java.text.Format} object that will be used to
 * format the value of the cell.</li>
 * <li><b>numberFormat:</b> a string specifying the number format to use to
 * format the value of the cell.
 * <li><b>dateFormat:</b> a string specifying the date format to use to format
 * the value of the cell.
 * <li><b>template:</b> a template that can contain placeholders that will be
 * filled in by keypaths/OGNL expressions evaluated on the cell's value. For
 * example, if the cell's value is a User object, then the template
 * <code>"The user's name is $userName"</code> would replace
 * <code>$userName</code> with the user name of that user. Placeholders can
 * either be specified as <code>$identifier</code>, in which case the
 * placeholder will be every character after the dollar sign up until the first
 * character that is not an identifier character or a period, or
 * <code>${expression}</code>, where the expression is everything contained
 * within the braces. In both cases, the identifier or expression is evaluated
 * using OGNL. Furthermore, if a template is specified, HTML is no longer
 * escaped in the output, so that the template can contain tags.</li>
 * </ul>
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class StringCell extends WCTableCell
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public StringCell(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................
   
    public Format formatter;
    

    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void setProperties(NSDictionary<String, Object> props)
    {
        super.setProperties(props);

        // Store the formatter or convert a specified format string, if
        // necessary.

        template = (String) props.objectForKey("template");

        Format formatter = (Format) props.objectForKey("formatter");
        if (formatter != null)
        {
            this.formatter = formatter;
        }
        else
        {
            String numberFormat = (String) props.objectForKey("numberFormat");
            if (numberFormat != null)
            {
                formatter =
                    WOFormatterRepository.numberFormatterForFormatString(
                            numberFormat);
            }
            else
            {
                String dateFormat = (String) props.objectForKey("dateFormat");
                if (dateFormat != null)
                {
                    formatter =
                        WOFormatterRepository.dateFormatterForFormatString(
                                dateFormat);
                }
            }
        }
    }
    

    // ----------------------------------------------------------
    /**
     * Gets the string representation of the cell's value after the formatter
     * is applied.
     * 
     * @return the string representation of the value after the formatter is
     *      applied
     */
    public String formattedValue()
    {
        if (value == null)
        {
            return "";
        }

        if (template != null)
        {
            return evaluatedTemplate();
        }
        else if (formatter != null)
        {
            return formatter.format(value);
        }
        else
        {
            return value.toString();
        }
    }
    
    
    // ----------------------------------------------------------
    public boolean escapeHTML()
    {
        return (template == null);
    }


    // ----------------------------------------------------------
    /**
     * Gets the string representation of the cell's value by evaluating the
     * template and plugging in interpolated values.
     * 
     * @return the string representation of the cell by evaluating the template
     */
    public String evaluatedTemplate()
    {
        if (template == null)
        {
            return null;
        }
        else
        {
            StringBuffer buffer = new StringBuffer(template.length());

            int startIndex = 0;
            
            while (startIndex < template.length())
            {
                int dollarIndex = template.indexOf('$', startIndex);
                while (dollarIndex > 0
                        && template.charAt(dollarIndex - 1) == '\\')
                {
                    // Skip the dollar sign if it's escaped by a backslash.
                    dollarIndex = template.indexOf('$', dollarIndex + 1);
                }

                if (dollarIndex == -1)
                {
                    String rawText = template.substring(startIndex,
                            template.length());
                    buffer.append(rawText);
                    startIndex = template.length();
                }
                else
                {
                    String rawText = template.substring(startIndex,
                            dollarIndex);
                    buffer.append(rawText);
                
                    String keyPath = null;
        
                    if (template.charAt(dollarIndex + 1) == '{')
                    {
                        // The interpolated name is between braces.
        
                        int endBraceIndex = template.indexOf('}',
                                dollarIndex + 2);
                        keyPath = template.substring(dollarIndex + 2,
                                endBraceIndex);
                        startIndex = endBraceIndex + 1;
                    }
                    else
                    {
                        // The interpolated name is everything up until the
                        // first character that is not an identifier or a
                        // period.
                        
                        StringBuffer keyPathBuffer = new StringBuffer(20);
        
                        int index = dollarIndex + 1;
                        char ch = template.charAt(index);
                        
                        while (ch != '$' &&
                                (Character.isJavaIdentifierPart(ch) ||
                                        ch == '.'))
                        {
                            keyPathBuffer.append(ch);
                            
                            index++;
                            ch = template.charAt(index);
                        }
        
                        keyPath = keyPathBuffer.toString();
                        startIndex = index;
                    }
        
                    Object subValue = WOOgnl.factory().getValue(keyPath, value);
                    
                    buffer.append(subValue.toString());
                }
            }
            
            return buffer.toString();
        }
    }


    //~ Static/instance variables .............................................
    
    private String template;
}
