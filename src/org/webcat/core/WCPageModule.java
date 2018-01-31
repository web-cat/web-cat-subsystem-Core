/*==========================================================================*\
 |  $Id: WCPageModule.java,v 1.2 2010/10/13 20:37:19 aallowat Exp $
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

import com.webobjects.appserver.*;
import er.extensions.foundation.ERXValueUtilities;
import org.apache.log4j.Logger;
import org.webcat.ui.util.ComponentIDGenerator;

// -------------------------------------------------------------------------
/**
 * A component that represents a visual "module" displayed on a page,
 * and which will be rendered with the <code>.module</code> CSS class.
 *
 * @author Stephen Edwards
 * @version $Id: WCPageModule.java,v 1.2 2010/10/13 20:37:19 aallowat Exp $
 */
public class WCPageModule
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The page's context
     */
    public WCPageModule(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public ComponentIDGenerator idFor;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse(WOResponse response, WOContext context)
    {
        idFor = new ComponentIDGenerator(this);

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
/*    public boolean isStateless()
    {
        return true;
    }*/


    // ----------------------------------------------------------
    public boolean synchronizesVariablesWithBindings()
    {
        return false;
    }


    // ----------------------------------------------------------
    public boolean hasTitle()
    {
        return hasBinding("title");
    }


    // ----------------------------------------------------------
    public String title()
    {
        return (String)valueForBinding("title");
    }


    // ----------------------------------------------------------
    public String id()
    {
        // Force an id on the panel if the user hasn't included an explicit
        // one, because we need to refer to it in the _onShow/onHide event
        // handlers.

        String idBinding = (String) valueForBinding("id");
        return (idBinding != null) ? idBinding : idFor.get();
    }


    // ----------------------------------------------------------
    public boolean showsLoadingMessageOnRefresh()
    {
        return ERXValueUtilities.booleanValueWithDefault(
                valueForBinding("showsLoadingMessageOnRefresh"), true);
    }


    // ----------------------------------------------------------
    public boolean isCollapsible()
    {
        return ERXValueUtilities.booleanValueWithDefault(
            valueForBinding("collapsible"), true);
    }


    // ----------------------------------------------------------
    private Boolean tryOpenBindingAsExplicitBoolean()
    {
        Object value = valueForBinding("open");

        if (value == null)
        {
            return true;
        }
        else if (value instanceof Number)
        {
            return ((Number) value).intValue() != 0;
        }
        else if (value instanceof Boolean)
        {
            return ((Boolean) value);
        }
        else
        {
            String s = value.toString().trim();

            if (s.equalsIgnoreCase("no")
                    || s.equalsIgnoreCase("false")
                    || s.equalsIgnoreCase("n"))
            {
                return false;
            }
            else if (s.equalsIgnoreCase("yes")
                    || s.equalsIgnoreCase("true")
                    || s.equalsIgnoreCase("y"))
            {
                return true;
            }
            else
            {
                try
                {
                    if (s.length() > 0)
                    {
                        return Integer.parseInt(s) != 0;
                    }
                }
                catch (NumberFormatException e)
                {
                    // Do nothing; returns null below.
                }
            }
        }

        return null;
    }


    // ----------------------------------------------------------
    public boolean startOpen()
    {
        Boolean boolConversion = tryOpenBindingAsExplicitBoolean();
        if (boolConversion != null)
        {
            return boolConversion;
        }
        else
        {
            String keyPath = valueForBinding("open").toString();
            Object value = parent().valueForKeyPath(keyPath);
            return ERXValueUtilities.booleanValueWithDefault(value, true);
        }
    }


    // ----------------------------------------------------------
    public boolean needsVisibilityNotifications()
    {
        return tryOpenBindingAsExplicitBoolean() == null;
    }


    // ----------------------------------------------------------
    public WOActionResults moduleWasClosed()
    {
        String keyPath = valueForBinding("open").toString();
        log.debug("Closing WCPageModule with persistence keypath " + keyPath);

        parent().takeValueForKeyPath(false, keyPath);
        return null;
    }


    // ----------------------------------------------------------
    public WOActionResults moduleWasOpened()
    {
        String keyPath = valueForBinding("open").toString();
        log.debug("Opening WCPageModule with persistence keypath " + keyPath);

        parent().takeValueForKeyPath(true, keyPath);
        return null;
    }


    //~ Static/instance variables .............................................

    private static Logger log = Logger.getLogger(WCPageModule.class);
}
