/*==========================================================================*\
 |  $Id: WCTreeModelRepetition.java,v 1.2 2011/11/08 14:05:23 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011 Virginia Tech
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

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.appserver._private.WOShared;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSLog.Logger;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

//-------------------------------------------------------------------------
/**
 * A dynamic element similar to {@code WORepetition} that "flattens" a tree
 * model and provides iteration for its elements in a hierarchical manner.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2011/11/08 14:05:23 $
 */
public class WCTreeModelRepetition extends WODynamicGroup
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTreeModelRepetition(String aName, NSDictionary someAssociations,
            WOElement template)
    {
        super(null, null, template);

        this._treeModel = ((WOAssociation) someAssociations.objectForKey("treeModel"));
        this._item = ((WOAssociation) someAssociations.objectForKey("item"));
        this._index = ((WOAssociation) someAssociations.objectForKey("index"));
        this._indexPath = ((WOAssociation) someAssociations.objectForKey("indexPath"));

        if (this._treeModel == null)
            throw new WODynamicElementCreationException("<"
                    + super.getClass().getName()
                    + "> Missing 'treeModel' attribute.");
        if ((this._treeModel != null) && (this._item == null))
            throw new WODynamicElementCreationException("<"
                    + super.getClass().getName()
                    + "> Missing 'item' attribute with 'treeModel' attribute.");
        if ((this._item != null) && (!(this._item.isValueSettable())))
            throw new WODynamicElementCreationException("<"
                    + super.getClass().getName()
                    + "> Illegal read-only 'item' attribute.");
        if ((this._index != null) && (!(this._index.isValueSettable())))
            throw new WODynamicElementCreationException("<"
                    + super.getClass().getName()
                    + "> Illegal read-only 'index' attribute.");
        if ((this._indexPath != null) && (!(this._indexPath.isValueSettable())))
            throw new WODynamicElementCreationException("<"
                    + super.getClass().getName()
                    + "> Illegal read-only 'indexPath' attribute.");
    }


    // ----------------------------------------------------------
    public String toString()
    {
        return "<WCTreeModelRepetition iterator: "
                + ((this._treeModel != null) ? this._treeModel.toString() : "null")
                + " item: "
                + ((this._item != null) ? this._item.toString() : "null")
                + " index: "
                + ((this._index != null) ? this._index.toString() : "null")
                + " indexPath: "
                + ((this._indexPath != null) ? this._indexPath.toString() : "null")
                + ">";
    }


    // ----------------------------------------------------------
    public WCIndexPath _prepareForIterationWithIndex(int anIndex,
            WCTreeModel<?> model, Stack<TreePosition> treeStack,
            WCIndexPath lastIndexPath, WOContext aContext,
            WOComponent aComponent)
    {
        TreePosition newValue = null;
        newValue = treeStack.pop();

        if (this._item != null)
        {
            pushChildrenOfItem(model, newValue.item, newValue.indexPath,
                    treeStack);

            this._item._setValueNoValidation(newValue.item, aComponent);

            if (this._indexPath != null)
            {
                this._indexPath._setValueNoValidation(newValue.indexPath,
                        aComponent);
            }
        }

        if (this._index != null)
        {
            Integer aNumber = WOShared.unsignedIntNumber(anIndex);
            this._index._setValueNoValidation(aNumber, aComponent);
        }

        if (lastIndexPath != null)
        {
            _removeIndexPathFromElementID(aContext, lastIndexPath);
        }

        _appendIndexPathToElementID(aContext, newValue.indexPath);

        return newValue.indexPath;
    }


    // ----------------------------------------------------------
    private void _appendIndexPathToElementID(WOContext context,
            WCIndexPath indexPath)
    {
        context.appendElementIDComponent(
                indexPath.toString().replace('.', '_'));
    }


    // ----------------------------------------------------------
    private void _removeIndexPathFromElementID(WOContext context,
            WCIndexPath indexPath)
    {
        context.deleteLastElementIDComponent();
    }


    // ----------------------------------------------------------
    public void _cleanupAfterIteration(WOContext aContext,
            WCIndexPath lastIndexPath, WOComponent aComponent, int count)
    {
        if (this._item != null)
        {
            this._item._setValueNoValidation(null, aComponent);
        }

        if (this._indexPath != null)
        {
            this._indexPath._setValueNoValidation(null, aComponent);
        }

        if (this._index != null)
        {
            Integer aNumber = WOShared.unsignedIntNumber(count);
            this._index._setValueNoValidation(aNumber, aComponent);
        }

        if (lastIndexPath != null)
        {
            _removeIndexPathFromElementID(aContext, lastIndexPath);
        }
    }


    // ----------------------------------------------------------
    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext)
    {
        int anIndex = 0;
        WOComponent aComponent = aContext.component();
        WCTreeModel<?> aModel = null;

        if (this._treeModel != null)
        {
            Object aValue = this._treeModel.valueInComponent(aComponent);

            if (aValue instanceof WCTreeModel)
            {
                aModel = (WCTreeModel<?>) aValue;
            }
            else
            {
                throw new IllegalArgumentException(
                        "<"
                                + super.getClass().getName()
                                + "> Evaluating 'treeModel' binding returned a "
                                + aValue.getClass().getName()
                                + " when it should return a WCTreeModel.");
            }
        }

        Stack<TreePosition> stack = new Stack<TreePosition>();
        pushChildrenOfItem(aModel, null, new WCIndexPath(), stack);

        WCIndexPath lastIndexPath = null;
        while (!stack.isEmpty())
        {
            lastIndexPath = _prepareForIterationWithIndex(anIndex, aModel,
                    stack, lastIndexPath, aContext, aComponent);
            anIndex++;

            super.takeValuesFromRequest(aRequest, aContext);
        }

        _cleanupAfterIteration(aContext, lastIndexPath, aComponent, anIndex);
    }


    // ----------------------------------------------------------
    private static class TreePosition
    {
        public TreePosition(WCIndexPath indexPath, Object item)
        {
            this.indexPath = indexPath;
            this.item = item;
        }

        public WCIndexPath indexPath;
        public Object item;
    }


    // ----------------------------------------------------------
    private static void pushChildrenOfItem(WCTreeModel model, Object item,
            WCIndexPath indexPath, Stack<TreePosition> stack)
    {
        if (model.objectHasArrangedChildren(item))
        {
            NSArray children = model.arrangedChildrenOfObject(item);
            if (children != null)
            {
                for (int i = children.count() - 1; i >= 0; i--)
                {
                    stack.push(new TreePosition(
                            indexPath.indexPathByAddingIndex(i),
                            children.objectAtIndex(i)));
                }
            }
        }
    }


    // ----------------------------------------------------------
    static String _indexStringForSenderAndElement(String aSenderId,
            String anElementId)
    {
        String anIndexString = null;
        int aLength = anElementId.length();

        int aStartIndex = aLength + 1;
        int anEndIndex = aSenderId.indexOf('.', aStartIndex);

        if (anEndIndex < 0)
        {
            anIndexString = aSenderId.substring(aStartIndex);
        }
        else
        {
            anIndexString = aSenderId.substring(aStartIndex, anEndIndex);
        }

        return anIndexString;
    }


    // ----------------------------------------------------------
    static String _indexOfChosenItemForRequestInContext(WORequest aRequest,
            WOContext aContext)
    {
        String anIndexString = null;
        String aSenderId = aContext.senderID();
        String anElementId = aContext.elementID();
        if (aSenderId.startsWith(anElementId))
        {
            int anElementIdLength = anElementId.length();
            if ((aSenderId.length() > anElementIdLength)
                    && (aSenderId.charAt(anElementIdLength) == '.'))
            {
                anIndexString = _indexStringForSenderAndElement(aSenderId,
                        anElementId);
            }
        }
        return anIndexString;
    }


    // ----------------------------------------------------------
    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext)
    {
        WOComponent aComponent = aContext.component();
        WOActionResults aResponsePage = null;
        String anIndexString = _indexOfChosenItemForRequestInContext(aRequest,
                aContext);

        if (anIndexString != null)
        {
            WCIndexPath indexPath = new WCIndexPath(
                    anIndexString.replace('_', '.'));

            if (this._treeModel != null)
            {
                Object aValue = this._treeModel.valueInComponent(aComponent);
                Object item = null;
                if (aValue != null)
                {
                    if (aValue instanceof WCTreeModel)
                    {
                        WCTreeModel<?> aModel = (WCTreeModel<?>) aValue;
                        item = aModel.objectAtIndexPath(indexPath);
                    }
                    else
                    {
                        throw new IllegalArgumentException(
                                "<"
                                        + super.getClass().getName()
                                        + "> Evaluating 'treeModel' binding returned a "
                                        + aValue.getClass().getName()
                                        + " when it should return a WCTreeModel.");
                    }

                    if (this._item != null)
                    {
                        this._item._setValueNoValidation(item, aComponent);
                    }
                }
            }

/*            if (this._index != null)
            {
                Integer aNumber = WOShared.unsignedIntNumber(anIndex);
                this._index._setValueNoValidation(aNumber, aComponent);
            }*/

            aContext.appendElementIDComponent(anIndexString);
            aResponsePage = super.invokeAction(aRequest, aContext);
            aContext.deleteLastElementIDComponent();
        }
        else
        {
            WCTreeModel<?> aModel = null;
            int anIndex = 0;

            if (this._treeModel != null)
            {
                Object aValue = this._treeModel.valueInComponent(aComponent);
                if (aValue != null)
                {
                    if (aValue instanceof WCTreeModel)
                    {
                        aModel = (WCTreeModel<?>) aValue;
                    }
                    else
                    {
                        throw new IllegalArgumentException(
                                "<"
                                        + super.getClass().getName()
                                        + "> Evaluating 'treeModel' binding returned a "
                                        + aValue.getClass().getName()
                                        + " when it should return a WCTreeModel.");
                    }
                }
            }

            Stack<TreePosition> stack = new Stack<TreePosition>();
            pushChildrenOfItem(aModel, null, new WCIndexPath(), stack);

            WCIndexPath lastIndexPath = null;
            while (!stack.isEmpty() && aResponsePage == null)
            {
                lastIndexPath = _prepareForIterationWithIndex(anIndex, aModel,
                        stack, lastIndexPath, aContext, aComponent);
                anIndex++;

                aResponsePage = super.invokeAction(aRequest, aContext);
            }

            if (anIndex > 0)
            {
                _cleanupAfterIteration(aContext, lastIndexPath, aComponent,
                        anIndex);
            }
        }
        return aResponsePage;
    }


    // ----------------------------------------------------------
    public void appendToResponse(WOResponse aResponse, WOContext aContext)
    {
        int anIndex = 0;
        WOComponent aComponent = aContext.component();
        WCTreeModel<?> aModel = null;

        if (this._treeModel != null)
        {
            Object aValue = this._treeModel.valueInComponent(aComponent);
            if (aValue != null)
            {
                if (aValue instanceof WCTreeModel)
                {
                    aModel = (WCTreeModel<?>) aValue;
                }
                else
                {
                    throw new IllegalArgumentException(
                            "<"
                                    + super.getClass()
                                    + "> Evaluating 'treeModel' binding returned a "
                                    + aValue.getClass().getName()
                                    + " when it should return a WCTreeModel.");
                }
            }
        }

        Stack<TreePosition> stack = new Stack<TreePosition>();
        pushChildrenOfItem(aModel, null, new WCIndexPath(), stack);

        WCIndexPath lastIndexPath = null;
        while (!stack.isEmpty())
        {
            lastIndexPath = _prepareForIterationWithIndex(anIndex, aModel,
                    stack, lastIndexPath, aContext, aComponent);
            anIndex++;

            super.appendToResponse(aResponse, aContext);
        }

        _cleanupAfterIteration(aContext, lastIndexPath, aComponent, anIndex);
    }


    //~ Static/instance variables .............................................

    private WOAssociation _treeModel;
    private WOAssociation _item;
    private WOAssociation _index;
    private WOAssociation _indexPath;
}
