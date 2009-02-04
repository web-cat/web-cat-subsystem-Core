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

import java.text.Format;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver._private.WOFormatterRepository;
import com.webobjects.foundation.NSDictionary;

//------------------------------------------------------------------------
/**
 * A cell that contains a simple string value.
 *
 * <p><b>Cell Properties</b></p>
 * <ul>
 * <li><b>cssClass:</b> one or more CSS class names (space-separated) that will
 * be applied to the &lt;span&gt; tag containing the cell's value.
 * <li><b>style:</b> a CSS style string that will be applied to the contents of
 * the cell.
 * <li><b>action:</b> the name of the action to be performed on the component
 * that contains the table.</li>
 * <li><b>formatter:</b> a {@link java.text.Format} object that will be used to
 * format the value of the cell.</li>
 * <li><b>numberFormat:</b> a string specifying the number format to use to
 * format the value of the cell.
 * <li><b>dateFormat:</b> a string specifying the date format to use to format
 * the value of the cell.
 * </ul>
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class ActionURLCell extends AbstractObjectTableCell
{
	//~ Constructors ..........................................................
    
    // ----------------------------------------------------------
    public ActionURLCell(WOContext context)
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

        Format format = (Format) props.objectForKey("formatter");
        if (format != null)
        {
            formatter = format;
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
        if (formatter != null)
        {
            return formatter.format(value);
        }
        else
        {
            return value.toString();
        }
    }

    
    // ----------------------------------------------------------
    /**
     * Performs the action named in the "action" property on the component that
     * contains the ObjectTable.
     * 
     * @return the results of the action
     */
	public WOActionResults performAction()
	{
		String action = (String) properties.objectForKey("action");
		return parent().performParentAction(action);
	}
}
