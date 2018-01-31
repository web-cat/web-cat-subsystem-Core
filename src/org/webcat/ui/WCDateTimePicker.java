/*==========================================================================*\
 |  $Id: WCDateTimePicker.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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

package org.webcat.ui;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;

// ------------------------------------------------------------------------
/**
 * Combines the Dojo date picker and time picker into a single component, with
 * a single {@code value} binding from which the values for both are obtained.
 *
 * <h2>Bindings</h2>
 * <table>
 * <tr>
 * <td>{@code value}</td>
 * <td>The NSTimestamp value of the date/time picker.</td>
 * </tr>
 * <tr>
 * <td>{@code dateformat}</td>
 * <td>A format string for the date part of the widget.</td>
 * </tr>
 * <tr>
 * <td>{@code timeformat}</td>
 * <td>A format string for the time part of the widget.</td>
 * </tr>
 * <tr>
 * <td>{@code dateWidth}</td>
 * <td>The width of the date part of the widget, specified as a string in CSS
 * units (that is, pixels, ems, a percentage, etc).</td>
 * </tr>
 * <tr>
 * <td>{@code timeWidth}</td>
 * <td>The width of the time part of the widget, specified as a string in CSS
 * units (that is, pixels, ems, a percentage, etc).</td>
 * </tr>
 * <tr>
 * <td>{@code timeZone}</td>
 * <td>A {@link TimeZone} object defining the time zone
 * for localizing the date and time.</td>
 * </tr>
 * </table>
 *
 * @author Tony Allevato
 * @author Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2010/05/11 14:51:58 $
 */
public class WCDateTimePicker extends WOComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new date/time picker.
     *
     * @param context
     *            the context
     */
    public WCDateTimePicker(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public String dateformat;
    public String timeformat;
    public String dateWidth;
    public String timeWidth;

    public TimeZone    timeZone;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets a timestamp representing the combined date and time parts of the
     * date picker.
     *
     * @return the timestamp representing the combined values of the date part
     *         and the time part
     */
    public NSTimestamp value()
    {
        GregorianCalendar valueCalendar =  (timeZone == null)
            ? new GregorianCalendar()
            : new GregorianCalendar(timeZone);
        valueCalendar.clear();

        splitIncomingValueIfNecessary();

        GregorianCalendar datePartCalendar = new GregorianCalendar(utc);
        datePartCalendar.setTime(datePartOfValue);

        GregorianCalendar timePartCalendar = new GregorianCalendar(utc);
        timePartCalendar.setTime(timePartOfValue);

        valueCalendar.set(
            datePartCalendar.get(Calendar.YEAR),
            datePartCalendar.get(Calendar.MONTH),
            datePartCalendar.get(Calendar.DAY_OF_MONTH),
            timePartCalendar.get(Calendar.HOUR_OF_DAY),
            timePartCalendar.get(Calendar.MINUTE));

        return new NSTimestamp(valueCalendar.getTime());
    }


    // ----------------------------------------------------------
    /**
     * Sets the date and time for the date picker.
     *
     * @param aValue an NSTimestamp
     */
    public void setValue(NSTimestamp aValue)
    {
        incomingValue = aValue;
        datePartOfValue = null;
        timePartOfValue = null;
    }


    // ----------------------------------------------------------
    public String dateWidthStyle()
    {
        if (dateWidth == null)
        {
            return null;
        }
        else
        {
            return "width: " + dateWidth;
        }
    }


    // ----------------------------------------------------------
    public String timeWidthStyle()
    {
        if (timeWidth == null)
        {
            return null;
        }
        else
        {
            return "width: " + timeWidth;
        }
    }


    // ----------------------------------------------------------
    public NSTimestamp datePartOfValue()
    {
        splitIncomingValueIfNecessary();
        return datePartOfValue;
    }


    // ----------------------------------------------------------
    public void setDatePartOfValue(NSTimestamp value)
    {
        datePartOfValue = value;
    }


    // ----------------------------------------------------------
    public NSTimestamp timePartOfValue()
    {
        splitIncomingValueIfNecessary();
        return timePartOfValue;
    }


    // ----------------------------------------------------------
    public void setTimePartOfValue(NSTimestamp value)
    {
        timePartOfValue = value;
    }


    // ----------------------------------------------------------
    public TimeZone UTC()
    {
        return utc;
    }


    // ----------------------------------------------------------
    private void splitIncomingValueIfNecessary()
    {
        if (datePartOfValue == null || timePartOfValue == null)
        {
            if (incomingValue == null)
            {
                incomingValue = new NSTimestamp();
            }
            GregorianCalendar incoming = (timeZone == null)
                ? new GregorianCalendar()
                : new GregorianCalendar(timeZone);
            incoming.setTime(incomingValue);

            GregorianCalendar datePartCalendar = new GregorianCalendar(utc);
            datePartCalendar.clear();
            datePartCalendar.set(
                incoming.get(Calendar.YEAR),
                incoming.get(Calendar.MONTH),
                incoming.get(Calendar.DAY_OF_MONTH));
            datePartOfValue = new NSTimestamp(datePartCalendar.getTime());

            GregorianCalendar timePartCalendar = new GregorianCalendar(utc);
            timePartCalendar.clear();
            timePartCalendar.set(
                1970,
                0,
                1,
                incoming.get(Calendar.HOUR_OF_DAY),
                incoming.get(Calendar.MINUTE),
                incoming.get(Calendar.SECOND));
            timePartCalendar.set(GregorianCalendar.MONTH, 0);
            timePartOfValue =
                new NSTimestamp(timePartCalendar.getTime());
        }
    }


    //~ Static/instance variables .............................................

    private NSTimestamp incomingValue;
    private NSTimestamp datePartOfValue;
    private NSTimestamp timePartOfValue;

    private static TimeZone utc = TimeZone.getTimeZone("UTC");
}
