/*==========================================================================*\
 |  $Id: IMessageSettings.java,v 1.2 2011/12/25 02:24:54 stedwar2 Exp $
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
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2011/12/25 02:24:54 $
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
