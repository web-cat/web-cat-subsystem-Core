package net.sf.webcat.core;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import er.extensions.eof.ERXQ;

public class CoreNavigatorObjects
{
    // ----------------------------------------------------------
    public static class AllSemesters
        implements INavigatorObject
    {
        // ----------------------------------------------------------
        public AllSemesters(EOEditingContext ec)
        {
            semesters = EOUtilities.objectsForEntityNamed(ec,
                    Semester.ENTITY_NAME);
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
}
