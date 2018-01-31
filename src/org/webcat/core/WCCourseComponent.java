/*==========================================================================*\
 |  $Id: WCCourseComponent.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2010 Virginia Tech
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

import com.webobjects.appserver.*;
import com.webobjects.foundation.NSArray;
import org.webcat.core.CoreSelectionsManager;
import org.webcat.core.Course;
import org.webcat.core.CourseOffering;
import org.webcat.core.EOManager;
import org.webcat.core.Semester;
import org.webcat.core.WCComponent;
import org.webcat.core.WCCourseComponent;
import org.apache.log4j.*;

//-------------------------------------------------------------------------
/**
 * This specialized subclass of WCComponent represents pages that have
 * a notion of a currently-selected course offering and/or course.
 *
 * @author Stephen Edwards
 * @author  latest changes by: $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2010/05/11 14:51:55 $
 */
public class WCCourseComponent
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The page's context
     */
    public WCCourseComponent( WOContext context )
    {
        super( context );
    }

    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Grab user's current selections when waking, if necessary.
     */
    @Override
    public void awake()
    {
        if (log.isDebugEnabled())
        {
            log.debug("awake(): begin " + getClass().getName());
        }
        super.awake();
        coreSelections();
        if (log.isDebugEnabled())
        {
            log.debug("awake(): end " + getClass().getName());
        }
    }


    // ----------------------------------------------------------
    public final void appendToResponse(WOResponse response, WOContext context)
    {
        // TODO make this method final and adjust all the other pages

        boolean force = forceNavigatorSelection();

        if (!force)
        {
            beforeAppendToResponse(response, context);
        }

        super.appendToResponse(response, context);

        if (!force)
        {
            afterAppendToResponse(response, context);
        }
    }


    // ----------------------------------------------------------
    protected void beforeAppendToResponse(WOResponse response, WOContext context)
    {
        // Overridden by subclasses.
    }


    // ----------------------------------------------------------
    protected void afterAppendToResponse(WOResponse response, WOContext context)
    {
        // Overridden by subclasses.
    }


    // ----------------------------------------------------------
    /**
     * Access the user's current core selections.
     * @return the core selections manager for this page
     */
    public CoreSelectionsManager coreSelections()
    {
        if (csm == null)
        {
            Object inheritedCsm = transientState().valueForKey( CSM_KEY );
            if (inheritedCsm == null)
            {
                if (user() != null)
                {
                    csm = new CoreSelectionsManager(
                        user().getMyCoreSelections(), ecManager());
                }
                // else: How is it possible to get here !?!?
            }
            else
            {
                csm = (CoreSelectionsManager)
                    ((CoreSelectionsManager)inheritedCsm).clone();
            }
        }
        return csm;
    }


    // ----------------------------------------------------------
    @Override
    public WOComponent pageWithName( String name )
    {
        if (csm != null)
        {
            transientState().takeValueForKey( csm, CSM_KEY );
        }
        WOComponent result = super.pageWithName( name );
        return result;
    }


    // ----------------------------------------------------------
    /**
     * This method determines whether any embedded navigator will
     * automatically pop up to force a selection and page reload.
     * The default implementation simply returns false, but is designed
     * to be overridden in subclasses.
     * @return True if the navigator should start out by opening automatically.
     */
    public boolean forceNavigatorSelection()
    {
        boolean result = false;

        // Check for required semester
        if (!allowsAllSemesters()
            && coreSelections().semester() == null)
        {
            // Guess the most recent semester
            Semester best = bestMatchingSemester();
            if (best == null)
            {
                result = true;
            }
            else
            {
                coreSelections().setSemester(best);
            }
        }

        // Check for required course
        if (!allowsAllOfferingsForCourse()
            && coreSelections().courseOffering() == null)
        {
            // Try to guess a course offering from the course
            CourseOffering bestOffering = bestMatchingCourseOffering();
            if (bestOffering != null)
            {
                coreSelections().setCourseOfferingRelationship(
                    bestOffering);
            }
            else
            {
                result = true;
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * This method guesses which semester is most appropriate, when
     * the user has chosen to see "all" semesters but the current page
     * requires only one.
     * @return The semester that seems best to use
     */
    protected Semester bestMatchingSemester()
    {
        NSArray<Semester> semesters =
            Semester.allObjectsOrderedByStartDate(localContext());
        if (semesters != null && semesters.count() > 0)
        {
            return semesters.objectAtIndex(0);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * This method guesses the best appropriate course offering to use,
     * when "all" of a given course is selected but a page requires a
     * specific course offering.
     * @return The course offering that seems like the best choice, or null
     * if none makes sense.
     */
    protected CourseOffering bestMatchingCourseOffering()
    {
        CourseOffering bestOffering = coreSelections().courseOffering();
        if (bestOffering == null)
        {
            Course course = coreSelections().course();
            if (course != null)
            {
                for (CourseOffering offering : user().enrolledIn())
                {
                    if (offering.course() == course)
                    {
                        bestOffering = offering;
                        break;
                    }
                }
                if (bestOffering == null)
                {
                    for (CourseOffering offering : user().teaching())
                    {
                        if (offering.course() == course)
                        {
                            bestOffering = offering;
                            break;
                        }
                    }
                }
                if (bestOffering == null)
                {
                    for (CourseOffering offering : user().graderFor())
                    {
                        if (offering.course() == course)
                        {
                            bestOffering = offering;
                            break;
                        }
                    }
                }
            }
        }
        return bestOffering;
    }


    // ----------------------------------------------------------
    /**
     * This method determines whether any embedded navigator will
     * allow users to select "all" offerings for a course.
     * The default implementation returns true, but is designed
     * to be overridden in subclasses.
     * @return True if the navigator should allow selection of all courses.
     */
    public boolean allowsAllOfferingsForCourse()
    {
        return true;
    }


    // ----------------------------------------------------------
    /**
     * This method determines whether any embedded navigator will
     * allow users to select "all" semesters.
     * The default implementation returns true, but is designed
     * to be overridden in subclasses.
     * @return True if the navigator should allow viewing of all semesters.
     */
    public boolean allowsAllSemesters()
    {
        return true;
    }


    // ----------------------------------------------------------
    public void flushNavigatorDerivedData()
    {
        // Nothing to do
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    protected EOManager.ECManager ecManager()
    {
        EOManager.ECManager result = (EOManager.ECManager)
            transientState().valueForKey(ECMANAGER_KEY);
        if (result == null)
        {
            result = new EOManager.ECManager();
            transientState().takeValueForKey(result, ECMANAGER_KEY);
        }
        return result;
    }


    //~ Instance/static variables .............................................

    private CoreSelectionsManager csm;
    private static final String CSM_KEY =
        CoreSelectionsManager.class.getName();
    private static final String ECMANAGER_KEY =
        EOManager.ECManager.class.getName();

    static Logger log = Logger.getLogger( WCCourseComponent.class );
}
