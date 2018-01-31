/*==========================================================================*\
 |  $Id: AdvancedQueryComparison.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
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

import org.webcat.core.QualifierInSubquery;
import org.webcat.core.QualifierUtils;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import er.extensions.eof.qualifiers.ERXBetweenQualifier;
import er.extensions.eof.qualifiers.ERXQualifierInSubquery;

//-------------------------------------------------------------------------
/**
 * This abstract base class defines the kinds of query comparison operations
 * supported by the {@link AdvancedQueryAssistant}.  It also defines internally
 * a few concrete subclasses, and exports public constants for shared
 * instances of these subclasses for the basic comparison operations supported
 * by the advanced query assistant.
 *
 * @author aallowat
 * @version $Id: AdvancedQueryComparison.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
 */
public abstract class AdvancedQueryComparison
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Only subclasses can call the constructor.  Then again, since this
     * is an abstract class, it isn't possible for other code to construct
     * instances anyway.
     */
    protected AdvancedQueryComparison()
    {
        // Nothing to do.  All data members are static.
    }


    //~ Public Constants ......................................................

    public static final AdvancedQueryComparison IS_EQUAL_TO;
    public static final AdvancedQueryComparison IS_NOT_EQUAL_TO;
    public static final AdvancedQueryComparison IS_LESS_THAN;
    public static final AdvancedQueryComparison IS_LESS_THAN_OR_EQUAL_TO;
    public static final AdvancedQueryComparison IS_GREATER_THAN;
    public static final AdvancedQueryComparison IS_GREATER_THAN_OR_EQUAL_TO;
    public static final AdvancedQueryComparison IS_BETWEEN;
    public static final AdvancedQueryComparison IS_NOT_BETWEEN;
    public static final AdvancedQueryComparison IS_LIKE;
    public static final AdvancedQueryComparison IS_NOT_LIKE;
    public static final AdvancedQueryComparison IS_ONE_OF;
    public static final AdvancedQueryComparison IS_NOT_ONE_OF;


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public boolean equals(Object obj)
    {
        if (obj instanceof AdvancedQueryComparison)
        {
            return toString().equals(((AdvancedQueryComparison)obj).toString());
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    public int hashCode()
    {
        return toString().hashCode();
    }


    // ----------------------------------------------------------
    public static AdvancedQueryComparison comparisonWithName(String name)
    {
        return nameToComparisonMap.objectForKey(name);
    }


    // ----------------------------------------------------------
    public static NSArray<AdvancedQueryComparison> comparisonsForType(
            Class<?> klass)
    {
        if (Boolean.class.isAssignableFrom(klass)
            || klass == Boolean.TYPE)
        {
            return booleanComparisons;
        }
        else if (Number.class.isAssignableFrom(klass)
                 || klass == Integer.TYPE
                 || klass == Double.TYPE
                 || klass == Float.TYPE)
        {
            return numericComparisons;
        }
        else if (String.class.isAssignableFrom(klass))
        {
            return stringComparisons;
        }
        else if (java.util.Date.class.isAssignableFrom(klass))
        {
            return timestampComparisons;
        }
        else if (EOEnterpriseObject.class.isAssignableFrom(klass))
        {
            return objectComparisons;
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns a criterion object that represents the given qualifier. This
     * allows us to access the properties of any qualifier via a common
     * interface, instead of using large blocks of conditional code based on
     * the qualifier type.
     *
     * @param q the qualifier to convert to a criterion object
     * @return an AdvancedQueryCriterion containing the qualifier properties
     */
    public static AdvancedQueryCriterion criterionForQualifier(EOQualifier q)
    {
        NSDictionary<String, Object> info = QualifierUtils.infoIfInQualifier(q);

        AdvancedQueryComparison comparison = null;

        if (info != null)
        {
            comparison = IS_ONE_OF;
        }
        else if (q instanceof QualifierInSubquery)
        {
            comparison = IS_ONE_OF;
        }
        else if (q instanceof ERXBetweenQualifier)
        {
            comparison = IS_BETWEEN;
        }
        else if (q instanceof EOKeyValueQualifier)
        {
            EOKeyValueQualifier kvq = (EOKeyValueQualifier)q;
            comparison = comparisonFromSelector(kvq.selector());
        }
        else if (q instanceof EOKeyComparisonQualifier)
        {
            EOKeyComparisonQualifier kcq = (EOKeyComparisonQualifier)q;
            comparison = comparisonFromSelector(kcq.selector());
        }
        else if (q instanceof EONotQualifier)
        {
            EOQualifier nq = ((EONotQualifier)q).qualifier();

            info = QualifierUtils.infoIfInQualifier(nq);

            if (info != null)
            {
                comparison = IS_NOT_ONE_OF;
            }
            else if (nq instanceof QualifierInSubquery)
            {
                comparison = IS_NOT_ONE_OF;
            }
            else if (nq instanceof ERXBetweenQualifier)
            {
                comparison = IS_NOT_BETWEEN;
            }
            else if (nq instanceof EOKeyValueQualifier)
            {
                EOKeyValueQualifier kvq = (EOKeyValueQualifier)nq;

                NSSelector selector = kvq.selector();
                if (selector.equals(
                        EOQualifier.QualifierOperatorCaseInsensitiveLike))
                {
                    comparison = IS_NOT_LIKE;
                }
            }
            else if (nq instanceof EOKeyComparisonQualifier)
            {
                EOKeyComparisonQualifier kcq = (EOKeyComparisonQualifier)nq;

                NSSelector selector = kcq.selector();
                if (selector.equals(
                        EOQualifier.QualifierOperatorCaseInsensitiveLike))
                {
                    comparison = IS_NOT_LIKE;
                }
            }
        }

        if (comparison != null)
        {
            AdvancedQueryCriterion criterion =
                comparison._criterionForQualifier(q);

            if(criterion != null)
            {
                criterion.setComparison(comparison);
            }

            return criterion;
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns a qualifier with the properties of the given criterion object.
     *
     * @param criterion an AdvancedQueryCriterion containing the qualifier
     *     properties
     * @return the qualifier that results from converting the criterion
     */
    public static EOQualifier qualifierForCriterion(
            AdvancedQueryCriterion criterion)
    {
        if (criterion.keyPath() == null
            || criterion.comparison() == null)
        {
            return null;
        }

        AdvancedQueryComparison comparison = criterion.comparison();

        return comparison._qualifierForCriterion(criterion);
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether this comparison supports a right-hand
     * side that is another keypath, rather than a just a literal value.
     *
     * @return true if the comparison supports keypaths and literals on the
     *     right-hand side; false if it only supports literals.
     */
    public abstract boolean doesSupportKeyPaths();


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether this comparison supports multiple right-
     * hand sides that are ORed together (either with actual ORs or in an IN
     * clause).
     *
     * @return true if the comparison supports multiple values on the right-
     *     hand side; false if it only supports singular values.
     */
    public abstract boolean doesSupportMultipleValues();


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether this comparison has an operand on the
     * right-hand side.
     *
     * @return true if the comparison requires a second operand; otherwise,
     *     false.
     */
    public abstract boolean hasSecondOperand();


    //~ Protected Methods .....................................................

    // ----------------------------------------------------------
    protected abstract AdvancedQueryCriterion _criterionForQualifier(
            EOQualifier q);


    // ----------------------------------------------------------
    protected abstract EOQualifier _qualifierForCriterion(
            AdvancedQueryCriterion criterion);


    //~ Private Methods/Classes ...............................................

    // ----------------------------------------------------------
    private static AdvancedQueryComparison comparisonFromSelector(
            NSSelector selector)
    {
        if (selector.equals(EOQualifier.QualifierOperatorEqual))
        {
            return IS_EQUAL_TO;
        }
        else if (selector.equals(EOQualifier.QualifierOperatorNotEqual))
        {
            return IS_NOT_EQUAL_TO;
        }
        else if (selector.equals(EOQualifier.QualifierOperatorLessThan))
        {
            return IS_LESS_THAN;
        }
        else if (selector.equals(
                     EOQualifier.QualifierOperatorLessThanOrEqualTo))
        {
            return IS_LESS_THAN_OR_EQUAL_TO;
        }
        else if (selector.equals(EOQualifier.QualifierOperatorGreaterThan))
        {
            return IS_GREATER_THAN;
        }
        else if (selector.equals(
                     EOQualifier.QualifierOperatorGreaterThanOrEqualTo))
        {
            return IS_GREATER_THAN_OR_EQUAL_TO;
        }
        else if (selector.equals(
                     EOQualifier.QualifierOperatorCaseInsensitiveLike))
        {
            return IS_LIKE;
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Implements comparison logic for the basic relational operators.
     */
    private static class SimpleComparison
        extends AdvancedQueryComparison
    {
        //~ Constructor .......................................................

        // ----------------------------------------------------------
        public SimpleComparison(int type)
        {
            this.type = type;
        }


        //~ Public Methods ....................................................

        // ----------------------------------------------------------
        public String toString()
        {
            return descriptions[type];
        }


        // ----------------------------------------------------------
        @Override
        public boolean doesSupportKeyPaths()
        {
            return true;
        }


        // ----------------------------------------------------------
        @Override
        public boolean doesSupportMultipleValues()
        {
            return false;
        }


        // ----------------------------------------------------------
        @Override
        public boolean hasSecondOperand()
        {
            return true;
        }


        //~ Protected Methods .................................................

        // ----------------------------------------------------------
        @Override
        protected AdvancedQueryCriterion _criterionForQualifier(EOQualifier q)
        {
            AdvancedQueryCriterion criterion = new AdvancedQueryCriterion();

            if (q instanceof EOKeyValueQualifier)
            {
                EOKeyValueQualifier kvq = (EOKeyValueQualifier)q;

                criterion.setKeyPath(kvq.key());
                criterion.setComparandType(
                        AdvancedQueryCriterion.COMPARAND_LITERAL);
                criterion.setValue(kvq.value());
            }
            else if (q instanceof EOKeyComparisonQualifier)
            {
                EOKeyComparisonQualifier kcq = (EOKeyComparisonQualifier)q;

                criterion.setKeyPath(kcq.leftKey());
                criterion.setComparandType(
                    AdvancedQueryCriterion.COMPARAND_KEYPATH);
                criterion.setValue(kcq.rightKey());
            }

            return criterion;
        }


        // ----------------------------------------------------------
        @Override
        protected EOQualifier _qualifierForCriterion(AdvancedQueryCriterion cri)
        {
            EOQualifier q = null;

            NSSelector selector = selectors[type];

            String keypath = cri.keyPath();
            int comparand = cri.comparandType();
            Object value = cri.value();

            if (comparand == AdvancedQueryCriterion.COMPARAND_LITERAL)
            {
                q = new EOKeyValueQualifier(keypath, selector, value);
            }
            else
            {
                q = new EOKeyComparisonQualifier(keypath, selector,
                    (String)value);
            }

            return q;
        }


        //~ Instance/static variables .........................................

        private int type;

        private static final String[] descriptions = {
            "==", "!=", "<", "<=", ">", ">="
        };

        private static final NSSelector[] selectors = {
            EOQualifier.QualifierOperatorEqual,
            EOQualifier.QualifierOperatorNotEqual,
            EOQualifier.QualifierOperatorLessThan,
            EOQualifier.QualifierOperatorLessThanOrEqualTo,
            EOQualifier.QualifierOperatorGreaterThan,
            EOQualifier.QualifierOperatorGreaterThanOrEqualTo
        };
    }


    // ----------------------------------------------------------
    /**
     * Implements comparison logic for the basic relational operators.
     */
    private static class BetweenComparison
        extends AdvancedQueryComparison
    {
        //~ Constructor .......................................................

        // ----------------------------------------------------------
        public BetweenComparison(boolean negate)
        {
            this.negate = negate;
        }


        //~ Public Methods ....................................................

        // ----------------------------------------------------------
        public String toString()
        {
            return negate
                ? "is not between"
                : "is between";
        }


        // ----------------------------------------------------------
        @Override
        public boolean doesSupportKeyPaths()
        {
            return false;
        }


        // ----------------------------------------------------------
        @Override
        public boolean doesSupportMultipleValues()
        {
            return false;
        }


        // ----------------------------------------------------------
        @Override
        public boolean hasSecondOperand()
        {
            return true;
        }


        //~ Protected Methods .................................................

        // ----------------------------------------------------------
        @Override
        protected AdvancedQueryCriterion _criterionForQualifier(EOQualifier q)
        {
            AdvancedQueryCriterion criterion = new AdvancedQueryCriterion();

            ERXBetweenQualifier bq = null;

            if (q instanceof ERXBetweenQualifier)
            {
                bq = (ERXBetweenQualifier)q;
            }
            else if (q instanceof EONotQualifier)
            {
                EONotQualifier nq = (EONotQualifier)q;

                if (nq.qualifier() instanceof ERXBetweenQualifier)
                {
                    bq = (ERXBetweenQualifier)nq.qualifier();
                }
            }

            if(bq == null)
            {
                return null;
            }

            criterion.setKeyPath(bq.key());
            criterion.setComparandType(
                AdvancedQueryCriterion.COMPARAND_LITERAL);

            if (bq.minimumValue() != null || bq.maximumValue() != null)
            {
                NSMutableDictionary<String, Object> values =
                    new NSMutableDictionary<String, Object>();
                values.setObjectForKey(bq.minimumValue(), "minimumValue");
                values.setObjectForKey(bq.maximumValue(), "maximumValue");
                criterion.setValue(values);
            }
            else
            {
                criterion.setValue(null);
            }

            return criterion;
        }


        // ----------------------------------------------------------
        @Override
        protected EOQualifier _qualifierForCriterion(AdvancedQueryCriterion cri)
        {
            String keypath = cri.keyPath();

            NSDictionary<String, Object> values =
                (NSDictionary<String, Object>)cri.value();

            Object minimum = null, maximum = null;

            if (values != null)
            {
                minimum = values.objectForKey("minimumValue");
                maximum = values.objectForKey("maximumValue");
            }

            ERXBetweenQualifier bq = new ERXBetweenQualifier(keypath, minimum,
                maximum);

            if (negate)
            {
                return new EONotQualifier(bq);
            }
            else
            {
                return bq;
            }
        }


        //~ Instance/static variables .........................................

        private boolean negate;
    }


    // ----------------------------------------------------------
    /**
     * Implements comparison logic for the basic relational operators.
     */
    private static class LikeComparison
        extends AdvancedQueryComparison
    {
        //~ Constructor .......................................................

        // ----------------------------------------------------------
        public LikeComparison(boolean negate)
        {
            this.negate = negate;
        }


        //~ Public Methods ....................................................

        // ----------------------------------------------------------
        public String toString()
        {
            return negate
                ? "is not like"
                : "is like";
        }


        // ----------------------------------------------------------
        @Override
        public boolean doesSupportKeyPaths()
        {
            return true;
        }


        // ----------------------------------------------------------
        @Override
        public boolean doesSupportMultipleValues()
        {
            return false;
        }


        // ----------------------------------------------------------
        @Override
        public boolean hasSecondOperand()
        {
            return true;
        }


        //~ Protected Methods .................................................

        // ----------------------------------------------------------
        @Override
        protected AdvancedQueryCriterion _criterionForQualifier(EOQualifier q)
        {
            AdvancedQueryCriterion criterion = new AdvancedQueryCriterion();

            if (q instanceof EONotQualifier)
            {
                q = ((EONotQualifier)q).qualifier();
            }

            if (q instanceof EOKeyValueQualifier)
            {
                EOKeyValueQualifier kvq = (EOKeyValueQualifier)q;

                criterion.setKeyPath(kvq.key());
                criterion.setComparandType(
                    AdvancedQueryCriterion.COMPARAND_LITERAL);
                criterion.setValue(kvq.value());
            }
            else if (q instanceof EOKeyComparisonQualifier)
            {
                EOKeyComparisonQualifier kcq = (EOKeyComparisonQualifier)q;

                criterion.setKeyPath(kcq.leftKey());
                criterion.setComparandType(
                    AdvancedQueryCriterion.COMPARAND_KEYPATH);
                criterion.setValue(kcq.rightKey());
            }

            return criterion;
        }


        // ----------------------------------------------------------
        @Override
        protected EOQualifier _qualifierForCriterion(AdvancedQueryCriterion cri)
        {
            EOQualifier q = null;

            String keypath = cri.keyPath();
            int comparand = cri.comparandType();
            Object value = cri.value();

            if (comparand == AdvancedQueryCriterion.COMPARAND_LITERAL)
            {
                q = new EOKeyValueQualifier(keypath,
                    EOQualifier.QualifierOperatorCaseInsensitiveLike,
                    value);
            }
            else
            {
                q = new EOKeyComparisonQualifier(keypath,
                    EOQualifier.QualifierOperatorCaseInsensitiveLike,
                    (String)value);
            }

            return negate
                ? new EONotQualifier(q)
                : q;
        }


        //~ Instance/static variables .........................................

        private boolean negate;
    }


    // ----------------------------------------------------------
    /**
     * Implements comparison logic for the IN operator.
     */
    private static class InComparison
        extends AdvancedQueryComparison
    {
        //~ Constructor .......................................................

        // ----------------------------------------------------------
        public InComparison(boolean negate)
        {
            this.negate = negate;
        }


        //~ Public Methods ....................................................

        // ----------------------------------------------------------
        public String toString()
        {
            return negate
                ? "is not one of"
                : "is one of";
        }


        // ----------------------------------------------------------
        @Override
        public boolean doesSupportKeyPaths()
        {
            return true;
        }


        // ----------------------------------------------------------
        @Override
        public boolean doesSupportMultipleValues()
        {
            return true;
        }


        // ----------------------------------------------------------
        @Override
        public boolean hasSecondOperand()
        {
            return true;
        }


        //~ Protected Methods .................................................

        // ----------------------------------------------------------
        @Override
        protected AdvancedQueryCriterion _criterionForQualifier(EOQualifier q)
        {
            AdvancedQueryCriterion criterion = new AdvancedQueryCriterion();

            if (q instanceof EONotQualifier)
            {
                q = ((EONotQualifier)q).qualifier();
            }

            NSDictionary<String, Object> info =
                QualifierUtils.infoIfInQualifier(q);

            if (info != null)
            {
                criterion.setKeyPath((String)info.objectForKey("key"));
                criterion.setComparandType(
                    AdvancedQueryCriterion.COMPARAND_LITERAL);
                criterion.setValue(info.objectForKey("values"));
            }
            else if (q instanceof QualifierInSubquery)
            {
                QualifierInSubquery qis = (QualifierInSubquery) q;
                EOQualifier sq = qis.qualifier();
                
                if (sq instanceof EOKeyComparisonQualifier)
                {
                    EOKeyComparisonQualifier kcq =
                        (EOKeyComparisonQualifier) sq;
                    
                    String leftKey = kcq.leftKey();
                    String rightKey = kcq.rightKey();
                    
                    if (leftKey.endsWith(".id") && rightKey.endsWith(".id"))
                    {
                        criterion.setKeyPath(
                                leftKey.substring(0, leftKey.length() - 3));
                        criterion.setComparandType(
                                AdvancedQueryCriterion.COMPARAND_KEYPATH);
                        criterion.setValue(
                                rightKey.substring(0, rightKey.length() - 3));
                    }
                }
            }

            return criterion;
        }


        // ----------------------------------------------------------
        @Override
        protected EOQualifier _qualifierForCriterion(AdvancedQueryCriterion cri)
        {
            EOQualifier q = null;

            if (cri.comparandType() == AdvancedQueryCriterion.COMPARAND_LITERAL)
            {
                String keypath = cri.keyPath();
                NSArray<Object> values = (NSArray<Object>)cri.value();

                if (values != null)
                {
                    q = QualifierUtils.qualifierForInCondition(keypath, values);

                    return negate ? new EONotQualifier(q) : q;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                String keyPath = cri.keyPath();
                String relationship = (String)cri.value();
                
                if (keyPath != null && relationship != null)
                {
                    q = QualifierUtils.qualifierForKeyPathInRelationship(
                            keyPath, relationship);
                }
                
                return negate ? new EONotQualifier(q) : q;
            }
        }


        //~ Instance/static variables .........................................

        private boolean negate;
    }


    //~ Instance/static variables .............................................

    private static final int SIMPLE_EQ = 0;
    private static final int SIMPLE_NE = 1;
    private static final int SIMPLE_LT = 2;
    private static final int SIMPLE_LE = 3;
    private static final int SIMPLE_GT = 4;
    private static final int SIMPLE_GE = 5;

    private static NSMutableDictionary<String, AdvancedQueryComparison>
        nameToComparisonMap;

    private static NSMutableArray<AdvancedQueryComparison> booleanComparisons;
    private static NSMutableArray<AdvancedQueryComparison> numericComparisons;
    private static NSMutableArray<AdvancedQueryComparison> stringComparisons;
    private static NSMutableArray<AdvancedQueryComparison> timestampComparisons;
    private static NSMutableArray<AdvancedQueryComparison> objectComparisons;

    private static NSMutableDictionary<AdvancedQueryComparison, Boolean>
        keyPathSupport;


    // ----------------------------------------------------------
    // Initialize static variables
    static
    {
        IS_EQUAL_TO = new SimpleComparison(SIMPLE_EQ);
        IS_NOT_EQUAL_TO = new SimpleComparison(SIMPLE_NE);
        IS_LESS_THAN = new SimpleComparison(SIMPLE_LT);
        IS_LESS_THAN_OR_EQUAL_TO = new SimpleComparison(SIMPLE_LE);
        IS_GREATER_THAN = new SimpleComparison(SIMPLE_GT);
        IS_GREATER_THAN_OR_EQUAL_TO = new SimpleComparison(SIMPLE_GE);
        IS_BETWEEN = new BetweenComparison(false);
        IS_NOT_BETWEEN = new BetweenComparison(true);
        IS_LIKE = new LikeComparison(false);
        IS_NOT_LIKE = new LikeComparison(true);
        IS_ONE_OF = new InComparison(false);
        IS_NOT_ONE_OF = new InComparison(true);

        booleanComparisons = new NSMutableArray<AdvancedQueryComparison>();
        booleanComparisons.add(IS_EQUAL_TO);
        booleanComparisons.add(IS_NOT_EQUAL_TO);

        numericComparisons = new NSMutableArray<AdvancedQueryComparison>();
        numericComparisons.add(IS_EQUAL_TO);
        numericComparisons.add(IS_NOT_EQUAL_TO);
        numericComparisons.add(IS_LESS_THAN);
        numericComparisons.add(IS_LESS_THAN_OR_EQUAL_TO);
        numericComparisons.add(IS_GREATER_THAN);
        numericComparisons.add(IS_GREATER_THAN_OR_EQUAL_TO);
        numericComparisons.add(IS_BETWEEN);
        numericComparisons.add(IS_NOT_BETWEEN);
        numericComparisons.add(IS_ONE_OF);
        numericComparisons.add(IS_NOT_ONE_OF);

        stringComparisons = new NSMutableArray<AdvancedQueryComparison>();
        stringComparisons.add(IS_EQUAL_TO);
        stringComparisons.add(IS_NOT_EQUAL_TO);
        stringComparisons.add(IS_BETWEEN);
        stringComparisons.add(IS_NOT_BETWEEN);
        stringComparisons.add(IS_LIKE);
        stringComparisons.add(IS_NOT_LIKE);
        stringComparisons.add(IS_ONE_OF);
        stringComparisons.add(IS_NOT_ONE_OF);

        timestampComparisons = new NSMutableArray<AdvancedQueryComparison>();
        timestampComparisons.add(IS_EQUAL_TO);
        timestampComparisons.add(IS_NOT_EQUAL_TO);
        timestampComparisons.add(IS_LESS_THAN);
        timestampComparisons.add(IS_LESS_THAN_OR_EQUAL_TO);
        timestampComparisons.add(IS_GREATER_THAN);
        timestampComparisons.add(IS_GREATER_THAN_OR_EQUAL_TO);
        timestampComparisons.add(IS_BETWEEN);
        timestampComparisons.add(IS_NOT_BETWEEN);

        objectComparisons = new NSMutableArray<AdvancedQueryComparison>();
        objectComparisons.add(IS_EQUAL_TO);
        objectComparisons.add(IS_NOT_EQUAL_TO);
        objectComparisons.add(IS_ONE_OF);
        objectComparisons.add(IS_NOT_ONE_OF);

        keyPathSupport = new NSMutableDictionary<AdvancedQueryComparison, Boolean>();
        keyPathSupport.setObjectForKey(true, IS_EQUAL_TO);
        keyPathSupport.setObjectForKey(true, IS_NOT_EQUAL_TO);
        keyPathSupport.setObjectForKey(true, IS_LESS_THAN);
        keyPathSupport.setObjectForKey(true, IS_LESS_THAN_OR_EQUAL_TO);
        keyPathSupport.setObjectForKey(true, IS_GREATER_THAN);
        keyPathSupport.setObjectForKey(true, IS_GREATER_THAN_OR_EQUAL_TO);
        keyPathSupport.setObjectForKey(false, IS_BETWEEN);
        keyPathSupport.setObjectForKey(false, IS_NOT_BETWEEN);
        keyPathSupport.setObjectForKey(true, IS_LIKE);
        keyPathSupport.setObjectForKey(true, IS_NOT_LIKE);
        keyPathSupport.setObjectForKey(true, IS_ONE_OF);
        keyPathSupport.setObjectForKey(true, IS_NOT_ONE_OF);

        nameToComparisonMap = new NSMutableDictionary<String, AdvancedQueryComparison>();
        nameToComparisonMap.setObjectForKey(IS_EQUAL_TO, "IS_EQUAL_TO");
        nameToComparisonMap.setObjectForKey(IS_NOT_EQUAL_TO, "IS_NOT_EQUAL_TO");
        nameToComparisonMap.setObjectForKey(IS_LESS_THAN, "IS_LESS_THAN");
        nameToComparisonMap.setObjectForKey(IS_LESS_THAN_OR_EQUAL_TO, "IS_LESS_THAN_OR_EQUAL_TO");
        nameToComparisonMap.setObjectForKey(IS_GREATER_THAN, "IS_GREATER_THAN");
        nameToComparisonMap.setObjectForKey(IS_GREATER_THAN_OR_EQUAL_TO, "IS_GREATER_THAN_OR_EQUAL_TO");
        nameToComparisonMap.setObjectForKey(IS_BETWEEN, "IS_BETWEEN");
        nameToComparisonMap.setObjectForKey(IS_NOT_BETWEEN, "IS_NOT_BETWEEN");
        nameToComparisonMap.setObjectForKey(IS_LIKE, "IS_LIKE");
        nameToComparisonMap.setObjectForKey(IS_NOT_LIKE, "IS_NOT_LIKE");
        nameToComparisonMap.setObjectForKey(IS_ONE_OF, "IS_ONE_OF");
        nameToComparisonMap.setObjectForKey(IS_NOT_ONE_OF, "IS_NOT_ONE_OF");
    }
}
