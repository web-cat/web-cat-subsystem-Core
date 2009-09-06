/*==========================================================================*\
 |  $Id$
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

package net.sf.webcat.ui;

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
 * @author Last changed by $Author$
 * @version $Revision$, $Date$
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

    public NSTimestamp datePartOfValue;
    public NSTimestamp timePartOfValue;
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
        GregorianCalendar valueCalendar = new GregorianCalendar();

        GregorianCalendar datePartCalendar = new GregorianCalendar();
        datePartCalendar.setTime(datePartOfValue);
        valueCalendar.set(GregorianCalendar.YEAR, datePartCalendar
                .get(GregorianCalendar.YEAR));
        valueCalendar.set(GregorianCalendar.DAY_OF_YEAR, datePartCalendar
                .get(GregorianCalendar.DAY_OF_YEAR));

        GregorianCalendar timePartCalendar = new GregorianCalendar();
        timePartCalendar.setTime(timePartOfValue);
        valueCalendar.set(GregorianCalendar.HOUR_OF_DAY, timePartCalendar
                .get(GregorianCalendar.HOUR_OF_DAY));
        valueCalendar.set(GregorianCalendar.MINUTE, timePartCalendar
                .get(GregorianCalendar.MINUTE));
        valueCalendar.set(GregorianCalendar.SECOND, timePartCalendar
                .get(GregorianCalendar.SECOND));
        valueCalendar.set(GregorianCalendar.MILLISECOND, timePartCalendar
                .get(GregorianCalendar.MILLISECOND));

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
        GregorianCalendar aValueCalendar = new GregorianCalendar();

        if (aValue == null)
        {
            aValue = new NSTimestamp();
        }

        aValueCalendar.setTime(aValue);

        GregorianCalendar datePartCalendar = new GregorianCalendar();
        datePartCalendar.set(GregorianCalendar.YEAR, aValueCalendar
                .get(GregorianCalendar.YEAR));
        datePartCalendar.set(GregorianCalendar.DAY_OF_YEAR, aValueCalendar
                .get(GregorianCalendar.DAY_OF_YEAR));
        datePartOfValue = new NSTimestamp(datePartCalendar.getTime());

        GregorianCalendar timePartCalendar = new GregorianCalendar();
        timePartCalendar.set(GregorianCalendar.HOUR_OF_DAY, aValueCalendar
                .get(GregorianCalendar.HOUR_OF_DAY));
        timePartCalendar.set(GregorianCalendar.MINUTE, aValueCalendar
                .get(GregorianCalendar.MINUTE));
        timePartCalendar.set(GregorianCalendar.SECOND, aValueCalendar
                .get(GregorianCalendar.SECOND));
        timePartCalendar.set(GregorianCalendar.MILLISECOND, aValueCalendar
                .get(GregorianCalendar.MILLISECOND));
        timePartOfValue = new NSTimestamp(timePartCalendar.getTime());
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


    //~ Static/instance variables .............................................

    private static final long serialVersionUID = 1L;
}
