/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011 Virginia Tech
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

package org.webcat.core.messaging;

import org.webcat.core.User;
import com.webobjects.foundation.NSArray;

//-------------------------------------------------------------------------
/**
 * A message that is sent to one person.
 *
 * @author  Stephen Edwards
 * @author  Latest changes by: $Author$
 * @version $Revision$ $Date$
 */
public abstract class SingleUserMessage
    extends Message
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public SingleUserMessage(User user)
    {
        users = new NSArray<User>(user.localInstance(editingContext()));
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public NSArray<User> users()
    {
        return users;
    }


    //~ Static/instance variables .............................................

    private NSArray<User> users;
}
