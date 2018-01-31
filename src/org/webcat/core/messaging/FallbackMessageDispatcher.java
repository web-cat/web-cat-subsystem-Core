/*==========================================================================*\
 |  $Id: FallbackMessageDispatcher.java,v 1.3 2011/12/25 02:24:54 stedwar2 Exp $
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
import com.webobjects.foundation.NSDictionary;

//-------------------------------------------------------------------------
/**
 * The fallback message dispatcher is installed at earliest startup by Core so
 * that, in the event of an error initializing the Notifications subsystem,
 * urgent messages will still be e-mailed to the system administrator.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.3 $, $Date: 2011/12/25 02:24:54 $
 */
public class FallbackMessageDispatcher
    implements IMessageDispatcher
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void sendMessage(Message message)
    {
        MessageDescriptor descriptor = message.messageDescriptor();

        String body = buildMessageBody(message);

        // Just e-mail any broadcast messages to the system administrators.

        if (descriptor.isBroadcast())
        {
            Application.sendAdminEmail(message.title(), body);
        }

        // Send the message directly to any users to whom the message applies,
        // if they have notifications for a particular protocol enabled.

        NSArray<User> users = message.users();
        if (users != null)
        {
            for (User user : users)
            {
                // Sanity check to ensure that messages don't get sent to users
                // who shouldn't receive them based on their access level.

                if (user.accessLevel() >= descriptor.accessLevel())
                {
                    Application.sendSimpleEmail(
                            user.email(), message.title(), body,
                            message.attachments());
                }
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Sends this message to the system notification e-mail addresses that are
     * specified in the installation wizard.
     */
    private String buildMessageBody(Message message)
    {
        StringBuffer body = new StringBuffer();
        body.append(message.fullBody());
        body.append("\n\n");

        NSDictionary<String, String> links = message.links();
        if (links != null && links.count() > 0)
        {
            body.append("Links:\n");

            for (String key : links.allKeys())
            {
                String url = links.objectForKey(key);

                body.append(key);
                body.append(": ");
                body.append(url);
                body.append("\n");
            }
        }

        return body.toString();
    }
}
