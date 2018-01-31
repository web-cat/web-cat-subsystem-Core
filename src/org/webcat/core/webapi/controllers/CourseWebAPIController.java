package org.webcat.core.webapi.controllers;

import org.webcat.core.Course;
import org.webcat.core.webapi.KeyValueExtractor;
import org.webcat.core.webapi.WebAPIController;
import org.webcat.core.webapi.WebAPIError;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

//-------------------------------------------------------------------------
/**
 * A sample web API controller for Course objects that should be replaced with
 * something better. This is just for illustration purposes.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2012/06/22 16:23:17 $
 */
public class CourseWebAPIController extends WebAPIController
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * An example of handling a "GET .../Course" request that lists all of the
     * courses that the requesting user has access to.
     *
     * @return the array of courses
     */
    public NSArray<Course> indexAction()
    {
        return Course.accessibleBy(user()).filtered(
                Course.allObjects(editingContext()));
    }


    // ----------------------------------------------------------
    /**
     * An example of handling a "GET .../Course/id" request that returns some
     * details about the requested course.
     *
     * @return a dictionary containing properties of the course
     */
    public NSDictionary<String, Object> showAction(Course course)
    {
        return new KeyValueExtractor()
            .with("deptNumberAndName")
            .with("offerings")
            .extract(course);
    }


    // ----------------------------------------------------------
    /**
     * An example of handling a "POST .../Course" creation request.
     *
     * @return an error
     */
    public WebAPIError createAction()
    {
        return new WebAPIError("create not supported");
    }


    // ----------------------------------------------------------
    /**
     * An example of handling a "PUT .../Course/id" update request.
     *
     * @param course the course to update
     * @return an error
     */
    public WebAPIError updateAction(Course course)
    {
        return new WebAPIError(39, "cannot update " + course.deptNumberAndName());
    }


    // ----------------------------------------------------------
    /**
     * An example of handling a "DELETE /Course/id" deletion request.
     *
     * @param course the course to delete
     * @return an error
     */
    public WebAPIError deleteAction(Course course)
    {
        return new WebAPIError(78);
    }


    // ----------------------------------------------------------
    /**
     * A custom collection action for the URL "/Course/test".
     *
     * @return the result
     */
    public String testAction()
    {
        return "test";
    }


    // ----------------------------------------------------------
    /**
     * A custom object action for the URL "/Course/id/test".
     *
     * @param course the course
     * @return the result
     */
    public String testAction(Course course)
    {
        return "test " + course.deptNumberAndName();
    }


    // ----------------------------------------------------------
    /**
     * A custom object action for the URL "/Course/id/void". Void actions can
     * be used for rare situations where an action should perform some
     * operation but for which there is no result (not even an error).
     *
     * @param course the course
     */
    public void voidAction(Course course)
    {
        System.out.println("dfsdfsdf");
    }
}
