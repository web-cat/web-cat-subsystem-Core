/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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

import org.webcat.core.QualifierInSubquery;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.eof.qualifiers.ERXInQualifier;

//-------------------------------------------------------------------------
/**
 * Utility methods to operate on qualifiers.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class QualifierUtils
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * This class provides only static utility methods, so no instance
     * should ever be created.
     */
    private QualifierUtils()
    {
        // Nothing to do
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Returns a value indicating whether specified qualifier tree can be
     * used in a fetch specification.
     *
     * @param q the qualifier to check
     * @param entityName the entity being fetched
     * @return true if the qualifier can be used in a fetch specification;
     *     otherwise, false.
     */
    public static boolean isQualifierFetchable(EOQualifier q, String entityName)
    {
        try
        {
            EOClassDescription classDescription =
                EOClassDescription.classDescriptionForEntityName(entityName);
            q.validateKeysWithRootClassDescription(classDescription);
            return true;
        }
        catch (IllegalStateException e)
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    /**
     * Partitions a qualifier two sets -- the set of qualifiers that can be
     * used in a fetch specification, and the set of those that cannot. This is
     * used to speed up many queries by passing the fetchable qualifiers to a
     * fetch specification to reduce the size of the initial fetch, and then
     * pruning that set further in-memory using the non-fetchable qualifiers.
     *
     * The result of this method is dependent on the type of qualifier passed
     * into it.  If the qualifier is an EOAndQualifier, then its child
     * qualifiers are partitioned into fetchable and non-fetchable sets. For
     * any other type of qualifier, the qualifier itself is placed into the
     * appropriate partition depending on whether or not it is fetchable.
     *
     * @param q the qualifier to partition
     * @param entityName the name of the entity being fetched
     * @return an array with two EOQualifiers. array[0] is the AND of all
     *     of the fetchable qualifiers, and array[1] is the AND of all of the
     *     non-fetchable qualifiers. Either of these entries can be null if
     *     there were no qualifiers in either set.
     */
    public static EOQualifier[] partitionQualifier(
        EOQualifier q, String entityName)
    {
        EOQualifier fetchQualifier = null;
        EOQualifier nonFetchQualifier = null;

        if (q instanceof EOAndQualifier)
        {
            EOAndQualifier aq = (EOAndQualifier)q;

            NSMutableArray<EOQualifier> fetchQualifiers =
                new NSMutableArray<EOQualifier>();
            NSMutableArray<EOQualifier> nonFetchQualifiers =
                new NSMutableArray<EOQualifier>();

            for (EOQualifier childQ : aq.qualifiers())
            {
                if (isQualifierFetchable(childQ, entityName))
                {
                    fetchQualifiers.addObject(childQ);
                }
                else
                {
                    nonFetchQualifiers.addObject(childQ);
                }
            }

            if (fetchQualifiers.count() > 0)
            {
                fetchQualifier = new EOAndQualifier(fetchQualifiers);
            }

            if (nonFetchQualifiers.count() > 0)
            {
                nonFetchQualifier = new EOAndQualifier(nonFetchQualifiers);
            }
        }
        else
        {
            if (isQualifierFetchable(q, entityName))
            {
                fetchQualifier = q;
            }
            else
            {
                nonFetchQualifier = q;
            }
        }

        return new EOQualifier[] { fetchQualifier, nonFetchQualifier };
    }


    // ----------------------------------------------------------
    /**
     * Returns a qualifier that represents the condition (key IN choices).
     * Currently this returns an OR qualifier of the form
     * (key == choice1) OR (key == choice2) OR ..., but this could be changed
     * to an ERXInQualifier once its in-memory behavior regarding cross-
     * relationship keypaths is fixed.
     *
     * @param key the key being changed for containment
     * @param choices the choices among which the value of key should be
     *
     * @return the qualifier that represents this condition
     */
    public static EOQualifier qualifierForInCondition(
        String key, NSArray<?> choices)
    {
        NSMutableArray<EOQualifier> terms = new NSMutableArray<EOQualifier>();

        for (Object choice : choices)
        {
            terms.addObject(new EOKeyValueQualifier(key,
                EOQualifier.QualifierOperatorEqual, choice));
        }

        return new EOOrQualifier(terms);
    }


    // ----------------------------------------------------------
    /**
     * Checks to see if the qualifier is essentially representative of an IN
     * condition. Currently this means that it is an EOOrQualifier for which
     * each child is an EOKeyValueQualifier with the same key; or that the
     * qualifier is an ERXInQualifier (though this isn't really used yet
     * because of some problems with that qualifier; see above).
     *
     * If the qualifier is representative of an IN condition, then the array
     * returned is the array of choices for the condition. This method returns
     * null if the qualifier is not representative of an IN condition.
     *
     * @param q the qualifier to check and get choices for
     * @return an array of choices, or null if not an IN qualifier
     */
    public static NSDictionary<String, Object> infoIfInQualifier(EOQualifier q)
    {
        NSMutableDictionary<String, Object> info =
            new NSMutableDictionary<String, Object>();

        if (q instanceof ERXInQualifier)
        {
            ERXInQualifier iq = (ERXInQualifier)q;
            info.setObjectForKey(iq.key(), "key");
            info.setObjectForKey(iq.values(), "values");
            return info;
        }
        else if (q instanceof EOOrQualifier)
        {
            EOOrQualifier oq = (EOOrQualifier)q;
            NSArray<EOQualifier> children = oq.qualifiers();
            NSMutableArray<Object> choices = new NSMutableArray<Object>();

            if (children.count() > 0)
            {
                EOQualifier cq = children.objectAtIndex(0);

                if (cq instanceof EOKeyValueQualifier)
                {
                    EOKeyValueQualifier kvq = (EOKeyValueQualifier)cq;

                    String firstKey = kvq.key();
                    info.setObjectForKey(firstKey, "key");
                    choices.addObject(kvq.value());

                    for (int i = 1; i < children.count(); i++)
                    {
                        cq = children.objectAtIndex(i);

                        if (cq instanceof EOKeyValueQualifier)
                        {
                            kvq = (EOKeyValueQualifier)cq;

                            String key = kvq.key();
                            if (key.equals(firstKey))
                            {
                                choices.addObject(kvq.value());
                            }
                            else
                            {
                                return null;
                            }
                        }
                        else
                        {
                            return null;
                        }
                    }
                }
                else
                {
                    return null;
                }
            }

            info.setObjectForKey(choices, "values");
            return info;
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Shortcut method to return a subquery qualifier that determines if the
     * object at a keypath is in a given relationship. Unlike a normal
     * EOKeyComparisonQualifier that does this, the qualifier returned here can
     * be properly negated with EONotQualifier to determine if the object is
     * NOT in the relationship.
     *
     * ".id" is automatically appended to the ends of the relationships passed
     * into this function.
     *
     * @param keyPath the key path of the object to test (a to-one relationship)
     * @param relationship the relationship in which to look for keyPath
     *     (a to-many relationship)
     * @return the qualifier
     */
    public static EOQualifier qualifierForKeyPathInRelationship(String keyPath,
            String relationship)
    {
        return new QualifierInSubquery(
                new EOKeyComparisonQualifier(keyPath + ".id",
                        EOQualifier.QualifierOperatorEqual,
                        relationship + ".id"));
    }
}
