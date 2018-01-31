/*==========================================================================*\
 |  $Id: MutableContainer.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

package org.webcat.core;

import org.webcat.core.MutableContainer;

// -------------------------------------------------------------------------
/**
 *  This interface is used to define the common properties of mutable
 *  collections that can be stored as EO attributes.
 *
 *  @author  Stephen Edwards
 *  @version $Id: MutableContainer.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public interface MutableContainer
{
    //----------------------------------------------------------
    /**
     * Test this object to see if it has been changed (mutated) since it
     * was last saved.
     * @return true if this dictionary has been changed
     */
    public boolean hasChanged();


    //----------------------------------------------------------
    /**
     * Mark this object as having changed (mutated) since it
     * was last saved.
     * @param value true if this dictionary has been changed
     */
    public void setHasChanged( boolean value );


    //----------------------------------------------------------
    /**
     * Set the enclosing container that holds this one, if any.
     * @param parent a reference to the enclosing container
     */
    public void setParent( MutableContainer parent );


    //----------------------------------------------------------
    /**
     * Set the enclosing container that holds this one, if any.
     * Also, recursively cycle through all contained mutable containers,
     * resetting their parents to this object as well.
     * @param parent a reference to the enclosing container
     */
    public void setParentRecursively( MutableContainer parent );


    //----------------------------------------------------------
    /**
     * Examine all contained objects for mutable containers, and reset
     * the parent relationships for any that are found.  Any NS containers
     * found will be converted to mutable versions.
     * @param recurse if true, force the reset to cascade recursively down
     *                the tree, rather than just affecting this node's
     *                immediate children.
     */
    public void resetChildParents( boolean recurse );


    //----------------------------------------------------------
    /**
     * Retrieve the enclosing container that holds this one, if any.
     * @return a reference to the enclosing container
     */
    public MutableContainer parent();


    //----------------------------------------------------------
    /**
     * An interface for an owner who needs to listen to change
     * notifications.  Note that a mutable container can have only
     * one owner.
     */
    public interface MutableContainerOwner
    {
        //------------------------------------------------------
        /**
         * Called whenever an owned mutable container has changed.
         */
        public void mutableContainerHasChanged();
    }


    //----------------------------------------------------------
    /**
     * Set the owner of this container.
     * @param owner the owner of this container container
     */
    public void setOwner( MutableContainerOwner owner );


    //----------------------------------------------------------
    /**
     * Replace this container's contents by copying from another (and
     * assuming parent ownership over any subcontainers).  The container
     * is free to assume the argument is of a compatible container type.
     * @param other the container to copy from
     */
    public void copyFrom( MutableContainer other );
}
