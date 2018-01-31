/*==========================================================================*\
 |  $Id: WCDirectActionWithSession.java,v 1.1 2010/10/24 18:51:02 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2010 Virginia Tech
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
import org.webcat.core.Application;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 * A direct action base class that provides support for session
 * creation/management.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.1 $, $Date: 2010/10/24 18:51:02 $
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

    // ----------------------------------------------------------
    /**
     * Restores the session associated with this request, if possible.
     */
    protected void restoreSession()
    {
        log.debug("restoreSession()");
        String thisWosid = wosid();
        if (session == null && thisWosid != null)
        {
            if (context().hasSession())
            {
                session = context().session();
            }
            else
            {
                session = Application.application()
                    .restoreSessionWithID(thisWosid, context());
                log.debug("restoreSession(): session = " + session);
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Saves the session associated with this request, if possible.
     */
    protected void saveSession()
    {
        log.debug("saveSession()");
        WOContext context = context();
        if (session != null && context != null)
        {
            log.debug(
                "saveSession(): attempting to save session = " + session);
            Application.application().saveSessionForContext(context);
            session = null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns an existing session, if there is one.
     *
     * @return the session if there is one, or null otherwise
     */
    public WOSession existingSession()
    {
        log.debug("existingSession()");
        if (session != null)
        {
            log.debug("existingSession(): returning one we created");
            return session;
        }

        restoreSession();
        if (session != null)
        {
            log.debug("existingSession(): returning restored session");
            return session;
        }

        log.debug("existingSession(): returning super.existingSession()");
        session = super.existingSession();
        return session;
    }


    // ----------------------------------------------------------
    /**
     * Returns the session for this transaction.
     *
     * @return the session object
     */
    public WOSession session()
    {
        log.debug("session()");
        WOSession mySession = existingSession();
        if (mySession == null)
        {
            log.debug("session(): calling super.session()");
            mySession = context().session();
        }
        if (log.isDebugEnabled())
        {
            log.debug("session() = "
                + (session == null ? "null" : session.sessionID()));
        }
        if (session == null)
        {
            session = mySession;
        }
        return mySession;
    }


    // ----------------------------------------------------------
    /**
     * Returns the session ID for this request, if there is one.
     *
     * @return the session object
     */
    public String wosid()
    {
        String result = wosid;
        if (result == null)
        {
            log.debug("wosid(): attempting to get ID from request");
            result = request().sessionID();
        }
        log.debug("wosid() = " + result);
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Returns the session ID for this request, if there is one.
     *
     * @return the session object
     */
    protected void rememberWosid(String id)
    {
        wosid = id;
    }


    // ----------------------------------------------------------
    /**
     * Tells this object to forget the session it has been working with, so
     * that the session won't be saved at the end of the current action.
     *
     * @return the session object
     */
    protected void forgetSession()
    {
        session = null;
    }


    // ----------------------------------------------------------
    /**
     * Dispatch an action.
     * @param actionName The name of the action to dispatch
     * @return the action's result
     */
    public synchronized WOActionResults performSynchronousActionNamed(
        String actionName)
    {
        WOActionResults result =
            super.performSynchronousActionNamed(actionName);
        saveSession();
        return result;
    }


    //~ Instance/static variables .............................................

    private WOSession session = null;
    private String    wosid   = null;

    static Logger log = Logger.getLogger(WCDirectActionWithSession.class);
}
