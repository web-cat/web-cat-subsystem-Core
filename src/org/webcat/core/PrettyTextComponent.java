/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2008 Virginia Tech
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

package org.webcat.core;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * A component that displays plain text with line numbers.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class PrettyTextComponent extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new {@code PrettyTextComponent} object.
     *
     * @param context the context
     */
    public PrettyTextComponent(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    /** The text content to display. */
    public String content;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        prettyContent = content;

        if (lineNumbers == null)
        {
            int currentLine = 1;
            StringBuffer lineNumberBuffer = new StringBuffer();
            boolean lastCharWasNewline = true;

            for (int i = 0; i < content.length(); i++)
            {
                if (content.charAt(i) == '\n')
                {
                    lineNumberBuffer.append(currentLine + "\n");
                    currentLine++;
                    lastCharWasNewline = true;
                }
                else
                {
                    lastCharWasNewline = false;
                }
            }

            if (!lastCharWasNewline)
            {
                lineNumberBuffer.append(currentLine + "\n");
            }

            lineNumbers = lineNumberBuffer.toString();
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public String prettyContent()
    {
        return prettyContent;
    }


    // ----------------------------------------------------------
    public String lineNumbers()
    {
        return lineNumbers;
    }


    //~ Static/instance variables .............................................

    private String prettyContent;
    private String lineNumbers;
}
