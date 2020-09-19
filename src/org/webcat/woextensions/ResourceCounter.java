/*==========================================================================*\
 |  Copyright (C) 2018 Virginia Tech
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * Represents a counter that can be used to track the allocation/deallocation
 * of shared resources in order to track resources leaks.
 *
 * @author  Stephen Edwards
 */
public class ResourceCounter
{
    //~ Instance/static fields ................................................

    private String name;
    private int threshold;
    private int period;
    private int allocCount = 0;
    private int deallocCount = 0;
    private Map<Object, String> objects = new HashMap<Object, String>();
    private Map<String, Counter> counters = new HashMap<String, Counter>();
    private final Logger log;
    private NSTimestamp nextDump;

    private static final int MAX_FRAMES = 15;
    private static final int MAX_FRAMES_EXAMINED = 32;
    private static final int TRIM_FRAMES = 2 + 1;
    private static final int PERIODIC_DUMP_HOURS = 1;

    private static class Counter {
        public int count = 0;
        public boolean overThreshold = false;
        public boolean wasOverThreshold = false;
    }


    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public ResourceCounter(String name)
    {
//        this(name, 750, 100);
        this(name, 100, 20);
    }


    // ----------------------------------------------------------
    public ResourceCounter(String name, int threshold, int period)
    {
        this.name = name;
        this.threshold = threshold;
        this.period = period;
        this.log = Logger.getLogger(this.getClass().getName() + "." + name);
        nextDump = null;   // calculateNextDump();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public synchronized void allocate(Object resource)
    {
        String here = stackTrace();
        String type = resource.getClass().getSimpleName();
        if (objects.containsKey(resource))
        {
            log.error("duplicate allocation of " + type
                + " : " + resource.hashCode()
                + " : " + resource
                + "\n" + here);
        }
        else
        {
            objects.put(resource, here);
        }
        Counter c = counters.get(here);
        if (c == null)
        {
            c = new Counter();
            counters.put(here, c);
        }
        c.count++;
        allocCount++;

        if (c.overThreshold)
        {
            if (allocCount % period == 0)
            {
//                dumpLeaks();
                log.info("leakinfo:CSV3:" + c.overThreshold
                    + "," + name
                    + "," + here.hashCode()
                    + "," + c.count
                    + "\n" + here);
            }
        }
        else if (c.count > threshold)
        {
            c.overThreshold = true;
            c.wasOverThreshold = true;
            log.error("leakinfo: pool " + name
                + " exceeded threshold " + threshold);
//            dumpLeaks();
            log.info("leakinfo:CSV3:" + c.overThreshold
                + "," + name
                + "," + here.hashCode()
                + "," + c.count
                + "\n" + here);
        }
//        else if (log.isDebugEnabled()
//            && (nextDump == null || new NSTimestamp().after(nextDump)))
//        {
//            log.info("Scheduled periodic resource tracking dump for " + name);
//            dumpLeaks();
//            nextDump = calculateNextDump();
//        }

//        log.debug("CSV:allocate," + resource.getClass().getSimpleName()
//            + "," + resource.hashCode()
//            + "," + here.hashCode()
//            + "," + allocCount
//            + "," + deallocCount
//            + "," + c.count
//            + "\n" + here);
    }


    public synchronized void deallocate(Object resource)
    // ----------------------------------------------------------
    {
        String type = resource.getClass().getSimpleName();
        if (!objects.containsKey(resource))
        {
            log.error("missing allocation record of " + type
                + " : " + resource.hashCode()
                + " : " + resource,
                stackTraceHere());
            return;
        }
        String here = objects.get(resource);
        objects.remove(resource);
        Counter c = counters.get(here);
        if (c == null)
        {
            log.error("missing allocation count of " + type + " : " + resource,
                stackTraceHere());
            return;
        }
        if (c.count < 1)
        {
            log.error("allocation count of " + type + " : " + resource
                + " is already 0 on deallocation",
                stackTraceHere());
        }
        else
        {
            c.count--;
        }
        deallocCount++;

        if (c.overThreshold)
        {
            if (c.count < threshold - period)
            {
                c.overThreshold = false;
                log.info("leakinfo: pool " + name + " fell back below threshold "
                    + threshold + ", allocations = " + allocCount
                    + ", deallocations = " + deallocCount);
                log.info("leakinfo:-CSV3:" + c.overThreshold
                    + "," + name
                    + "," + here.hashCode()
                    + "," + c.count
                    + "\n" + here);
                c.overThreshold = false;
            }
        }
        else if (c.wasOverThreshold && c.count % period == 0)
        {
            log.info("leakinfo:-CSV3:" + c.overThreshold
                + "," + name
                + "," + here.hashCode()
                + "," + c.count
                + "\n" + here);
        }
        if (c.count == 0)
        {
            c.wasOverThreshold = false;
        }

//        log.debug("CSV:deallocate," + resource.getClass().getSimpleName()
//            + "," + resource.hashCode()
//            + "," + here.hashCode()
//            + "," + allocCount
//            + "," + deallocCount
//            + "," + c.count
//            + "\n" + here);
    }


    // ----------------------------------------------------------
    public synchronized void dumpLeaks()
    {
        log.info("CSV2:" + name
            + "," + objects.size()
            + "," + allocCount
            + "," + deallocCount
            + "," + (allocCount - deallocCount));
        for (Map.Entry<String, Counter> pair : counters.entrySet())
        {
            if (pair.getValue().count > 5)
            {
                log.info("CSV3:" + name
                    + "," + pair.getKey().hashCode()
                    + "," + pair.getValue().count
                    + "\n" + pair.getKey());
            }
        }
    }


    // ----------------------------------------------------------
    private NSTimestamp calculateNextDump()
    {
        NSTimestamp now = new NSTimestamp();

        // Return 3 hours from now
        return now.timestampByAddingGregorianUnits(
            0, 0, 0, PERIODIC_DUMP_HOURS, 0, 0);
    }


    // ----------------------------------------------------------
    private RuntimeException stackTraceHere()
    {
        return stackTraceHere(TRIM_FRAMES, MAX_FRAMES);
    }


    // ----------------------------------------------------------
    private RuntimeException stackTraceHere(int trim, int size)
    {
        RuntimeException e = new RuntimeException("here");
        StackTraceElement[] fullTrace = e.getStackTrace();
        List<StackTraceElement> keep =
            new ArrayList<StackTraceElement>(MAX_FRAMES);
        List<StackTraceElement> full =
            new ArrayList<StackTraceElement>(MAX_FRAMES_EXAMINED);
        boolean skipping = false; // true

        for (int i = trim; i < MAX_FRAMES_EXAMINED
             && keep.size() < MAX_FRAMES
             && i < fullTrace.length; i++)
        {
            if (skipping)
            {
                if (fullTrace[i].getClassName()
                    .startsWith("org.webcat.woextensions.WCEC"))
                {
                    continue;
                }
                else
                {
                    skipping = false;
                }
            }
            full.add(fullTrace[i]);
            if (fullTrace[i].getClassName().startsWith("org.webcat"))
            {
                keep.add(fullTrace[i]);
            }
        }

        if (keep.size() > 0)
        {
            StackTraceElement last = keep.get(keep.size() - 1);
            if ("org.webcat.core.Application".equals(last.getClassName())
                && "dispatchRequest".equals(last.getMethodName()))
            {
                keep.clear();
                keep = full;
            }
            else
            {
                full.clear();
            }
            e.setStackTrace(keep.toArray(new StackTraceElement[keep.size()]));
        }
        else
        {
            full.clear();
        }
        return e;
    }


    // ----------------------------------------------------------
    private String stackTrace()
    {
        return stackTrace(stackTraceHere(TRIM_FRAMES, MAX_FRAMES));
    }


    // ----------------------------------------------------------
    private String stackTrace(Throwable t)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            PrintStream ps = new PrintStream(baos);
            t.printStackTrace(ps);
            ps.close();
            baos.close();
        }
        catch (IOException e)
        {
            log.error(e);
        }
        return baos.toString();
    }
}
