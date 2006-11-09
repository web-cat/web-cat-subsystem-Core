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
 *  A page to display backtracking errors to users (when a
 *  PageRestorationError occurs).  This is a replacement for the
 *  default error page displayed in this situation:
 *  {@link com.webobjects.woextensions.WOPageRestorationError}.
 *
 *  @author  stedwar2
 *  @version $Id$
 */
public class WCPageRestorationErrorPage
    extends WCComponent
{

    // ----------------------------------------------------------
    /**
     * Creates a new page object.
     * 
     * @param context The context to use
     */
    public WCPageRestorationErrorPage( WOContext context )
    {
        super( context );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse( WOResponse response, WOContext context )
    {
        wcSession().tabs.selectDefault();
        super.appendToResponse( response, context );
    }
}
