/*==========================================================================*\
 |  $Id: ObjectQuery.java,v 1.3 2012/01/05 19:57:01 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2011 Virginia Tech
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

import org.webcat.core.objectquery.AbstractQueryAssistantModel;
import org.webcat.core.objectquery.QueryAssistantDescriptor;
import org.webcat.core.objectquery.QueryAssistantManager;
import org.webcat.core.MutableDictionary;
import org.webcat.core.QualifierSerialization;
import org.webcat.core.QualifierUtils;
import org.webcat.core._ObjectQuery;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import er.extensions.eof.ERXEOControlUtilities;

// -------------------------------------------------------------------------
/**
 * TODO: place a real description here.
 *
 * @author  Tony Allevato
 * @author  latest changes by: $Author: stedwar2 $
 * @version $Revision: 1.3 $, $Date: 2012/01/05 19:57:01 $
 */
public class ObjectQuery
    extends _ObjectQuery
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new ObjectQuery object.
     */
    public ObjectQuery()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String userPresentableDescription()
    {
        return description();
    }


    // ----------------------------------------------------------
    @Override
    public String description()
    {
        String result = super.description();
        if (result == null)
        {
            result = qualifierDescription();
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Gets the qualifier that applies to this query, loading any EOs in the
     * qualifier into the same editing context as this object.
     *
     * @return the qualifier
     */
    public EOQualifier qualifier()
    {
        return qualifier(editingContext());
    }


    // ----------------------------------------------------------
    /**
     * Gets the qualifier that applies to this query, loading any EOs in the
     * qualifier into the specified editing context.
     *
     * @param ec the editing context
     * @return the qualifier
     */
    public EOQualifier qualifier(EOEditingContext ec)
    {
        MutableDictionary info = queryInfo();

        if (info == null)
        {
            return null;
        }
        else
        {
            EOQualifier q = (EOQualifier) info.objectForKey(KEY_QUALIFIER);
            return QualifierSerialization.convertGIDsToEOs(q, ec);
        }
    }


    // ----------------------------------------------------------
    /**
     * Sets the qualifier that applies to this query.
     *
     * @param q
     *            the qualifier to use
     */
    public void setQualifier(EOQualifier q)
    {
        if (queryInfo() == null)
        {
            setQueryInfo(new MutableDictionary());
        }

        if (q == null)
        {
            queryInfo().removeObjectForKey(KEY_QUALIFIER);
        }
        else
        {
            q = QualifierSerialization.convertEOsToGIDs(q, editingContext());
            queryInfo().setObjectForKey(q, KEY_QUALIFIER);
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets a human-readable description of the kinds of objects that will be
     * fetched with this query.
     *
     * @return a human-readable description of the query
     */
    public String qualifierDescription()
    {
        EOQualifier q = qualifier();
        String qaid = queryAssistantId();

        QueryAssistantDescriptor qad =
            QueryAssistantManager.getInstance().assistantWithId(qaid);
        if (qad != null)
        {
            AbstractQueryAssistantModel model = qad.createModel();
            model.takeValuesFromQualifier(q);
            return model.humanReadableDescription();
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the identifier of the query assistant that was used to construct
     * this query.
     *
     * @return the id of the query assistant
     */
    public String queryAssistantId()
    {
        MutableDictionary info = queryInfo();

        // Since older queries did not store the query assistant that was used
        // to construct them, we put in a special case to fallback to the
        // advanced query builder, which should handle any of the queries
        // created up to the time that this was done.

        if (info == null)
        {
            return FALLBACK_QUERY_ASSISTANT;
        }
        else
        {
            String id = (String) info.objectForKey(KEY_QUERY_ASSISTANT_ID);

            return (id != null) ? id : FALLBACK_QUERY_ASSISTANT;
        }
    }


    // ----------------------------------------------------------
    /**
     * Sets the identifier of the query assistant that was used to construct
     * this query.
     *
     * @param id
     *            the id of the query assistant
     */
    public void setQueryAssistantId(String id)
    {
        if (queryInfo() == null)
        {
            setQueryInfo(new MutableDictionary());
        }

        if (id == null)
        {
            queryInfo().removeObjectForKey(KEY_QUERY_ASSISTANT_ID);
        }
        else
        {
            queryInfo().setObjectForKey(id, KEY_QUERY_ASSISTANT_ID);
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets an upper bound on the number of objects in the database that
     * satisfy this query. This is an upper bound instead of an exact count
     * because it is meant to be fast; a query's qualifier may have parts that
     * can only be evaluated in memory.
     *
     * @return the maximum number of objects that satisfy this query
     */
    public int upperBoundOfObjectCount()
    {
        EOQualifier[] quals = QualifierUtils.partitionQualifier(
                qualifier(), objectType());

        return ERXEOControlUtilities.objectCountWithQualifier(
                editingContext(), objectType(), quals[0]);
    }


    // ----------------------------------------------------------
    public NSArray<?> fetchPrimaryKeys()
    {
        EOQualifier[] quals = QualifierUtils.partitionQualifier(qualifier(),
                objectType());

        EOEntity entity = EOUtilities.entityNamed(
                editingContext(), objectType());

        EOFetchSpecification pkFetchSpec =
            ERXEOControlUtilities.primaryKeyFetchSpecificationForEntity(
                editingContext(), objectType(), quals[0], null, null);

        NSArray<?> primaryKeyDictionaries =
            editingContext().objectsWithFetchSpecification(pkFetchSpec);

        String pkAttributeName =
            entity.primaryKeyAttributes().lastObject().name();
        return (NSArray<?>)primaryKeyDictionaries.valueForKey(pkAttributeName);
    }


    //~ Static/instance variables .............................................

    /*
     * A safe fallback for older queries that did not keep track of the query
     * assistant used to construct them.
     */
    private static final String FALLBACK_QUERY_ASSISTANT =
        "org.webcat.core.objectquery.advancedQuery";

    private static final String KEY_QUALIFIER = "qualifier";
    private static final String KEY_QUERY_ASSISTANT_ID = "queryAssistantId";
}
