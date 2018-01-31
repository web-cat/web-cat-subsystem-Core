/*==========================================================================*\
 |  $Id: Semester.java,v 1.3 2012/03/28 13:48:08 stedwar2 Exp $
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

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import java.io.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.webcat.core.AuthenticationDomain;
import org.webcat.core.Semester;
import org.webcat.core._Semester;
import org.apache.log4j.*;

// -------------------------------------------------------------------------
/**
 * Represents a single school semester.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.3 $, $Date: 2012/03/28 13:48:08 $
 */
public class Semester
    extends _Semester
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Semester object.
     */
    public Semester()
    {
        super();
    }


    // ----------------------------------------------------------
    /**
     * A static factory method for creating a new
     * Semester object given required
     * attributes and relationships.
     * @param editingContext The context in which the new object will be
     * inserted
     * @param theSeason The semester of the year
     * @param startDate The start date
     * @param endDate The end date
     * @return The newly created object
     */
    public static Semester create(
        EOEditingContext editingContext,
        int theSeason,
        NSTimestamp startDate,
        NSTimestamp endDate,
        int startYear
        )
    {
        Semester result = create(editingContext, startYear);
        result.setSeason(theSeason);
        result.setSemesterStartDate(startDate);
        result.setSemesterEndDate(endDate);
        return result;
    }


    // ----------------------------------------------------------
    /**
     * A static factory method for creating a new
     * Semester object given required
     * attributes and relationships.
     * @param editingContext The context in which the new object will be
     * inserted
     * @param theSeason The semester of the year
     * @param startYear The year for the semester
     * @param timeZone The time zone for localizing dates, or
     * null for the system default
     * @return The newly created object
     */
    public static Semester create(
        EOEditingContext editingContext,
        int theSeason,
        int startYear,
        TimeZone timeZone
        )
    {
        return create(
            editingContext,
            theSeason,
            defaultStartingDate(theSeason, startYear, timeZone),
            defaultEndingDate(theSeason, startYear, timeZone),
            startYear);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Returns the "season" portion of a semester name as a string.
     *
     * @return The season name as a string
     */
    public String seasonName()
    {
        String result = "none";
        Number mySeason = season();
        if (mySeason != null)
        {
            result = names.objectAtIndex(season().intValue());
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Set the season by number.
     *
     * @param value The season number
     */
    public void setSeason( int value )
    {
        setSeason( integers[value] );
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>season</code>
     * property.
     *
     * @param value The new value for this property
     */
    public void setSeason( Integer value )
    {
        if (dirNeedingRenaming == null)
        {
            dirNeedingRenaming = dirName();
        }
        super.setSeason(value);
    }


    // ----------------------------------------------------------
    public Object validateSeason( Object value )
    {
        if ( value == null || value.equals("") || ! (value instanceof Number) )
        {
            throw new ValidationException(
                "Please provide a season." );
        }
        int ival = ((Number)value).intValue();
        if (ival < 0 || ival > WINTER)
        {
            throw new ValidationException(
                "The season must be between 0 and " + WINTER + ", inclusive." );
        }
        return value;
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>year</code>
     * property.
     *
     * @param value The new value for this property
     */
    public void setYearRaw( Number value )
    {
        if (dirNeedingRenaming == null)
        {
            dirNeedingRenaming = dirName();
        }
        takeStoredValueForKey( value, "year" );
    }


    // ----------------------------------------------------------
    public void validateForSave()
        throws ValidationException
    {
        super.validateForSave();
        // Make sure the season is not a duplicate
        NSArray<Semester> others = objectsMatchingQualifier(editingContext(),
            year.is(year()).and(season.is(season())));
        if (others.count() > 1
            || (others.count() == 1
                && others.objectAtIndex(0) != this))
        {
            throw new ValidationException(
                "Another semester for this season and year already exists." );
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns the name of this semester as a string.
     *
     * @return The semester name as a string
     */
    public String name()
    {
        return seasonName() + " " + year();
    }


    // ----------------------------------------------------------
    /**
     * Get a short (no longer than 60 characters) description of this semester,
     * which currently returns {@link #name()}.
     * @return the description
     */
    public String userPresentableDescription()
    {
        return name();
    }


    // ----------------------------------------------------------
    /**
     * Returns the name of this semester in a form usable as a
     * subdirectory name.
     *
     * @return The semester name as a string
     */
    public String dirName()
    {
        return ( seasonName() + year() ).replaceAll( "\\s", "" );
    }


    // ----------------------------------------------------------
    @Override
    public void willUpdate()
    {
        java.util.GregorianCalendar now = new java.util.GregorianCalendar();
        int thisMonth = now.get(Calendar.MONTH) + 1;
        if (year() == 0)
        {
            setYear(now.get(Calendar.YEAR));
        }
        if (season() == null)
        {
            setSeason(defaultSemesterFor(now));
        }
        if (semesterStartDate() == null)
        {
            // remember, these months start at 1, while those stored
            // in "now" start at zero ...
            int month = defaultStartingMonth( season().intValue() );
            int startYear = year();
            if (month > thisMonth)
            {
                startYear--;
            }
            NSTimestamp start = new NSTimestamp(
                startYear, month, 1, 0, 0, 0, java.util.TimeZone.getDefault()
                );
            if (semesterEndDate() == null || semesterEndDate().after( start ))
            {
                setSemesterStartDate(start);
            }
            else
            {
                setSemesterStartDate(semesterEndDate());
            }
        }
        if (semesterEndDate() == null)
        {
            int month = defaultEndingMonth( season().intValue() );
            int endYear = year();
            if (month < thisMonth)
            {
                endYear++;
            }
            NSTimestamp end = new NSTimestamp(
                endYear, month, 1, 23, 59, 59, java.util.TimeZone.getDefault()
                );
            if (semesterStartDate() == null || semesterStartDate().before(end))
            {
                setSemesterEndDate(end);
            }
            else
            {
                setSemesterEndDate(semesterStartDate());
            }
        }
        super.willUpdate();
    }


    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see er.extensions.eof.ERXGenericRecord#didUpdate()
     */
    public void didUpdate()
    {
        super.didUpdate();
        if ( dirNeedingRenaming != null )
        {
            renameSubdirs( dirNeedingRenaming, dirName() );
            dirNeedingRenaming = null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Guess a semester for a given date.
     * @param date the date to guess for
     * @return The semester
     */
    public static int defaultSemesterFor( java.util.Calendar date )
    {
        int month = date.get( java.util.Calendar.MONTH ) + 1;
        if ( month <= 5 )
            return SPRING;
        else if ( month <= 6 )
            return SUMMER1;
        else if ( month <= 7 )
            return SUMMER2;
        else
            return FALL;
    }


    // ----------------------------------------------------------
    /**
     * Guess the starting month for a semester (assumed to start on day 1).
     * @param theSeason the semester to guess for
     * @return The month (starting from 1)
     */
    public static int defaultStartingMonth(int theSeason)
    {
        int result = 1;
        switch (theSeason)
        {
            case SUMMER1: result = 6; break;
            case SUMMER2: result = 7; break;
            case FALL:    result = 8; break;
            case WINTER:  result = 11; break;
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Guess the starting date for a semester.
     * @param theSeason the semester to guess for
     * @param startYear the year in which the semester starts
     * @param timeZone the time zone to use for localization
     * @return The starting date, as a time stamp
     */
    public static NSTimestamp defaultStartingDate(
        int theSeason, int startYear, TimeZone timeZone)
    {
        // Note that these month values start at 1, not 0, so they need to
        // be decremented before being shoved into a calendar object
        int startMonth = defaultStartingMonth(theSeason);
        int startDay = 1;

        Calendar cal = (timeZone == null)
            ? new GregorianCalendar()
            : new GregorianCalendar(timeZone);
        cal.clear();
        cal.set(Calendar.YEAR, startYear);
        cal.set(Calendar.MONTH, startMonth - 1);
        cal.set(Calendar.DAY_OF_MONTH, startDay);
        return new NSTimestamp(cal.getTimeInMillis());
    }


    // ----------------------------------------------------------
    /**
     * Guess the ending month for a semester (assumed to end on last day
     * of the month).
     * @param theSeason the semester to guess for
     * @return The month (starting from 1)
     */
    public static int defaultEndingMonth(int theSeason)
    {
        int result = 12;
        switch (theSeason)
        {
            case SPRING:  result = 5; break;
            case SUMMER1: result = 6; break;
            case SUMMER2: result = 7; break;
            case WINTER:  result = 2; break;
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Guess the ending date for a semester.
     * @param theSeason the semester to guess for
     * @param startYear the year in which the semester starts (<b>not</b> ends)
     * @param timeZone the time zone to use for localization
     * @return The ending date, as a time stamp
     */
    public static NSTimestamp defaultEndingDate(
        int theSeason, int startYear, TimeZone timeZone)
    {
        // Note that these month values start at 1, not 0, so they need to
        // be decremented before being shoved into a calendar object
        int endMonth = defaultEndingMonth(theSeason);
        int endDay = 30;
        if (defaultStartingMonth(theSeason) > endMonth)
        {
            startYear++;
        }
        switch (endMonth)
        {
            case 2: endDay = 28; break;
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12: endDay = 31; break;
        }

        Calendar cal = (timeZone == null)
            ? new GregorianCalendar()
            : new GregorianCalendar(timeZone);
        cal.clear();
        cal.set(Calendar.YEAR, startYear);
        cal.set(Calendar.MONTH, endMonth - 1);
        cal.set(Calendar.DAY_OF_MONTH, endDay);
        return new NSTimestamp(cal.getTimeInMillis());
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private void renameSubdirs(String oldSubdir, String newSubdir)
    {
        NSArray<AuthenticationDomain> domains =
            AuthenticationDomain.authDomains();
        String msgs = null;
        for (AuthenticationDomain domain : domains)
        {
            StringBuffer dir = domain.submissionBaseDirBuffer();
            dir.append('/');
            int baseDirLen = dir.length();
            dir.append(oldSubdir);
            File oldDir = new File(dir.toString());
            log.debug("Checking for: " + oldDir);
            if (oldDir.exists())
            {
                dir.delete(baseDirLen, dir.length());
                dir.append(newSubdir);
                File newDir = new File(dir.toString());
                log.debug("Renaming: " + oldDir + " => " + newDir);
                if (!oldDir.renameTo(newDir))
                {
                    msgs = (msgs == null ? "" : (msgs + "  "))
                        + "Failed to rename directory: "
                        + oldDir + " => " + newDir;
                }
            }
        }
        if (msgs != null)
        {
            throw new RuntimeException(msgs);
        }
    }


    //~ Instance/static variables .............................................

    public static final int SPRING  = 0;
    public static final int SUMMER1 = 1;
    public static final int SUMMER2 = 2;
    public static final int FALL    = 3;
    public static final int WINTER  = 4; // For quarters instead of semesters

    public static final Integer[] integers = {
            new Integer( SPRING ),
            new Integer( SUMMER1 ),
            new Integer( SUMMER2 ),
            new Integer( FALL ),
            new Integer( WINTER )
        };

    public static final NSArray<Integer> integersInNS =
        new NSArray<Integer>(integers);

    public static final NSArray<String> names = new NSArray<String>(
        new String[] {
            "Spring",
            "Summer I",
            "Summer II",
            "Fall",
            "Winter"
        });

    private String dirNeedingRenaming      = null;

    static Logger log = Logger.getLogger( Semester.class );
}
