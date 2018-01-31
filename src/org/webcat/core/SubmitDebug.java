/*==========================================================================*\
 |  $Id: SubmitDebug.java,v 1.2 2011/03/07 18:44:37 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2011 Virginia Tech
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

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

// -------------------------------------------------------------------------
/**
 *  A simple test component used to debug some direct action issues.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2011/03/07 18:44:37 $
 */
public class SubmitDebug
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new SubmitDebug object.
     *
     * @param context The context to use
     */
    public SubmitDebug( WOContext context )
    {
        super( context );
    }


    //~ Methods ...............................................................

    //~ Instance/static variables .............................................

    public NSDictionary<?, ?> errors;
}
