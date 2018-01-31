/*==========================================================================*\
 |  $Id: ConfirmingAction.java,v 1.6 2010/11/03 19:36:56 aallowat Exp $
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
import org.webcat.ui.util.JSHash;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
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
 *     return new ConfirmingAction(this, false)
 *     {
 *         protected String confirmationMessage()
 *         {
 *             return "Are you sure you want to do this?";
 *         }
 *
 *         protected WOActionResults actionWasConfirmed()
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
 * "Yes" in this dialog, then {@link #actionWasInvoked()} will be called
 * so that the appropriate action can be taken.
 * </p>
 *
 * @author  Tony Allevato
 * @version $Id: ConfirmingAction.java,v 1.6 2010/11/03 19:36:56 aallowat Exp $
 */
public abstract class ConfirmingAction extends DualAction
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the ConfirmingAction class.
     *
     * @param component the component on which the action is being invoked
     * @param isRemote true if the action that should be carried out if the
     *     user clicks "Yes" should be executed remotely; false if it should be
     *     executed as a standard page-load action
     */
    public ConfirmingAction(WOComponent component, boolean isRemote)
    {
        super(component);

        // We have to retrieve this information at the time that this action is
        // constructed inside the action method on the component, not later on
        // when the action response is actually being generated, because at
        // that time the context will not be the correct one.

        WOContext currentContext = ERXWOContext.currentContext();
        formName = WCForm.formName(currentContext, null);
        elementID = currentContext.elementID();

        this.request = currentContext.request();
        this.isRemote = isRemote;
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

        if (!showedConfirmationDialog())
        {
            page.confirm(confirmationTitle(), confirmationMessage(),
                    new JavascriptFunction() {
                        @Override
                        public void generate(JavascriptGenerator g)
                        {
                            generateYesHandler(g);
                        }
            });
        }
        else
        {
            // We're in the second remote phase of an action where the use
            // clicked yes. Call the appropriate method.

            return actionWasConfirmed();
        }

        return page;
    }


    // ----------------------------------------------------------
    /**
     * Indicates whether the confirmation dialog has been shown to the user.
     * This is only needed in the case where the post-confirmation phase of
     * the request is also performed remotely, in order to differentiate
     * between the first phase (displaying the dialog) and the
     * post-confirmation phase (executing some server-side action).
     *
     * @return the action's current state
     */
    private boolean showedConfirmationDialog()
    {
        Boolean wasConfirmed = Boolean.parseBoolean(
                (String) request.formValueForKey(
                        SHOWED_CONFIRMATION_DIALOG_KEY));

        if (wasConfirmed == null)
        {
            return false;
        }
        else
        {
            return wasConfirmed;
        }
    }


    // ----------------------------------------------------------
    /**
     * This method calls {@link #actionWasConfirmed()}. Subclasses should
     * override that method to provide behavior when the dialog is confirmed,
     * not this method.
     *
     * @return the results of the action
     */
    @Override
    protected WOActionResults performStandardAction()
    {
        return actionWasConfirmed();
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Generates the Javascript code that should be called immediately after
     * the user clicks "Yes", but before the associated server-side action is
     * executed.
     * </p><p>
     * The default implementation does nothing; subclasses can override this to
     * prepare for a potentially long-running operation, by starting a progress
     * spinner for example.
     * </p>
     *
     * @param page the JavascriptGenerator that will contain code to be
     *     executed immediately when the "Yes" button is clicked
     */
    protected void beforeActionWasConfirmed(JavascriptGenerator page)
    {
        // Default implementation does nothing.
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Generates the Javascript code that should be executed on the client
     * if the "Yes" button on the confirmation dialog was selected. The
     * default behavior is to execute a Javascript statement that will cause
     * the form to be submitted as a standard page-load action (if the action
     * is not remote) or to re-execute the action remotely (if the action is
     * remote). Both will cause the {@link #actionWasConfirmed()} method to be
     * invoked.</p>
     *
     * @param page the JavascriptGenerator used to provide the client-side
     *     behavior
     */
    private void generateYesHandler(JavascriptGenerator page)
    {
        beforeActionWasConfirmed(page);

        if (isRemote)
        {
            JSHash options = new JSHash();
            options.put("url", request.uri());
            options.put("submit", JSHash.code("null"));
            options.put("handleAs", "javascript");
            options.put("sender", elementID);

            JSHash content = new JSHash();
            content.put(SHOWED_CONFIRMATION_DIALOG_KEY, true);
            options.put("content", content);

            page.remoteSubmit(options);
        }
        else
        {
            page.submit(formName, elementID);
        }
    }


    // ----------------------------------------------------------
    /**
     * Subclasses should override this in order to execute an action once, and
     * if, the user selected "Yes" in the confirmation dialog. The default
     * implementation simply returns null in order to reload the page (in the
     * case of a non-remote action) or to do nothing (in the case of a remote
     * action).
     *
     * @return the results of the action
     */
    protected WOActionResults actionWasConfirmed()
    {
        return null;
    }


    //~ Static/instance variables .............................................

    private static final String SHOWED_CONFIRMATION_DIALOG_KEY =
        "org.webcat.core.ConfirmingAction.showedConfirmationDialog";

    private boolean isRemote;
    private WORequest request;

    private String formName;
    private String elementID;
}
