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

import java.util.HashMap;
import java.util.Map;
import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;
import er.extensions.foundation.ERXValueUtilities;
import org.webcat.core.Application;
import org.webcat.core.AuthenticationDomain;
import org.webcat.core.DirectAction;
import org.webcat.core.LoginPage;
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
import org.webcat.core.lti.LTIConfiguration;
import org.webcat.core.lti.LTILaunchRequest;
import org.webcat.woextensions.ECActionWithResult;
import org.webcat.woextensions.WCEC;

//-------------------------------------------------------------------------
/**
 * The default direct action class for Web-CAT.
 *
 * @author  Stephen Edwards
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
    public WOActionResults defaultAction()
    {
        if (Application.wcApplication().needsInstallation())
        {
            return (new install(request())).defaultAction();
        }
        if (log.isDebugEnabled())
        {
            log.debug("defaultAction(): uri = " + request().uri());
        }
        NSMutableDictionary<Object, Object> errors =
            new NSMutableDictionary<Object, Object>();
        NSMutableDictionary<String, NSArray<Object>> extra =
            request().formValues().mutableClone();
        for (String key : keysToScreen)
        {
            extra.removeObjectForKey(key);
        }
        if (log.isDebugEnabled())
        {
            log.debug("defaultAction(): extra keys = " + extra);
            log.debug("formValues = " + request().formValues());
        }

        WOResponse result = checkForCas(request());
        if (result != null)
        {
            return result;
        }

        // Look for an existing session via cookie
        if (isLoggedIn()
            // Or, if no existing session, try logging in
            || tryLogin(request(), errors))
        {
            Session session = (Session)session();
            WORequest req = request();
            CasRequestInfo info = peekAtRequest(req);
            if (info != null)
            {
                if (req.stringFormValueForKey(CONTEXT_ID_KEY) != null
                    && info.action != null
                    && !info.action.isEmpty()
                    && !"default".equals(info.action))
                {
                    return casLoginAction();
                }
                info = recallRequest(req);
                req = info.request;
            }

            String pageId = req.stringFormValueForKey("page");
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
            result = startPage.generateResponse();

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
                        domain.get("name"),
                        context().urlWithRequestHandlerKey(null, null, null),
                        null, ONE_YEAR, false));
                result.addCookie(
                    new WOCookie(
                        CONTEXT_ID_KEY,
                        "none",
                        context().urlWithRequestHandlerKey(null, null, null),
                        null, new NSTimestamp(), false));
            }
            if (log.isDebugEnabled())
            {
                log.debug("response cookies = " + result.cookies());
            }

            return result;
        }
        else
        {
            return loginFailed(errors, extra);
        }
    }


    // ----------------------------------------------------------
    private WOActionResults loginFailed(
        NSMutableDictionary<Object, Object> errors,
        NSMutableDictionary<String, NSArray<Object>> extra)
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


    // ----------------------------------------------------------
    protected WOResponse checkForCas(WORequest request)
    {
        log.debug("checkforCas()");
        WOResponse result = null;
        NSDictionary<String, String> d = null;

        String ticket = request.stringFormValueForKey("ticket");
        if (ticket != null)
        {
            log.debug("checkforCas(): ticket found, passing through");
            return result;
        }
        if (isLoggedIn())
        {
            log.debug("checkforCas(): already logged in, passing through");
            return result;
        }

        String contextId = request.stringFormValueForKey(CONTEXT_ID_KEY);
        if (contextId != null)
        {
            log.debug("found contextId = " + contextId);
            CasRequestInfo info = recallRequest(request);
            if (info != null)
            {
                request = info.request;
                log.debug("reloading request, form values = "
                    + request.formValues());
            }
        }

        String auth = request.stringFormValueForKey("AuthenticationDomain");
        if (auth != null)
        {
            log.debug("found AuthenticationDomain form value");
            int authIndex = -1;
            try
            {
                // This conversion handles null correctly
                authIndex = ERXValueUtilities.intValueWithDefault(auth, -1);
            }
            catch (Exception e)
            {
                // Silently ignore failed conversions, which will be
                // treated as no selection
            }
            if (authIndex >= 0)
            {
                d = AuthenticationDomain.authDomainStubs().get(authIndex);
            }
        }
        if (d == null)
        {
            if (auth == null)
            {
                log.debug("checking institution form value");
                auth = request.stringFormValueForKey("institution");
            }
            if (auth == null)
            {
                log.debug("checking d form value");
                auth = request.stringFormValueForKey("d");
            }
            if (auth == null)
            {
                log.debug("checking auth domain cookie");
                auth = request.cookieValueForKey(
                    AuthenticationDomain.COOKIE_LAST_USED_INSTITUTION);
            }
            if (auth != null && !auth.isEmpty())
            {
                log.debug("checkForCas(): looking up domain: " + auth);
                try
                {
                    d = AuthenticationDomain.authDomainStubByName(auth);
                }
                catch (EOObjectNotAvailableException e)
                {
                    log.error("Unrecognized institution parameter provided: '"
                        + auth + "'", e);
                }
                catch (EOUtilities.MoreThanOneException e)
                {
                    log.error("Ambiguous institution parameter provided: '"
                        + auth + "'", e);
                }
            }
            else if (AuthenticationDomain.authDomainStubs().size() == 1)
            {
                d = AuthenticationDomain.defaultDomainStub();
            }
        }

        if (d != null)
        {
            log.debug("checkForCas(): found domain: " + d);
            UserAuthenticator authr = AuthenticationDomain
                .authenticatorForProperty(d.get("propertyName"));
            if (authr instanceof CasAuthenticator)
            {
                log.debug("checkForCas(): creating CAS redirect page");
                CasAuthenticator authenticator = (CasAuthenticator)authr;
                String returnUrl = Application
                    .completeURLWithRequestHandlerKey(context(),
                    Application.application().directActionRequestHandlerKey(),
                    "casLogin", null, true, 0);
                log.debug("returnUrl = " + returnUrl);
                String id = rememberRequest(request, d.get("name"),
                    request.requestHandlerPath());
//                if (request.cookies().size() > 0)
//                {
//                    WORedirect redir= (WORedirect)pageWithName("WORedirect");
//                    redir.setUrl(authenticator.casLoginUrl(returnUrl));
//                    result = redir.generateResponse();
//                }
//                else
                {
                    CasRedirectPage redir =
                        pageWithName(CasRedirectPage.class);
                    redir.contextId = id;
                    redir.loginUrl = authenticator.casLoginUrl(returnUrl);
                    result = redir.generateResponse();
                }
            }
        }

        return result;
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
                && request.formValueForKey("AuthenticationDomain") == null
                && request.formValueForKey("ticket") == null))
        {
            log.debug("tryLogin(): mismatched parameters");
            log.debug("  form values = " + request.formValues());
            log.debug("  ticket = " + request.stringFormValueForKey("ticket"));
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

        String ticket = request.stringFormValueForKey("ticket");
        if (ticket != null)
        {
            log.debug("tryLogin(): ticket found");
            password = ticket;
            userName = Application.completeURLWithRequestHandlerKey(
                context(), "wa", "casLogin", null, true, 0);
            CasRequestInfo info = peekAtRequest(request);
            if (info != null)
            {
                auth = info.auth;
            }
            log.debug("ticket = " + ticket + ", url = " + userName
                + ", auth = " + auth);
        }

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
            domain = AuthenticationDomain.authDomainStubs().get(authIndex);
        }
        else if (auth != null)
        {
//            try
//            {
                log.debug("tryLogin(): looking up domain");
                domain = AuthenticationDomain.authDomainStubByName(auth);
                if (domain == null)
                {
                    errors.setObjectForKey(
                        "Illegal institution/affiliation provided ("
                        + auth + ").",
                        "authDomain");
                }
//            }
//            catch (EOObjectNotAvailableException e)
//            {
//                errors.setObjectForKey(
//                    "Illegal institution/affiliation provided ("
//                    + e + ").",
//                    "authDomain");
//            }
//            catch (EOUtilities.MoreThanOneException e)
//            {
//                errors.setObjectForKey(
//                    "Ambiguous institution/affiliation provided ("
//                    + e + ").",
//                    "authDomain");
//            }
        }
        else if (AuthenticationDomain.authDomainStubs().count() == 1)
        {
            // If there is just one authentication domain, then use it, since
            // no choice will appear on the login page
            domain = AuthenticationDomain.authDomainStubs()
                .objectAtIndex(0);
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
            WCEC ec = WCEC.newEditingContext();
            try
            {
                ec.lock();
                AuthenticationDomain authDomain = AuthenticationDomain
                    .authDomainByPropertyName(
                    ec, domain.get("propertyName"));
                log.debug( "tryLogin(): looking up user" );
                user = User.validate(userName, password, authDomain, ec);
                if (user == null)
                {
                    log.info("Failed login attempt: " + userName
                        + " ("
                        + (authDomain == null
                            ? "null" : authDomain.displayableName())
                        + ")");
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
                    recoverSessionForUser(user);
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
    public Session recoverSessionForUser(User u)
    {
        user = u;
        return (Session)session();
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
        Session session = (Session)super.session();
        if (session != null && !session.isLoggedIn())
        {
            if (user == null)
            {
                log.debug("session(): no user available yet");
            }
            else
            {
                log.debug("session(): no user associated with session");

                if (session.isTerminating())
                {
                    log.error("session(), user = "
                        + (user == null ? "null" : user.userName())
                        + ", called when existing session "
                        + session.sessionID() + "is terminating!!");
                }
                if (session.user() == null)
                {
                    if (user != null)
                    {
                        session.setUser(user);
                    }
                }
                else if (session.primeUser().id().equals(user.id()))
                {
                    //ignore, users match
                }
                else
                {
                    log.error("session(), user = " + user.userName()
                        + ", called for session "
                        + session.sessionID()
                        + "that already has logged in user "
                        + session.primeUser().userName());
                    user = session.primeUser();
                }
            }
        }
        return session;
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
        WCEC ec = WCEC.newEditingContext();
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
                recoverSessionForUser(pcr.user());
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
        if (Application.wcApplication().needsInstallation())
        {
            return defaultAction();
        }

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
    public WOActionResults ltiConfigurationAction()
    {
        if (Application.wcApplication().needsInstallation())
        {
            return defaultAction();
        }
        return pageWithName(LTIConfiguration.class).generateResponse();
    }


    // ----------------------------------------------------------
    public WOActionResults ltiLaunchAction()
    {
        if (Application.wcApplication().needsInstallation())
        {
            return defaultAction();
        }
        log.debug("entering ltiLaunchAction()");
        log.debug("hasSession() = " + context().hasSession());
//        CallbackDiagnosticPage result =
//            pageWithName(CallbackDiagnosticPage.class);
//        result.incomingRequest = request();
        WOActionResults result = new ECActionWithResult<WOActionResults>()
        {
            // ----------------------------------------------------------
            @Override
            public WOActionResults action()
            {
                LTILaunchRequest lti = new LTILaunchRequest(context(), ec);
                if (lti.isValid())
                {
                    Session session = recoverSessionForUser(lti.user());
                    log.debug("ltiLaunchAction(): validation success");
                    WORequest req = request();

                    log.debug("calling subsystem handler");
                    Subsystem subsystem = Application.wcApplication()
                        .subsystemManager().subsystem("Grader");
                    return subsystem.handleDirectAction(
                        req, session, context());
                }
                else
                {
                    log.debug("ltiLaunchAction(): validation failure");
                    SubmitDebug page = pageWithName(SubmitDebug.class);
                    return page.generateResponse();
                }
            }
        }.call();
        log.debug("exiting ltiLaunchAction()");
        return result;
    }


    // ----------------------------------------------------------
    public WOActionResults casLoginAction()
    {
        if (Application.wcApplication().needsInstallation())
        {
            return defaultAction();
        }
        log.debug("entering casLogin()");
        WOActionResults result = null;
        if (!isLoggedIn())
        {
            result = checkForCas(request());
            if (result != null)
            {
                return result;
            }
        }

        CasRequestInfo info = null;
        NSMutableDictionary<Object, Object> errors =
            new NSMutableDictionary<Object, Object>();
        Session session = null;
        // Look for an existing session via cookie
        if (isLoggedIn()
            // Or, if no existing session, try logging in
            || tryLogin(request(), errors))
        {
            session = (Session)session();
            log.debug("casLogin(): now logged in, redirecting to real action");
            info = peekAtRequest(request());
            if (info != null)
            {
                if ("submit".equals(info.action))
                {
                    result = submitAction();
                }
                else if ("report".equals(info.action))
                {
                    result = reportAction();
                }
            }
            if (result == null)
            {
                result = defaultAction();
            }
        }
        else
        {
            result = loginFailed(errors, null);
        }
        WOResponse response = (result instanceof WOResponse)
            ? (WOResponse)result
            : result.generateResponse();
        if (info != null && info.auth != null)
        {
            response.addCookie(
                new WOCookie(
                    AuthenticationDomain.COOKIE_LAST_USED_INSTITUTION,
                    info.auth,
                    context().urlWithRequestHandlerKey(null, null, null),
                    null, ONE_YEAR, false));
        }
        if (session != null)
        {
            session._appendCookieToResponse(response);
        }
        response.addCookie(
            new WOCookie(
                CONTEXT_ID_KEY,
                "none",
                context().urlWithRequestHandlerKey(null, null, null),
                null, new NSTimestamp(), false));
        return response;
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
        if (Application.wcApplication().needsInstallation())
        {
            return defaultAction();
        }
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
        if (Application.wcApplication().needsInstallation())
        {
            return defaultAction();
        }
        log.debug("submitAction()");
        WOActionResults result = checkForCas(request());
        if (result != null)
        {
            return result;
        }

        // TODO: this entire action should be moved to a separate
        // class in the Grader subsystem.
        NSMutableDictionary<?, ?> errors =
            new NSMutableDictionary<Object, Object>();
        log.debug("entering submitAction()");
        log.debug("hasSession() = " + context().hasSession());
        log.debug("submitAction(): attempting login/session check");
        if (isLoggedIn() || tryLogin(request(), errors))
        {
            log.debug("submitAction(): authentication success");
            Session session = (Session)session();
            WORequest req = request();
            CasRequestInfo info = recallRequest(req);
            if (info != null)
            {
                req = info.request;
            }

            log.debug("calling subsystem handler");
            Subsystem subsystem = Application.wcApplication()
                .subsystemManager().subsystem("Grader");
            result = subsystem.handleDirectAction(
                req, session, context(), info == null ? null : info.files);
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
        if (Application.wcApplication().needsInstallation())
        {
            return defaultAction();
        }
        log.debug("reportAction()");
        WOActionResults result = checkForCas(request());
        if (result != null)
        {
            return result;
        }

        // TODO: this entire action should be moved to a separate
        // class in the Grader subsystem.
        log.debug("entering reportAction()");
        log.debug("hasSession() = " + context().hasSession());
        log.debug("check 2 = " + request().isSessionIDInRequest());
        NSMutableDictionary<?, ?> errors =
            new NSMutableDictionary<Object, Object>();
        if (isLoggedIn() || tryLogin(request(), errors))
        {
            log.debug("reportAction(): authentication success");
            Session session = (Session)session();
            WORequest req = request();
            CasRequestInfo info = recallRequest(req);
            if (info != null)
            {
                req = info.request;
            }

            log.debug("calling subsystem handler");
            Subsystem subsystem = Application.wcApplication()
                .subsystemManager().subsystem("Grader");
            result = subsystem.handleDirectAction(
                req, session, context());
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
            if (errors.count() == 0)
            {
                errors.takeValueForKey(msg, msg);
            }
            page.errors = errors;
            result = page.generateResponse();
        }
        log.debug("exiting reportAction()");
        // forgetSession();
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
            return !Application.wcApplication().needsInstallation();
        }
        return super.actionShouldWaitForInitialization(actionName);
    }


    // ----------------------------------------------------------
    private String rememberRequest(
        WORequest request, String auth, String action)
    {
        String id = java.util.UUID.randomUUID().toString();
//        rememberRequest(id, request, auth, action);
        synchronized (casRequests)
        {
            // First, remove stale entries
            NSTimestamp now = new NSTimestamp();
            if (now.after(nextCasSweep))
            {
                // 5 minutes ago
                NSTimestamp cutoff =
                    now.timestampByAddingGregorianUnits(0, 0, 0, 0, -5, 0);
                if (casRequests.size() > 0)
                {
                    log.info("Sweeping for stale CAS entries, current count = "
                    + casRequests.size());
                }
                int staleCount = 0;
                long staleSize = 0;
                NSMutableArray<String> keys = new NSMutableArray<String>();
                for (Map.Entry<String, CasRequestInfo> entry :
                    casRequests.entrySet())
                {
                    if (entry.getValue().time.before(cutoff))
                    {
                        staleCount++;
                        NSData f = entry.getValue().files.get("file1");
                        if (f != null)
                        {
                            staleSize += f.length();
                        }
                        keys.add(entry.getKey());
                    }
                }
                for (String key : keys)
                {
                    casRequests.remove(key);
                }
                if (staleCount > 0)
                {
                    log.info("Purged " + staleCount + " entries, "
                        + staleSize + " bytes");
                }
                // 5 minutes later ...
                nextCasSweep =
                    now.timestampByAddingGregorianUnits(0, 0, 0, 0, 5, 0);
            }
            casRequests.put(id, new CasRequestInfo(request, auth, action));
        }
        log.debug("rememberRequest(" + id + ") with action = " + action);
        return id;
    }


    // ----------------------------------------------------------
//    private void rememberRequest(
//        String id, WORequest request, String auth, String action)
//    {
//        log.debug("rememberRequest(" + id + ")");
//        synchronized (casRequests)
//        {
//            casRequests.put(id, new CasRequestInfo(request, auth, action));
//        }
//    }


    // ----------------------------------------------------------
    private String casRequestInfoKey(WORequest request)
    {
        String id = request.stringFormValueForKey(CONTEXT_ID_KEY);
        if (id == null)
        {
            id = request.cookieValueForKey(CONTEXT_ID_KEY);
        }
        log.debug("casRequestInfoKey() = " + id);
        return id;
    }


    // ----------------------------------------------------------
    private CasRequestInfo peekAtRequest(WORequest req)
    {
        CasRequestInfo request = null;
        String id = casRequestInfoKey(req);
        log.debug("peekAtRequest(" + id + ")");
        if (id != null)
        {
            synchronized (casRequests)
            {
                request = casRequests.get(id);
            }
        }
        log.debug("peekAtRequest() = " + request);
        return request;
    }


    // ----------------------------------------------------------
    private CasRequestInfo recallRequest(WORequest req)
    {
        CasRequestInfo request = null;
        String id = casRequestInfoKey(req);
        log.debug("recallRequest(" + id + ")");
        if (id != null)
        {
            synchronized (casRequests)
            {
                log.debug("recallRequest() cache = " + casRequests.size());
                request = casRequests.get(id);
                log.debug("recallRequest() check = " + request);
                request = casRequests.remove(id);
            }
        }
        log.debug("recallRequest() = " + request);
        return request;
    }


    // ----------------------------------------------------------
    private boolean isLoggedIn()
    {
        Session existingSession = (Session)existingSession();
        return existingSession != null
            && existingSession.isLoggedIn()
            && !existingSession.isTerminating();
    }


    //~ Instance/static variables .............................................

    private User                 user    = null;
//    private AuthenticationDomain domain  = null;
    private NSDictionary<String, String> domain  = null;

    private static final String[] keysToScreen = new String[] {
        "u",
        "UserName",
        "p",
        "UserPassword",
        "d",
        "institution",
        "AuthenticationDomain",
        "ticket"
    };
    // One year, in seconds
    public static final int ONE_YEAR = 60 * 60 * 24 * 365;
    public static final int TEN_MINUTES = 60 * 10;

    private static class CasRequestInfo
    {
        public WORequest request;
        public String auth;
        public String action;
        public NSMutableDictionary<String, NSData> files;
        public NSTimestamp time;
        public CasRequestInfo(WORequest request, String auth, String action)
        {
            this.request = request;
            this.auth = auth;
            this.action = action;
            files = new NSMutableDictionary<String, NSData>();
            int count = 1;
            Object blob = request.formValueForKey("file" + count);
            time = new NSTimestamp();
            while (blob != null)
            {
                if (blob instanceof NSData)
                {
                    NSData fileData = new NSData(((NSData)blob).bytes());
                    files.takeValueForKey(fileData, "file" + count);
                }
                count++;
                blob = request.formValueForKey("file" + count);
            }
        }
    }

    private static final
        Map<String, CasRequestInfo> casRequests =
        new HashMap<String, CasRequestInfo>();
    private static NSTimestamp nextCasSweep = new NSTimestamp();

    public static final String CONTEXT_ID_KEY = "owc_c";

    static Logger log = Logger.getLogger(DirectAction.class);
}
