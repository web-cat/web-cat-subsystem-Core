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

import java.net.URL;
import net.sf.webcat.core.ProtocolSettings;
import net.sf.webcat.core.User;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSPropertyListSerialization;

//-------------------------------------------------------------------------
/**
 * The abstract base class for all messaging protocols used in the system.
 * These are instantiated and registered as part of the Application's
 * initialization.
 *
 * Only one instance of each protocol will exist for the lifetime of the
 * server; therefore, no message-specific state should be retained by the
 * instance. Only globally-necessary state, such as logging into an external
 * service, should be stored.
 *
 * @author Tony Allevato
 * @version $Id$
 */
public abstract class Protocol implements NSKeyValueCodingAdditions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new protocol.
     */
    public Protocol()
    {
        // Load the protocol's settings plist file into memory.

        String name = getClass().getCanonicalName();
        NSBundle bundle = NSBundle.bundleForClass(getClass());

        URL url = bundle.pathURLForResourcePath(name + ".settings.plist");
        if (url != null)
        {
            settingsDescriptors =
                NSPropertyListSerialization.dictionaryWithPathURL(url);
        }
        else
        {
            settingsDescriptors = NSDictionary.emptyDictionary();
        }
    }

    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Sends the specified message via this protocol. Subclasses should
     * construct an appropriate message based on parts of the message (title,
     * short body, full body, etc.) respecting constraints of the protocol
     * (such as message length).
     *
     * @param message the message that should be sent
     * @param user if the protocol is a direct messaaging protocol, the user
     *     who will receive the message; if it is a broadcast protocol, this
     *     parameter will be null
     * @param settings the protocol-specific settings to be used when sending
     *     the message
     *
     * @throws Exception if there was an error sending the message
     */
    public abstract void sendMessage(
            Message message,
            User user,
            ProtocolSettings settings)
        throws Exception;


    // ----------------------------------------------------------
    /**
     * Gets the human-readable name of the protocol.
     *
     * @return the human-readable name of the protocol
     */
    public abstract String name();


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the protocol is a broadcast protocol or
     * a direct-to-user protocol.
     *
     * @return true if the protocol broadcasts its messages system-wide; false
     *     if it sends them directly to users
     */
    public abstract boolean isBroadcast();


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether this protocol is enabled by default if
     * the user has not explicitly modified his or her subscription settings
     * for it.
     *
     * @return true if the protocol is enabled by default; otherwise false
     */
    public boolean isEnabledByDefault()
    {
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Gets the array of protocol options that represent per-user settings.
     *
     * @return the per-user protocol options
     */
    public NSArray<NSDictionary<String, Object>> options()
    {
        return (NSArray<NSDictionary<String, Object>>)
            settingsDescriptors.objectForKey("options");
    }


    // ----------------------------------------------------------
    /**
     * Gets the array of protocol options that represent global system
     * settings.
     *
     * @return the system-wide protocol options
     */
    public NSArray<NSDictionary<String, Object>> globalOptions()
    {
        return (NSArray<NSDictionary<String, Object>>)
            settingsDescriptors.objectForKey("globalOptions");
    }


    // ----------------------------------------------------------
    public void takeValueForKey(Object value, String key)
    {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(
                this, value, key);
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }


    // ----------------------------------------------------------
    public void takeValueForKeyPath(Object value, String keyPath)
    {
        NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(
                this, value, keyPath);
    }


    // ----------------------------------------------------------
    public Object valueForKeyPath(String keyPath)
    {
        return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(
                this, keyPath);
    }


    //~ Static/instance variables .............................................

    private NSDictionary<String, Object> settingsDescriptors;
}
