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

package net.sf.webcat.core.messaging;

import java.net.InetAddress;
import org.apache.log4j.Logger;
import net.sf.webcat.core.Application;
import net.sf.webcat.core.User;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

//-------------------------------------------------------------------------
/**
 * A message that is broadcast when the system starts up.
 *
 * @author Tony Allevato
 * @author  latest changes by: $Author$
 * @version $Revision$ $Date$
 */
public class ApplicationStartupMessage extends Message
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Called by the subsystem init() to register the message.
     */
    public static void register()
    {
        Message.registerMessage(
                ApplicationStartupMessage.class,
                "Application",
                "Web-CAT Startup",
                true,
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
        return "The Web-CAT server hosted at " + hostString()
            + " has started up.";
    }


    // ----------------------------------------------------------
    @Override
    public String title()
    {
        return "Web-CAT (" + hostString() + ") has started up";
    }


    // ----------------------------------------------------------
    @Override
    public NSArray<User> users()
    {
        return null;
//        return User.objectsMatchingQualifier(editingContext(),
//            User.accessLevel.greaterThanOrEqualTo(
//                    (int) User.WEBCAT_RW_PRIVILEGES));
    }


    //~ Private methods .......................................................

    // ----------------------------------------------------------
    /**
     * Gets a string representing the host name of the Web-CAT server that is
     * starting up.
     */
    private String hostString()
    {
        String host = Application.application().host();
        int port = Application.application().port().intValue();

        return (port == 80) ? host : host + ":" + port;
    }
}
