/*==========================================================================*\
 |  Copyright (C) 2006-2018 Virginia Tech
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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import ognl.helperfunction.WOHelperFunctionHTMLTemplateParser;
import ognl.helperfunction.WOTagProcessor;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.webcat.archives.ArchiveManager;
import org.webcat.archives.IArchiveHandler;
import org.webcat.core.git.GitUtilities;
import org.webcat.core.git.http.GitRequestHandler;
import org.webcat.core.messaging.ApplicationStartupMessage;
import org.webcat.core.messaging.FallbackMessageDispatcher;
import org.webcat.core.messaging.IMessageDispatcher;
import org.webcat.core.messaging.UnexpectedExceptionMessage;
import org.webcat.core.webapi.WebAPIRequestHandler;
import org.webcat.core.webdav.WebDAVRequestHandler;
import org.webcat.dbupdate.UpdateEngine;
import org.webcat.woextensions.AjaxUpdateContainerTagProcessor;
import org.webcat.woextensions.ECAction;
import org.webcat.woextensions.WCContext;
import org.webcat.woextensions.WCEC;
import org.webcat.woextensions.WCResourceManager;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WOPageNotFoundException;
import com.webobjects.appserver.WORedirect;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.appserver.WOSessionStore;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.woextensions.WOExceptionParser;
import com.webobjects.woextensions.WOParsedErrorLine;
import er.extensions.ERXExtensions;
import er.extensions.appserver.ERXMessageEncoding;
import er.extensions.formatters.ERXTimestampFormatter;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXSystem;
import er.extensions.foundation.ERXValueUtilities;

// -------------------------------------------------------------------------
/**
 * This is the main Web-CAT application class.  It takes care of all loading
 * and setup issues.  It also serves as a central source for accessing
 * properties and subsystem objects, and provides for centralized handling
 * of exception handling for the Web-CAT application.
 *
 * @author  Stephen Edwards
 */
public class Application
    extends er.extensions.appserver.ERXApplication
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Application object.  Also does the following:
     * <ul>
     * <li> Sets the <code>EOModelGroup</code> delegate so that
     *      EOModels stored in jarred subsystems can be loaded. </li>
     * <li> Registers a callback with the notification center for
     *      creating new database channels. </li>
     * <li> Clears out any stale <code>WCLoginSession</code> objects
     *      remaining in the database. </li>
     * </ul>
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    public Application()
    {
        super();

        try
        {
        // Set UTF-8 encoding, to support localization
        WOMessage.setDefaultEncoding("UTF-8");
        WOMessage.setDefaultURLEncoding("UTF-8");
        ERXMessageEncoding.setDefaultEncoding("UTF-8");
        ERXMessageEncoding.setDefaultEncodingForAllLanguages("UTF-8");

        // We'll use plain WO sessions, even in a servlet context, since
        // restoring sessions through the WCServletSessionStore doesn't
        // really work.
        setSessionStoreClassName("WOServerSessionStore");

        // I'm not sure these do anything, since the corresponding
        // notifications should have occurred before this constructor
        // ever executes :-(.  I put them in to replicate features in
        // ERXApplication.main(), which isn't executed for servlets, but
        // that is broken.
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            new NSSelector("logNotification",
                           new Class[] { NSNotification.class }),
            NSBundle.BundleDidLoadNotification,
            null);
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            new NSSelector("logNotification",
                           new Class[] { NSNotification.class }),
            NSBundle.LoadedClassesNotification,
            null);

        if (log.isInfoEnabled())
        {
            log.info("Web-CAT v" + version()
                + "\nCopyright (C) 2006-2018 Virginia Tech\n\n"
                + "Web-CAT comes with ABSOLUTELY NO WARRANTY; this is "
                + "free software\n"
                + "under the terms of the GNU Affero General Public License "
                + "v3.  See:\n"
                + "http://www.gnu.org/licenses/agpl.html\n"
                + "For full source code, see:\n"
                + "http://www.sourceforge.net/projects/web-cat\n");
        }
        if (log.isDebugEnabled())
        {
            log.debug("Properties loaded from:");
            NSArray<?> dirs = ERXProperties.pathsForUserAndBundleProperties();
            for (Object dir : dirs)
            {
                log.info("\t" + dir);
            }
            dirs = ERXProperties.optionalConfigurationFiles();
            if (dirs == null)
            {
                log.debug("no optional configuration files specified.");
            }
            else
            {
                log.debug("Also loading properties from optional files:");
                for (Object dir : dirs)
                {
                    log.info("\t" + dir);
                }
            }

            try
            {
                File here = new File(".");
                log.debug("current dir = " + here.getCanonicalPath());
            }
            catch (java.io.IOException e)
            {
                log.error("exception checking cwd: ", e);
            }
        }

        // Present the first page via the default DirectAction.
        setDefaultRequestHandler(
            requestHandlerForKey(directActionRequestHandlerKey()));

        // Register the Git request handler.
        apiHandler = new WebAPIRequestHandler();
        registerRequestHandler(apiHandler,
                WebAPIRequestHandler.REQUEST_HANDLER_KEY);

        // Register the entity resource request handler.
        registerRequestHandler(new EntityResourceRequestHandler(),
            EntityResourceRequestHandler.REQUEST_HANDLER_KEY);

        // Register the Git request handler.
        registerRequestHandler(new GitRequestHandler(),
                GitRequestHandler.REQUEST_HANDLER_KEY);

        // Register the WebDAV request handler.
        registerRequestHandler(new WebDAVRequestHandler(),
                WebDAVRequestHandler.REQUEST_HANDLER_KEY);

        // Set page cache size from property
        setPageCacheSize(ERXValueUtilities.intValueWithDefault(
            configurationProperties().valueForKey("WOPageCacheSize"), 30));

        WCEC.installWOECFactory();

        if (!isDevelopmentModeSafe() && isDirectConnectEnabled())
        {
            setDirectConnectEnabled(false);
        }

        if (log.isDebugEnabled())
        {
            log.debug("classpath = " + System.getProperty("java.class.path"));
        }

        if (configurationProperties().hasUsableConfiguration())
        {
            log.debug("initializing application");
            initializeApplication();
            setNeedsInstallation(false);
            notifyAdminsOfStartup();
        }
        else
        {
            updateStaticHtmlResources();
        }

        }
        catch (Exception e)
        {
            log.fatal(
                "An unexpected exception during application initialization "
                + "kept Web-CAT from starting properly.",
                e);
            if (e instanceof RuntimeException)
            {
                throw (RuntimeException)e;
            }
            else
            {
                throw new RuntimeException(e);
            }
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * The main startup method for the application.
     *
     * @param argv The command line arguments
     */
    public static void main(String argv[])
    {
        er.extensions.appserver.ERXApplication.main(argv, Application.class);
    }


    // ----------------------------------------------------------
    /**
     * Pause the calling thread until the constructor for the singleton
     * instance of this class has been created.
     */
    public static void waitForConstructorToComplete()
    {
        // Just ensure the instance exists, so we can ignore the result
        wcApplication().needsInstallation();
    }


    // ----------------------------------------------------------
    /**
     * Pause the calling thread until the application has been completely
     * initialized (i.e., after the constructor and initializeApplication()
     * have both completed).
     */
    public static void waitForInitializationToComplete()
    {
        int count = 0;
        boolean logEntryAndExit = !initializationComplete;
        if (logEntryAndExit && log.isDebugEnabled())
        {
            log.debug("Thread " + Thread.currentThread().getName()
                + " waiting for intialization to complete");
        }
        while (!initializationComplete)
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                // ignore
            }
            count++;
            if (count % 1000 == 0)
            {
                log.error("Client thread " + Thread.currentThread().getName()
                    + " is still waiting for initialization to complete ("
                    + (count/2) + " sec).");
            }
        }
        if (logEntryAndExit && log.isDebugEnabled())
        {
            log.debug("Thread " + Thread.currentThread().getName()
                + ": intialization completed");
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns true if this application needs to run its self-installer,
     * and false if it is ready to run.
     * @return true if installation is needed
     */
    public boolean needsInstallation()
    {
        return needsInstallation;
    }


    // ----------------------------------------------------------
    /**
     * Tell this application whether or not it needs to run its self-installer.
     * @param value true if installation is needed
     */
    public void setNeedsInstallation(boolean value)
    {
        needsInstallation = value;
    }


    // ----------------------------------------------------------
    /**
     * If installation has been completed, initialize all subsystems in the
     * application and prepare it for running.
     */
    public void notifyAdminsOfStartup()
    {
        new ApplicationStartupMessage().send();
    }


    // ----------------------------------------------------------
    public void installPatches()
    {
        super.installPatches();
        WCContext.installIntoApplication(this);
    }


    // ----------------------------------------------------------
    /**
     * If installation has been completed, initialize all subsystems in the
     * application and prepare it for running.
     */
    public void initializeApplication()
    {
        loadArchiveManagers();
        updateStaticHtmlResources();

        // Apply any pending database updates for the core
        UpdateEngine.instance().database().setConnectionInfoFromProperties(
            configurationProperties());
        UpdateEngine.instance().applyNecessaryUpdates(
            new CoreDatabaseUpdates());

        // Set the eo model delegator
        EOModelGroup.defaultGroup().setDelegate(new SubsystemEOMRedirector());

        // register for database channel needed notification
        NSNotificationCenter.defaultCenter().addObserver(
            this,
            new NSSelector<Void>("createAdditionalDatabaseChannel",
                new Class<?>[] { NSNotification.class }),
            EODatabaseContext.DatabaseChannelNeededNotification,
            null);

        // log.debug("models = " + EOModelGroup.defaultGroup());

        // set up the SMTP server to use for sending Emails
        {
            // This is just support for legacy properties used by Web-CAT
            String host =
                configurationProperties().getProperty("mail.smtp.host");
            if (host == null || "".equals(host))
            {
                log.info("attempting to set mail.smtp.host from WOSMTPHost");
                configurationProperties().setProperty(
                    "mail.smtp.host", SMTPHost());
                configurationProperties().attemptToSave();
                configurationProperties().updateToSystemProperties();
            }
            else
            {
                setSMTPHost(host);
            }
        }
        log.info("Using SMTP host " + SMTPHost());
        log.debug("cmdShell = " + cmdShell());

        //      add handlers for main MIME types
        MailcapCommandMap mc =
            (MailcapCommandMap)CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; "
            + "x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; "
            + "x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; "
            + "x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; "
            + "x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; "
            + "x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);

        // Remove all state login session data
        boolean nsLogDebugEnabled = NSLog.debug.isEnabled();
        NSLog.debug.setIsEnabled(false);
        new ECAction() { public void action() {
            // First, attempt to force the initial JNDI exception because
            // the name jdbc is not bound
            try
            {
                LoginSession.allObjects(ec);
            }
            catch (Exception e)
            {
                // Silently swallow it, then retry on the next line
            }
            for (LoginSession session : LoginSession.allObjects(ec))
            {
                session.delete();
            }
            NSTimestamp thirtyDaysAgo = new NSTimestamp()
                .timestampByAddingGregorianUnits(0, 0, -30, 0, 0, 0);
            try
            {
                for (UsagePeriod period : UsagePeriod.objectsMatchingQualifier(
                    ec, UsagePeriod.startTime.before(thirtyDaysAgo)))
                {
                    period.delete();
                }
            }
            catch (Exception e)
            {
                log.error("Unable to purge old usage periods due to exception",
                    e);
                // TODO: drop all rows in usage period table here
            }
            ec.saveChanges();
        }}.run();
        NSLog.debug.setIsEnabled(nsLogDebugEnabled);

        // Force common objects to be loaded into the shared editing context
        // org.webcat.woextensions.WCSharedEC.install();
        AuthenticationDomain.refreshAuthDomains();
        Language.refreshLanguages();
        Theme.refreshThemes();

        NSLog.debug.setAllowedDebugLevel(NSLog.DebugLevelInformational);
        NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupMultithreading);

        // Add useful tag shortcuts for inline component tags.
        WOHelperFunctionHTMLTemplateParser.registerTagShortcut(
            "org.webcat.core.TableRow", "tr");
        WOHelperFunctionHTMLTemplateParser.registerTagShortcut(
            "WOComponentContent", "content");
        WOHelperFunctionHTMLTemplateParser.registerTagShortcut(
            "org.webcat.ui.WCTableString", "tstr");

        AjaxUpdateContainerTagProcessor tp =
            new AjaxUpdateContainerTagProcessor();
        WOTagProcessor simpleTp = new WOGenericContainerTagProcessor();

        WOHelperFunctionHTMLTemplateParser.registerTagProcessorForElementType(
            tp, "adiv");
        WOHelperFunctionHTMLTemplateParser.registerTagProcessorForElementType(
            tp, "aspan");
        WOHelperFunctionHTMLTemplateParser.registerTagProcessorForElementType(
            simpleTp, "label");
        WOHelperFunctionHTMLTemplateParser.registerTagProcessorForElementType(
            simpleTp, "div");

        setIncludeCommentsInResponses(false);

        initializeMessagingSystem();

        // Ensure subsystems are all loaded
        __subsystemManager = new SubsystemManager(configurationProperties());
//        subsystemManager();
        startTime = new NSTimestamp();
        checkBootstrapVersion();
        initializationComplete = true;
    }


    // ----------------------------------------------------------
    /**
     * A subclass-specific version of the inherited application() static
      * method.
     * @return The singleton instance of this class.
     */
    public static Application wcApplication()
    {
        return (Application)application();
    }


    // ----------------------------------------------------------
    /**
     * Access the application's subsystem manager.
     *
     * @return The subsystem manager
     */
    public SubsystemManager subsystemManager()
    {
        if (__subsystemManager == null)
        {
            // Wait until initialization is complete before letting the
            // caller proceed
            waitForInitializationToComplete();
        }
        return __subsystemManager;
    }

    public void logNotification(NSNotification notification)
    {
        log.info("notification posted: " + notification);
        log.info("notification object: " + notification.object());
    }


    // ----------------------------------------------------------
    /**
     * This notification callback is registered to respond when a
     * new database channel is needed.
     *
     * @param notification The notification received
     */
    public void createAdditionalDatabaseChannel(NSNotification notification)
    {
        EODatabaseContext dbContext = (EODatabaseContext)notification.object();
        EODatabaseChannel dbChannel = new EODatabaseChannel(dbContext);
        if (dbContext != null)
        {
            // consensus is that if you need more than 5 open channels,
            // you might want to re-think something in your code or model
            if (dbContext.registeredChannels().count() < 5)
            {
                log.debug("createAdditionalDatabaseChannel()");
                dbContext.registerChannel(dbChannel);
            }
            else
            {
                log.error("requesting > 5 database channels");
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns the class that represents the sessions that WebObjects should
     * create. Without this override, WebObjects assumes that the session class
     * is named "Session" in the same package as the application (which may be
     * the legacy net.sf.webcat.core.Application class in some cases).
     *
     * @return the class for the Session objects
     */
    protected Class<?> _sessionClass()
    {
        return Session.class;
    }


    // ----------------------------------------------------------
    /**
     * Restores a given session in context.
     *
     * @param sessionID  The ID of the session to restore
     * @param context    The context for the retrieval
     * @return        The session object
     */
    public WOSession restoreSessionWithID(
            String    sessionID,
            WOContext context)
    {
        WOSession result = super.restoreSessionWithID(sessionID, context);
        return result;
    }


    // ----------------------------------------------------------
    public static String loginUrl(WOContext context)
    {
        String dest = configurationProperties().getProperty("login.url");
        if (dest == null)
        {
            dest = configurationProperties().getProperty("base.url");
        }
        if (dest == null)
        {
            dest = completeURLWithRequestHandlerKey(
                context, null, null, null, false, 0);
        }
        return dest;
    }


    // ----------------------------------------------------------
    /**
     * Redirect to the login page.
     * @param context the context of the request
     * @return The login page
     */
    public WORedirect gotoLoginPage(WOContext context)
    {
        WORedirect redirect = (WORedirect)pageWithName("WORedirect", context);
        String dest = loginUrl(context);
        log.debug("gotoLoginPage: " + dest);
        redirect.setUrl(dest);
        return redirect;
    }


    // ----------------------------------------------------------
    /**
     * Returns the URL for the direct action to view the results.
     * @param context the context of the request
     * @param requestHandlerKey
     * @param aRequestHandlerPath
     * @param aQueryString
     * @param isSecure
     * @param somePort
     * @return the URL as a string
     */
    public static String completeURLWithRequestHandlerKey(
            WOContext context,
            String    requestHandlerKey,
            String    aRequestHandlerPath,
            String    aQueryString,
            boolean   isSecure,
            int       somePort)
    {
        return completeURLWithRequestHandlerKey(
            context,
            requestHandlerKey,
            aRequestHandlerPath,
            aQueryString,
            isSecure,
            somePort,
            false);
    }


    // ----------------------------------------------------------
    /**
     * Returns the URL for the direct action to view the results.
     * @param context the context of the request
     * @param requestHandlerKey
     * @param aRequestHandlerPath
     * @param aQueryString
     * @param isSecure
     * @param somePort
     * @param forceSecureSetting
     * @return the URL as a string
     */
    public static String completeURLWithRequestHandlerKey(
            WOContext context,
            String    requestHandlerKey,
            String    aRequestHandlerPath,
            String    aQueryString,
            boolean   isSecure,
            int       somePort,
            boolean   forceSecureSetting)
    {
        WORequest request = context.request();
        String dest = context.completeURLWithRequestHandlerKey(
            requestHandlerKey,
            aRequestHandlerPath,
            aQueryString,
            isSecure,
            somePort);
        log.debug("prior to munging, dest = " + dest);
        if (urlHostPrefix == null)
        {
            String result = configurationProperties().getProperty("base.url");
            if (result != null)
            {
                Matcher matcher = Pattern.compile("^http(s)?://[^/]*",
                    Pattern.CASE_INSENSITIVE ).matcher(result);
                if (matcher.find())
                {
                    urlHostPrefix = matcher.group() + "/";
                }
            }
            if (urlHostPrefix == null && request != null)
            {
                urlHostPrefix = "http://" + hostName(request) + "/";
            }
            log.debug("urlHostPrefix = " + urlHostPrefix);
        }
        if (urlHostPrefix != null)
        {
            dest = dest.replaceFirst("^http(s)?://[^/]*(/)?", urlHostPrefix);
        }
        dest = dest.replaceFirst("^http(s)?:",
            "http"
            + (((forceSecureSetting && isSecure)
                || (!forceSecureSetting
                    && request != null
                    && isSecure(request)))
                ? "s" : "")
            + ":");
        log.debug("link = " + dest);
        return dest;
    }


    // ----------------------------------------------------------
    /**
     * Returns <code>true</code> if the server prefers SSL connections.
     * @return <code>true</code> if SSL should be used by default.
     */
    static public boolean useSecureConnectionsByDefault()
    {
        if (defaultsToSecure == null)
        {
            String base = configurationProperties().getProperty("base.url");
            if (base != null)
            {
                defaultsToSecure = Boolean.valueOf(
                    base.startsWith("https")
                    || base.startsWith("HTTPS"));
            }
        }
        return defaultsToSecure == null
            ? false
            : defaultsToSecure.booleanValue();
    }


    // ----------------------------------------------------------
    /**
     * Returns <code>true</code> if the request was made via https/SSL,
     * <code>false</code> otherwise.  It makes the rather grand assumption that
     * all HTTPS connections are on port 443.
     * @param request the request being processed
     * @return <code>true</code> if the request was made via https/SSL,
     * <code>false</code> otherwise.
     */
    static public boolean isSecure(WORequest request)
    {
        /** require [valid_param] request != null; **/

        // The method of determining whether the request was via HTTPS depends
        // on the adaptor / the web server.

        // First we try and see if the request was made on the standard
        // https port
        String serverPort = null;
        for (int i = 0; serverPort == null
                        && i < SERVER_PORT_KEYS.count(); i++)
        {
            serverPort = request.headerForKey(
                SERVER_PORT_KEYS.objectAtIndex(i));
        }

        // Apache and some other web servers use this to indicate HTTPS mode.
        // This is much better as it does not depend on the port number used.
        String httpsMode = request.headerForKey("https");

        // If either the https header is 'on' or the server port is 443 then
        // we consider this to be an HTTP request.
        return (httpsMode  != null  &&  httpsMode.equalsIgnoreCase("on"))
            || (serverPort != null  &&  serverPort.equals("443"))
            || (httpsMode == null
                && serverPort == null
                && useSecureConnectionsByDefault());
    }


    // ----------------------------------------------------------
    /**
     * Returns the host name (a.k.a. server name, domain name) used in
     * this request.  The request headers are examined for the keys in
     * HOST_NAME_KEYS to determine the name.
     *
     * @param request the request to get the hostname from
     * @return the host name used in this request.
     */
    static public String hostName(WORequest request)
    {
        /** require [valid_param] request != null; **/

        String hostName = null;
        for (int i = 0; hostName == null && i < HOST_NAME_KEYS.count(); i++)
        {
            hostName = request.headerForKey(HOST_NAME_KEYS.objectAtIndex(i));
        }

        return hostName;

        /** ensure [valid_result] Result != null; **/
     }


    // ----------------------------------------------------------
    /**
     * If there is an error restoring a session (because of timeouts
     * or termination) this simply redirects to the login page again.
     * @param  context The context for the retrieval
     * @return The login page
     */
    public WOResponse handleSessionRestorationErrorInContext(
        WOContext context)
    {
        log.debug("handleSessionRestorationErrorInContext()");
        return gotoLoginPage(context).generateResponse();
    }


    // ----------------------------------------------------------
    public WOResponse handleSessionCreationErrorInContext(
        WOContext context)
    {
        log.debug("handleSessionCreationErrorInContext()");
        return super.handleSessionCreationErrorInContext(context);
    }


    // ----------------------------------------------------------
    /**
     * This is a typesafe version of the
     * {@link #pageWithName(String, WOContext)}
     * method, and should be used instead of the string version.
     * @param pageClass the class of the page to create
     * @param context The context for the page
     * @return a new instance of the class, appropriately typed.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends WOComponent> T pageWithName(
        Class<T> pageClass, WOContext context)
    {
        return (T)pageWithName(pageClass.getName(), context);
    }


    // ----------------------------------------------------------
    /**
     * Returns the given component by name.
     *
     * @param name    The name of the component to find
     * @param context The context for the retrieval
     * @return        The component
     */
    public WOComponent pageWithName(String name, WOContext context)
    {
        if (requestLog.isDebugEnabled())
        {
            requestLog.debug("pageWithName( "
                + ((name == null) ? "<null>" : name)
                + ", "
                + context
                + " )");
        }
        if (name == null || name.length() == 0 || "Main".equals(name))
        {
            log.info("pageWithName(" + name + ") called for URI "
                + context.request().uri());
            if (context.hasSession()
                && context.session() instanceof Session
                && ((Session)context.session()).isLoggedIn())
            {
                name = ((Session)context.session()).tabs.selectDefault()
                    .pageName();
            }
            else
            {
                name = LoginPage.class.getName();
            }
        }
        try
        {
            return super.pageWithName(name, context);
        }
        catch (WOPageNotFoundException pnf)
        {
            log.info("pageWithName(" + name + ")\n\tfor URI "
                + context.request().uri()
                + "\n\tproduced " + pnf);
            return super.pageWithName(LoginPage.class.getName(), context);
        }
    }


    // ----------------------------------------------------------
    /**
     * Force the garbage collector to run and release as much memory
     * back into the heap's free pool as possible.
     */
    public void forceFullGarbageCollection()
    {
        final int iterationLimit = 10;
        Runtime runtime = Runtime.getRuntime();
        log.info("Forcing full garbage collection...");
        long isFree = runtime.freeMemory();
        long wasFree;
        int iterations = 0;
        log.info("    wasFree: " + isFree + "...");
        do
        {
            wasFree = isFree;
            runtime.gc();
            isFree = runtime.freeMemory();
        } while (isFree > wasFree && iterations++ < iterationLimit);
        runtime.runFinalization();
        log.info("    isFree: " + isFree + ", total mem : "
            + runtime.totalMemory() + "...");
    }


    // ----------------------------------------------------------
    /**
     * Overrides parent implementation to add heap check and force
     * garbage collection when necessary.
     */
    @Override
    public WOResponse dispatchRequest(WORequest aRequest)
    {
        Runtime runtime = Runtime.getRuntime();
        final int freeLimit = 100000;
        final int dieLimit  =   5000;
        if (runtime.freeMemory() < freeLimit)
        {
            forceFullGarbageCollection();
            if (runtime.freeMemory() < freeLimit)
            {
                if (runtime.freeMemory() < dieLimit)
                {
                    sendAdminEmail("Dying, out of memory...",
                                   "Cannot force GC to free more than "
                                   + freeLimit + " bytes.  Terminating.");
                    killInstance();
                }
                else
                {
                    sendAdminEmail("Running out of memory...",
                                   "Cannot force GC to free more than "
                                   + freeLimit + " bytes.");
                }
            }
        }
        if (dieTime != null)
        {
            if (dieTime.before(new NSTimestamp()))
            {
                killInstance();
            }
        }
        else if (isRefusingNewSessions())
        {
            dieTime = (new NSTimestamp())
            .timestampByAddingGregorianUnits(
                0,  // years
                0,  // months
                0,  // days
                0,  // hours
                5,  // minutes
                0   // seconds
                );
        }
        if (requestLog.isDebugEnabled())
        {
            requestLog.debug("dispatchRequest(): method = "
                + aRequest.method());
            requestLog.debug("\tqueryString = " + aRequest.queryString());
            requestLog.debug("\trequestHandlerKey = "
                + aRequest.requestHandlerKey());
            requestLog.debug("\trequestHandlerPath = "
                + aRequest.requestHandlerPath());
            requestLog.debug("\turi = " + aRequest.uri());
            requestLog.debug("\tcookies = " + aRequest.cookies());
        }
        try
        {
            WOResponse result = super.dispatchRequest(aRequest);
            if (requestLog.isDebugEnabled())
            {
                requestLog.debug("dispatchRequest() result:\n" + result);
            }
            return result;
        }
        catch (IllegalStateException e)
        {
            String message = e.getMessage();
            // Swallow Eclipse update site requests that were
            // incorrectly targeted
            if (message == null
                || !message.contains("Direct Action type URL")
                || !message.contains("eclipse")
                || !(message.contains("contents.jar")
                    || message.contains("artifacts.jar")
                    || message.contains("p2.index")))
            {
                throw e;
            }
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Access the time this application was started up.
     * @return the time when this instance started
     */
    public NSTimestamp startTime()
    {
        return startTime;
    }


    // ----------------------------------------------------------
    /**
     * Access the application's property settings.
     * @return the property settings
     */
    public WCConfigurationFile properties()
    {
        return configurationProperties();
    }


    // ----------------------------------------------------------
    static public String configurationFileName()
    {
        String configFileName = ERXSystem.getProperty(
            "webobjects.user.dir");
        if (configFileName == null || configFileName.equals(""))
        {
            configFileName = ERXSystem.getProperty("user.dir");
        }
        configFileName = configFileName.replace('\\', '/');
        if (configFileName.length() > 0
            && !configFileName.endsWith("/"))
        {
            configFileName += "/";
        }
        configFileName += "configuration.properties";
        return configFileName;
    }


    // ----------------------------------------------------------
    /**
     * Access the application's property settings.
     * @return the property settings
     */
    static public WCConfigurationFile configurationProperties()
    {
        if (__properties == null)
        {
            __properties = new WCConfigurationFile(configurationFileName());
            __properties.updateToSystemProperties();
        }
        return __properties;
    }


    // ----------------------------------------------------------
    /**
     * Access the application's identifier (name).
     * @return the identifier
     */
    static public String appIdentifier()
    {
        if (__appIdentifier == null)
        {
            String appIdentifierRaw = configurationProperties().getProperty(
                "coreApplicationIdentifier");
            __appIdentifier = (appIdentifierRaw == null)
                ? "[Web-CAT] "
                : "[" + appIdentifierRaw + "] ";
        }
        return __appIdentifier;
    }


    // ----------------------------------------------------------
    /**
     * Overrides default WO version to include XP as a supported
     * platform.
     */
    public boolean _isForeignSupportedDevelopmentPlatform()
    {
        return super._isForeignSupportedDevelopmentPlatform() ||
            isRunningOnWindows();
    }


    // ----------------------------------------------------------
    /**
     * Overrides default WO version to force deployment-mode behavior
     * until valid configuration has been reached.
     */
    public boolean _rapidTurnaroundActiveForAnyProject()
    {
        return staticHtmlResourcesNeedInitializing
            || super._rapidTurnaroundActiveForAnyProject();
    }


    // ----------------------------------------------------------
    /**
     * Check to see if we are running on any flavor of Windows.
     * @return true when running on Windows
     */
    public static boolean isRunningOnWindows()
    {
        String s = System.getProperty("os.name");
        return s != null  &&  s.indexOf("Windows") >= 0;
    }


    // ----------------------------------------------------------
    /**
     * Reports an exception by logging, e-mailing admins, and returning
     * an error page.
     * @param exception to be reported
     * @param extraInfo dictionary of extra information about what was
     *        happening when the exception was thrown
     * @param context   the context in which the exception occurred
     * @return an error page
     */
    @SuppressWarnings("unchecked")
    public WOResponse reportException(Exception    exception,
                                      NSDictionary extraInfo,
                                      WOContext    context)
    {
        Throwable t = exception instanceof NSForwardException
            ? ((NSForwardException) exception).originalException()
            : exception;

        if (t != null
            && t instanceof java.lang.IllegalStateException
            && t.getMessage() != null
            && t.getMessage().contains("Couldn't locate action class"))
        {
            // Then don't e-mail this, because it is really just a bad
            // client-side request with a bad action class name
        }
        else
        {
            new UnexpectedExceptionMessage(t, context, extraInfo, null)
                .send();
        }

        if (context != null)
        {
            // Return a "clean" error page
            WOComponent errorPage =
                pageWithName(ErrorPage.class.getName(), context);
            errorPage.takeValueForKey(t, "exception");
            return errorPage.generateResponse();
        }
        else
        {
            // No context, so we cannot generate a real error page.  Instead,
            // return null, which should force a trivial error message
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Replaces the default exception page in WebObjects, and e-mails
     * a copy of the message to the administrator notification
     * address list.
     *
     * @param exception the exception that occurred
     * @param context   the context in which the exception occurred
     * @return          the error message page
     */
    public WOResponse handleException(Exception exception, WOContext context)
    {
        try
        {
            // We first want to test if we ran out of memory. If so we need
            // to quit ASAP.
            handlePotentiallyFatalException(exception);

            // Not a fatal exception, business as usual.
            NSDictionary<?, ?> extraInfo =
                extraInformationForExceptionInContext(exception, context);
            WOResponse response =
                reportException(exception, extraInfo, context);
            if (response == null && context != null)
            {
                response = super.handleException(exception, context);
            }
            return response;
        }
        catch (Throwable t)
        {
            log.error("handleException failed", t);
            return super.handleException(exception, context);
        }
    }


    // ----------------------------------------------------------
    /**
     * Replaces the default page restoration error page in WebObjects
     * with {@link WCPageRestorationErrorPage}.
     *
     * @param context   the context in which the exception occurred
     * @return          the error message page
     */
    public WOResponse handlePageRestorationErrorInContext(WOContext context)
    {
        return pageWithName(
            WCPageRestorationErrorPage.class.getName(), context)
            .generateResponse();
    }


    // ----------------------------------------------------------
    public NSMutableDictionary<?, ?> extraInformationForExceptionInContext(
        Exception exception, WOContext context)
    {
        NSMutableDictionary<?, ?> result =
            super.extraInformationForExceptionInContext(exception, context);
        if (   context != null
            && context.hasSession()
            && context.session() instanceof Session)
        {
            Session s = (Session)context.session();
            TabDescriptor currentTab =
                (s.tabs == null)
                    ? null
                    : s.tabs.selectedDescendant();
            result.setObjectForKey(
                (currentTab == null)
                    ? "null"
                    : currentTab.printableTabLocation(),
                "current tab");
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Sends a text e-mail message to the specified recipient.
     *
     * @param to          the destination address
     * @param subject     the subject line
     * @param body        the body of the message
     */
    static public void sendSimpleEmail(String to, String subject, String body)
    {
        sendSimpleEmail(new NSArray<String>(to), subject, body, null);
    }


    // ----------------------------------------------------------
    /**
     * Sends a text e-mail message to the specified recipient.
     *
     * @param to          the destination address
     * @param subject     the subject line
     * @param body        the body of the message
     * @param attachments the attachments
     */
    static public void sendSimpleEmail(
        String to,
        String subject,
        String body,
        List<File> attachments)
    {
        sendSimpleEmail(new NSArray<String>(to), subject, body, attachments);
    }


    // ----------------------------------------------------------
    /**
     * Sends a text e-mail message to the specified recipients.
     *
     * @param to          the destination addresses
     * @param subject     the subject line
     * @param body        the body of the message
     * @param attachments the attachments
     */
    static public void sendSimpleEmail(NSArray<String> to,
                                       String subject,
                                       String body)
    {
        sendSimpleEmail(to, subject, body, null);
    }


    // ----------------------------------------------------------
    /**
     * Sends a text e-mail message to the specified recipients.
     *
     * @param to          the destination addresses
     * @param subject     the subject line
     * @param body        the body of the message
     * @param attachments the attachments
     */
    static public void sendSimpleEmail(NSArray<String> to,
                                       String subject,
                                       String body,
                                       List<File> attachments)
    {
        try
        {
            // Define message
            javax.mail.Message message =
                new javax.mail.internet.MimeMessage(
                        javax.mail.Session.getInstance(
                            configurationProperties(), null));

            message.setFrom(new InternetAddress(
                configurationProperties().getProperty("coreAdminEmail")));
            message.setSentDate(new NSTimestamp());

            // Add each recipient to the message.
            for (String toAddress : to)
            {
                String defaultDomain = configurationProperties()
                    .getProperty("mail.default.domain");

                if (toAddress.indexOf( '@' ) == -1 && defaultDomain != null)
                {
                    toAddress += "@" + defaultDomain;
                }

                message.addRecipient(javax.mail.Message.RecipientType.TO,
                                     new InternetAddress(toAddress));
            }

            message.setSubject(appIdentifier() + subject);

            if (attachments == null || attachments.size() == 0)
            {
                message.setText(body);
            }
            else
            {
                // Create the message part
                javax.mail.BodyPart messageBodyPart = new MimeBodyPart();

                // Fill the message
                messageBodyPart.setText(body);

                // Create a Multipart
                javax.mail.Multipart multipart = new MimeMultipart();

                // Add part one
                multipart.addBodyPart(messageBodyPart);

                //
                // The next parts are attachments
                //
                for (File file : attachments)
                {
                    // Create another body part
                    messageBodyPart = new MimeBodyPart();

                    // Don't include files bigger than this as e-mail
                    // attachments
                    if (file.length() < maxAttachmentSize)
                    {
                        // Get the attachment
                        FileDataSource source = new FileDataSource(file);

                        // Set the data handler to the attachment
                        messageBodyPart.setDataHandler(
                            new DataHandler(source));

                        // Set the filename
                        messageBodyPart.setFileName(file.getName());
                    }
                    else
                    {
                        // Fill the message
                        messageBodyPart.setText(
                            "File "
                            + file.getName()
                            + " has been omitted from this message ("
                            + file.length()
                            + " bytes)\n");
                    }

                    // Add attachment
                    multipart.addBodyPart(messageBodyPart);
                }

                // Put parts in message
                message.setContent(multipart);
            }

            // Send the message
            if ("donotsendmail".equals(
                configurationProperties().getProperty("mail.smtp.host")))
            {
                log.warn("E-MAIL DISABLED: unsent message:\nTo: "
                    + to
                    + "\nSubject: " + (subject == null ? "null" : subject)
                    + "\nBody:\n" + (body == null ? "null" : body));
            }
            else
            {
                // AJA 2010.01.12: Fix for the JAF/Mail issue that prevented
                // mail from being sent from a servlet when running under
                // Java 1.6:
                // http://blog.hpxn.net/2009/12/02/tomcat-java-6-and-javamail-cant-load-dch/
                ClassLoader originalClassLoader =
                    Thread.currentThread().getContextClassLoader();

                try
                {
                    Thread.currentThread().setContextClassLoader(
                            Application.class.getClassLoader());
                    javax.mail.Transport.send(message);
                }
                finally
                {
                    Thread.currentThread().setContextClassLoader(
                            originalClassLoader);
                }
            }
        }
        catch (Exception e)
        {
            String msg = e.getMessage();
            if (msg != null && msg.contains("java.net.UnknownHostException:"))
            {
                log.error("Exception sending mail message: " + e);
            }
            else
            {
                log.error("Exception sending mail message:\n", e);
                log.error("unsent message:\nTo: "
                       + (to == null ? "null" : to)
                       + "\nSubject: " + (subject == null ? "null" : subject)
                       + "\nBody:\n" + (body == null ? "null" : body));
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Sends a text e-mail message to the system administrators.
     *
     * @param subject     the subject line
     * @param body        the body of the message
     * @return the array of admin e-mail addresses
     */
    public static NSArray<String> adminEmailAddresses()
    {
        String adminList =
            configurationProperties().getProperty("adminNotifyAddrs");

        if (adminList == null)
        {
            adminList = configurationProperties()
                .getProperty("coreAdminEmail");
        }
        else
        {
            String primaryAdmin = configurationProperties()
                .getProperty("coreAdminEmail");

            if (primaryAdmin != null && !adminList.contains(primaryAdmin))
            {
                adminList = primaryAdmin + "," + adminList;
            }
        }

        if (adminList == null)
        {
            return null;
        }

        String[] admins = adminList.split("\\s*,\\s*");
        return new NSArray<String>(admins);
    }


    // ----------------------------------------------------------
    /**
     * Sends a text e-mail message to the system administrators.
     *
     * @param subject     the subject line
     * @param body        the body of the message
     */
    public static void sendAdminEmail(String subject, String body)
    {
        sendAdminEmail(subject, body, null);
    }


    // ----------------------------------------------------------
    /**
     * Sends a text e-mail message to the system administrators.
     *
     * @param subject     the subject line
     * @param body        the body of the message
     * @param attachments the attachments
     */
    public static void sendAdminEmail(
        String subject, String body, List<File> attachments)
    {
        NSArray<String> adminList = adminEmailAddresses();
        if (adminList == null || adminList.count() == 0)
        {
            log.error("No bound admin e-mail addresses.  "
                      + "Cannot send message:\n"
                      + "Subject: " + subject + "\n"
                      + "Message:\n" + body);
            return;
        }
        sendSimpleEmail(adminList, subject, body, attachments);
    }


    // ----------------------------------------------------------
    /**
     * Generate a string of extra info for the given context (can be used
     * for debugging output).
     * @param context the context for this request (can be null, if desired)
     * @return the extra information in a formatted, human-readable,
     *         multi-line string
     */
    public static String extraInfoForContext(WOContext context)
    {
        if (context != null && context.page() != null)
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append("CurrentPage = ");
            buffer.append(context.page().name());
            buffer.append("\n");

            if (context.component() != null)
            {
                buffer.append("CurrentComponent = ");
                buffer.append(context.component().name());
                buffer.append("\n");
                if (context.component().parent() != null)
                {
                    WOComponent component = context.component();
                    while (component.parent() != null)
                    {
                        component = component.parent();
                        buffer.append("    parent = ");
                        buffer.append(component.name());
                        buffer.append("\n");
                    }
                }
            }
            buffer.append("uri = ");
            buffer.append(context.request().uri());
            buffer.append("\n");
            if (context.hasSession())
            {
                if (context.session().statistics() != null)
                {
                    buffer.append("PreviousPageList = ");
                    buffer.append(context.session().statistics());
                    buffer.append("\n");
                }
                if (context.session() instanceof Session)
                {
                    Session s = (Session)context.session();
                    TabDescriptor currentTab = (s.tabs == null)
                        ? null
                        : s.tabs.selectedDescendant();
                    buffer.append("current tab = ");
                    buffer.append((currentTab == null)
                        ? "null"
                        : currentTab.printableTabLocation());
                    buffer.append("\n");
                }
            }
            return buffer.toString();
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the application is running as a
     * servlet.
     *
     * @return True if the application is running as a servlet, otherwise
     *         false.
     */
    public static boolean isRunningAsServlet()
    {
        return net.sf.webcat.WCServletAdaptor.getInstance() != null;
    }


    // ----------------------------------------------------------
    /**
     * Method to assemble information on the exception, including the
     * message, stack trace, the name of the component which contained
     * the error (or caused it), and any session information required.
     * This is an instance method instead of being static to ensure that
     * it is synchronized.
     * @param anException the exception that occurred
     * @param extraInfo   dictionary of extra information about what was
     *                    happening when the exception was thrown
     * @param aContext    the context in which the exception occurred
     * @return a printable description of the error
     */
    public synchronized String informationForExceptionInContext(
        Throwable          anException,
        NSDictionary<?, ?> extraInfo,
        WOContext          aContext)
    {
        Session s = (aContext != null  &&  aContext.hasSession())
            ? (Session)aContext.session()
            : null;

        // Set up a buffer for the content
        StringBuffer errorBuffer = new StringBuffer();

        if (s != null  &&  s.primeUser() != null)
        {
            // Get the pid of the user of the session
            errorBuffer.append("User     :   ");
            errorBuffer.append(s.primeUser().nameAndUid());
        }

        // Get the date and time for the exception
        NSTimestamp now = new NSTimestamp();
        errorBuffer.append("\nDate/time:   ");
        errorBuffer.append(now.toString());
        if (aContext != null && aContext.request() != null)
        {
            errorBuffer.append("\nRequest: ");
            errorBuffer.append(aContext.request().uri());
            errorBuffer.append("\nReferer: ");
            errorBuffer.append(aContext.request().headerForKey("referer"));
        }

        if (errorLoggingContext == null)
        {
            errorLoggingContext = WCEC.newEditingContext();
        }
        try
        {
            errorLoggingContext.lock();
            LoggedError loggedError = null;
            if (!needsInstallation())
            {
                loggedError = LoggedError.objectForException(
                    errorLoggingContext, anException);
            }

            if (loggedError != null)
            {
                loggedError.setOccurrences(loggedError.occurrences() + 1);
                loggedError.setMostRecent(now);
                errorBuffer.append("\nOccurrences: ");
                errorBuffer.append(loggedError.occurrences());
            }
            if (   aContext != null
                && aContext.component() != null)
            {
                // Get the current component
                errorBuffer.append("\nComponent:   ");
                String name = aContext.component().name();
                if (name == null)
                {
                    name = aContext.component().getClass().getName();
                }
                if (loggedError != null)
                {
                    loggedError.setComponent(name);
                }
                errorBuffer.append(name);
            }
            if (   aContext        != null
                && aContext.page() != null
                && loggedError     != null)
            {
                loggedError.setPage(aContext.page().name());
            }

            // Get the session associated (if any)
            if (s != null)
            {
                errorBuffer.append("\nSessionID: " + s.sessionID());
            }

            if (anException != null)
            {
                // Get the full message for the exception
                errorBuffer.append("\n\nException:\n----------\n");
                errorBuffer.append(anException.getClass().getName());
                errorBuffer.append(":\n");
                errorBuffer.append(anException.getMessage());
                errorBuffer.append("\n");
                if (anException.getMessage() != null
                    && loggedError != null)
                {
                    loggedError.setMessage(anException.getMessage());
                }

                if (extraInfo != null)
                {
                    errorBuffer.append(
                        "\n\nExtra information:\n--------------------\n");
                    for (Enumeration<?> e = extraInfo.keyEnumerator();
                         e.hasMoreElements();)
                    {
                        Object key = e.nextElement();
                        if (!"Session".equals(key)
                            && !"Bundles".equals(key))
                        {
                            Object value = extraInfo.objectForKey(key);
                            errorBuffer.append(key);
                            errorBuffer.append(" = ");
                            errorBuffer.append(value);
                            errorBuffer.append('\n');
                        }
                    }
                }

                // Get the stack trace for the exception
                errorBuffer.append("\nStack trace:\n-----------------\n");
                if (loggedError != null)
                {
                    StringWriter writer = new StringWriter();
                    PrintWriter pwriter = new PrintWriter(writer);
                    anException.printStackTrace(pwriter);
                    pwriter.close();
                    loggedError.setStackTrace(
                        writer.getBuffer().toString());
                }
                if (!isRunningAsServlet())
                {
                    // If we're not running as a servlet, then assume we're
                    // in a developer environment and generate fully
                    // compliant stack trace info for IDE parsing:
                    StringWriter writer = new StringWriter();
                    PrintWriter pwriter = new PrintWriter(writer);
                    anException.printStackTrace(pwriter);
                    pwriter.close();
                    errorBuffer.append(writer.getBuffer());
                }
                else
                {
                    // For deployment, use a simplified stack trace
                    // presentation to make e-mail messages lighter (and
                    // also somewhat more readable).
                    WOExceptionParser exParser =
                        new WOExceptionParser(anException);
                    Enumeration<?> traceEnum =
                        exParser.stackTrace().objectEnumerator();

                    // Append each trace line
                    while (traceEnum.hasMoreElements())
                    {
                        WOParsedErrorLine aLine =
                            (WOParsedErrorLine)traceEnum.nextElement();
                        errorBuffer.append("at " + aLine.methodName() + "("
                            + aLine.fileName() + ":"
                            + aLine.lineNumber() + ")\n");
                    }
                }
            }
            else
            {
                if (extraInfo != null)
                {
                    errorBuffer.append(
                        "\n\nExtra information:\n--------------------\n");
                    for (Enumeration<?> e = extraInfo.keyEnumerator();
                         e.hasMoreElements();)
                    {
                        Object key = e.nextElement();
                        Object value = extraInfo.objectForKey(key);
                        errorBuffer.append(key);
                        errorBuffer.append("\t= ");
                        errorBuffer.append(value);
                        errorBuffer.append('\n');
                    }
                }
            }
            errorLoggingContext.saveChanges();
        }
        catch (Exception e)
        {
            EOEditingContext old = errorLoggingContext;
            errorLoggingContext = null;
            try
            {
                try
                {
                    // Try to unlock it, if possible
                    old.unlock();
                }
                catch (Exception ee)
                {
                    // ignore, since we're throwing it away
                }
                old.dispose();
            }
            catch (Exception e2)
            {
                log.fatal("error releasing error logging editing context",
                    e2);
                log.fatal("original exception causing error logging "
                    + "context to be released", e);
            }
        }
        finally
        {
            if (errorLoggingContext != null)
            {
                errorLoggingContext.unlock();
            }
        }

        // Return the information
        return errorBuffer.toString();
    }


    // ----------------------------------------------------------
    @SuppressWarnings("deprecation")
    public void refuseNewSessions(boolean refuse)
    {
        boolean isDirectConnectEnabled = isDirectConnectEnabled();
        if (refuse)
        {
            setDirectConnectEnabled(false);
            int timeToKill = configurationProperties().intForKey(
                "ERTimeToKill");
            if (timeToKill > 0)
            {
                dieTime = (new NSTimestamp())
                    .timestampByAddingGregorianUnits(
                        0, 0, 0, 0, 0, timeToKill);
            }
        }
        else
        {
            dieTime = null;
        }
        super.refuseNewSessions(refuse);
        setDirectConnectEnabled(isDirectConnectEnabled);
    }


    // ----------------------------------------------------------
    public NSTimestamp deathTime()
    {
        return dieTime;
    }


    // ----------------------------------------------------------
    public boolean deathIsScheduled()
    {
        return dieTime != null;
    }


    // ----------------------------------------------------------
    @SuppressWarnings("deprecation")
    public String deathMessage()
    {
        if (dieTime != null && deathMessage == null)
        {
            StringBuffer buffer = new StringBuffer(200);
            buffer.append("<b>Immediate shutdown:</b> ");
            buffer.append("Web-CAT will be going off-line at ");
            ERXTimestampFormatter formatter =
                new ERXTimestampFormatter("%I:%M%p");
            java.text.FieldPosition pos = new java.text.FieldPosition(0);
            formatter.format(dieTime, buffer, pos);
            buffer.append(".  Save your work and logout.");
            deathMessage = buffer.toString();
        }
        return deathMessage;
    }


    // ----------------------------------------------------------
    public static String cmdShell()
    {
        if (cmdShell == null)
        {
            cmdShell = configurationProperties().getProperty("cmdShell");
            if (cmdShell == null)
            {
                if (isRunningOnWindows())
                {
                    cmdShell = "cmd /c";
                }
                else
                {
                    cmdShell = "sh -c \"exec";
                }
            }
            int len = cmdShell.length();
            if (len > 0
                && cmdShell.charAt( len - 1 ) != ' '
                && cmdShell.charAt( len - 1 ) != '"')
            {
                cmdShell += " ";
            }
        }
        return cmdShell;
    }


    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see er.extensions.appserver.ERXApplication#killInstance()
     */
    public void killInstance()
    {
        String killAction =
            configurationProperties().getProperty("coreKillAction");
        if (killAction == null)
        {
            log.fatal("Using default kill action", new Exception("from here"));
            super.killInstance();
        }
        else
        {
            String cmd = cmdShell() + killAction;
            log.fatal("Killing application using: " + cmd,
                new Exception("from here"));
            Process proc = null;
            try
            {
                proc = Runtime.getRuntime().exec(cmd);
                proc.waitFor();
                // wait for ten seconds to give the kill command time to
                // work externally, since immediate return of the process
                // may not always mean its work is complete
                Thread.sleep(10000);
            }
            catch (Exception e)
            {
                // stopped by timeout
                if (proc != null)
                {
                    proc.destroy();
                }
                log.fatal("exception executing kill action '" + cmd + "'", e);
                super.killInstance();
            }
        }
    }


    // ----------------------------------------------------------
    @Override
    public WOSessionStore sessionStore()
    {
        WOSessionStore store = super.sessionStore();
        if (log.isDebugEnabled())
        {
            log.debug("sessionStore() class = " + store.getClass().getName());
            log.debug("sessionStore() = " + store);
        }
        return store;
    }


    // ----------------------------------------------------------
    public String version()
    {
        if (version == null)
        {
            WCConfigurationFile config = properties();
            int major = 0;
            int minor = 0;
            int revision = 0;
            int date = 0;
            for (Enumeration<Object> keys = System.getProperties().keys();
                 keys.hasMoreElements();)
            {
                String key = keys.nextElement().toString();
                if (key.endsWith("version.major"))
                {
                    int thisMajor = config.intForKey(key);
                    if (thisMajor > major)
                    {
                        major = thisMajor;
                    }
                }
                else if (key.endsWith( "version.minor"))
                {
                    int thisMinor = config.intForKey(key);
                    if (thisMinor > minor)
                    {
                        minor = thisMinor;
                    }
                }
                else if ( key.endsWith("version.revision"))
                {
                    int thisRevision = config.intForKey(key);
                    if (thisRevision > revision)
                    {
                        revision = thisRevision;
                    }
                }
                else if (key.endsWith("version.date"))
                {
                    int thisDate = config.intForKey(key);
                    if (thisDate > date)
                    {
                        date = thisDate;
                    }
                }
            }
            version =   "" + config.intForKey("webcat.version.major") + "."
                + config.intForKey("webcat.version.minor") + "."
                + config.intForKey("webcat.version.revision") + "/"
                + major + "." + minor + "." + revision + "." + date;
        }
        return version;
    }


    // ----------------------------------------------------------
    public String sessionStoreClassName()
    {
        String result = super.sessionStoreClassName();
        if (log.isDebugEnabled())
        {
            log.debug("sessionStoreClassName() = " + result);
        }
        return result;
    }


    // ----------------------------------------------------------
    public WOResourceManager createResourceManager()
    {
        return new WCResourceManager();
    }


    // ----------------------------------------------------------
    /**
     * Execute an external command, using the appropriate command shell
     * setting from the app configuration data.  If subsystems have
     * already been initialized, their exported ENV settings will be passed
     * to the command as well.
     * @param commandLine The command to execute
     * @param cwd The (optional) working directory in which the command
     * should be executed.  If null, the application's cwd will be used.
     * @throws java.io.IOException if an error occurs communicating with
     * the child process
     * @throws InterruptedException if the child process gets interrupted
     */
    public void executeExternalCommand(String commandLine, File cwd)
        throws java.io.IOException, InterruptedException
    {
        Process proc = null;

        try
        {
            proc = executeExternalCommandAsync(commandLine, cwd);
            int exitCode = proc.waitFor();
            log.debug("external command returned exit code: " + exitCode);
        }
        catch (InterruptedException e)
        {
            // stopped by timeout
            if (proc != null)
            {
                if (DESTROY_FORCIBLY == null)
                {
                    proc.destroy();
                }
                else
                {
                    try
                    {
                        DESTROY_FORCIBLY.invoke(proc);
                    }
                    catch (Exception ee)
                    {
                        log.error("Error invoking destroyForcibly()", ee);
                        proc.destroy();
                    }
                }
            }
            throw e;
        }
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Execute an external command, using the appropriate command shell
     * setting from the app configuration data, but does not wait for it to
     * finish.  The Java Process object will be returned so that it can be
     * manipulated and its input/output streams can be accessed.
     * </p><p>
     * If subsystems have already been initialized, their exported ENV settings
     * will be passed to the command as well.
     * </p>
     *
     * @param commandLine The command to execute
     * @param cwd The (optional) working directory in which the command
     * should be executed.  If null, the application's cwd will be used.
     * @return the Java Process object that represents the external process
     * @throws java.io.IOException if an error occurs communicating with
     * the child process
     * @throws InterruptedException if the child process gets interrupted
     */
    public Process executeExternalCommandAsync(String commandLine, File cwd)
        throws java.io.IOException
    {
        String[] cmdArray = null;
        Process proc = null;

        // Tack on the command shell prefix to the beginning, quoting the
        // whole argument sequence if necessary
        {
            String shell = org.webcat.core.Application.cmdShell();
            if (shell != null && shell.length() > 0)
            {
                int pos = shell.lastIndexOf('"');
                if (pos > 0)
                {
                    cmdArray = shell.substring(0, pos + 1).split("\\s+");
                    cmdArray[cmdArray.length - 1] =
                        shell.substring(pos + 1) + commandLine;
                }
                else
                {
                    commandLine = shell + commandLine;
                }
            }
        }

        String[] envp = null;
        // If subsystems are already loaded, get their ENV info:
        if (__subsystemManager != null)
        {
            envp = subsystemManager().envp();
        }
        if (cmdArray != null)
        {
            log.debug("executeExternalCommand(): "
                + Arrays.toString(cmdArray));
            proc = Runtime.getRuntime().exec(cmdArray, envp, cwd);
        }
        else
        {
            log.debug("executeExternalCommand(): " + commandLine);
            proc = Runtime.getRuntime().exec(commandLine, envp, cwd);
        }

        return proc;
    }


    // ----------------------------------------------------------
    /**
     * Enables logging of executed SQL statements.
     */
    public static synchronized void enableSQLLogging()
    {
        Logger.getLogger("NSLog").setLevel(Level.DEBUG);
        Logger.getLogger(er.extensions.logging.ERXNSLogLog4jBridge.class)
                .setLevel(Level.DEBUG);
        Logger.getLogger("com.webobjects.jdbcadaptor.MySqlPlugin")
                .setLevel(Level.DEBUG);
        Logger.getLogger("er.transaction.adaptor.EOAdaptorDebugEnabled")
                .setLevel(Level.DEBUG);
        NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupDatabaseAccess);
        System.setProperty("er.extensions.ERXAdaptorChannelDelegate.enabled",
                "true");
    }


    // ----------------------------------------------------------
    /**
     * Disables logging of executed SQL statements.
     */
    public static synchronized void disableSQLLogging()
    {
        Logger.getLogger("NSLog").setLevel(Level.OFF);
        Logger.getLogger(er.extensions.logging.ERXNSLogLog4jBridge.class)
                .setLevel(Level.OFF);
        Logger.getLogger("com.webobjects.jdbcadaptor.MySqlPlugin")
                .setLevel(Level.OFF);
        Logger.getLogger("er.transaction.adaptor.EOAdaptorDebugEnabled")
                .setLevel(Level.OFF);
        NSLog.refuseDebugLoggingForGroups(NSLog.DebugGroupDatabaseAccess);
        System.setProperty("er.extensions.ERXAdaptorChannelDelegate.enabled",
                "false");
    }


    // ----------------------------------------------------------
    private void updateStaticHtmlResources()
    {
        String staticHtmlBase = configurationProperties().getProperty(
            "static.html.baseURL");

        if (isRunningAsServlet())
        {
            File woaDir = configurationProperties().file().getParentFile();
            File appBase = woaDir.getParentFile().getParentFile();
            File staticResourceBaseDir = appBase;
            File frameworkDir =
                new File(woaDir, "Contents/Frameworks/Library/Frameworks");
            if (!frameworkDir.exists())
            {
                frameworkDir =
                    new File(woaDir, "Contents/Library/Frameworks");
            }
            String staticHtmlDirName = configurationProperties()
                .getProperty("static.html.dir");
            String lastStaticHtmlDirName = configurationProperties()
                .getProperty("last.static.html.dir");

            if (staticHtmlDirName != null && staticHtmlBase != null)
            {
                // Only use the static HTML dir parameter if the app is also
                // configured to use an external static HTML base URL for
                // resources.
                staticHtmlDirName = staticHtmlDirName.replace('\\', '/');
                File dir = new File(staticHtmlDirName);
                if (!dir.exists())
                {
                    try
                    {
                        dir.mkdirs();
                    }
                    catch (Exception e)
                    {
                        log.error("Exception attempting to create static HTML "
                            + "resource dir '" + staticHtmlDirName + "':", e);
                    }
                }
                if (dir.exists())
                {
                    staticResourceBaseDir = dir;
                }
            }
            else
            {
                // If there is no static HTML dir specified, or if there is
                // no base URL given, then the servlet will have to serve all
                // these resources, and therefore we need to store static
                // resources in the app base dir.

                // But we still need a value we can use to compare against the
                // previous value, to see if the location has changed
                staticHtmlDirName = appBase.getAbsolutePath()
                    .replace('\\', '/');
            }

            if (log.isDebugEnabled())
            {
                log.debug("updateStaticHtmlResources: staticHtmlDir = "
                    + staticHtmlDirName);
                log.debug("updateStaticHtmlResources: lastStaticHtmlDir = "
                    + lastStaticHtmlDirName);
                log.debug("appBase = " + appBase.getAbsolutePath());
                log.debug("staticResourceBase = " + staticResourceBaseDir);
            }

            // Note: we can't use the subsystem manager yet, since the
            // application has not been fully initialized yet and that data
            // isn't available at this point.
            for (File framework : frameworkDir.listFiles())
            {
                log.debug("Checking for static html resources in => "
                    + framework.getName());
                String frameworkName = framework.getName();
                if (!frameworkName.endsWith(".framework"))
                {
                    continue;
                }
                frameworkName = frameworkName.substring(0,
                    frameworkName.length() - ".framework".length());
                String frameworkDatestamp = configurationProperties().
                    getProperty(frameworkName + ".version.date");
                // frameworkLastUpdated will be null for frameworks that are
                // not packaged subsystems
                if (frameworkDatestamp != null)
                {
                    String lastUpdated = configurationProperties().getProperty(
                        "static.HTML.date." + frameworkName, "00000000");
                    if (log.isDebugEnabled())
                    {
                        log.debug("Comparing last update " + lastUpdated +
                            " against " + frameworkName + " datestamp of: "
                            + frameworkDatestamp);
                    }
                    if (!lastUpdated.equals(frameworkDatestamp))
                    {
                        log.debug("Updating resources for " + frameworkName);
                        updateExecutablePermissionsForFramework(
                            frameworkName, framework);
                        updateStaticHtmlResourcesForFramework(
                            framework, staticResourceBaseDir);
                        // Now check the additional included frameworks too
                        String alsoContainsList = configurationProperties()
                            .getProperty(frameworkName + ".alsoContains");
                        if (alsoContainsList != null)
                        {
                            for (String fw : alsoContainsList.split(",\\s*"))
                            {
                                File otherFramework =
                                    new File(frameworkDir, fw);
                                if (log.isDebugEnabled())
                                {
                                    String includedName = fw.substring(0,
                                        fw.length() - ".framework".length());
                                    log.debug("Updating resources for "
                                        + includedName);
                                }
                                updateStaticHtmlResourcesForFramework(
                                    otherFramework, staticResourceBaseDir);
                            }
                        }
                        configurationProperties().setProperty(
                            "static.HTML.date." + frameworkName,
                            frameworkDatestamp);
                    }
                    else
                    {
                        log.debug("Already up to date: " + frameworkName);
                    }
                }
            }

            // Attempt to update the "last saved" info
            configurationProperties().setProperty("last.static.html.dir",
                staticResourceBaseDir.getAbsolutePath().replace('\\', '/'));
            configurationProperties().attemptToSave();

            if (staticHtmlBase == null)
            {
                staticHtmlBase = configurationProperties().getProperty(
                    "base.url");

                if (staticHtmlBase == null)
                {
                    staticHtmlBase =
                        Application.application().servletConnectURL();
                    System.out.println("servlet URL = " + staticHtmlBase);
                    staticHtmlBase =
                        staticHtmlBase.replaceFirst("^[^/]*//[^/]+/", "");
                    System.out.println("servlet URL = " + staticHtmlBase);
                }

                if (staticHtmlBase.endsWith(".woa"))
                {
                    int loc = staticHtmlBase.lastIndexOf('/');
                    if (loc > 0)
                    {
                        staticHtmlBase = staticHtmlBase.substring(0, loc);
                    }
                }
                if (staticHtmlBase.endsWith("WebObjects"))
                {
                    int loc = staticHtmlBase.lastIndexOf('/');
                    if (loc > 0)
                    {
                        staticHtmlBase = staticHtmlBase.substring(0, loc);
                    }
                }
                if (!staticHtmlBase.endsWith("/"))
                {
                    staticHtmlBase = staticHtmlBase + "/";
                }
            }
        }
        else
        {
            // If we're not running as a servlet, there's no updating to do
            log.debug("Skipping static HTML resource updates");
        }
        if (staticHtmlBase != null)
        {
            log.debug("attempting to set frameworks Base URL = "
                + staticHtmlBase);
            setFrameworksBaseURL(staticHtmlBase);
        }

        // Dump any cached data using the previous frameworks base url
        resourceManager().flushDataCache();
        staticHtmlResourcesNeedInitializing = false;

        log.debug("frameworks Base URL = " + frameworksBaseURL());
    }


    // ----------------------------------------------------------
    private void updateStaticHtmlResourcesForFramework(
        File framework, File staticResourceBaseDir)
    {
        File webServerResources = new File(framework, "WebServerResources");
        if (webServerResources.isDirectory())
        {
            log.info("Copying static html resources from => "
                + framework.getName());
            try
            {
                File target = new File(staticResourceBaseDir,
                        framework.getName()
                        + "/" + webServerResources.getName());
                if (target.exists())
                {
                    if (target.isDirectory())
                    {
                        FileUtilities.deleteDirectory(target);
                    }
                    else
                    {
                        target.delete();
                    }
                }
                target.mkdirs();
                FileUtilities.copyDirectoryContents(
                    webServerResources, target);
            }
            catch (java.io.IOException e)
            {
                log.error("Exception copying static html resource from '"
                    + webServerResources + "' to '"
                    + staticResourceBaseDir + "'",
                    e);
            }
        }
    }


    // ----------------------------------------------------------
    private void updateExecutablePermissionsForFramework(
        String frameworkName, File frameworkDir)
    {
        // No need for file perms on windows
        if (isRunningOnWindows())
        {
            return;
        }

        // Otherwise, attempt to add executable permissions to necessary
        // files
        String execFileList = configurationProperties()
            .getProperty(frameworkName + ".chmodx");
        if (execFileList != null)
        {
            for (String fileName : execFileList.split(",\\s*"))
            {
                File file = new File(frameworkDir, fileName);
                if (file.exists())
                {
                    // Make it executable, if possible.
                    // First, build the chmod command line.  Start with
                    // file name, and escape any space chars (other shell-
                    // special chars are not escaped!)
                    String cmd = file.toString();
                    cmd = cmd.replaceAll(" ", "\\\\ ");

                    // Now add the chmod part
                    cmd = "chmod a+x " + cmd;

                    // And any configured shell prefix
                    String shell = org.webcat.core.Application.cmdShell();
                    if (shell != null && shell.length() > 0)
                    {
                        cmd = shell + cmd;
                        if (shell.charAt(shell.length() - 1) == '"')
                        {
                            cmd += "\"";
                        }
                    }

                    // Execute this command
                    try
                    {
                        log.info(cmd);
                        executeExternalCommand(cmd, file.getParentFile());
                    }
                    catch (Exception e)
                    {
                        log.error("attempting to execute command:\n" + cmd, e);
                    }
                }
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the message dispatcher used to send Message objects to users. If
     * no dispatcher is yet registered (by the Notifications subsystem,
     * primarily), then the default fallback message dispatcher is created so
     * that messages will be e-mailed.
     *
     * @return the message dispatcher used by the application
     */
    public synchronized IMessageDispatcher messageDispatcher()
    {
        if (messageDispatcher == null)
        {
            messageDispatcher = new FallbackMessageDispatcher();
        }

        return messageDispatcher;
    }


    // ----------------------------------------------------------
    /**
     * Sets the message dispatcher used to send Message objects to users.
     *
     * @param dispatcher the message dispatcher
     */
    public synchronized void setMessageDispatcher(IMessageDispatcher dispatcher)
    {
        messageDispatcher = dispatcher;
    }


    // ----------------------------------------------------------
    /**
     * Register all messaging protocols and initializes the system-wide
     * protocol settings object if it does not exist.
     */
    private void initializeMessagingSystem()
    {
        // Register Core messages.

        ApplicationStartupMessage.register();
        UnexpectedExceptionMessage.register();
    }


    // ----------------------------------------------------------
    /**
     * Gets the web API request handler so that other subsystems can add hooks
     * for their own entities to the external API.
     *
     * @return the {@link WebAPIRequestHandler}
     */
    public synchronized WebAPIRequestHandler webAPI()
    {
        return apiHandler;
    }


    // ----------------------------------------------------------
    /**
     * If the app is running as a servlet, this method checks the version
     * stamp stored in the {@link net.sf.webcat.WCServletAdaptor} to see if
     * it is the same as the "expected" version number stored in the Core
     * subsystem's properties file.  The "expected" version is captured at
     * subsystem build time from the most recent Bootstrap.jar build.
     * But because the Bootstrap.jar file performs the automatic update
     * operations, it cannot automatically update itself--instead, it must
     * be updated manually.  This check detects when a new release is
     * available, and notifies the administrator that a manual update is
     * needed.
     *
     * Instructions for performing a manual update are provided at:
     * <a href="http://web-cat.cs.vt.edu/WCWiki/UpdateBootstrapJar">
     * http://web-cat.cs.vt.edu/WCWiki/UpdateBootstrapJar</a>
     */
    private void checkBootstrapVersion()
    {
        String expectedVersion = configurationProperties().getProperty(
            "bootstrap.project.version");
        if (expectedVersion != null)
        {
            net.sf.webcat.WCServletAdaptor adaptor =
                net.sf.webcat.WCServletAdaptor.getInstance();
            // Bootstrapping is only enabled when running as a servlet,
            // so we can only check the version at that time
            if (adaptor != null)
            {
                String currentVersion = null;
                // Use reflection to get the current version, in case the
                // current bootstrap version is so old it doesn't even
                // support version retrieval!
                try
                {
                    currentVersion = (String)adaptor.getClass()
                        .getMethod("version").invoke(adaptor);
                }
                catch (Exception e)
                {
                    log.error("exception retrieving Bootstrap.jar version:", e);
                }

                if (!expectedVersion.equals(currentVersion))
                {
                    log.error("Bootstrap.jar is out of date: expected '"
                        + expectedVersion + "' but found '" + currentVersion
                        + "'");
                    sendAdminEmail( "Please update your Bootstrap.jar",
                        "During startup, Web-CAT detected that an older "
                        + "version of Bootstrap.jar\nis installed.  Web-CAT "
                        + "expected version '"
                        + expectedVersion
                        + "', but found '"
                        + currentVersion
                        + "'.\nThis jar provides Web-CAT's automatic update "
                        + "support and it can only be\nupdated manually.\n\n"
                        + "Please follow these instructions to download and "
                        + "install the latest\navailable version:\n\n"
                        + "http://web-cat.org/WCWiki/"
                        + "UpdateBootstrapJar\n");
                }
            }
        }
        else
        {
            log.error("Unable to read expected bootstrap.project.version");
            sendAdminEmail( "Unable to verify bootstrap version",
                "During startup, Web-CAT was unable to read the expected\n"
                + "bootstrap.project.version.  Your Web-CAT instance will "
                + "continue\nrunning normally.  Please contact the Web-CAT "
                + "team for assistance\nresolving this problem.\n");
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the path to the Web-CAT storage directory (where submissions and
     * object file stores are located).
     *
     * @return the path to the Web-CAT storage directory
     */
    public static File storageDirectory()
    {
        // TODO can we rename this property easily? Find all references to it
        // and pass it through this method instead.

        String path = configurationProperties().getProperty(
                "grader.submissiondir");
        return new File(path);
    }


    // ----------------------------------------------------------
    /**
     * Gets the path to the repository root area inside the Web-CAT storage
     * directory.
     *
     * @return the path to the repository root area
     */
    private static File repositoryRootDirectory()
    {
        return new File(storageDirectory(), "_Repositories");
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether a repository currently exists for the
     * specified object.
     *
     * @param object the object
     * @return true if the repository currently exists, false if it has not yet
     *     been created
     */
    public boolean repositoryExistsForObject(EOBase object)
    {
        if (object instanceof RepositoryProvider)
        {
            File fsDir = new File(repositoryRootDirectory(),
                    object.entityName() + "/" + object.apiId());

            return fsDir.exists();
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the path to the repository for the specified object. This path
     * points to a bare Git repository; to access the repository directly, use
     * the {@link GitUtilities#repositoryForObject(EOEnterpriseObject)} method
     * instead of this one, which also creates the repository on demand if it
     * does not yet exist.
     *
     * @param object the EO to get the repository location for
     * @return the path to the repository for the EO, or null if the EO is not
     *     a file store provider
     */
    public File repositoryPathForObject(EOBase object)
    {
        if (object instanceof RepositoryProvider)
        {
            File fsDir = new File(repositoryRootDirectory(),
                    object.entityName() + "/" + object.apiId());

            if (!fsDir.exists())
            {
                fsDir.mkdirs();
            }

            return fsDir;
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the path to the temporary working copy for the specified object.
     * This path will be used to determine where to checkout files in an EO's
     * file repository, so that they can be exposed for protocols such as
     * Web-DAV.
     *
     * @param object the EO to get the working copy location for
     * @return the path to the working copy for the EO, or null if the EO is
     *     not a file store provider
     */
    public File workingCopyPathForObject(EOBase object)
    {
        if (object instanceof RepositoryProvider)
        {
            File fsDir = new File(repositoryRootDirectory(),
                    "_WorkingCopies/" + object.entityName()
                    + "/" + object.apiId());

            return fsDir;
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets an instance of {@link RelativeTimestampFormatter} that performs
     * approximate date formatting relative to the current time.
     *
     * @return an instance of {@link RelativeTimestampFormatter} that performs
     *     approximate date formatting relative to the current time
     */
    public RelativeTimestampFormatter approximateRelativeTimestampFormatter()
    {
        if (approximateRelativeTimestampFormatter == null)
        {
            approximateRelativeTimestampFormatter =
                new RelativeTimestampFormatter(true);
        }

        return approximateRelativeTimestampFormatter;
    }


    // ----------------------------------------------------------
    /**
     * Gets an instance of {@link RelativeTimestampFormatter} that performs
     * exact date formatting relative to the current time.
     *
     * @return an instance of {@link RelativeTimestampFormatter} that performs
     *     exact date formatting relative to the current time
     */
    public RelativeTimestampFormatter exactRelativeTimestampFormatter()
    {
        if (exactRelativeTimestampFormatter == null)
        {
            exactRelativeTimestampFormatter =
                new RelativeTimestampFormatter(true);
        }

        return exactRelativeTimestampFormatter;
    }


    // ----------------------------------------------------------
    /**
     * Gets an instance of {@link FileSizeFormatter} that can be used to format
     * file sizes in components.
     *
     * @return an instance of {@link FileSizeFormatter}
     */
    public FileSizeFormatter fileSizeFormatter()
    {
        if (fileSizeFormatter == null)
        {
            fileSizeFormatter = new FileSizeFormatter();
        }

        return fileSizeFormatter;
    }


    // ----------------------------------------------------------
    private static void loadArchiveManagers()
    {
        @SuppressWarnings("unchecked")
        NSArray<String> handlers = (NSArray<String>)configurationProperties()
            .arrayForKey("Core.archive.handler.list");
        if (handlers != null)
        {
            ArchiveManager manager = ArchiveManager.getInstance();
            for (String name : handlers)
            {
                try
                {
                    manager.addHandler(
                        (IArchiveHandler)DelegatingUrlClassLoader
                        .getClassLoader().loadClass(name).newInstance());
                }
                catch (Exception e)
                {
                    log.error("Exception loading archive handler: ", e);
                }
            }
        }
    }


    //~ Instance/static variables .............................................

    // Force the ERXExtensions bundle to be initialized before this class by
    // referencing it here.
    @SuppressWarnings("unused")
    private static ERXExtensions forcedInitialization1 = null;
    @SuppressWarnings("unused")
    private static ERXProperties forcedInitialization2 = null;

    public static int userCount = 0;

    private static final int maxAttachmentSize = 100000; // bytes

    // Add more options here (e.g. for IIS, NSAPI, etc.), if necessary...
    private static final NSArray<String> HOST_NAME_KEYS = new NSArray<String>(
        new String[]{
            "x-webobjects-server-name", "server_name", "Host", "http_host"});

    // Add more options here (e.g. for IIS, NSAPI, etc.), if necessary...
    private static final NSArray<String> SERVER_PORT_KEYS =
        new NSArray<String>(new String[]{
            "x-webobjects-server-port", "SERVER_PORT"});

    private static String version;
    private static NSTimestamp startTime = new NSTimestamp();
    private static NSTimestamp dieTime = null;
    private static String deathMessage = null;
    private static WCConfigurationFile __properties;
    private static String __appIdentifier;
    private static SubsystemManager __subsystemManager;
    private static String cmdShell;
    private static Boolean defaultsToSecure;
    private static String urlHostPrefix;

    private static boolean staticHtmlResourcesNeedInitializing = true;
    private static boolean initializationComplete = false;
    private static boolean needsInstallation = true;

    private IMessageDispatcher messageDispatcher;
    private WebAPIRequestHandler apiHandler;
    private EOEditingContext errorLoggingContext;

    private RelativeTimestampFormatter approximateRelativeTimestampFormatter;
    private RelativeTimestampFormatter exactRelativeTimestampFormatter;
    private FileSizeFormatter fileSizeFormatter;

    private static java.lang.reflect.Method DESTROY_FORCIBLY;
    static
    {
        try
        {
            DESTROY_FORCIBLY = Process.class
                .getMethod("destroyForcibly", new Class<?>[] {});
        }
        catch (Exception e)
        {
            System.out.println("Cannot find Process.destroyForcibly() (only "
                + "available in Java 1.8+): " + e.getClass().getSimpleName()
                + ": " + e.getMessage());
        }
    }

    static Logger log = Logger.getLogger(Application.class);
    static Logger requestLog = Logger.getLogger(Application.class.getName()
        + ".requests");
}
