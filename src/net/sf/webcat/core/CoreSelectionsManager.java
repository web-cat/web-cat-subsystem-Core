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

import com.webobjects.eocontrol.*;

//-------------------------------------------------------------------------
/**
 *  An {@link CachingEOManager} specialized for managing a
 *  {@link CoreSelections} object.
 *
 *  @author  Stephen Edwards
 *  @version $Id$
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


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Retrieve the entity pointed to by the <code>course</code>
     * relationship.
     * @return the entity in the relationship
     */
    public Course course()
    {
        return (Course)valueForKey(CoreSelections.COURSE_KEY);
    }


    // ----------------------------------------------------------
    /**
     * Set the entity pointed to by the <code>course</code>
     * relationship.
     * @param value The new course
     */
    public void setCourseRelationship( Course value )
    {
        addObjectToBothSidesOfRelationshipWithKey(
            value, CoreSelections.COURSE_KEY);
    }

    // ----------------------------------------------------------
    /**
     * Retrieve the entity pointed to by the <code>courseOffering</code>
     * relationship.
     * @return the entity in the relationship
     */
    public CourseOffering courseOffering()
    {
        return (CourseOffering)valueForKey(CoreSelections.COURSE_OFFERING_KEY);
    }


    // ----------------------------------------------------------
    /**
     * Set the entity pointed to by the <code>courseOffering</code>
     * relationship.
     * @param value The new course offering
     */
    public void setCourseOfferingRelationship( CourseOffering value )
    {
        addObjectToBothSidesOfRelationshipWithKey(
            value, CoreSelections.COURSE_OFFERING_KEY);
    }
}
