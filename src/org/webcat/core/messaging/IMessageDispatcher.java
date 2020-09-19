/*==========================================================================*\
 |  Copyright (C) 2010-2021 Virginia Tech
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

//-------------------------------------------------------------------------
/**
 * An interface for anything that can send message objects.
 *
 * @author  Tony Allevato
 */
public interface IMessageDispatcher
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Send the given message to its list of users using an appropriate
     * protocol.
     * @param message The message to send.
     */
    void sendMessage(Message message);
}
