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

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

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


    // ----------------------------------------------------------
    /**
     * Retrieve the entity pointed to by the <code>courseOffering</code>
     * relationship.
     * @return the entity in the relationship
     */
    public net.sf.webcat.core.CourseOffering courseOffering()
    {
        try
        {
            CourseOffering result = super.courseOffering();
            if ( result != null )
                result.course();  // Force access of this object
            return result;
        }
        catch ( com.webobjects.eoaccess.EOObjectNotAvailableException e )
        {
            super.setCourseOfferingRelationship( null );
            return super.courseOffering();
        }
    }


// If you add instance variables to store property values you
// should add empty implementions of the Serialization methods
// to avoid unnecessary overhead (the properties will be
// serialized for you in the superclass).

//    // ----------------------------------------------------------
//    /**
//     * Serialize this object (an empty implementation, since the
//     * superclass handles this responsibility).
//     * @param out the stream to write to
//     */
//    private void writeObject( java.io.ObjectOutputStream out )
//        throws java.io.IOException
//    {
//    }
//
//
//    // ----------------------------------------------------------
//    /**
//     * Read in a serialized object (an empty implementation, since the
//     * superclass handles this responsibility).
//     * @param in the stream to read from
//     */
//    private void readObject( java.io.ObjectInputStream in )
//        throws java.io.IOException, java.lang.ClassNotFoundException
//    {
//    }
}
