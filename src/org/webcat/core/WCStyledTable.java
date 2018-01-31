/*==========================================================================*\
 |  $Id: WCStyledTable.java,v 1.2 2012/03/28 13:48:07 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.webcat.ui.util.ComponentIDGenerator;
import org.webcat.ui.util.DojoUtils;
import org.webcat.core.WCStyledTable;
import com.webobjects.appserver.*;
import com.webobjects.foundation.NSMutableSet;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXStringUtilities;

// -------------------------------------------------------------------------
/**
 * <p>
 * A WOGenericContainer that represents a table, formatted and styled
 * the standard Web-CAT way. This component also supports drag-and-drop of
 * rows.
 * </p>
 *
 * <h2>Bindings</h2>
 *
 * <p>
 * Any bindings not specified below are passed directly to the underlying
 * &lt;table&gt; tag so that you can style it and specify any other attributes.
 * </p>
 *
 * <dl>
 *
 * <dt>id</dt>
 * <dd>A unique identifier for the table. When using drag-and-drop, this is
 * used to identify the source and target tables for a drag operation.</dd>
 *
 * <dt>onDropMethod</dt>
 * <dd>The name of a method that will be called when one or more rows are
 * dropped on this table. If omitted, the table will not support drag and drop.
 * This method should be implemented by the component
 * that conatins the WCStyledTable, and should have the following signature:
 * <code>void yourMethodName(String sourceId, int[] draggedRowIndices,
 * String targetId, int[] droppedRowIndices, boolean isCopy).</code>
 *
 * <dt>multiple</dt>
 * <dd>True to allow multiple selection and dragging of rows in the table,
 * otherwise false. Defaults to true.</dd>
 *
 * <dt>withHandles</dt>
 * <dd>True if rows can only be dragged by a drag handle; false if they can
 * be dragged by clicking anywhere in the row. To place a drag handle on a
 * row, put the WCDragHandle element somewhere in the row with an appropriate
 * id (the same id can be used for all drag handles in the table, unless you
 * do something more complicated like nest tables inside rows), and then
 * on the table row, use the <code>dragHandle="theId"</code> attribute. Unlike
 * the default Dojo behavior, this value defaults to true.</dd>
 *
 * <dt>isSource</dt>
 * <dd>True if the table can be used as a drag source; false if it can only
 * act as a target. Defaults to true.</dd>
 *
 * <dt>copyOnly</dt>
 * <dd>Set to true if items may only be copied; false if moves are also
 * permitted. Defaults to false.</dd>
 *
 * <dt>moveOnly</dt>
 * <dd>Set to true if items may only be rearranged in this container; false if
 * they may also be copied. Defaults to false.</dd>
 *
 * <dt>accept</dt>
 * <dd>A comma-delimited list of DnD types that can be accepted by this table
 * when it acts as a target. The type of a row can be specified by using the
 * dndType attribute on the table row's <code>&lt;tr&gt;</code> tag. By
 * default this value will be equivalent to "text", which is also the default
 * type of any row that does not have a dndType explicitly provided.</dd>
 *
 * </dl>
 *
 *  @author  Stephen Edwards, Tony Allevato
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:07 $
 */

public class WCStyledTable
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new WCStyledTable object.
     *
     * @param context The page's context
     */
    public WCStyledTable( WOContext context )
    {
        super( context );
    }


    //~ KVC attributes (must be public) .......................................

/*    public String onDropMethod;
    public boolean multiple = true;
    public boolean withHandles = true;
    public boolean isSource = true;
    public boolean copyOnly = false;
    public boolean moveOnly = false;
    public String accept = null;*/

    public ComponentIDGenerator idFor;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        idFor = new ComponentIDGenerator(this);

        if (id == null)
        {
            id = ERXStringUtilities.safeIdentifierName(context.elementID());
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public boolean synchronizesVariablesWithBindings()
    {
        return false;
    }


    // ----------------------------------------------------------
    public String id()
    {
        return (String) valueForBinding("id");
    }


    // ----------------------------------------------------------
    public String onDropMethod()
    {
        return (String) valueForBinding("onDropMethod");
    }


    // ----------------------------------------------------------
    public boolean multiple()
    {
        return ERXComponentUtilities.booleanValueForBinding(this,
                "multiple", true);
    }


    // ----------------------------------------------------------
    public boolean withHandles()
    {
        return ERXComponentUtilities.booleanValueForBinding(this,
                "withHandles", true);
    }


    // ----------------------------------------------------------
    public boolean isSource()
    {
        return ERXComponentUtilities.booleanValueForBinding(this,
                "isSource", true);
    }


    // ----------------------------------------------------------
    public boolean copyOnly()
    {
        return ERXComponentUtilities.booleanValueForBinding(this,
                "copyOnly", false);
    }


    // ----------------------------------------------------------
    public boolean moveOnly()
    {
        return ERXComponentUtilities.booleanValueForBinding(this,
                "moveOnly", false);
    }


    // ----------------------------------------------------------
    public String accept()
    {
        return (String) valueForBinding("accept");
    }


    // ----------------------------------------------------------
    /**
     * Gets the dojoType of the table depending on whether items can be copied
     * or not, or whether the table is a pure target.
     *
     * @return the dojoType of the table
     */
    public String sourceDojoType()
    {
        return "webcat.TableSource";
    }


    // ----------------------------------------------------------
    /**
     * Returns the accepted dndTypes of this target as a JSON array that can be
     * passed into Dojo. For example, the type list "text,image" would be
     * converted to the JSON string '["text", "image"]'.
     *
     * @return the accepted dndTypes of this target as a JSON array string
     */
    public String acceptedTypesAsJSONArray()
    {
        String accept = accept();

        if (accept == null)
        {
            return null;
        }

        JSONArray array = new JSONArray();

        String[] types = accept.split("\\s*,\\s*");
        for (String type : types)
        {
            array.put(type);
        }

        return DojoUtils.doubleToSingleQuotes(array.toString());
    }


    // ----------------------------------------------------------
    /**
     * The internal JavaScript identifier of the JSON bridge that will be used
     * to communicate with the server to handle user interaction events and
     * model data requests.
     *
     * @return the internal JavaScript identifier of the JSON bridge
     */
    public String JSONBridgeName()
    {
        return "__JSONBridge_" + id;
    }


    // ----------------------------------------------------------
    /**
     * The internal JavaScript identifier that will refer to the actual
     * component inside an AjaxProxy.
     *
     * @return the internal JavaScript identifier of the component inside the
     *     AjaxProxy
     */
    public String componentProxyName()
    {
        return "styledTable";
    }


    // ----------------------------------------------------------
    /**
     * The full JavaScript reference to the proxy object.
     *
     * @return the full JavaScript reference to the component proxy object
     */
    public String fullProxyReference()
    {
        return JSONBridgeName() + "." + componentProxyName();
    }


    // ----------------------------------------------------------
    public String attributeStringForOtherBindings()
    {
        StringBuffer buffer = new StringBuffer(32);

        boolean first = true;
        for (String binding : bindingKeys())
        {
            if (explicitBindings.containsObject(binding))
            {
                continue;
            }

            Object value = valueForBinding(binding);

            if (!first)
            {
                buffer.append(' ');
            }

            buffer.append(binding);
            buffer.append("=\"");
            buffer.append(ERXStringUtilities.escapeNonXMLChars(
                    value.toString()));
            buffer.append('"');

            first = false;
        }

        return buffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * Called when items were dropped onto the table. Delegates to the method
     * on the parent component that was specified by the "onDropMethod"
     * binding.
     *
     * @param sourceId the identifier of the source of the items that were
     *     dropped
     * @param _dragIndices the array of indices of the items that were dropped
     * @param targetId the identifier of this target
     * @param _dropIndices the array of indices that represent the locations at
     *     which the corresponding items in _dragIndices were dropped
     * @param isCopy true if the items were copied; false if they were moved
     */
    public void _itemsWereDropped(
            String sourceId,
            JSONArray _dragIndices,
            String targetId,
            JSONArray _dropIndices,
            boolean isCopy)
    {
        try
        {
            Method method = parent().getClass().getMethod(
                    onDropMethod(),
                    String.class,   // sourceId
                    int[].class,    // draggedRowIndices
                    String.class,   // targetId
                    int[].class,    // droppedRowIndices
                    boolean.class   // copy
            );

            int[] draggedRowIndices = new int[_dragIndices.length()];
            for (int i = 0; i < _dragIndices.length(); i++)
            {
                draggedRowIndices[i] = _dragIndices.getInt(i);
            }

            int[] droppedRowIndices = new int[_dropIndices.length()];
            for (int i = 0; i < _dropIndices.length(); i++)
            {
                droppedRowIndices[i] = _dropIndices.getInt(i);
            }

            method.invoke(parent(),
                    sourceId, draggedRowIndices,
                    targetId, droppedRowIndices,
                    isCopy);
        }
        catch (InvocationTargetException e)
        {
            log.warn(e.getCause());
        }
        catch (Exception e)
        {
            log.warn(e);
        }
    }


    //~ Static/instance variables .............................................

    private String id;

    static NSMutableSet<String> explicitBindings;

    static
    {
        explicitBindings = new NSMutableSet<String>();
        explicitBindings.addObject("id");
        explicitBindings.addObject("onDropMethod");
        explicitBindings.addObject("multiple");
        explicitBindings.addObject("withHandles");
        explicitBindings.addObject("isSource");
        explicitBindings.addObject("copyOnly");
        explicitBindings.addObject("moveOnly");
        explicitBindings.addObject("accept");
    }

    static Logger log = Logger.getLogger(WCStyledTable.class);
}
