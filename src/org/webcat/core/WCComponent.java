/*==========================================================================*\
 |  $Id: WCComponent.java,v 1.5 2013/09/16 13:11:42 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2010 Virginia Tech
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
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import org.webcat.core.Application;
import org.webcat.core.PageWithNavigation;
import org.webcat.core.Session;
import org.webcat.core.TabDescriptor;
import org.webcat.core.User;
import org.webcat.core.WCComponent;
import org.webcat.core.WCComponentWithErrorMessages;
import org.webcat.core.WizardPage;
import org.apache.log4j.Logger;
import org.webcat.core.messaging.UnexpectedExceptionMessage;
import org.webcat.woextensions.WCEC;

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
 * @author  latest changes by: $Author: stedwar2 $
 * @version $Revision: 1.5 $, $Date: 2013/09/16 13:11:42 $
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

    public WCComponent   nextPage;
    public boolean       cancelsForward;
    public boolean       nextPerformsSave;

    public static final String ALL_TASKS = "all";

    //~ Public Methods ........................................................

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
            "help.baseURL", "http://web-cat.org/Web-CAT.help/" );
    }


    // ----------------------------------------------------------
    /**
     * Returns the URL for this page's help documentation, relative
     * to <code>helpBaseURL</code>.
     *
     * This generic implementation returns the current page's actual
     * class name, with "org.webcat." stripped from the front,
     * with periods transformed to slashes (/), and with ".html" appended
     * on the end.
     *
     * @return The page URL
     */
    public String helpRelativeURL()
    {
        final String base = "org.webcat.";
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
        return this.getClass().getName();
    }


    // ----------------------------------------------------------
    /**
     * Get the selected tab that corresponds to this component's page.
     * @return this page's navigation tab
     */
    public TabDescriptor currentTab()
    {
        if (currentTab == null)
        {
            currentTab = wcSession().tabs.selectedDescendant();
        }
        return currentTab;
    }


    // ----------------------------------------------------------
    /**
     * Set the tab that corresponds to this component's page.
     * @param current this page's navigation tab
     */
    public void setCurrentTab(TabDescriptor current)
    {
        currentTab = current;
    }


    // ----------------------------------------------------------
    public void reselectCurrentTab()
    {
        if (currentTab() != null)
        {
            currentTab().select();
        }
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
        TabDescriptor parent = currentTab().parent();
        if ( parent.parent().parent() != null )
        {
            // If we're on a third-level tab, jump up one level so that
            // we move to the default second-level tab.
            parent = parent.parent();
        }
        if (cancelsForward)
        {
            return internalNext(false);
        }
        else
        {
            changeWorkflow();
            return pageWithName( parent.selectDefault().pageName() );
        }
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
        return nextPage != null || currentTab().hasPreviousSibling();
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
        if ( hasBlockingErrors() )
        {
            return null;
        }
        if ( nextPage != null )
        {
            if (breakWorkflow)
            {
                breakWorkflow = false;
            }
            else if (nextPage.peerContextManager == null
                     && peerContextManager != null)
            {
                nextPage.peerContextManager = peerContextManager;
                nextPage.alreadyGrabbed = true;
            }
            return nextPage;
        }
        else
        {
            return pageWithName(
                currentTab().previousSibling().select().pageName() );
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
            && ( nextPage != null || currentTab().hasNextSibling() );
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
        return internalNext(true);
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
     * method calls {@link Session#commitSessionChanges()}.
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
            commitLocalChanges();
            return true;
        }
        catch ( com.webobjects.foundation.NSValidation.ValidationException e )
        {
            cancelLocalChanges();
            warning( e.getMessage() );
            return false;
        }
        catch ( Exception e )
        {
            new UnexpectedExceptionMessage(e, context(), null,
                "Exception trying to save component's local changes" )
                .send();
            // forces revert and refaultAllObjects
            cancelLocalChanges();
            String msg =
                "An exception occurred while trying to save your changes";
            String eMsg = e .getMessage();
            if (eMsg != null && eMsg.length() > 0)
            {
                msg += ": " + eMsg;
            }
            if (!msg.endsWith("."))
            {
                msg += ".";
            }
            warning( msg + "  As a result, your changes were canceled.  "
                + "Please try again." );
            return false;
        }
    }


    // ----------------------------------------------------------
    /**
     * Cancel all local changes and revert to the default editing context
     * state.
     */
    public void cancelLocalChanges()
    {
        if (peerContextManager != null)
        {
            try
            {
                // Make sure to grab the lock, in case this EC hasn't been
                // used for anything yet in this RR-loop and Wonder hasn't
                // auto-locked it yet.
                peerContextManager.editingContext().lock();

                peerContextManager.editingContext().revert();
                peerContextManager.editingContext().refaultAllObjects();
            }
            finally
            {
                peerContextManager.editingContext().unlock();
            }
        }
        else
        {
            wcSession().cancelSessionChanges();
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
        if ( applyLocalChanges() && !hasBlockingErrors() )
        {
            TabDescriptor parent = currentTab().parent();
            if ( parent.parent().parent() != null )
            {
                // If we're on a third-level tab, jump up one level so that
                // we move to the default second-level tab.
                parent = parent.parent();
            }
            changeWorkflow();
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


    // ----------------------------------------------------------
    /**
     * Attempt to set any session-specific local data for this page from
     * the given dictionary so that this page can be rendered successfully.
     * @param params a dictionary of key/value pairs specifying local
     * data for this page
     * @return True if the page can be rendered using the info from params,
     * or false if required parameters are missing.
     */
    public boolean startWith( NSDictionary<String, Object> params )
    {
        return true;
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
    public void awake()
    {
        if (log.isDebugEnabled())
        {
            log.debug("awake(): " + getClass().getName());
        }
        localContext();
        if (currentTab != null)
        {
            reselectCurrentTab();
        }
        super.awake();
        // Force currentTab to be initialized
        currentTab();
    }


    // ----------------------------------------------------------
    @Override
    public void sleep()
    {
        if (peerContextManager != null)
        {
            peerContextManager.sleep();
        }
        super.sleep();
    }


    // ----------------------------------------------------------
    public void willCachePermanently()
    {
        //
    }


    // ----------------------------------------------------------
    /**
     * Access this session's child editing context for storing multi-page
     * changes.
     * @return The child editing context
     */
    public EOEditingContext localContext()
    {
        return peerContextManager().editingContext();

        // To turn off the use of peer editing context and revert to
        // performing all modifications in the single, shared session
        // default editing context, comment out the line above and
        // uncommet this line instead:

        // return wcSession().sessionContext();
    }


    // ----------------------------------------------------------
    /**
     * Returns the current user, or null if one is not logged in.
     * This object lives in the page's child editing context.
     * @return The current user
     */
    public User user()
    {
        if (user == null && wcSession().user() != null)
        {
            user = wcSession().user().localInstance( localContext() );
        }
        return user;
    }


    // ----------------------------------------------------------
    /**
     * Change the local user for the session, to support impersonation of
     * students by administrators and instructors.
     * @param u the new user to impersonate
     */
    public void setLocalUser( User u )
    {
        user = null;
        wcSession().setLocalUser(
            u.localInstance( wcSession().sessionContext() ));
    }


    // ----------------------------------------------------------
    /**
     * This is a typesafe version of the WO {@link #pageWithName(String)}
     * method, and should be used instead of the string version.
     * @param pageClass the class of the page to create
     * @return a new instance of the class, appropriately typed.
     */
    @SuppressWarnings("unchecked")
    public final <T> T pageWithName(Class<T> pageClass)
    {
        reselectCurrentTab();
        return (T)pageWithName(pageClass.getName());
    }


    // ----------------------------------------------------------
    @Override
    /**
     * Where possible, use {@link #pageWithName(Class)} instead.
     */
    public WOComponent pageWithName( String name )
    {
        if (log.isDebugEnabled())
        {
            log.debug("pageWithName(" + name + ") from "
                + getClass().getName());
        }
        String managerKey  = null;
        if (breakWorkflow)
        {
            breakWorkflow = false;
        }
        else if (peerContextManager != null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("storing " + peerContextManager
                    + " on manager tag = "
                    + Thread.currentThread().toString());
            }
            managerKey = Thread.currentThread().toString();
            wcSession().transientState().takeValueForKey(
                peerContextManager, managerKey);
        }
        WOComponent result = super.pageWithName( name );
        if (managerKey != null)
        {
            wcSession().transientState().removeObjectForKey(managerKey);
        }
        return result;
    }


    // ----------------------------------------------------------
    public void changeWorkflow()
    {
        breakWorkflow = true;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve an NSMutableDictionary used to hold transient settings for
     * this page (data that is not database-backed).
     * @return A map of transient settings
     */
    public NSMutableDictionary<String, Object> transientState()
    {
        return peerContextManager().transientState();
    }


    //~ Protected Methods .....................................................

    // ----------------------------------------------------------
    protected String stringValueForKey(
        NSDictionary<String, Object> dict, String key)
    {
        Object value = dict.valueForKey( key );
        if ( value != null && value instanceof NSArray )
        {
            NSArray<?> values = (NSArray<?>)value;
            if ( values.count() == 1 )
            {
                value = values.objectAtIndex( 0 );
            }
        }
        return value == null ? null : value.toString();
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private WOComponent internalNext(boolean save)
    {
        if ( hasBlockingErrors() )
        {
            return null;
        }

        if (save && nextPerformsSave)
        {
            if (!applyLocalChanges())
            {
                return null;
            }
        }

        if ( nextPage != null )
        {
            if (breakWorkflow)
            {
                breakWorkflow = false;
            }
            else if (nextPage.peerContextManager == null
                     && peerContextManager != null)
            {
                nextPage.peerContextManager = peerContextManager;
                nextPage.alreadyGrabbed = true;
            }
            return nextPage;
        }
        else
        {
            try
            {
                return pageWithName(
                    currentTab().nextSibling().select().pageName() );
            }
            catch (NullPointerException e)
            {
                log.error("exception selecting next tab from "
                    + currentTab().printableTabLocationDetails(), e);
                // Assume something is broken w/ the current tab selection
                return pageWithName(
                    wcSession().tabs.selectDefault().pageName());
            }
        }
    }


    // ----------------------------------------------------------
    private WCComponent outermostWCComponent()
    {
        WCComponent result = null;
        WOComponent ancestor = parent();
        while (ancestor != null)
        {
            if (ancestor instanceof WCComponent)
            {
                result = (WCComponent)ancestor;
            }
            ancestor = ancestor.parent();
        }
        return result;
    }


    // ----------------------------------------------------------
    private void grabTaskECManagerIfNecessary()
    {
        if (alreadyGrabbed)
        {
            log.debug("grabTaskECManagerIfNecessary(): "
                + getClass().getName() + "(" + hashCode() + ") "
                + "has already grabbed");
        }
        else
        {
            // First, check to see if the top-level ancestor has one
            WCComponent outer = outermostWCComponent();
            if (outer != null)
            {
                peerContextManager = outer.peerContextManager;
            }

            if (peerContextManager == null)
            {
                String managerKey  = Thread.currentThread().toString();

                // set up nested ec for this task, if there is one
                peerContextManager = (WCEC.PeerManager)wcSession()
                    .transientState().valueForKey(managerKey);
                if (log.isDebugEnabled())
                {
                    log.debug("manager tag = " + managerKey );
                    log.debug("grabTaskECManagerIfNecessary(): "
                        + getClass().getName() + "(" + hashCode() + ") "
                        + "childContextManager = " + peerContextManager);
                }
            }
            alreadyGrabbed = true;
        }
    }


    // ----------------------------------------------------------
    private WCEC.PeerManager peerContextManager()
    {
        if (peerContextManager == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("localContext(): attempting to grab from "
                    + getClass().getName() + "(" + hashCode() + ")");
            }
            grabTaskECManagerIfNecessary();
            if (peerContextManager == null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("localContext(): creating manager for "
                        + getClass().getName() + "(" + hashCode() + ")");
                }
                peerContextManager =
                    wcSession().createManagedPeerEditingContext();
            }
        }
        return peerContextManager;
    }


    // ----------------------------------------------------------
    /**
     * Save all child context changes to the default editing context, then
     * commit them to the database.
     */
    private void commitLocalChanges()
    {
        log.debug( "commitLocalChanges()" );
      if (peerContextManager != null)
      {
          try
          {
              // Make sure to grab the lock, in case this EC hasn't been
              // used for anything yet in this RR-loop and Wonder hasn't
              // auto-locked it yet.
              peerContextManager.editingContext().lock();

              peerContextManager.editingContext().saveChanges();
              peerContextManager.editingContext().revert();
              peerContextManager.editingContext().refaultAllObjects();
          }
          finally
          {
              peerContextManager.editingContext().unlock();
          }
      }
      else
      {
          wcSession().commitSessionChanges();
      }
//        if (childContextManager != null)
//        {
//            childContextManager.editingContext().saveChanges();
//        }
//        wcSession().commitSessionChanges();
//        if (childContextManager != null)
//        {
//            childContextManager.editingContext().revert();
//            childContextManager.editingContext().refaultAllObjects();
//        }
    }


    //~ Instance/static variables .............................................

    private TabDescriptor     currentTab;
    private WCEC.PeerManager  peerContextManager;
    private User              user;
    private boolean           alreadyGrabbed;
    private boolean           breakWorkflow;
    static Logger log = Logger.getLogger( WCComponent.class );
}
