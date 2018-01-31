/*==========================================================================*\
 |  $Id: QualifierAugmenter.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.eof.ERXQ;
import er.extensions.eof.qualifiers.ERXBetweenQualifier;
import er.extensions.eof.qualifiers.ERXInQualifier;
import er.extensions.qualifiers.ERXKeyComparisonQualifier;
import er.extensions.qualifiers.ERXKeyValueQualifier;

//-------------------------------------------------------------------------
/**
 * Methods used to augment qualifiers in order to support the proper fetching
 * of objects that have migratory attributes.
 *
 * @author Tony Allevato
 * @version $Id: QualifierAugmenter.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public class QualifierAugmenter
{
    // ----------------------------------------------------------
    public QualifierAugmenter(String entityName, EOQualifier q)
    {
        this.entityName = entityName;
        this.augmentedKeyPaths = new NSMutableArray<String>();

        originalQualifier = q;
        augmentedQualifier = augmentQualifier(q, this);
    }


    // ----------------------------------------------------------
    public EOQualifier originalQualifier()
    {
        return originalQualifier;
    }


    // ----------------------------------------------------------
    public EOQualifier augmentedQualifier()
    {
        return augmentedQualifier;
    }


    // ----------------------------------------------------------
    public NSArray<String> augmentedKeyPaths()
    {
        return augmentedKeyPaths;
    }


    // ----------------------------------------------------------
    public boolean isSignificantDifference()
    {
        return (augmentedKeyPaths.count() > 0);
    }


    // ----------------------------------------------------------
    /**
     * Deeply traverses a qualifier tree and returns a new qualifier where
     * any comparisons involving migratory attributes have been augmented to
     * the form
     * <code>(original attribute comparison OR attribute == NULL)</code>.
     *
     * @param q the qualifier to augmented

     * @return a new qualifier that has been augmented
     */
    private static EOQualifier augmentQualifier(EOQualifier q,
            QualifierAugmenter augmenter)
    {
        if (q == null)
        {
            return null;
        }

        Class<?> qType = q.getClass();
        IAugmenter auger = augmenters.objectForKey(qType);

        if (auger != null)
        {
            return auger.augmentQualifier(q, augmenter);
        }
        else
        {
            return (EOQualifier) q.clone();
        }
    }


    // ----------------------------------------------------------
    private void addAugmentedKeyPath(String keypath)
    {
        augmentedKeyPaths.addObject(keypath);
    }


    // ----------------------------------------------------------
    private boolean isKeypathMigratoryAttribute(String keypath)
    {
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);

        if (entity == null)
            return false;

        EOAttribute attribute = entity._attributeForPath(keypath);

        if (attribute == null || attribute.userInfo() == null)
            return false;

        String needsMigration = (String) attribute.userInfo().objectForKey(
                "needsMigration");

        return (needsMigration != null && Boolean.parseBoolean(needsMigration));
    }


    // ----------------------------------------------------------
    /**
     * The interface that defines the methods used to augment qualifiers.
     */
    private interface IAugmenter
    {
        // ----------------------------------------------------------
        EOQualifier augmentQualifier(EOQualifier q,
                QualifierAugmenter augmenter);
    }


    // ----------------------------------------------------------
    private static class AndQualifierAugmenter implements IAugmenter
    {
        // ----------------------------------------------------------
        public EOQualifier augmentQualifier(EOQualifier q,
                QualifierAugmenter augmenter)
        {
            EOAndQualifier aq = (EOAndQualifier) q;

            NSMutableArray<EOQualifier> children =
                new NSMutableArray<EOQualifier>();

            for (EOQualifier child : aq.qualifiers())
            {
                children.addObject(QualifierAugmenter.augmentQualifier(
                        child, augmenter));
            }

            return new EOAndQualifier(children);
        }
    }


    // ----------------------------------------------------------
    private static class OrQualifierAugmenter implements IAugmenter
    {
        // ----------------------------------------------------------
        public EOQualifier augmentQualifier(EOQualifier q,
                QualifierAugmenter augmenter)
        {
            EOOrQualifier oq = (EOOrQualifier) q;

            NSMutableArray<EOQualifier> children =
                new NSMutableArray<EOQualifier>();

            for (EOQualifier child : oq.qualifiers())
            {
                children.addObject(QualifierAugmenter.augmentQualifier(
                        child, augmenter));
            }

            return new EOOrQualifier(children);
        }
    }


    // ----------------------------------------------------------
    private static class NotQualifierAugmenter implements IAugmenter
    {
        // ----------------------------------------------------------
        public EOQualifier augmentQualifier(EOQualifier q,
                QualifierAugmenter augmenter)
        {
            EONotQualifier nq = (EONotQualifier) q;

            return ERXQ.not(QualifierAugmenter.augmentQualifier(nq
                    .qualifier(), augmenter));
        }
    }


    // ----------------------------------------------------------
    private static class InQualifierAugmenter implements IAugmenter
    {
        // ----------------------------------------------------------
        public EOQualifier augmentQualifier(EOQualifier q,
                QualifierAugmenter augmenter)
        {
            ERXInQualifier iq = (ERXInQualifier) q;

            if (augmenter.isKeypathMigratoryAttribute(iq.key()))
            {
                augmenter.addAugmentedKeyPath(iq.key());

                return ERXQ.or(
                        new ERXInQualifier(iq.key(), iq.values()),
                        ERXQ.isNull(iq.key()));
            }
            else
            {
                return (EOQualifier) iq.clone();
            }
        }
    }


    // ----------------------------------------------------------
    private static class BetweenQualifierAugmenter implements IAugmenter
    {
        // ----------------------------------------------------------
        public EOQualifier augmentQualifier(EOQualifier q,
                QualifierAugmenter augmenter)
        {
            ERXBetweenQualifier bq = (ERXBetweenQualifier) q;

            if (augmenter.isKeypathMigratoryAttribute(bq.key()))
            {
                augmenter.addAugmentedKeyPath(bq.key());

                return ERXQ.or(
                        new ERXBetweenQualifier(
                                bq.key(), bq.minimumValue(), bq.maximumValue()),
                        ERXQ.isNull(bq.key()));
            }
            else
            {
                return (EOQualifier) bq.clone();
            }
        }
    }


    // ----------------------------------------------------------
    private static class KeyValueQualifierAugmenter implements IAugmenter
    {
        // ----------------------------------------------------------
        public EOQualifier augmentQualifier(EOQualifier q,
                QualifierAugmenter augmenter)
        {
            EOKeyValueQualifier kvq = (EOKeyValueQualifier) q;

            if (augmenter.isKeypathMigratoryAttribute(kvq.key()))
            {
                augmenter.addAugmentedKeyPath(kvq.key());

                return ERXQ.or(
                        (EOQualifier) kvq.clone(),
                        ERXQ.isNull(kvq.key()));
            }
            else
            {
                return (EOQualifier) kvq.clone();
            }
        }
    }


    // ----------------------------------------------------------
    private static class ERXKeyValueQualifierAugmenter implements IAugmenter
    {
        // ----------------------------------------------------------
        public EOQualifier augmentQualifier(EOQualifier q,
                QualifierAugmenter augmenter)
        {
            ERXKeyValueQualifier kvq = (ERXKeyValueQualifier) q;

            if (augmenter.isKeypathMigratoryAttribute(kvq.key()))
            {
                augmenter.addAugmentedKeyPath(kvq.key());

                return ERXQ.or(
                        (EOQualifier) kvq.clone(),
                        ERXQ.isNull(kvq.key()));
            }
            else
            {
                return (EOQualifier) kvq.clone();
            }
        }
    }


    // ----------------------------------------------------------
    private static class KeyComparisonQualifierAugmenter implements IAugmenter
    {
        // ----------------------------------------------------------
        public EOQualifier augmentQualifier(EOQualifier q,
                QualifierAugmenter augmenter)
        {
            EOKeyComparisonQualifier kcq = (EOKeyComparisonQualifier) q;

            NSMutableArray<EOQualifier> subq =
                new NSMutableArray<EOQualifier>();

            if (augmenter.isKeypathMigratoryAttribute(kcq.leftKey()))
            {
                augmenter.addAugmentedKeyPath(kcq.leftKey());
                subq.addObject(ERXQ.isNull(kcq.leftKey()));
            }

            if (augmenter.isKeypathMigratoryAttribute(kcq.rightKey()))
            {
                augmenter.addAugmentedKeyPath(kcq.rightKey());
                subq.addObject(ERXQ.isNull(kcq.rightKey()));
            }

            if (subq.count() > 0)
            {
                subq.insertObjectAtIndex((EOQualifier) kcq.clone(), 0);
                return ERXQ.or((EOQualifier[]) subq.toArray());
            }
            else
            {
                return (EOQualifier) kcq.clone();
            }
        }
    }


    // ----------------------------------------------------------
    private static class ERXKeyComparisonQualifierAugmenter
    implements IAugmenter
    {
        // ----------------------------------------------------------
        public EOQualifier augmentQualifier(EOQualifier q,
                QualifierAugmenter augmenter)
        {
            ERXKeyComparisonQualifier kcq = (ERXKeyComparisonQualifier) q;

            NSMutableArray<EOQualifier> subq =
                new NSMutableArray<EOQualifier>();

            if (augmenter.isKeypathMigratoryAttribute(kcq.leftKey()))
            {
                augmenter.addAugmentedKeyPath(kcq.leftKey());
                subq.addObject(ERXQ.isNull(kcq.leftKey()));
            }

            if (augmenter.isKeypathMigratoryAttribute(kcq.rightKey()))
            {
                augmenter.addAugmentedKeyPath(kcq.rightKey());
                subq.addObject(ERXQ.isNull(kcq.rightKey()));
            }

            if (subq.count() > 0)
            {
                subq.insertObjectAtIndex((EOQualifier) kcq.clone(), 0);
                return ERXQ.or((EOQualifier[]) subq.toArray());
            }
            else
            {
                return (EOQualifier) kcq.clone();
            }
        }
    }


    // ----------------------------------------------------------
    private static class SubqueryQualifierAugmenter implements IAugmenter
    {
        // ----------------------------------------------------------
        public EOQualifier augmentQualifier(EOQualifier q,
                QualifierAugmenter augmenter)
        {
            QualifierInSubquery qis = (QualifierInSubquery) q;

            EOQualifier augq = QualifierAugmenter.augmentQualifier(
                    qis.qualifier(), augmenter);

            if (qis.relationshipName() != null)
            {
                return new QualifierInSubquery(augq, qis.entityName(),
                        qis.relationshipName());
            }
            else
            {
                return new QualifierInSubquery(augq, qis.entityName(),
                        qis.attributeName(), qis.destinationAttributeName());
            }
        }
    }


    //~ Static/instance variables .............................................

    private String entityName;
    private EOQualifier originalQualifier;
    private EOQualifier augmentedQualifier;
    private NSMutableArray<String> augmentedKeyPaths;

    /** A mapping between qualifier classes and converter instances. */
    private static NSMutableDictionary<Class<?>, IAugmenter> augmenters;


    static
    {
        augmenters = new NSMutableDictionary<Class<?>, IAugmenter>();

        augmenters.setObjectForKey(new AndQualifierAugmenter(),
                EOAndQualifier.class);
        augmenters.setObjectForKey(new OrQualifierAugmenter(),
                EOOrQualifier.class);
        augmenters.setObjectForKey(new NotQualifierAugmenter(),
                EONotQualifier.class);
        augmenters.setObjectForKey(new InQualifierAugmenter(),
                ERXInQualifier.class);
        augmenters.setObjectForKey(new BetweenQualifierAugmenter(),
                ERXBetweenQualifier.class);
        augmenters.setObjectForKey(new KeyValueQualifierAugmenter(),
                EOKeyValueQualifier.class);
        augmenters.setObjectForKey(new ERXKeyValueQualifierAugmenter(),
                ERXKeyValueQualifier.class);
        augmenters.setObjectForKey(new KeyComparisonQualifierAugmenter(),
                EOKeyComparisonQualifier.class);
        augmenters.setObjectForKey(new ERXKeyComparisonQualifierAugmenter(),
                ERXKeyComparisonQualifier.class);
        augmenters.setObjectForKey(new SubqueryQualifierAugmenter(),
                QualifierInSubquery.class);
    }
}
