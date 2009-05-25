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

package net.sf.webcat.ui;

import com.webobjects.appserver.*;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;

//------------------------------------------------------------------------
/**
 * A cell that contains a simple string value.
 *
 * <p><b>Cell Properties</b></p>
 * <ul>
 * <li><b>framework:</b> the name of the framework that contains the images to
 * be used. If not specified, the framework containing the component that is
 * hosting the instance of the ObjectTable is used.
 * <li><b>trueImage:</b> the path to the image used to represent a true value,
 * relative to the WebServerResources directory.
 * <li><b>falseImage:</b> the path to the image used to represent a false
 * value, relative to the WebServerResources directory.
 * </ul>
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class BooleanActionCell extends WCTableCell
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public BooleanActionCell(WOContext context)
    {
        super(context);
    }
    

    //~ KVC attributes (must be public) .......................................

    public String framework;
    
    
    // ----------------------------------------------------------
    @Override
    public void setProperties(NSDictionary<String, Object> props)
    {
        super.setProperties(props);

        // Grab an appropriate default for the framework property. We want to
        // duplicate WO's functionality where the framework doesn't have to be
        // specified if the image is in the same framework as the component --
        // we mimic this by getting the framework that hosts the cell's
        // grandparent (the cell's parent is the ObjectTable; the table's
        // parent is the user's component).

        framework = (String) props.objectForKey("framework");
        if (framework == null)
        {
            framework = NSBundle.bundleForClass(
                    parent().parent().getClass()).name();
        }
    }
}
