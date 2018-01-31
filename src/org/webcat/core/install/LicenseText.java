/*==========================================================================*\
 |  $Id: LicenseText.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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

package org.webcat.core.install;

import com.webobjects.appserver.*;

// -------------------------------------------------------------------------
/**
 *  A component that displays the Web-CAT license, suitable for inclusion
 *  in another page.  The height attribute, if set, will cause the license
 *  to be displayed in a 100%-wide auto-scrolling div of the specified
 *  height (in pixels).  If the height is unset, the license is surrounded
 *  by an unstyled div instead.
 *
 *  @author Stephen Edwards
 *  @version $Id: LicenseText.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class LicenseText
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new PreCheckPage object.
     *
     * @param context The context to use
     */
    public LicenseText( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public Number height;
}
