/*==========================================================================*\
 |  $Id: Course.java,v 1.5 2012/06/22 16:23:17 aallowat Exp $
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;

// -------------------------------------------------------------------------
/**
 * Represents one course, which may be taught multiple times in different
 * semesters (represented by separate course offerings).
 *
 * @author  Stephen Edwards
 * @author  Last changed by: $Author: aallowat $
 * @version $Revision: 1.5 $, $Date: 2012/06/22 16:23:17 $
 */
public class Course
    extends _Course
    implements RepositoryProvider
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Course object.
     */
    public Course()
    {
        super();
    }


    //~ Constants (for key names) .............................................

    // Derived Attributes ---
    public static final String INSTITUTION_KEY  =
        DEPARTMENT_KEY + "." + Department.INSTITUTION_KEY;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Returns the course's department abbreviation combined with
     * the course's number (e.g., "CS 1705").
     * @return the department abbreviation and the course number
     */
    public String deptNumber()
    {
        if (department() != null)
        {
            return department().abbreviation() + " " + number();
        }
        else
        {
            return department() + " " + number();
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns the course's department abbreviation combined with
     * the course's number, followed by its name (e.g., "CS 1705: Intro to
     * Data Structures").
     * @return the full course number and name
     */
    public String deptNumberAndName()
    {
        return deptNumber() + ": " + name();
    }


    // ----------------------------------------------------------
    /**
     * Get a short (no longer than 60 characters) description of this coursse,
     * which currently returns {@link #deptNumber()}.
     * @return the description
     */
    public String userPresentableDescription()
    {
        return deptNumber();
    }


    // ----------------------------------------------------------
    @Override
    public boolean accessibleByUser(User user)
    {
        for (CourseOffering offering : offerings())
        {
            if (offering.accessibleByUser(user))
            {
                return true;
            }
        }
        return false;
    }


    // ----------------------------------------------------------
    public Object validateNumber( Object value )
    {
        if ( value == null )
        {
            throw new ValidationException(
                "Please provide a course number." );
        }
        return value;
    }

    // ----------------------------------------------------------
    @Override
    public void mightDelete()
    {
        log.debug("mightDelete()");
        if (isNewObject()) return;
        if (offerings().count() > 0)
        {
            log.debug("mightDelete(): course has offerings");
            throw new ValidationException("You may not delete a course "
                + "offering that has course offerings.");
        }
        super.mightDelete();
    }


    // ----------------------------------------------------------
    @Override
    public boolean canDelete()
    {
        boolean result = (editingContext() == null
            || offerings().count() == 0);
        log.debug("canDelete() = " + result);
        return result;
    }


    // ----------------------------------------------------------
    public static Course findObjectWithApiId(
            EOEditingContext ec, String repoId)
        throws EOUtilities.MoreThanOneException
    {
        String[] parts = repoId.split("\\.");

        EOQualifier qualifier;

        if (parts.length == 3)
        {
            String institution = parts[0];
            String deptAbbrev = parts[1];
            int courseNumber;

            try
            {
                courseNumber = Integer.parseInt(parts[2]);
            }
            catch (NumberFormatException e)
            {
                return null;
            }

            qualifier = department.dot(Department.abbreviation).is(deptAbbrev)
                .and(number.is(courseNumber));

            NSArray<Course> courses = objectsMatchingQualifier(ec, qualifier);

            for (Course course : courses)
            {
                if (institution.equals(
                        course.department().institution().subdirName()))
                {
                    return course;
                }
            }

            return null;
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public String apiId()
    {
        try
        {
            return department().institution().subdirName() + "."
                + department().abbreviation() + "." + number();
        }
        catch (Exception e)
        {
            return id().toString();
        }
    }


    // ----------------------------------------------------------
    public void initializeRepositoryContents(File location) throws IOException
    {
        File readme = new File(location, "README.txt");
        PrintWriter writer = new PrintWriter(readme);

        writer.print(
          "This Git repository has been created to manage shared files\n"
        + "for the course \"" + department().abbreviation() + " "
        + number() + "\" (" + name() + ").\n\n");
        writer.print(
          "This repository can be used to store files that are relevant to a\n"
        + "particular course, such as reference tests, assignment write-ups,\n"
        + "and any other artifacts that should be shared among the staff of\n"
        + "the course.\n\n"
        + "You can delete this readme file if you like; it was provided merely\n"
        + "for informational purposes.");

        writer.close();
    }


    // ----------------------------------------------------------
    public static NSArray<Course> repositoriesPresentedToUser(User user,
            EOEditingContext ec)
    {
        NSMutableSet<Course> courses = new NSMutableSet<Course>();

        for (CourseOffering co : user.teaching())
        {
            courses.addObject(co.course());
        }

        for (CourseOffering co : user.graderFor())
        {
            courses.addObject(co.course());
        }

        return courses.allObjects();
    }


    // ----------------------------------------------------------
    public boolean userCanAccessRepository(User user)
    {
        for (CourseOffering offering : offerings())
        {
            if (offering.isStaff(user))
            {
                return true;
            }
        }

        return false;
    }
}
