/*==========================================================================*\
 |  $Id: WCPageWithNavigation.java,v 1.3 2011/09/16 16:11:19 stedwar2 Exp $
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

package org.webcat.core;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import org.apache.log4j.Logger;
import org.webcat.ui.WCBasePage;
import org.webcat.core.Application;
import org.webcat.core.FeedbackPage;
import org.webcat.core.Session;
import org.webcat.core.TabDescriptor;
import org.webcat.core.User;
import org.webcat.core.WCComponent;
import org.webcat.core.WCPageWithNavigation;

// -------------------------------------------------------------------------
/**
 * A page wrapper for logged-in users that includes the standard header,
 * tab-based navigation features, and footer.  It inherits from
 * BarePage (which is also uses), mostly to inherit all the same KVC
 * keys, which it passes on to its BarePage container.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.3 $, $Date: 2011/09/16 16:11:19 $
 */
public class WCPageWithNavigation
    extends WCBasePage
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new SimplePage object.
     *
     * @param context The page's context
     */
    public WCPageWithNavigation( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public TabDescriptor selectedRole;
    public TabDescriptor secondLevelSelection;
    public int           tertiaryTabIndex;
    public WCComponent   thisPage;
    public String        sideStepTitle;
    public boolean       hideSteps = false;
    public String        permalink;

    // Repetition variables
    public TabDescriptor primaryTabItem;
    public TabDescriptor secondaryTabItem;
    public TabDescriptor tertiaryTabItem;
    public TabDescriptor aRole;
    public int           aRoleIndex;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Set up this component for this response.
     * @see com.webobjects.appserver.WOComponent#awake()
     */
    public void awake()
    {
        log.debug( "awake()" );
        super.awake();
        if ( thisPage == null )
        {
            WOComponent comp = context().page();
            // initialize thisPage if needed
            if ( comp instanceof WCComponent )
            {
                thisPage = (WCComponent)comp;
            }
            else
            {
                thisPage = null; // Will probably force a dirty crash
                if ( log.isDebugEnabled() )
                {
                    log.debug(
                        "top-level component "
                        + ( ( comp == null )
                            ? "<null>"
                            : comp.getClass().getName() )
                        + " is not a WCComponent"
                    );
                }
            }
        }
        if ( myTab == null )
        {
            if (thisPage != null)
            {
                myTab = thisPage.currentTab();
                TabDescriptor tabs = ( (Session)session() ).tabs;
                selectedRole = tabs.selectedChild();
                secondLevelSelection =
                    selectedRole.selectedChild().selectedChild();
            }
            if (selectedRole == null)
            {
                selectedRole = ( (Session)session() ).tabs.selectedChild();
            }
            if (myTab == null)
            {
                myTab = selectedRole.selectedDescendant();
                secondLevelSelection =
                    selectedRole.selectedChild().selectedChild();
            }
        }
        if ( title == null && thisPage != null )
        {
            title = thisPage.title();
        }
        if ( title == null )
        {
            if (secondLevelSelection == null)
            {
                title = myTab.label();
            }
            else
            {
                title = secondLevelSelection.label();
            }
        }
        if (log.isDebugEnabled())
        {
            log.debug("my tab = " + myTab);
            log.debug("my role = " + selectedRole);
        }
    }


    // ----------------------------------------------------------
    /**
     * Log's the user out of the current session.
     * @return A redirect to the main login page, after terminating this
     *         session
     */
    public WOComponent logout()
    {
        if ( ( (Session)session() ).user() != null )
        {
            log.info( "user "
                      + ( (Session)session() ).user().userName()
                      + " logging out" );
            ( (Session)session() ).userLogout();
        }
        return Application.wcApplication().gotoLoginPage( context() );
    }


    // ----------------------------------------------------------
    /**
     * Determine whether this page has a set title.
     *
     * @return True if there is a title
     */
    public boolean hasTitle()
    {
        return ( title != null );
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the URL for this page's help information.
     * This value is extracted from this page's WCComponent.
     *
     * @return The desired URL
     */
    public String helpURL()
    {
        if ( helpURL == null )
        {
            if ( thisPage != null )
            {
                helpURL = thisPage.helpURL();
            }
            else
            {
                return WCComponent.helpBaseURL();
            }
            boolean hasQ = false;
            if ( secondLevelSelection != null )
            {
                helpURL += "?t1=" + secondLevelSelection.parent().parent()
                                    .selectedChildIndex()
                        + "&t2="
                        + secondLevelSelection.parent().selectedChildIndex();
                hasQ = true;
                if ( hasSteps() )
                {
                    helpURL += "&t3="
                            + secondLevelSelection.selectedChildIndex();
                }
            }
            User user = ( (Session) session()).user();
            if ( user != null )
            {
                if ( !hasQ ) { helpURL += "?"; hasQ = true; }
                else         { helpURL += "&"; }
                helpURL += "ua=" + user.accessLevel();
            }
        }
        return helpURL;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve a sentence-case version of the page's side-step title,
     * where only the first letter is capitalized.
     *
     * @return The title in sentence case
     */
    public String lcSideStepTitle()
    {
        return TabDescriptor.lowerCaseAfterFirst( sideStepTitle );
    }


    // ----------------------------------------------------------
    /**
     * Go to the feedback form, and record the necessary information about
     * this page as the originating source.
     *
     * @return The feedback form page
     */
    public WOComponent goToFeedback()
    {
        FeedbackPage feedbackPage = (FeedbackPage)pageWithName(
            ( (Session)session() ).tabs.selectById( "Feedback" ).pageName() );
        feedbackPage.pageTitle = title;
        feedbackPage.extraInfo = Application.extraInfoForContext( context() );
        return feedbackPage;
    }


    // ----------------------------------------------------------
    /**
     * Go to the user's profile page.
     *
     * @return The profile page
     */
    public WOComponent goToProfile()
    {
        return pageWithName(
            ( (Session)session() ).tabs.selectById( "Profile" ).pageName() );
    }


    // ----------------------------------------------------------
    /**
     * Determine if the current primary tab can be seen by this user
     * (enforce tab hiding when user has toggled to student-only view).
     * @return True if this primary tab can be seen
     */
    public boolean primaryTabIsVisible()
    {
        return primaryTabItem.accessLevel() == 0
            || (hasSession()
                && ((Session)session()).user() != null
                && !((Session)session()).user().restrictToStudentView());
    }


    // ----------------------------------------------------------
    /**
     * Determine if the current secondary tab can be seen by this user
     * (enforce tab hiding when user has toggled to student-only view).
     * @return True if this secondary tab can be seen
     */
    public boolean secondaryTabIsVisible()
    {
        return secondaryTabItem.accessLevel() == 0
        || (hasSession()
            && ((Session)session()).user() != null
            && !((Session)session()).user().restrictToStudentView());
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the CSS class to use for the list item for a secondary tab.
     * @return The CSS class
     */
    public String secondaryTabClass()
    {
        if ( secondaryTabItem.isSelected() )
            return "here2";
        else
            return secondaryTabItem.parent().cssClass();
    }


    // ----------------------------------------------------------
    /**
     * Follow the link for a primary tab.
     * @return the new component
     */
    public WOComponent primaryTabLink()
    {
        if (thisPage != null)
        {
            thisPage.changeWorkflow();
        }
        return pageWithName( primaryTabItem.selectDefault().pageName() );
    }


    // ----------------------------------------------------------
    /**
     * Follow the link for a primary tab.
     * @return the new component
     */
    public WOComponent switchRole()
    {
        if (thisPage != null)
        {
            thisPage.changeWorkflow();
        }
        return pageWithName(
            aRole.selectDefault().selectedDescendant().pageName() );
    }


    // ----------------------------------------------------------
    /**
     * Follow the link for a secondary tab.
     * @return the new component
     */
    public WOComponent secondaryTabLink()
    {
        if (thisPage != null)
        {
            thisPage.changeWorkflow();
        }
        return pageWithName( secondaryTabItem.selectDefault().pageName() );
    }


    // ----------------------------------------------------------
    /**
     * Follow the link for a tertiary tab (a wizard step).
     * @return the new component
     */
    public WOComponent stepLink()
    {
        return pageWithName( tertiaryTabItem.selectDefault().pageName() );
    }


    // ----------------------------------------------------------
    /**
     * Check whether there is a title for a diversionary "side step".
     * @return True if we are on a detour in wizard page sequencing
     */
    public boolean isSideStep()
    {
        return sideStepTitle != null  &&  stepIsSelected();
    }


    // ----------------------------------------------------------
    /**
     * Determing whether the "Step" menu for third-level tabs should be
     * displayed on this page.
     * @return True if the step menu should be shown
     */
    public boolean hasSteps()
    {
        return !hideSteps && secondLevelSelection.children().count() > 0;
    }


    // ----------------------------------------------------------
    /**
     * Check whether a third-level tab (wizard step) requires a hyperlink.
     * @return True if the third-level tab comes before the currently
     *         selected third level tab
     */
    public boolean stepUsesLink()
    {
        return tertiaryTabIndex < secondLevelSelection.selectedChildIndex()
            || ( tertiaryTabIndex == secondLevelSelection.selectedChildIndex()
                 && isSideStep() );
    }


    // ----------------------------------------------------------
    /**
     * Returns true for the currently-selected tertiary tab (a wizard step).
     * @return True if this wizard step is selected
     */
    public boolean stepIsSelected()
    {
        return tertiaryTabIndex == secondLevelSelection.selectedChildIndex();
    }


    // ----------------------------------------------------------
    /**
     * Generate the string representation of the third-level tab number.
     * @return the step number
     */
    public String tertiaryNumeral()
    {
        return "" + ( tertiaryTabIndex + 1 );
    }


    // ----------------------------------------------------------
    public void pushValuesToParent()
    {
        // Make sure to handle logout actions on form pages correctly
        // by blocking value pushing if the session is terminating
        if ( hasSession() && !session().isTerminating() )
        {
            super.pushValuesToParent();
        }
    }


    // ----------------------------------------------------------
    public boolean hasVisibleSecondaryTabs()
    {
        int count = 0;
        Session session = (Session)session();
        if ( session.user() == null || session.user().restrictToStudentView() )
        {
            NSArray<TabDescriptor> secondaries = primaryTabItem.children();
            for (TabDescriptor secondary : secondaries)
            {
                if (secondary.accessLevel() == 0)
                {
                    count++;
                    if (count > 1)
                    {
                        break;
                    }
                }
            }
        }
        else
        {
            count = primaryTabItem.children().count();
        }
        return count > 1;
    }


    // ----------------------------------------------------------
    /**
     * Toggle the student view setting for this user.
     * @return Returns null, to force reloading of the calling page
     * (if desired)
     */
    public WOComponent toggleStudentView()
    {
        ( (Session)session() ).toggleStudentView();
        return pageWithName(
            ( (Session)session() ).tabs.selectedDescendant().pageName() );
    }


    // ----------------------------------------------------------
    @Override
    public WOComponent pageWithName( String name )
    {
        if (log.isDebugEnabled())
        {
            log.debug("pageWithName(" + name + ")");
        }
        return (thisPage == null)
            ? super.pageWithName(name)
            : thisPage.pageWithName(name);
    }


    //~ Instance/static variables .............................................

    private TabDescriptor myTab;
    private String        helpURL;
    static Logger log = Logger.getLogger(WCPageWithNavigation.class);
}
