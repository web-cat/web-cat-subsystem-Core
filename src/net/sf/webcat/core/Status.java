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

// -------------------------------------------------------------------------
/**
 *  Defines the constants and static methods needed to translate byte-sized
 *  status codes into image URLs for the corresponding icons.
 *
 *  @author  stedwar2
 *  @version Jan 28, 2005
 */
public class Status
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * This constructor is hidden from the outside so that no instances of
     * this class can be created.  It is used purely for its static methods
     * and constant definitions.
     */
    private Status()
    {
        // Nothing to do
    }


    //~ Constants .............................................................

    static public final byte ERROR       = 0;
    static public final byte UNFINISHED  = 1;
    static public final byte CAUTION     = 2;
    static public final byte TO_DO       = 3;
    static public final byte CHECK       = 4;
    static public final byte NOT_READY   = 5;

    static public final String ERROR_URL       = "icons/exclaim.gif";
    static public final String UNFINISHED_URL  = "icons/partially.gif";
    static public final String CAUTION_URL     = "icons/caution.gif";
    static public final String TO_DO_URL       = "icons/todo.gif";
    static public final String CHECK_URL       = "icons/check.gif";
    static public final String NOT_READY_URL   = "icons/notready.gif";


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Get the icon URL that corresponds to the given status value.
     * 
     * @param status The status value to look up
     * @return       The corresponding image file URL
     */
    static public String statusURL( byte status )
    {
        return urlOf[status];
    }


    // ----------------------------------------------------------
    /**
     * Get the icon URL that corresponds to the given status value.
     * 
     * @param status The status value to look up
     * @return       The corresponding image file URL
     */
    static public String statusURL( Number status )
    {
        return statusURL( status.byteValue() );
    }


    //~ Instance/static variables .............................................

    static protected final String[] urlOf = new String[] {
        ERROR_URL,
        UNFINISHED_URL,
        CAUTION_URL,
        TO_DO_URL,
        CHECK_URL,
        NOT_READY_URL
    };
}
