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

import com.webobjects.foundation.NSArray;
import net.sf.webcat.core.Application;
import net.sf.webcat.core.ProtocolSettings;
import net.sf.webcat.core.User;

//-------------------------------------------------------------------------
/**
 * A notification protocol that delivers messages as mobile SMS messages, using
 * SMS-to-email gateways provided by the various mobile providers.
 *
 * TODO These gateways are currently hard-coded; they should probably be
 * configurable properties with default values for common providers.
 *
 * @author Tony Allevato
 * @version $Id$
 */
public class SMSProtocol extends Protocol
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void sendMessage(Message message, User user,
            ProtocolSettings settings) throws Exception
    {
/*        String mobileNumber = settings.stringSettingForKey(
                MOBILE_NUMBER_SETTING, null);
        int providerIndex = settings.intSettingForKey(
                MOBILE_PROVIDER_SETTING, -1);

        if (mobileNumber != null && providerIndex != -1)
        {
            String gatewayEmail =
                mobileNumber + "@" + gatewayMapping[providerIndex];

            Application.sendAdminEmail(
                    gatewayEmail, null, false,
                    message.title(), message.fullBody(), null);
        }*/
    }


    // ----------------------------------------------------------
    @Override
    public boolean isBroadcast()
    {
        return false;
    }


    // ----------------------------------------------------------
    @Override
    public String name()
    {
        return "Mobile Message";
    }


    //~ Static/instance variables .............................................

    private static final String MOBILE_NUMBER_SETTING =
        "net.sf.webcat.core.messaging.SMSProtocol.mobileNumber";

    private static final String MOBILE_PROVIDER_SETTING =
        "net.sf.webcat.core.messaging.SMSProtocol.mobileProvider";

    private static final String[] gatewayMapping = new String[] {
        "mms.att.net",
        "messaging.sprintpcs.com",
        "tmomail.net",
        "vtext.com"
    };
}
