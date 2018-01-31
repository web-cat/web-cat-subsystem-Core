/*==========================================================================*\
 |  $Id: ConfirmingActionTestPage.java,v 1.5 2010/11/01 17:04:05 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2010 Virginia Tech
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

import org.apache.log4j.Logger;
import org.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

//-------------------------------------------------------------------------
/**
 * A test page to show how to use confirming actions--buttons with
 * confirmation pop-up dialogs.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.5 $, $Date: 2010/11/01 17:04:05 $
 */
public class ConfirmingActionTestPage
    extends WCComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public ConfirmingActionTestPage(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public boolean checkBoxChecked;
    public String textBoxValue;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public int autoIncrementingInteger()
    {
        return autoIncrementingInteger++;
    }


    // ----------------------------------------------------------
    public WOActionResults dummyAction()
    {
        log.info("Executing dummy action");
        return null;
    }


    // ----------------------------------------------------------
    public WOActionResults processThings()
    {
        return new ConfirmingAction(this, false)
        {
            // ----------------------------------------------------------
            @Override
            protected String confirmationMessage()
            {
                return "Confirming the values <b>" + checkBoxChecked
                    + "</b> and <b>" + textBoxValue + "</b>?";
            }

            // ----------------------------------------------------------
            @Override
            protected WOActionResults actionWasConfirmed()
            {
                log.info("Yes button was clicked; executing action as a "
                        + "page-load");
                return null;
            }
        };
    }


    // ----------------------------------------------------------
    public WOActionResults processThingsRemotely()
    {
        return new ConfirmingAction(this, true)
        {
            // ----------------------------------------------------------
            @Override
            protected String confirmationMessage()
            {
                return "Confirming the values <b>" + checkBoxChecked
                    + "</b> and <b>" + textBoxValue + "</b>?";
            }

            // ----------------------------------------------------------
            @Override
            protected WOActionResults actionWasConfirmed()
            {
                log.info("Yes button was clicked; executing action as a "
                        + "remote action");
                return new JavascriptGenerator().refresh("contentPane");
            }
        };
    }


    //~ Static/instance variables .............................................

    private int autoIncrementingInteger;

    private static Logger log = Logger.getLogger(
            ConfirmingActionTestPage.class);
}
