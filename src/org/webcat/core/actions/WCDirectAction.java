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
import com.webobjects.foundation.*;
import er.extensions.appserver.ERXDirectAction;
import er.extensions.appserver.ERXWOContext;
import org.apache.log4j.Logger;
import org.webcat.core.Application;

//-------------------------------------------------------------------------
/**
 * A direct action base class that blocks incoming requests until the
 * main application is done initializing.
 *
 * @author  Stephen Edwards
 */
public abstract class WCDirectAction
    extends ERXDirectAction
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param aRequest The request to respond to
     */
    public WCDirectAction(WORequest aRequest)
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
        // ERXWOContext overrides existingSession() to do this already
        WOContext ctxt = context();
        if (ctxt instanceof ERXWOContext)
        {
            if (((ERXWOContext)ctxt).existingSession() == null
                && ctxt._requestSessionID() != null)
            {
                // Unable to restore, so must be stale id
                forgetSession();
            }
        }
        else
        {
            String sessionID = ctxt._requestSessionID();
            if (!ctxt.hasSession() && sessionID != null)
            {
                log.debug("restoreSession(" + sessionID
                    + ") calling restoreSessionWithID() for plain context");
                if (Application.application().restoreSessionWithID(
                    sessionID, ctxt) == null)
                {
                    // Unable to restore, so must be stale id
                    forgetSession();
                }
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * If there is a current session but it hasn't been logged in, then
     * terminate it.
     */
    protected void terminateSessionIfTemporary()
    {
        WOSession session = context()._session();
        if (session != null
            && !session.isTerminating()
            && session instanceof org.webcat.core.Session)
        {
            org.webcat.core.Session wc = (org.webcat.core.Session)session;
            if (!wc.isLoggedIn())
            {
                // Never logged in
                wc.terminate();
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
        if (context != null)
        {
            WOSession session = context._session();
            if (session != null)
            {
                log.debug("saveSession(): attempting to save session = "
                    + session.sessionID());
                Application.application().saveSessionForContext(context);
            }
        }
    }


    // ----------------------------------------------------------
    public boolean alreadyHasSessionActive()
    {
        return context()._session() != null;
    }


    // ----------------------------------------------------------
    /**
     * Returns an existing session, if there is one.
     *
     * @return the session if there is one, or null otherwise
     */
    @Override
    public WOSession existingSession()
    {
        log.debug("existingSession()");
        WOSession session = context()._session();
        if (session != null)
        {
            log.debug("existingSession(): returning one we created: "
                + session.sessionID());
            return session;
        }

        restoreSession();
        session = context()._session();
        if (session != null)
        {
            log.debug("existingSession(): returning restored session: "
                + session.sessionID());
            return session;
        }

        session = super.existingSession();
        log.debug("existingSession(): returning super.existingSession(): "
            + (session == null ? "null" : session.sessionID()));
        return session;
    }


    // ----------------------------------------------------------
    /**
     * Returns the session for this transaction.
     *
     * @return the session object
     */
    @Override
    public WOSession session()
    {
        log.debug("session(): checking for existing session");
        WOSession session = existingSession();
        if (session == null)
        {
            log.debug("session(): calling context().session()");
            session = context().session();
        }
        if (log.isDebugEnabled())
        {
            log.debug("session(): result = "
                + (session == null ? "null" : session.sessionID()));
        }
        return session;
    }


    // ----------------------------------------------------------
    /**
     * Returns the session ID for this request, if there is one.
     *
     * @return the session object
     */
    protected void rememberWosid(String id)
    {
        context()._setRequestSessionID(id);
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
        context()._setRequestSessionID(null);
    }


    // ----------------------------------------------------------
    /**
     * Dispatch an action.
     * @param actionName The name of the action to dispatch
     * @return the action's result
     */
    @Override
    public WOActionResults performActionNamed(String actionName)
    {
        // wait for application to initialize
        log.debug( "performActionNamed(\"" + actionName + "\")");
        if (actionShouldWaitForInitialization(actionName))
        {
            if (log.isDebugEnabled())
            {
                log.debug("Action " + actionName
                    + " will wait for Application initialization to complete");
            }
            Application.waitForInitializationToComplete();
        }
        return performActionNamed(actionName, this);
    }


    // ----------------------------------------------------------
    /**
     * Dispatch an action.
     * @param actionName The name of the action to dispatch
     * @param owner      The DirectAction object on which the action
     *                   will be invoked
     * @return the action's result
     */
    public static WOActionResults performActionNamed(
        String actionName, WCDirectAction owner)
    {
        log.debug("performActionNamed(\"" + actionName + "\", owner)");
        return owner.performSynchronousActionNamed(actionName);
    }


    // ----------------------------------------------------------
    /**
     * Dispatch an action, ensuring that only one action at a time execute
     * in this object.  This method provides concurrency protection against
     * separate browser requests on the same direct action object.
     * @param actionName The name of the action to dispatch
     * @return the action's result
     */
    public synchronized WOActionResults performSynchronousActionNamed(
        String actionName)
    {
        log.debug("performSynchronousActionNamed(\"" + actionName + "\")");
        WOActionResults result = null;
        try
        {
            result = super.performActionNamed(actionName);
        }
        catch (NSForwardException e)
        {
            if (e.originalException() instanceof NoSuchMethodException)
            {
                // assume this was a bad request with an invalid action
                // name, and just go to the default action instead.
                result = defaultAction();
            }
            else
            {
                log.error(e);
                throw e;
            }
        }
        catch (RuntimeException e)
        {
            log.error(e);
            throw e;
        }
        finally
        {
            terminateSessionIfTemporary();
            saveSession();
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Determine whether a given action should wait for the Application
     * to finish initialization before it proceeds.  This default
     * implementation returns true for all actions, but can be overridden
     * in subclasses.
     *
     * @param actionName The name of the action
     * @return True if the named action should wait for initialization of
     *         the application to complete before the action executes.
     */
    protected boolean actionShouldWaitForInitialization(String actionName)
    {
        return true;
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger(WCDirectAction.class);
}
