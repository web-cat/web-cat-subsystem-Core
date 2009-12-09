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

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

//-------------------------------------------------------------------------
/**
 * Describes a message type in the notification system. Objects of this type
 * should not be instantiated directly; they are created by calling the
 * {@link Message#registerMessage(Class, String, String, boolean, int)} method
 * and can be queried with the {@link Message#registeredMessages(boolean)}
 * method.
 *
 * @author Tony Allevato
 * @version $Id$
 */
public class MessageDescriptor implements NSKeyValueCodingAdditions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /* package */ MessageDescriptor(String className,
            String category, String description,
            boolean isBroadcast, int accessLevel)
    {
        this.className = className;
        this.category = category;
        this.description = description;
        this.isBroadcast = isBroadcast;
        this.accessLevel = accessLevel;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String className()
    {
        return className;
    }


    // ----------------------------------------------------------
    public String category()
    {
        return category;
    }


    // ----------------------------------------------------------
    public String description()
    {
        return description;
    }


    // ----------------------------------------------------------
    public boolean isBroadcast()
    {
        return isBroadcast;
    }


    // ----------------------------------------------------------
    public int accessLevel()
    {
        return accessLevel;
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

    private String className;
    private String category;
    private String description;
    private boolean isBroadcast;
    private int accessLevel;
}
