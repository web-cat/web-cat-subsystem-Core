package org.webcat.core;

import com.webobjects.foundation.NSArray;

//--------------------------------------------------------------------------
/**
 *
 * @author Tony Allevato
 * @version $Id: INavigatorObject.java,v 1.2 2011/04/21 17:18:30 aallowat Exp $
 */
public interface INavigatorObject
{
    // ----------------------------------------------------------
    /**
     * Gets the set of (possibly one) entities that are represented by this
     * navigator object.
     *
     * @return the array of represented objects
     */
    NSArray<?> representedObjects();
}
