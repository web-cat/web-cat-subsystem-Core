/*==========================================================================*\
 |  $Id: LockErrorScreamerEditingContext.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
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

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.io.StringWriter;
import java.io.PrintWriter;
import org.webcat.core.LockErrorScreamerEditingContext;
import org.apache.log4j.Logger;


// -------------------------------------------------------------------------
/**
 *  EOEditingContext subclass that tracks locking activity and
 *  complains about mismatching locking calls.  Based on the original
 *  class of the same name by Jonathan 'Wolf' Rentzsch (jon at redshed dot net)
 *  enhanced by Anthony Ingraldi (a.m.ingraldi at larc.nasa.gov).
 *
 * @author  Jonathan 'Wolf' Rentzsch
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class LockErrorScreamerEditingContext
    extends er.extensions.eof.ERXEC
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new LockErrorScreamerEditingContext object.
     */
    public LockErrorScreamerEditingContext()
    {
        super();
    }


    // ----------------------------------------------------------
    /**
     * Constructor for nested editing contexts.
     * @param parent EOEditingContext that this editing context is a child of
     */
    public LockErrorScreamerEditingContext( EOObjectStore parent )
    {
        super( parent );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Overridden to capture trace of where lock was taken and to show error
     * and trace if the editing context is already lock by by a thread other
     * than the one asking for the lock.  For editing contexts associated
     * with sessions this usually results in deadlock for the session and
     * also for the application if requrests are not being dispatched
     * concurrently.
     */
    public void lock()
    {
        String nameOfCurrentThread = Thread.currentThread().getName();

        // If we have not recorded any traces this editing context is not
        // currently locked and this results in a the lock being taken by
        // a new thread.
        if ( stackTraces.count() == 0 )
        {
            stackTraces.addObject( trace() );
            nameOfLockingThread = nameOfCurrentThread;
            log.debug( "+++ Lock number (" +  stackTraces.count()
                       + ") in " + nameOfCurrentThread );
        }
        else
        {
            // This editing context has already been locked.
            // If the thread is the same then this is a secondary call that
            // results in an increased recursionCount() for the
            // NSRecursiveLock.
            if ( nameOfCurrentThread.equals( nameOfLockingThread ) )
            {
                stackTraces.addObject( trace() );
                log.debug( "+++ Lock number (" + stackTraces.count()
                           + ") in " + nameOfCurrentThread );
            }
            // If the thread is not the same it will block.  For editing
            // contexts in a session this results in deadlock so an error
            // message is output.  The error includes the trace of the most
            // recent lock taken which is probably the offending unreleased
            // lock.  It might not be if your lock and unlocks are not
            // cleanly nested.  In that case you might need to capture and
            // display traces of all the lock and unlock calls to find what
            // is not nested correctly.
            else
            {
                log.error( "!!! Attempting to lock editing context from "
                           + nameOfCurrentThread
                           + " that was previously locked in "
                           + nameOfLockingThread );
                log.error( "!!! Current stack trace:\n" + trace() );
                log.error( "!!! Stack trace for most recent lock:\n"
                           + stackTraces.lastObject() );
            }
        }
        super.lock();
    }


    // ----------------------------------------------------------
    /**
     * Overridden to capture trace of where lock was taken and to show error
     * and trace if the editing context is already lock by by a thread other
     * than the one asking for the lock.
     */
    public void unlock()
    {
        // This will throw an IllegalStateException if the editing context
        // is not locked, or if the unlocking thread is not the thread with
        // the lock.
        super.unlock();

        // This editing context is already locked, so remove the trace for the
        // latest lock(), assuming that it corresponds to this unlock().
        if ( stackTraces.count() > 0 )
        {
            stackTraces.removeLastObject();
        }
        if ( stackTraces.count() == 0 )
        {
            // No more traces means that we are no longer locked so
            // dis-associate ourselves with the thread that had us locked.
            nameOfLockingThread = null;
        }
        String nameOfCurrentThread = Thread.currentThread().getName();
        log.debug( "--- Unlocked in " +  nameOfCurrentThread + " ("
                   + stackTraces.count() + " remaining)" );
    }


    // ----------------------------------------------------------
    /**
     * Support method with check common to dispose() and finalize().
     * Allowing locked editing contexts to be garbage collected or
     * disposed is not a good practice.  It is probably OK for peer
     * editing contexts, but this is not a really good coding practice.
     * This method outputs a warning message if this happens.  If you want
     * to follow this bad practice, change the test below to:<br>
     * <code>( (_stackTraces.count() != 0)
     *         && (parent() instanceof EOEditingContext) )</code>
     */
    public void goodbye()
    {
        if ( stackTraces.count() != 0 )
        {
            log.error( "!!! editing context being disposed with "
                       + stackTraces.count() + " locks." );
            log.error( "!!! Most recently locked by:\n"
                       + stackTraces.lastObject() );
        }
    }


    // ----------------------------------------------------------
    /**
     * Allowing locked editing contexts to be disposed is not a good
     * practice.  This method calls goodbye() to output a warning message
     * if this happens.
     */
    public void dispose()
    {
        try
        {
            goodbye();
        }
        finally
        {
            super.dispose();
        }
    }


    // ----------------------------------------------------------
    /**
     * Allowing locked editing contexts to be garbage collected is not a good
     * practice.  This method calls goodbye() to outputs a warning message if
     * this happens.
     * @throws Throwable if the superclass implementation produces an error
     */
    public void finalize()
        throws Throwable
    {
        try
        {
            goodbye();
        }
        finally
        {
            super.finalize();
        }
    }


    // ----------------------------------------------------------
    /**
     * Utility method to return the stack trace from the current location as
     * a string.
     * @return the stack trace from the current location as a string
     */
    private String trace()
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter( stringWriter );
        ( new Throwable() ).printStackTrace( printWriter );
        return stringWriter.toString();
    }


    //~ Instance/static variables .............................................

    private String         nameOfLockingThread = null;
    private NSMutableArray<String> stackTraces = new NSMutableArray<String>();

    static Logger log =
        Logger.getLogger( LockErrorScreamerEditingContext.class );
}
