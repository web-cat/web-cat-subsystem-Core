/*==========================================================================*\
 |  $Id: WCDialog.java,v 1.2 2011/05/16 17:29:30 aallowat Exp $
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

package org.webcat.ui;

import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.ComponentIDGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXValueUtilities;

//-------------------------------------------------------------------------
/**
 * A Dojo dialog whose content is represented by component content. The content
 * is generated on demand, only when the dialog is first shown.
 *
 * <h2>Bindings</h2>
 *
 * <dl>
 * <dt>id</dt>
 * <dd>The identifier of the Dijit dialog. Use it to spawn the dialog like
 * <code>dijit.byId(theId).show()</code>.</dd>
 *
 * <dt>title</dt>
 * <dd>The title of the dialog.</dd>
 *
 * <dt>immediate</dt>
 * <dd>NOT IMPLEMENTED. If true, the dialog contents will be rendered immediately when the
 * dialog's container is first loaded, instead of when the dialog appears.
 * This is best used for small requesters to show them more quickly, instead of
 * displaying a "Loading..." message when they first appear.</dd>
 *
 * <dt>okAction</dt>
 * <dd>The name of the action (specified as a string) that will be executed on
 * the component that contains the dialog when the OK button is pressed.</dd>
 *
 * <dt>okButtonId</dt>
 * <dd>If this binding exists, it will receive the widget ID of the dialog's
 * OK button. This can be useful when the dialog owner needs to manipulate the
 * button (such as enabling or disabling it).</dd>
 *
 * <dt>okLabel</dt>
 * <dd>The text that should appear on the dialog's OK button. Defaults to
 * "OK".</dd>
 *
 * <dt>cancelLabel</dt>
 * <dd>The text that should appear on the dialog's Cancel button. Defaults to
 * "Cancel".</dd>
 *
 * </dl>
 *
 * @author Tony Allevato
 * @version $Id: WCDialog.java,v 1.2 2011/05/16 17:29:30 aallowat Exp $
 */
public class WCDialog extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the WCDialog class.
     *
     * @param context
     */
    public WCDialog(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public ComponentIDGenerator idFor;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        idFor = new ComponentIDGenerator(this);

        if (canSetValueForBinding("okButtonId"))
        {
            setValueForBinding(idFor.get("okButtonId"), "okButtonId");
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public boolean synchronizesVariablesWithBindings()
    {
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Gets the value of the <code>id</code> binding.
     *
     * @return the value of the <code>id</code> binding
     */
    public String id()
    {
        return (String) valueForBinding("id");
    }


    // ----------------------------------------------------------
    /**
     * Gets the value of the <code>immediate</code> binding.
     *
     * @return the value of the <code>immediate</code> binding
     */
    public boolean immediate()
    {
        return ERXComponentUtilities.booleanValueForBinding(
                this, "immediate", false);
    }


    // ----------------------------------------------------------
    /**
     * Gets the inverse of the <code>immediate</code> binding.
     *
     * @return the inverse of the <code>immediate</code> binding
     */
    public boolean alwaysDynamic()
    {
        return !immediate();
    }


    // ----------------------------------------------------------
    /**
     * Gets the value of the <code>okAction</code> binding.
     *
     * @return the value of the <code>okAction</code> binding
     */
    public WOActionResults okAction()
    {
        return (WOActionResults) valueForBinding("okAction");
    }


    // ----------------------------------------------------------
    /**
     * Determines if the dialog should have an OK button, based on whether it
     * has an "okAction" binding.
     *
     * @return true if the dialog should have an OK button, otherwise false
     */
    public boolean hasOkButton()
    {
        return hasBinding("okAction");
    }


    // ----------------------------------------------------------
    /**
     * Gets the value of the <code>title</code> binding.
     *
     * @return the value of the <code>title</code> binding
     */
    public String title()
    {
        return (String) valueForBinding("title");
    }


    // ----------------------------------------------------------
    /**
     * Gets the value of the <code>okLabel</code> binding.
     *
     * @return the value of the <code>okLabel</code> binding
     */
    public String okLabel()
    {
        String value = (String) valueForBinding("okLabel");
        return value != null ? value : "OK";
    }


    // ----------------------------------------------------------
    /**
     * Gets the value of the <code>cancelLabel</code> binding.
     *
     * @return the value of the <code>cancelLabel</code> binding
     */
    public String cancelLabel()
    {
        String value = (String) valueForBinding("cancelLabel");
        return value != null ? value : "Cancel";
    }


    // ----------------------------------------------------------
    /**
     * Executes the bound okAction and hides the dialog.
     *
     * @return the action result
     */
    public WOActionResults okPressed()
    {
        WOActionResults result = okAction();
        JavascriptGenerator js;

        // Three possibilities here:
        //
        // 1) The action bound to the OK button returns a JavascriptGenerator.
        //    In this case, append the dialog hide() call to it so that the
        //    dialog is dismissed after the action returns.
        // 2) The action returns null. Create a new JavascriptGenerator that
        //    hides the dialog.
        // 3) The action returns something else (such as a ValidatingAction).
        //    In this case, just return the action result verbatim; it will be
        //    up to the action to dismiss the dialog manually.

        if (result == null || result instanceof JavascriptGenerator)
        {
            // If the action returned a JavascriptGenerator, attach a call to the
            // end of it to hide the dialog; otherwise, just return a new one that
            // hides the dialog.

            js = (result == null) ? new JavascriptGenerator() :
                (JavascriptGenerator) result;

            js.dijit(id()).call("hide");
            js.dijit(idFor.get("okButtonId")).enable();

            return js;
        }
        else
        {
            return result;
        }
    }
}
