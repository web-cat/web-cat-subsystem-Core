/*==========================================================================*\
 |  $Id$
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

import com.webobjects.appserver.*;
import er.extensions.foundation.ERXValueUtilities;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * A component that represents a visual "module" displayed on a page,
 * and which will be rendered with the <code>.module</code> CSS class.
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class WCPageModule
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The page's context
     */
    public WCPageModule(WOContext context)
    {
        super(context);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
/*    public boolean isStateless()
    {
        return true;
    }*/


    // ----------------------------------------------------------
    public boolean synchronizesVariablesWithBindings()
    {
        return false;
    }


    // ----------------------------------------------------------
    public boolean hasTitle()
    {
        return hasBinding("title");
    }


    // ----------------------------------------------------------
    public String title()
    {
        return (String)valueForBinding("title");
    }


    // ----------------------------------------------------------
    public String id()
    {
        return (String)valueForBinding("id");
    }


    // ----------------------------------------------------------
    public boolean showsLoadingMessageOnRefresh()
    {
        return ERXValueUtilities.booleanValueWithDefault(
                valueForBinding("showsLoadingMessageOnRefresh"), true);
    }


    // ----------------------------------------------------------
    public boolean isCollapsible()
    {
        return ERXValueUtilities.booleanValueWithDefault(
            valueForBinding("collapsible"), true);
    }


    // ----------------------------------------------------------
    public boolean startOpen()
    {
        return ERXValueUtilities.booleanValueWithDefault(
            valueForBinding("open"), true);
    }
}
