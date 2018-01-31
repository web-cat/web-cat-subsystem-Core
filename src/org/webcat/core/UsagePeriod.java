/*==========================================================================*\
 |  $Id: UsagePeriod.java,v 1.1 2012/01/27 16:36:20 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2012 Virginia Tech
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

package org.webcat.core;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

// -------------------------------------------------------------------------
/**
 * Represents a login period by a user, recorded for historical access.
 *
 * @author  Stephen Edwards
 * @author  Last changed by: $Author: stedwar2 $
 * @version $Revision: 1.1 $, $Date: 2012/01/27 16:36:20 $
 */
public class UsagePeriod
    extends _UsagePeriod
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new UsagePeriod object.
     */
    public UsagePeriod()
    {
        super();
    }


    // ----------------------------------------------------------
    public static UsagePeriod currentUsagePeriodForUser(
        EOEditingContext editingContext, User aUser)
    {
        NSTimestamp now = new NSTimestamp();
        UsagePeriod period = mostRecentForUser(editingContext, aUser);
        if (period != null
            && (period.isLoggedOut()
                || now.getTime() - period.endTime().getTime()
                    > USAGE_PERIOD_WINDOW))
        {
            period = null;
        }
        if (period == null)
        {
            period = UsagePeriod.create(
                editingContext, now, false, now, aUser);
        }
        else
        {
            period.setEndTime(now);
        }
        return period;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Changes the end time to "now".
     */
    public void updateEndTime()
    {
        setEndTime(new NSTimestamp());
    }


    // ----------------------------------------------------------
    public boolean isStillActive()
    {
        if (isLoggedOut())
        {
            return false;
        }

        NSArray<LoginSession> sessions = LoginSession.objectsMatchingQualifier(
            editingContext(),
            LoginSession.usagePeriod.is(this));
        if (sessions.size() > 0)
        {
            return true;
        }

        return (System.currentTimeMillis() - endTime().getTime())
            < USAGE_PERIOD_WINDOW;
    }


    //~ Instance/static variables .............................................

    private static final long USAGE_PERIOD_WINDOW = 1000 * 60 * 5; // 5 minutes
}
