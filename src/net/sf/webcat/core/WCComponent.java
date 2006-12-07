/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License
 |  along with Web-CAT; if not, write to the Free Software
 |  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 |
 |  Project manager: Stephen Edwards <edwards@cs.vt.edu>
 |  Virginia Tech CS Dept, 660 McBryde Hall (0106), Blacksburg, VA 24061 USA
\*==========================================================================*/

package net.sf.webcat.core;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * This class is the root class for all "pages" that are to be nested
 * inside a {@link PageWithNavigation} or {@link WizardPage}.
 *
 * It provides signatures and/or default implementations for the
 * callback operations used by these page wrappers.
 * <p>
 * Typically, a subsytem will create its own custom subclass of
 * <code>WCComponent</code> that provides a subsystem-specific default
 * implementation for {@link #title()} returning a general-purpose title
 * that can be used for the subsystem pages that do not provide their own.
 * Such a subclass can also return subsystem-specific data stored in
 * the session object.
 * </p>
 * <p>
 * The default implementations in this base class will provide
 * will provide unique {@link #helpRelativeURL()} and {@link #feedbackId()}
 * values derived from the page's actual class name.
 * </p>
 * <p>
 * Instead, individual wizard pages will need to override the wizard control
 * button callback functions to give appropriate semantics
 * ({@link #cancel()}, {@link #back()},
 *  {@link #next()}, {@link #apply()}, and {@link #finish()}, together with
 * their related <code>-Enabled</code> functions).
 * </p>
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class WCComponent
    extends WCComponentWithErrorMessages
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new WCComponent object.
     * 
     * @param context The page's context
     */
    public WCComponent( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public WCComponent          nextPage;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Returns the current session object as the application-specific
     * subtype <code>Session</code>.  This avoids the need for downcasting
     * on each <code>session</code> call.
     *
     * @return The current session
     */
    public Session wcSession()
    {
        return (Session)session();
    }


    // ----------------------------------------------------------
    /**
     * Returns the current application object as the application-specific
     * subtype <code>Application</code>.  This avoids the need for
     * downcasting on each <code>application</code> call.
     *
     * @return The current application
     */
    public Application wcApplication()
    {
        return (Application)application();
    }


    // ----------------------------------------------------------
    /**
     * Returns the page's title string.
     *
     * This generic implementation returns null, which will force the
     * use of the default title "Web-CAT", which will be used for pages
     * that do not provide their own title.  Ideally, subsystems will
     * override this default.
     * 
     * @return The page title
     */
    public String title()
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Returns the base URL at which all <code>helpRelativeURL</code>
     * values are rooted.
     *
     * The generic implementation returns the root for all of this
     * installation's Web-CAT documentation.
     * 
     * @return The base URL
     */
    public static String helpBaseURL()
    {
        return Application.configurationProperties().getProperty(
            "help.baseURL", "http://web-cat.cs.vt.edu/Web-CAT.help/" );
    }


    // ----------------------------------------------------------
    /**
     * Returns the URL for this page's help documentation, relative
     * to <code>helpBaseURL</code>.
     *
     * This generic implementation returns the current page's actual
     * class name, with "net.sf.webcat." stripped from the front,
     * with periods transformed to slashes (/), and with ".html" appended
     * on the end.
     * 
     * @return The page URL
     */
    public String helpRelativeURL()
    {
        final String base = "net.sf.webcat.";
        String       url  = this.getClass().getName();

        if ( url.startsWith( base ) )
        {
            url = url.substring( base.length() );
        }
        
        return url.replace( '.', '/' ) + ".php";
    }


    // ----------------------------------------------------------
    /**
     * Returns the URL for this page's  help documentation.
     *
     * The URL is formed by concatenating <code>helpBaseURL</code> and
     * <code>helpRelativeURL</code>.
     * 
     * @return The URL
     */
    public String helpURL()
    {
       // System.out.println("The help url is " + helpBaseURL() + helpRelativeURL());
        return helpBaseURL() + helpRelativeURL();
    }


    // ----------------------------------------------------------
    /**
     * Returns the page's feedback ID for use in feedback e-mail.
     *
     * This generic implementation returns the fully qualified class name
     * of the current page.
     * 
     * @return The feedback ID
     */
    public String feedbackId()
    {
        // TODO:
        return this.getClass().getName();
    }


    // ----------------------------------------------------------
    /**
     * Determines whether the wizard page's "Cancel" button is visible.
     *
     * This generic implementation returns true.  This callback is
     * not used by {@link PageWithNavigation}; it is only meaningful inside
     * a {@link WizardPage} container.
     * 
     * @return True if "Cancel" should appear
     */
    public boolean cancelEnabled()
    {
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Cancels any editing in progress.  Typically called when pressing
     * a cancel button or using a tab to transfer to a different page.
     */
    public void cancelLocalChanges()
    {
        wcSession().cancelLocalChanges();
    }


    // ----------------------------------------------------------
    /**
     * Returns the page to go to when "Cancel" is pressed.
     *
     * This generic implementation moves to the default sibling of the
     * currently selected tab.
     * 
     * This callback is not used by {@link PageWithNavigation}; it is only
     * meaningful inside a {@link WizardPage} container.
     * 
     * @return The page to go to
     */
    public WOComponent cancel()
    {
        clearMessages();
        cancelLocalChanges();
        TabDescriptor parent = wcSession().currentTab().parent();
        if ( parent.parent().parent() != null )
        {
            // If we're on a third-level tab, jump up one level so that
            // we move to the default second-level tab.
            parent = parent.parent();
        }
        return pageWithName( parent.selectDefault().pageName() );
    }


    // ----------------------------------------------------------
    /**
     * Determines whether the wizard page's "Back" button is visible.
     * 
     * This generic implementation looks at the currently selected tab
     * and calls its {@link TabDescriptor#hasPreviousSibling()} method to
     * get the name of the page to create.
     * 
     * This callback is not used by {@link PageWithNavigation}; it is only
     * meaningful inside a {@link WizardPage} container.
     *
     * @return True if "Back" should appear
     */
    public boolean backEnabled()
    {
        return nextPage != null
            || wcSession().currentTab().hasPreviousSibling();
    }


    // ----------------------------------------------------------
    /**
     * Returns the page to go to when "Back" is pressed.
     * 
     * This generic implementation looks at the current tab
     * and calls its {@link TabDescriptor#previousSibling()} method to
     * get the name of the page to create.
     * 
     * This callback is not used by {@link PageWithNavigation}; it is only
     * meaningful inside a {@link WizardPage} container.
     * 
     * @return The page to go to
     */
    public WOComponent back()
    {
        if ( hasMessages() )
        {
            return null;
        }
        if ( nextPage != null )
        {
            return nextPage;
        }
        else
        {
            return pageWithName( wcSession().currentTab().previousSibling()
                .select().pageName() );
        }
    }


    // ----------------------------------------------------------
    /**
     * Determines whether the wizard page's "Next" button is visible.
     * 
     * This generic implementation looks at the currently selected tab
     * and calls its {@link TabDescriptor#hasNextSibling()} method to
     * get the name of the page to create.
     * 
     * This callback is not used by {@link PageWithNavigation}; it is only
     * meaningful inside a {@link WizardPage} container.
     * 
     * @return True if "Next" should appear
     */
    public boolean nextEnabled()
    {
        return !hasBlockingErrors()
            && ( nextPage != null
               || wcSession().currentTab().hasNextSibling() );
    }


    // ----------------------------------------------------------
    /**
     * Returns the page to go to when "Next" is pressed.
     * 
     * This generic implementation looks at the current tab
     * and calls its {@link TabDescriptor#nextSibling()} method to
     * get the name of the page to create.
     * 
     * This callback is not used by {@link PageWithNavigation}; it is only
     * meaningful inside a {@link WizardPage} container.
     * 
     * @return The page to go to
     */
    public WOComponent next()
    {
        if ( hasMessages() )
        {
            return null;
        }
        else if ( nextPage != null )
        {
            return nextPage;
        }
        else
        {
            return pageWithName( wcSession().currentTab().nextSibling()
                                 .select().pageName() );
        }
    }


    // ----------------------------------------------------------
    /**
     * Determines whether the wizard page's "Apply All" button is visible.
     * 
     * This generic implementation returns false, but should be overridden
     * by wizard pages that have recordable settings on them.
     * 
     * This callback is not used by {@link PageWithNavigation}; it is only
     * meaningful inside a {@link WizardPage} container.
     * 
     * @return True if "Apply All" should appear
     */
    public boolean applyEnabled()
    {
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Saves all local changes.  This is the core "save" behavior that
     * is called by both {@link #apply()} and {@link #finish()}.  Override
     * this (and call super) if you need to extend these actions.  This
     * method calls {@link Session#commitLocalChanges()}.
     * @return True if the commit action succeeds, or false if some error
     *     occurred
     */
    public boolean applyLocalChanges()
    {
        if ( hasBlockingErrors() )
        {
            return false;
        }
        try
        {
            // TODO: fix this to handle general save validation failures
            wcSession().commitLocalChanges();
            return true;
        }
        catch ( Exception e )
        {
            ( (Application)application() ).emailExceptionToAdmins(
                e,
                context(),
                "Exception trying to save component's local changes" );
            // forces revert and refaultAllObjects
            wcSession().cancelLocalChanges();
            warning( "An exception occurred while trying to save your "
                + "changes: " + e + ".  As a result, your changes were "
                + "canceled.  Please try again." );
            return false;
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns the page to go to when "Apply All" is pressed.
     * 
     * This generic implementation commits changes but remains on the
     * same page.
     * 
     * This callback is not used by {@link PageWithNavigation}; it is only
     * meaningful inside a {@link WizardPage} container.
     * 
     * @return The page to go to (always null in this default implementation)
     */
    public WOComponent apply()
    {
        applyLocalChanges();
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Determines whether the wizard page's "Finish" button is visible.
     * 
     * This generic implementation returns true.
     * 
     * This callback is not used by {@link PageWithNavigation}; it is only
     * meaningful inside a {@link WizardPage} container.
     * 
     * @return True if "Finish" should appear
     */
    public boolean finishEnabled()
    {
        return !hasBlockingErrors();
    }


    // ----------------------------------------------------------
    /**
     * Returns the page to go to when "Finish" is pressed.
     * 
     * This generic implementation commits changes, then moves to the
     * default sibling page.
     * 
     * This callback is not used by {@link PageWithNavigation}; it is only
     * meaningful inside a {@link WizardPage} container.
     * 
     * @return The page to go to
     */
    public WOComponent finish()
    {
        if ( applyLocalChanges() && !hasMessages() )
        {
            TabDescriptor parent = wcSession().currentTab().parent();
            if ( parent.parent().parent() != null )
            {
                // If we're on a third-level tab, jump up one level so that
                // we move to the default second-level tab.
                parent = parent.parent();
            }
            return pageWithName( parent.selectDefault().pageName() );
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * For wizard pages, implements the default action that takes place
     * when the user presses "Enter" on a wizard page.  This implementation
     * calls next (if enabled) or else applyChanges (if enabled) or simply
     * remains on this page.  Subclasses can override this to
     * provide their own desired behavior.
     * 
     * @return The page to go to
     */
    public WOComponent defaultAction()
    {
        log.debug( "defaultAction()" );
        if ( nextEnabled() )
        {
            return next();
        }
        else if ( applyEnabled() )
        {
            return apply();
        }
        else if ( finishEnabled() )
        {
            return finish();
        }
        else
            return null;
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( WCComponent.class );
}
