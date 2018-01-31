/*==========================================================================*\
 |  $Id: WCTreeHeading.java,v 1.1 2011/05/13 19:43:46 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2009 Virginia Tech
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

package org.webcat.ui;

import org.webcat.ui._base.WCTableSubcomponent;
import org.webcat.ui._base.WCTreeSubcomponent;
import org.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WOResponse;
import er.extensions.appserver.ERXWOContext;

//-------------------------------------------------------------------------
/**
 * <p>
 * Represents the heading of a single column in the tree. Provides the title
 * of the column.
 * </p><p>
 * This component may only be nested inside WCTreeHeadings.
 * </p>
 * Bindings
 * <dl>
 * <dt>title (<code>String</code>)</dt>
 * <dd>The title that will be displayed in the heading.</dd>
 * </dl>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:43:46 $
 */
public class WCTreeHeading extends WCTreeSubcomponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTreeHeading(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public String title;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String passthroughAttributes()
    {
        return passthroughAttributes;
    }


    // ----------------------------------------------------------
    public boolean isCountingHeadings()
    {
        Boolean value =
            (Boolean) ERXWOContext.contextDictionary().objectForKey(
                COUNTING_HEADINGS_KEY);

        return (value != null) ? value : false;
    }


    // ----------------------------------------------------------
    public void handleTakeValueForUnboundKey(Object value, String key)
    {
        if (passthroughAttributes == null)
        {
            passthroughAttributes = "";
        }

        if (value != null)
        {
            passthroughAttributes += " " + key + "=\""
                + WOMessage.stringByEscapingHTMLAttributeValue(
                        value.toString()) + "\"";
        }
    }


    //~ Static/instance variables .............................................

    private String passthroughAttributes;

    protected static final String COUNTING_HEADINGS_KEY =
        "org.webcat.ui.WCTreeHeading.isCountingHeadings";
}
