/*==========================================================================*\
 |  $Id$
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

// -------------------------------------------------------------------------
/**
 * A custom base class for all EOs that captures common features we want
 * them all to inherit.
 *
 * @author  Stephen Edwards
 * @author  Last changed by: $Author$
 * @version $Revision$, $Date$
 */
public class EOBase
    extends er.extensions.eof.ERXGenericRecord
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     */
    public EOBase()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Determine if a given user can access this object.  The default
     * implementation simply returns true, and should be overridden by
     * subclasses that wish to limit access to a subset of users.
     * @param user The user to check.
     * @return True if the user can access this object.
     */
    public boolean accessibleByUser(User user)
    {
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Determine if a given user can access this object.  The default
     * implementation simply returns true, and should be overridden by
     * subclasses that wish to limit access to a subset of users.
     * @param user The user to check.
     * @return True if the user can access this object.
     */
    public static WCAccessibleQualifier accessibleBy(User user)
    {
        return new WCAccessibleQualifier(user);
    }
}
