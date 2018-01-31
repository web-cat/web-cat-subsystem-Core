/*==========================================================================*\
 |  $Id: TypographyDemoPage.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

//-------------------------------------------------------------------------
/**
* A page for development use that has samples of the main typographical
* elements so they can all be viewed together when developing and/or
* debugging new themes.
*
*  @author Stephen Edwards
*  @version $Id: TypographyDemoPage.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
*/
public class TypographyDemoPage
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new TBDPage object.
     *
     * @param context The context to use
     */
    public TypographyDemoPage( WOContext context )
    {
        super( context );
    }
}
