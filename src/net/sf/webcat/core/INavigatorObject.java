package net.sf.webcat.core;

import com.webobjects.foundation.NSArray;

//--------------------------------------------------------------------------
/**
 * 
 * @author Tony Allevato
 * @version $Id$
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
