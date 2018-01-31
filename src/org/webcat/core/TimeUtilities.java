/*==========================================================================*\
 |  $Id: TimeUtilities.java,v 1.1 2011/04/19 16:47:36 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011 Virginia Tech
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

import java.util.Calendar;
import java.util.GregorianCalendar;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * Helper methods that deal with dates and times.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/04/19 16:47:36 $
 */
public class TimeUtilities
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    private TimeUtilities()
    {
        // Prevent instantiation.
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether two timestamps fall on the same calendar
     * day.
     *
     * @param date1 the first date
     * @param date2 the second date
     * @return true if the dates fall on the same calendar day, otherwise false
     */
    public static boolean sameCalendarDay(NSTimestamp date1, NSTimestamp date2)
    {
        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new GregorianCalendar();

        cal1.setTime(date1);
        cal2.setTime(date2);

        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE));
    }


    // ----------------------------------------------------------
    /**
     * Computes the relative time from the origin to the target and returns a
     * human-readable string that describes the change in time. If the target
     * comes before the origin, then the time difference represents one in the
     * past, which will have the negative suffix appended to it. If the target
     * comes after the origin, then the difference is appended with the
     * positive suffix.
     *
     * @param origin the origin timestamp
     * @param target the target timestamp
     * @param approximate if true, only the most significant time unit will be
     *     included in the result; otherwise, all time units are included
     * @param positiveSuffix the suffix to use if the target comes after the
     *     origin (for example, "late" or "from now")
     * @param negativeSuffix the suffix to use if the target comes before the
     *     origin (for example, "early" or "ago")
     * @return a human-readable string representing the difference in the
     *     origin and target times
     */
    public static String stringForTimeDifference(NSTimestamp origin,
            NSTimestamp target, boolean approximate,
            String positiveSuffix, String negativeSuffix)
    {
        StringBuffer buffer = new StringBuffer();

        long totalMillis = Math.abs(target.getTime() - origin.getTime());
        long totalSeconds = totalMillis / MILLIS_PER_SECOND;

        long justSeconds = totalSeconds % SECONDS_PER_MINUTE;
        long totalMinutes = totalSeconds / SECONDS_PER_MINUTE;

        long justMinutes = totalMinutes % MINUTES_PER_HOUR;
        long totalHours = totalMinutes / MINUTES_PER_HOUR;

        long justHours = totalHours % HOURS_PER_DAY;
        long totalDays = totalHours / HOURS_PER_DAY;

        long justDays = totalDays % DAYS_PER_MONTH;
        long totalMonths = totalDays / DAYS_PER_MONTH;

        long justMonths = totalMonths % MONTHS_PER_YEAR;
        long totalYears = totalMonths / MONTHS_PER_YEAR;

        boolean appended = false;

        appended = appendUnit(buffer, totalYears, "year");

        if (!approximate || !appended)
        {
            appended |= appendUnit(buffer, justMonths, "month");
        }

        if (!approximate || !appended)
        {
            appended |= appendUnit(buffer, justDays, "day");
        }

        if (!approximate || !appended)
        {
            appended |= appendUnit(buffer, justHours, "hour");
        }

        if (!approximate || !appended)
        {
            appended |= appendUnit(buffer, justMinutes, "minute");
        }

        if (!approximate || !appended)
        {
            appended |= appendUnit(buffer, justSeconds, "second");
        }

        if (appended)
        {
            buffer.append(' ');

            if (target.before(origin))
            {
                buffer.append(negativeSuffix);
            }
            else
            {
                buffer.append(positiveSuffix);
            }
        }
        else
        {
            buffer.append("just now");
        }

        return buffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * Computes the relative time from the origin to the target and returns a
     * human-readable string that describes the change in time. This version of
     * the method treats the origin as a deadline, appending "late" if the
     * target is after the origin and "early" if the target is before.
     *
     * @param origin the origin ("deadline") timestamp
     * @param target the target timestamp
     * @param approximate if true, only the most significant time unit will be
     *     included in the result; otherwise, all time units are included
     * @return a human-readable string representing the difference in the
     *     origin and target times
     */
    public static String stringForTimeDifference(NSTimestamp origin,
            NSTimestamp target, boolean approximate)
    {
        return stringForTimeDifference(origin, target, approximate,
                "late", "early");
    }


    // ----------------------------------------------------------
    /**
     * Computes the relative time from the current time to the target and
     * returns a human-readable string that describes the change in time. Since
     * this version of the method forces the origin time to be the current
     * time, it appends "from now" if the target is later than the current time
     * and "ago" if the target is earlier.
     *
     * @param target the target timestamp
     * @param approximate if true, only the most significant time unit will be
     *     included in the result; otherwise, all time units are included
     * @return a human-readable string representing the difference between the
     *     current time and the target time
     */
    public static String stringForTimeFromNow(NSTimestamp target,
            boolean approximate)
    {
        return stringForTimeDifference(new NSTimestamp(), target, approximate,
                "from now", "ago");
    }


    // ----------------------------------------------------------
    /**
     * Appends a time unit to a {@code StringBuffer}, handling pluralization
     * and commas appropriately.
     *
     * @param buffer the string buffer to append to
     * @param value the number of time units
     * @param unit the name of the time unit (hour, minute, day, etc.)
     * @return true if something was actually appended (value &gt; 0),
     *     otherwise false
     */
    private static boolean appendUnit(StringBuffer buffer, long value,
            String unit)
    {
        if (value > 0)
        {
            if (buffer.length() > 0)
            {
                buffer.append(", ");
            }

            buffer.append(value);
            buffer.append(' ');
            buffer.append(unit);

            if (value > 1)
            {
                buffer.append('s');
            }

            return true;
        }
        else
        {
            return false;
        }
    }


    //~ Static/instance variables .............................................

    private static final long MILLIS_PER_SECOND = 1000L;
    private static final long SECONDS_PER_MINUTE = 60L;
    private static final long MINUTES_PER_HOUR = 60L;
    private static final long HOURS_PER_DAY = 24L;
    private static final long DAYS_PER_MONTH = 30L;
    private static final long MONTHS_PER_YEAR = 12L;
}
