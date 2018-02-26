/*==========================================================================*\
 |  Copyright (C) 2018 Virginia Tech
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

package org.webcat.core.lti;

import java.util.UUID;

// -------------------------------------------------------------------------
/**
 * Represents one LTI consumer registered with this LTI tool.
 *
 * @author Stephen Edwards
 */
public class LMSInstance
    extends _LMSInstance
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new LMSInstance object.
     */
    public LMSInstance()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String userPresentableDescription()
    {
        return consumerKey();
    }


    // ----------------------------------------------------------
    @Override
    public void willInsert()
    {
        if (consumerKey() == null || consumerKey().isEmpty())
        {
            setConsumerKey(UUID.randomUUID().toString());
        }
        if (consumerSecret() == null || consumerSecret().isEmpty())
        {
            setConsumerSecret(UUID.randomUUID().toString());
        }
        super.willInsert();
    }


    // ----------------------------------------------------------
    public static String lmsNameFor(LMSInstance instance)
    {
        String name = null;
        if (instance != null)
        {
            name = instance.lmsType().name();
        }
        if (name == null)
        {
            name = "Course Management System";
        }
        return name;
    }
}
