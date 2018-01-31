/*==========================================================================*\
 |  $Id: CoreSelectionsManager.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

package org.webcat.core;

import org.webcat.core.CachingEOManager;
import org.webcat.core.CoreSelections;
import org.webcat.core.Course;
import org.webcat.core.CourseOffering;
import org.webcat.core.Semester;
import org.webcat.core.User;
import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXValueUtilities;

//-------------------------------------------------------------------------
/**
 *  An {@link CachingEOManager} specialized for managing a
 *  {@link CoreSelections} object.
 *
 *  @author  Stephen Edwards
 *  @author  latest changes by: $Author: aallowat $
 *  @version $Revision: 1.1 $ $Date: 2010/05/11 14:51:55 $
 */
public class CoreSelectionsManager
    extends CachingEOManager
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new manager for the given CoreSelections object (which
     * presumably lives in the session's editing context).
     * @param selections the object to manage
     * @param manager the (probably shared) editing context manager to use
     * for independent saving of the given eo
     */
    public CoreSelectionsManager(CoreSelections selections, ECManager manager)
    {
        super(selections, manager);
    }


    //~ Constants .............................................................

    public static final String SEMESTER_KEY = "semester";
    public static final String INCLUDE_WHAT_IM_TEACHING_KEY
        = "includeWhatImTeaching";
    public static final String INCLUDE_ADMIN_ACCESS_KEY = "includeAdminAccess";


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Retrieve the entity pointed to by the <code>course</code>
     * relationship.
     * @return the entity in the relationship
     */
    public Course course()
    {
        return (Course)handleQueryWithUnboundKey(CoreSelections.COURSE_KEY);
    }


    // ----------------------------------------------------------
    /**
     * Set the entity pointed to by the <code>course</code>
     * relationship.
     * @param value The new course
     */
    public void setCourseRelationship( Course value )
    {
        if (value == null)
        {
            Course old = course();
            if (old != null)
            {
                removeObjectFromBothSidesOfRelationshipWithKey(
                    old, CoreSelections.COURSE_KEY);
            }
        }
        else
        {
            addObjectToBothSidesOfRelationshipWithKey(
                value, CoreSelections.COURSE_KEY);
        }
    }

    // ----------------------------------------------------------
    /**
     * Retrieve the entity pointed to by the <code>courseOffering</code>
     * relationship.
     * @return the entity in the relationship
     */
    public CourseOffering courseOffering()
    {
        return (CourseOffering)handleQueryWithUnboundKey(
            CoreSelections.COURSE_OFFERING_KEY);
    }


    // ----------------------------------------------------------
    /**
     * Set the entity pointed to by the <code>courseOffering</code>
     * relationship.
     * @param value The new course offering
     */
    public void setCourseOfferingRelationship( CourseOffering value )
    {
        if (value == null)
        {
            CourseOffering old = courseOffering();
            if (old != null)
            {
                removeObjectFromBothSidesOfRelationshipWithKey(
                    old, CoreSelections.COURSE_OFFERING_KEY);
            }
        }
        else
        {
            addObjectToBothSidesOfRelationshipWithKey(
                value, CoreSelections.COURSE_OFFERING_KEY);
        }
    }


    // ----------------------------------------------------------
    public Semester semester()
    {
        if (semester == null)
        {
            User user =
                (User)handleQueryWithUnboundKey(CoreSelections.USER_KEY);
            Object semesterPref =
                user.preferences().valueForKey(SEMESTER_KEY);
            if (semesterPref != null)
            {
                semester = Semester.forId(user.editingContext(),
                    ERXValueUtilities.intValue(semesterPref));
            }
        }
        return semester;
    }


    // ----------------------------------------------------------
    public void setSemester(Semester semester)
    {
        this.semester = semester;
        User user = (User)valueForKey(CoreSelections.USER_KEY);
        user.preferences().takeValueForKey(
            semester == null ? ERXConstant.ZeroInteger : semester.id(),
            SEMESTER_KEY);
        user.savePreferences();
    }


    // ----------------------------------------------------------
    public boolean includeWhatImTeaching()
    {
        if (includeWhatImTeaching == null)
        {
            User user = (User)valueForKey(CoreSelections.USER_KEY);
            includeWhatImTeaching = Boolean.valueOf(
                ERXValueUtilities.booleanValueWithDefault(
                    user.preferences().valueForKey(
                        INCLUDE_WHAT_IM_TEACHING_KEY),
                    true));
        }
        return includeWhatImTeaching.booleanValue();
    }


    // ----------------------------------------------------------
    public void setIncludeWhatImTeaching(boolean value)
    {
        includeWhatImTeaching = Boolean.valueOf(value);
        User user = (User)valueForKey(CoreSelections.USER_KEY);
        user.preferences().takeValueForKey(
            includeWhatImTeaching,
            INCLUDE_WHAT_IM_TEACHING_KEY);
        user.savePreferences();
    }


    // ----------------------------------------------------------
    public boolean includeAdminAccess()
    {
        if (includeAdminAccess == null)
        {
            User user = (User)valueForKey(CoreSelections.USER_KEY);
            includeAdminAccess = Boolean.valueOf(
                ERXValueUtilities.booleanValueWithDefault(
                    user.preferences().valueForKey(
                        INCLUDE_ADMIN_ACCESS_KEY),
                    false));
        }
        return includeAdminAccess.booleanValue();
    }


    // ----------------------------------------------------------
    public void setIncludeAdminAccess(boolean value)
    {
        includeAdminAccess = Boolean.valueOf(value);
        User user = (User)valueForKey(CoreSelections.USER_KEY);
        user.preferences().takeValueForKey(
            includeAdminAccess,
            INCLUDE_ADMIN_ACCESS_KEY);
        user.savePreferences();
    }


    //~ Instance/static variables .............................................

    private Semester semester;
    private Boolean includeWhatImTeaching;
    private Boolean includeAdminAccess;
}
