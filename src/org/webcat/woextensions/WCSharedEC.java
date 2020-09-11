/*==========================================================================*\
 |  Copyright (C) 2011-2018 Virginia Tech
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestamp.IntRef;

// -------------------------------------------------------------------------
/**
 *  This is a specialized shared editing context that enforces read-only
 *  access.
 *
 *  @author  Stephen Edwards
 */
public class WCSharedEC
    extends EOSharedEditingContext
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     */
    public WCSharedEC()
    {
        this(defaultParentObjectStore());
    }


    // ----------------------------------------------------------
    /**
     * Creates a new object.
     * @param os the parent object store
     */
    public WCSharedEC(EOObjectStore os)
    {
        super(os);
        setDelegate(WCEC.factory().defaultEditingContextDelegate());
//        counter.allocate(this);
        if (log.isDebugEnabled())
        {
            log.debug("creating " + getClass().getSimpleName()
                + " with parent object store " + os
                + "; " + this
                + "; by " + Thread.currentThread()
                + "; at " + System.currentTimeMillis());
        }
    }


    // ----------------------------------------------------------
    /**
     * Installs an instance of this class as the default shared
     * editing context for the application.
     */
    public static void install()
    {
        EOSharedEditingContext.setDefaultSharedEditingContext(
            new WCSharedEC());
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void lock()
    {
        String message = null;
        NSTimestamp tryTime = new NSTimestamp();
        if (log.isDebugEnabled())
        {
            synchronized (this)
            {
                message = "shared EC; " + this
                    + "; by " + Thread.currentThread()
                    + "; at " + System.currentTimeMillis();
                log.debug("lock() attempt; "
                    + message
                    , new Exception("from here"));
                if (owners == null)
                {
                    initDebugFields();
                }
                else if (owners.size() > 0
                    && owners.get(owners.size() - 1)
                       != Thread.currentThread())
                {
                    log.warn("This shared editing context is already "
                        + "locked by another thread! Owners = " + owners);
                    String dump = "Existing locks from = "
                        + lockLocations.size();
                    NSTimestamp now = new NSTimestamp();
                    NSTimestamp.IntRef sec = new NSTimestamp.IntRef();
                    for (int i = 0; i < lockLocations.size(); i++)
                    {
                        NSTimestamp ack = acquireTimes.get(i);
                        now.gregorianUnitsSinceTimestamp(
                            null, null, null, null, null, sec, ack);
                        String msg =
                            tryTimes.get(i) + ": "
                            +" acquired " + ack + ", held for "
                            + sec.value + "s: "
                            + lockLocations.get(i);
                        dump += "\n" + msg;
                    }
                    log.warn(dump);
                }
            }
        }

        super.lock();

        if (log.isDebugEnabled())
        {
            synchronized (this)
            {
                if (writer == null)
                {
                    initDebugFields();
                }
                writer.getBuffer().setLength(0);
                NSTimestamp ackTime = new NSTimestamp();
                message = "lock() acquired at "
                    + ackTime + " (tried at " + tryTime
                    + "); " + message;
                new Exception(message).printStackTrace(out);
                lockLocations.add(writer.toString());
                owners.add(Thread.currentThread());
                acquireTimes.add(ackTime);
                tryTimes.add(tryTime);
                log.debug(message);
            }
        }
    }


    // ----------------------------------------------------------
    @Override
    public boolean tryLock()
    {
        String message = null;
        NSTimestamp tryTime = new NSTimestamp();
        if (log.isDebugEnabled())
        {
            synchronized (this)
            {
                message = "shared EC; " + this
                    + "; by " + Thread.currentThread()
                    + "; at " + System.currentTimeMillis();
                log.debug("tryLock() attempt; "
                    + message
                    , new Exception("from here"));
            }
        }

        boolean result = super.tryLock();

        if (log.isDebugEnabled())
        {
            synchronized (this)
            {
                if (result)
                {
                    if (writer == null)
                    {
                        initDebugFields();
                    }
                    writer.getBuffer().setLength(0);
                    NSTimestamp ackTime = new NSTimestamp();
                    message = "tryLock() acquired at "
                        + ackTime + " (tried at " + tryTime
                        + "); " + message;
                    new Exception(message).printStackTrace(out);
                    lockLocations.add(writer.toString());
                    owners.add(Thread.currentThread());
                    acquireTimes.add(ackTime);
                    tryTimes.add(tryTime);
                    log.debug(message);
                }
                else
                {
                    log.debug("tryLock() failed; " + message);
                }
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    @Override
    public void insertObject(EOEnterpriseObject object)
    {
        throw new UnsupportedOperationException(
            getClass().getSimpleName() + " is READ ONLY!",
            new Exception("called from here"));
    }


    // ----------------------------------------------------------
    @Override
    public void deleteObject(EOEnterpriseObject object)
    {
        throw new UnsupportedOperationException(
            getClass().getSimpleName() + " is READ ONLY!",
            new Exception("called from here"));
    }


    // ----------------------------------------------------------
    @Override
    public void saveChanges()
    {
        throw new UnsupportedOperationException(
            getClass().getSimpleName() + " is READ ONLY!",
            new Exception("called from here"));
    }


    // ----------------------------------------------------------
    @Override
    public void unlock()
    {
        super.unlock();

        if (log.isDebugEnabled())
        {
            synchronized (this)
            {
                if (lockLocations == null)
                {
                    initDebugFields();
                }
                String extra = "";
                if (acquireTimes.size() > 0)
                {
                    NSTimestamp now = new NSTimestamp();
                    NSTimestamp ack =
                        acquireTimes.get(acquireTimes.size() - 1);
                    NSTimestamp.IntRef sec = new NSTimestamp.IntRef();
                    now.gregorianUnitsSinceTimestamp(
                        null, null, null, null, null, sec, ack);
                    extra = " acquired " + ack + ", held for "
                        + sec.value + "s; ";
                }
                log.debug("unlock();" + extra + " shared EC; " + this
                    + "; by " + Thread.currentThread()
                    + "; at " + System.currentTimeMillis()
                    // , new Exception("from here")
                );
                if (lockLocations.size() > 0)
                {
                    lockLocations.remove(lockLocations.size() - 1);
                    owners.remove(owners.size() - 1);
                    acquireTimes.remove(acquireTimes.size() - 1);
                    tryTimes.remove(tryTimes.size() - 1);
                }
                else
                {
                    log.error("no lock location to pop!");
                }
            }
        }
    }


    // ----------------------------------------------------------
    @Override
    public void dispose()
    {
        if (log.isDebugEnabled())
        {
            log.debug("dispose(): " + this);
        }
//        counter.deallocate(this);
        super.dispose();
    }


    // ----------------------------------------------------------
    private void initDebugFields()
    {
        lockLocations = new ArrayList<String>(16);
        owners = new ArrayList<Thread>(16);
        tryTimes = new ArrayList<NSTimestamp>(16);
        acquireTimes = new ArrayList<NSTimestamp>(16);
        writer = new StringWriter();
        out = new PrintWriter(writer);
    }


    // ----------------------------------------------------------
//    public static void dumpLeaks()
//    {
//        counter.dumpLeaks();
//    }


    //~ Instance/static variables .............................................

    private List<String> lockLocations = null;
    private List<Thread> owners = null;
    private List<NSTimestamp> tryTimes = null;
    private List<NSTimestamp> acquireTimes = null;
    private StringWriter writer = null;
    private PrintWriter out = null;

//    private static final ResourceCounter counter =
//        new ResourceCounter(WCSharedEC.class.getSimpleName());

    static Logger log = Logger.getLogger(WCSharedEC.class);
}
