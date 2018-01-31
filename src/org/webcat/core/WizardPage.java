/*==========================================================================*\
 |  $Id: WizardPage.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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
import org.webcat.core.PageWithNavigation;
import org.webcat.core.WCComponent;
import org.webcat.core.WizardPage;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * An extension of {@link PageWithNavigation} that adds wizard-style features.
 *
 * The parent WOComponent should be a WCComponent.  Interaction with
 * this component is primarily through method redefinition of inherited
 * methods from WCComponent.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class WizardPage
    extends PageWithNavigation
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new WizardPage object.
     *
     * @param context The page's context
     */
    public WizardPage( WOContext context )
    {
        super( context );
    }



    //~ KVC Attributes (must be public) .......................................

    public Boolean     applyEnabled;
    public String      applyPageName;
    public Boolean     backEnabled;
    public String      backPageName;
    public Boolean     cancelEnabled;
    public String      cancelPageName;
    public Boolean     nextEnabled;
    public String      nextPageName;
    public Boolean     finishEnabled;
    public String      finishPageName;
    public String      debugKey;
    public boolean     debugState = false;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Cache's a pointer to the parent WCComponent in <code>thisPage</code>
     * after all components have been created.
     */
    public void awake()
    {
        super.awake();
    }


    // ----------------------------------------------------------
    /**
     * Determines whether the wizard page's "Cancel" button is visible.
     * This value is determined by the cancelEnabled property, if set,
     * or the containing page's {@link WCComponent#cancelEnabled()} method.
     *
     * @return True if the button should appear
     */
    public Boolean cancelEnabled()
    {
        if ( cancelEnabled != null )
            return cancelEnabled;
        else if ( thisPage != null )
            return Boolean.valueOf( thisPage.cancelEnabled() );
        else
            return Boolean.TRUE;
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
        log.debug( "cancel()" );
        if ( cancelPageName != null )
            return pageWithName( cancelPageName );
        else if ( thisPage != null )
            return thisPage.cancel();
        else
            return null;
    }


    // ----------------------------------------------------------
    /**
     * Determines whether the wizard page's "Back" button is visible.
     * This value is determined by the backEnabled property, if set, or the
     * containing page's {@link WCComponent#backEnabled()} method.
     *
     * @return True if the button should appear
     */
    public Boolean backEnabled()
    {
        if ( backEnabled != null )
            return backEnabled;
        else if ( thisPage != null )
            return Boolean.valueOf( thisPage.backEnabled() );
        else
            return Boolean.FALSE;
    }


    // ----------------------------------------------------------
    /**
     * Returns the page to go to when "Back" is pressed.
     * This value is determined by the backPageName property, if set, or
     * the containing page's {@link WCComponent#back()} method.
     *
     * @return The page to go to
     */
    public WOComponent back()
    {
        log.debug( "back()" );
        if ( backPageName != null )
            return pageWithName( backPageName );
        else if ( thisPage != null )
            return thisPage.back();
        else
            return null;
    }


    // ----------------------------------------------------------
    /**
     * Determines whether the wizard page's "Next" button is visible.
     * This value is determined by the nextEnabled property, if set, or the
     * containing page's {@link WCComponent#nextEnabled()} method.
     *
     * @return True if the button should appear
     */
    public Boolean nextEnabled()
    {
        if ( nextEnabled != null )
            return nextEnabled;
        else if ( thisPage != null )
            return Boolean.valueOf( thisPage.nextEnabled() );
        else
            return Boolean.FALSE;
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
        log.debug( "next()" );
        if ( nextPageName != null )
            return pageWithName( nextPageName );
        else if ( thisPage != null )
            return thisPage.next();
        else
            return null;
    }


    // ----------------------------------------------------------
    /**
     * Determines whether the wizard page's "Apply All" button is visible.
     * This value is determined by the applyEnabled property, if
     * set, or the containing page's  {@link WCComponent#applyEnabled()}
     * method.
     *
     * @return True if the button should appear
     */
    public Boolean applyEnabled()
    {
        if ( applyEnabled != null )
            return applyEnabled;
        else if ( thisPage != null )
            return Boolean.valueOf( thisPage.applyEnabled() );
        else
            return Boolean.TRUE;
    }


    // ----------------------------------------------------------
    /**
     * Returns the page to go to when "Apply All" is pressed.
     * This value is determined by the containing page's
     * {@link WCComponent#apply()} method.
     *
     * @return The page to go to
     */
    public WOComponent apply()
    {
        log.debug( "apply()" );
        if ( applyPageName != null )
            return pageWithName( applyPageName );
        else if ( thisPage != null )
            return thisPage.apply();
        else
            return null;
    }


    // ----------------------------------------------------------
    /**
     * Determines whether the wizard page's "Finish" button is visible.
     * This value is determined by the finishEnabled property, if
     * set, or the containing page's  {@link WCComponent#finishEnabled()}
     * method.
     *
     * @return True if the button should appear
     */
    public Boolean finishEnabled()
    {
        if ( finishEnabled != null )
            return finishEnabled;
        else if ( thisPage != null )
            return Boolean.valueOf( thisPage.finishEnabled() );
        else
            return Boolean.TRUE;
    }


    // ----------------------------------------------------------
    /**
     * Returns the page to go to when "Finish" is pressed.
     * This value is determined by the containing page's
     * {@link WCComponent#finish()} method.
     *
     * @return The page to go to
     */
    public WOComponent finish()
    {
        log.debug( "finish()" );
        if ( finishPageName != null )
            return pageWithName( finishPageName );
        else if ( thisPage != null )
            return thisPage.finish();
        else
            return null;
    }


    // ----------------------------------------------------------
    /**
     * Returns the page to go to when "Enter" is pressed.
     * This value is determined by the containing page's
     * {@link WCComponent#defaultAction()} method.
     *
     * @return The page to go to
     */
    public WOComponent defaultAction()
    {
        log.debug( "defaultAction()" );
        if ( thisPage != null )
            return thisPage.defaultAction();
        else
            return null;
    }


    // ----------------------------------------------------------
    /**
     * Determines whether sufficient buttons are visible to place an
     * extra gap between the back/next group and the cancel/apply/finish
     * group.
     *
     * @return True if the gap should appear
     */
    public boolean hasExtraGap()
    {
        return ( backEnabled().booleanValue()
               || nextEnabled().booleanValue() )
            && (  cancelEnabled().booleanValue()
               || applyEnabled().booleanValue()
               || finishEnabled().booleanValue() );
    }


    // ----------------------------------------------------------
    /**
     * Returns the page to go to when "Enter" is pressed.
     * This value is determined by the containing page's
     * {@link WCComponent#defaultAction()} method.
     *
     * @return The page to go to
     */
    public String initialFocusTo()
    {
        if ( nextEnabled().booleanValue() )
        {
            return "NextButton";
        }
        else if ( applyEnabled().booleanValue() )
        {
            return "ApplyButton";
        }
        else if ( finishEnabled().booleanValue() )
        {
            return "FinishButton";
        }
        else
        {
            return "DefaultButton";
        }
    }


    // ----------------------------------------------------------
    /**
     * Translates the current <code>debugKey</code> into its corresponding
     * <code>state</code> value.
     *
     * @return The value associated with <code>debugKey</code> in
     *         <code>state</code>
     */
    public String debugKeyValue()
    {
        if ( debugKey == null || thisPage == null ) return "";
        Object val = thisPage.valueForKey( debugKey );
        if ( val == null ) return "";
        return val.toString();
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( WizardPage.class );
}
