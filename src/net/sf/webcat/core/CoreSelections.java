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
            super.setCourseRelationship( null );
            return super.course();
        }
    }


    public void setCourse( Course value )
    {
        log.debug("setCourse(" + value + ")");
        super.setCourse( value );
    }


    public void setCourseOffering( CourseOffering value )
    {
        log.debug("setCourseOffering(" + value + ")");
        super.setCourseOffering( value );
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
            super.setCourseOfferingRelationship( null );
            return super.courseOffering();
        }
    }


    // ----------------------------------------------------------
    /**
     * Use a separate editing context to save this object's data,
     * if possible.
     */
    public void save()
    {
        log.debug("save(): course = " + course() + ", offering = "
            + courseOffering());
        boolean usingFreshEC = (ecForPrefs == null);
        if (usingFreshEC)
        {
            ecForPrefs = Application.newPeerEditingContext();
        }
        ecForPrefs.lock();
        try
        {
            // Use a separate EC to store the changed preferences
            CoreSelections me = (CoreSelections)EOUtilities
                .localInstanceOfObject(ecForPrefs, this);
            // Transfer the course setting
            {
                Course course = course();
                if (course != null)
                {
                    course = (Course)EOUtilities
                        .localInstanceOfObject(ecForPrefs, course);
                }
                me.setCourseRelationship( course );
            }
            // Transfer the courseOffering setting
            {
                CourseOffering offering = courseOffering();
                if (offering != null)
                {
                    offering = (CourseOffering)EOUtilities
                        .localInstanceOfObject(ecForPrefs, offering);
                }
                me.setCourseOfferingRelationship( offering );
            }
            ecForPrefs.saveChanges();
            // Now refresh the session's copy of this object so that it loads
            // this saved preferences value
            editingContext().refreshObject( this );
            log.debug("save(): after refresh: course = " + course()
                + ", offering = " + courseOffering());
        }
        catch (Exception e)
        {
            // If there was an error saving ...
            try
            {
                // Try to unlock first, if possible
                try
                {
                    ecForPrefs.unlock();
                }
                catch (Exception eee)
                {
                    // nothing
                }
                // Try to clean up the broken editing context, if possible
                Application.releasePeerEditingContext(ecForPrefs);
            }
            catch (Exception ee)
            {
                // if there is an error, ignore it since we're not going to
                // use this ec any more anyway
            }
            ecForPrefs = null;
            if (!usingFreshEC)
            {
                save();
            }
        }
        finally
        {
            if (ecForPrefs != null)
            {
                ecForPrefs.unlock();
            }
        }
    }


    //~ Instance/static variables .............................................
    private EOEditingContext ecForPrefs;

    static Logger log = Logger.getLogger( CoreSelections.class );
}
