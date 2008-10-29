/*==========================================================================*\
 |  $Id$
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

package net.sf.webcat.core;

import org.apache.log4j.Logger;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import er.extensions.eof.ERXEC;

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
 * @author Tony Allevato
 * @version $Id$
 */
public class ReadOnlyEditingContext extends ERXEC
{
    //~ Methods ...............................................................

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

    
    //~ Static/instance variables .............................................
    
    private boolean modificationWasAttempted = false;
    private boolean loggingSuppressed = false;
    private boolean suppressesLogAfterFirstAttempt = false;

    static final Logger log = Logger.getLogger(ReadOnlyEditingContext.class);
}
