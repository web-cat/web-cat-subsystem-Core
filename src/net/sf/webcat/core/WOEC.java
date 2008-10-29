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

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  This is a specialized editing context subclass that is used to track
 *  down an obscure WO bug.
 *
 *  @author  Stephen Edwards
 *  @version $Id$
 */
public class WOEC
    extends er.extensions.eof.ERXEC
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new WOEC object.
     */
    public WOEC()
    {
        super();
        if (log.isDebugEnabled())
        {
            log.debug("creating new ec: " + hashCode());
        }
    }


    // ----------------------------------------------------------
    /**
     * Creates a new WOEC object.
     * @param os the parent object store
     */
    public WOEC( EOObjectStore os )
    {
        super( os );
        if (log.isDebugEnabled())
        {
            log.debug("creating new ec: " + hashCode());
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void dispose()
    {
        if (log.isDebugEnabled())
        {
            log.debug("dispose(): " + hashCode());
        }
        super.dispose();
    }


    // ----------------------------------------------------------
    public static class WOECFactory
        extends er.extensions.eof.ERXEC.DefaultFactory
    {
        protected EOEditingContext _createEditingContext( EOObjectStore parent )
        {
            return new WOEC( parent == null
                             ? EOEditingContext.defaultParentObjectStore()
                             : parent );
        }
    }


    // ----------------------------------------------------------
    public static void installWOECFactory()
    {
        er.extensions.eof.ERXEC.setFactory( new WOECFactory() );
    }


    // ----------------------------------------------------------
    public static class PeerManager
    {
        // ----------------------------------------------------------
        public PeerManager(PeerManagerPool pool)
        {
            owner = pool;
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
                ec = Application.newPeerEditingContext();
                if (log.isDebugEnabled())
                {
                    log.debug("creating ec: " + ec.hashCode()
                        + " for manager: " + this);
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
                    log.debug("disposing ec: " + ec.hashCode()
                        + " for manager: " + this);
                }
                Application.releasePeerEditingContext(ec);
                ec = null;
            }
            else
            {
                log.debug("dispose() called with null ec for manager: " + this);
            }
            if (transientState != null)
            {
                NSArray values = transientState.allValues();
                for (int i = 0; i < values.count(); i++)
                {
                    Object value = values.objectAtIndex(i);
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
                }
                transientState = null;
            }
        }


        // ----------------------------------------------------------
        public void sleep()
        {
            if (ec != null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("sleep(): " + this);
                }
                if (cachePermanently)
                {
                    owner.cachePermanently( this );
                }
                else
                {
                    owner.cache( this );
                }
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
        public NSMutableDictionary transientState()
        {
            if (transientState == null)
            {
                transientState = new NSMutableDictionary();
            }
            return transientState;
        }


        //~ Instance/static variables .........................................
        private EOEditingContext    ec;
        private PeerManagerPool    owner;
        private boolean             cachePermanently;
        private NSMutableDictionary transientState;
        static Logger log = Logger.getLogger(
            PeerManager.class.getName().replace('$', '.'));
    }


    // ----------------------------------------------------------
    public static class PeerManagerPool
    {
        // ----------------------------------------------------------
        public PeerManagerPool()
        {

            managerCache = new NSMutableArray();
            permanentManagerCache = new NSMutableArray();
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
        private void cache(PeerManager manager, NSMutableArray cache)
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
                    ((PeerManager)cache.objectAtIndex(0)).dispose();
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
        private void dispose(NSMutableArray cache)
        {
            for (int i = 0; i < cache.count(); i++)
            {
                ((PeerManager)cache.objectAtIndex(0)).dispose();
            }
            cache.clear();
        }


        //~ Instance/static variables .........................................
        private NSMutableArray managerCache;
        private NSMutableArray permanentManagerCache;
        static Logger log = Logger.getLogger(
            PeerManagerPool.class.getName().replace('$', '.'));
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( WOEC.class );
}
