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

package org.webcat.ui;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

// ------------------------------------------------------------------------
/**
 * Represents the data model for a {@link org.webcat.ui.WCTree} element. The
 * {@code model} binding of the {@code Tree} element should be set to an
 * instance of an {@code AbstractTreeModel} subclass that provides the data and
 * visual representation of the items in the tree.
 *
 * @author Tony Allevato
 * @version $Id$
 */
public abstract class AbstractTreeModel
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new instance of the AbstractTreeModel class.
     */
    protected AbstractTreeModel()
    {
        itemLookupCache = new NSMutableDictionary<String, Object>();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Given the ID of an item in the tree model, this method computes the
     * children for the actual underlying item (by calling
     * {@link childrenOfItem}), and also precomputes the labels and other
     * properties for each of those children. These values are then bundled into
     * a JSON array that will be returned back to the JavaScript caller.
     * <p>
     * This bundling allows the Dojo tree to gather all of the properties that
     * it needs for one level of the tree in one Ajax request, rather than
     * sending a separate request for each child and property that it needs.
     *
     * @param itemId
     *            the ID of an item whose children should be accessed
     *
     * @return a {@link JSONArray} containing data about the children
     *
     * @throws JSONException
     */
    public JSONArray _jsDataForChildrenOfItemWithId(String itemId)
            throws JSONException
    {
        return _jsDataForChildrenOfItemWithId(itemId, null);
    }


    // ----------------------------------------------------------
    /**
     * Given the ID of an item in the tree model, this method computes the
     * children for the actual underlying item (by calling
     * {@link childrenOfItem}), and also precomputes the labels and other
     * properties for each of those children. These values are then bundled into
     * a JSON array that will be returned back to the JavaScript caller.
     * <p>
     * This version of the method accepts an array of decorators that can be
     * used to tag each item representation with additional properties that will
     * be accessible to JavaScript &ndash; for example, to associate a "checked"
     * state with each node.
     * <p>
     * This bundling allows the Dojo tree to gather all of the properties that
     * it needs for one level of the tree in one Ajax request, rather than
     * sending a separate request for each child and property that it needs.
     *
     * @param itemId
     *            the ID of an item whose children should be accessed
     * @param decorators
     *            an array of objects that implement {@link IItemDecorator}
     *
     * @return a {@link JSONArray} containing data about the children
     *
     * @throws JSONException
     */
    public JSONArray _jsDataForChildrenOfItemWithId(String itemId,
            NSArray<IItemDecorator> decorators) throws JSONException
    {
        Object parentItem;

        if (itemId == null)
        {
            parentItem = null;
            itemId = "";
        }
        else
        {
            parentItem = itemLookupCache.objectForKey(itemId);
        }

        NSArray<?> children = childrenOfItem(parentItem);

        JSONArray array = new JSONArray();

        if (children != null)
        {
            for (Object childItem : children)
            {
                JSONObject properties = new JSONObject();

                String label = labelForItem(childItem);
                properties.put("label", label);

                String childId = idForItem(childItem);
                if (childId == null)
                    childId = itemId + "/" + label.replace('/', '\\');

                properties.put("itemId", childId);

                boolean hasChildren = itemHasChildren(childItem);
                properties.put("hasChildren", hasChildren);

                String iconClass = iconClassForItem(childItem);
                properties.put("iconClass", iconClass);

                // Let any decorators attach their own properties to the item before
                // it is returned.
                if (decorators != null)
                {
                    for (IItemDecorator decorator : decorators)
                    {
                        decorator.decorateItem(childItem, properties);
                    }
                }

                array.put(properties);

                itemLookupCache.setObjectForKey(childItem, childId);
            }
        }

        return array;
    }


    // ----------------------------------------------------------
    /**
     * Gets the model item that corresponds to the specified item ID.
     * <p>
     * Client code should only have to call this code if it is doing custom
     * event handling on a tree widget and needs to access a model item based on
     * the item ID passed to the event handler.
     *
     * @param itemId
     *            the item ID to look up
     * @return the model item that corresponds to the specified item ID
     */
    public Object itemForId(String itemId)
    {
        return itemLookupCache.objectForKey(itemId);
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the specified item has children.
     * <p>
     * By default, this method simply calls {@link #childrenOfItem(Object)} and
     * returns {@code true} if the resulting array is not null or empty. In the
     * event that computing the children of an item is an inefficient operation,
     * subclasses should override this to get this value without actually
     * computing the children.
     *
     * @param parentItem
     *            the parent item
     * @return true if the item has children; otherwise false
     */
    protected boolean itemHasChildren(Object parentItem)
    {
        NSArray<?> children = childrenOfItem(parentItem);
        return (children != null && children.count() > 0);
    }


    // ----------------------------------------------------------
    /**
     * Gets an array of items that represent the children of the specified item
     * in the tree.
     *
     * @param parentItem
     *            the parent item, or {@code null} to get the root items
     * @return an {@link NSArray} containing the children of the parent item
     */
    protected abstract NSArray<?> childrenOfItem(Object parentItem);


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets the label that will be displayed for the specified item.
     * </p><p>
     * By default, this method simply calls {@link toString} on the item.
     * Subclasses can override this method to provide different behavior.
     * </p><p>
     * Unlike the standard Dojo Tree widget, the Web-CAT UI version of the tree
     * permits arbitrary HTML content in the label. Use this with care to avoid
     * breaking the layout of the tree widget; the intention here is to permit
     * rich formatting of the label.
     * </p>
     *
     * @param item
     *            the item
     * @return the label to display for the item
     */
    protected String labelForItem(Object item)
    {
        return item.toString();
    }


    // ----------------------------------------------------------
    /**
     * Gets the CSS class that defines the icon to be displayed for the
     * specified item.
     * <p>
     * By default, this method returns null, which instructs the tree to use a
     * default icon depending on whether the node is a leaf or has children.
     * Subclasses can override this method to provide different behavior for
     * individual items.
     *
     * @param item
     *            the item
     * @return the CSS class of the icon for this item, or null to display a
     *         default icon
     */
    protected String iconClassForItem(Object item)
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Gets a unique identifier that will be used to keep track of the item when
     * the tree is being manipulated on the JavaScript side. This creates a
     * mapping between identifiers and items so that when the tree is altered by
     * the user, the server-side logic can translate these events back into the
     * actual data model items that were affected.
     * <p>
     * The identifier associated with a node may also be used in form fields
     * attached to the node; for example, a tree with checkboxes might use this
     * identifier as the value for a batch of same-named checkbox fields so that
     * the request handler receives an array of identifiers that were checked.
     * <p>
     * By default, this method returns null, which instructs the tree model to
     * use the label path of the node as its identifier. This should be
     * sufficient if the labels are known to be unique and the tree contents
     * will not change. If this is not the case, subclasses should override this
     * method and return values that are unique to the item, independent of its
     * location in the tree.
     *
     * @param item
     *            the item
     * @return the unique identifier for the item
     */
    protected String idForItem(Object item)
    {
        return null;
    }


    //~ Static/instance variables .............................................

    private NSMutableDictionary<String, Object> itemLookupCache;
}
