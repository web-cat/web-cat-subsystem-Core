/*==========================================================================*\
 |  $Id$
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

package net.sf.webcat.ui;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;
import net.sf.webcat.ui._base.DojoElement;

//--------------------------------------------------------------------------
/**
 * <p>
 * Represents an animated progress indicator in the form of a spinner. This
 * widget exposes the JavaScript methods start() and stop() so that it can be
 * controlled from events on the page.
 * </p>
 *
 * <h2>Bindings</h2>
 *
 * <dl>
 * <dt>id</dt>
 * The widget identifier.
 * <dd></dd>
 *
 * <dt>size</dt>
 * <dd>The size of the progress spinner. Currently three sizes are supported:
 * "small" (18 px), "medium" (36 px), and "large" (72 px). The default is
 * "small".
 * </dl>
 *
 * @author Tony Allevato
 * @version $Id$
 */
public class WCSpinner extends DojoElement
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the WCSpinner class.
     *
     * @param name
     * @param someAssociations
     * @param template
     */
    public WCSpinner(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(name, someAssociations, template);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return "webcat.Spinner";
    }
}
