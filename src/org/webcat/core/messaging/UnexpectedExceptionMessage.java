/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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

package org.webcat.core.messaging;

import org.jfree.util.Log;
import org.webcat.core.Application;
import org.webcat.core.User;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

//-------------------------------------------------------------------------
/**
 * A message that is sent to system administrators when an unexpected exception
 * occurs.
 *
 * @author Tony Allevato
 * @author  latest changes by: $Author$
 * @version $Revision$ $Date$
 */
public class UnexpectedExceptionMessage extends SysAdminMessage
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public UnexpectedExceptionMessage(Throwable t, WOContext context,
            NSDictionary extraInfo, String message)
    {
        this.exception = t;
        this.extraInfo = extraInfo;
        this.context = context;
        this.message = message;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Called by the subsystem init() to register the message.
     */
    public static void register()
    {
        Message.registerMessage(
                UnexpectedExceptionMessage.class,
                "Application",
                "Unexpected Exception",
                false,
                User.WEBCAT_RW_PRIVILEGES);
    }


    // ----------------------------------------------------------
    @Override
    public String fullBody()
    {
        return shortBody();
    }


    // ----------------------------------------------------------
    @Override
    public String shortBody()
    {
        String body = ((Application) Application.application())
            .informationForExceptionInContext(exception, extraInfo, context);

        if (message != null)
        {
            body = message + "\n\n" + body;
        }

        return body;
    }


    // ----------------------------------------------------------
    @Override
    public String title()
    {
        return "Unexpected Exception";
    }


    // ----------------------------------------------------------
    @Override
    public NSArray<User> users()
    {
        return User.systemAdmins(editingContext());
    }


    //~ Static/instance variables .............................................

    private Throwable exception;
    private NSDictionary extraInfo;
    private WOContext context;
    private String message;
}
