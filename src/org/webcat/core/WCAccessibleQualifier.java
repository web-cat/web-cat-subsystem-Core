/*==========================================================================*\
 |  $Id: WCAccessibleQualifier.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2012 Virginia Tech
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

import com.webobjects.foundation.NSSelector;
import er.extensions.qualifiers.*;

// -------------------------------------------------------------------------
/**
 * A custom ERX-style qualifier to check for user accessibility, and that
 * can only be used in-memory.  This is NOT EOF-compatible for database
 * queries!
 *
 * @author  Stephen Edwards
 * @author  Last changed by: $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class WCAccessibleQualifier
    extends ERXKeyValueQualifier
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new qualifier checking for accessibility by the specified
     * user.
     * @param user The user to check
     */
    public WCAccessibleQualifier(User user)
    {
        super("accessibleBy", ACCESSIBLE_BY_USER, user);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public boolean evaluateWithObject(Object object)
    {
        return object != null
            && (((User)value()).hasAdminPrivileges()
                || (object instanceof EOBase
                    && ((EOBase)object).accessibleByUser((User)value())));
    }


    //~ Fields ................................................................

    private static final NSSelector<User> ACCESSIBLE_BY_USER =
        new NSSelector<User>("accessibleByUser:");
}
