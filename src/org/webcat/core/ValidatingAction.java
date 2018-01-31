/*==========================================================================*\
 |  $Id: ValidatingAction.java,v 1.2 2010/10/07 20:47:31 aallowat Exp $
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

import org.webcat.core.DualAction;
import org.json.JSONException;
import org.json.JSONObject;
import org.webcat.ui.WCButton;
import org.webcat.ui.WCForm;
import org.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;
import er.extensions.appserver.ERXWOContext;

//-------------------------------------------------------------------------
/**
 * <p>
 * An action wrapper that provides for validation of fields in a form. To use
 * this functionality, assign a {@link WCButton} with {@code remote=true} to
 * an action on your component, and inside your action, do the following:
 * </p>
 * <pre>
 * public WOActionResults myAction()
 * {
 *     return new ValidatingAction(this)
 *     {
 *         protected WOActionResults performStandardAction()
 *         {
 *             // logic to be performed when validation was successful
 *             return pageWithName(SomeNewPage.class); // or null
 *         }
 *     };
 * }
 * </pre>
 * <p>
 * Clicking the button will invoke this action remotely, which will push the
 * field value into their bindings, and then this class will run the validators
 * to validate the values of those bindings. If validation failed, the response
 * will be a Javascript fragment that will modify the page in order to display
 * the errors, and the action will end. However, if validation succeeded, then
 * the Javascript response will invoke a standard form submit again on the same
 * button, which will result in the {@link #validationDidSucceed()} method
 * being called so that the new values can be committed and a new page can be
 * loaded.
 * </p>
 *
 * @author  Tony Allevato
 * @version $Id: ValidatingAction.java,v 1.2 2010/10/07 20:47:31 aallowat Exp $
 */
public class ValidatingAction extends DualAction
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the ValidatingAction class.
     *
     * @param component the component on which the action is being invoked
     */
    public ValidatingAction(WOComponent component)
    {
        super(component);

        // We have to retrieve this information at the time that this action is
        // constructed inside the action method on the component, not later on
        // when the action response is actually being generated, because at
        // that time the context will not be the correct one.

        WOContext currentContext = ERXWOContext.currentContext();
        formName = WCForm.formName(currentContext, null);
        formJsId = "form_" + formName;
        formValidationResults = "validationResults_" + formName;
        validators = WCForm.validators();
        senderID = currentContext.senderID();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Runs the validators and manipulates the page layout to reflect any
     * validation errors that may have occurred.
     *
     * @return a JavascriptGenerator that manipulates the page layout
     */
    protected final WOActionResults performRemoteAction()
    {
        JavascriptGenerator page = new JavascriptGenerator();

        JSONObject results = collectValidatorResults();

        page.call(formValidationResults + ".setAll", results);
        page.call(formJsId + ".validate");

        if (results.length() == 0)
        {
            validationDidSucceed(page);
        }
        else
        {
            validationDidFail(page);
        }

        return page;
    }


    // ----------------------------------------------------------
    /**
     * Executes each validator method on the hosting component and collects the
     * resulting messages.
     *
     * @return a JSONObject containing the validation results, where each key
     *     is the WebObjects element ID of a widget that failed validation, and
     *     the corresponding value is the message to be shown to the user
     */
    private JSONObject collectValidatorResults()
    {
        JSONObject results = new JSONObject();

        for (String elementID : validators.keySet())
        {
            WOAssociation validator =
                validators.objectForKey(elementID);
            String message = (String) validator.valueInComponent(component());

            if (message != null)
            {
                try
                {
                    results.put(elementID, message);
                }
                catch (JSONException e)
                {
                    // Do nothing.
                }
            }
        }

        return results;
    }


    // ----------------------------------------------------------
    /**
     * Subclasses should override this in order to execute an action once, and
     * if, validation succeeded. The default implementation simply returns
     * null in order to reload the page. This method is not abstract because
     * in some cases, you may override {@link #validationDidSucceed(JavascriptGenerator)}
     * to take some action other than executing a component action.
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
     * Called if validation succeeded. The default behavior is to execute a
     * Javascript statement that will cause the form to be submitted as a
     * standard page-load action. Subclasses may override this if they wish to
     * provide alternate behavior; for example, a validated form inside a
     * dialog box may wish to close the dialog rather than reload the page.
     * </p><p>
     * If a subclass wants to add its own behavior but still retain the final
     * form submit action, call <code>super.validationDidSucceed(page)</code>
     * <b>after</b> adding your own behavior with the JavascriptGenerator.
     * </p>
     *
     * @param page the JavascriptGenerator used to provide the client-side
     *     behavior
     */
    protected void validationDidSucceed(JavascriptGenerator page)
    {
        // Generate a call that will re-submit the form as a standard
        // page load request, which will result in validationDidSucceed()
        // being called.

        page.submit(formName, senderID);
    }


    // ----------------------------------------------------------
    /**
     * Called if validation failed. The default behavior, after flagging the
     * invalid fields in the form, is to do nothing further; subclasses may
     * override this if they wish to manipulate the page in any other way.
     *
     * @param page the JavascriptGenerator used to provide the client-side
     *     behavior
     */
    protected void validationDidFail(JavascriptGenerator page)
    {
        // Do nothing.
    }


    //~ Static/instance variables .............................................

    private NSDictionary<String, WOAssociation> validators;
    private String formName;
    private String formJsId;
    private String formValidationResults;
    private String senderID;
}
