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

// -------------------------------------------------------------------------
/**
 *  An interface for subsystem extensions to the session state.
 *
 *  @author  Stephen Edwards
 *  @version $Id$
 */
public interface SubsystemSessionState
{
    // ----------------------------------------------------------
    /**
     * Called by the session when the course selected in the session
     * changes.
     * @param course the new course selection (possibly null)
     */
    void selectCourse( Course course );


    // ----------------------------------------------------------
    /**
     * Called by the session when the course offering selected in the session
     * changes.
     * @param courseOffering the new course offering selection (possibly null)
     */
    void selectCourseOffering( CourseOffering courseOffering );


    // ----------------------------------------------------------
    /**
     * Called by the session when the local user stored in the session
     * changes.  This might change when an instructor or admin is
     * "impersonating" a student, for example.
     * @param user the new local user selection (possibly different from
     *        the session.primeUser)
     */
    void selectLocalUser( User user );
}
