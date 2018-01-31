/*==========================================================================*\
 |  $Id: CoreNavigatorObjects.java,v 1.3 2012/01/19 16:29:52 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2010-2012 Virginia Tech
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

import org.webcat.core.CourseOffering;
import org.webcat.core.INavigatorObject;
import org.webcat.core.Semester;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

//--------------------------------------------------------------------------
/**
 * Provides inner classes that represent selections in navigator controls.
 *
 * @author  Tony Allevato
 * @author  Last changed by: $Author: stedwar2 $
 * @version $Revision: 1.3 $ $Date: 2012/01/19 16:29:52 $
 */
public class CoreNavigatorObjects
{
    // ----------------------------------------------------------
    /**
     * This class is a utility class only that provides static declarations.
     * No instances should ever be created.
     */
    private CoreNavigatorObjects()
    {
        // no initialization necessary
    }


    // ----------------------------------------------------------
    public static class AllSemesters
        implements INavigatorObject
    {
        // ----------------------------------------------------------
        public AllSemesters(EOEditingContext ec)
        {
            semesters = Semester.allObjects(ec);
        }


        // ----------------------------------------------------------
        public NSArray<?> representedObjects()
        {
            return semesters;
        }


        // ----------------------------------------------------------
        public String toString()
        {
            return "All";
        }


        // ----------------------------------------------------------
        public boolean equals(Object obj)
        {
            if (obj instanceof AllSemesters)
            {
                AllSemesters o = (AllSemesters) obj;
                return semesters.equals(o.semesters);
            }
            else
            {
                return false;
            }
        }


        private NSArray<Semester> semesters;
    }


    // ----------------------------------------------------------
    public static class SingleSemester
        implements INavigatorObject
    {
        // ----------------------------------------------------------
        public SingleSemester(Semester semester)
        {
            this.semester = semester;
        }


        // ----------------------------------------------------------
        public NSArray<?> representedObjects()
        {
            return new NSMutableArray<Semester>(semester);
        }


        // ----------------------------------------------------------
        public String toString()
        {
            return (semester == null)
                ? "null"
                : semester.toString();
        }


        // ----------------------------------------------------------
        public boolean equals(Object obj)
        {
            if (obj instanceof SingleSemester)
            {
                SingleSemester o = (SingleSemester) obj;
                return semester.equals(o.semester);
            }
            else
            {
                return false;
            }
        }


        private Semester semester;
    }


    // ----------------------------------------------------------
    public static class CourseOfferingSet
        implements INavigatorObject
    {
        // ----------------------------------------------------------
        /**
         * Create an "all" object for a set of course offerings.  Assumes
         * that all the course offerings in the set are offerings of
         * the same course.
         */
        public CourseOfferingSet(NSArray<CourseOffering> offerings)
        {
            courseOfferings = offerings;
        }


        // ----------------------------------------------------------
        public NSArray<?> representedObjects()
        {
            return courseOfferings;
        }


        // ----------------------------------------------------------
        public String toString()
        {
            if (courseOfferings == null || courseOfferings.count() == 0)
            {
                return "null";
            }
            else
            {
                return courseOfferings.objectAtIndex(0).course().toString()
                    + " (All)";
            }
        }


        // ----------------------------------------------------------
        public boolean equals(Object obj)
        {
            if (obj instanceof CourseOfferingSet)
            {
                CourseOfferingSet o = (CourseOfferingSet) obj;

                return courseOfferings.equals(o.courseOfferings);
            }
            else
            {
                return false;
            }
        }


        private NSArray<CourseOffering> courseOfferings;
    }


    // ----------------------------------------------------------
    public static class SingleCourseOffering
        implements INavigatorObject
    {
        // ----------------------------------------------------------
        public SingleCourseOffering(CourseOffering offering)
        {
            this.offering = offering;
        }


        // ----------------------------------------------------------
        public NSArray<?> representedObjects()
        {
            return new NSMutableArray<CourseOffering>(offering);
        }


        // ----------------------------------------------------------
        public String toString()
        {
            return (offering == null)
                ? "null"
                : offering.toString();
        }


        // ----------------------------------------------------------
        public boolean equals(Object obj)
        {
            if (obj instanceof SingleCourseOffering)
            {
                SingleCourseOffering o = (SingleCourseOffering) obj;

                return offering.equals(o.offering);
            }
            else
            {
                return false;
            }
        }


        private CourseOffering offering;
    }


    //~ Static/instance variables .............................................

    public static final INavigatorObject FILTER_PLACEHOLDER =
        new INavigatorObject()
    {
        // --------------------------------------------------
        public NSArray<?> representedObjects()
        {
            return null;
        }
    };
}
