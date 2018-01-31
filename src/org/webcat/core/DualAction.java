/*==========================================================================*\
 |  $Id: DualAction.java,v 1.2 2010/10/11 18:31:11 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2009 Virginia Tech
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

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXWOContext;

//-------------------------------------------------------------------------
/**
 * <p>
 * A wrapper that eases the creation of component actions that can operate both
 * as remote (Ajax) actions and full page load actions. The easiest way to use
 * this is to create a new anonymous instance of the class inside your action
 * method, overriding the {@link #performRemoteAction()} and
 * {@link #performStandardAction()} methods to provide the appropriate
 * behavior. For example:
 * </p><p>
 * <pre>
 * public WOActionResults myAction()
 * {
 *     return new DualAction(this)
 *     {
 *         protected WOActionResults performRemoteAction()
 *         {
 *             JavascriptGenerator g = new JavascriptGenerator();
 *             // use g to manipulate the elements on the current page
 *             return g;
 *         }
 *
 *         protected WOActionResults performStandardAction()
 *         {
 *             SomeNewPage page = pageWithName(SomeNewPage.class);
 *             // initialize values on page
 *             return page;
 *         }
 *     };
 * }
 * </pre>
 * </p><p>
 * NOTE: This class, and any of its subclasses, are intended to be
 * <b>stateless</b>. Note that in the above example, the <code>myAction()</code>
 * method will be called <b>twice</b>: once for the remote action invocation,
 * and again for the standard action invocation. This means that two separate
 * instances of the inner class will be created. It is easier to maintain any
 * state that you need in the surrounding component class; if you want to
 * include state in the action class, you must pull it out of the method and
 * into a named inner class, then ensure that the instance gets created once
 * and only once.
 * </p>
 *
 * @author  Tony Allevato
 * @version $Id: DualAction.java,v 1.2 2010/10/11 18:31:11 aallowat Exp $
 */
public abstract class DualAction implements WOActionResults
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the DualAction class, with the specified
     * owning component.
     *
     * @param component the owning component, which will be used as the return
     *     value if {@code performStandardAction} returns null
     */
    public DualAction(WOComponent component)
    {
        this.component = component;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Generates the appropriate response for the action depending on whether
     * the request is an Ajax request or a standard request.
     *
     * @return the action response
     */
    public final WOResponse generateResponse()
    {
        context = ERXWOContext.currentContext();
        WORequest request = context.request();
        boolean isRemote = AjaxUtils.isAjaxRequest(request);

        WOActionResults results = null;

        if (isRemote)
        {
            results = performRemoteAction();

            if (results == null)
            {
                return AjaxUtils.createResponse(request, context);
            }
        }
        else
        {
            results = performStandardAction();

            if (results == null)
            {
                return component.generateResponse();
            }
        }

        context = null;
        return results.generateResponse();
    }


    // ----------------------------------------------------------
    /**
     * Anonymous instances should override this to provide the logic for the
     * action that will be executed when invoked as a remote (Ajax) request.
     *
     * @return the results of the action
     */
    protected abstract WOActionResults performRemoteAction();


    // ----------------------------------------------------------
    /**
     * <p>
     * Anonymous instances should override this to provide the logic for the
     * action that will be executed when invoked as a standard page load
     * request.
     * </p><p>
     * As with regular action methods, this method can return <code>null</code>,
     * which will be interpreted as returning the same instance of the
     * component on which this action was invoked.
     * </p>
     *
     * @return the results of the action
     */
    protected abstract WOActionResults performStandardAction();


    // ----------------------------------------------------------
    /**
     * Gets the component on which this action was invoked.
     *
     * @return the component
     */
    protected final WOComponent component()
    {
        return component;
    }


    // ----------------------------------------------------------
    /**
     * Gets the context under which this action has been invoked. This method
     * is intended to be used only inside the {@code performRemoteAction} and
     * {@code performStandardAction} methods.
     *
     * @return the context
     */
    protected final WOContext context()
    {
        return context;
    }


    //~ Static/instance variables .............................................

    private WOComponent component;
    private WOContext context;
}
