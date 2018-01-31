/*==========================================================================*\
 |  $Id: TabDescriptor.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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

import org.apache.log4j.Logger;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;
import er.extensions.foundation.ERXValueUtilities;

// -------------------------------------------------------------------------
/**
 *  A class used to describe individual tabs in a hierarchical navigation
 *  scheme.  Note that although such a niavigational element may nominally
 *  be called a "tab", it may be rendered in different ways depending on
 *  what level it is at in hierarchical navigation.
 *
 *  @author Stephen Edwards
 *  @author Last changed by $Author: aallowat $
 *  @version $Revision: 1.1 $, $Date: 2010/05/11 14:51:55 $
 */
public class TabDescriptor
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Constructs a new <code>TabDescriptor</code> object.
     *
     * @param pageName    The class name for the page represented by this tab
     * @param label       The text label (description) associated with this tab
     * @param accessLevel The accessLevel needed to access this tab
     * @param priority    The order priority for this tab (larger numbers come
     *                    later in the sequence of sibling tabs)
     * @param wantsStart  True if this tab wants to be the default starting
     *                    subtab for its parent
     * @param children    An array of subtab elements
     * @param id          A unique identifier for this tab (optional)
     * @param config      An optional dictionary of configuration parameters;
     *                    if the actual is mutable, it will be taken and owned;
     *                    if not, a mutable clone will be created
     */
    public TabDescriptor( String  pageName,
                          String  label,
                          int     accessLevel,
                          int     priority,
                          boolean wantsStart,
                          NSArray<TabDescriptor> children,
                          String  id,
                          NSDictionary<String, Object> config
                        )
    {
        this.pageName    = pageName;
        this.label       = label;
        this.accessLevel = accessLevel;
        this.priority    = priority;
        this.wantsStart  = wantsStart;
        this.children    = new NSMutableArray<TabDescriptor>();
        this.id          = id;
        if (config != null)
        {
            if (config instanceof NSMutableDictionary)
            {
                this.config = (NSMutableDictionary<String, Object>)config;
            }
            else
            {
                this.config = config.mutableClone();
            }
        }
        if (children != null)
        {
            addChildren(children);
        }
        // log.debug( "created, before selection:" + this );
        // selectDefault();
        // log.debug( "created, after selection:" + this );
    }


    // ----------------------------------------------------------
    /**
     * Constructs a new <code>TabDescriptor</code> object.
     *
     * @param pageName    The class name for the page represented by this tab
     * @param label       The text label (description) associated with this tab
     * @param accessLevel The accessLevel needed to access this tab
     * @param priority    The order priority for this tab (larger numbers come
     *                    later in the sequence of sibling tabs)
     * @param wantsStart  True if this tab wants to be the default starting
     *                    subtab for its parent
     */
    public TabDescriptor( String  pageName,
                          String  label,
                          int     accessLevel,
                          int     priority,
                          boolean wantsStart
                        )
    {
        this( pageName, label, accessLevel, priority, wantsStart,
              null, null, null );
    }


    // ----------------------------------------------------------
    /**
     * Constructs a new <code>TabDescriptor</code> object.
     *
     * @param pageName    The class name for the page represented by this tab
     * @param label       The text label (description) associated with this tab
     * @param accessLevel The accessLevel needed to access this tab
     * @param priority    The order priority for this tab (larger numbers come
     *                    later in the sequence of sibling tabs)
     */
    public TabDescriptor( String pageName,
                          String label,
                          int    accessLevel,
                          int    priority
                        )
    {
        this( pageName, label, accessLevel, priority, false,
              null, null, null );
    }


    // ----------------------------------------------------------
    /**
     * Constructs a new <code>TabDescriptor</code> object.
     *
     * @param pageName    The class name for the page represented by this tab
     * @param label       The text label (description) associated with this tab
     * @param accessLevel The accessLevel needed to access this tab
     */
    public TabDescriptor( String pageName,
                          String label,
                          int    accessLevel
                        )
    {
        this( pageName, label, accessLevel, 1000, false,
              null, null, null );
    }


    // ----------------------------------------------------------
    /**
     * Constructs a new <code>TabDescriptor</code> object.
     *
     * @param pageName    The class name for the page represented by this tab
     * @param label       The text label (description) associated with this tab
     */
    public TabDescriptor( String pageName,
                          String label
                        )
    {
        this( pageName, label, 0, 1000, false,
              null, null, null );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Retrieve the class name for the page this tab represents.
     *
     * @return The page class name
     */
    public String pageName()
    {
        return pageName;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the class name for the page of the currently selected
     * descendant tab.
     *
     * @return The page class name
     */
    public String selectedPageName()
    {
        return selectedDescendant().pageName();
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the text label to use when rendering this tab.
     *
     * @return The tab label
     */
    public String label()
    {
        return label;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the sentence-case version of the text label to use when
     * rendering this tab.
     *
     * @return The sentence-case tab label (only first char capitalized)
     */
    public String lcLabel()
    {
        if ( lcLabel == null )
        {
            lcLabel = lowerCaseAfterFirst( label() );
        }
        return lcLabel;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the accessLevel a user must have to use this tab.
     *
     * @return The tab access level
     */
    public int accessLevel()
    {
        return accessLevel;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the priority for ordering this tab.
     *
     * @return The tab priority
     */
    public int priority()
    {
        return priority;
    }


    // ----------------------------------------------------------
    /**
     * Determine whether this tab wants to be the default tab for its parent.
     *
     * @return True if this tab wants to be the default tab for parent
     */
    public boolean wantsStart()
    {
        return wantsStart;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the list of children possessed by this tab.
     *
     * @return The tab label
     */
    public NSMutableArray<TabDescriptor> children()
    {
        return children;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the ID for the page this tab represents.
     *
     * @return The page ID
     */
    public String id()
    {
        return id;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the config settings dictionary for this tab.
     *
     * @return The dictionary of config settings (possibly null)
     */
    public NSMutableDictionary<String, Object> config()
    {
        return config;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve this tab's CSS class, based on its accessLevel.
     *
     * @return The CSS class associated with this tab
     */
    public String cssClass()
    {
        if ( accessLevel < 25 )
            return "user";
        else if ( accessLevel < 75 )
            return "staff";
        else
            return "admin";
    }


    // ----------------------------------------------------------
    /**
     * This operation does nothing, since the cssClass is determined by the
     * accessLevel.  It is provided only to prevent certain KVC exceptions
     * from arising because of how this attribute is typically bound in pages.
     * @param value ignored
     */
    public void setCssClass( String value )
    {
        // Does nothing
    }


    // ----------------------------------------------------------
    /**
     * Find out if this tab is the currently selected tab.
     *
     * @return True if this tab is selected
     */
    public boolean isSelected()
    {
        return isSelected;
    }


    // ----------------------------------------------------------
    /**
     * Find out if this subtab has a sibling before it.
     *
     * @return True if this tab has a previous sibling
     */
    public boolean hasPreviousSibling()
    {
        return ( parent != null && myIndex > 0 );
    }


    // ----------------------------------------------------------
    /**
     * Retrieve this subtab's previous sibling, if any.
     *
     * @return The previous sibling
     */
    public TabDescriptor previousSibling()
    {
        return hasPreviousSibling()
            ? parent.children.objectAtIndex(myIndex - 1)
            : null;
    }


    // ----------------------------------------------------------
    /**
     * Find out if this subtab has a sibling after it.
     *
     * @return True if this tab has a next sibling
     */
    public boolean hasNextSibling()
    {
        return ( parent != null && myIndex < parent.children.count() - 1 );
    }


    // ----------------------------------------------------------
    /**
     * Retrieve this subtab's next sibling, if any.
     *
     * @return The next sibling
     */
    public TabDescriptor nextSibling()
    {
        return hasNextSibling()
            ? parent.children.objectAtIndex(myIndex + 1)
            : null;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve a subtab by position.
     *
     * @param pos The index of the child to retrieve
     * @return The child tab at the specified position
     */
    public TabDescriptor childAt(int pos)
    {
        if (pos >= 0 && pos < children.count())
        {
            return children.objectAtIndex(pos);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Retrieve a subtab's parent.
     * @return The parent tab of this tab
     */
    public TabDescriptor parent()
    {
        return parent;
    }


    // ----------------------------------------------------------
    /**
     * Access the position of the currently selected subtab.
     *
     * @return The selected child tab's index, or -1 if none is selected
     */
    public int selectedChildIndex()
    {
        return getSelectedChild();
    }


    // ----------------------------------------------------------
    /**
     * Access the currently selected subtab.
     *
     * @return The child tab that is selected
     */
    public TabDescriptor selectedChild()
    {
        TabDescriptor result = childAt( getSelectedChild() );
//        if ( result == null )
//        {
//            log.error( "null selected child",
//                       new RuntimeException( "here") );
//            log.error( "tab = " + this );
//        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Access the currently selected subtab.
     *
     * @return The child tab that is selected
     */
    public TabDescriptor selectedDescendant()
    {
        TabDescriptor chosen = selectedChild();
        if ( chosen != null )
            return chosen.selectedDescendant();
        else if ( isSelected )
            return this;
        else
            return null;
    }


    // ----------------------------------------------------------
    /**
     * Select this tab.
     * @return the selected tab
     */
    public TabDescriptor select()
    {
        if ( config != null )
        {
            String target = (String)config.objectForKey( "jumpTo" );
            if ( target != null )
            {
                return selectById( target );
            }
        }
        if ( parent != null )
        {
            parent.select();
            deselectSibling();
            parent.setSelectedChild( myIndex );
        }
        isSelected = true;
        return this;
    }


    // ----------------------------------------------------------
    /**
     * Unselect this tab.
     */
    protected void deselect()
    {
        isSelected = false;
        if ( parent != null )
        {
            parent.setSelectedChild( -1 );
        }
    }


    // ----------------------------------------------------------
    /**
     * Unselect this tab.
     */
    protected void deselectSibling()
    {
        if ( parent != null )
        {
            TabDescriptor sibling = parent.selectedChild();
            if ( sibling != null )
            {
                sibling.deselect();
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Select the first tab that wants to be the default.
     * @return the selected tab
     */
    public TabDescriptor selectDefault()
    {
        if ( children.count() > 0 )
        {
            for (TabDescriptor child : children)
            {
                if ( child.wantsStart )
                {
                    return child.selectDefault();
                }
            }
            return
                children.objectAtIndex(0).selectDefault();
        }
        else
        {
            return select();
        }
    }


    // ----------------------------------------------------------
    /**
     * Select the descendant tab that has the given ID.
     * @param targetId the ID to search for
     * @return the selected tab
     */
    private TabDescriptor selectDownwardById( String targetId )
    {
        if ( targetId.equals( id ) )
        {
            return select();
        }
        else if ( children.count() > 0 )
        {
            for (TabDescriptor child : children)
            {
                child = child.selectDownwardById( targetId );
                if ( child != null )
                {
                    return child;
                }
            }
        }
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Select the tab (anywhere in the entire hierarchy of tabs) that has the
     * given ID.
     * @param targetId the ID to search for
     * @return the selected tab
     */
    public TabDescriptor selectById( String targetId )
    {
        TabDescriptor root = this;
        while ( root.parent != null )
        {
            root = root.parent;
        }
        TabDescriptor result = root.selectDownwardById( targetId );
        if ( result == null )
        {
            log.error( "no matching child for id '" + targetId + "'",
                            new RuntimeException( "here") );
            log.error( "root tab = " + root );
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Select the first sibling tab that wants to be the default.
     * @return the selected tab
     */
    public TabDescriptor selectDefaultSibling()
    {
        if ( parent == null )
            return select();
        else
            return parent.selectDefault();
    }


    // ----------------------------------------------------------
    /**
     * Add a list of additional children to this tab.
     * @param moreChildren the new subtabs to add
     */
    public void addChildren( NSArray<TabDescriptor> moreChildren )
    {
        if ( moreChildren == null  ||  moreChildren.count() == 0 ) return;
        TabDescriptor selectedChildTab = selectedChild();
        children.addObjectsFromArray( moreChildren );
        if ( this.children != null )
        {
            try
            {
                this.children.sortUsingComparator( priorityOrder );
            }
            catch ( NSComparator.ComparisonException e )
            {
                log.error( "Exception sorting tab chilren:", e );
            }
        }
        resetChildIndices( selectedChildTab );
    }


    // ----------------------------------------------------------
    /**
     * Reset a the indices of child tabs.
     * @param selectedChildTab the child selection to maintain
     */
    private void resetChildIndices( TabDescriptor selectedChildTab )
    {
        for ( int i = 0; i < children.count(); i++ )
        {
            TabDescriptor child = children.objectAtIndex( i );
            child.myIndex = i;
            child.parent  = this;
            if ( child == selectedChildTab )
            {
                child.isSelected = true;
                setSelectedChild( i );
            }
            else
            {
                child.isSelected = false;
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Merge a list of additional children into this tab.
     * @param moreChildren the new subtabs to add
     */
    public void mergeClonedChildren(NSArray<TabDescriptor> moreChildren)
    {
        if ( moreChildren == null  ||  moreChildren.count() == 0 ) return;
        NSMutableArray<TabDescriptor> newChildren =
            new NSMutableArray<TabDescriptor>();
        tabSearch: for (TabDescriptor newTab : moreChildren)
        {
            for (TabDescriptor oldTab : children)
            {
                if ( oldTab.label().equals( newTab.label() ) )
                {
                    // Found old tab already present

                    // First, override any settings, as appropriate
                    if (newTab.pageName() != null)
                    {
                        oldTab.pageName = newTab.pageName();
                    }
                    if (newTab.accessLevel() > 0
                        || (newTab.config() != null
                            && newTab.config().containsKey("overrideAccessLevel")))
                    {
                        oldTab.accessLevel = newTab.accessLevel();
                    }
                    if (newTab.priority() > 0)
                    {
                        oldTab.priority = newTab.priority();
                    }
                    if (newTab.wantsStart())
                    {
                        oldTab.wantsStart = newTab.wantsStart();
                    }
                    if (newTab.id() != null)
                    {
                        oldTab.id = newTab.id();
                    }
                    if (newTab.config() != null && newTab.config().count() > 0)
                    {
                        if (oldTab.config() == null)
                        {
                            oldTab.config =
                                new NSMutableDictionary<String, Object>();
                        }
                        oldTab.config().addEntriesFromDictionary(
                            newTab.config());
                    }

                    // Now, recursively merge its children
                    oldTab.mergeClonedChildren( newTab.children() );
                    continue tabSearch;
                }
            }
            newChildren.addObject( newTab.clone() );
        }
        addChildren( newChildren );
    }


    // ----------------------------------------------------------
    /**
     * Recursively remove any tabs with an access level greater than
     * a given cutoff.  Calling this method necessarily forces all
     * tabs to be deselected.
     * @param aLevel The access level to use to limit tabs
     */
    public void filterByAccessLevel( int aLevel )
    {
        TabDescriptor selected = selectedChild();
        if ( selected != null )
            selected.deselect();
        else
            setSelectedChild( -1 );
        for ( int i = 0; i < children.count(); i++ )
        {
            TabDescriptor child = childAt( i );
            if ( child.accessLevel > aLevel )
            {
                children.removeObjectAtIndex( i );
                i--;
            }
            else
                child.filterByAccessLevel( aLevel );
        }
        resetChildIndices( null );
    }


    // ----------------------------------------------------------
    /**
     * Generate a string representation of this tab's class and address.
     * @return A human-readable description of this tab
     */
    private String addressId()
    {
        return super.toString();
    }


    // ----------------------------------------------------------
    /**
     * Generate a string representation of this tab's state.
     * @param buffer the StringBuffer to append the description to
     */
    public void appendToStringBuffer( StringBuffer buffer )
    {
        buffer.append( label );
    }


    // ----------------------------------------------------------
    /**
     * Generate a string representation of this tab's state.
     * @param buffer the StringBuffer to append the description to
     * @param appendChildren if true, then recursively append all
     *        child tab descriptions as well
     * @param indentLevel the number of spaces to indent this description
     */
    public void appendDetailsToStringBuffer( StringBuffer buffer,
                                             boolean appendChildren,
                                             int indentLevel )
    {
        StringBuffer indent = new StringBuffer( indentLevel + 4 );
        for ( int i = 0; i < indentLevel; i++ )
        {
            indent.append( ' ' );
        }
        buffer.append( indent );
        buffer.append( addressId() );
        indent.append( "    " );
        buffer.append( " = {\n" );
        buffer.append( indent );
        buffer.append( "label = " );
        buffer.append( label );
        buffer.append( ";\n" );
        buffer.append( indent );
        buffer.append( "pageName = " );
        buffer.append( pageName );
        buffer.append( ";\n" );
        buffer.append( indent );
        buffer.append( "parent = " );
        buffer.append( ( parent == null ) ? "null" : parent.addressId() );
        buffer.append( ";\n" );
        buffer.append( indent );
        buffer.append( "myIndex = " );
        buffer.append( myIndex );
        buffer.append( ";\n" );
        buffer.append( indent );
        buffer.append( "isSelected = " );
        buffer.append( isSelected );
        buffer.append( ";\n" );
        buffer.append( indent );
        buffer.append( "selectedChild = " );
        buffer.append( getSelectedChild() );
        buffer.append( ";\n" );
        buffer.append( indent );
        buffer.append( "accessLevel = " );
        buffer.append( accessLevel );
        buffer.append( ";\n" );
        buffer.append( indent );
        buffer.append( "priority = " );
        buffer.append( priority );
        buffer.append( ";\n" );
        buffer.append( indent );
        buffer.append( "wantsStart = " );
        buffer.append( wantsStart );
        buffer.append( ";\n" );
        buffer.append( indent );
        buffer.append( "parent = " );
        buffer.append( ( parent == null ) ? "null" : parent.label );
        buffer.append( ";\n" );
        if ( appendChildren )
        {
            buffer.append( indent );
            buffer.append( "children = {\n" );
            for ( int i = 0; i < children.count(); i++ )
            {
                TabDescriptor child = children.objectAtIndex(i);
                child.appendDetailsToStringBuffer(
                    buffer, appendChildren, indent.length() );
                if ( i == children.count() - 1)
                {
                    buffer.append( '\n' );
                }
                else
                {
                    buffer.append( ",\n" );
                }
            }
            buffer.append( indent );
            buffer.append( "};\n" );
        }
        indent.replace( indentLevel, indentLevel + 3, "" );
        buffer.append( indent );
        buffer.append( "}" );
    }


    // ----------------------------------------------------------
    /**
     * Generate a string representation of this tab's state.
     * @return A human-readable description of this tab
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer( 200 );
        appendToStringBuffer( buffer );
        return buffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * Generate a string representation of this tab's state, including
     * all its ancestor tabs.
     * @return A human-readable description of this tab
     */
    public String printableTabLocation()
    {
        StringBuffer buffer = new StringBuffer( 200 );
        appendToStringBuffer( buffer );
        TabDescriptor p = parent;
        while ( p != null )
        {
            buffer.append( " <-- " );
            p.appendToStringBuffer( buffer );
            p = p.parent;
        }
        return buffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * Generate a string representation of this tab's state, including
     * all its ancestor tabs.
     * @return A human-readable description of this tab
     */
    public String printableTabLocationDetails()
    {
        StringBuffer buffer = new StringBuffer( 200 );
        appendDetailsToStringBuffer( buffer, true, 0 );
        buffer.append( '\n' );
        TabDescriptor p = parent;
        while ( p != null )
        {
            buffer.append( "---------------------\n" );
            p.appendDetailsToStringBuffer( buffer, false, 0 );
            buffer.append( '\n' );
            p = p.parent;
        }
        return buffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * Generate tab descriptors from an dictionary of property values.
     * @param dict the properties to use
     * @return An array of the new tab descriptors
     */
    public static NSArray<TabDescriptor> tabsFromDictionary(
        NSDictionary<String, NSDictionary<String, Object>> dict)
    {
        NSMutableArray<TabDescriptor> tabs
            = new NSMutableArray<TabDescriptor>();
        for (String label : dict.keySet())
        {
            NSDictionary<String, Object> settings = dict.get(label);
            String pageName = (String)settings.objectForKey( "pageName" );
            int accessLevel = ERXValueUtilities.intValueWithDefault(
                settings.objectForKey( "accessLevel" ), 0 );
            int priority = ERXValueUtilities.intValueWithDefault(
                settings.objectForKey( "priority" ), 0 );
            boolean wantsStart = ERXValueUtilities.booleanValueWithDefault(
                settings.objectForKey( "wantsStart" ), false );
            @SuppressWarnings("unchecked")
            NSDictionary<String, NSDictionary<String, Object>> children =
                (NSDictionary<String, NSDictionary<String, Object>>)settings
                    .objectForKey( "children" );
            String overridingLabel = (String)settings.objectForKey("label");
            if (overridingLabel != null)
            {
                label = overridingLabel;
            }
            @SuppressWarnings("unchecked")
            NSDictionary<String, Object> tabConfig =
                (NSDictionary<String, Object>)settings.objectForKey("config");
            tabs.addObject( new TabDescriptor(
                pageName,
                label,
                accessLevel,
                priority,
                wantsStart,
                ( children == null )
                    ? null
                    : tabsFromDictionary( children ),
                (String)settings.objectForKey( "id" ),
                tabConfig
            ) );
        }
        return tabs;
    }


    // ----------------------------------------------------------
    /**
     * Generate tab descriptors from a property list description.
     * @param data the raw bytes from the property list
     * @return An array of the new tab descriptors
     */
    public static NSArray<TabDescriptor> tabsFromPropertyList(NSData data)
    {
        @SuppressWarnings("unchecked")
        NSDictionary<String, NSDictionary<String, Object>> dict =
            (NSDictionary<String, NSDictionary<String, Object>>)
            NSPropertyListSerialization
                .propertyListFromData(data, "UTF-8");
        log.debug("tabFromPropertyList(): dict = " + dict);
        return tabsFromDictionary(dict);
    }


    // ----------------------------------------------------------
    /**
     * Convert a mixed-case prose-style string into a string where
     * only the first character (at most) is capitalized.  This method
     * is typically used to convert a "title case" label into a
     * "sentence case" string.
     * @param title the string to convert
     * @return The string with all characters after the first one
     *     converted to lower case
     */
    public static String lowerCaseAfterFirst( String title )
    {
        String result = null;
        if ( title != null )
        {
            if ( title.length() < 2 )
            {
                result = title;
            }
            else
            {
                result = title.substring( 0, 1 )
                    + title.substring( 1 ).toLowerCase();
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Generate an independent copy of this tab.
     * @return A clone of this tab
     */
    public Object clone()
    {
        TabDescriptor result = new TabDescriptor(
            pageName,
            label,
            accessLevel,
            priority,
            wantsStart,
            children.mutableClone(),
            id,
            ( config == null )
                ? null
                : config.mutableClone()
        );
        result.myIndex = myIndex;
        result.isSelected = isSelected;
        result.setSelectedChild( getSelectedChild() );
        for ( int i = 0; i < children.count(); i++ )
        {
            TabDescriptor thisTab = (TabDescriptor)
                result.children.objectAtIndex(i).clone();
            thisTab.parent = result;
            result.children.replaceObjectAtIndex( thisTab, i );
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * @param selectedChild The selectedChild to set.
     */
    protected void setSelectedChild( int selectedChild )
    {
//        if ( selectedChild == 5 )
//        {
//            log.error( "setting selected child = " + selectedChild,
//                       new Exception( "here" ) );
//            log.error( "tabs = " + this );
//        }
        this.theSelectedChild = selectedChild;
    }


    // ----------------------------------------------------------
    /**
     * @return Returns the selectedChild.
     */
    protected int getSelectedChild()
    {
        return theSelectedChild;
    }


    // ----------------------------------------------------------
    /**
     * A custom comparator class used to sort tab arrays by priority.
     */
    protected class PriorityComparator
        extends NSComparator
    {
        // ----------------------------------------------------------
        /* (non-Javadoc)
         * @see com.webobjects.foundation.NSComparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare( Object left, Object right )
            throws ComparisonException
        {
            if ( ! (  left instanceof TabDescriptor
                   && right instanceof TabDescriptor ) )
            {
                throw new ComparisonException( "incomparable TabDescriptors" );
            }
            int leftp  = ( (TabDescriptor)left  ).priority;
            int rightp = ( (TabDescriptor)right ).priority;
            if ( leftp < rightp )
                return OrderedAscending;
            else if ( leftp == rightp )
                return OrderedSame;
            else
                return OrderedDescending;
        }
    }


    //~ Instance/static variables .............................................

    public static final String TAB_DEFINITIONS = "Tabs.plist";

    private String              pageName;
    private String              label;
    private String              lcLabel;
    private int                 accessLevel;
    private int                 priority;
    private boolean             wantsStart;
    private NSMutableArray<TabDescriptor> children;
    private String              id;
    private NSMutableDictionary<String, Object> config;

    private TabDescriptor       parent           = null;
    private int                 myIndex          = -1;
    private boolean             isSelected       = false;
    private int                 theSelectedChild = -1;

    protected final NSComparator priorityOrder = new PriorityComparator();

    static Logger log = Logger.getLogger( TabDescriptor.class );
}
