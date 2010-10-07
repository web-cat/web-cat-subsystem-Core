/*==========================================================================*\
 |  $Id$
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

import org.webcat.ui.WCButton;
import org.webcat.ui.WCForm;
import org.webcat.ui.generators.JavascriptFunction;
import org.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import er.extensions.appserver.ERXWOContext;

//-------------------------------------------------------------------------
/**
 * <p>
 * An action wrapper that allows the user to confirm whether they want an
 * action to be invoked. To use this functionality, assign a {@link WCButton}
 * with {@code remote=true} to an action on your component, and inside your
 * action, do the following:
 * </p>
 * <pre>
 * public WOActionResults myAction()
 * {
 *     return new ConfirmingAction(this)
 *     {
 *         protected String confirmationMessage()
 *         {
 *             return "Are you sure you want to do this?";
 *         }
 *
 *         protected WOActionResults performStandardAction()
 *         {
 *             // logic to be performed if "Yes" was clicked
 *             return pageWithName(SomeNewPage.class); // or null
 *         }
 *     };
 * }
 * </pre>
 * <p>
 * Clicking the button will invoke this action remotely, which will push the
 * form values into their bindings, and then this class will construct a
 * confirmation dialog that will be presented to the user. If the user selects
 * "Yes" in this dialog, then {@link #performStandardAction()} will be called
 * so that the appropriate action can be taken.
 * </p>
 *
 * @author  Tony Allevato
 * @version $Id$
 */
public abstract class ConfirmingAction extends DualAction
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the ConfirmingAction class.
     *
     * @param component the component on which the action is being invoked
     */
    public ConfirmingAction(WOComponent component)
    {
        super(component);

        // We have to retrieve this information at the time that this action is
        // constructed inside the action method on the component, not later on
        // when the action response is actually being generated, because at
        // that time the context will not be the correct one.

        WOContext currentContext = ERXWOContext.currentContext();
        formName = WCForm.formName(currentContext, null);
        senderID = currentContext.senderID();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Subclasses can override this method to change the title of the
     * confirmation dialog.
     *
     * @return the title of the confirmation dialog
     */
    protected String confirmationTitle()
    {
        return "Confirm Action";
    }


    // ----------------------------------------------------------
    /**
     * Subclasses must override this method to provide the text that will
     * appear in the confirmation dialog when the action is invoked.
     *
     * @return the message that will appear in the confirmation dialog
     */
    protected abstract String confirmationMessage();


    // ----------------------------------------------------------
    /**
     * When the action is invoked as an Ajax request, this method displays the
     * confirmation dialog to the user. The dialog is configured so that when
     * the Yes button is clicked, this action is invoked again, this time as a
     * standard action.
     *
     * @return a JavascriptGenerator that displays the confirmation dialog
     */
    @Override
    protected WOActionResults performRemoteAction()
    {
        JavascriptGenerator page = new JavascriptGenerator();

        page.confirm(confirmationTitle(), confirmationMessage(),
                new JavascriptFunction() {
                    @Override
                    public void generate(JavascriptGenerator g)
                    {
                        actionWasConfirmed(g);
                    }
        });

        return page;
    }


    // ----------------------------------------------------------
    /**
     * Subclasses should override this in order to execute an action once, and
     * if, the user selected "Yes" in the confirmation dialog. The default
     * implementation simply returns null in order to reload the page. This
     * method is not abstract because in some cases, you may override
     * {@link #actionWasConfirmed(JavascriptGenerator)} to take some action
     * other than executing a component action.
     *
     * @return the results of the action
     */
    @Override
    protected WOActionResults performStandardAction()
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Called if the "Yes" button on the confirmation dialog was selected. The
     * default behavior is to execute a Javascript statement that will cause
     * the form to be submitted as a standard page-load action. Subclasses may
     * override this if they wish to provide alternate behavior; for example,
     * if you simply wish to refresh a content pane rather than reload the
     * entire page.
     * </p><p>
     * If a subclass wants to add its own behavior but still retain the final
     * form submit action, call <code>super.actionWasConfirmed(page)</code>
     * <b>after</b> adding your own behavior with the JavascriptGenerator.
     * </p>
     *
     * @param page the JavascriptGenerator used to provide the client-side
     *     behavior
     */
    protected void actionWasConfirmed(JavascriptGenerator page)
    {
        page.submit(formName, senderID);
    }


    //~ Static/instance variables .............................................

    private String formName;
    private String senderID;
}
