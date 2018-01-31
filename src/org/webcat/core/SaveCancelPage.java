/*==========================================================================*\
 |  $Id: SaveCancelPage.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

package org.webcat.core;

import org.webcat.core.SaveCancelPage;
import org.webcat.core.WCComponent;
import org.webcat.core.WCPageWithNavigation;
import org.webcat.core.WizardPage;
import org.apache.log4j.Logger;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

//-------------------------------------------------------------------------
/**
 * A page wrapper for logged-in users that includes a save button and a
 * cancel button.  This is the new theme-based replacement for the old
 * {@link WizardPage}.
 *
 * @author Stephen Edwards
 * @author Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2010/05/11 14:51:55 $
 */
public class SaveCancelPage
    extends WCPageWithNavigation
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The page's context
     */
    public SaveCancelPage(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public String      cancelPageName;
    public String      nextPageName;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void awake()
    {
        super.awake();
        if (thisPage != null)
        {
            thisPage.cancelsForward = true;
            thisPage.nextPerformsSave = true;
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns the page to go to when "Cancel" is pressed.
     * This value is determined by the cancelPageName property, if set, or
     * the containing page's {@link WCComponent#cancel()} method.
     *
     * @return The page to go to
     */
    public WOComponent cancel()
    {
        log.debug("cancel()");
        if (cancelPageName != null)
            return pageWithName(cancelPageName);
        else if (thisPage != null)
            return thisPage.cancel();
        else
            return null;
    }


    // ----------------------------------------------------------
    /**
     * Returns the page to go to when "Next" is pressed.
     * This value is determined by the nextPageName property, if set, or
     * the containing page's {@link WCComponent#next()} method.
     *
     * @return The page to go to
     */
    public WOComponent next()
    {
        log.debug("next()");
        if (nextPageName != null)
            return pageWithName(nextPageName);
        else if (thisPage != null)
            return thisPage.next();
        else
            return null;
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger(SaveCancelPage.class);
}
