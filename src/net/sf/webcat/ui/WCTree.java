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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.json.JSONArray;
import org.json.JSONException;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import er.extensions.foundation.ERXStringUtilities;

// ------------------------------------------------------------------------
/**
 * A tree widget that gets its data from an instance of
 * {@link AbstractTreeModel}.
 * <p>
 * Component content inside the instance of the {@code Tree} component will be
 * passed along into the element content of the underlying Dojo {@code Tree}
 * element. This makes it possible to override {@code dijit.Tree} methods by
 * including {@code dojo/method} script blocks inside the component content.
 * <p>
 * {@code Tree} automatically overrides the Dojo tree widget's {@code onClick}
 * event in order to pass these events along to the parent component. Because of
 * this, the {@code Tree} component exposes two new JavaScript events of its
 * own: {@code onPreClick} and {@code onPostClick}. When an item in the tree is
 * clicked, the following actions occur:
 * <ol>
 * <li>The {@code onPreClick} method is called, if it exists. This event takes
 * the tree item as an argument and returns a boolean value indicating whether
 * processing should continue. If {@code false}, processing stops here. If
 * {@code true}:
 * <li>If the parent component (the one that contains the instance of
 * {@code Tree}) implements the method whose name is given in the
 * {@code onClickMethodName} binding (which defaults to "treeItemClicked"),
 * then it is called. It takes two arguments: the JavaScript id of the tree
 * component (a String), and the data model item that was clicked (an Object).
 * This method can also optionally return a value, which is used below.
 * <li>The {@code onPostClick} method is called, if it exists. This event takes
 * the tree item as its first argument and the value returned by the method
 * named in {@code onClickMethodName} in the previous step, if any, as the
 * second argument. (If this method was of type {@code void}, then this second
 * argument is null.)
 * </ol>
 * 
 * <h2>Bindings</h2>
 * <table>
 * <tr>
 * <td>{@code cookieName}</td>
 * <td>If {@code persist} is true, the name of the cookie to store the
 * expanded/collapsed state of the nodes in the tree.</td>
 * </tr>
 * <tr>
 * <td>{@code cssClass}</td>
 * <td>The CSS class to apply to the tree widget</td>
 * </tr>
 * <tr>
 * <td>{@code decorators}</td>
 * <td>An array of objects that implement {@link IItemDecorator}, which are used
 * to associate custom properties which the JavaScript representation of each
 * item in the tree.</td>
 * </tr>
 * <tr>
 * <td>{@code id}</td>
 * <td>The DOM id to assign to the tree widget</td>
 * </tr>
 * <td>{@code model}</td>
 * <td>An instance of {@code AbstractTreeModel} from which the tree will obtain
 * its contents.</td>
 * </tr>
 * <tr>
 * <td>{@code onClickMethodName}</td>
 * <td>The name of a method implemented by the parent component that will be
 * called when an item in the tree is clicked. If not provided, no method on
 * the server-side component will be called.</td>
 * </tr>
 * <tr>
 * <td>{@code openOnClick}</td>
 * <td>A boolean value indicating whether elements should expand or collapse
 * when their title is clicked, rather than just when the +/- button is clicked.
 * If true, {@code onClick} will not be called. The default value is false.</td>
 * </tr>
 * <tr>
 * <td>{@code persist}</td>
 * <td>A boolean value indicating whether a cookie should be used to keep track
 * of the expanded/collapsed state of the nodes in the tree. The default value
 * is true.</td>
 * </tr>
 * <tr>
 * <td>{@code rootLabel}</td>
 * <td>If {@code showRoot} is true, the label to be assigned to the top-level
 * root node.</td>
 * </tr>
 * <tr>
 * <td>{@code showRoot}</td>
 * <td>A boolean value indicating whether the root item in the tree is
 * displayed. The default value is true.</td>
 * </tr>
 * <tr>
 * <td>{@code style}</td>
 * <td>The CSS styles to apply to the tree widget</td>
 * </tr>
 * </table>
 * 
 * <h2>Events</h2>
 * <table>
 * <tr>
 * <td>{@code boolean preOnClick(item)}</td>
 * <td>Called when an item in the tree is clicked, but before the server is
 * notified. Returning {@code false} from this method will prevent the server
 * from being notified.</td>
 * </tr>
 * <tr>
 * <td>{@code void postOnClick(item, serverResult)}</td>
 * <td>Called when an item in the tree is clicked, after the server is
 * notified. The second argument to this method is the value that was returned
 * by the component on the server, if any.</td>
 * </tr>
 * </table>
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class WCTree extends WOComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new TreeComponent.
     * 
     * @param context
     *            the context
     */
    public WCTree(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public String id;
    public String cssClass;
    public String style;
    public String cookieName;
    public AbstractTreeModel model;
    public NSArray<IItemDecorator> decorators;
    public String onClickMethodName;
    public Boolean openOnClick;
    public String otherTagString;
    public Boolean persist;
    public String rootLabel;
    public Boolean showRoot;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (id == null)
        {
            id = ERXStringUtilities.safeIdentifierName(context.elementID());
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    /**
     * The DOM identifier of the tree widget.
     * 
     * @return the DOM identifier of the tree widget
     */
    public String id()
    {
        return id;
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
        return "__JSONBridge_" + id();
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
        return "tree";
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
    /**
     * The JavaScript identifier of the tree model embedded inside this
     * component.
     * 
     * @return the JavaScript identifier of the tree model
     */
    public String modelJSId()
    {
        return "__treeModel_" + id();
    }


    // ----------------------------------------------------------
    /**
     * Shadow method for the method of the same name in
     * {@link AbstractTreeModel}. Subclasses can override this to attach any
     * decorators that they need to assign extra attributes to the item
     * representations.
     * 
     * @param itemId
     * @return a JSONArray containing the children of the item with the
     *     specified identifier
     * @throws JSONException
     */
    public JSONArray _childrenOfItemWithId(String itemId)
    throws JSONException
    {
        return model._jsDataForChildrenOfItemWithId(
                itemId, decorators);
    }


    // ----------------------------------------------------------
    /**
     * Called from the JavaScript side when the tree widget's {@code onClick}
     * event fires. This method passes control along to the parent component if
     * it implements the {@code treeComponentItemClicked(String, Object)}
     * method.
     * 
     * @param itemId
     *            the unique identifier of the item that was clicked
     * @return an arbitrary value to pass back to JavaScript from the server
     * 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     * @throws InvocationTargetException 
     */
    public Object handleOnClick(String itemId) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException
    {
        if (onClickMethodName == null)
        {
            return null;
        }
        
        Object item = model.itemForId(itemId);

        try
        {
            Method method = parent().getClass().getMethod(
                    onClickMethodName, String.class, Object.class);

            return method.invoke(parent(), id(), item);
        }
        catch (NoSuchMethodException e)
        {
            // Parent component does not implement the delegate method;
            // do nothing.

            return null;
        }
    }

    
    //~ Static/instance variables .............................................

    private static final long serialVersionUID = 1L;
}
