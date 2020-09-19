/*==========================================================================*\
 |  Copyright (C) 2021 Virginia Tech
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
import java.util.Hashtable;
import java.util.List;
import org.apache.log4j.Logger;
import com.webobjects.appserver.WOSession;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import er.extensions.appserver.ERXSession;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXObjectStoreCoordinatorSynchronizer;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXThreadStorage;

//-------------------------------------------------------------------------
/**
 * Customized version of ERXObjectStoreCoordinatorPool, which couldn't be
 * extended in the right ways via inheritance.
 *
 * @author  edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class WCObjectStoreCoordinatorPool
{
    //~ Constructors ..........................................................

    /**
     * Creates a new WCObjectStoreCoordinatorPool. This object is a singleton.
     * This object is responsible to provide EOObjectStoreCoordinators
     * based on the current Threads' session.
     * It is used by MultiOSCFactory to get a rootObjectStore if the
     * MultiOSCFactory is asked for a new EOEditingContext.
     */
    private WCObjectStoreCoordinatorPool()
    {
        _maxOS = ERXProperties.intForKey(
            WCObjectStoreCoordinatorPool.class.getName()
            + ".maxCoordinators");
        if (_maxOS == 0)
        {
            //this should work like the default implementation
            log.warn("Registering the pool with only one coordinator doesn't make a lot of sense.");
            _maxOS = 1;
        }
        _oscForSession = new Hashtable<String, EOObjectStore>();

        NSSelector<Void> sel = new NSSelector<Void>(
            "sessionDidCreate", ERXConstant.NotificationClassArray);
        NSNotificationCenter.defaultCenter().addObserver(
            this, sel, WOSession.SessionDidCreateNotification, null);
        sel = new NSSelector<Void>(
            "sessionDidTimeout", ERXConstant.NotificationClassArray);
        NSNotificationCenter.defaultCenter().addObserver(
            this, sel, WOSession.SessionDidTimeOutNotification, null);
    }


    //~ Methods ...............................................................

    public static WCObjectStoreCoordinatorPool _pool()
    {
        return _pool;
    }

    /**
     * Calls initialize() if the required system properties exist.
     *
     */
    public static void initializeIfNecessary()
    {
        if (ERXProperties.stringForKey(
            WCObjectStoreCoordinatorPool.class.getName()
            + ".maxCoordinators") != null)
        {
            WCObjectStoreCoordinatorPool.initialize();
        }
    }

    /**
     * Creates the singleton and registers the multi factory.
     */
    public static void initialize()
    {
        if (_pool == null)
        {
            ERXObjectStoreCoordinatorSynchronizer.initialize();
            _pool = new WCObjectStoreCoordinatorPool();
            log.info("setting ERXEC.factory to MultiOSCFactory");
            ERXEC.setFactory(new MultiOSCFactory());
        }
    }


    /**
     * checks if the new Session has already  a EOObjectStoreCoordinator assigned,
     * if not it assigns a EOObjectStoreCoordinator to the session.
     * @param n {@link WOSession#SessionDidCreateNotification}
     */
    public void sessionDidCreate(NSNotification n)
    {
        WOSession s = (WOSession) n.object();
        if (_oscForSession.get(s.sessionID()) == null)
        {
            _oscForSession.put(s.sessionID(), currentThreadObjectStore());
        }
    }

    /** Removes the timed out session from the internal array.
     * session.
     * @param n {@link WOSession#SessionDidTimeOutNotification}
     */
    public void sessionDidTimeout(NSNotification n)
    {
        String sessionID = (String) n.object();
        _oscForSession.remove(sessionID);
    }

    /**
     * @return the sessionID from the session stored in ERXThreadStorage.
     */
    protected String sessionID()
    {
        WOSession session = ERXSession.session();
        String sessionID = null;
        if (session != null)
        {
            sessionID = session.sessionID();
        }
        return sessionID;
    }

    /**
     * returns the session related EOObjectStoreCoordinator.
     * If session is null then it returns the nextObjectStore.
     * This method is used to create new EOEditingContexts with the MultiOSCFactory
     * @return an EOEditingContext
     */
    public EOObjectStore currentRootObjectStore()
    {
        String sessionID = sessionID();
        EOObjectStore os = null;
        if (sessionID != null)
        {
            os = _oscForSession.get(sessionID);
            if (os == null)
            {
                os = currentThreadObjectStore();
                _oscForSession.put(sessionID, os);
            }
            else
            {
                // Check to see if thread already has an object store
                WCObjectStoreCoordinator.ThreadLockedObjectStore threadLocked =
                    WCObjectStoreCoordinator.threadHasLockedStore();
                if (threadLocked != null && threadLocked.os != os)
                {
                    log.fatal("currentRootObjectStore(): thead object store = "
                        + os + ", but current thread already holds lock on "
                        + threadLocked.os);
                }
                EOObjectStore threadOs = (EOObjectStore) ERXThreadStorage
                    .valueForKey(WCObjectStoreCoordinatorPool.THREAD_OSC_KEY);
                if (threadOs != null && threadOs != os)
                {
                    log.warn("currentRootObjectStore(): replacing thread "
                        + "store " + threadOs + " with " + os);
                }
                ERXThreadStorage.takeValueForKey(
                    os, WCObjectStoreCoordinatorPool.THREAD_OSC_KEY);
            }
        }
        else
        {
            os = currentThreadObjectStore();
        }
        return os;
    }

    /**
     * Returns the object store for the current thread (or requests one and sets it if there isn't one).
     *
     * @return the object store for the current thread
     */
    protected EOObjectStore currentThreadObjectStore()
    {
        EOObjectStore os = (EOObjectStore) ERXThreadStorage.valueForKey(
            WCObjectStoreCoordinatorPool.THREAD_OSC_KEY);
        if (os == null)
        {
            WCObjectStoreCoordinator.ThreadLockedObjectStore threadLocked =
                WCObjectStoreCoordinator.threadHasLockedStore();
            if (threadLocked != null)
            {
                log.error("currentThreadObjectStore(): no thread OSC, but "
                    + "thread has already locked " + threadLocked.os);
                os = threadLocked.os;
            }
            else
            {
                os = nextObjectStore();
            }
            if (os != null)
            {
                ERXThreadStorage.takeValueForKey(
                    os, WCObjectStoreCoordinatorPool.THREAD_OSC_KEY);
            }
        }
        return os;
    }

    /**
     * Lazy initialises the objectStores and then returns the next one,
     * this is based on round robin.
     * @return the next EOObjectStore based on round robin
     */
    public EOObjectStore nextObjectStore()
    {
        synchronized (_lock)
        {
            if (_objectStores == null)
            {
                _initObjectStores();
            }
            if (_currentObjectStoreIndex == _maxOS)
            {
                _currentObjectStoreIndex = 0;
            }
            // Use old round-robin strategy for finding starting candidate
            WCObjectStoreCoordinator os =
                _objectStores.get(_currentObjectStoreIndex++);
            int size = os.sourceCount();
            if (size > 0)
            {
                // search for lowest occupancy store, if round-robin already
                // has any users
                for (WCObjectStoreCoordinator os2 : _objectStores)
                {
                    int size2 = os2.sourceCount();
                    if (size2 < size)
                    {
                        os = os2;
                        size = size2;
                    }
                }
            }
            return os;
        }
    }

    public EOSharedEditingContext sharedEditingContextForObjectStore(
        EOObjectStore os)
    {
        int index = _objectStores.indexOf(os);
        EOSharedEditingContext ec = null;
        if (index >= 0)
        {
            ec = _sharedEditingContexts.get(index);
        }
        return ec;
    }

    /**
     * This class uses different EOF stack when creating new EOEditingContexts.
     */
    public static class MultiOSCFactory extends ERXEC.DefaultFactory
    {
        public MultiOSCFactory()
        {
            super();
        }

        public EOEditingContext _newEditingContext()
        {
            return _newEditingContext(true);
        }

        public EOEditingContext _newEditingContext(boolean validationEnabled)
        {
            EOObjectStore os = _pool.currentRootObjectStore();
            EOEditingContext ec = _newEditingContext(os, validationEnabled);
            ec.lock();
            try
            {
                EOSharedEditingContext sec = (useSharedEditingContext())
                    ? _pool.sharedEditingContextForObjectStore(os)
                    : null;
                ec.setSharedEditingContext(sec);
            }
            finally
            {
                ec.unlock();
            }
            return ec;
        }
    }

    private void _initObjectStores()
    {
        log.info("initializing Pool...");
        _objectStores = new ArrayList<WCObjectStoreCoordinator>(_maxOS);
        _sharedEditingContexts = new ArrayList<EOSharedEditingContext>(_maxOS);
        for (int i = 0; i < _maxOS; i++)
        {
            WCObjectStoreCoordinator os = new WCObjectStoreCoordinator();
            _objectStores.add(os);
            _sharedEditingContexts.add(new WCSharedEC(os));
        }
        if (_maxOS > 0)
        {
            EOObjectStoreCoordinator.setDefaultCoordinator(
                _objectStores.get(0));
        }
        log.info("initializing Pool finished");
     }


    //~ Instance/static fields ................................................

    static Logger log = Logger.getLogger(WCObjectStoreCoordinatorPool.class);
    private static final String THREAD_OSC_KEY =
        WCObjectStoreCoordinatorPool.class.getName() + ".threadOSC";
    private Hashtable<String, EOObjectStore> _oscForSession;
    private int _maxOS;
    private int _currentObjectStoreIndex;
    private List<WCObjectStoreCoordinator> _objectStores;
    private List<EOSharedEditingContext> _sharedEditingContexts;
    private Object _lock = new Object();
    protected static WCObjectStoreCoordinatorPool _pool;

}
