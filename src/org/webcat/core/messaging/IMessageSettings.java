package org.webcat.core.messaging;

import org.webcat.core.MutableDictionary;

//-------------------------------------------------------------------------
/**
 * An interface that provides settings describing how a particular message
 * should be sent. Typically the system default settings are sufficient, but
 * individual message types may wish to override certain settings, such as
 * notifications about a course being posted to a course-specific Twitter feed
 * instead of the main feed.
 *
 * This interface is implemented by the {@link ProtocolSettings} class in the
 * Notifications subsystem, and most operations dealing with settings will be
 * provided by those objects. This interface exists to completely decouple the
 * messages from the protocol machinery in Notifications, and also to offer the
 * option of providing options from a source other than ProtocolSettings EOs.
 *
 * @author Tony Allevato
 * @version $Id$
 */
public interface IMessageSettings
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets a snapshot of the settings used to send a message.
     *
     * @return a dictionary containing the snapshot of the settings
     */
    MutableDictionary settingsSnapshot();


    // ----------------------------------------------------------
    Object settingForKey(String key);


    // ----------------------------------------------------------
    String stringSettingForKey(String key, String defaultValue);


    // ----------------------------------------------------------
    boolean booleanSettingForKey(String key, boolean defaultValue);


    // ----------------------------------------------------------
    int intSettingForKey(String key, int defaultValue);


    // ----------------------------------------------------------
    double doubleSettingForKey(String key, double defaultValue);
}
