/*==========================================================================*\
 |  Copyright (C) 2006-2018 Virginia Tech
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

import org.apache.log4j.Logger;
import org.webcat.core.Application;
import org.webcat.core.EOManager;
import org.webcat.core.LockErrorScreamerEditingContext;
import com.webobjects.eocontrol.EOEditingContext;
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
//    extends ERXEC
    extends LockErrorScreamerEditingContext
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     */
    public WCEC()
    {
        this(defaultParentObjectStore());
        setUndoManager(null);
        // setSharedEditingContext(null);
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
    }


    // ----------------------------------------------------------
    /**
     * Creates a new peer editing context, typically used to make
     * changes outside of a session's editing context.
     * @return the new editing context
     */
    public static WCEC newEditingContext()
    {
        return (WCEC)factory()._newEditingContext();
    }


    // ----------------------------------------------------------
    /**
     * Creates a new peer editing context that performs
     * auto-locking/auto-unlocking on each method call.
     * @return the new editing context
     */
    public static WCEC newAutoLockingEditingContext()
    {
        WCEC result = newEditingContext();
        result.setCoalesceAutoLocks(false);
        result.setUseAutoLock(true);
        return result;
    }


    //~ Methods ...............................................................

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


//    // ----------------------------------------------------------
//    @Override
//    public void saveChanges()
//    {
//        EOSharedEditingContext sharedEC = null;
//        if (sharedEditingContext() == null)
//        {
//            sharedEC = EOSharedEditingContext.defaultSharedEditingContext();
//            if (sharedEC != null)
//            {
//                sharedEC.lock();
//            }
//        }
//        try
//        {
//            super.saveChanges();
//        }
//        finally
//        {
//            if (sharedEC != null)
//            {
//                sharedEC.unlock();
//            }
//        }
//    }


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
        }
        counter.deallocate(this);
        super.dispose();
    }


    // ----------------------------------------------------------
    public static void dumpLeaks()
    {
        counter.dumpLeaks();
    }


    // ----------------------------------------------------------
    public static class WCECFactory
        extends er.extensions.eof.ERXEC.DefaultFactory
    {
        protected EOEditingContext _createEditingContext(EOObjectStore parent)
        {
            return new WCEC(parent == null
                ? EOEditingContext.defaultParentObjectStore()
                : parent);
        }
    }


    // ----------------------------------------------------------
    public static Factory factory() {
        if (factory == null) {
            factory = new WCECFactory();
        }
        return factory;
    }


    // ----------------------------------------------------------
    public static void installWOECFactory()
    {
        er.extensions.eof.ERXEC.setFactory(factory());
    }


    // ----------------------------------------------------------
    public static class PeerManager
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
        public EOEditingContext editingContext()
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
            if (transientState != null)
            {
                for (Object value : transientState.allValues())
                {
                    if (value instanceof EOManager.ECManager)
                    {
                        ((EOManager.ECManager)value).dispose();
                    }
                    else if (value instanceof EOEditingContext)
                    {
                        ((EOEditingContext)value).dispose();
                    }
                    else if (value instanceof PeerManager)
                    {
                        ((PeerManager)value).dispose();
                    }
                    else if (value instanceof PeerManagerPool)
                    {
                        ((PeerManagerPool)value).dispose();
                    }
                    else
                    {
                        log.error("unable to dispose() of "
                            + value.getClass() + ": " + value);
                    }
                }
                transientState = null;
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
        private EOEditingContext                    ec;
        private PeerManagerPool                     owner;
        private boolean                             cachePermanently;
        private NSMutableDictionary<String, Object> transientState;
        static Logger log = Logger.getLogger(
            PeerManager.class.getName().replace('$', '.'));
    }


    // ----------------------------------------------------------
    public static class PeerManagerPool
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

    private static Factory factory;
    static Logger log = Logger.getLogger(WCEC.class);
}
