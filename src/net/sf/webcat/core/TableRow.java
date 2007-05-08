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

import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * A WOGenericContainer that represents a Web-CAT alternating-color table
 * row.
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class TableRow
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new WCTableRow object.
     * 
     * @param context The page's context
     */
    public TableRow( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public  int     index;
    public  Boolean showError   = Boolean.FALSE;
    public  Boolean showCaution = Boolean.FALSE;
    public  Boolean increment   = Boolean.FALSE;
    public  String  id;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse( WOResponse response, WOContext context )
    {
        super.appendToResponse( response, context );
        if ( increment )
        {
            index++;
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns the RGB color to use for the current row.
     * 
     * @return The color as an #RRGGBB string
     */
    public String cssClass()
    {
        int tag = index % 2;
        if ( Boolean.TRUE.equals( showCaution ) ) tag += 2;
        if ( Boolean.TRUE.equals( showError   ) ) tag += 4;
        return cssTag[tag];
    }


    // ----------------------------------------------------------
    public boolean isStateless()
    {
        return true;
    }


    // ----------------------------------------------------------
    public boolean synchronizesVariablesWithBindings()
    {
        return true;
    }


    // ----------------------------------------------------------
    public void reset()
    {
        showError   = Boolean.FALSE;
        showCaution = Boolean.FALSE;
        increment   = Boolean.FALSE;
        id          = null;
    }


    //~ Instance/static variables .............................................

    // Remember, the first row is indexed as 0, so index numbers that
    // are even should really be labeled with "odd" css styles and vice
    // versa.
    static final String cssTag[] = new String[] {
        "o",
        "e",
        "oc",
        "ec",
        "oe",
        "ee",
        "oe",
        "ee"
    };
    
    static Logger log = Logger.getLogger( TableRow.class );
}
