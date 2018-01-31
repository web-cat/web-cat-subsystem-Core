/*==========================================================================*\
 |  $Id: WCTreeModel.java,v 1.6 2014/06/16 15:57:54 stedwar2 Exp $
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

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;
import er.extensions.eof.ERXQ;
import er.extensions.eof.ERXS;

//-------------------------------------------------------------------------
/**
 * The {@code WCTreeModel} class represents the data model used by the
 * {@link WCTree} component. It is roughly a hierarchical analogue to
 * {@code WODisplayGroup} (but more closely related to Apple's
 * {@code NSTreeController} from Cocoa), providing functionality such as
 * maintaining the selection state and the sort ordering of the tree.
 *
 * @param <T> the type of elements contained in the tree model. There is no
 *     requirement that the tree model be homogeneous, so {@code Object} is
 *     an acceptable parameter to easily get a heterogeneous model; however,
 *     users who extend this class will have to perform the necessary typecasts
 *     to retrieve the children of those objects based on the actual type
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.6 $, $Date: 2014/06/16 15:57:54 $
 */
public abstract class WCTreeModel<T> implements NSKeyValueCodingAdditions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the {@code WCTreeModel} class.
     */
    public WCTreeModel()
    {
        cachedArrangedChildren = new HashMap<T, NSArray<T>>();
        selectedObjects = new NSMutableSet<T>();
        sortOrderings = null;
        qualifier = null;
        expandedItems = new HashMap<String, Boolean>();
        cachedParentRelationship = new HashMap<T, T>();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * <p>
     * Gets the children of the specified object.
     * </p><p>
     * Clients must override this method to return the children in their
     * natural order, irrespective of any filtering or sorting that is set on
     * the model. To retrieve the array of sorted and filtered children (as
     * the {@code WCTree} component does when it displays the content), one
     * should call {@link #arrangedChildrenOfObject(T)} instead.
     * </p>
     *
     * @param object the object whose children should be retrieved, or null to
     *     retrieve the root objects
     * @return the children of the object. If the object is a leaf, this
     *     method may return either null or an empty array
     */
    public abstract NSArray<T> childrenOfObject(T object);


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets a value indicating whether a node has children, after any qualifier
     * filtering has been applied. This method is used by the tree component to
     * determine if an expansion arrow should be displayed for a particular
     * item.
     * </p><p>
     * By default, this method simply calls {@link #arrangedChildrenOfObject(Object)}
     * and returns true if the number of children is greater than 0. In
     * situations where determining the existence of children can be much
     * faster than computing the children themselves, subclasses should
     * override this method to provide a faster implementation.
     * </p>
     *
     * @param object the object to check for children
     * @return true if the object has children, otherwise false
     */
    public boolean objectHasArrangedChildren(T object)
    {
        NSArray<T> children = arrangedChildrenOfObject(object);
        return (children != null && children.count() > 0);
    }


    // ----------------------------------------------------------
    public T childWithPathComponent(T object, String component)
    {
        if (component != null)
        {
            for (T child : childrenOfObject(object))
            {
                if (component.equals(persistentIdOfObject(child)))
                {
                    return child;
                }
            }
        }
        return null;
    }


    // ----------------------------------------------------------
    public String pathForObject(T object)
    {
        String path = persistentIdOfObject(object);
        if (path == null)
        {
            path = "null";
        }

        T parent = parentOf(object);
        if (parent != null)
        {
            path = pathForObject(parent) + "/" + path;
        }
        return path;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets the children of the specified object, filtered according to the
     * model's qualifier and sorted according to the model's sort orderings.
     * </p><p>
     * Clients should not need to override this method, but rather the
     * abstract {@link #childrenOfObject(T)} method instead.
     * </p>
     *
     * @param object the object whose children should be retrieved, or null to
     *     retrieve the root items
     * @return the sorted and filtered array of children of the object
     */
    public NSArray<T> arrangedChildrenOfObject(T object)
    {
        NSArray<T> arrangedChildren;

        if (cachedArrangedChildren.containsKey(object))
        {
            arrangedChildren = cachedArrangedChildren.get(object);
        }
        else
        {
            arrangedChildren = childrenOfObject(object);
            if (arrangedChildren != null)
            {
                // Save parent info in cache
                for (T child : arrangedChildren)
                {
                    cachedParentRelationship.put(child, object);
                }

                if (qualifier != null)
                {
                    arrangedChildren = ERXQ.filtered(
                            arrangedChildren, qualifier);
                }

                if (sortOrderings != null)
                {
                    arrangedChildren = ERXS.sorted(
                            arrangedChildren, sortOrderings);
                }
            }

            cachedArrangedChildren.put(object, arrangedChildren);
        }

        return arrangedChildren;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Use this method to trigger reordering of the model's contents.
     * </p><p>
     * This method is automatically called by other {@code WCTreeModel}
     * methods that would affect the ordering of the objects in the model,
     * such as {@link #setSortOrderings(NSArray)} and
     * {@link #setQualifier(EOQualifier)}. Clients should invoke it directly
     * if an external event would force the objects in the model to change.
     * </p>
     */
    public void rearrangeObjects()
    {
        cachedArrangedChildren.clear();
        cachedParentRelationship.clear();
    }


    // ----------------------------------------------------------
    /**
     * Gets the sort orderings associated with the model.
     *
     * @return an {@code NSArray} of sort orderings
     */
    public NSArray<EOSortOrdering> sortOrderings()
    {
        return sortOrderings;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Sets the sort orderings associated with the model.
     * </p><p>
     * Since the same sort orderings are applied at every level of the tree,
     * using this property only makes sense if the tree contains homogeneous
     * data (or at least data that has a common set of key paths).
     * </p>
     *
     * @param orderings an {@code NSArray} of sort orderings
     */
    public void setSortOrderings(NSArray<EOSortOrdering> orderings)
    {
        sortOrderings = orderings;
        rearrangeObjects();
    }


    // ----------------------------------------------------------
    /**
     * Gets the qualifier used to filter the items in the model.
     *
     * @return an {@code EOQualifier} used to filter the items in the model
     */
    public EOQualifier qualifier()
    {
        return qualifier;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Sets the qualifier used to filter the items in the model. Only items
     * that satisfy the qualifier will be included in the array returned by
     * {@link #arrangedChildrenOfObject(T)}.
     * </p><p>
     * The {@link WCTree} component currently calls
     * {@link #arrangedChildrenOfObject(T)} in such a way that this qualifier
     * would be applied top-down. This means that child items that match the
     * qualifier will only appear if all of their ancestors do as well. It
     * may be desirable to change this behavior in the future, for example, by
     * displaying the search results as a flat list that traverses the entire
     * tree.
     * </p><p>
     * Since the same qualifier is applied at every level of the tree, using
     * this property only makes sense if the tree contains homogeneous data
     * (or at least data that has a common set of key paths).
     * </p>
     *
     * @param aQualifier an {@code EOQualifier} used to filter the items in
     *     the model
     */
    public void setQualifier(EOQualifier aQualifier)
    {
        qualifier = aQualifier;
        rearrangeObjects();
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets a unique identifier for the specified object. This identifier is
     * used to persist the expansion state of the tree in the user's
     * preferences.
     * </p><p>
     * By default, the method use the ID of an EO, or the hashCode of
     * an object, together with its simple class name, to generate a
     * persistent ID.  If you wish to use this tree model
     * with an item type where hash codes are not persistent (or may
     * collide), then you must override this method in your subclass.
     * </p>
     *
     * @param object the object
     * @return a String representing the unique identifier for the object
     */
    public String persistentIdOfObject(T object)
    {
        if (object instanceof EOGenericRecord)
        {
            EOGenericRecord record = (EOGenericRecord) object;
            return record.entityName() + "_"
                + record.valueForKey("id");
        }
        else
        {
            // Won't work for objects that don't define hashCode properly,
            // so models that use such objects should override this
            // method.
            return object.getClass().getSimpleName() + "_" + object.hashCode();
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the object in the model that is located at the specified index path,
     * starting from the root.
     *
     * @param indexPath the index path of the object to retrieve
     * @return the object located at the index path, or null if none was found
     */
    public T objectAtIndexPath(WCIndexPath indexPath)
    {
        T object = null;

        for (int index : indexPath.indices())
        {
            NSArray<? extends T> children = childrenOfObject(object);

            if (children != null
                    && children.count() > 0 && index < children.count())
            {
                object = children.objectAtIndex(index);
            }
            else
            {
                return null;
            }
        }

        return object;
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether or not the specified object is allowed
     * to be selected. By default, all items can be selected, but this method
     * can be overridden to limit this. For example, a file picker may only
     * allow choosing items that represent files and not directories.
     *
     * @param anObject the object
     * @return true if the object can be selected, otherwise false
     */
    public boolean canSelectObject(T anObject)
    {
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Gets the currently selected objects in the model.
     *
     * @return an NSSet containing the currently selected objects
     */
    public NSSet<T> selectedObjects()
    {
        return selectedObjects;
    }


    // ----------------------------------------------------------
    /**
     * Changes the model's {@code selectedObjects} to a set containing only
     * {@code anObject}.
     *
     * @param anObject the object to select
     */
    public void setSelectedObject(T anObject)
    {
        if (anObject != null && canSelectObject(anObject))
        {
            selectedObjects = new NSMutableSet<T>(anObject);
        }
        else
        {
            selectedObjects.removeAllObjects();
        }

        _selectionDidChange();
    }


    // ----------------------------------------------------------
    /**
     * Changes the model's {@code selectedObjects} to a set containing only
     * {@code objects}.
     *
     * @param objects the objects to select
     */
    public void setSelectedObjects(NSSet<T> objects)
    {
        selectedObjects = new NSMutableSet<T>();
        boolean changed = false;

        for (T object : objects)
        {
            if (canSelectObject(object))
            {
                selectedObjects.addObject(object);
                changed = true;
            }
        }

        if (changed)
        {
            _selectionDidChange();
        }
    }


    // ----------------------------------------------------------
    /**
     * Adds {@code anObject} to the model's current selection.
     *
     * @param anObject the object to add to the selection
     */
    public void selectObject(T anObject)
    {
        if (canSelectObject(anObject))
        {
            selectedObjects.addObject(anObject);
            _selectionDidChange();
        }
    }


    // ----------------------------------------------------------
    /**
     * Removes {@code anObject} from the model's current selection.
     *
     * @param anObject the object to remove from the selection
     */
    public void deselectObject(T anObject)
    {
        selectedObjects.removeObject(anObject);
        _selectionDidChange();
    }


    // ----------------------------------------------------------
    /**
     * Adds {@code objects} to the model's current selection.
     *
     * @param objects the object to add to the selection
     */
    public void selectObjects(NSSet<T> objects)
    {
        boolean changed = false;

        for (T object : objects)
        {
            if (canSelectObject(object))
            {
                selectedObjects.addObject(object);
                changed = true;
            }
        }

        if (changed)
        {
            _selectionDidChange();
        }
    }


    // ----------------------------------------------------------
    /**
     * Removes {@code objects} from the model's current selection.
     *
     * @param objects the object to remove from the selection
     */
    public void deselectObjects(NSSet<T> objects)
    {
        selectedObjects.subtractSet(objects);
        _selectionDidChange();
    }


    // ----------------------------------------------------------
    /**
     * Clears the current selection in the model.
     */
    public void clearSelection()
    {
        selectedObjects.removeAllObjects();
        _selectionDidChange();
    }


    // ----------------------------------------------------------
    private void _selectionDidChange()
    {
        // Make sure the current selection is always expanded.

        for (T object : selectedObjects())
        {
            String pathToSelectedObject = pathForObject(object);

            if (pathToSelectedObject != null)
            {
                String[] components = pathToSelectedObject.split("/");

                T current = null;

                for (String component : components)
                {
                    current = childWithPathComponent(current, component);

                    if (current == null)
                    {
                        break;
                    }

                    String path = pathForObject(current);
                    if (!expandedItems.containsKey(path))
                    {
                        expandedItems.put(path, true);
                    }
                }
            }
        }

        selectionDidChange();
    }


    // ----------------------------------------------------------
    /**
     * A hook that subclasses can use to be notified when the tree selection
     * changes. This should be used when it is necessary to keep other data
     * structures synchronized with the selection after a page submit.
     */
    protected void selectionDidChange()
    {
        // Do nothing; subclasses can override.
    }


    // ----------------------------------------------------------
    public boolean isObjectExpanded(T object)
    {
        if (object != null)
        {
            String path = pathForObject(object);
            return Boolean.TRUE.equals(expandedItems.get(path));
        }
        else
        {
            return true;
        }
    }


    // ----------------------------------------------------------
    public void setObjectExpanded(T object, boolean expanded)
    {
        if (object != null)
        {
            String path = pathForObject(object);
            if (expanded)
            {
                expandedItems.put(path, Boolean.TRUE);
            }
            else
            {
                expandedItems.remove(path);
            }
        }
    }


    //~ KVC support implementation ............................................

    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }


    // ----------------------------------------------------------
    public void takeValueForKey(Object value, String key)
    {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(
                this, value, key);
    }


    // ----------------------------------------------------------
    public Object valueForKeyPath(String keyPath)
    {
        return NSKeyValueCodingAdditions.DefaultImplementation
            .valueForKeyPath(this, keyPath);
    }


    // ----------------------------------------------------------
    public void takeValueForKeyPath(Object value, String keyPath)
    {
        NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(
                this, value, keyPath);
    }


    // ----------------------------------------------------------
    private T parentOf(T item)
    {
        T result = cachedParentRelationship.get(item);
        if (result == null)
        {
            if (!cachedParentRelationship.containsKey(item))
            {
                log.warn("tree model " + this + " does not contain "
                    + "parent info for item " + item);
            }
        }
        return result;
    }


    //~ Static/instance variables .............................................

    /* The cache used to maintain the lists of arranged children for each
       node. */
    private Map<T, NSArray<T>> cachedArrangedChildren;

    /* The currently selected objects in the tree model. */
    private NSMutableSet<T> selectedObjects;

    /* The sort orderings that define how to sort the elements at each level
       of the tree. */
    private NSArray<EOSortOrdering> sortOrderings;

    /* A qualifier used to filter the displayed items in the tree. */
    private EOQualifier qualifier;

    private Map<String, Boolean> expandedItems;
    private Map<T, T> cachedParentRelationship;

    static Logger log = Logger.getLogger(WCTreeModel.class);
}
