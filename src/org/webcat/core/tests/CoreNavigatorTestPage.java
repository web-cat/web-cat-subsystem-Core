/*==========================================================================*\
|  $Id: CoreNavigatorTestPage.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
|*-------------------------------------------------------------------------*|
|  Copyright (C) 2006-2009 Virginia Tech
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

package org.webcat.core.tests;

import org.webcat.core.*;
import com.webobjects.appserver.*;
import org.webcat.core.WCCourseComponent;

//-------------------------------------------------------------------------
/**
 * A test page for testing the core navigator pop-up component.
 *
 * @author Stephen Edwards
 * @author  latest changes by: $Author: aallowat $
 * @version $Revision: 1.1 $ $Date: 2010/05/11 14:51:59 $
 */
public class CoreNavigatorTestPage
    extends WCCourseComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new page.
     *
     * @param context The context to use
     */
    public CoreNavigatorTestPage(WOContext context)
    {
        super(context);
    }


   // ----------------------------------------------------------
    public boolean forceNavigatorSelection()
    {
        return true;
    }
}
