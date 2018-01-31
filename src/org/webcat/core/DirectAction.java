/*==========================================================================*\
 |  $Id: DirectAction.java,v 1.8 2012/02/13 02:53:52 stedwar2 Exp $
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
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import er.extensions.foundation.ERXValueUtilities;
import org.webcat.core.Application;
import org.webcat.core.AuthenticationDomain;
import org.webcat.core.DirectAction;
import org.webcat.core.LoginPage;
import org.webcat.core.LoginSession;
import org.webcat.core.PasswordChangeRequest;
import org.webcat.core.PasswordChangeRequestPage;
import org.webcat.core.Session;
import org.webcat.core.SubmitDebug;
import org.webcat.core.Subsystem;
import org.webcat.core.TabDescriptor;
import org.webcat.core.User;
import org.webcat.core.WCComponent;
import org.apache.log4j.Logger;
import org.webcat.core.actions.WCDirectActionWithSession;
import org.webcat.core.install.*;
import org.webcat.woextensions.WCEC;

//-------------------------------------------------------------------------
/**
 * The default direct action class for Web-CAT.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.8 $, $Date: 2012/02/13 02:53:52 $
 */
public class DirectAction
    extends WCDirectActionWithSession
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new DirectAction object.
     *
     * @param aRequest The request to respond to
     */
    public DirectAction(WORequest aRequest)
    {
        super(aRequest);
        if (log.isDebugEnabled())
        {
            log.debug("DirectAction.<init>: " + hashCode());
        }
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
    @SuppressWarnings("unchecked")
    public WOActionResults defaultAction()
    {
        if (Application.wcApplication().needsInstallation())
        {
            return (new install(request())).defaultAction();
        }
        NSMutableDictionary<?, ?> errors =
            new NSMutableDictionary<Object, Object>();
        NSMutableDictionary extra = request().formValues().mutableClone();
        for (String key : keysToScreen)
        {
            extra.removeObjectForKey(key);
        }
        if (log.isDebugEnabled())
        {
            log.debug("defaultAction(): extra keys = " + extra);
            log.debug("formValues = " + request().formValues());
        }

        // Look for an existing session via cookie
        Session existingSession = (Session)existingSession();
        if ((existingSession != null
            && existingSession.isLoggedIn()
            && !existingSession.isTerminating())

            // Or, if no existing session, try logging in
            || tryLogin(request(), errors))
        {
            Session session = (Session)session();
            String pageId = request().stringFormValueForKey("page");
            log.debug("target page = " + pageId);
            WOComponent startPage = null;
            if (pageId != null)
            {
                TabDescriptor previousPage = session.tabs.selectedDescendant();

                // Try to go to the targeted page, if possible
                if (session.tabs.selectById(pageId) != null
                     && session.tabs.selectedDescendant().accessLevel() <=
                         session.user().accessLevel())
                {
                    log.debug("found target page, validating ...");
                    startPage = pageWithName(session.currentPageName());
                    // Try to configure the targeted page with the given
                    // parameters, if possible
                    if (!(startPage instanceof WCComponent
                         && ((WCComponent)startPage).startWith(extra)))
                    {
                        // If we can't jump to this page successfully
                        startPage = null;
                        previousPage.select();
                        log.debug("target page validation failed");
                    }
                }
            }
            if (startPage == null)
            {
                startPage = pageWithName(session.currentPageName());
            }
            WOResponse result = startPage.generateResponse();

            // Update the current theme in the cookie when the user logs in,
            // so that it is always the most recent theme in situations where
            // multiple users are using the same browser/client.
            if (session.user().theme() != null)
            {
                session.user().theme().setAsLastUsedThemeInContext(context());
            }

            // Store selected authentication domain in cookie
            if (domain != null)
            {
                result.addCookie(
                    new WOCookie(
                        AuthenticationDomain.COOKIE_LAST_USED_INSTITUTION,
                        domain.propertyName()
                            .substring("authenticator.".length()),
                        context().urlWithRequestHandlerKey(null, null, null),
                        null, ONE_YEAR, false));
            }
            if (log.isDebugEnabled())
            {
                log.debug("response cookies = " + result.cookies());
            }

            return result;
        }
        else
        {
            log.debug("login failed");
            LoginPage loginPage = pageWithName(org.webcat.core.LoginPage.class);
            loginPage.errors   = errors;
            loginPage.userName = request().stringFormValueForKey("UserName");
            loginPage.extraKeys = extra;
            if (domain != null)
            {
                loginPage.domain = domain;
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
    protected boolean tryLogin(
        WORequest request, NSMutableDictionary<?, ?> errors)
    {
        boolean result = false;
        if (request.formValues().count() == 0
            || (request.formValues().count() == 1
                 && request.stringFormValueForKey("next") != null)
            || (request.formValues().count() == 1
                && request.stringFormValueForKey("institution") != null)
            || (request.formValues().count() > 0
                && request.formValueForKey("u") == null
                && request.formValueForKey("UserName") == null
                && request.formValueForKey("p") == null
                && request.formValueForKey("UserPassword") == null
                && request.formValueForKey("AuthenticationDomain") == null))
        {
            return result;
        }

        String userName = request.stringFormValueForKey("UserName");
        if (userName == null)
        {
            userName = request.stringFormValueForKey("u");
        }

        String password = request.stringFormValueForKey("UserPassword");
        if (password == null)
        {
            password = request.stringFormValueForKey("p");
        }

        Object authIndexObj =
            request().formValueForKey("AuthenticationDomain");
        int authIndex = -1;
        String auth = request.stringFormValueForKey("d");
        domain = null;

        if (userName == null)
        {
            errors.setObjectForKey(
                "Please enter your user name.", "userName");
        }
        if (password == null)
        {
            errors.setObjectForKey(
                "Please enter your password.", "password");
        }
        try
        {
            // This conversion handles null correctly
            authIndex = ERXValueUtilities.intValueWithDefault(
                            authIndexObj, -1);
        }
        catch (Exception e)
        {
            // Silently ignore failed conversions, which will be
            // treated as no selection
        }
        // also check for auth == null
        if (authIndex >= 0)
        {
            domain = AuthenticationDomain.authDomains().get(authIndex);
        }
        else if (auth != null)
        {
            try
            {
                log.debug("tryLogin(): looking up domain");
                domain = AuthenticationDomain.authDomainByName(auth);
            }
            catch (EOObjectNotAvailableException e)
            {
                errors.setObjectForKey(
                    "Illegal institution/affiliation provided ("
                    + e + ").",
                    "authDomain");
            }
            catch (EOUtilities.MoreThanOneException e)
            {
                errors.setObjectForKey(
                    "Ambiguous institution/affiliation provided ("
                    + e + ").",
                    "authDomain");
            }
        }
        else if (AuthenticationDomain.authDomains().count() == 1)
        {
            // If there is just one authentication domain, then use it, since
            // no choice will appear on the login page
            domain = AuthenticationDomain.authDomains().objectAtIndex(0);
        }
        else
        {
            if (userName != null && !userName.contains("@"))
            // Try to go through and look up by e-mail address
            errors.setObjectForKey(
                "Please specify your e-mail address, or select your "
                + "institution/affiliation.",
                "authDomain");
        }

        // The second half of this condition is here just to satisfy the
        // null pointer error detection in Java 6, since we know it can't
        // be null from the error count
        if (errors.count() == 0 && userName != null)
        {
            EOEditingContext ec = WCEC.newEditingContext();
            try
            {
                ec.lock();
                log.debug( "tryLogin(): looking up user" );
                user = User.validate(userName, password, domain, ec);
                if (user == null)
                {
                    log.info("Failed login attempt: " + userName
                        + " (" + domain.displayableName() + ")");
                    errors.setObjectForKey(
                        "Your login information could not be validated.  "
                        + "Be sure you typed your e-mail (or user name) "
                        + "and password "
                        + "correctly, and selected the proper "
                        + "institution/affiliation.",
                        "failedAuthentication");
                }
                else
                {
                    result = true;
                    LoginSession ls =
                        LoginSession.getLoginSessionForUser(ec, user);
                    if (ls != null)
                    {
                        // Remember the existing session id for restoration
                        rememberWosid(ls.sessionId());
                    }
                    session();
                }
            }
            finally
            {
                ec.unlock();
                ec.dispose();
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Returns the session for this transaction.
     *
     * @return the session object
     */
    @Override
    public WOSession session()
    {
        log.debug("session()");
        Session mySession = (Session)super.session();
        if (mySession != null && !mySession.isLoggedIn())
        {
            if (user == null)
            {
                log.debug("session(): no user available yet");
            }
            else
            {
                log.debug("session(): no user associated with session");
                EOEditingContext ec = mySession.defaultEditingContext();
                try
                {
                    ec.lock();
                    user = user.localInstance(ec);
                    String sessionID = mySession.setUser(user);
                    log.info("login: "
                        + user.userName()
                        + " ("
                        + user.authenticationDomain().displayableName()
                        + ") (now "
                        + Application.userCount
                        + " users)");
                    if (!sessionID.equals(mySession.sessionID()))
                    {
                        log.error("session(): mismatched session IDs: have "
                            + mySession.sessionID()
                            + " but expected " + sessionID);
                    }
                }
                finally
                {
                    ec.unlock();
                }
            }
        }
        return mySession;
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
        WORequest request, NSMutableDictionary<?, ?> errors)
    {
        boolean result = false;
        EOEditingContext ec = WCEC.newEditingContext();
        String code = request().stringFormValueForKey("code");
        if (code == null)
        {
            return result;
        }
        try
        {
            ec.lock();
            log.debug("tryPasswordReset(): looking up code");
            PasswordChangeRequest pcr =
                PasswordChangeRequest.requestForCode(ec, code);
            if (pcr == null)
            {
                log.info("Invalid password change code: " + code);
                errors.setObjectForKey(
                    "The password change link you used has expired or is "
                    + "invalid.  You may request another one.",
                    "invalidCode");
            }
            else
            {
                result = true;
                LoginSession ls =
                    LoginSession.getLoginSessionForUser(ec, pcr.user());
                if (ls != null)
                {
                    // Remember the existing session id for restoration
                    rememberWosid(ls.sessionId());
                }
                Session session = (Session)session();
                session.setUser(pcr.user().localInstance(
                        session.defaultEditingContext()));
                pcr.delete();
                try
                {
                    ec.saveChanges();
                }
                catch (Exception e)
                {
                    log.error("Unable to delete password change request "
                        + pcr + " for user " + pcr.user(), e);
                }
            }
        }
        finally
        {
            ec.unlock();
            ec.dispose();
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
        NSMutableDictionary<?, ?> errors =
            new NSMutableDictionary<Object, Object>();

        if (tryPasswordReset(request(), errors))
        {
            WCComponent result = (WCComponent)pageWithName(
                ((Session)session()).tabs.selectById("Profile").pageName());
            result.confirmationMessage("To change your password, enter a "
                + "new password and confirm it below.");
            return result.generateResponse();
        }
        else
        {
            PasswordChangeRequestPage page = pageWithName(
                org.webcat.core.PasswordChangeRequestPage.class);
            // careful: don't clobber any errors that are already there!
            page.errors.addEntriesFromDictionary(errors);
            return page;
        }
    }


    // ----------------------------------------------------------
    public WOActionResults casCallbackAction()
    {
        log.debug("entering casCallbackAction()");
        log.debug("hasSession() = " + context().hasSession());
        CallbackDiagnosticPage result =
            pageWithName(CallbackDiagnosticPage.class);
        result.incomingRequest = request();
        return result;
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
        log.debug("entering cmsRequestAction()");
        log.debug("hasSession() = " + context().hasSession());
        Subsystem subsystem = Application.wcApplication().subsystemManager()
            .subsystem("Grader");
        WOActionResults result = null;
        result = subsystem.handleDirectAction(
            request(), null /*(Session)session()*/, context());
        log.debug("exiting cmsRequestAction()");
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
        NSMutableDictionary<?, ?> errors =
            new NSMutableDictionary<Object, Object>();
        log.debug("entering submitAction()");
        log.debug("hasSession() = " + context().hasSession());
        WOActionResults result = null;
        if (tryLogin(request(), errors))
        {
            log.debug("calling subsystem handler");
            Subsystem subsystem = Application.wcApplication()
                .subsystemManager().subsystem("Grader");
            result = subsystem.handleDirectAction(
                request(), (Session)session(), context());
        }
        else
        {
            log.debug("authentication error, aborting submission");
            SubmitDebug page = pageWithName(SubmitDebug.class);
            page.errors = errors;
            result = page.generateResponse();
        }
        log.debug("exiting submitAction()");
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
        log.debug("entering reportAction()");
        log.debug("hasSession() = " + context().hasSession());
        log.debug("check 2 = " + request().isSessionIDInRequest());
        WOActionResults result = null;
        Session mySession = (Session)session();
        if (mySession != null)
        {
            log.debug("calling subsystem handler");
            Subsystem subsystem = Application.wcApplication()
                .subsystemManager().subsystem("Grader");
            result = subsystem.handleDirectAction(
                request(), mySession, context());
        }
        else
        {
            log.debug("No session, so aborting");
            SubmitDebug page = pageWithName(SubmitDebug.class);
            String msg =
                "Your login session no longer exists.  Try logging in "
                + "through <a href=\""
                + context().urlWithRequestHandlerKey("wa", "default", null)
                + "\">Web-CAT's main page</a> to view your report.";
            page.errors = new NSDictionary<Object, Object>(msg, msg);
            result = page.generateResponse();
        }
        log.debug("exiting reportAction()");
        forgetSession();
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


    // ----------------------------------------------------------
    @Override
    protected boolean actionShouldWaitForInitialization(String actionName)
    {
        if ("default".equals(actionName))
        {
            return false;
        }
        return super.actionShouldWaitForInitialization(actionName);
    }


    //~ Instance/static variables .............................................

    private User                 user    = null;
    private AuthenticationDomain domain  = null;

    private static final String[] keysToScreen = new String[] {
        "u",
        "UserName",
        "p",
        "UserPassword",
        "d",
        "institution",
        "AuthenticationDomain"
    };
    // One year, in seconds
    private static final int ONE_YEAR = 60 * 60 * 24 * 365;

    static Logger log = Logger.getLogger(DirectAction.class);
}
