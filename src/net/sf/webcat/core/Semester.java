/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License
 |  along with Web-CAT; if not, write to the Free Software
 |  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 |
 |  Project manager: Stephen Edwards <edwards@cs.vt.edu>
 |  Virginia Tech CS Dept, 660 McBryde Hall (0106), Blacksburg, VA 24061 USA
\*==========================================================================*/

package net.sf.webcat.core;

import com.webobjects.foundation.*;
import com.webobjects.foundation.NSValidation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;

import java.io.*;

import org.apache.log4j.*;

// -------------------------------------------------------------------------
/**
 * Represents a single school semester.
 *
 * @author Stephen Edwards
 * @version $Id$
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
     * Look up a Semester by id number.  Assumes the editing
     * context is appropriately locked.
     * @param ec The editing context to use
     * @param id The id to look up
     * @return The semester, or null if no such id exists
     */
    public static Semester semesterForId( EOEditingContext ec, int id )
    {
        Semester semester = null;
        NSArray results = EOUtilities.objectsMatchingKeyAndValue( ec,
            ENTITY_NAME, "id", new Integer( id ) );
        if ( results != null && results.count() > 0 )
        {
            semester = (Semester)results.objectAtIndex( 0 );
        }
        return semester;
    }


    // ----------------------------------------------------------
    /**
     * Look up a Semester by id number.  Assumes the editing
     * context is appropriately locked.
     * @param ec The editing context to use
     * @param id The id to look up
     * @return The semester, or null if no such id exists
     */
    public static Semester semesterForId( EOEditingContext ec, String id )
    {
        Semester semester = null;
        int idNumber = er.extensions.ERXValueUtilities.intValue( id );
        if ( idNumber > 0 )
        {
            semester = semesterForId( ec, idNumber );
        }
        return semester;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Retrieve this object's <code>id</code> value.
     * @return the value of the attribute
     */
    public Number id()
    {
        try
        {
            return (Number)EOUtilities.primaryKeyForObject(
                editingContext() , this ).objectForKey( "id" );
        }
        catch (Exception e)
        {
            return er.extensions.ERXConstant.ZeroInteger;
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns the "season" portion of a semester name as a string.
     *
     * @return The season name as a string
     */
    public String seasonName()
    {
        return (String)names.objectAtIndex( season().intValue() );
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
    public void setSeason( Number value )
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
        NSArray others = EOUtilities.objectsMatchingValues(
            editingContext(),
            ENTITY_NAME,
            new NSDictionary(
                new Object[] {  year(),   season()   },
                new Object[] {  YEAR_KEY, SEASON_KEY }
                ) );
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
        return seasonName() + ", " + year();
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
     * Get a human-readable representation of this semester, which is
     * the same as {@link #userPresentableDescription()}.
     * @return this semester's name
     */
    public String toString()
    {
        return userPresentableDescription();
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
    /* (non-Javadoc)
     * @see er.extensions.ERXGenericRecord#didUpdate()
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
     * @param semester the semester to guess for
     * @return The month (starting from 1)
     */
    public static int defaultStartingMonth( int semester )
    {
        int result = 1;
        switch ( semester )
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
     * Guess the ending month for a semester (assumed to end on last day
     * of the month).
     * @param semester the semester to guess for
     * @return The month (starting from 1)
     */
    public static int defaultEndingMonth( int semester )
    {
        int result = 12;
        switch ( semester )
        {
            case SPRING:  result = 5; break;
            case SUMMER1: result = 6; break;
            case SUMMER2: result = 7; break;
            case WINTER:  result = 2; break;
        }
        return result;
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private void renameSubdirs( String oldSubdir, String newSubdir )
    {
        NSArray domains = AuthenticationDomain.authDomains();
        String msgs = null;
        for ( int i = 0; i < domains.count(); i++ )
        {
            AuthenticationDomain domain =
                (AuthenticationDomain)domains.objectAtIndex( i );
            StringBuffer dir = domain.submissionBaseDirBuffer();
            dir.append('/');
            int baseDirLen = dir.length();
            dir.append( oldSubdir );
            File oldDir = new File( dir.toString() );
            log.debug("Checking for: " + oldDir);
            if ( oldDir.exists() )
            {
                dir.delete( baseDirLen, dir.length() );
                dir.append( newSubdir );
                File newDir = new File( dir.toString() );
                log.debug("Renaming: " + oldDir + " => " + newDir);
                if (!oldDir.renameTo( newDir ))
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

    public static final NSArray names = new NSArray( new Object[] {
            "Spring",
            "Summer I",
            "Summer II",
            "Fall",
            "Winter"
        });

    private String dirNeedingRenaming      = null;

    static Logger log = Logger.getLogger( Semester.class );
}
