/*==========================================================================*\
 |  $Id: WCSharedEC.java,v 1.1 2011/12/25 02:24:54 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011 Virginia Tech
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

// -------------------------------------------------------------------------
/**
 *  This is a specialized shared editing context that enforces read-only
 *  access.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.1 $, $Date: 2011/12/25 02:24:54 $
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
                    for (String msg : lockLocations)
                    {
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
                message = "lock() acquired; " + message;
                new Exception(message).printStackTrace(out);
                lockLocations.add(writer.toString());
                owners.add(Thread.currentThread());
                log.debug(message);
            }
        }
    }


    // ----------------------------------------------------------
    @Override
    public boolean tryLock()
    {
        String message = null;
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
                    message = "tryLock() acquired; " + message;
                    new Exception(message).printStackTrace(out);
                    lockLocations.add(writer.toString());
                    owners.add(Thread.currentThread());
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
                log.debug("unlock(); shared EC; " + this
                    + "; by " + Thread.currentThread()
                    + "; at " + System.currentTimeMillis()
                    // , new Exception("from here")
                );
                if (lockLocations == null)
                {
                    initDebugFields();
                }
                else if (lockLocations.size() > 0)
                {
                    lockLocations.remove(lockLocations.size() - 1);
                    owners.remove(owners.size() - 1);
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
        super.dispose();
    }


    // ----------------------------------------------------------
    private void initDebugFields()
    {
        lockLocations = new ArrayList<String>(16);
        owners = new ArrayList<Thread>(16);
        writer = new StringWriter();
        out = new PrintWriter(writer);
    }


    //~ Instance/static variables .............................................

    private List<String> lockLocations = null;
    private List<Thread> owners = null;
    private StringWriter writer = null;
    private PrintWriter out = null;

    static Logger log = Logger.getLogger(WCSharedEC.class);
}
