/*==========================================================================*\
 |  $Id: WCTree.java,v 1.5 2014/06/16 15:57:54 stedwar2 Exp $
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

import org.webcat.core.WCComponent;
import org.webcat.ui.generators.JavascriptFunction;
import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.ComponentIDGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.appserver.ERXWOContext;

//-------------------------------------------------------------------------
/**
 * <p>
 * A reusable table component that is bound to a display group, and provides
 * options to select rows, sort on table headings, and paging.
 * </p>
 * <dl>
 * <dt>id (<code>String</code>)</dt>
 * <dd>The widget id for the content pane that will surround the table. This
 * can be used to refresh the table contents in response to events that occur
 * outside the table.</dd>
 * <dt>treeModel (<code>WCTreeModel&lt;?&gt;</code>)</dt>
 * <dd>The tree model that contains the objects that this tree will display.
 * </dd>
 * <dt>settingsKey (<code>String</code>)</dt>
 * <dd>A key prefix that will be used to persist the table's settings in the
 * current user's preferences.</dd>
 * <dt>canSelectItems (<code>boolean</code>)</dt>
 * <dd>Indicates whether checkbox or radio button controls will be inserted in
 * front of each tree item's expansion controls to allow the user to select
 * the item.</dd>
 * <dt>multipleSelection (<code>boolean</code>)</dt>
 * <dd>Indicates whether multiple items can be selected at once (using
 * checkboxes) or if only one item can be selected at a time (using radio
 * buttons).</dd>
 * <dt>initialExpandDepth (<code>int</code>)</dt>
 * <dd>Specifies the number of levels that should be initially expanded in the
 * tree. 0 means expand nothing (just show the root nodes collapsed), 1 means
 * show the root nodes expanded, and so forth. -1 means expand everything.
 * </dd>
 * <dt>onSelectionChanged (<code>String</code>)</dt>
 * <dd>Javascript code that will be executed when the selection state in the
 * tree changes.</dd>
 * <dt>fixedSize ({@code String})</dt>
 * <dd>If specified, the tree will be displayed with the specified size, and
 * scrollbars will be added if the items require more space. This string should
 * be in the format {@code "width,height"}, where the width and height are
 * specified using CSS units. If this binding is omitted (or null), the tree
 * will auto-size to fit its contents.</dd>
 * </dl>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.5 $, $Date: 2014/06/16 15:57:54 $
 */
public class WCTree extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTree(WOContext context)
    {
        super(context);

        idFor = new ComponentIDGenerator(this);
    }


    //~ KVC attributes (must be public) .......................................

    public String id;

    public WCTreeModel<Object> treeModel;
    public String settingsKey;
    public boolean canSelectItems = false;
    public boolean multipleSelection = false;
    public int initialExpandDepth = 0;
    public String onSelectionChanged;
    public String selectionAction;
    public String itemDoubleClickedAction;
    public String fixedSize;
    public String shadowSelection;

    public ComponentIDGenerator idFor;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        WCTree oldTree = setCurrentTree(this);

        super.appendToResponse(response, context);

        setCurrentTree(oldTree);
    }


    // ----------------------------------------------------------
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        WCTree oldTree = setCurrentTree(this);

        super.takeValuesFromRequest(request, context);

        setCurrentTree(oldTree);
    }


    // ----------------------------------------------------------
    @Override
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        WCTree oldTree = setCurrentTree(this);

        WOActionResults result = super.invokeAction(request, context);

        setCurrentTree(oldTree);

        return result;
    }


    // ----------------------------------------------------------
    public String id()
    {
        if (id == null)
        {
            id = idFor.get();
        }

        return id;
    }


    // ----------------------------------------------------------
    public String cssStyleForContentPane()
    {
        StringBuffer buffer = new StringBuffer();

        if (fixedSize != null)
        {
            String[] parts = fixedSize.split(",");

            buffer.append("overflow: auto; ");
            buffer.append("width: ");
            buffer.append(parts[0]);
            buffer.append("; height: ");
            buffer.append(parts[1]);
            buffer.append("; white-space: nowrap;");
        }

        if (buffer.length() > 0)
        {
            return buffer.toString();
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public String cssClassForContentPane()
    {
        if (fixedSize != null)
        {
            return "tableborder";
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public String cssStyleForTable()
    {
        StringBuffer buffer = new StringBuffer();

        if (fixedSize != null)
        {
            buffer.append("width: 100%;");
        }

        if (buffer.length() > 0)
        {
            return buffer.toString();
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public String cssClassForTable()
    {
        if (fixedSize != null)
        {
            return "layout nomargin tree";
        }
        else
        {
            return "tree";
        }
    }


//    // ----------------------------------------------------------
//    public Map<Object, Boolean> expandedItems()
//    {
//        if (expandedItems == null)
//        {
//            retrieveExpansionState();
//        }
//
//        // If there's a selection, expand to make that visible too.
//
//
//        return expandedItems;
//    }


    // ----------------------------------------------------------
    public boolean isItemExpanded(Object item)
    {
        return treeModel.isObjectExpanded(item);
    }


    // ----------------------------------------------------------
    public void toggleItemExpanded(Object item)
    {
        treeModel.setObjectExpanded(item, !treeModel.isObjectExpanded(item));
//        storeExpansionState();
    }


    // ----------------------------------------------------------
//    private void retrieveExpansionState()
//    {
//        expandedItems = new HashMap<Object, Boolean>();
//
//        if (settingsKey != null)
//        {
//            String ids = (String) user().preferences().valueForKey(
//                    settingsKey + "_expandedItemIds");
//
//            if (ids != null)
//            {
//                String[] splitIds = ids.split("\0");
//
//                Map<String, Boolean> expandedItemIds =
//                    new HashMap<String, Boolean>();
//
//                for (int i = 0; i < splitIds.length; i += 2)
//                {
//                    String id = splitIds[i];
//                    boolean expanded = Boolean.parseBoolean(splitIds[i + 1]);
//
//                    if (id.length() > 0)
//                    {
//                        expandedItemIds.put(id, expanded);
//                    }
//                }
//
//                //recursivelyExpandChildren(null, 0, expandedItemIds);
//            }
//        }
//    }


    // ----------------------------------------------------------
//    private void recursivelyExpandChildren(Object item, int depth,
//                                           Map<String, Boolean> expandedItemIds)
//    {
//        if (treeModel.objectHasArrangedChildren(item))
//        {
//            NSArray<?> children = treeModel.arrangedChildrenOfObject(item);
//            if (children != null)
//            {
//                for (Object child : children)
//                {
//                    String childId = treeModel.persistentIdOfObject(child);
//
//                    if ((childId != null && expandedItemIds.containsKey(childId))
//                            || (!expandedItemIds.containsKey(childId)
//                                    && (initialExpandDepth == -1
//                                            || depth < initialExpandDepth)))
//                    {
//                        expandedItems.put(child,
//                                expandedItemIds.get(childId));
//                        recursivelyExpandChildren(child, depth + 1, expandedItemIds);
//                    }
//                }
//            }
//        }
//    }


    // ----------------------------------------------------------
//    private void storeExpansionState()
//    {
//        if (settingsKey != null)
//        {
//            StringBuffer buffer = new StringBuffer();
//            for (Object item : expandedItems.keySet())
//            {
//                boolean expanded = expandedItems.get(item);
//                String id = treeModel.persistentIdOfObject(item);
//
//                if (id != null)
//                {
//                    buffer.append(id);
//                    buffer.append('\0');
//                    buffer.append(Boolean.toString(expanded));
//                    buffer.append('\0');
//                }
//            }
//            String expandedItemIdsString = buffer.toString();
//
//            user().preferences().takeValueForKey(
//                    expandedItemIdsString, settingsKey + "_expandedItemIds");
//            user().savePreferences();
//        }
//    }


    // ----------------------------------------------------------
    public void updateSelectionFromShadowField()
    {
        String[] parts = shadowSelection.split(",");

        treeModel.clearSelection();

        for (String item : parts)
        {
            item = item.substring(
                    idFor.get("path_").length()).replace('_', '.');
            WCIndexPath path = new WCIndexPath(item);
            treeModel.selectObject(treeModel.objectAtIndexPath(path));
        }
    }


    // ----------------------------------------------------------
    public WOActionResults singleSelectionChanged()
    {
        updateSelectionFromShadowField();

        if (selectionAction != null)
        {
            return performParentAction(selectionAction);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public WOActionResults itemDoubleClicked()
    {
        updateSelectionFromShadowField();

        if (itemDoubleClickedAction != null)
        {
            return performParentAction(itemDoubleClickedAction);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public static void refresh(JavascriptGenerator js, String id)
    {
        refresh(js, id, null);
    }


    // ----------------------------------------------------------
    public static void refresh(JavascriptGenerator js, String id,
            JavascriptFunction onAfterRefresh)
    {
        js.append(renderTableBusyScript(id));
        js.refresh(onAfterRefresh, id);
    }


    // ----------------------------------------------------------
    public JavascriptGenerator refreshTable()
    {
        return refreshTable(null);
    }


    // ----------------------------------------------------------
    public JavascriptGenerator refreshTable(JavascriptFunction onAfterRefresh)
    {
        if (onAfterRefresh == null)
        {
            return new JavascriptGenerator().refresh(id());
        }
        else
        {
            return new JavascriptGenerator().refresh(onAfterRefresh, id());
        }
    }


    // ----------------------------------------------------------
    public String tableBodyId()
    {
        return id() + "__tbody";
    }


    // ----------------------------------------------------------
    public static String renderTableBusyScript(String gridId)
    {
        NSMutableDictionary<String, Object> props =
            new NSMutableDictionary<String, Object>();
        props.setObjectForKey(0.25, "opacity");

        JavascriptGenerator js = new JavascriptGenerator();
        // TODO change this id to be more general
        js.animateProperty(gridId + "__tbody", props, 250, null).play();
        return js.toString(true);
    }


    // ----------------------------------------------------------
    // TODO rewrite this function to use the one above
    public String renderTableBusyScript()
    {
        NSMutableDictionary<String, Object> props =
            new NSMutableDictionary<String, Object>();
        props.setObjectForKey(0.25, "opacity");

        JavascriptGenerator js = new JavascriptGenerator();
        js.animateProperty(tableBodyId(), props, 250, null).play();
        return js.toString(true);
    }


    // ----------------------------------------------------------
    public static WCTree currentTree()
    {
        return (WCTree) ERXWOContext.contextDictionary().objectForKey(
                CURRENT_TREE_KEY);
    }


    // ----------------------------------------------------------
    public static WCTree setCurrentTree(WCTree tree)
    {
        WCTree oldTree =
            (WCTree) ERXWOContext.contextDictionary().objectForKey(
                CURRENT_TREE_KEY);

        if (tree == null)
        {
            ERXWOContext.contextDictionary().removeObjectForKey(
                    CURRENT_TREE_KEY);
        }
        else
        {
            ERXWOContext.contextDictionary().setObjectForKey(tree,
                    CURRENT_TREE_KEY);
        }

        return oldTree;
    }


    // ----------------------------------------------------------
    public int numberOfColumns()
    {
        return numberOfColumns;
    }


    // ----------------------------------------------------------
    public String passthroughAttributes()
    {
        return passthroughAttributes;
    }


    // ----------------------------------------------------------
    public void handleTakeValueForUnboundKey(Object value, String key)
    {
        if (passthroughAttributes == null)
        {
            passthroughAttributes = "";
        }

        if (value != null)
        {
            passthroughAttributes += " " + key + "=\""
                + WOMessage.stringByEscapingHTMLAttributeValue(
                        value.toString()) + "\"";
        }
    }


    //~ Static/instance variables .............................................

    private static final String CURRENT_TREE_KEY =
        "org.webcat.ui.WCTree.currentTree";

    private String passthroughAttributes;

    //private Map<Object, Boolean> expandedItems;
    //private boolean expandedItemsCalled;

    protected int numberOfColumns = 0;
}
