/*==========================================================================*\
 |  $Id: LoggedError.java,v 1.2 2011/03/07 18:44:37 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2011 Virginia Tech
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

import org.webcat.core.LoggedError;
import org.webcat.core._LoggedError;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import er.extensions.foundation.ERXExceptionUtilities;

// -------------------------------------------------------------------------
/**
 * Represents data about a specific logged exception trace, the source of
 * the error, and the number of times this specific error has occurred.
 *
 * @author Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2011/03/07 18:44:37 $
 */
public class LoggedError
    extends _LoggedError
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new LoggedError object.
     */
    public LoggedError()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Retrieve the object associated with a given Throwable, creating one
     * if none exists.  Assumes the given editing context has been locked.
     *
     * @param context The editing context to use
     * @param throwable The error to look up
     * @return the corresponding LoggedError object
     */
    public static LoggedError objectForException(
            EOEditingContext context,
            Throwable        throwable
        )
    {
        if (throwable == null)
        {
            return null;
        }

        // Drill down to the root cause first
        throwable = ERXExceptionUtilities.getMeaningfulThrowable(throwable);

        StackTraceElement[] trace = throwable.getStackTrace();
        StackTraceElement top = (trace != null && trace.length > 0)
            ? trace[0]
            : new StackTraceElement("unknown", "unknown", "unknown", 0);

        LoggedError result = null;
        NSArray<LoggedError> results = errorsWithExceptionLocation(
            context,
            top.getClassName(),
            top.getLineNumber(),
            top.getMethodName(),
            throwable.getClass().getName() );
        if (results != null && results.count() > 0)
        {
            result = results.objectAtIndex(0);
        }
        else
        {
            result = create(context, top.getLineNumber(), 1);
            result.setExceptionName(throwable.getClass().getName());
            result.setInClass(top.getClassName());
            result.setInMethod(top.getMethodName());
        }
        return result;
    }


    // ----------------------------------------------------------
    private String trim(String value, int maxLen, boolean leftTrunc)
    {
        if (value != null && value.length() > maxLen)
        {
            if (leftTrunc)
            {
                value = value.substring(value.length() - maxLen);
            }
            else // truncate right side
            {
                value = value.substring(0, maxLen);
            }
        }
        return value;
    }


    // ----------------------------------------------------------
    private String tiny(String value)
    {
        return trim(value, TINY_LENGTH, false);
    }


    // ----------------------------------------------------------
    private String tinyRight(String value)
    {
        return trim(value, TINY_LENGTH, true);
    }


    // ----------------------------------------------------------
    private String medium(String value)
    {
        // Enforce limits of MEDIUMTEXT for this column, which can
        // hold max 2^16 = 65,536 characters
        return trim(value, MEDIUM_LENGTH, false);
    }


    // ----------------------------------------------------------
    @Override
    public void setInMethod(String value)
    {
        super.setInMethod(tiny(value));
    }


    // ----------------------------------------------------------
    @Override
    public void setInClass(String value)
    {
        super.setInClass(tinyRight(value));
    }


    // ----------------------------------------------------------
    @Override
    public void setExceptionName(String value)
    {
        super.setExceptionName(tinyRight(value));
    }


    // ----------------------------------------------------------
    @Override
    public void setMessage(String value)
    {
        super.setMessage(tiny(value));
    }


    // ----------------------------------------------------------
    @Override
    public void setStackTrace(String value)
    {
        super.setStackTrace(medium(value));
    }


    //~ Fields ................................................................

    private static final int MEDIUM_LENGTH = 65500;
    private static final int TINY_LENGTH = 250;
}
