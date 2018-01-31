/*==========================================================================*\
 |  $Id: TableRow.java,v 1.5 2011/10/19 12:57:44 stedwar2 Exp $
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
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXValueUtilities;

import org.webcat.core.TableRow;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * A WOGenericContainer that represents a Web-CAT alternating-color table
 * row.
 *
 * @author Stephen Edwards
 * @version $Id: TableRow.java,v 1.5 2011/10/19 12:57:44 stedwar2 Exp $
 */
public class TableRow
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new TableRow object.
     *
     * @param context The page's context
     */
    public TableRow(WOContext context)
    {
        super(context);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public boolean showError()
    {
        return ERXComponentUtilities.booleanValueForBinding(
                this, "showError", false);
    }


    // ----------------------------------------------------------
    public boolean showCaution()
    {
        return ERXComponentUtilities.booleanValueForBinding(
                this, "showCaution", false);
    }


    // ----------------------------------------------------------
    public boolean isSelected()
    {
        return ERXComponentUtilities.booleanValueForBinding(
                this, "isSelected", false);
    }


    // ----------------------------------------------------------
    public boolean increment()
    {
        return ERXComponentUtilities.booleanValueForBinding(
                this, "increment", false);
    }


    // ----------------------------------------------------------
    public int index()
    {
        return ERXValueUtilities.intValueWithDefault(
                valueForBinding("index"), 0);
    }


    // ----------------------------------------------------------
    public String id()
    {
        return (String)valueForBinding("id");
    }


    // ----------------------------------------------------------
    public boolean isSelectable()
    {
        return ERXComponentUtilities.booleanValueForBinding(
                this, "isSelectable", false);
    }


    // ----------------------------------------------------------
    public String dragHandle()
    {
        return (String)valueForBinding("dragHandle");
    }


    // ----------------------------------------------------------
    public String dndType()
    {
        return (String)valueForBinding("dndType");
    }


    // ----------------------------------------------------------
    public String dndData()
    {
        return (String)valueForBinding("dndData");
    }


    // ----------------------------------------------------------
    public void appendToResponse(WOResponse response, WOContext context)
    {
        super.appendToResponse(response, context);

        if (increment() && hasBinding("index"))
        {
            setValueForBinding(index() + 1, "index");
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
        String cssClass = (index() % 2 == 1) ? "e" : "o";
        if (showCaution())
        {
            cssClass += " caution";
        }
        if (showError())
        {
            cssClass += " error";
        }
        if (isSelected())
        {
            if (isSelectable())
            {
                cssClass += " dojoDndItemSelected";
            }
            else
            {
                cssClass += " selected";
            }
        }
        if (dragHandle() != null || isSelectable())
        {
            cssClass += " dojoDndItem";
        }
        return cssClass;
    }


    // ----------------------------------------------------------
    public String style()
    {
        return (String)valueForBinding("style");
    }


    // ----------------------------------------------------------
    public boolean isStateless()
    {
        return true;
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger(TableRow.class);
}
