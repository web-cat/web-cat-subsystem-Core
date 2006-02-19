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

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import org.apache.log4j.*;

// -------------------------------------------------------------------------
/**
 *  This is a specialized editing context subclass that is used to track
 *  down an obscure WO bug.
 *
 *  @author  Stephen Edwards
 *  @version $Id$
 */
public class WOEC
    extends LockErrorScreamerEditingContext
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new WOEC object.
     */
    public WOEC()
    {
        super();
    }


    // ----------------------------------------------------------
    /**
     * Creates a new WOEC object.
     * @param os the parent object store
     */
    public WOEC( EOObjectStore os )
    {
        super( os );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void insertObject( EOEnterpriseObject arg0 )
    {
        super.insertObject( arg0 );
        if ( insertedObjects.indexOfIdenticalObject( arg0 )
             == NSArray.NotFound )
            insertedObjects.addObject( arg0 );
    }


    // ----------------------------------------------------------
    public void deleteObject( EOEnterpriseObject arg0 )
    {
        super.deleteObject( arg0 );
        insertedObjects.removeObject( arg0 );
    }


    // ----------------------------------------------------------
    public void deleteObjects( NSArray arg0 )
    {
        super.deleteObjects( arg0 );
        insertedObjects.removeObjectsInArray( arg0 );
    }


    // ----------------------------------------------------------
    public void saveChanges()
    {
        NSArray insertedObjectsNow = insertedObjects();
        if ( insertedObjects.count() != insertedObjectsNow.count() )
        {
            log.error( "saveChanges(): inserted object count mismatch" );
            log.error( "saveChanges(): inserted objects  = "
                       + insertedObjectsNow );
            log.error( "saveChanges(): tracked insertions = "
                            + insertedObjects );
            log.error( "saveChanges(): changed objects  = "
                            + updatedObjects() );
        }
        else
        {
            NSArray trackedInsertions = insertedObjects.immutableClone();
            insertedObjects.removeObjectsInArray( insertedObjectsNow );
            if ( insertedObjects.count() > 0 )
            {
                log.error( "saveChanges(): inserted object contents mismatch" );
                log.error( "saveChanges(): inserted objects  = "
                           + insertedObjectsNow );
                log.error( "saveChanges(): tracked insertions = "
                           + trackedInsertions );
                log.error( "saveChanges(): changed objects  = "
                           + updatedObjects() );
            }
        }
        super.saveChanges();
        insertedObjects.removeAllObjects();
    }


    // ----------------------------------------------------------
    public void revert()
    {
        super.revert();
        insertedObjects.removeAllObjects();
    }


    // ----------------------------------------------------------
    public void reset()
    {
        super.reset();
        insertedObjects.removeAllObjects();
    }


    // ----------------------------------------------------------
    public void forgetObject( EOEnterpriseObject arg0 )
    {
        super.forgetObject( arg0 );
        insertedObjects.removeObject( arg0 );
    }


    // ----------------------------------------------------------
    public void dispose()
    {
        insertedObjects.removeAllObjects();
        super.dispose();
    }


    // ----------------------------------------------------------
    public static class WOECFactory
        extends er.extensions.ERXEC.DefaultFactory
    {
        protected EOEditingContext _createEditingContext( EOObjectStore parent )
        {
            return new WOEC( parent == null
                             ? EOEditingContext.defaultParentObjectStore()
                             : parent );
        }
    }


    // ----------------------------------------------------------
    public static void installWOECFactory()
    {
        er.extensions.ERXEC.setFactory( new WOECFactory() );
    }


    //~ Instance/static variables .............................................

    private NSMutableArray insertedObjects = new NSMutableArray();

    static Logger log = Logger.getLogger( WOEC.class );
}
