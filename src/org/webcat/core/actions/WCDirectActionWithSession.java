/*==========================================================================*\
 |  Copyright (C) 2010-2021 Virginia Tech
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

package org.webcat.core.actions;

import com.webobjects.appserver.*;

//-------------------------------------------------------------------------
/**
 * A direct action base class that provides support for session
 * creation/management.
 *
 * @author  Stephen Edwards
 */
public abstract class WCDirectActionWithSession
    extends WCDirectAction
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param aRequest The request to respond to
     */
    public WCDirectActionWithSession(WORequest aRequest)
    {
        super(aRequest);
    }


    //~ Methods ...............................................................

    // Move them all to WCDirectAction, since the session handling
    // was needed there in case the default existingSession() is
    // called. With ERXContext, that method will try to restore the
    // session, so we need full session cleanup code for all direct
    // actions anyway. I think.

    //~ Instance/static variables .............................................

    // static Logger log = Logger.getLogger(WCDirectActionWithSession.class);
}
