/*==========================================================================*\
 |  $Id: AdvancedQueryModel.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
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

package org.webcat.core.objectquery;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

//-------------------------------------------------------------------------
/**
 * A query model implementation.
 *
 * @author aallowat
 * @version $Id: AdvancedQueryModel.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
 */
public class AdvancedQueryModel
    extends AbstractQueryAssistantModel
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public AdvancedQueryModel()
    {
        // Create a default criterion.

        criteria = new NSMutableArray<AdvancedQueryCriterion>();
        insertNewCriterionAtIndex(0);
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    @Override
    public EOQualifier qualifierFromValues()
    {
        NSMutableArray<EOQualifier> terms = new NSMutableArray<EOQualifier>();

        for (AdvancedQueryCriterion cri : criteria)
        {
            EOQualifier termQual =
                AdvancedQueryComparison.qualifierForCriterion(cri);

            if (termQual != null)
            {
                terms.addObject(termQual);
            }
        }

        return new EOAndQualifier(terms);
    }


    // ----------------------------------------------------------
    @Override
    public void takeValuesFromQualifier(EOQualifier qualifier)
    {
        criteria = new NSMutableArray<AdvancedQueryCriterion>();

        if (qualifier instanceof EOAndQualifier)
        {
            EOAndQualifier qAnd = (EOAndQualifier)qualifier;

            for (EOQualifier q : qAnd.qualifiers())
            {
                AdvancedQueryCriterion cri =
                    AdvancedQueryComparison.criterionForQualifier(q);

                if (cri != null)
                {
                    criteria.addObject(cri);
                }
            }
        }

        // Insert a blank criterion if the qualifier produces none, so that
        // editing can be performed correctly.
        if (criteria.count() == 0)
        {
            insertNewCriterionAtIndex(0);
        }
    }


    // ----------------------------------------------------------
    public String humanReadableDescription()
    {
        StringBuffer buffer = new StringBuffer();

/*        boolean first = true;

        for (AdvancedQueryCriterion cri : criteria)
        {
            cri.appendHumanReadableDescription(buffer);

            if (!first)
            {
                buffer.append(", and ");
            }
            else
            {
                first = false;
            }
        }*/

        // TODO: add descriptions for AdvancedQueryModel
        buffer.append("TODO: add descriptions for AdvancedQueryModel");

        return buffer.toString();
    }


    // ----------------------------------------------------------
    public NSArray<AdvancedQueryCriterion> criteria()
    {
        return criteria;
    }


    // ----------------------------------------------------------
    public void setCriteria(NSArray<AdvancedQueryCriterion> value)
    {
        criteria = value.mutableClone();
    }


    // ----------------------------------------------------------
    public void insertNewCriterionAtIndex(int index)
    {
        AdvancedQueryCriterion newCriterion = new AdvancedQueryCriterion();

        newCriterion.setCastType(String.class);
        newCriterion.setComparison(AdvancedQueryComparison.IS_EQUAL_TO);
        newCriterion.setComparandType(AdvancedQueryCriterion.COMPARAND_LITERAL);

        criteria.insertObjectAtIndex(newCriterion, index);
    }


    // ----------------------------------------------------------
    public void removeCriterionAtIndex(int index)
    {
        criteria.removeObjectAtIndex(index);

        if (criteria.count() == 0)
        {
            insertNewCriterionAtIndex(0);
        }
    }


    //~ Instance/static variables .............................................

    private NSMutableArray<AdvancedQueryCriterion> criteria;
}
