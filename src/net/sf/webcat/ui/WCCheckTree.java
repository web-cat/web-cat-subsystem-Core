/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2008 Virginia Tech
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import er.extensions.foundation.ERXStringUtilities;

// ------------------------------------------------------------------------
/**
 * A subclass of {@link WCTree} that attaches check box widgets to each node.
 *
 * <h2>Bindings</h2>
 * <table>
 * <tr>
 * <td>{@code checkedItems}</td>
 * <td>An array of items that are checked in the tree.</td>
 * </tr>
 * <tr>
 * <td>{@code independentChecks}</td>
 * <td>If true, items in the tree can be checked independently of their parent
 * or child items. The default value of this property is false, indicating that
 * the checked state of an item is tied to its children (a non-leaf item will
 * only be checked if all of its children are checked).</td>
 * </tr>
 * </table>
 *
 * @author Tony Allevato
 * @version $Id$
 */
public class WCCheckTree extends WCTree
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new TreeComponent.
     *
     * @param context
     *            the context
     */
    public WCCheckTree(WOContext context)
    {
        super(context);

        NSMutableArray<IItemDecorator> decorators =
            new NSMutableArray<IItemDecorator>();
        decorators.add(new CheckItemDecorator());
        this.decorators = decorators;

        this.checkedItems = new NSMutableArray<Object>();
    }


    //~ KVC attributes (must be public) .......................................

    public String checkboxName;
    public NSMutableArray<Object> checkedItems;
    public Boolean independentChecks;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String additionalAttributes()
    {
        StringBuilder buffer = new StringBuilder();

        if(independentChecks != null)
        {
            buffer.append("decorationOptions=\"{ ");
            buffer.append("independentChecks: ");
            buffer.append(independentChecks.toString());
            buffer.append(" }\"");
        }

        return buffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * The internal JavaScript identifier of the JSON bridge that will be used
     * to communicate with the server to handle user interaction events and
     * model data requests.
     */
    public String JSONBridgeName()
    {
        return "__JSONBridge_checkTree_" + id;
    }


    // ----------------------------------------------------------
    /**
     * The internal JavaScript identifier that will refer to the actual
     * component inside an AjaxProxy.
     */
    public String componentProxyName()
    {
        return "checkTree";
    }


    // ----------------------------------------------------------
    /**
     * The full JavaScript reference to the proxy object.
     */
    public String fullProxyReference()
    {
        return JSONBridgeName() + "." + componentProxyName();
    }


    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        checkboxName = context.elementID();

        if (id == null)
        {
            id = ERXStringUtilities.safeIdentifierName(context.elementID());
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        NSArray<Object> values = request.formValuesForKey(checkboxName);

        if (values != null)
        {
            NSMutableArray<Object> selection = new NSMutableArray<Object>();

            for (Object itemId : values)
            {
                Object item = model.itemForId(itemId.toString());

                if (item != null)
                    selection.addObject(item);
            }

            checkedItems = selection;
        }

        super.takeValuesFromRequest(request, context);
    }


    // ----------------------------------------------------------
    public void _handleCheckChanged(JSONArray items, boolean checked)
    throws JSONException
    {
        for (int i = 0; i < items.length(); i++)
        {
            String itemId = items.getString(i);
            Object item = model.itemForId(itemId);

            if (item != null)
            {
                if (checked)
                {
                    if (checkedItems == null)
                    {
                        checkedItems = new NSMutableArray<Object>();
                    }

                    if (!checkedItems.containsObject(item))
                    {
                        checkedItems.addObject(item);
                    }
                }
                else
                {
                    checkedItems.removeIdenticalObject(item);
                }
            }
        }

        // Manually push the value of the checkedItems binding back to the
        // parent whenever changes are made to ensure that the component state
        // is kept synchronized with the client-side tree widget. We do this
        // here so that we don't have to wait for a complete form submit for
        // the state to be updated (but it will happen there anyway, too).

        WOAssociation association =
            _keyAssociations.objectForKey("checkedItems");

        if (association != null &&
                association.isValueSettableInComponent(parent()))
        {
            association.setValue(checkedItems, parent());
        }
    }


    //~ Private classes .......................................................

    // ----------------------------------------------------------
    /**
     * Decorates the JSON representation of a tree item with a "checked"
     * property, based on the {@code checkedItems} binding in this component.
     */
    private class CheckItemDecorator implements IItemDecorator
    {
        // ------------------------------------------------------
        public void decorateItem(Object modelItem, JSONObject properties)
                throws JSONException
        {
            if (checkedItems != null && checkedItems.containsObject(modelItem))
            {
                properties.put("checked", true);
            }
        }
    }


    //~ Static/instance variables .............................................

    private static final long serialVersionUID = 1L;
}
