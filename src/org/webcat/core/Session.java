/*==========================================================================*\
 |  Copyright (C) 2006-2021 Virginia Tech
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
import org.webcat.core.messaging.UnexpectedExceptionMessage;
import org.webcat.woextensions.EOManager;
import org.webcat.woextensions.ResourceCounter;
import org.webcat.woextensions.WCEC;
import org.webcat.woextensions.WCEC.PeerManager;
import org.webcat.woextensions.WCEC.PeerManagerPool;
import com.webobjects.appserver.WOComponent;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimeZone;

// -------------------------------------------------------------------------
/**
 * The current user session.
 *
 * @author  Stephen Edwards
 */
public class Session
    extends er.extensions.appserver.ERXSession
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Session object.
     */
    public Session()
    {
        super();
        initSession();
    }


    // ----------------------------------------------------------
    /**
     * Creates a new Session object.
     * @param sessionID The ID to use for this session
     */
    public Session(String sessionID)
    {
        super();
        _setSessionID(sessionID);
        initSession();
    }


    // ----------------------------------------------------------
    /**
     * Common initialization helper method used by all constructors.
     */
    private final void initSession()
    {
        tracer.debug("creating " + sessionID());
        createdAt = new Exception("here");
        // , new Exception("from here"));
        counter.allocate(this);

        setStoresIDsInCookies(true);
        setStoresIDsInURLs(false);
        defaultEditingContext().setUndoManager(null);
//        defaultEditingContext().setSharedEditingContext(null);

        tabs.mergeClonedChildren(subsystemTabTemplate);
        tabs.selectDefault();
        childManagerPool = new WCEC.PeerManagerPool();
    }


    // ----------------------------------------------------------
    public Exception createdAt()
    {
        return createdAt;
    }


    //~ KVC Attributes (must be public) .......................................

    public TabDescriptor tabs = new TabDescriptor("TBDPage", "root");


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public WCEC defaultEditingContext()
    {
        EOEditingContext ec = super.defaultEditingContext();
        if (ec != null && !(ec instanceof WCEC))
        {
            throw new IllegalStateException("defaultEditingContext(): "
                + "Expected WCEC but found " + ec.getClass());
        }
        return (WCEC)ec;
    }


    // ----------------------------------------------------------
    /**
     * Determine whether the user is currently logged in.
     *
     * @return True if the user is logged in
     */
    public boolean isLoggedIn()
    {
        return primeUser != null;
    }


    // ----------------------------------------------------------
    /**
     * Set the user's identify when he or she first logs in.
     *
     * Returns the appropriate login <code>Session</code> ID value.
     * Note that this may return a <code>Session</code> other than the
     * recipient of this message, in which case the user has
     * another session open, which they must go to.
     *
     * @param u The user logging in
     * @return  The Session ID to use for this user
     */
    public synchronized void setUser(User u)
    {
        log.debug("setUser(" + u.userName() + ")) for session " + sessionID());
       // tracer , new Exception("from here"));
        if (u.editingContext() != defaultEditingContext())
        {
            u = u.localInstance(defaultEditingContext());
        }
        primeUser = u;
        localUser = u;
        if (isLoginSession && primeUser != null)
        {
            Application.userCount.incrementAndGet();
        }
        if (log.isDebugEnabled())
        {
            log.debug("setUser: userPreferences = "
                + (primeUser == null ? null : primeUser.preferences()));
        }
        Application.wcApplication().subsystemManager()
            .initializeSessionData(this);
        if (!properties().booleanForKey("core.suppressAccessControl"))
        {
            tabs.filterByAccessLevel(u.accessLevel());
        }
        tabs.selectDefault();
        if (isLoginSession)
        {
            UsagePeriod.updateUsagePeriodForUser(primeUser.globalId());
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns the current user, or null if one is not logged in.
     * This object lives in the session's local/child editing context.
     * @return The current user
     */
    public User user()
    {
        return localUser;
    }


    // ----------------------------------------------------------
    /**
     * Returns the current user, or null if one is not logged in.
     * This object lives in the session's local/child editing context.
     * @return The current user
     */
    public User localUser()
    {
        return localUser;
    }


    // ----------------------------------------------------------
    /**
     * Returns the current user, or null if one is not logged in.
     * This object lives in the session's default editing context.
     * @return The current user
     */
    public User primeUser()
    {
        return primeUser;
    }


    // ----------------------------------------------------------
    /**
     * Determine if we are operating as a different user (e.g., impersonating
     * a student).
     * @return True if the localUser is not the primeUser
     */
    public boolean impersonatingAnotherUser()
    {
        return localUser != primeUser;
    }


    // ----------------------------------------------------------
    /**
     * Find out if this session is memo-ized for later login reuse via
     * a LoginSession object.
     * @return True if this session will be shared among all logins
     */
    public boolean useLoginSession()
    {
        return isLoginSession;
    }


    // ----------------------------------------------------------
    /**
     * Set whether this session is memo-ized for later login reuse via
     * a LoginSession object.
     * @param value If true, this session will be shared among all logins
     */
    public void setUseLoginSession(boolean value)
    {
        isLoginSession = value;
        if (primeUser != null)
        {
            Application.userCount.decrementAndGet();
        }
    }


    // ----------------------------------------------------------
    /**
     * Called when request-response loop is done.  Saves the current timeout
     * to the usage period database.
     */
    public void sleep()
    {
        log.debug("sleep()");
        if (primeUser != null)
        {
            UsagePeriod.updateUsagePeriodForUser(primeUser.globalId());
        }
        super.sleep();
    }


    // ----------------------------------------------------------
    /**
     * Returns true is access controls are currently disabled.
     * This is a convenience function that allows the
     * core.suppressAccessControl property to be accessible
     * from the DirectToWeb rule engine.  The engine cannot access
     * it directly through the <code>properties</code> method, since
     * the property has a dot in its name.  Regular web-cat client
     * code should use this expression instead:
     * <code>properties().getBoolean("core.suppressAccessControl")</code>.
     *
     * @return 1 if access controls are being suppressed, 0 otherwise
     */
    public Number suppressAccessControl()
    {
        return properties().booleanForKey("core.suppressAccessControl")
            ? one
            : zero;
    }


    // ----------------------------------------------------------
    /**
     * Returns the name of the page the session is currently viewing.
     *
     * @return The page name
     */
    public String currentPageName()
    {
        return tabs.selectedPageName();
    }


    // ----------------------------------------------------------
    /**
     * Access the application's property settings.  This is a
     * convenience function that allows properties to be accessible
     * from the DirectToWeb rule engine.
     * @return The application's property settings
     */
    public WCProperties properties()
    {
        return Application.configurationProperties();
    }


    // ----------------------------------------------------------
    /**
     * Terminate this session.
     */
    public void terminate()
    {
        if (primeUser != null)
        {
            tracer.info("session timeout, logging out user: "
                + primeUser.userName()
                + ", session: " + sessionID());
            userLogout();
            return;
        }
        if (tracer.isDebugEnabled())
        {
            tracer.debug("terminating session " + sessionID());
                // + " from here:", new Exception("terminate() called"));
        }

        if (terminatedFrom != null)
        {
            log.error("terminate() called on session " + sessionID()
                + " that has already been terminated. Called from:",
                new Exception("here"));
            log.error("first terminated here:", terminatedFrom);
        }
        else
        {
            terminatedFrom = new Exception("terminate() called on session "
                + sessionID());
        }

        if (transientState != null)
        {
            NSArray<Object> values = transientState.allValues();
            for (int i = 0; i < values.count(); i++)
            {
                Object value = values.objectAtIndex(i);
                if (value instanceof EOManager.ECManager)
                {
                    ((EOManager.ECManager)value).dispose();
                }
                else if (value instanceof EOEditingContext)
                {
                    ((EOEditingContext)value).dispose();
                }
                else if (value instanceof WCEC.PeerManager)
                {
                    ((PeerManager)value).dispose();
                }
                else if (value instanceof WCEC.PeerManagerPool)
                {
                    ((PeerManagerPool)value).dispose();
                }
                else
                {
                    log.error("unable to dispose() of "
                        + value.getClass() + ": " + value);
                }
            }
            transientState = null;
        }
        if (childManagerPool != null)
        {
            childManagerPool.dispose();
            childManagerPool = null;
        }

        try
        {
            super.terminate();
        }
        catch (Exception e)
        {
            new UnexpectedExceptionMessage(e, context(), null, null)
            .send();
        }
        counter.deallocate(this);
    }


    // ----------------------------------------------------------
    @Override
    public void savePageInPermanentCache(WOComponent page)
    {
        if (page instanceof WCComponent)
        {
            ((WCComponent)page).willCachePermanently();
        }
        super.savePageInPermanentCache(page);
    }


    // ----------------------------------------------------------
    /**
     * Set the user to null and erase the login session info from
     * the database.
     */
    public synchronized void userLogout()
    {
        if (primeUser != null && isLoginSession)
        {
            UsagePeriod.userDidLogout(primeUser.globalId());
            Application.userCount.decrementAndGet();
            primeUser.userHasLoggedOut();
            tracer.info("user logout: "
                + (primeUser == null ? "null" : primeUser.userName())
                + " (now "
                + Application.userCount
                + " users), in session: " + sessionID());
            // FIXME: what about localUser's prefs?
            // Ignore for now, since that is so rarely used, but it probably
            // causes a very small EC leak
        }
        else
        {
            tracer.info("user logout: "
                + (primeUser == null ? "null" : primeUser.userName())
                + " (not login session, "
                + Application.userCount
                + " users), in session: " + sessionID());
        }
        primeUser = null;
        localUser = null;
        terminate();
    }


    // ----------------------------------------------------------
    /**
     * Access this session's child editing context for storing multi-page
     * changes.
     * @return The child editing context
     */
    public EOEditingContext sessionContext()
    {
        return defaultEditingContext(); // childContext;
    }


    // ----------------------------------------------------------
    /**
     * Create a new child editing context within the session's default
     * context.
     * @return The child editing context, encapsulated in a manager wrapper
     */
    public WCEC.PeerManager createManagedPeerEditingContext()
    {
        return new WCEC.PeerManager(childManagerPool);
    }


    // ----------------------------------------------------------
    /**
     * Save all child context changes to the default editing context, then
     * commit them to the database.
     */
    public void commitSessionChanges()
    {
        log.debug("commitLocalChanges()");
        temporaryTheme = null;
        defaultEditingContext().saveChanges();
        defaultEditingContext().revert();
        defaultEditingContext().refaultAllObjects();
    }


    // ----------------------------------------------------------
    /**
     * Cancel all local changes and revert to the default editing context
     * state.
     */
    public void cancelSessionChanges()
    {
        temporaryTheme = null;
        defaultEditingContext().revert();
        defaultEditingContext().refaultAllObjects();
    }


    // ----------------------------------------------------------
    /**
     * Change the local user, to support impersonation of students by
     * administrators and instructors.
     * @param u the new user to impersonate
     */
    public void setLocalUser(User u)
    {
        localUser = u;
//            (User)EOUtilities.localInstanceOfObject( childContext, u );
//        setCoreSelectionsForLocalUser();
    }


    // ----------------------------------------------------------
    /**
     * Undo the effects of #setLocalUser(User) and revert back to
     * single-user mode.
     */
    public void clearLocalUser()
    {
        localUser = primeUser;
//            (User)EOUtilities.localInstanceOfObject(
//                        childContext, primeUser );
//        setCoreSelectionsForLocalUser();
    }


    // ----------------------------------------------------------
    /**
     * Toggle the student view setting for this user, resetting tabs
     * as appropriate.  This method uses {@link User#toggleStudentView()}
     * to toggle the user state, and then resets the session's tab navigation
     * as appropriate.  It is intended to be called from within a page,
     * such as in {@link PageWithNavigation#toggleStudentView()}.
     */
    public void toggleStudentView()
    {
        user().toggleStudentView();
        if (user().restrictToStudentView())
        {
            TabDescriptor td = tabs;
            while (td != null && td.selectedChild() != null)
            {
                if (td.selectedChild().accessLevel() > 0)
                {
                    td.selectDefault();
                    break;
                }
                td = td.selectedChild();
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Get the user's preferred timestamp formatter.  This method generates
     * and caches a formatter from the user's preferences on first access.
     * Later accesses use the cached value.  If the user's time formatting
     * preferences change, use {@link #clearCachedTimeFormatter()}.
     * @return a formatter
     */
    @SuppressWarnings("deprecation")
    public com.webobjects.foundation.NSTimestampFormatter timeFormatter()
    {
        if (timeFormatter == null)
        {
            String formatString =
                user().dateFormat() + " " + user().timeFormat();
            timeFormatter = new com.webobjects.foundation.NSTimestampFormatter(
                formatString);
            NSTimeZone zone = NSTimeZone.timeZoneWithName(
                user().timeZoneName(), true);
            timeFormatter.setDefaultFormatTimeZone(zone);
            timeFormatter.setDefaultParseTimeZone(zone);
            if (log.isDebugEnabled())
            {
                log.debug("timeFormatter(): format = " + formatString);
                log.debug("timeFormatter(): zone = " + zone);
            }
        }
        return timeFormatter;
    }


    // ----------------------------------------------------------
    /**
     * Get the theme to use for this session.
     * @return The Theme to use
     */
    public Theme theme()
    {
        if (temporaryTheme != null)
        {
            return temporaryTheme;
        }
        return (user() == null || user().theme() == null)
            ? Theme.defaultTheme(defaultEditingContext())
            :  user().theme();
    }


    // ----------------------------------------------------------
    public void setTemporaryTheme(Theme theme)
    {
        temporaryTheme = theme;
    }


    // ----------------------------------------------------------
    /**
     * Clear the cached timestamp formatter in this session so that a fresh
     * one will be created from user preferences the next time one is needed.
     */
    public void clearCachedTimeFormatter()
    {
        log.debug("clearCachedTimeFormatter()");
        timeFormatter = null;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve an NSMutableDictionary used to hold transient settings for
     * this session (data that is not database-backed).
     * @return A map of transient settings
     */
    public NSMutableDictionary<String, Object> transientState()
    {
        if (transientState == null)
        {
            transientState = new NSMutableDictionary<String, Object>();
        }
        return transientState;
    }


    // ----------------------------------------------------------
    public static void dumpLeaks()
    {
        counter.dumpLeaks();
    }


    //~ Instance/static variables .............................................

    private User                  primeUser            = null;
    private User                  localUser            = null;
    private NSMutableDictionary<String, Object> transientState;
    private WCEC.PeerManagerPool  childManagerPool;
    private boolean               isLoginSession = true;
    private Theme                 temporaryTheme;
    private Exception             terminatedFrom;

    @SuppressWarnings("deprecation")
    private com.webobjects.foundation.NSTimestampFormatter timeFormatter = null;

    private static final Integer zero = new Integer(0);
    private static final Integer one  = new Integer(1);

    private static final ResourceCounter counter =
        new ResourceCounter(Session.class.getSimpleName());

    private static NSArray<TabDescriptor> subsystemTabTemplate;
    {
        NSBundle myBundle = NSBundle.bundleForClass(Session.class);
        subsystemTabTemplate = TabDescriptor.tabsFromPropertyList(
            new NSData (myBundle.bytesForResourcePath(
                TabDescriptor.TAB_DEFINITIONS)));
     }

    private Exception createdAt;
    static Logger log = Logger.getLogger(Session.class);
    static Logger tracer =
        Logger.getLogger(Session.class.getName() + ".tracer");
}
