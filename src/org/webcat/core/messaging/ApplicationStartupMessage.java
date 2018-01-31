/*==========================================================================*\
 |  $Id: ApplicationStartupMessage.java,v 1.4 2011/12/25 02:24:54 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2010-2011 Virginia Tech
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

import org.webcat.core.Application;
import org.webcat.core.User;
import com.webobjects.foundation.NSArray;

//-------------------------------------------------------------------------
/**
 * A message that is broadcast when the system starts up.
 *
 * @author  Tony Allevato
 * @author  latest changes by: $Author: stedwar2 $
 * @version $Revision: 1.4 $ $Date: 2011/12/25 02:24:54 $
 */
public class ApplicationStartupMessage
    extends SysAdminMessage
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
    public synchronized NSArray<User> users()
    {
        return User.systemAdmins(editingContext());
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

        return (port == 80 || port == -1) ? host : host + ":" + port;
    }
}
