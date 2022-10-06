package org.webcat.woextensions;


import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXThreadStorage;

/**
 * This class implements EOF stack pooling including EOF stack synchronizing.
 * It provides a special ERXEC.Factory in order to work without any changes in
 * existing applications. The number of EOObjectStoreCoordinators can be set
 * with the system Property
 * <code>er.extensions.ERXObjectStoreCoordinatorPool.maxCoordinators</code>.
 * Each Session will become one EOObjectStoreCoordinator and the method
 * <code>newEditingContext</code> will always return an
 * <code>EOEditingContext</code> with the same
 * <code>EOObjectStoreCoordinator</code> for the same <code>WOSession</code>.
 * This first release uses round-robin pooling, future versions might use
 * better algorithms to decide which <code>EOObjectStoreCoordinator</code>
 * will be used for the next new <code>WOSession</code>.<br>The code is
 * tested in a heavy multithreaded application and afawk no deadlock occurs,
 * neither in EOF nor directly in Java.
 *
 * @author David Teran, Frank Caputo @ cluster9
 */
public class WCObjectStoreCoordinatorPool
{
    private static final Logger log =
        Logger.getLogger(WCObjectStoreCoordinatorPool.class);

    private static final String THREAD_OSC_KEY =
        WCObjectStoreCoordinatorPool.class.getName() + ".threadOSC";
    private Map<String, EOObjectStore> _oscForSession;
    private int _maxObjectStoreCoordinators;
    private int _currentObjectStoreIndex;
    private List<WCObjectStoreCoordinator> _objectStores;
    private List<EOSharedEditingContext> _sharedEditingContexts;
    private Object _lock = new Object();
    private static WCObjectStoreCoordinatorPool
        _sharedObjectStoreCoordinatorPool;

    public static WCObjectStoreCoordinatorPool _pool()
    {
    	return _sharedObjectStoreCoordinatorPool;
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
        if (_sharedObjectStoreCoordinatorPool == null)
        {
            WCObjectStoreCoordinatorSynchronizer.initialize();
            _sharedObjectStoreCoordinatorPool =
                new WCObjectStoreCoordinatorPool(
                ERXProperties.intForKey(
                WCObjectStoreCoordinatorPool.class.getName()
                + ".maxCoordinators"));
            log.info("setting ERXEC.factory to MultiOSCFactory");
            ERXEC.setFactory(new MultiOSCFactory(
                _sharedObjectStoreCoordinatorPool,
                WCEC.factory()));
        }
    }

    /**
     * Creates a new WCObjectStoreCoordinatorPool. This object is a singleton.
     * This object is responsible to provide EOObjectStoreCoordinators
     * based on the current Threads' session.
     * It is used by MultiOSCFactory to get a rootObjectStore if the
     * MultiOSCFactory is asked for a new EOEditingContext.
     */
    public WCObjectStoreCoordinatorPool(int maxObjectStoreCoordinators)
    {
    	_maxObjectStoreCoordinators = maxObjectStoreCoordinators;
        if (_maxObjectStoreCoordinators == 0)
        {
            //this should work like the default implementation
            log.warn("Registering the pool with only one coordinator "
                + "doesn't make a lot of sense.");
            _maxObjectStoreCoordinators = 1;
        }
        _oscForSession = new HashMap<String, EOObjectStore>();

        NSNotificationCenter.defaultCenter().addObserver(this,
            new NSSelector<Void>("sessionDidCreate",
                ERXConstant.NotificationClassArray),
            WOSession.SessionDidCreateNotification,
            null);
        NSNotificationCenter.defaultCenter().addObserver(this,
            new NSSelector<Void>("sessionDidTimeout",
                ERXConstant.NotificationClassArray),
            WOSession.SessionDidTimeOutNotification,
            null);
    }

    /**
     * checks if the new Session has already  a EOObjectStoreCoordinator
     * assigned, if not it assigns a EOObjectStoreCoordinator to the session.
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
        // return ERXSession.currentSessionID();
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
     * Returns the object store for the current thread (or requests one and
     * sets it if there isn't one).
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
     * Lazy initializes the objectStores and then returns the next one,
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
            if (_currentObjectStoreIndex == _maxObjectStoreCoordinators)
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
    public static class MultiOSCFactory implements ERXEC.Factory
    {
        private WCObjectStoreCoordinatorPool _pool;
        private boolean _useSharedEditingContext;
        private WCEC.Factory _backingFactory;

        public MultiOSCFactory(
            WCObjectStoreCoordinatorPool pool,
            WCEC.Factory backingFactory)
        {
            _pool = pool;
            _backingFactory = backingFactory;
            _useSharedEditingContext = ERXProperties.booleanForKeyWithDefault(
                ERXEC.class.getName() + ".useSharedEditingContext", true);
        }

        public Object defaultEditingContextDelegate()
        {
            return _backingFactory.defaultEditingContextDelegate();
        }

        public void setDefaultEditingContextDelegate(Object delegate)
        {
            _backingFactory.setDefaultEditingContextDelegate(delegate);
        }

        public Object defaultNoValidationDelegate()
        {
            return _backingFactory.defaultNoValidationDelegate();
        }

        public void setDefaultNoValidationDelegate(Object delegate)
        {
            _backingFactory.setDefaultNoValidationDelegate(delegate);
        }

        public void setDefaultDelegateOnEditingContext(EOEditingContext ec)
        {
            _backingFactory.setDefaultDelegateOnEditingContext(ec);
        }

        public void setDefaultDelegateOnEditingContext(
            EOEditingContext ec, boolean validation)
        {
            _backingFactory.setDefaultDelegateOnEditingContext(ec, validation);
        }

        public EOEditingContext _newEditingContext(EOObjectStore objectStore)
        {
            return _backingFactory._newEditingContext(objectStore);
        }

        public EOEditingContext _newEditingContext(
            EOObjectStore objectStore, boolean validationEnabled)
        {
            return _backingFactory._newEditingContext(
                objectStore, validationEnabled);
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

        public boolean useSharedEditingContext()
        {
            if (_backingFactory instanceof ERXEC.DefaultFactory)
            {
                return ((ERXEC.DefaultFactory)_backingFactory)
                    .useSharedEditingContext();
            }
            return _useSharedEditingContext;
        }

		public void setUseSharedEditingContext(boolean value)
		{
            if (_backingFactory instanceof ERXEC.DefaultFactory)
            {
                ((ERXEC.DefaultFactory)_backingFactory)
                    .setUseSharedEditingContext(value);
            }
			_useSharedEditingContext = value;
		}

    }

    private void _initObjectStores()
    {
        log.info("initializing Pool...");
        _objectStores = new ArrayList<WCObjectStoreCoordinator>(
            _maxObjectStoreCoordinators);
        _sharedEditingContexts = new ArrayList<EOSharedEditingContext>(
            _maxObjectStoreCoordinators);

        String className = ERXProperties.stringForKeyWithDefault(
            "EOSharedEditingContext.defaultSharedEditingContextClassName",
            WCSharedEC.class.getName());
        try
        {
            Constructor<? extends EOSharedEditingContext> sharedEditingContextConstructor =
                Class.forName(className)
                .asSubclass(EOSharedEditingContext.class)
                .getConstructor(EOObjectStore.class);
            for (int i = 0; i < _maxObjectStoreCoordinators; i++)
            {
                WCObjectStoreCoordinator os = new WCObjectStoreCoordinator();
                _objectStores.add(os);
                _sharedEditingContexts.add(sharedEditingContextConstructor.newInstance(os));
            }
            if (_maxObjectStoreCoordinators > 0)
            {
                EOObjectStoreCoordinator.setDefaultCoordinator(
                    _objectStores.get(0));
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to create "
                + "defaultSharedEditingContext with className = "
                + className, e);
        }

        log.info("initializing Pool finished");
     }
}

