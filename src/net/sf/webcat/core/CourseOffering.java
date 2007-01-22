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
import com.webobjects.eocontrol.*;


// -------------------------------------------------------------------------
/**
 * Represents a single offering of a course (i.e., one section in a given
 * semester).
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class CourseOffering
    extends _CourseOffering
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new CourseOffering object.
     */
    public CourseOffering()
    {
        super();
    }


    //~ Constants (for key names) .............................................

    // Derived Attributes ---
    public static final String COURSE_NUMBER_KEY  =
        COURSE_KEY + "." + Course.NUMBER_KEY;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Creates a string containing a comma-separated list of
     * instructor e-mail addresses.  Returns null if no instructors
     * for this course offering have been defined.
     *
     * @return the e-mail address(es) as a string
     */
    public NSArray studentsSortedByPID()
    {
        return EOSortOrdering.sortedArrayUsingKeyOrderArray(
               students(),
               new NSArray( new Object[]{
                   new EOSortOrdering(
                           User.USER_NAME_KEY,
                           EOSortOrdering.CompareCaseInsensitiveAscending
                       )
               })
           );
    }


    // ----------------------------------------------------------
    /**
     * Returns the course's department abbreviation combined with
     * the course's number (e.g., "CS 1705").
     * @return the department abbreviation and the course number
     */
    public String compactName()
    {
        if ( cachedCompactName == null )
        {
            cachedCompactName = course().deptNumber() + "(" + crn() + ")";
        }
        return cachedCompactName;
    }


    // ----------------------------------------------------------
    /**
     * Returns the course's department abbreviation combined with
     * the course's number (e.g., "CS 1705").
     * @return the department abbreviation and the course number
     */
    public String deptNumberAndName()
    {
        if ( cachedDeptNumberAndName == null )
        {
            cachedDeptNumberAndName = compactName() + ": " + course().name();
        }
        return cachedDeptNumberAndName;
    }


    // ----------------------------------------------------------
    /**
     * Returns true if the given user is an instructor of this
     * course offering
     * 
     * @param user     The user to check
     * @return true if the user is an instructor of the offering
     */
    public boolean isInstructor( User user )
    {
        NSArray instructors = instructors();
        return ( ( instructors.indexOfObject( user ) ) != NSArray.NotFound );
    }


    // ----------------------------------------------------------
    /**
     * Returns true if the given user is a TA for this
     * course offering
     * 
     * @param user     The user to check
     * @return true if the user is a TA for the offering
     */
    public boolean isTA( User user )
    {
        NSArray tas = TAs();
        return ( ( tas.indexOfObject( user ) ) != NSArray.NotFound );
    }


    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see net.sf.webcat.core._CourseOffering#setCourse(net.sf.webcat.core.Course)
     */
    public void setCourse( Course value )
    {
        cachedCompactName = null;
        cachedDeptNumberAndName = null;
        super.setCourse( value );
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>crn</code>
     * property.
     * 
     * @param value The new value for this property
     */
    public void setCrn( String value )
    {
        cachedSubdirName = null;
        cachedCompactName = null;
        cachedDeptNumberAndName = null;
        super.setCrn( value );
    }


    // ----------------------------------------------------------
    public String crnSubdirName()
    {
        if ( cachedSubdirName == null )
        {
            String name = crn();
            cachedSubdirName = subdirNameOf( name );
            log.debug( "trimmed name '" + name + "' to '"
                       + cachedSubdirName + "'" );
        }
        return cachedSubdirName;
    }


    // ----------------------------------------------------------
    public static String subdirNameOf( String name )
    {
        String result = null;
        if ( name != null )
        {
            char[] chars = new char[ name.length() ];
            int  pos   = 0;
            for ( int i = 0; i < name.length(); i++ )
            {
                char c = name.charAt( i );
                if ( Character.isLetterOrDigit( c ) ||
                     c == '_'                       ||
                     c == '-' )
                {
                    chars[ pos ] = c;
                    pos++;
                }
            }
            result = new String( chars, 0, pos );
        }
        return result;
    }


    // ----------------------------------------------------------
//    /**
//     * Creates a string containing a comma-separated list of
//     * instructor e-mail addresses.  Returns null if no instructors
//     * for this course offering have been defined.
//     *
//     * @return the e-mail address(es) as a string
//     */
//    public String instructorEmailString()
//    {
//     NSArray instructors = instructors();
//     StringBuffer instructorEmailAddrs = new StringBuffer( 20 );
//     for ( int i = 0; i < instructors.count(); i++ )
//     {
//         if ( i > 0 )
//         {
//             instructorEmailAddrs.append( ", " );
//         }
//         instructorEmailAddrs.append(
//             ( (WCUser)instructors.objectAtIndex( i ) ).email() );
//     }

//     return ( instructors.count() != 0 )
//         ? instructorEmailAddrs.toString()
//         : null;
//    }


// If you add instance variables to store property values you
// should add empty implementions of the Serialization methods
// to avoid unnecessary overhead (the properties will be
// serialized for you in the superclass).

//    // ----------------------------------------------------------
//    /**
//     * Serialize this object (an empty implementation, since the
//     * superclass handles this responsibility).
//     * @param out the stream to write to
//     */
//    private void writeObject( java.io.ObjectOutputStream out )
//        throws java.io.IOException
//    {
//    }
//
//
//    // ----------------------------------------------------------
//    /**
//     * Read in a serialized object (an empty implementation, since the
//     * superclass handles this responsibility).
//     * @param in the stream to read from
//     */
//    private void readObject( java.io.ObjectInputStream in )
//        throws java.io.IOException, java.lang.ClassNotFoundException
//    {
//    }

    //~ Instance/static variables .............................................

    private String cachedSubdirName        = null;
    private String cachedCompactName       = null;
    private String cachedDeptNumberAndName = null;
}
