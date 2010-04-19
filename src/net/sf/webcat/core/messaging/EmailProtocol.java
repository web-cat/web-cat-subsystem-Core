/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2009 Virginia Tech
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

import net.sf.webcat.core.Application;
import net.sf.webcat.core.ProtocolSettings;
import net.sf.webcat.core.User;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

//-------------------------------------------------------------------------
/**
 * A notification protocol that delivers messages via e-mail.
 *
 * @author Tony Allevato
 * @version $Id$
 */
public class EmailProtocol extends Protocol
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void sendMessage(Message message, User user,
            ProtocolSettings protocolSettings) throws Exception
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

        Application.sendSimpleEmail(
                user.email(), message.title(), body.toString(),
                message.attachments());
    }


    // ----------------------------------------------------------
    @Override
    public boolean isBroadcast()
    {
        return false;
    }


    // ----------------------------------------------------------
    @Override
    public boolean isEnabledByDefault()
    {
        return true;
    }


    // ----------------------------------------------------------
    @Override
    public String name()
    {
        return "E-mail";
    }
}
