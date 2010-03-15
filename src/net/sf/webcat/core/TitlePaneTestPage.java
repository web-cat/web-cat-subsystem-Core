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

import com.webobjects.appserver.WOContext;

public class TitlePaneTestPage extends WCComponent
{
    // ----------------------------------------------------------
    public TitlePaneTestPage(WOContext context)
    {
        super(context);
    }


    // ----------------------------------------------------------
    public String closedTitlePane1Value()
    {
        System.out.println("Calling closedTitlePane1Value");
        new Exception("here").printStackTrace();
        return "Title pane 1";
    }


    // ----------------------------------------------------------
    public String closedTitlePane2Value()
    {
        System.out.println("Calling closedTitlePane2Value");
        return "Title pane 2";
    }
}
