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

package net.sf.webcat.ui.validation;

import net.sf.webcat.core.Application;
import net.sf.webcat.core.DualAction;
import net.sf.webcat.ui.WCButton;
import net.sf.webcat.ui.WCForm;
import net.sf.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import er.ajax.AjaxUtils;
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
 *         protected WOActionResults wasValidated()
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
 * @version $Id$
 */
public abstract class ValidatingAction extends DualAction
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the ValidatingAction class.
     *
     * @param component the component on which the action is being invoked
     * @param validators the validators used to validate the fields in the form
     */
    public ValidatingAction(WOComponent component, Validator... validators)
    {
        super(component);

        this.validators = validators;

        // We have to retrieve this information at the time that this action is
        // constructed inside the action method on the component, not later on
        // when the action response is actually being generated.

        WOContext currentContext = ERXWOContext.currentContext();
        formName = WCForm.formName(currentContext, null);
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
        JavascriptGenerator g = new JavascriptGenerator();

        NSMutableDictionary<String, String> validationErrors =
            new NSMutableDictionary<String, String>();
        NSMutableSet<String> validFields =
            new NSMutableSet<String>();

        boolean success = true;
        for (Validator validator : validators)
        {
            String error = validator.validate(component());
            success &= (error == null);

            if (error != null)
            {
                validationErrors.setObjectForKey(error, validator.fieldId());
                validFields.removeObject(validator.fieldId());
            }
            else
            {
                validFields.addObject(validator.fieldId());
            }
        }

        if (success)
        {
            // Generate a call that will re-submit the form as a standard
            // page load request, which will result in validationDidSucceed()
            // being called.

            g.submit(formName, senderID);
        }
        else
        {
            // TODO manipulate the page to show the error messages
            //
            // for each error in validationErrors
            //     collect messages
            // manipulate page to show messages and remove error messages from
            // valid fields, if necessary
        }

        return g.generateResponse();
    }


    // ----------------------------------------------------------
    /**
     * Delegates to {@link #validationDidSucceed()}.
     *
     * @return the new page to load when validation was successful
     */
    protected final WOActionResults performStandardAction()
    {
        return validationDidSucceed();
    }


    // ----------------------------------------------------------
    /**
     * Called only if validation was successful.
     *
     * @return the new page to load
     */
    protected abstract WOActionResults validationDidSucceed();


    //~ Static/instance variables .............................................

    private Validator[] validators;
    private String formName;
    private String senderID;
}
