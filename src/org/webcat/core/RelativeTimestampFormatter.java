/*==========================================================================*\
 |  $Id: RelativeTimestampFormatter.java,v 1.2 2011/05/13 19:46:57 aallowat Exp $
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

package org.webcat.core;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * <p>
 * A formatter that operates on {@code NSTimestamp} objects, formatting them
 * as strings that represent relative time differences (for example, "2 days,
 * 3 hours ago"). See
 * {@link TimeUtilities#stringForTimeFromNow(NSTimestamp, boolean)} and
 * {@link TimeUtilities#stringForTimeDifference(NSTimestamp, NSTimestamp, boolean)}
 * for details on how the formatting is actually performed.
 * </p><p>
 * Instances of these objects can be created by manually for formatting dates
 * that are relative to a specific time, but in the frequent case of using the
 * formatter to format a {@code WOString} in a component, you can use the
 * {@code application.approximateRelativeTimestampFormatter} and
 * {@code application.exactRelativeTimestampFormatter} bindings to easily
 * retrieve instances of those.
 * </p>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2011/05/13 19:46:57 $
 */
public class RelativeTimestampFormatter extends Format
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new {@code RelativeTimestampFormatter} that will use the
     * time of the {@link #format(Object)} method being called as the origin,
     * and with the specified behavior with regard to approximation.
     *
     * @param approximate true if the formatted string should be approximate,
     *     false if it should be exact
     */
    public RelativeTimestampFormatter(boolean approximate)
    {
        this(null, approximate);
    }


    // ----------------------------------------------------------
    /**
     * Creates a new {@code RelativeTimestampFormatter} that will use the
     * specified origin and behavior with regard to approximation.
     *
     * @param origin the origin timestamp
     * @param approximate true if the formatted string should be approximate,
     *     false if it should be exact
     */
    public RelativeTimestampFormatter(NSTimestamp origin, boolean approximate)
    {
        this.origin = origin;
        this.approximate = approximate;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo,
            FieldPosition pos)
    {
        if (obj instanceof NSTimestamp)
        {
            NSTimestamp target = (NSTimestamp) obj;

            if (origin == null)
            {
                toAppendTo.append(TimeUtilities.stringForTimeFromNow(
                        target, approximate));
            }
            else
            {
                toAppendTo.append(TimeUtilities.stringForTimeDifference(
                        origin, target, approximate));
            }

            return toAppendTo;
        }
        else
        {
            throw new IllegalArgumentException("RelativeTimeFormatter can "
                    + "only format objects of type NSTimestamp");
        }
    }


    // ----------------------------------------------------------
    @Override
    public Object parseObject(String source, ParsePosition pos)
    {
        throw new UnsupportedOperationException("RelativeTimestampFormatter "
                + "does not support parsing.");
    }


    //~ Static/instance variables .............................................

    private static final long serialVersionUID = 2945994740438265771L;

    private NSTimestamp origin;
    private boolean approximate;
}
