/*==========================================================================*\
 |  $Id: WCCollapsibleSection.java,v 1.2 2012/03/28 13:48:07 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXValueUtilities;

//-------------------------------------------------------------------------
/**
 *  A layout component rendered as a collapsible page section.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:07 $
 */
public class WCCollapsibleSection
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The page's context
     */
    public WCCollapsibleSection( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Get the HTML element name used for the collapsible section title
     * (default is H2, with the level determined by the level KVC attribute).
     * @return The name of the HTML element that will be used.
     */
    public String elementName()
    {
        if (elementName == null)
        {
            if (hasBinding("elementName"))
            {
                elementName = (String)valueForBinding("elementName");
            }
            else
            {
                elementName = "h" + level();
            }
        }
        return elementName;
    }


    // ----------------------------------------------------------
    public String divId()
    {
        if ( divId == null )
        {
            divId = "s" + context().elementID();
        }
        return divId;
    }


    // ----------------------------------------------------------
    public String onClickValue()
    {
        return "showHide(this, '" + divId() + "');";
    }


    // ----------------------------------------------------------
    @Override
    public boolean isStateless()
    {
        return true;
    }


    // ----------------------------------------------------------
    public void reset()
    {
        divId = null;
        elementName = null;
        initiallyExpanded = null;
        super.reset();
    }


    // ----------------------------------------------------------
    public String title()
    {
        return (String)valueForBinding("title");
    }


    // ----------------------------------------------------------
    public int level()
    {
        if (hasBinding("level"))
        {
            return ERXValueUtilities.intValueWithDefault(
                valueForBinding("level"), 2);
        }
        else
        {
            return 2;
        }
    }


    // ----------------------------------------------------------
    /**
     * Determine if this section starts out expanded.
     * @return True if this section starts expanded, or false if it
     * starts collapsed.
     */
    public Boolean initiallyExpanded()
    {
        if (initiallyExpanded == null)
        {
            initiallyExpanded = Boolean.valueOf(
                !hasBinding("initiallyExpanded")
                || ERXComponentUtilities.booleanValueForBinding(
                    this, "initiallyExpanded", true));
        }
        return initiallyExpanded;
    }


    //~ Instance/static variables .............................................

    private String divId;
    private String elementName;
    private Boolean initiallyExpanded;
}
