/*==========================================================================*\
 |  $Id: WCObjectStoreCoordinator.java,v 1.1 2011/12/25 02:24:54 stedwar2 Exp $
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
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
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
        if (log.isDebugEnabled())
        {
            String message = "creating " + getClass().getSimpleName()
                + "; " + this
                + "; by " + Thread.currentThread()
                + "; at " + System.currentTimeMillis();
            log.debug(message, new Exception("from here"));
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
    public void lock()
    {
//            EOSharedEditingContext sharedEC =
//                EOSharedEditingContext.defaultSharedEditingContext();
//            if (sharedEC != null)
//            {
//                sharedEC.lock();
//            }
        String message = null;
        if (log.isDebugEnabled())
        {
            synchronized (this)
            {
                message = "object store; " + this
                    + "; by " + Thread.currentThread()
                    + "; at " + System.currentTimeMillis();
                log.debug("lock() attempt; "
                    + message,
                    new Exception("from here"));
                if (owners == null)
                {
                    initDebugFields();
                }
                else if (owners.size() > 0
                    && owners.get(owners.size() - 1) != Thread.currentThread())
                {
                    log.warn("This object store is already locked "
                        + "by another thread! Owners = " + owners);
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
    public void unlock()
    {
        super.unlock();
        if (log.isDebugEnabled())
        {
            synchronized (this)
            {
                log.debug("unlock(); object store; " + this
                    + "; by " + Thread.currentThread()
                    + "; at " + System.currentTimeMillis()
                    // , new Exception("from here")
                );
                if (lockLocations == null)
                {
                    initDebugFields();
                }
                if (lockLocations.size() > 0)
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

    static Logger log = Logger.getLogger(WCObjectStoreCoordinator.class);
}
