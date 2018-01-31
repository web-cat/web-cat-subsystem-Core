/*==========================================================================*\
 |  $Id: FormattedString.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

//-------------------------------------------------------------------------
/**
 * Display a (possibly formatted) text string.
 *
 * @author Stephen Edwards
 * @version $Id: FormattedString.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public class FormattedString
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Footer object.
     *
     * @param context The page's context
     */
    public FormattedString( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public byte   format;
    public String value;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public boolean preformatted()
    {
        return format == 1;
    }
}
