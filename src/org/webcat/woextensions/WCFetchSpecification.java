/*==========================================================================*\
 |  $Id: WCFetchSpecification.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
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

package org.webcat.woextensions;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import er.extensions.eof.ERXFetchSpecification;

//-------------------------------------------------------------------------
/**
 * Changes default behavior of fetch specs to always refresh in-memory
 * objects with the results of the fetch, and to include pending results
 * from the current editing context, not just results from the database.
 * [Note: because of bugs in Wonder, pending changes from the current
 * editing context are <b>not</b> included in results yet!]
 *
 * @param <T> The type of entity to be fetched.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class WCFetchSpecification<T extends EOEnterpriseObject>
    extends ERXFetchSpecification<T>
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCFetchSpecification(
        String entityName,
        EOQualifier qualifier,
        NSArray<EOSortOrdering> sortOrderings,
        boolean usesDistinct,
        boolean isDeep,
        NSDictionary<String, ?> hints)
    {
        super(
            entityName, qualifier, sortOrderings, usesDistinct, isDeep, hints);
        setDefaults();
    }


    // ----------------------------------------------------------
    public WCFetchSpecification(
        String entityName,
        EOQualifier qualifier,
        NSArray<EOSortOrdering> sortOrderings)
    {
        super(entityName, qualifier, sortOrderings);
        setDefaults();
    }


    // ----------------------------------------------------------
    public WCFetchSpecification(EOFetchSpecification spec)
    {
        super(spec);
        setDefaults();
    }


    // ----------------------------------------------------------
    public WCFetchSpecification(ERXFetchSpecification<T> spec)
    {
        super(spec);
        if (spec instanceof WCFetchSpecification)
        {
            setRefreshesRefetchedObjects(spec.refreshesRefetchedObjects());
            setIncludeEditingContextChanges(
//                spec.includeEditingContextChanges()

                // Can't use the value above, because of bugs in Wonder.
                // See comments in setDefaults().  Instead, force it off:
                false
                );
        }
        else
        {
            setDefaults();
        }
    }


    //~ Methods ...............................................................


    // ----------------------------------------------------------
    /**
     * Set the default behaviors for this kind of fetch specification,
     * including refreshing refetched objects and including editing context
     * changes.
     *
     * Note: Wonder has two critical bugs that are preventing us from using
     * the option to include editing context changes.  First, turning this
     * option on results in fetches that may return nothing, even when there
     * is data matching the fetch.  Second, turning this option on causes
     * the option to refresh refetched objects to be ignored, because of the
     * way ERXFetchSpecification implements the includeEditingContextChanges()
     * option.
     *
     * The option is implemented by calling
     * ERXEOControlUtilities.objectsWithQualifier(), and passing the contents
     * of the fetch specification as parameters (!).  That method then
     * reconstructs a new EOFetchSpecification to use internally (!).  Of
     * course, the refreshesRefetchedObjects option is <b>not</b> passed to
     * objectsWithQualifier(), so it isn't carried over to the internally
     * created EOFetchSpecification inside that method.  Yuck.
     *
     * I don't know the source of the bug that causes the merging process
     * to sometimes produce an empty result set, but now isn't the time to
     * try to debug Wonder.  We'll just go without that option until it can
     * be fixed later.
     */
    private void setDefaults()
    {
        setRefreshesRefetchedObjects(false); // FIXME: TRUE!!!
        // Want to do this:
        // setIncludeEditingContextChanges(true);
        // but ... it doesn't work.  See Javadoc comments above.  Instead:
        setIncludeEditingContextChanges(false);
    }


    // ----------------------------------------------------------
    public static <T extends EOEnterpriseObject> WCFetchSpecification<T>
        fetchSpec(EOFetchSpecification fs, Class<T> clazz)
    {
        if (fs instanceof WCFetchSpecification)
        {
            @SuppressWarnings("unchecked")
            WCFetchSpecification<T> result = (WCFetchSpecification<T>)fs;
            return result;
        }
        return new WCFetchSpecification<T>(fs);
    }


    // ----------------------------------------------------------
    public static <T extends EOEnterpriseObject> WCFetchSpecification<T>
        fetchSpec(EOFetchSpecification fs)
    {
        if (fs instanceof WCFetchSpecification)
        {
            @SuppressWarnings("unchecked")
            WCFetchSpecification<T> result = (WCFetchSpecification<T>)fs;
            return result;
        }
        return new WCFetchSpecification<T>(fs);
    }

}
