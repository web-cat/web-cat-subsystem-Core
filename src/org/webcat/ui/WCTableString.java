/*==========================================================================*\
 |  $Id: WCTableString.java,v 1.1 2010/10/28 00:37:30 aallowat Exp $
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

package org.webcat.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOString;
import com.webobjects.foundation.NSDictionary;

//-------------------------------------------------------------------------
/**
 * A replacement for WOString inside a WCTable row cell that will automatically
 * highlight the table's current search string inside it.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2010/10/28 00:37:30 $
 */
public class WCTableString extends WOString
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new WCTableString.
     *
     * @param aName the name
     * @param someAssociations the associations
     * @param template the template
     */
    public WCTableString(String aName,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super(aName, someAssociations, template);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        String searchText = WCTable.currentTable().searchText();

        if (searchText == null || searchText.trim().length() == 0)
        {
            super.appendToResponse(response, context);
        }
        else
        {
            searchText = searchText.toLowerCase();
            int length = searchText.length();

            WOResponse fakeResponse = new WOResponse();
            super.appendToResponse(fakeResponse, context);

            String original = fakeResponse.contentString();
            String lower = original.toLowerCase();
            StringBuffer result = new StringBuffer();

            int start = lower.indexOf(searchText);
            int end = 0;

            while (start != -1)
            {
                if (start != end)
                {
                    result.append(original.subSequence(end, start));
                }

                end = start + length;

                result.append("<strong>");
                result.append(original.substring(start, end));
                result.append("</strong>");

                start = lower.indexOf(searchText, end);
            }

            if (end < original.length())
            {
                result.append(original.substring(end));
            }

            response.appendContentString(result.toString());
        }
    }
}
