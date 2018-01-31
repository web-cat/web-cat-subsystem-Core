/*==========================================================================*\
 |  $Id: WCPageRestorationErrorPage.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

import org.webcat.core.WCComponent;
import com.webobjects.appserver.*;

// -------------------------------------------------------------------------
/**
 *  A page to display backtracking errors to users (when a
 *  PageRestorationError occurs).  This is a replacement for the
 *  default error page displayed in this situation:
 *  {@link com.webobjects.woextensions.WOPageRestorationError}.
 *
 *  @author  stedwar2
 *  @version $Id: WCPageRestorationErrorPage.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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
