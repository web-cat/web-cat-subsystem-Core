/*==========================================================================*\
 |  $Id: WCDirectAction.java,v 1.1 2010/10/24 18:51:02 stedwar2 Exp $
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
import com.webobjects.foundation.*;
import er.extensions.appserver.ERXDirectAction;
import org.apache.log4j.Logger;
import org.webcat.core.Application;

//-------------------------------------------------------------------------
/**
 * A direct action base class that blocks incoming requests until the
 * main application is done initializing.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.1 $, $Date: 2010/10/24 18:51:02 $
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
     * Dispatch an action.
     * @param actionName The name of the action to dispatch
     * @return the action's result
     */
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
        log.debug("performActionNamed(\"" + actionName + "\")");
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
                throw e;
            }
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
