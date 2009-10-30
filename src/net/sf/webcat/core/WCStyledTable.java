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

package net.sf.webcat.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.json.JSONArray;
import net.sf.webcat.ui.util.ComponentIDGenerator;
import com.webobjects.appserver.*;
import er.extensions.foundation.ERXStringUtilities;

// -------------------------------------------------------------------------
/**
 * A WOGenericContainer that represents a table, formatted and styled
 * the standard Web-CAT way.
 *
 * @author Stephen Edwards
 * @version $Id$
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

    public String id;
    public String itemsWereDraggedHandler;
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
    public void _itemsWereDragged(
            JSONArray _dragIndices,
            JSONArray _dropIndices)
    {
        try
        {
            Method method = parent().getClass().getMethod(
                    itemsWereDraggedHandler,
                    int[].class, int[].class);

            int[] dragIndices = new int[_dragIndices.length()];
            for (int i = 0; i < _dragIndices.length(); i++)
            {
                dragIndices[i] = _dragIndices.getInt(i);
            }

            int[] dropIndices = new int[_dropIndices.length()];
            for (int i = 0; i < _dropIndices.length(); i++)
            {
                dropIndices[i] = _dropIndices.getInt(i);
            }

            method.invoke(parent(), dragIndices, dropIndices);
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


    static Logger log = Logger.getLogger(WCStyledTable.class);
}
