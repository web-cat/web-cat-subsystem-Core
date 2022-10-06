/*==========================================================================*\

 |  Copyright (C) 2006-2021 Virginia Tech
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

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import org.webcat.core.Application;
import org.webcat.core.Disposable;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.eof.ERXEC;

// -------------------------------------------------------------------------
/**
 *  This is a specialized editing context subclass with infrastructure
 *  support for peer manager pools.
 *
 *  @author  Stephen Edwards
 */
public class WCEC
    extends ERXEC
    implements Disposable
//    extends LockErrorScreamerEditingContext
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     */
    public WCEC()
    {
        this(defaultParentObjectStore());
    }


    // ----------------------------------------------------------
    /**
     * Creates a new object.
     * @param os the parent object store
     */
    public WCEC(EOObjectStore os)
    {
        super(os);
        counter.allocate(this);
        setUndoManager(null);
        // setSharedEditingContext(null);
        if (log.isDebugEnabled())
        {
            String message = "creating " + getClass().getSimpleName()
                + " with parent object store " + os
                + "; " + this
                + "; by " + Thread.currentThread()
                + "; at " + System.currentTimeMillis();
            log.debug(message, new Exception("from here"));
        }
        active.incrementAndGet();
    }


    // ----------------------------------------------------------
    /**
     * Creates a new peer editing context, typically used to make
     * changes outside of a session's editing context.
     * @return the new editing context
     */
    public static WCEC newEditingContext()
    {
        return factory().newEditingContext();
    }


    // ----------------------------------------------------------
    /**
     * Creates a new peer editing context that performs
     * auto-locking/auto-unlocking on each method call.
     * @return the new editing context
     */
    public static WCEC newAutoLockingEditingContext()
    {
        return factory().newAutoLockingEditingContext();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void _EOAssertSafeMultiThreadedAccess(String info)
    {
        // TODO Auto-generated method stub
        if (!_doAssertLock)
        {
            return;
        }
        if (!_doAssertLockInitialized)
        {
            _checkAssertLock();
            if (!_doAssertLock)
            {
                return;
            }
        }
        if (lockCount() == 0)
        {
            // shut off warnings past limit, so log doesn't overflow
            if (lockWarnings.incrementAndGet() <= MAX_LOCK_WARNINGS)
            {
                log.warn("*** WCEditingContext: access with no lock: "
                    + info + "!",
                    new Exception(
                    "WCEditingContext accessed without lock from here"));
            }
            return;
        }
        else if (_baseClassLockCount() == 0)
        {
            if (lockWarnings.incrementAndGet() <= MAX_LOCK_WARNINGS)
            {
                log.warn("*** WCEditingContext: access with inconsistent "
                    + "lock counts: lockCount() = " + lockCount()
                    + ", _lockCount = " + _baseClassLockCount(),
                    new Exception("WCEditingContext accessed without "
                        + "inconsistent lock counts from here"));
            }
            return;
        }
        // Let inherited method handle case where locked by other
        // thread
        super._EOAssertSafeMultiThreadedAccess(info);
    }



//    // ----------------------------------------------------------
//    @Override
//    public void lockObjectStore()
//    {
//        if (lockCount() == 0)
//        {
//            EOSharedEditingContext.defaultSharedEditingContext().lock();
//        }
//        super.lockObjectStore();
//    }
//
//
//    // ----------------------------------------------------------
//    @Override
//    public void unlockObjectStore()
//    {
//        boolean wasLocked = lockCount() > 0;
//        super.unlockObjectStore();
//        if (wasLocked
//            && lockCount() == 0)
//        {
//            EOSharedEditingContext.defaultSharedEditingContext().unlock();
//        }
//    }


    private static int[] WAITS = {1, 2500, 1000, 500, 100 };
    
    // ----------------------------------------------------------
    @Override
    public void initializeObject(
        EOEnterpriseObject eoenterpriseobject,
        EOGlobalID eoglobalid,
        EOEditingContext eoeditingcontext)
    {
        int tries = 5;
        while (tries-- > 0)
        {
            try
            {
                super.initializeObject(
                    eoenterpriseobject, eoglobalid, eoeditingcontext);
                return;
            }
            catch (NullPointerException ee)
            {
                if (tries > 0)
                {
//                    log.warn("initializeObject() failed with NPE on "
//                        + eoglobalid
//                        + ", will retry "
//                        + tries + " times", ee);
                    try
                    {
                        // Give other threads a chance to finish mods to
                        // database first
                        Thread.sleep(WAITS[tries]);
                    }
                    catch (InterruptedException e)
                    {
                        // ignore
                    }
                }
                else
                {
                    log.warn("initializeObject() failed with NPE on "
                        + eoglobalid
                        + " after max "
                        + "retries, giving up", ee);
                    NullPointerException eee = new NullPointerException(
                        "initializeObject() failed with NPE on "
                            + eoglobalid
                            + " after max "
                            + "retries, giving up");
                    eee.setStackTrace(ee.getStackTrace());
                    throw eee;
                }
            }
        }
        if (tries < 3)
        {
            log.warn("initializeObject() succeeded after " + (5 - tries)
                + " tries on " + eoglobalid);
        }
    }


    // ----------------------------------------------------------
    @Override
    public void saveChangesTolerantly()
    {
        boolean wasAutoLocked = autoLock("saveChangesTolerantly");
        try {
            super.saveChangesTolerantly();
        }
        finally {
            autoUnlock(wasAutoLocked);
        }
    }


    // ----------------------------------------------------------
    @Override
    protected void _checkOpenLockTraces()
    {
        synchronized (this)
        {
            int lockCount = lockCount();
            if (lockCount > 0)
            {
                String msg = "Open lock count = " + lockCount;
                log.error(msg, new RuntimeException(msg + " here"));
            }
            super._checkOpenLockTraces();
        }
    }


    // ----------------------------------------------------------
    @Override
    public void dispose()
    {
        if (log.isDebugEnabled())
        {
            log.debug("dispose(): " + this);
            if (lockCount() > 0)
            {
                log.error("EC disposed while locked (" + lockCount() + ")",
                    new Exception("EC dispose() with locks called from here"));
            }
        }
        counter.deallocate(this);
        active.decrementAndGet();
        super.dispose();
    }


    // ----------------------------------------------------------
//    public boolean unlockIfNecessary()
//    {
//        int count = lockCount();
//        if (count > 0)
//        {
//            unlock();
//        }
//        return count > 0;
//    }


    // ----------------------------------------------------------
    @Override
    public void unlock()
    {
        // This will throw an IllegalStateException if the editing context
        // is not locked, or if the unlocking thread is not the thread with
        // the lock.
        try
        {
            super.unlock();
        }
        catch (IllegalMonitorStateException e)
        {
            log.error(e);
            _checkOpenLockTraces();
        }
    }


    // ----------------------------------------------------------
    public static int createdCount()
    {
        return created.get();
    }


    // ----------------------------------------------------------
    public static int activeCount()
    {
        return active.get();
    }


    // ----------------------------------------------------------
    public static void dumpLeaks()
    {
        counter.dumpLeaks();
    }


    // ----------------------------------------------------------
    /**
     * An internal factory method used by the different factory classes
     * to create WCEC's. It ensures the created WCEC uses tolerant
     * saving by default, to help with auto-recovery from optimistic
     * locking failures.
     * @return The newly created WCEC.
     */
    private static WCEC createWCEC(EOObjectStore parent)
    {
        WCEC ec = new WCEC(parent == null
            ? EOEditingContext.defaultParentObjectStore()
            : parent);
        ec.lock();
        try {
            ec.setOptions(true, true, true);
        }
        finally {
            ec.unlock();
        }
        return ec;
    }


    // ----------------------------------------------------------
    /**
     * The default factory class, using the WC OSC pool for multi-threading
     * support. This is the kind of factory that is installed for
     * EOEditingContext creation, and that is accessible from the
     * factory() method.
     */
    public static class WCECFactory
        extends WCObjectStoreCoordinatorPool.MultiOSCFactory
    {
        // ----------------------------------------------------------
        public WCECFactory(Factory backingFactory)
        {
            super(WCObjectStoreCoordinatorPool._pool(), backingFactory);
        }


        // ----------------------------------------------------------
        public WCECFactory()
        {
            this(new ERXEC.DefaultFactory() {
                // ------------------------------------------------------
                @Override
                protected WCEC _createEditingContext(EOObjectStore parent)
                {
                    return WCEC.createWCEC(parent);
                }
            });
        }


        // ----------------------------------------------------------
        protected WCEC _createEditingContext(EOObjectStore parent)
        {
            return WCEC.createWCEC(parent);
        }


        // ----------------------------------------------------------
        /**
         * Creates a new peer editing context, typically used to make
         * changes outside of a session's editing context.
         * @return the new editing context
         */
        public WCEC newEditingContext()
        {
            return (WCEC)_newEditingContext();
        }


        // ----------------------------------------------------------
        /**
         * Creates a new peer editing context that performs
         * auto-locking/auto-unlocking on each method call.
         * @return the new editing context
         */
        public WCEC newAutoLockingEditingContext()
        {
            WCEC result = newEditingContext();
            result.setCoalesceAutoLocks(false);
            result.setUseAutoLock(true);
            return result;
        }
    }


    // ----------------------------------------------------------
    /**
     * A custom factory class that includes its own independent (non-pooled)
     * OSC, separate from the pool managed by the pool coordinator.
     */
    public static class WCECFactoryWithOSC
        extends er.extensions.eof.ERXEC.DefaultFactory
    {
        private EOObjectStore rootStore;
        private EOSharedEditingContext sec = null;


        // ----------------------------------------------------------
        public WCECFactoryWithOSC(EOObjectStore rootStore)
        {
            super();
            this.rootStore = rootStore;
            if (useSharedEditingContext())
            {
                sec = new WCSharedEC(rootStore);
            }
        }


        // ----------------------------------------------------------
        public WCECFactoryWithOSC(WCECFactoryWithOSC parent)
        {
            this(parent.factoryRootObjectStore());
        }


        // ----------------------------------------------------------
        public WCECFactoryWithOSC()
        {
            this(new WCObjectStoreCoordinator());
        }


        // ----------------------------------------------------------
        public EOObjectStore factoryRootObjectStore()
        {
            return rootStore;
        }


        // ----------------------------------------------------------
        public WCEC _newEditingContext()
        {
            return _newEditingContext(true);
        }


        // ----------------------------------------------------------
        public WCEC _newEditingContext(boolean validationEnabled)
        {
            WCEC ec = (WCEC)_newEditingContext(rootStore, validationEnabled);
            ec.lock();
            try
            {
                ec.setSharedEditingContext(sec);
            }
            finally
            {
                ec.unlock();
            }
            return ec;
        }


        // ----------------------------------------------------------
        @Override
        protected WCEC _createEditingContext(EOObjectStore parent)
        {
            return WCEC.createWCEC(parent);
        }


        // ----------------------------------------------------------
        /**
         * Creates a new peer editing context, typically used to make
         * changes outside of a session's editing context.
         * @return the new editing context
         */
        public WCEC newEditingContext()
        {
            return _newEditingContext();
        }


        // ----------------------------------------------------------
        /**
         * Creates a new peer editing context that performs
         * auto-locking/auto-unlocking on each method call.
         * @return the new editing context
         */
        public WCEC newAutoLockingEditingContext()
        {
            WCEC result = newEditingContext();
            result.setCoalesceAutoLocks(false);
            result.setUseAutoLock(true);
            return result;
        }
    }


    // ----------------------------------------------------------
    public static WCECFactory factory()
    {
        synchronized (WCEC.class)
        {
            if (factory == null)
            {
                factory = new WCECFactory();
            }
            return factory;
        }
    }


    // ----------------------------------------------------------
    public static WCECFactoryWithOSC factoryWithToolOSC()
    {
        synchronized (WCEC.class)
        {
            if (factoryWithOSC == null)
            {
                factoryWithOSC = new WCECFactoryWithOSC();
            }
            return factoryWithOSC;
        }
    }


    // ----------------------------------------------------------
    public static void installWOECFactory()
    {
        EOEditingContext.setDefaultFetchTimestampLag(0);
//        er.extensions.eof.ERXObjectStoreCoordinatorSynchronizer.synchronizer()
//            .setDefaultSettings(new er.extensions.eof
//            .ERXObjectStoreCoordinatorSynchronizer.SynchronizerSettings(
//            false, true, true, true));
        WCObjectStoreCoordinatorPool.initialize();
        er.extensions.eof.ERXEC.setFactory(factory());
    }


    // ----------------------------------------------------------
    public static class PeerManager
        implements Disposable
    {
        // ----------------------------------------------------------
        public PeerManager(PeerManagerPool pool)
        {
            this(pool, false);
        }


        // ----------------------------------------------------------
        public PeerManager(PeerManagerPool pool, boolean cachePermanently)
        {
            owner = pool;
            this.cachePermanently = cachePermanently;
            if (log.isDebugEnabled())
            {
                log.debug("creating manager: " + this);
            }
        }


        // ----------------------------------------------------------
        public WCEC editingContext()
        {
            if (ec == null)
            {
                ec = newEditingContext();
                if (log.isDebugEnabled())
                {
                    log.debug("creating ec: " + ec.hashCode()
                        + " for manager: " + this);
                }
                if (cachePermanently)
                {
                    owner.cachePermanently(this);
                }
                else
                {
                    owner.cache(this);
                }
            }
            return ec;
        }


        // ----------------------------------------------------------
        public void dispose()
        {
            if (transientState != null)
            {
                for (Object value : transientState.allValues())
                {
                    if (value instanceof Disposable)
                    {
                        ((Disposable)value).dispose();
                    }
                    else if (value instanceof EOEditingContext)
                    {
                        ((EOEditingContext)value).dispose();
                    }
                    else
                    {
                        log.error("unable to dispose() of "
                            + value.getClass() + ": " + value);
                    }
                }
                transientState = null;
            }
            if (ec != null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("disposing ec: " + ec
                        + " for manager: " + this);
                }
                ec.dispose();
                ec = null;
            }
            else
            {
                log.debug("dispose() called with null ec for manager: " + this);
            }
        }


        // ----------------------------------------------------------
        public void sleep()
        {
            if (log.isDebugEnabled())
            {
                log.debug("sleep(): " + this);
            }
        }


        // ----------------------------------------------------------
        public boolean cachePermanently()
        {
            return cachePermanently;
        }


        // ----------------------------------------------------------
        public void setCachePermanently(boolean value)
        {
            if (log.isDebugEnabled())
            {
                log.debug("setCachePermanently(" + value
                    + ") for manager: " + this);
            }
            cachePermanently = value;
        }


        // ----------------------------------------------------------
        /**
         * Retrieve an NSMutableDictionary used to hold transient settings for
         * this editing context (data that is not database-backed).
         * @return A map of transient settings
         */
        public NSMutableDictionary<String, Object> transientState()
        {
            if (transientState == null)
            {
                transientState = new NSMutableDictionary<String, Object>();
            }
            return transientState;
        }


        //~ Instance/static variables .........................................
        private WCEC                                ec;
        private PeerManagerPool                     owner;
        private boolean                             cachePermanently;
        private NSMutableDictionary<String, Object> transientState;
        static Logger log = Logger.getLogger(
            PeerManager.class.getName().replace('$', '.'));
    }


    // ----------------------------------------------------------
    public static class PeerManagerPool
        implements Disposable
    {
        // ----------------------------------------------------------
        public PeerManagerPool()
        {

            managerCache = new NSMutableArray<PeerManager>();
            permanentManagerCache = new NSMutableArray<PeerManager>();
            if (log.isDebugEnabled())
            {
                log.debug("creating manager pool: " + this);
            }
        }


        // ----------------------------------------------------------
        public void cache(PeerManager manager)
        {
            if (log.isDebugEnabled())
            {
                log.debug("cache(" + manager + ")");
            }
            cache(manager, managerCache);
        }


        // ----------------------------------------------------------
        public void cachePermanently(PeerManager manager)
        {
            if (log.isDebugEnabled())
            {
                log.debug("cachePermanently(" + manager + ")");
            }
            managerCache.removeObject(manager);
            cache(manager, permanentManagerCache);
        }


        // ----------------------------------------------------------
        public void dispose()
        {
            log.debug("dispose()");
            dispose(managerCache);
            dispose(permanentManagerCache);
        }


        // ----------------------------------------------------------
        private void cache(
            PeerManager manager, NSMutableArray<PeerManager> cache)
        {
            int pos = cache.indexOfObject(manager);
            if (pos == NSArray.NotFound)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("caching: manager " + manager + " not in cache");
                }
                if (cache.count()
                    > Application.application().pageCacheSize())
                {
                    log.debug("caching: pool full, flushing oldest");
                    cache.objectAtIndex(0).dispose();
                    cache.removeObjectAtIndex(0);
                }
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("caching: manager " + manager
                        + " found at pos " + pos);
                }
                cache.remove(pos);
            }
            if (log.isDebugEnabled())
            {
                log.debug("caching: manager " + manager
                    + " placed at pos " + cache.count());
            }
            cache.add(manager);
        }


        // ----------------------------------------------------------
        private void dispose(NSMutableArray<PeerManager> cache)
        {
            for (PeerManager manager : cache)
            {
                manager.dispose();
            }
            cache.clear();
        }


        //~ Instance/static variables .........................................
        private NSMutableArray<PeerManager> managerCache;
        private NSMutableArray<PeerManager> permanentManagerCache;
        static Logger log = Logger.getLogger(
            PeerManagerPool.class.getName().replace('$', '.'));
    }


    //~ Instance/static variables .............................................

    private static final ResourceCounter counter =
        new ResourceCounter(WCEC.class.getSimpleName());

    private static final AtomicInteger created = new AtomicInteger();
    private static final AtomicInteger active = new AtomicInteger();
    private static final AtomicInteger lockWarnings = new AtomicInteger();
    private static final int MAX_LOCK_WARNINGS = 1000;

    private static WCECFactory factory;
    private static WCECFactoryWithOSC factoryWithOSC;
    static Logger log = Logger.getLogger(WCEC.class);
}
