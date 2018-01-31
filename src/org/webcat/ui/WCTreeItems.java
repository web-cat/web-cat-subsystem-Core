/*==========================================================================*\
 |  $Id: WCTreeItems.java,v 1.2 2014/06/16 15:57:54 stedwar2 Exp $
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

import org.webcat.ui._base.WCTreeSubcomponent;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import er.extensions.appserver.ERXWOContext;

//-------------------------------------------------------------------------
/**
 * <p>
 * Provides the iteration over the items in the current batch of the display
 * group. This element should contain a single {@link WCTreeItem} component
 * that defines the cells for a row of the table.
 * </p>
 * Bindings
 * <dl>
 * <dt>item</dt>
 * <dd>The value bound to this key will be updated every time through the set
 * of displayed objects in the tree with the current item in the model (like a
 * <code>WORepetition</code>).</dd>
 * <dt>isExpanded</dt>
 * <dd>The value bound to this key will be updated every time through the set
 * of displayed objects in the tree with the expansion state of the current
 * item in the model.</dd>
 * <dt>noItemsMessage</dt>
 * <dd>A message to display when there are no rows to display in the table.
 * The content of this message should make sense both in the situation where
 * the display group has no objects at all, as well as in the situation where
 * the user has done a search that returned no results.</dd>
 * </dl>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2014/06/16 15:57:54 $
 */
public class WCTreeItems extends WCTreeSubcomponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTreeItems(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public Object item;
    public boolean isExpanded;
    public int rowIndex;
    public WCIndexPath indexPath;
    public String noItemsMessage;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        WCTreeItems oldTreeItems = setCurrentTreeItems(this);

        super.appendToResponse(response, context);

        setCurrentTreeItems(oldTreeItems);
    }


    // ----------------------------------------------------------
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        WCTreeItems oldTreeItems = setCurrentTreeItems(this);

        super.takeValuesFromRequest(request, context);

        setCurrentTreeItems(oldTreeItems);
    }


    // ----------------------------------------------------------
    @Override
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        WCTreeItems oldTreeItems = setCurrentTreeItems(this);

        WOActionResults result = super.invokeAction(request, context);

        setCurrentTreeItems(oldTreeItems);

        return result;
    }


    // ----------------------------------------------------------
    public void setItem(Object object)
    {
        item = object;
        isExpanded = tree().isItemExpanded(object);
        pushValuesToParent();
    }


    // ----------------------------------------------------------
    public boolean hasItems()
    {
        NSArray roots = treeModel().arrangedChildrenOfObject(null);
        return (roots != null && roots.count() > 0);
    }


    // ----------------------------------------------------------
    public String noItemsMessage()
    {
        if (noItemsMessage != null)
        {
            return noItemsMessage;
        }
        else
        {
            return "No data";
        }
    }


    // ----------------------------------------------------------
    public WrappedTreeModel wrappedTreeModel()
    {
        //if (wrappedTreeModel == null)
        {
            wrappedTreeModel = new WrappedTreeModel(treeModel());
        }

        return wrappedTreeModel;
    }


    // ----------------------------------------------------------
    public static WCTreeItems currentTreeItems()
    {
        return (WCTreeItems) ERXWOContext.contextDictionary().objectForKey(
                CURRENT_TREE_ITEMS_KEY);
    }


    // ----------------------------------------------------------
    public static WCTreeItems setCurrentTreeItems(WCTreeItems treeItems)
    {
        WCTreeItems oldTreeItems =
            (WCTreeItems) ERXWOContext.contextDictionary().objectForKey(
                CURRENT_TREE_ITEMS_KEY);

        if (treeItems == null)
        {
            ERXWOContext.contextDictionary().removeObjectForKey(
                    CURRENT_TREE_ITEMS_KEY);
        }
        else
        {
            ERXWOContext.contextDictionary().setObjectForKey(treeItems,
                    CURRENT_TREE_ITEMS_KEY);
        }

        return oldTreeItems;
    }


    //~ Nested classes ........................................................

    // ----------------------------------------------------------
    /**
     * A wrapper tree model that is passed to the
     * {@link WCTreeModelRepetition} element to iterate over only the items
     * in the tree that are currently expanded.
     */
    private class WrappedTreeModel extends WCTreeModel<Object>
    {
        //~ Constructors ......................................................

        // ------------------------------------------------------
        public WrappedTreeModel(WCTreeModel<Object> model)
        {
            this.model = model;
        }


        //~ Methods ...........................................................

        // ------------------------------------------------------
        @Override
        public NSArray<Object> childrenOfObject(Object object)
        {
            return model.childrenOfObject(object);
        }


        // ------------------------------------------------------
        @Override
        public NSArray<Object> arrangedChildrenOfObject(Object object)
        {
            if (object == null || tree().isItemExpanded(object))
            {
                return model.arrangedChildrenOfObject(object);
            }
            else
            {
                return null;
            }
        }


        // ----------------------------------------------------------
        @Override
        public String persistentIdOfObject(Object object)
        {
            return model.persistentIdOfObject(object);
        }


        //~ Static/instance variables .........................................

        private WCTreeModel<Object> model;
    }


    //~ Static/instance variables .............................................

    private static final String CURRENT_TREE_ITEMS_KEY =
        "org.webcat.ui.WCTreeItems.currentTreeItems";

    private WrappedTreeModel wrappedTreeModel;
}
