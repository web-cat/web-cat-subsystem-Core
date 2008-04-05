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

package net.sf.webcat.core;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import er.extensions.ERXMutableDictionary;
import java.util.*;

import net.sf.webcat.core.WOEC.*;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

// -------------------------------------------------------------------------
/**
 * The current user session.
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class Session
    extends er.extensions.ERXSession
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
    public Session( String sessionID )
    {
        super();
        _setSessionID( sessionID );
        initSession();
    }


    // ----------------------------------------------------------
    /**
     * Common initialization helper method used by all constructors.
     */
    private final void initSession()
    {
        log.debug( "creating " + sessionID() );
        defaultEditingContext().setUndoManager( null );
//        defaultEditingContext().setSharedEditingContext( null );

        tabs.mergeClonedChildren( subsystemTabTemplate );
        tabs.selectDefault();
        childManagerPool = new WOEC.PeerManagerPool();
    }


    //~ KVC Attributes (must be public) .......................................

    public TabDescriptor tabs = new TabDescriptor( "TBDPage", "root" );


    //~ Methods ...............................................................

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
     * Returns the appropriate login <code>Session</code> object.
     * Note that this may return a <code>Session</code> other than the
     * recipient of this message, in which case the user has
     * another session open, which they must go to.
     *
     * @param u The user loggin in
     * @return  The Session ID to use for this user
     */
    public String setUser( User u )
    {
        log.debug( "setUser( " + u.userName() + " )" );
        primeUser = u;
        localUser = u;
        log.debug( "setUser: userPreferences = "
            + (primeUser == null
                ? null : primeUser.preferences() ) );
        ( (Application)Application.application() ).subsystemManager().
            initializeSessionData( this );
        if ( ! properties().booleanForKey( "core.suppressAccessControl" ) )
        {
            tabs.filterByAccessLevel( u.accessLevel() );
        }
        tabs.selectDefault();
        EOEditingContext ec = Application.newPeerEditingContext();
        try
        {
            ec.lock();
            loginSession = LoginSession.getLoginSessionForUser( ec, user() );
            if ( loginSession != null )
            {
                NSTimestamp now = new NSTimestamp();
                if ( loginSession.expirationTime().after( now ) )
                {
                    return loginSession.sessionId();
                }
                // otherwise ... fall through to default case
            }
        }
        finally
        {
            ec.unlock();
        }
        if ( loginSession == null )
        {
            Application.releasePeerEditingContext( ec );
        }
        updateLoginSession();
        return this.sessionID();
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
     * Refresh the stored information about the current login session
     * in the database.
     *
     * This updates the stored timeout for this session.
     */
    private void updateLoginSession()
    {
        log.debug( "updateLoginSession()" );
        if ( primeUser == null ) return;
        if ( loginSession == null )
        {
            EOEditingContext ec = Application.newPeerEditingContext();
            try
            {
                ec.lock();
                User loginUser = primeUser.localInstance( ec );
                loginSession =
                    LoginSession.getLoginSessionForUser( ec, loginUser );
                if ( loginSession == null )
                {
                    loginSession = new LoginSession();
                    ec.insertObject( loginSession );
                    loginSession.setSessionId( sessionID() );
                    loginSession.setUserRelationship( loginUser );
                }
            }
            finally
            {
                ec.unlock();
            }
            if ( loginSession == null )
            {
                Application.releasePeerEditingContext( ec );
            }
        }
        try
        {
            loginSession.editingContext().lock();
            loginSession.setExpirationTime(
                ( new NSTimestamp() ).timestampByAddingGregorianUnits(
                    0, 0, 0, 0, 0, (int)timeOut() ) );
            try
            {
                log.debug( "attempting to save" );
                loginSession.editingContext().saveChanges();
                log.debug( "saving complete" );
            }
            catch ( Exception e )
            {
                Application.emailExceptionToAdmins( e, context(), null );
                EOEditingContext ec = loginSession.editingContext();
                loginSession = null;
                ec.revert();
                ec.invalidateAllObjects();
                ec.unlock();
            }
        }
        finally
        {
            if ( loginSession != null && loginSession.editingContext() != null )
                loginSession.editingContext().unlock();
        }
    }


    // ----------------------------------------------------------
    /**
     * Called when request-response loop is done.  Saves the current timeout
     * to the loginsession database.
     */
    public void sleep()
    {
        log.debug( "sleep()" );
        super.sleep();
        updateLoginSession();
        if (  loginSession != null
           && !loginSession.sessionId().equals( sessionID() ) )
        {
            log.error(
                "Error: sleep()'ing with multiple sessions active for user: "
                + ( user() == null ? "<null>" : user().name() )
                );
        }
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
        return properties().booleanForKey( "core.suppressAccessControl" )
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
        if ( log.isDebugEnabled() )
        {
            log.debug( "terminating session " + sessionID() );
            log.debug( "from here:", new Exception( "here" ) );
        }
        if ( primeUser != null )
        {
            log.info( "session timeout: "
                      + ( primeUser == null ? "null" : primeUser.userName() ) );
            userLogout();
        }
        else
        {
            try
            {
                super.terminate();
            }
            catch ( Exception e )
            {
                Application.emailExceptionToAdmins( e, context(), null );
            }
        }
    }


    // ----------------------------------------------------------
    @Override
    public void savePageInPermanentCache( WOComponent page )
    {
        if (page instanceof WCComponent)
        {
            ( (WCComponent)page ).willCachePermanently();
        }
        super.savePageInPermanentCache( page );
    }


    // ----------------------------------------------------------
    /**
     * Set the user to null and erase the login session info from
     * the database.
     */
    public void userLogout()
    {
        Application.userCount--;
        log.info( "user logout: "
                  + ( primeUser == null ? "null" : primeUser.userName() )
                  + " (now "
                  + Application.userCount
                  + " users)" );
        try
        {
            if (  loginSession != null
               && sessionID().equals( loginSession.sessionId() ) )
            {
                try
                {
                    log.debug( "deleting login session "
                             + loginSession.sessionId() );
                    loginSession.editingContext().deleteObject( loginSession );
                    loginSession.editingContext().saveChanges();
                }
                catch ( Exception e )
                {
                    Application.emailExceptionToAdmins( e, context(), null );
                    EOEditingContext ec = Application.newPeerEditingContext();
                    try
                    {
                        ec.lock();
                        User u = primeUser.localInstance( ec );
                        NSArray items = EOUtilities.objectsMatchingKeyAndValue(
                                ec,
                                LoginSession.ENTITY_NAME,
                                LoginSession.USER_KEY,
                                u
                            );
                        if ( items != null && items.count() >= 1 )
                        {
                            LoginSession ls =
                                (LoginSession)items.objectAtIndex( 0 );
                            ec.deleteObject( ls );
                        }
                        try
                        {
                            ec.saveChanges();
                        }
                        catch ( Exception e2 )
                        {
                            Application.emailExceptionToAdmins(
                                e2, context(), null );
                        }
                    }
                    finally
                    {
                        ec.unlock();
                        Application.releasePeerEditingContext( ec );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            Application.emailExceptionToAdmins( e, context(), null );
        }
        primeUser = null;
        localUser = null;
        if (transientState != null)
        {
            NSArray values = transientState.allValues();
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
                else if (value instanceof WOEC.PeerManager)
                {
                    ((PeerManager)value).dispose();
                }
                else if (value instanceof WOEC.PeerManagerPool)
                {
                    ((PeerManagerPool)value).dispose();
                }
            }
            transientState = null;
        }
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
    public WOEC.PeerManager createManagedPeerEditingContext()
    {
        return new WOEC.PeerManager(childManagerPool);
    }


    // ----------------------------------------------------------
    /**
     * Save all child context changes to the default editing context, then
     * commit them to the database.
     */
    public void commitSessionChanges()
    {
        log.debug( "commitLocalChanges()" );
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
        defaultEditingContext().revert();
        defaultEditingContext().refaultAllObjects();
    }


    // ----------------------------------------------------------
    /**
     * Change the local user, to support impersonation of students by
     * administrators and instructors.
     * @param u the new user to impersonate
     */
    public void setLocalUser( User u )
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
        if ( user().restrictToStudentView() )
        {
            TabDescriptor td = tabs;
            while ( td != null && td.selectedChild() != null )
            {
                if ( td.selectedChild().accessLevel() > 0 )
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
    public NSTimestampFormatter timeFormatter()
    {
        if ( timeFormatter == null )
        {
            String formatString =
                user().dateFormat() + " " + user().timeFormat();
            timeFormatter = new NSTimestampFormatter( formatString );
            NSTimeZone zone =
                NSTimeZone.timeZoneWithName( user().timeZoneName(), true );
            timeFormatter.setDefaultFormatTimeZone( zone );
            timeFormatter.setDefaultParseTimeZone( zone );
            if ( log.isDebugEnabled() )
            {
                log.debug( "timeFormatter( ): format = " + formatString );
                log.debug( "timeFormatter( ): zone = " + zone );
            }
        }
        return timeFormatter;
    }


    // ----------------------------------------------------------
    /**
     * Clear the cached timestamp formatter in this session so that a fresh
     * one will be created from user preferences the next time one is needed.
     */
    public void clearCachedTimeFormatter()
    {
        log.debug( "clearCachedTimeFormatter()" );
        timeFormatter = null;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve an NSMutableDictionary used to hold transient settings for
     * this session (data that is not database-backed).
     * @return A map of transient settings
     */
    public NSMutableDictionary transientState()
    {
        if (transientState == null)
        {
            transientState = new NSMutableDictionary();
        }
        return transientState;
    }


    //~ Instance/static variables .............................................

    private User                  primeUser      = null;
    private User                  localUser      = null;
    private LoginSession          loginSession   = null;
    private NSTimestampFormatter  timeFormatter  = null;
    private NSMutableDictionary   transientState;
    private WOEC.PeerManagerPool childManagerPool;

    private static final Integer zero = new Integer( 0 );
    private static final Integer one  = new Integer( 1 );

    private static NSArray subsystemTabTemplate;
    {
        NSBundle myBundle = NSBundle.bundleForClass( Session.class );
        subsystemTabTemplate = TabDescriptor.tabsFromPropertyList(
            new NSData ( myBundle.bytesForResourcePath(
                             TabDescriptor.TAB_DEFINITIONS ) ) );
     }

    static Logger log = Logger.getLogger( Session.class );
}
