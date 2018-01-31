/*==========================================================================*\
 |  $Id: EntityUtils.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

package org.webcat.core;

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

//-------------------------------------------------------------------------
/**
 * Various utility functions used by the Reporter to deal with entities.
 *
 * @author Tony Allevato
 * @version $Id: EntityUtils.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public class EntityUtils
{
    // ----------------------------------------------------------
    /**
     * Static class; prevent instantiation.
     */
    private EntityUtils()
    {
        // Do nothing.
    }
    
    
    // ----------------------------------------------------------
    /**
     * Gets an array of sort orderings that sort entities of the given type
     * based on how they are displayed in the user interface.
     * 
     * @param entityName the name of the entity to sort
     * @return an array of sort orderings, or null if we do not sort entities
     *     of this type
     */
    public static NSArray<EOSortOrdering> sortOrderingsForEntityNamed(
            String entityName)
    {
        NSMutableArray<EOSortOrdering> orderings =
            new NSMutableArray<EOSortOrdering>();
        
        if ("Assignment".equals(entityName))
        {
            orderings.addObject(new EOSortOrdering("name",
                    EOSortOrdering.CompareCaseInsensitiveAscending));
        }
        else if ("AssignmentOffering".equals(entityName))
        {
            orderings.addObject(new EOSortOrdering(
                    "courseOffering.course.department.abbreviation",
                    EOSortOrdering.CompareCaseInsensitiveAscending));
            orderings.addObject(new EOSortOrdering(
                    "courseOffering.course.number",
                    EOSortOrdering.CompareAscending));
            orderings.addObject(new EOSortOrdering("courseOffering.label",
                    EOSortOrdering.CompareCaseInsensitiveAscending));
            orderings.addObject(new EOSortOrdering("courseOffering.crn",
                    EOSortOrdering.CompareAscending));
            orderings.addObject(new EOSortOrdering("assignment.name",
                    EOSortOrdering.CompareCaseInsensitiveAscending));
        }
        else if ("Course".equals(entityName))
        {
            orderings.addObject(new EOSortOrdering("department.abbreviation",
                    EOSortOrdering.CompareCaseInsensitiveAscending));
            orderings.addObject(new EOSortOrdering("number",
                    EOSortOrdering.CompareAscending));
        }
        else if ("CourseOffering".equals(entityName))
        {
            orderings.addObject(new EOSortOrdering(
                    "course.department.abbreviation",
                    EOSortOrdering.CompareCaseInsensitiveAscending));
            orderings.addObject(new EOSortOrdering("course.number",
                    EOSortOrdering.CompareAscending));
            orderings.addObject(new EOSortOrdering("label",
                    EOSortOrdering.CompareCaseInsensitiveAscending));
            orderings.addObject(new EOSortOrdering("crn",
                    EOSortOrdering.CompareAscending));
        }
        else if ("Department".equals(entityName))
        {
            orderings.addObject(new EOSortOrdering("abbreviation",
                    EOSortOrdering.CompareCaseInsensitiveAscending));
        }
        else if ("Semester".equals(entityName))
        {
            orderings.addObject(new EOSortOrdering("season",
                    EOSortOrdering.CompareAscending));
            orderings.addObject(new EOSortOrdering("year",
                    EOSortOrdering.CompareAscending));
        }
        else if ("User".equals(entityName))
        {
            orderings.addObject(new EOSortOrdering("lastName",
                    EOSortOrdering.CompareCaseInsensitiveAscending));
            orderings.addObject(new EOSortOrdering("firstName",
                    EOSortOrdering.CompareCaseInsensitiveAscending));
        }
        
        return orderings.isEmpty() ? null : orderings;
    }
}
