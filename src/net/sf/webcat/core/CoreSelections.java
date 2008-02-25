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

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import org.apache.log4j.*;

// -------------------------------------------------------------------------
/**
 * This class represents persistent navigation choices a user has made
 * for entities in the Core subsystem.
 *
 * @author stedwar2
 * @version $Id$
 */
public class CoreSelections
    extends _CoreSelections
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new CoreSelections object.
     */
    public CoreSelections()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Retrieve the entity pointed to by the <code>course</code>
     * relationship.
     * @return the entity in the relationship
     */
    public Course course()
    {
        try
        {
            Course result =  super.course();
            if ( result != null )
                result.number();  // Force access of this object
            return result;
        }
        catch ( com.webobjects.eoaccess.EOObjectNotAvailableException e )
        {
            log.debug("course(): attempting to force null after " + e);
            if (log.isDebugEnabled())
            {
                // cut off debugging in base class to avoid recursive
                // calls to this method!
                Level oldLevel = log.getLevel();
                try
                {
                    log.setLevel( Level.OFF );
                    // Do NOT call setCourseRelationship, since it in turn
                    // calls course()!
                    super.setCourse( null );
                }
                finally
                {
                    log.setLevel( oldLevel );
                }
            }
            else
            {
                // Do NOT call setCourseRelationship, since it in turn calls
                // course()!
                super.setCourse( null );
            }
            return super.course();
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
        try
        {
            CourseOffering result = super.courseOffering();
            if ( result != null )
                result.course();  // Force access of this object
            log.debug("courseOffering() = " + result);
            return result;
        }
        catch ( com.webobjects.eoaccess.EOObjectNotAvailableException e )
        {
            log.debug("courseOffering(): attempting to force null after " + e);
            if (log.isDebugEnabled())
            {
                // cut off debugging in base class to avoid recursive
                // calls to this method!
                Level oldLevel = log.getLevel();
                try
                {
                    log.setLevel( Level.OFF );
                    // Do NOT call setCourseOfferingRelationship, since it in
                    // turn calls courseOffering()!
                    super.setCourseOffering( null );
                }
                finally
                {
                    log.setLevel( oldLevel );
                }
            }
            else
            {
                // Do NOT call setCourseOfferingRelationship, since it in
                // turn calls courseOffering()!
                super.setCourseOffering( null );
            }
            return super.courseOffering();
        }
    }


    //~ Instance/static variables .............................................
    static Logger log = Logger.getLogger( CoreSelections.class );
}
