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

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

//-------------------------------------------------------------------------
/**
* This class is a base for WCComponent that extracts out the error
* message handling features.
*
* @author Stephen Edwards
* @version $Id$
*/
public class WCComponentWithErrorMessages
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     * 
     * @param context The page's context
     */
    public WCComponentWithErrorMessages( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public NSMutableDictionary  errors;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Determines whether this page has any error messages.
     * @return True if any error messages are present
     */
    public boolean hasErrors()
    {
        return errors != null && errors.count() > 0;
    }


    // ----------------------------------------------------------
    /**
     * Record an error message for this page.
     * @param message the error message
     */
    public void errorMessage( String message )
    {
        errorMessage( message, message );
    }


    // ----------------------------------------------------------
    /**
     * Record an error message for this page.
     * @param message the error message
     * @param id a unique id used to distinguish this message from others
     */
    public void errorMessage( String message, String id )
    {
        if ( errors == null )
        {
            errors = new NSMutableDictionary();
        }
        errors.setObjectForKey( message, id );
    }


    // ----------------------------------------------------------
    /**
     * Remove all recorded error message for this page.
     */
    public void clearErrors()
    {
        if ( errors != null )
        {
            errors.removeAllObjects();
        }
    }


    // ----------------------------------------------------------
    /**
     * Remove a specific error message from this page.
     * @param id the unique identifier for the message
     */
    public void clearError( String id )
    {
        if ( errors != null )
        {
            errors.removeObjectForKey( id );
        }
    }
}
