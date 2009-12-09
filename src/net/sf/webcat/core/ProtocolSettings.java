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

package net.sf.webcat.core;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

// -------------------------------------------------------------------------
/**
 * TODO: place a real description here.
 *
 * @author
 * @author  latest changes by: $Author$
 * @version $Revision$ $Date$
 */
public class ProtocolSettings
    extends _ProtocolSettings
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new ProtocolSettings object.
     */
    public ProtocolSettings()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the system-wide broadcast protocol settings as defined by the
     * system administrator.
     *
     * This object will always have id 1; it is assumed that the Application
     * class has made sure that this object exists during its initialization.
     *
     * @param ec the editing context
     * @return the system-wide broadcast protocol settings, or null if it does
     *     not exist
     */
    public static ProtocolSettings systemSettings(EOEditingContext ec)
    {
        return ProtocolSettings.forId(ec, 1);
    }
}
