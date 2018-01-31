/*==========================================================================*\
 |  $Id: ReadOnlyEditingContext.java,v 1.1 2011/12/25 02:24:54 stedwar2 Exp $
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

package org.webcat.woextensions;

import org.webcat.core.Application;
import org.webcat.core.QualifierAugmenter;
import org.webcat.woextensions.ReadOnlyEditingContext;
import org.apache.log4j.Logger;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;

//-------------------------------------------------------------------------
/**
 * An editing context that has its {@link #saveChanges()} method overridden to
 * throw out the changes, effectively making it read-only. Calls to methods
 * that would otherwise have altered the object store are logged as well as
 * errors, along with a stack trace showing where the modification was
 * attempted.
 * </p><p>
 * Use the {@link Application#newReadOnlyEditingContext()} and
 * {@link Application#releaseReadOnlyEditingContext()} methods to manage the
 * lifetimes of these objects.
 * </p>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.1 $, $Date: 2011/12/25 02:24:54 $
 */
public class ReadOnlyEditingContext
    extends WCEC
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     * @param os the parent object store
     */
    public ReadOnlyEditingContext(EOObjectStore os)
    {
        super(os);
    }


    // ----------------------------------------------------------
    /**
     * Creates a new peer editing context, typically used to make
     * changes outside of a session's editing context.
     * @return the new editing context
     */
    public static ReadOnlyEditingContext newEditingContext()
    {
        return (ReadOnlyEditingContext)factory()._newEditingContext();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public NSArray<EOEnterpriseObject> objectsWithFetchSpecification(
        EOFetchSpecification fspec, EOEditingContext ec)
    {
        // Augment the qualifier and use that for the original database fetch.
        EOFetchSpecification augFspec = (EOFetchSpecification) fspec.clone();

        EOQualifier q = fspec.qualifier();

        QualifierAugmenter augmenter = new QualifierAugmenter(
                fspec.entityName(), q);
        augFspec.setQualifier(augmenter.augmentedQualifier());

        @SuppressWarnings("unchecked")
        NSArray<EOEnterpriseObject> objects =
            super.objectsWithFetchSpecification(augFspec, ec);

        if (augmenter.isSignificantDifference())
        {
            // Since the objects have been fetched, this has caused their
            // migratory attributes to be populated. We can now filter this
            // array in-memory with the original qualifier.

            if (!fspec.fetchesRawRows())
            {
                objects = EOQualifier.filteredArrayWithQualifier(objects, q);
            }
        }

        return objects;
    }


    // ----------------------------------------------------------
    @Override
    public void deleteObject(EOEnterpriseObject object)
    {
        logModificationAttempt(
                "deleteObject method called on read-only editing context; " +
                "the operation was ignored.");
    }


    // ----------------------------------------------------------
    @Override
    public void insertObject(EOEnterpriseObject object)
    {
        logModificationAttempt(
                "insertObject method called on read-only editing context; " +
                "the operation was ignored.");
    }


    // ----------------------------------------------------------
    @Override
    public void insertObjectWithGlobalID(EOEnterpriseObject object,
            EOGlobalID gid)
    {
        logModificationAttempt(
                "insertObjectWithGlobalID method called on read-only " +
                "editing context; the operation was ignored.");
    }


    // ----------------------------------------------------------
    @Override
    public void saveChanges()
    {
        logModificationAttempt(
                "saveChanges method called on read-only editing context; " +
                "the operation was ignored.");
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether a modification attempt was made on this
     * editing context. Once this flag has been set by one of the modifying
     * methods, it remains set through the lifetime of the editing context and
     * can never be cleared.
     *
     * @return true if an attempt was made to modify the editing context,
     *     otherwise false.
     */
    public boolean modificationWasAttempted()
    {
        return modificationWasAttempted;
    }


    // ----------------------------------------------------------
    /**
     * Logs a modification attempt.
     *
     * @param message a message describing the modification attempt
     */
    private void logModificationAttempt(String message)
    {
        modificationWasAttempted = true;

        if (!loggingSuppressed)
        {
            log.error(message, new Throwable("called here"));

            if (suppressesLogAfterFirstAttempt)
            {
                setLoggingSuppressed(true);
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether logging is suppressed.
     *
     * @return true if suppressed, false if activated
     */
    public boolean isLoggingSuppressed()
    {
        return loggingSuppressed;
    }


    // ----------------------------------------------------------
    /**
     * Suppresses logging of modification attempts if true, or reactivates
     * logging if false.
     *
     * @param suppress true to suppress, false to activate
     */
    public void setLoggingSuppressed(boolean suppress)
    {
        loggingSuppressed = suppress;
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether logging should be suppressed after the
     * first modification attempt.
     *
     * @return true if suppressed, false if activated
     */
    public boolean suppressesLogAfterFirstAttempt()
    {
        return suppressesLogAfterFirstAttempt;
    }


    // ----------------------------------------------------------
    /**
     * Suppresses logging of modification attempts after the first one if true,
     * or logs every modification attempt if false. This acts as a form of
     * flood control when the editing context is being used repeatedly, such as
     * during report generation.
     *
     * @param suppress true to suppress after the first attempt, false to
     *     log all modification attempts
     */
    public void setSuppressesLogAfterFirstAttempt(boolean suppress)
    {
        suppressesLogAfterFirstAttempt = suppress;
    }


    // ----------------------------------------------------------
    public static class ReadOnlyFactory
        extends WCECFactory
    {
        protected EOEditingContext _createEditingContext(EOObjectStore parent)
        {
            return new ReadOnlyEditingContext(parent == null
                ? EOEditingContext.defaultParentObjectStore()
                : parent);
        }
    }


    // ----------------------------------------------------------
    public static Factory factory() {
        if (factory == null) {
            factory = new ReadOnlyFactory();
        }
        return factory;
    }


    //~ Static/instance variables .............................................

    private boolean modificationWasAttempted = false;
    private boolean loggingSuppressed = false;
    private boolean suppressesLogAfterFirstAttempt = false;

    private static Factory factory;
    static final Logger log = Logger.getLogger(ReadOnlyEditingContext.class);
}
