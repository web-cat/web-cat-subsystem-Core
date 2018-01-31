/*==========================================================================*\
 |  $Id: QualifierSerialization.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.eof.qualifiers.ERXInQualifier;
import er.extensions.qualifiers.ERXKeyValueQualifier;

//-------------------------------------------------------------------------
/**
 * Methods used to serialize and deserialize qualifiers from the database, by
 * replacing any EOs that they are bound to with the corresponding global IDs,
 * and vice-versa.
 *
 * @author Tony Allevato
 * @version $Id: QualifierSerialization.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public class QualifierSerialization
{
    // ----------------------------------------------------------
    /**
     * Static class; prevent instantiation.
     */
    private QualifierSerialization()
    {
        // Do nothing
    }


    // ----------------------------------------------------------
    /**
     * Deeply traverses a qualifier tree and returns a new qualifier where
     * any EOs have been replaced by EOGlobalIDs that refer to them. This is
     * used before a qualifier is serialized into the database.
     *
     * @param q the qualifier to convert
     * @param ec the editing context to use
     * @return a new qualifier that has had EOs replaced by EOGlobalIDs
     */
    public static EOQualifier convertEOsToGIDs(EOQualifier q,
            EOEditingContext ec)
    {
        if (q == null)
        {
            return null;
        }

        Class<?> qType = q.getClass();
        IConverter converter = converters.objectForKey(qType);

        if (converter != null)
        {
            return converter.convertEOsToGIDs(q, ec);
        }
        else
        {
            return (EOQualifier) q.clone();
        }
    }


    // ----------------------------------------------------------
    /**
     * Deeply traverses a qualifier tree and returns a new qualifier where
     * any EOGlobalIDs have been replaced by the EOs that they refer to. This
     * is used to reconstitute a qualifier after it has been read in from the
     * database.
     *
     * @param q the qualifier to convert
     * @param ec the editing context to use
     * @return a new qualifier that has had EOGlobalIDs replaced by EOs
     */
    public static EOQualifier convertGIDsToEOs(EOQualifier q,
            EOEditingContext ec)
    {
        if (q == null)
        {
            return null;
        }

        Class<?> qType = q.getClass();
        IConverter converter = converters.objectForKey(qType);

        if (converter != null)
        {
            return converter.convertGIDsToEOs(q, ec);
        }
        else
        {
            return (EOQualifier) q.clone();
        }
    }


    // ----------------------------------------------------------
    /**
     * The interface that defines the methods used to serialize and
     * deserialize qualifiers.
     */
    private interface IConverter
    {
        // ----------------------------------------------------------
        EOQualifier convertEOsToGIDs(EOQualifier q, EOEditingContext ec);


        // ----------------------------------------------------------
        EOQualifier convertGIDsToEOs(EOQualifier q, EOEditingContext ec);
    }


    // ----------------------------------------------------------
    private static class AndQualifierConverter implements IConverter
    {
        // ----------------------------------------------------------
        public EOQualifier convertEOsToGIDs(EOQualifier q, EOEditingContext ec)
        {
            EOAndQualifier aq = (EOAndQualifier) q;

            NSMutableArray<EOQualifier> children =
                new NSMutableArray<EOQualifier>();

            for (EOQualifier child : aq.qualifiers())
            {
                children.addObject(QualifierSerialization.convertEOsToGIDs(
                        child, ec));
            }

            return new EOAndQualifier(children);
        }


        // ----------------------------------------------------------
        public EOQualifier convertGIDsToEOs(EOQualifier q, EOEditingContext ec)
        {
            EOAndQualifier aq = (EOAndQualifier) q;

            NSMutableArray<EOQualifier> children =
                new NSMutableArray<EOQualifier>();

            for (EOQualifier child : aq.qualifiers())
            {
                children.addObject(QualifierSerialization.convertGIDsToEOs(child,
                        ec));
            }

            return new EOAndQualifier(children);
        }
    }


    // ----------------------------------------------------------
    private static class OrQualifierConverter implements IConverter
    {
        // ----------------------------------------------------------
        public EOQualifier convertEOsToGIDs(EOQualifier q, EOEditingContext ec)
        {
            EOOrQualifier oq = (EOOrQualifier) q;

            NSMutableArray<EOQualifier> children =
                new NSMutableArray<EOQualifier>();

            for (EOQualifier child : oq.qualifiers())
            {
                children.addObject(QualifierSerialization.convertEOsToGIDs(child,
                        ec));
            }

            return new EOOrQualifier(children);
        }


        // ----------------------------------------------------------
        public EOQualifier convertGIDsToEOs(EOQualifier q, EOEditingContext ec)
        {
            EOOrQualifier oq = (EOOrQualifier) q;

            NSMutableArray<EOQualifier> children =
                new NSMutableArray<EOQualifier>();

            for (EOQualifier child : oq.qualifiers())
            {
                children.addObject(QualifierSerialization.convertGIDsToEOs(child,
                        ec));
            }

            return new EOOrQualifier(children);
        }
    }


    // ----------------------------------------------------------
    private static class NotQualifierConverter implements IConverter
    {
        // ----------------------------------------------------------
        public EOQualifier convertEOsToGIDs(EOQualifier q, EOEditingContext ec)
        {
            EONotQualifier nq = (EONotQualifier) q;
            return new EONotQualifier(QualifierSerialization.convertEOsToGIDs(nq
                    .qualifier(), ec));
        }


        // ----------------------------------------------------------
        public EOQualifier convertGIDsToEOs(EOQualifier q, EOEditingContext ec)
        {
            EONotQualifier nq = (EONotQualifier) q;
            return new EONotQualifier(QualifierSerialization.convertGIDsToEOs(nq
                    .qualifier(), ec));
        }
    }


    // ----------------------------------------------------------
    private static class InQualifierConverter implements IConverter
    {
        // ----------------------------------------------------------
        public EOQualifier convertEOsToGIDs(EOQualifier q, EOEditingContext ec)
        {
            ERXInQualifier iq = (ERXInQualifier) q;
            NSMutableArray<Object> values = new NSMutableArray<Object>();

            for (Object value : iq.values())
            {
                if (value instanceof EOEnterpriseObject)
                {
                    values.addObject(ec
                            .globalIDForObject((EOEnterpriseObject) value));
                }
                else
                {
                    values.addObject(value);
                }
            }

            return new ERXInQualifier(iq.key(), values);
        }


        // ----------------------------------------------------------
        public EOQualifier convertGIDsToEOs(EOQualifier q, EOEditingContext ec)
        {
            ERXInQualifier iq = (ERXInQualifier) q;
            NSMutableArray<Object> values = new NSMutableArray<Object>();

            for (Object value : iq.values())
            {
                if (value instanceof EOGlobalID)
                {
                    values.addObject(ec
                            .faultForGlobalID((EOGlobalID) value, ec));
                }
                else
                {
                    values.addObject(value);
                }
            }

            return new ERXInQualifier(iq.key(), values);
        }
    }


    // ----------------------------------------------------------
    private static class KeyValueQualifierConverter implements IConverter
    {
        // ----------------------------------------------------------
        public EOQualifier convertEOsToGIDs(EOQualifier q, EOEditingContext ec)
        {
            EOKeyValueQualifier kvq = (EOKeyValueQualifier) q;
            Object value;

            if (kvq.value() instanceof EOEnterpriseObject)
            {
                value = ec.globalIDForObject((EOEnterpriseObject) kvq.value());
            }
            else
            {
                value = kvq.value();
            }

            return new EOKeyValueQualifier(kvq.key(), kvq.selector(), value);
        }


        // ----------------------------------------------------------
        public EOQualifier convertGIDsToEOs(EOQualifier q, EOEditingContext ec)
        {
            EOKeyValueQualifier kvq = (EOKeyValueQualifier) q;
            Object value;

            if (kvq.value() instanceof EOGlobalID)
            {
                value = ec.faultForGlobalID((EOGlobalID) kvq.value(), ec);
            }
            else
            {
                value = kvq.value();
            }

            return new EOKeyValueQualifier(kvq.key(), kvq.selector(), value);
        }
    }


    // ----------------------------------------------------------
    private static class ERXKeyValueQualifierConverter implements IConverter
    {
        // ----------------------------------------------------------
        public EOQualifier convertEOsToGIDs(EOQualifier q, EOEditingContext ec)
        {
            ERXKeyValueQualifier kvq = (ERXKeyValueQualifier) q;
            Object value;

            if (kvq.value() instanceof EOEnterpriseObject)
            {
                value = ec.globalIDForObject((EOEnterpriseObject) kvq.value());
            }
            else
            {
                value = kvq.value();
            }

            return new ERXKeyValueQualifier(kvq.key(), kvq.selector(), value);
        }


        // ----------------------------------------------------------
        public EOQualifier convertGIDsToEOs(EOQualifier q, EOEditingContext ec)
        {
            ERXKeyValueQualifier kvq = (ERXKeyValueQualifier) q;
            Object value;

            if (kvq.value() instanceof EOGlobalID)
            {
                value = ec.faultForGlobalID((EOGlobalID) kvq.value(), ec);
            }
            else
            {
                value = kvq.value();
            }

            return new ERXKeyValueQualifier(kvq.key(), kvq.selector(), value);
        }
    }


    // ----------------------------------------------------------
    private static class SubqueryQualifierConverter implements IConverter
    {
        // ----------------------------------------------------------
        public EOQualifier convertEOsToGIDs(EOQualifier q, EOEditingContext ec)
        {
            QualifierInSubquery qis = (QualifierInSubquery) q;

            EOQualifier converted = QualifierSerialization.convertEOsToGIDs(
                    qis.qualifier(), ec);

            if (qis.relationshipName() != null)
            {
                return new QualifierInSubquery(converted, qis.entityName(),
                        qis.relationshipName());
            }
            else
            {
                return new QualifierInSubquery(converted, qis.entityName(),
                        qis.attributeName(), qis.destinationAttributeName());
            }
        }


        // ----------------------------------------------------------
        public EOQualifier convertGIDsToEOs(EOQualifier q, EOEditingContext ec)
        {
            QualifierInSubquery qis = (QualifierInSubquery) q;

            EOQualifier converted = QualifierSerialization.convertGIDsToEOs(
                    qis.qualifier(), ec);

            if (qis.relationshipName() != null)
            {
                return new QualifierInSubquery(converted, qis.entityName(),
                        qis.relationshipName());
            }
            else
            {
                return new QualifierInSubquery(converted, qis.entityName(),
                        qis.attributeName(), qis.destinationAttributeName());
            }
        }
    }


    //~ Static variables ......................................................

    /** A mapping between qualifier classes and converter instances. */
    private static NSMutableDictionary<Class<?>, IConverter> converters;


    static
    {
        converters = new NSMutableDictionary<Class<?>, IConverter>();

        converters.setObjectForKey(new AndQualifierConverter(),
                EOAndQualifier.class);
        converters.setObjectForKey(new OrQualifierConverter(),
                EOOrQualifier.class);
        converters.setObjectForKey(new NotQualifierConverter(),
                EONotQualifier.class);
        converters.setObjectForKey(new InQualifierConverter(),
                ERXInQualifier.class);
        converters.setObjectForKey(new KeyValueQualifierConverter(),
                EOKeyValueQualifier.class);
        converters.setObjectForKey(new ERXKeyValueQualifierConverter(),
                ERXKeyValueQualifier.class);
        converters.setObjectForKey(new SubqueryQualifierConverter(),
                QualifierInSubquery.class);
    }
}
