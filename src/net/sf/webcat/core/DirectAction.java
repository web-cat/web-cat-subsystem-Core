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
import er.extensions.ERXDirectAction;
import er.extensions.ERXValueUtilities;
import net.sf.webcat.core.install.*;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 * The default direct action class for Web-CAT.
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class DirectAction
    extends ERXDirectAction
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new DirectAction object.
     *
     * @param aRequest The request to respond to
     */
    public DirectAction( WORequest aRequest )
    {
        super( aRequest );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * The default action here is used to forward requests to the main
     * login page (without creating a session).  It is used on logout, and
     * also when rejoining an existing session.  Also, note that the login
     * page actually uses this default action with appropriate form values
     * to log a user in.
     *
     * @return The LoginPage, unless login credentials come along with the
     * request, in which case a session is created and the session's current
     * page is returned
     */
    public WOActionResults defaultAction()
    {
        if ( ( (Application)Application.application() ).needsInstallation() )
        {
            return ( new install( request() ) ).defaultAction();
        }
        NSMutableDictionary errors = new NSMutableDictionary();
        NSMutableDictionary extra = request().formValues().mutableClone();
        for ( String key : keysToScreen )
        {
            extra.removeObjectForKey( key );
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "defaultAction(): extra keys = " + extra );
            log.debug( "formValues = " + request().formValues());
        }

        if ( tryLogin( request(), errors ) )
        {
            String pageId = request().stringFormValueForKey( "page" );
            log.debug( "target page = " + pageId );
            WOComponent startPage = null;
            if ( pageId != null )
            {
                TabDescriptor previousPage = session.tabs.selectedDescendant();

                // Try to go to the targeted page, if possible
                if ( session.tabs.selectById( pageId ) != null
                     && session.tabs.selectedDescendant().accessLevel() <=
                         session.user().accessLevel() )
                {
                    log.debug( "found target page, validating ..." );
                    startPage = pageWithName( session.currentPageName() );
                    // Try to configure the targeted page with the given
                    // parameters, if possible
                    if ( ! ( startPage instanceof WCComponent
                         && ( (WCComponent)startPage ).startWith( extra ) ) )
                    {
                        // If we can't jump to this page successfully
                        startPage = null;
                        previousPage.select();
                        log.debug( "target page validation failed" );
                    }
                }
            }
            if ( startPage == null )
            {
                startPage = pageWithName( session.currentPageName() );
            }
            WOActionResults result = startPage.generateResponse();
            return result;
        }
        else
        {
            log.debug( "login failed" );
            LoginPage loginPage = (LoginPage)pageWithName(
                net.sf.webcat.core.LoginPage.class.getName() );
            loginPage.errors   = errors;
            loginPage.userName = request().stringFormValueForKey( "UserName" );
            loginPage.extraKeys = extra;
            if ( domain != null )
            {
                loginPage.domain   = domain;
            }
            return loginPage;
        }
    }


    // ----------------------------------------------------------
    /**
     * Attempt to validate and login the user using a request's form values.
     * This method tries to authenticate the entered username and password.
     * If successful, it checks for an existing session to connect with and
     * logs the user in.  The existing session is left in the private
     * <code>session</code> field.  Leaves the authentication domain object that
     * was used for this attempt in the private <code>domain</code> field.
     *
     * @param request The request containing the form values to inspect
     * @param errors  An empty dictionary which will be filled with any
     *                validation errors to report back to the user on failure
     * @return True on success
     */
    protected boolean tryLogin( WORequest request, NSMutableDictionary errors )
    {
        boolean result = false;
        if ( request.formValues().count() == 0
             || ( request.formValues().count() == 1
                  && request.stringFormValueForKey( "next" ) != null )
             || ( request.formValues().count() == 1
                  && request.stringFormValueForKey( "institution" ) != null )
             || ( request.formValues().count() > 0
                  && request.formValueForKey( "u" ) == null
                  && request.formValueForKey( "UserName" ) == null
                  && request.formValueForKey( "p" ) == null
                  && request.formValueForKey( "UserPassword" ) == null
                  && request.formValueForKey( "AuthenticationDomain" ) == null
                ) )
        {
            return result;
        }

        String userName = request.stringFormValueForKey( "UserName" );
        if ( userName == null )
            userName = request.stringFormValueForKey( "u" );

        String password = request.stringFormValueForKey( "UserPassword" );
        if ( password == null )
            password = request.stringFormValueForKey( "p" );

        Object authIndexObj =
            request().formValueForKey( "AuthenticationDomain" );
        int authIndex = -1;
        String auth = request.stringFormValueForKey( "d" );
        domain = null;

        if ( userName == null )
        {
            errors.setObjectForKey( "Please enter your user name.",
                                    "userName" );
        }
        if ( password == null )
        {
            errors.setObjectForKey( "Please enter your password.",
                                    "password" );
        }
        try
        {
            // This conversion handles null correctly
            authIndex = ERXValueUtilities.intValueWithDefault(
                            authIndexObj, -1 );
        }
        catch ( Exception e )
        {
            // Silently ignore failed conversions, which will be
            // treated as no selection
        }
        // also check for auth == null
        if ( authIndex >= 0 )
        {
            domain = (AuthenticationDomain)AuthenticationDomain
                .authDomains().objectAtIndex( authIndex );
        }
        else if ( auth != null )
        {
            try
            {
                log.debug( "tryLogin(): looking up domain" );
                domain = AuthenticationDomain.authDomainByName( auth );
            }
            catch ( EOObjectNotAvailableException e )
            {
                errors.setObjectForKey(
                    "Illegal institution/affiliation provided ("
                    + e + ").",
                    "authDomain"
                    );
            }
            catch ( EOUtilities.MoreThanOneException e )
            {
                errors.setObjectForKey(
                    "Ambiguous institution/affiliation provided ("
                    + e + ").",
                    "authDomain"
                    );
            }
        }
        else if ( AuthenticationDomain.authDomains().count() == 1 )
        {
            // If there is just one authentication domain, then use it, since
            // no choice will appear on the login page
            domain = (AuthenticationDomain)AuthenticationDomain
                .authDomains().objectAtIndex( 0 );
        }
        else
        {
            errors.setObjectForKey(
                "Please select your institution/affiliation.",
                "authDomain" );
        }

        // The second half of this condition is here just to satisfy the
        // null pointer error detection in Java 6, since we know it can't
        // be null from the error count
        if ( errors.count() == 0 && userName != null )
        {
            userName = userName.toLowerCase();
            EOEditingContext ec = Application.newPeerEditingContext();
            try
            {
            ec.lock();
//            domain = (AuthenticationDomain)EOUtilities.localInstanceOfObject(
//                ec, domain );
            log.debug( "tryLogin(): looking up user" );
            user = User.validate( userName, password, domain, ec );
            if ( user == null )
            {
                log.info( "Failed login attempt: " + userName
                          + " (" + domain.displayableName() + ")" );
                errors.setObjectForKey(
                    "Your login information could not be validated.  "
                    + "Be sure you typed your user name and password "
                    + "correctly, and selected the proper "
                    + "institution/affiliation.",
                    "failedAuthentication" );
            }
            else
            {
                result = true;
                LoginSession ls =
                    LoginSession.getLoginSessionForUser( ec, user );
                if ( ls != null )
                {
                    // Remember the existing session id for restoration
                    wosid = ls.sessionId();
                }
                session = (Session)session();
//                session.setUser(
//                    (User)EOUtilities.localInstanceOfObject(
//                        session.defaultEditingContext(), user ) );
            }
            }
            finally
            {
                ec.unlock();
                Application.releasePeerEditingContext( ec );
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Restores the session associated with this request, if possible.
     */
    protected void restoreSession()
    {
        log.debug( "restoreSession()" );
        String thisWosid = wosid();
        if ( session == null && thisWosid != null )
        {
            if (context().hasSession())
            {
                session = (Session)context().session();
            }
            else
            {
                session = (Session)Application.application()
                    .restoreSessionWithID( thisWosid, context() );
                log.debug( "restoreSession(): session = " + session );
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Saves the session associated with this request, if possible.
     */
    protected void saveSession()
    {
        log.debug( "saveSession()" );
        WOContext context = context();
        if ( session != null && context != null )
        {
            log.debug( "saveSession(): attempting to save session = "
                     + session );
            Application.application().saveSessionForContext( context );
            session = null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns an existing session, if there is one.
     *
     * @return the session if there is one, or null otherwise
     */
    public WOSession existingSession()
    {
        log.debug( "existingSession()" );
        if ( session != null )
        {
            log.debug( "existingSession(): returning one we created" );
            return session;
        }

        restoreSession();
        if ( session != null )
        {
            log.debug( "existingSession(): returning restored session" );
            return session;
        }

        log.debug( "existingSession(): returning super.existingSession()" );
        session = (Session)super.existingSession();
        return session;
    }


    // ----------------------------------------------------------
    /**
     * Returns the session for this transaction.
     *
     * @return the session object
     */
    public WOSession session()
    {
        log.debug( "session()" );
        Session mySession = (Session)existingSession();
        if ( mySession == null )
        {
            log.debug( "session(): calling super.session()" );
            mySession = (Session)context().session();
        }

        if ( mySession == null )
        {
            log.debug( "session(): null session" );
        }
        else if ( !mySession.isLoggedIn() )
        {
            if ( user == null )
            {
                log.debug( "session(): no user available yet" );
            }
            else
            {
                log.debug( "session(): no user associated with session" );
                EOEditingContext ec = mySession.defaultEditingContext();
                ec.lock();
                user = user.localInstance( ec );
                String sessionID = mySession.setUser( user );
                Application.userCount++;
                log.info( "login: "
                          + user.userName()
                          + " ("
                          + user.authenticationDomain().displayableName()
                          + ") (now "
                          + Application.userCount
                          + " users)" );
                if ( !sessionID.equals( mySession.sessionID() ) )
                {
                    log.error( "session(): mismatched session IDs: have "
                               + mySession.sessionID()
                               + " but expected " + sessionID );
                }
                ec.unlock();
                if ( this.session == null )
                {
                    this.session = mySession;
                }
//              log.debug( "session(): session is now " + session );
            }
        }
        if (session == null)
        {
            session = mySession;
        }
        return mySession;
    }


    // ----------------------------------------------------------
    /**
     * Returns the session ID for this request, if there is one.
     *
     * @return the session object
     */
    public String wosid()
    {
        String result = wosid;
        if ( result == null )
        {
            log.debug( "wosid(): attempting to get ID from request" );
            result = request().sessionID();
        }
        log.debug( "wosid() = " + result );
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Dispatch an action.
     * @param actionName The name of the action to dispatch
     * @return the action's result
     */
    public WOActionResults performActionNamed( String actionName )
    {
        log.debug( "performActionNamed( " + actionName + " )" );
        return performActionNamed( actionName, this );
    }


    // ----------------------------------------------------------
    /**
     * Dispatch an action.
     * @param actionName The name of the action to dispatch
     * @param owner      The DirectAction object on which the action
     *                   will be invoked
     * @return the action's result
     */
    public static WOActionResults performActionNamed( String       actionName,
                                                      DirectAction owner )
    {
        log.debug( "performActionNamed( " + actionName + " )" );
        return owner.performSynchronousActionNamed( actionName );
    }


    // ----------------------------------------------------------
    /**
     * Dispatch an action.
     * @param actionName The name of the action to dispatch
     * @return the action's result
     */
    public synchronized WOActionResults performSynchronousActionNamed(
        String actionName )
    {
        log.debug( "performSynchronousActionNamed( " + actionName + " )" );
        WOActionResults result = null;
        try
        {
            result = super.performActionNamed( actionName );
        }
        catch (NSForwardException e)
        {
            if (e.originalException() instanceof
                java.lang.NoSuchMethodException)
            {
                // assume this was a bad request with an invalid action
                // name, and just go to the login page instead.
                result = defaultAction();
            }
            else
            {
                throw e;
            }
        }
        saveSession();
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Attempt to validate a user's password change request code, taken from
     * the form values.  If successful, it checks for an existing session to
     * connect with and logs the user in.  The existing session is left in the
     * private <code>session</code> field.
     *
     * @param request The request containing the form values to inspect
     * @param errors  An empty dictionary which will be filled with any
     *                validation errors to report back to the user on failure
     * @return True on success
     */
    protected boolean tryPasswordReset(
        WORequest request, NSMutableDictionary errors )
    {
        boolean result = false;
        EOEditingContext ec = Application.newPeerEditingContext();
        String code = request().stringFormValueForKey( "code" );
        if ( code == null ) { return result; }
        try
        {
            ec.lock();
            log.debug( "tryPasswordReset(): looking up code" );
            PasswordChangeRequest pcr =
                PasswordChangeRequest.requestForCode( ec, code );
            if ( pcr == null )
            {
                log.info( "Invalid password change code: " + code );
                errors.setObjectForKey(
                    "The password change link you used has expired or is "
                    + "invalid.  You may request another one.",
                    "invalidCode" );
            }
            else
            {
                result = true;
                LoginSession ls =
                    LoginSession.getLoginSessionForUser( ec, pcr.user() );
                if ( ls != null )
                {
                    // Remember the existing session id for restoration
                    wosid = ls.sessionId();
                }
                session = (Session)session();
                session.setUser( pcr.user().localInstance(
                        session.defaultEditingContext() ) );
                pcr.delete();
                ec.saveChanges();
            }
        }
        finally
        {
            ec.unlock();
            Application.releasePeerEditingContext( ec );
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * This action presents a password reset request page (without creating a
     * session).  Also, note that the password reset request page actually
     * uses this action again with appropriate form values to initiate the
     * password change request.
     *
     * @return A PasswordChangeRequestPage, unless valid password change
     * request info comes along with the request, in which case a password
     * change request is registered and the user is e-mailed a password
     * change code.
     */
    public WOActionResults passwordChangeRequestAction()
    {
        NSMutableDictionary errors = new NSMutableDictionary();

        if ( tryPasswordReset( request(), errors ) )
        {
            WCComponent result = (WCComponent)pageWithName(
                session.tabs.selectById( "Profile" ).pageName() );
            result.confirmationMessage( "To change your password, enter a "
                + "new password and confirm it below." );
            return result.generateResponse();
        }
        else
        {
            PasswordChangeRequestPage page =
                (PasswordChangeRequestPage)pageWithName(
                net.sf.webcat.core.PasswordChangeRequestPage.class.getName() );
            // careful: don't clobber any errors that are already there!
            page.errors.addEntriesFromDictionary( errors );
            return page;
        }
    }


    // ----------------------------------------------------------
    /**
     * This action is designed for use by content management systems
     * interacting with Web-CAT, its grades database, and its submission
     * front-end.
     *
     * @return The results page for the submission just made
     */
    public WOActionResults cmsRequestAction()
    {
        // TODO: this entire action should be moved to a separate
        // class in the Grader subsystem.
        log.debug( "entering cmsRequestAction()" );
        log.debug( "hasSession() = " + context().hasSession() );
        Subsystem subsystem = ( (Application)( Application.application() ) )
            .subsystemManager().subsystem( "Grader" );
        WOActionResults result = null;
        result = subsystem.handleDirectAction(
            request(), null /*(Session)session()*/, context() );
        log.debug( "exiting cmsRequestAction()" );
        return result;
    }


    // ----------------------------------------------------------
    /**
     * This action is designed for use with BlueJ's submission
     * extension.
     *
     * @return The results page for the submission just made
     */
    public WOActionResults submitAction()
    {
        // TODO: this entire action should be moved to a separate
        // class in the Grader subsystem.
        NSMutableDictionary errors = new NSMutableDictionary();
        log.debug( "entering submitAction()" );
        log.debug( "hasSession() = " + context().hasSession() );
        WOActionResults result = null;
        if ( tryLogin( request(), errors ) )
        {
            log.debug( "calling subsystem handler" );
            Subsystem subsystem = ( (Application)( Application.application() ) )
                    .subsystemManager().subsystem( "Grader" );
            result = subsystem.handleDirectAction( request(),
                                                   (Session)session(),
                                                   context() );
//          result = pageWithName( "net.sf.webcat.core.SubmitDebug" );
//          ( (SubmitDebug)result ).message =
//              "authentication succeeded";
//          log.debug( "hasSession() = " + context().hasSession() );
//          session.sleep();
        }
        else
        {
            log.debug( "authentication error, aborting submission" );
            SubmitDebug page =
                (SubmitDebug)pageWithName( SubmitDebug.class.getName() );
            page.errors = errors;
            result = page.generateResponse();
        }
        log.debug( "exiting submitAction()" );
//      session = null;
        return result;
    }


    // ----------------------------------------------------------
    /**
     * This action is designed for use with BlueJ's submission
     * extension.
     *
     * @return The results page for the submission just made
     */
    public WOActionResults reportAction()
    {
        // TODO: this entire action should be moved to a separate
        // class in the Grader subsystem.
        log.debug( "entering reportAction()" );
        log.debug( "hasSession() = " + context().hasSession() );
        log.debug( "check 2 = " + request().isSessionIDInRequest() );
//      WOContext context = Application.application()
//          .createContextForRequest( request() );
//      log.debug( "hasSession() = " + context.hasSession() );
//      session = (Session)context.session();
//      session = session();
//      log.debug( "session = " + session );
        WOActionResults result = null;
        Session mySession = (Session)session();
        if ( mySession != null )
        {
            log.debug( "calling subsystem handler" );
            Subsystem subsystem = ( (Application)( Application.application() ) )
                    .subsystemManager().subsystem( "Grader" );
            result = subsystem.handleDirectAction(
                            request(), mySession, context() );
//          result = pageWithName( "net.sf.webcat.core.SubmitDebug" );
//          ( (SubmitDebug)result ).message =
//              "authentication succeeded";
//          log.debug( "hasSession() = " + context().hasSession() );
//          session.sleep();
        }
        else
        {
            log.debug( "No session, so aborting" );
            SubmitDebug page =
                (SubmitDebug)pageWithName( SubmitDebug.class.getName() );
            String msg =
                "Your login session no longer exists.  Try logging in "
                + "through <a href=\""
                + context().urlWithRequestHandlerKey( "wa", "default", null )
                + "\">Web-CAT's main page</a> to view your report.";
            page.errors = new NSDictionary( msg, msg );
            result = page.generateResponse();
        }
        log.debug( "exiting reportAction()" );
        // Omit session saving, since we need to use it in the
        // response generation for this page
        session = null;
        return result;
    }


    // ----------------------------------------------------------
    /**
     * This action hides the default ut direct action class in the WOUnitTest
     * framework for security.  The same ability is provided via the
     * Administer tab instead.
     *
     * @return The results page for the submission just made
     */
    public WOActionResults utAction()
    {
        return defaultAction();
    }


    // ----------------------------------------------------------
    /**
     * This action hides the default uta direct action class in the WOUnitTest
     * framework for security.  The same ability is provided via the
     * Administer tab instead.
     *
     * @return The results page for the submission just made
     */
    public WOActionResults utaAction()
    {
        return defaultAction();
    }


    //~ Instance/static variables .............................................

    private Session              session = null;
    private String               wosid   = null;
    private User                 user    = null;
    private AuthenticationDomain domain  = null;

    private static String[] keysToScreen = new String[] {
        "u",
        "UserName",
        "p",
        "UserPassword",
        "d",
        "institution",
        "AuthenticationDomain"
    };

    static Logger log = Logger.getLogger( DirectAction.class );
}
