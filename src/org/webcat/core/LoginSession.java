/*==========================================================================*\
 |  $Id: LoginSession.java,v 1.2 2012/01/27 16:36:20 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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
import com.webobjects.eocontrol.*;
import org.webcat.core.LoginSession;
import org.webcat.core.User;
import org.webcat.core._LoginSession;

// -------------------------------------------------------------------------
/**
 * Keeps track of which user is logged in where.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/01/27 16:36:20 $
 */
public class LoginSession
    extends _LoginSession
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new LoginSession object.
     */
    public LoginSession()
    {
        super();
    }


    // ----------------------------------------------------------
    /**
     * A static factory method for creating a new LoginSession object for
     * the given user and session ID.
     * @param editingContext The context in which the new object will be
     * inserted
     * @param aUser The user for this login session.
     * @param sessionID The session ID.
     * @return The newly created object
     */
    public static LoginSession create(
        EOEditingContext editingContext, User aUser, String sessionID)
    {
        LoginSession loginSession = create(
            editingContext,
            UsagePeriod.currentUsagePeriodForUser(editingContext, aUser));
        loginSession.setSessionId(sessionID);
        loginSession.setUserRelationship(aUser);
        return loginSession;
    }


    //~ Methods ...............................................................


    // ----------------------------------------------------------
    /**
     * Looks up the current WCLoginSession for this user in the
     * database.
     *
     * This is used for session timeout tracking, as well as to
     * handle situations where the user is logged in through multiple
     * browser windows simultaneously.  This method presumes the given
     * editing context is already locked or uses auto-locking.
     *
     * @param ec   The current editing context
     * @param aUser The user to look for
     * @return The current login session, or null if there is not one
     */
    public static LoginSession getLoginSessionForUser(
        EOEditingContext ec, User aUser)
    {
        log.debug("getLoginSession()");
        LoginSession result = null;
        if (aUser != null)
        {
            log.debug("searching for login session for " + aUser.userName());
            NSArray<LoginSession> items =
                objectsMatchingValues(ec, USER_KEY, aUser);

            if (items != null)
            {
                if (items.count() > 1)
                {
                    // More than one active session with this user.
                    log.error(
                        "Error: multiple stored login sessions for user: "
                        + aUser.name());
                }
                if (items.count() > 0)
                {
                    result = items.objectAtIndex(0);
                }
            }
        }
        else
        {
            log.debug("null login session: user not logged in");
        }
        if (result != null)
        {
            log.debug("getLoginSession(): " + result.sessionId());
        }
        else
        {
            log.debug("getLoginSession(): null login session");
        }
        return result;
    }
}
