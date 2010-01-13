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

package net.sf.webcat.core;

import com.webobjects.foundation.*;
import com.webobjects.foundation.NSValidation.*;
import com.webobjects.eocontrol.*;

// -------------------------------------------------------------------------
/**
 * Represents one course, which may be taught multiple times in different
 * semesters (represented by separate course offerings).
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class Course
    extends _Course
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
    public static final String iNSTITUTION_KEY  =
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
     * the course's number (e.g., "CS 1705").
     * @return the department abbreviation and the course number
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


}
