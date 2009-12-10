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

package net.sf.webcat.ui;

import net.sf.webcat.ui.util.ComponentIDGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

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
 * <dt>okAction</dt>
 * <dd>The name of the action (specified as a string) that will be executed on
 * the component that contains the dialog when the OK button is pressed.</dd>
 *
 * <dt>refreshPanesOnOk</dt>
 * <dd>The id(s) of the content panes that should be refreshed when the dialog
 * is closed via the OK button.</dd>
 * </dl>
 *
 * @author Tony Allevato
 * @version $Id$
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
     * Gets the value of the <code>refreshPanesOnOk</code> binding.
     *
     * @return the value of the <code>refreshPanesOnOk</code> binding
     */
    public String refreshPanesOnOk()
    {
        return (String) valueForBinding("refreshPanesOnOk");
    }


    // ----------------------------------------------------------
    /**
     * Gets the value of the <code>okAction</code> binding.
     *
     * @return the value of the <code>okAction</code> binding
     */
    public String okAction()
    {
        return (String) valueForBinding("okAction");
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
     * Forwards the OK button action to the parent component.
     *
     * @return the action result
     */
    public WOActionResults okPressed()
    {
        String okAction = okAction();

        if (okAction != null)
        {
            return performParentAction(okAction);
        }
        else
        {
            return null;
        }
    }
}
