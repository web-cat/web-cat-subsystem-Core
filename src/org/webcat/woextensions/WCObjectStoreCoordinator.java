/*==========================================================================*\
 |  Copyright (C) 2011-2021 Virginia Tech
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import com.webobjects.eocontrol.EOCooperatingObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.appserver.ERXSession;
import er.extensions.eof.ERXObjectStoreCoordinator;

// -------------------------------------------------------------------------
/**
 *  This is a specialized subclass with extra debugging support.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.1 $, $Date: 2011/12/25 02:24:54 $
 */
public class WCObjectStoreCoordinator
    extends ERXObjectStoreCoordinator
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     */
    public WCObjectStoreCoordinator()
    {
        super();
//        counter.allocate(this);
        if (log.isDebugEnabled())
        {
            log.debug("creating " + this); // , new Exception("from here"));
        }
    }


    // ----------------------------------------------------------
    /**
     * Installs an instance of this class as the default object
     * store coordinator for the application.
     */
    public static void install()
    {
        EOObjectStoreCoordinator.setDefaultCoordinator(
            new WCObjectStoreCoordinator());
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void addCooperatingObjectStore(EOCooperatingObjectStore objectStore)
    {
        int oldCount = sourceCount.get();
        try
        {
            super.addCooperatingObjectStore(objectStore);
        }
        finally
        {
            sourceCount.set(cooperatingObjectStores().size());
            if (oldCount != sourceCount.get())
            {
                myRepresentation = null;
                log.debug("addCooperatingObjectStore("
                    + objectStore + ") total => "
                    +  sourceCount.get());
            }
        }
    }


    // ----------------------------------------------------------
    @Override
    public void removeCooperatingObjectStore(
        EOCooperatingObjectStore objectStore)
    {
        int oldCount = sourceCount.get();
        try
        {
            super.removeCooperatingObjectStore(objectStore);
        }
        finally
        {
            sourceCount.set(cooperatingObjectStores().size());
            if (oldCount != sourceCount.get())
            {
                myRepresentation = null;
                log.debug("removeCooperatingObjectStore("
                    + objectStore + ") total => "
                    +  sourceCount.get());
            }
        }
    }


    // ----------------------------------------------------------
    @Override
    public void lock()
    {
//            EOSharedEditingContext sharedEC =
//                EOSharedEditingContext.defaultSharedEditingContext();
//            if (sharedEC != null)
//            {
//                sharedEC.lock();
//            }
        LockRecord record = new LockRecord(this.toString());
//        log.fatal("lock(): thread[" + Thread.currentThread().getName()
//            + "] on " + this + " called here",
//            new Exception("lock(): thread[" + Thread.currentThread().getName()
//            + "] on " + this + " called here"));
        synchronized (locks)
        {
            activeLogging =
                record.preCheckout("lock", locks, queue, activeLogging);
        }
        {
            ThreadLockedObjectStore alreadyHolding = lockedStore.get();
            if (alreadyHolding != null && alreadyHolding.os != this)
            {
                log.fatal("!!!!! lock(): attempting to lock " + this
                    + " while already holding lock on " + alreadyHolding.os
                    + "!!!!!");
            }
        }
//        String message = null;
//        if (log.isDebugEnabled())
//        {
//            synchronized (this)
//            {
//                message = "object store; " + this
//                    + "; by " + Thread.currentThread()
//                    + "; at " + System.currentTimeMillis();
//                log.debug("lock() attempt; "
//                    + message,
//                    new Exception("from here"));
//                if (owners == null)
//                {
//                    initDebugFields();
//                }
//                else if (owners.size() > 0
//                    && owners.get(owners.size() - 1) != Thread.currentThread())
//                {
//                    log.warn("This object store is already locked "
//                        + "by another thread! Owners = " + owners);
//                    String dump = "Existing locks from = "
//                        + lockLocations.size();
//                    for (String msg : lockLocations)
//                    {
//                        dump += "\n" + msg;
//                    }
//                    log.warn(dump);
//                }
//            }
//        }
        super.lock();
        lockedStore.set(new ThreadLockedObjectStore(this, lockedStore.get()));
        synchronized (locks)
        {
            activeLogging =
                record.acquired("lock", locks, queue, activeLogging);
        }
//        if (log.isDebugEnabled())
//        {
//            synchronized (this)
//            {
//                if (writer == null)
//                {
//                    initDebugFields();
//                }
//                writer.getBuffer().setLength(0);
//                message = "lock() acquired; " + message;
//                new Exception(message).printStackTrace(out);
//                lockLocations.add(writer.toString());
//                owners.add(Thread.currentThread());
//                log.debug(message);
//            }
//        }
    }

    // ----------------------------------------------------------
    @Override
    public void unlock()
    {
        LockRecord record = null;
//        log.fatal("unlock(): thread[" + Thread.currentThread().getName()
//            + "] on " + this + " called here",
//            new Exception("lock(): thread[" + Thread.currentThread().getName()
//            + "] on " + this + " called here"));
        synchronized (locks)
        {
            record = findLock(locks);
            if (record == null)
            {
                log.error("unlock(): thread ["
                    + Thread.currentThread().getName() + "] attempting to "
                    + " unlock " + this + " but is not in lock list!",
                    new Exception("unlock() called from here"));
            }
        }
        {
            ThreadLockedObjectStore alreadyHolding = lockedStore.get();
            if (alreadyHolding == null)
            {
                log.fatal("!!!!! unlock(): attempting to unlock " + this
                    + " while not holding lock"
                    + "!!!!!");
            }
            else if (alreadyHolding.os != this)
            {
                log.fatal("!!!!! unlock(): attempting to unlock " + this
                    + " while holding lock on " + alreadyHolding.os
                    + "!!!!!");
            }
            else
            {
                // Must be alreadyHolding == this, so clear it
                lockedStore.set(alreadyHolding.parent);
            }
        }
        super.unlock();
        if (record != null)
        {
            synchronized (locks)
            {
                int held = difference(record.acquireTime, new NSTimestamp());
                int wait = difference(record.tryTime, record.acquireTime);
                if (!locks.remove(record))
                {
                    log.error("unlock(): called for " + record.tag
                        + ", but failed to find record in lock list!");
                }
                else if (activeLogging)
                {
                    log.info("unlock(): called for " + record.tag);
                }
                if (activeLogging)
                {
                    log.info("unlock(): released after " + held +"s, now "
                        + record.queue("locks", locks)
                        + ", " + record.queue("queue", queue));
                    dumpLocks("lock", locks);
                    dumpLocks("queue", queue);
                    if (wait < 2 && held < 2)
                    {
                        activeLogging = false;
                        log.info("unlock(): " + this
                            + " turning off lock logging after latest "
                            + "wait = " + wait + ", held = " + held);
                    }
                }
                if (queue.size() == 0)
                {
                    activeLogging = false;
                }
            }
        }
//        if (log.isDebugEnabled())
//        {
//            synchronized (this)
//            {
//                log.debug("unlock(); object store; " + this
//                    + "; by " + Thread.currentThread()
//                    + "; at " + System.currentTimeMillis()
//                    // , new Exception("from here")
//                );
//                if (lockLocations == null)
//                {
//                    initDebugFields();
//                }
//                if (lockLocations.size() > 0)
//                {
//                    lockLocations.remove(lockLocations.size() - 1);
//                    owners.remove(owners.size() - 1);
//                }
//                else
//                {
//                    log.error("no lock location to pop!");
//                }
//            }
//        }
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
    public int sourceCount()
    {
        return sourceCount.get();
    }


    // ----------------------------------------------------------
    public static ThreadLockedObjectStore threadHasLockedStore()
    {
        return lockedStore.get();
    }


    // ----------------------------------------------------------
    public String toString()
    {
        if (myRepresentation == null)
        {
            myRepresentation = "ObjectStore-" + storeId
                + "(" + sourceCount.get() + ")";
        }
        return myRepresentation;
    }


    // ----------------------------------------------------------
//    private void initDebugFields()
//    {
//        lockLocations = new ArrayList<String>(16);
//        owners = new ArrayList<Thread>(16);
//        writer = new StringWriter();
//        out = new PrintWriter(writer);
//    }


    // ----------------------------------------------------------
    public static class ThreadLockedObjectStore
    {
        public final WCObjectStoreCoordinator os;
        public final Exception location;
        public final int count;
        public final ThreadLockedObjectStore parent;
        public ThreadLockedObjectStore(WCObjectStoreCoordinator os,
            ThreadLockedObjectStore parent)
        {
            this.os = os;
            this.parent = parent;
            this.count = parent == null ? 1 : parent.count + 1;
            ERXSession s = ERXSession.session();
            location = new Exception("thread ["
                + Thread.currentThread().getName()
                + "]"
                + (s == null ? "" : "[" + s.sessionID() + "]")
                + " locked " + os + " here");
        }
    }


    // ----------------------------------------------------------
    public static class LockRecord
    {
        public String id;
        public String sessionId;
        public String owner;
        public NSTimestamp tryTime;
        public NSTimestamp acquireTime;
        public Exception location;
        public String tag;
        public String ownerLong;
        public LockRecord(String store)
        {
            this.id = store;
            owner = Thread.currentThread().getName();
            ownerLong = "thread [" + owner + "]";
            {
                ERXSession s = ERXSession.session();
                if (s != null)
                {
                    sessionId = s.sessionID();
                    ownerLong += "[" + sessionId + "]";
                }
            }
            tryTime = new NSTimestamp();
            tag = ownerLong + " attempting to lock "
            + id + " at " + this.tryTime;
            location = new Exception(tag);
        }
        private String queue(String type, List<LockRecord> locks)
        {
            if (locks.size() > 0)
            {
                return type + " = " + locks.size();
            }
            else
            {
                return "no " + type;
            }
        }
        public boolean acquired(
            String method, List<LockRecord> locks, List<LockRecord> queue,
            boolean activeLogging)
        {
            acquireTime = new NSTimestamp();
            queue.remove(this);
            tag = ownerLong + " acquired lock on " + id
                + " at " + acquireTime;
            int delay = difference(acquireTime, tryTime);
            tag += " (waited " + delay + "s)";
            location = new Exception(tag);
            String type = "";
            if (locks.size() > 0 && this.owner.equals(locks.get(0).owner))
            {
                type = "relocked: ";
            }
            if (activeLogging)
            {
            log.info(method + "(): " + type + tag
                + ", " + queue("locks", locks)
                + ", " + queue("queue", queue));
            }
            locks.add(this);
            if (locks.size() > 1 && activeLogging)
            {
                dumpLocks("lock", locks);
            }
            if (activeLogging)
            {
                dumpLocks("queue", queue);
            }
            if (activeLogging && queue.size() == 0)
            {
                activeLogging = false;
                log.info(method + "(): " + id + " turning off lock "
                    + "logging due to queue = 0");
            }
            return activeLogging;
        }
        public boolean preCheckout(
            String method, List<LockRecord> locks, List<LockRecord> queue,
            boolean activeLogging)
        {
            if (locks.size() > 0)
            {
//                if (locks.size() > 10)
//                {
//                    activeLogging = true;
//                }
                LockRecord earliest = locks.get(0);
                if (!owner.equals(earliest.owner))
                {
                    int delay = difference(earliest.acquireTime, tryTime);
                    if (delay > 10)
                    {
                        activeLogging = true;
                        log.info(method + "(): " + id + " turning on lock "
                            + "logging due to wait = " + delay);
                    }
                    if (activeLogging)
                    {
                    if (delay > 15)
                    {
                        log.error(method + "(): "
                            + tag + ", but current lock held for " + delay
                            + "s so far"
                            + ", " + queue("locks", locks)
                            + ", " + queue("queue", queue), earliest.location);
                    }
                    else
                    {
                        log.error(method + "(): " + tag
                            + ", but current lock held for " + delay
                            + "s so far"
                            + ", " + queue("locks", locks)
                            + ", " + queue("queue", queue));
                    }
                    }
                }
                else if (activeLogging)
                {
                    log.info(method + "(): attempting to relock: " + tag + ", "
                        + queue("locks", locks) + ", " + queue("queue", queue));
                }
                if (activeLogging)
                {
                    dumpLocks("lock", locks);
                }
            }
            else if (activeLogging)
            {
                log.info(method + "(): " + tag
                    + ", " + queue("locks", locks)
                    + ", " + queue("queue", queue));
            }
            queue.add(this);
            if (activeLogging)
            {
                dumpLocks("queue", queue);
            }
            return activeLogging;
        }
    }

    private List<LockRecord> queue = new ArrayList<LockRecord>();
    private List<LockRecord> locks = new ArrayList<LockRecord>();
    @SuppressWarnings("deprecation")
    private static int difference(NSTimestamp earlier, NSTimestamp later)
    {
        NSTimestamp.IntRef sec = new NSTimestamp.IntRef();
        later.gregorianUnitsSinceTimestamp(
            null, null, null, null, null, sec, earlier);
        return sec.value;
    }
    private static LockRecord findLock(List<LockRecord> locks)
    {
        String owner = Thread.currentThread().getName();
        for (int i = locks.size() - 1; i  >= 0; i--)
        {
            LockRecord lr = locks.get(i);
            if (owner.equals(lr.owner))
            {
                return lr;
            }
        }
        return null;
    }
    private static void dumpLocks(String type, List<LockRecord> locks)
    {
        boolean deep = locks.size() > 5;
        NSTimestamp now = new NSTimestamp();
        int i = 0;
        for (LockRecord lr : locks)
        {
            i++;
            boolean acquired = (lr.acquireTime != null);
            int delay =
                difference((acquired ? lr.acquireTime : lr.tryTime), now);
            if (acquired && delay > 5)
            {
                deep = true;
            }
            String msg = type + " record " + i + ", "
                + (acquired ? "locked" : "waiting")
                + " for " + delay + "s: " + lr.tag;
            if (deep)
            {
                log.warn(msg, lr.location);
            }
            else
            {
                log.info(msg);
            }
        }
    }


    //~ Instance/static variables .............................................

    private String myRepresentation = null;
    private boolean activeLogging = false;
    private static final java.util.concurrent.atomic.AtomicInteger maxId =
        new java.util.concurrent.atomic.AtomicInteger();
    private final int storeId = maxId.incrementAndGet();
    private final AtomicInteger sourceCount = new AtomicInteger();
    private static ThreadLocal<ThreadLockedObjectStore> lockedStore =
        new ThreadLocal<ThreadLockedObjectStore>();

    //    private StringWriter writer = null;
//    private PrintWriter out = null;

//    private static final ResourceCounter counter =
//        new ResourceCounter(WCObjectStoreCoordinator.class.getSimpleName());

    static Logger log = Logger.getLogger(WCObjectStoreCoordinator.class);
}
