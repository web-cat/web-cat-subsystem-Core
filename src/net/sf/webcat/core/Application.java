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
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import com.webobjects.woextensions.*;
import net.sf.webcat.dbupdate.*;
import er.extensions.*;
import java.io.File;
import java.net.*;
import java.util.Enumeration;
import java.util.Vector;
import javax.activation.*;
import javax.mail.internet.*;
import net.sf.webcat.archives.*;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * This is the main Web-CAT application class.  It takes care of all loading
 * and setup issues.  It also serves as a central source for accessing
 * properties and subsystem objects, and provides for centralized handling
 * of exception handling for the Web-CAT application.
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class Application
	extends er.extensions.ERXApplication
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
    public Application()
    {
        super();
        
        // Set UTF-8 encoding, to support localization
        WOMessage.setDefaultEncoding( "UTF-8" );
        WOMessage.setDefaultURLEncoding( "UTF-8" );
        ERXMessageEncoding.setDefaultEncoding( "UTF8" );
        ERXMessageEncoding.setDefaultEncodingForAllLanguages( "UTF8" );

        // We'll use plain WO sessions, even in a servlet context, since
        // restoring sessions through the WCServletSessionStore doesn't
        // really work.
        setSessionStoreClassName( "WOServerSessionStore" );
        NSNotificationCenter.defaultCenter().addObserver(
            this, 
            new NSSelector( "logNotification", 
                            new Class[] { NSNotification.class } ), 
            NSBundle.BundleDidLoadNotification,
            null
        );
        NSNotificationCenter.defaultCenter().addObserver(
            this, 
            new NSSelector( "logNotification", 
                            new Class[] { NSNotification.class } ), 
            NSBundle.LoadedClassesNotification,
            null
        );
        if ( log.isInfoEnabled() )
        {
            log.info( "Web-CAT v" + version()
                + "\nCopyright (C) 2006 Virginia Tech\n\n"
                + "Web-CAT comes with ABSOLUTELY NO WARRANTY; this is "
                + "free software\n"
                + "under the terms of the GNU General Public License.  "
                + "See:\n"
                + "http://www.gnu.org/licenses/gpl.html\n" );
            log.info( "Properties loaded from:" );
            NSArray dirs =
                ERXProperties.pathsForUserAndBundleProperties();
            for ( int i = 0; i < dirs.count(); i++ )
            {
                log.info( "\t" + dirs.objectAtIndex( i ) );
            }
            dirs = ERXProperties.optionalConfigurationFiles();
            if ( dirs == null )
            {
                log.info( "no optional configuration files specified." );
            }
            else
            {
                log.info( "Also loading properties from optional files:" );
                for ( int i = 0; i < dirs.count(); i++ )
                {
                    log.info( "\t" + dirs.objectAtIndex( i ) );
                }
            }
        }
        if ( log.isDebugEnabled() )
        {
            try
            {
                File here = new File( "." );
                log.debug( "current dir = " + here.getCanonicalPath() );
            }
            catch ( java.io.IOException e )
            {
                log.error( "exception checking cwd: ", e );
            }
        }

        // Present the first page via the default DirectAction.
        setDefaultRequestHandler(
            requestHandlerForKey( directActionRequestHandlerKey() ) );

        if ( configurationProperties().hasUsableConfiguration() )
        {
            log.debug( "initializing application" );
            initializeApplication();
            setNeedsInstallation( false );
            notifyAdminsOfStartup();
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * The main startup method for the application.
     *
     * @param argv The command line arguments
     */
    public static void main( String argv[] )
    {
        er.extensions.ERXApplication.main( argv, Application.class );
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
    public void setNeedsInstallation( boolean value )
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
        sendAdminEmail( null, null, true, "Web-CAT starting up",
            "Web-CAT is starting up at " + startTime,
            null );

    }


    // ----------------------------------------------------------
    /**
     * If installation has been completed, initialize all subsystems in the
     * application and prepare it for running.
     */
    public void initializeApplication()
    {
        updateStaticHtmlResources();
        
        loadArchiveManagers();
        WOEC.installWOECFactory();

        // Apply any pending database updates for the core
        UpdateEngine.instance().database().setConnectionInfoFromProperties(
                        configurationProperties() );
        UpdateEngine.instance().applyNecessaryUpdates(
                        new CoreDatabaseUpdates() );

        // Set the eo model delegator
        EOModelGroup.defaultGroup().setDelegate(
                new SubsystemEOMRedirector()
            );

        // register for database channel needed notification
        NSNotificationCenter.defaultCenter().addObserver(
                this, 
                new NSSelector( "createAdditionalDatabaseChannel", 
                                new Class[] { NSNotification.class } ), 
                EODatabaseContext.DatabaseChannelNeededNotification, 
                null
            );

        // log.debug( "models = " + EOModelGroup.defaultGroup() );

        //set up the SMTP server to use for sending Emails
        {
            // This is just support for legacy properties used by Web-CAT
            String host =
                configurationProperties().getProperty( "mail.smtp.host" );
            if ( host != null
                 && !host.equals( "" )
                 && "smtp".equals(
                     configurationProperties().getProperty( "SMTPHost" ) ) )
            {
                setSMTPHost( host );
            }
        }
        log.info( "Using SMTP host " + SMTPHost() );
        log.debug( "cmdShell = " + cmdShell() );

        // Remove all state login session data
        EOEditingContext ec = newPeerEditingContext();
        try
        {
            ec.lock();
            NSArray old_sessions = EOUtilities.objectsForEntityNamed(
                ec, LoginSession.ENTITY_NAME );
            for ( int i = 0; i < old_sessions.count(); i++ )
            {
                ec.deleteObject( (LoginSession)old_sessions.objectAtIndex( i ) );
            }
            ec.saveChanges();
        }
        finally
        {
            ec.unlock();
            releasePeerEditingContext( ec );
        }
        AuthenticationDomain.refreshAuthDomains();
        Language.refreshLanguages();

        NSLog.debug.setAllowedDebugLevel( NSLog.DebugLevelInformational );
        NSLog.allowDebugLoggingForGroups( NSLog.DebugGroupMultithreading );
        setAllowsConcurrentRequestHandling( true );
        
        // Ensure subsystems are all loaded
        subsystemManager();
        startTime = new NSTimestamp();
    }


    // ----------------------------------------------------------
    /**
     * Access the application's subsystem manager.
     * 
     * @return The subsystem manager
     */
    public SubsystemManager subsystemManager()
    {
        if ( __subsystemManager == null )
            __subsystemManager = new SubsystemManager( configurationProperties() );
        return __subsystemManager;
    }

    public void logNotification( NSNotification notification )
    {
        log.info( "notification posted: " + notification );
        log.info( "notification object: " + notification.object() );
    }


    // ----------------------------------------------------------
    /**
     * This notification callback is registered to respond when a
     * new database channel is needed.
     * 
     * @param notification The notification received
     */
    public void createAdditionalDatabaseChannel( NSNotification notification )
    {
        EODatabaseContext dbContext = (EODatabaseContext)notification.object();
        EODatabaseChannel dbChannel = new EODatabaseChannel( dbContext );
        if ( dbContext != null )
        {
            // consensus is that if you need more than 5 open channels,
            // you might want to re-think something in your code or model
            if ( dbContext.registeredChannels().count() < 5 )
            {
                if ( dbChannel != null )
                {
                    log.debug( "createAdditionalDatabaseChannel()" );
                    dbContext.registerChannel( dbChannel );
                }
            }
            else
            {
                log.error( "requesting > 5 database channels" );
            }
        }
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
            WOContext context
        )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "restoreSessionWithID( " + sessionID + " )" );
            log.debug( "from  here", new Exception( "here" ) );
        }
        WOSession result = super.restoreSessionWithID( sessionID, context );
        if ( log.isDebugEnabled() )
        {
            log.debug( "restored session = " + result );
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Redirect to the login page.
     * @param context the context of the request
     * @return The login page
     */
    public WORedirect gotoLoginPage( WOContext context )
    {
        WORedirect redirect = (WORedirect)pageWithName( "WORedirect", context );
        // TODO: fix this
        String dest = completeURLWithRequestHandlerKey(
            context, null, null, null, false, 0 );
        log.debug( "gotoLoginPage: " + dest );
        redirect.setUrl( dest );
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
            false );
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
            boolean   forceSecureSetting )
    {
        String dest = context.completeURLWithRequestHandlerKey(
                requestHandlerKey,
                aRequestHandlerPath,
                aQueryString,
                isSecure,
                somePort
            );
        WORequest request = ( context != null )
            ? context.request()
            : null;
        if ( request != null )
        {
            String host = hostName( request );
            log.debug( "prior to munging, dest = " + dest );
            log.debug( "host = " + host );
            dest = dest.replaceFirst( "^http(s)?://[^/]*",
                    "http"
                    + ( (  ( forceSecureSetting && isSecure )
                        || ( !forceSecureSetting && isSecure( request ) ) )
                        ? "s" : "" )
                    + "://" + host );
        }
        // TODO: find a way to fix this
//        String prefix1 = "http://WEB-CAT:";
//        if ( dest.startsWith( prefix1 ) )
//        {
//            dest = "http://web-cat.cs.vt.edu:"
//                + dest.substring( prefix1.length() );
//        }
//        String prefix2 = "http://web-cat:";
//        if ( dest.startsWith( prefix2 ) )
//        {
//            dest = "http://web-cat.cs.vt.edu:"
//                + dest.substring( prefix2.length() );
//        }
        log.debug( "link = " + dest );
        return dest;
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
    static public boolean isSecure( WORequest request )
    {
        /** require [valid_param] request != null; **/

        boolean isSecure = false;

        // The method of determining whether the request was via HTTPS depends 
        // on the adaptor / the web server.  
        
        // First we try and see if the request was made on the standard
        // https port
        String serverPort = null;
        for ( int i = 0; serverPort == null
                         && i < SERVER_PORT_KEYS.count(); i++ )
        {
            serverPort = request.headerForKey(
                SERVER_PORT_KEYS.objectAtIndex( i ) );
        }
        
        // Apache and some other web servers use this to indicate HTTPS mode.
        // This is much better as it does not depend on the port number used.
        String httpsMode = request.headerForKey( "https" );

        // If either the https header is 'on' or the server port is 443 then 
        // we consider this to be an HTTP request.
        isSecure = ( httpsMode != null  &&  httpsMode.equalsIgnoreCase( "on" ) )
                || ( serverPort != null  &&  serverPort.equals( "443" ) );

        return isSecure;
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
    static public String hostName( WORequest request )
    {
        /** require [valid_param] request != null; **/

        String hostName = null;
        for (int i = 0; (hostName == null) && (i < HOST_NAME_KEYS.count()); i++)
        {
            hostName = request.headerForKey( HOST_NAME_KEYS.objectAtIndex(i) );
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
            WOContext context
        )
    {
        log.debug( "handleSessionRestorationErrorInContext()" );
        return gotoLoginPage( context ).generateResponse();
    }


    // ----------------------------------------------------------
    public WOResponse handleSessionCreationErrorInContext(
            WOContext context
        )
    {
        log.debug( "handleSessionCreationErrorInContext()" );
        return super.handleSessionCreationErrorInContext( context );
    }


    // ----------------------------------------------------------
    /**
     * Returns the given component by name.
     * 
     * @param name    The name of the component to find
     * @param context The context for the retrieval
     * @return        The component
     */
    public WOComponent pageWithName( String name, WOContext context )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "pageWithName( "
                     + ( ( name == null ) ? "<null>" : name )
                     + ", "
                     + context
                     + " )" );
        }
        return super.pageWithName( name, context );
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
        log.info( "Forcing full garbage collection..." );
        long isFree = runtime.freeMemory();
        long wasFree;
        int iterations = 0;
        log.info( " wasFree: " + isFree + "..." );
        do
        {
            wasFree = isFree;
            runtime.gc();
            isFree = runtime.freeMemory();
        } while ( isFree > wasFree && iterations++ < iterationLimit );
        runtime.runFinalization();
        log.info( "isFree: " + isFree + ", total mem : " +
                  runtime.totalMemory() + "..." );
    }


    // ----------------------------------------------------------
    /**
     * Overrides parent implementation to add heap check and force
     * garbage collection when necessary.
     * @see er.extensions.ERXApplication#dispatchRequest(com.webobjects.appserver.WORequest)
     */
    public WOResponse dispatchRequest( WORequest aRequest )
    {
        Runtime runtime = Runtime.getRuntime();
        final int freeLimit = 100000;
        final int dieLimit  =   5000;
        if ( runtime.freeMemory() < freeLimit )
        {
            forceFullGarbageCollection();
            if ( runtime.freeMemory() < freeLimit )
            {
                if ( runtime.freeMemory() < dieLimit )
                {
                    sendAdminEmail( null, null, true,
                                    "Dying, out of memory...",
                                    "Cannot force GC to free more than "
                                    + freeLimit + " bytes.  Terminating.",
                                    null );
                    ERXApplication.erxApplication().killInstance();
                }
                else
                {
                    sendAdminEmail( null, null, true,
                                    "Running out of memory...",
                                    "Cannot force GC to free more than "
                                    + freeLimit + " bytes.", null );
                }
            }
        }
        if ( dieTime != null )
        {
            if ( dieTime.before( new NSTimestamp() ) )
            {
                ERXApplication.erxApplication().killInstance();
            }
        }
        else if ( isRefusingNewSessions() )
        {
            dieTime = ( new NSTimestamp() )
            .timestampByAddingGregorianUnits(
                    0,  // years
                    0,  // months
                    0,  // days
                    0,  // hours
                    5,  // minutes
                    0   // seconds
                );
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "dispatchRequest():\n\tmethod = " + aRequest.method() );
            log.debug( "\tqueryString = " + aRequest.queryString() );
            log.debug( "\trequestHandlerKey = " + aRequest.requestHandlerKey() );
            log.debug( "\trequestHandlerPath = " + aRequest.requestHandlerPath() );
            log.debug( "\turi = " + aRequest.uri() );
            log.debug( "\tcookies = " + aRequest.cookies() );
        }
        WOResponse result = super.dispatchRequest( aRequest );
        if ( log.isDebugEnabled() )
        {
            log.debug( "dispatchRequest() result:\n" + result  );
        }
        return result;
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
            "webobjects.user.dir" );
        if ( configFileName == null || configFileName.equals( "" ) )
        {
            configFileName = ERXSystem.getProperty( "user.dir" );
        }
        configFileName = configFileName.replace( '\\', '/' );
        if ( configFileName.length() > 0
             && !configFileName.endsWith( "/" ) )
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
        if ( __properties == null )
        {
            __properties = new WCConfigurationFile( configurationFileName() );
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
        if ( __appIdentifier == null )
        {
            String appIdentifierRaw =
                configurationProperties().getProperty( "coreApplicationIdentifier" );
            __appIdentifier = ( appIdentifierRaw == null )
                ? "[Web-CAT] "
                : "[" + appIdentifierRaw + "] ";
        }
        return __appIdentifier;
    }


    // ----------------------------------------------------------
    /**
     * Overrides default WO version to include XP as a supported
     * platform.
     * @see com.webobjects.appserver.WOApplication#_isForeignSupportedDevelopmentPlatform()
     */
    public boolean _isForeignSupportedDevelopmentPlatform()
    {
        return super._isForeignSupportedDevelopmentPlatform() ||
            isAdditionalForeignSupportedDevelopmentPlatform();
    }

    // ----------------------------------------------------------
    /**
     * Overrides default WO version to force deployment-mode behavior
     * until valid configuration has been reached.
     * @see com.webobjects.appserver.WOApplication#_rapidTurnaroundActiveForAnyProject()
     */
    public boolean _rapidTurnaroundActiveForAnyProject()
    {
        return staticHtmlResourcesNeedInitializing
            || super._rapidTurnaroundActiveForAnyProject();
    }

    // ----------------------------------------------------------
    /**
     * Check for Windows XP
     * @return true when running on XP
     */
    protected boolean isAdditionalForeignSupportedDevelopmentPlatform()
    {
        String s = System.getProperty( "os.name" );
        return s != null && s.equals( "Windows XP" );
    }


    // ----------------------------------------------------------
    /**
     * Check to see if we are running on any flavor of Windows.
     * @return true when running on Windows
     */
    public static boolean isRunningOnWindows()
    {
        String s = System.getProperty( "os.name" );
        return s != null  &&  s.indexOf( "Windows" ) >= 0;
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
     * @see er.extensions.ERXApplication#reportException(java.lang.Throwable, com.webobjects.foundation.NSDictionary)
     */
    public WOResponse reportException( Exception    exception,
                                       NSDictionary extraInfo,
                                       WOContext    context )
    {
        Throwable t = exception instanceof NSForwardException
            ? ( (NSForwardException) exception ).originalException()
            : exception;
        emailExceptionToAdmins( t, extraInfo, context, null );
        // Return a "clean" error page
        WOComponent errorPage =
            pageWithName( ErrorPage.class.getName(), context );
        errorPage.takeValueForKey( t, "exception" );
        return errorPage.generateResponse();
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
    public WOResponse handleException( Exception exception,
                                       WOContext context )
    {
        try
        {
            // We first want to test if we ran out of memory. If so we need
            // to quit ASAP.
            handlePotentiallyFatalException( exception );

            // Not a fatal exception, business as usual.
            NSDictionary extraInfo =
                extraInformationForExceptionInContext( exception, context );
            WOResponse response =
                reportException( exception, extraInfo, context );
            if ( response == null )
                response = super.handleException( exception, context );
            return response;
        }
        catch ( Throwable t )
        {
            log.error( "handleException failed", t );
            return super.handleException( exception, context );
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
    public WOResponse handlePageRestorationErrorInContext( WOContext context )
    {
        return pageWithName(
            WCPageRestorationErrorPage.class.getName(), context )
            .generateResponse();
    }


    // ----------------------------------------------------------
    public NSMutableDictionary extraInformationForExceptionInContext(
        Exception exception, WOContext context )
    {
        NSMutableDictionary result =
            super.extraInformationForExceptionInContext( exception, context );
        if (  context != null
           && context.hasSession()
           && context.session() instanceof Session )
        {
            Session s = (Session)context.session();
            TabDescriptor currentTab =
                ( s.tabs == null )
                    ? null
                    : s.tabs.selectedDescendant();
            result.setObjectForKey(
                ( currentTab == null )
                    ? "null"
                    : currentTab.printableTabLocation(),
                "current tab" );
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Sends an e-mail message, optionally with attachments.  If
     * no domain is specified for the To: address, then the
     * property mail.default.domain is used.  If the To: address
     * is null or if toAdmins is true, then the message is also
     * sent to the administrator notification list.
     * 
     * @param to          the To: address
     * @param users       Additional list of User objects to add to the
     *                    recipient list
     * @param toAdmins    if true, send to the admin notification list too
     * @param subject     the subject line
     * @param body        the text of the e-mail message
     * @param attachments if non-null, a vector of string file names to
     *                    send as attachments on the message
     */
    static public void sendAdminEmail( String  to,
                                       NSArray users,
                                       boolean toAdmins,
                                       String  subject, 
                                       String  body,
                                       Vector  attachments )
    {
        try
        {
            // Define message
            javax.mail.Message message =
                new javax.mail.internet.MimeMessage(
                        javax.mail.Session.getInstance( configurationProperties(), null )
                    );
            message.setFrom( new InternetAddress(
                configurationProperties().getProperty( "coreAdminEmail" ) ) );
            if ( to != null )
            {
                String defaultDomain =
                    configurationProperties().getProperty( "mail.default.domain" );
                if ( to.indexOf( '@' ) == -1  &&  defaultDomain != null )
                {
                    to += "@" + defaultDomain;
                }
                message.addRecipient( javax.mail.Message.RecipientType.TO, 
                                      new InternetAddress( to ) );
            }
            if ( to == null || toAdmins )
            {
                String adminList =
                    configurationProperties().getProperty( "adminNotifyAddrs" );
                if ( adminList == null )
                {
                    adminList = configurationProperties().getProperty( "coreAdminEmail" );
                }
                if ( adminList == null )
                {
                    log.error( "No bound admin e-mail addresses.  "
                               + "Cannot send message:\n"
                               + "Subject: " + subject + "\n"
                               + "Message:\n" + body );
                    return;
                }
                String[] admins = adminList.split( "\\s*,\\s*" );
                for ( int i = 0; i < admins.length; i++ )
                {
                    message.addRecipient(
                            javax.mail.Message.RecipientType.TO, 
                            new InternetAddress( admins[i] )
                        );
                }
            }
            if ( users != null )
            {
                for ( int i = 0; i < users.count(); i++ )
                {
                    User thisUser = (User)users.objectAtIndex( i );
                    message.addRecipient(
                            javax.mail.Message.RecipientType.TO, 
                            new InternetAddress( thisUser.email() )
                        );
                }
            }
            message.setSubject( appIdentifier() + subject );

            // Create the message part
            javax.mail.BodyPart messageBodyPart = new MimeBodyPart();

            // Fill the message
            messageBodyPart.setText( body );

            // Create a Multipart
            javax.mail.Multipart multipart = new MimeMultipart();

            // Add part one
            multipart.addBodyPart( messageBodyPart );

            //
            // The next parts are attachments
            //
            if ( attachments != null )
            {
                for ( Enumeration e = attachments.elements();
                      e.hasMoreElements(); )
                {
                    String filename = (String)e.nextElement();
                    File file = new File( filename );

                    // Create another body part
                    messageBodyPart = new MimeBodyPart();

                    // Don't include files bigger than this as e-mail
                    // attachments
                    if ( file.length() < maxAttachmentSize )
                    {
                        // Get the attachment
                        DataSource source = new FileDataSource( file );

                        // Set the data handler to the attachment
                        messageBodyPart.setDataHandler(
                            new DataHandler( source ) );

                        // Set the filename
                        messageBodyPart.setFileName( file.getName() );
                    }
                    else
                    {
                        // Fill the message
                        messageBodyPart.setText(
                                "File "
                                + file.getName()
                                + " has been omitted from this message ("
                                + file.length()
                                + " bytes)\n"
                            );
                    }

                    // Add attachment
                    multipart.addBodyPart( messageBodyPart );
                }
            }

            // Put parts in message
            message.setContent( multipart );

            // Send the message
            javax.mail.Transport.send( message );
        }
        catch ( Exception e )
        {
            log.error( "Exception sending mail message:\n", e );
            log.error( "unsent message:\nTo: "
                       + ( to == null ? "null" : to )
                       + "\ntoAdmins: " + toAdmins
                       + "\nSubject: " + ( subject == null ? "null" : subject )
                       + "\nBody:\n" + ( body == null ? "null" : body )
                       );
        }
    }


    // ----------------------------------------------------------
    /**
     * Sends a text e-mail message to the specified recipient.
     * 
     * @param to          the to address
     * @param subject     the subject line
     * @param messageText the body of the message
     */
    static public void sendSimpleEmail( String to,
                                        String subject, 
                                        String messageText )
    {
        sendAdminEmail( to, null, false, subject, messageText, null );
    }


    // ----------------------------------------------------------
    /**
     * Sends an exception notification report by e-mail to the
     * administor notification list.
     * 
     * @param anException the exception that occurred
     * @param aContext    the context in which the exception occurred
     * @param msg         the text message accompanying the exception info
     */
    static public void emailExceptionToAdmins( Throwable    anException,
                                               WOContext    aContext,
                                               String       msg )
    {
        emailExceptionToAdmins(
            anException,
            ( anException instanceof Exception )
                ? erxApplication().extraInformationForExceptionInContext(
                  (Exception)anException, aContext )
                : null,
            aContext,
            msg );
    }


    // ----------------------------------------------------------
    /**
     * Generate a string of extra info for the given context (can be used
     * for debugging output).
     * @param context the context for this request (can be null, if desired)
     * @return the extra information in a formatted, human-readable,
     *         multi-line string
     */
    public static String extraInfoForContext( WOContext context )
    {
        if (context != null && context.page() != null )
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append( "CurrentPage = " );
            buffer.append( context.page().name() );
            buffer.append( "\n" );
            
            if ( context.component() != null )
            {
                buffer.append( "CurrentComponent = " );
                buffer.append( context.component().name() );
                buffer.append( "\n" );
                if ( context.component().parent() != null )
                {
                    WOComponent component = context.component();
                    while ( component.parent() != null )
                    {
                        component = component.parent();
                        buffer.append( "    parent = " );
                        buffer.append( component.name() );
                        buffer.append( "\n" );
                    }
                }
            }
            buffer.append( "uri = " );
            buffer.append( context.request().uri() );
            buffer.append( "\n" );
            if ( context.hasSession() )
            {
                if ( context.session().statistics() != null )
                {
                    buffer.append( "PreviousPageList = " );
                    buffer.append( context.session().statistics() );
                    buffer.append( "\n" );
                }
                if ( context.session() instanceof Session )
                {
                    Session s = (Session)context.session();
                    TabDescriptor currentTab =
                        ( s.tabs == null )
                            ? null
                            : s.tabs.selectedDescendant();
                    buffer.append( "current tab = " );
                    buffer.append( ( currentTab == null )
                            ? "null"
                            : currentTab.printableTabLocation() );
                    buffer.append( "\n" );
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
     * Sends an exception notification report by e-mail to the
     * administor notification list.
     * 
     * @param anException the exception that occurred
     * @param extraInfo   dictionary of extra information about what was
     *                    happening when the exception was thrown
     * @param aContext    the context in which the exception occurred
     * @param msg         the text message accompanying the exception info
     */
    static public void emailExceptionToAdmins( Throwable    anException,
                                               NSDictionary extraInfo,
                                               WOContext    aContext,
                                               String       msg )
    {
        String body = informationForExceptionInContext( anException,
                                                        extraInfo,
                                                        aContext );
        if ( msg != null )
        {
            body = msg + "\n\n" + body;
        }
        sendAdminEmail( null,
                        null,
                        true,
                        "Unexpected Exception",
                        body,
                        null );
        log.error( body );
        if ( anException instanceof java.io.IOException )
        {
            String exceptionMsg = anException.getMessage();
            if ( exceptionMsg != null
                 && exceptionMsg.indexOf( "Too many open" ) >= 0 )
            {
                log.fatal( "Aborting: " + exceptionMsg );
                ERXApplication.erxApplication().killInstance();
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Method to assemble information on the exception, including the
     * message, stack trace, the name of the component which contained
     * the error (or caused it), and any session information required.
     * @param anException the exception that occurred
     * @param extraInfo   dictionary of extra information about what was
     *                    happening when the exception was thrown
     * @param aContext    the context in which the exception occurred
     * @return a printable description of the error
     */
    public static String informationForExceptionInContext(
            Throwable    anException,
            NSDictionary extraInfo,
            WOContext    aContext )
    {
       Session s = ( aContext != null  &&  aContext.hasSession() )
                       ? (Session)aContext.session()
                       : null;
       // Set up a buffer for the content
       StringBuffer errorBuffer = new StringBuffer();
       
       if ( s != null  &&  s.primeUser() != null )
       {
           // Get the pid of the user of the session
           errorBuffer.append( "User     :   " );
           errorBuffer.append( s.primeUser().nameAndUid() );
       }

       // Get the date and time for the exception
       errorBuffer.append( "\nDate/Time:   " );
       errorBuffer.append( ( new NSTimestamp() ).toString() );

       if ( aContext != null &&
            aContext.component() != null
            )
       {
           // Get the current component
           errorBuffer.append( "\nComponent:   " );
           String name = aContext.component().name();
           if ( name == null )
           {
               name = aContext.component().getClass().getName();
           }
           errorBuffer.append( name );
       }

       // Get the session associated (if any)
       if ( s != null ) {
           errorBuffer.append(
                "\n\nSession Information:\n--------------------\n" );
           errorBuffer.append( "SessionID: " + s.sessionID() );
           // Add anything else you need here ...
       }
       
       if ( anException != null )
       {
           // Get the full message for the exception
           errorBuffer.append( "\n\nException:\n----------\n" );
           errorBuffer.append( anException.getClass().getName() );
           errorBuffer.append( ":\n" );
           errorBuffer.append( anException.getMessage() );

           if ( extraInfo != null )
           {
               errorBuffer.append(
                   "\n\nExtra Information:\n--------------------\n" );
               for ( Enumeration e = extraInfo.keyEnumerator();
                     e.hasMoreElements(); )
               {
                   Object key = e.nextElement();
                   Object value = extraInfo.objectForKey( key );
                   errorBuffer.append( key );
                   errorBuffer.append( "\t= " );
                   errorBuffer.append( value );
                   errorBuffer.append( '\n' );
               }
           }

           // Get the stack trace for the exception
           errorBuffer.append( "\n\nAbbreviated Stack Trace:\n"
                               + "------------------------" );
           WOExceptionParser exParser = new WOExceptionParser( anException );
           Enumeration traceEnum =
               ( exParser.stackTrace() ).objectEnumerator();

           // Append each trace line
           while ( traceEnum.hasMoreElements() )
           {
               WOParsedErrorLine aLine =
                   (WOParsedErrorLine)traceEnum.nextElement();
               String fileName = aLine.fileName();
               if ( ! fileName.startsWith( "WO" ) &&
                    ! fileName.startsWith( "NS" ) )
               {
                   errorBuffer.append( "\nat " + aLine.methodName() + "("
                                       + aLine.fileName() + ":"
                                       + aLine.lineNumber() + ")" );
               }
           }

           // Get the stack trace for the exception
           errorBuffer.append( "\n\nFull Stack Trace:\n-----------------" );
           traceEnum =
               ( exParser.stackTrace() ).objectEnumerator();

           // Append each trace line
           while ( traceEnum.hasMoreElements() )
           {
               WOParsedErrorLine aLine =
                   (WOParsedErrorLine)traceEnum.nextElement();
               errorBuffer.append( "\nat " + aLine.methodName() + "("
                                   + aLine.fileName() + ":"
                                   + aLine.lineNumber() + ")" );
           }
       }
       else
       {
           if ( extraInfo != null )
           {
               errorBuffer.append(
                   "\n\nExtra Information:\n--------------------\n" );
               for ( Enumeration e = extraInfo.keyEnumerator();
                     e.hasMoreElements(); )
               {
                   Object key = e.nextElement();
                   Object value = extraInfo.objectForKey( key );
                   errorBuffer.append( key );
                   errorBuffer.append( "\t= " );
                   errorBuffer.append( value );
                   errorBuffer.append( '\n' );
               }
           }
       }

       // Return the information
       return errorBuffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * Creates a new peer editing context, typically used to make
     * changes outside of a session's editing context.
     * @return the new editing context
     */
    public static EOEditingContext newPeerEditingContext()
    {
        EOEditingContext result = er.extensions.ERXEC.newEditingContext();
        result.setUndoManager( null );
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Used to get rid of a peer editing context that will no longer be used.
     * @param ec the editing context to release
     */
    public static void releasePeerEditingContext( EOEditingContext ec )
    {
        ec.dispose();
    }


    // ----------------------------------------------------------
    public void refuseNewSessions( boolean arg0 )
    {
        boolean isDirectConnectEnabled = isDirectConnectEnabled();
        if ( arg0 )
        {
            setDirectConnectEnabled( false );
            int timeToKill = configurationProperties().intForKey( "ERTimeToKill" );
            if ( timeToKill > 0 )
            {
                dieTime = ( new NSTimestamp() )
                    .timestampByAddingGregorianUnits(
                                    0, 0, 0, 0, 0, timeToKill );
            }
        }
        else
        {
            dieTime = null;
        }
        super.refuseNewSessions( arg0 );
        setDirectConnectEnabled( isDirectConnectEnabled );
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
    public String deathMessage()
    {
        if ( dieTime != null && deathMessage == null )
        {
            StringBuffer buffer = new StringBuffer( 200 );
            buffer.append( "<b>Immediate shutdown:</b> " );
            buffer.append( "Web-CAT will be going off-line at " );
            NSTimestampFormatter formatter =
                new NSTimestampFormatter( "%I:%M%p" );
            java.text.FieldPosition pos = new java.text.FieldPosition( 0 );
            formatter.format( dieTime, buffer, pos );
            buffer.append( ".  Save your work and logout." );
            deathMessage = buffer.toString();
        }
        return deathMessage;
    }


    // ----------------------------------------------------------
    public static String cmdShell()
    {
        if ( cmdShell == null )
        {
            cmdShell = configurationProperties().getProperty( "cmdShell" );
            if ( cmdShell == null )
            {
                if ( isRunningOnWindows() )
                {
                    cmdShell = "cmd /c";
                }
                else
                {
                    cmdShell = "sh -c \"";
                }
            }
            int len = cmdShell.length();
            if ( len > 0
                 && cmdShell.charAt( len - 1 ) != ' '
                 && cmdShell.charAt( len - 1 ) != '"' )
            {
                cmdShell += " ";
            }
        }
        return cmdShell;
    }


    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see er.extensions.ERXApplication#killInstance()
     */
    public void killInstance()
    {
        String killAction = configurationProperties().getProperty( "coreKillAction" );
        if ( killAction == null )
        {
            super.killInstance();            
        }
        else
        {
            String cmd = cmdShell() + killAction;
            Process proc = null;
            try
            {
                proc = Runtime.getRuntime().exec( cmd );
                proc.waitFor();
            }
            catch ( Exception e )
            {
                // stopped by timeout
                if ( proc != null )
                {
                    proc.destroy();
                }
                log.fatal( "exception executing kill action '" + cmd + "'", e );
                super.killInstance();
            }
        }
    }


    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see com.webobjects.appserver.WOApplication#sessionStore()
     */
    public WOSessionStore sessionStore()
    {
        WOSessionStore store = super.sessionStore();
        if ( log.isDebugEnabled() )
        {
            log.debug( "sessionStore() class = " + store.getClass().getName() );
            log.debug( "sessionStore() = " + store );
        }
        return store;
    }


    // ----------------------------------------------------------
    public String version()
    {
        if ( version == null )
        {
            WCConfigurationFile config = properties();
            int major = 0;
            int minor = 0;
            int revision = 0;
            int date = 0;
            for ( Enumeration keys = System.getProperties().keys();
                  keys.hasMoreElements(); )
            {
                String key = keys.nextElement().toString();
                if ( key.endsWith( "version.major" ) )
                {
                    int thisMajor = config.intForKey( key );
                    if ( thisMajor > major )
                    {
                        major = thisMajor;
                    }
                }
                else if ( key.endsWith( "version.minor" ) )
                {
                    int thisMinor = config.intForKey( key );
                    if ( thisMinor > minor )
                    {
                        minor = thisMinor;
                    }
                }
                else if ( key.endsWith( "version.revision" ) )
                {
                    int thisRevision = config.intForKey( key );
                    if ( thisRevision > revision )
                    {
                        revision = thisRevision;
                    }
                }
                else if ( key.endsWith( "version.date" ) )
                {
                    int thisDate = config.intForKey( key );
                    if ( thisDate > date )
                    {
                        date = thisDate;
                    }
                }
            }
            version =   "" + config.intForKey( "webcat.version.major" ) + "."
                + config.intForKey( "webcat.version.minor" ) + "."
                + config.intForKey( "webcat.version.revision" ) + "/"
                + major + "." + minor + "." + revision + "." + date;
        }
        return version;
    }


    // ----------------------------------------------------------
    public String sessionStoreClassName()
    {
        String result = super.sessionStoreClassName();
        if ( log.isDebugEnabled() )
            log.debug( "sessionStoreClassName() = " + result );
        return result;
    }


    // ----------------------------------------------------------
    public WOResourceManager createResourceManager()
    {
        return new WCResourceManager();
    }


    // ----------------------------------------------------------
    private void updateStaticHtmlResources()
    {
        if ( net.sf.webcat.WCServletAdaptor.getInstance() == null )
        {
            // If we're not running as a servlet, then there's no updating
            // to do
            return;
        }

        File woaDir = configurationProperties().file().getParentFile();
        String lastUpdated = configurationProperties().getProperty(
            "static.HTML.date", "00000000" );
        File appBase = woaDir.getParentFile().getParentFile();
        log.debug( "appBase = " + appBase );
        log.debug( "appBase = " + appBase.getAbsolutePath() );
        File frameworkDir =
            new File( woaDir, "Contents/Frameworks/Library/Frameworks" );
        File[] framework = frameworkDir.listFiles();
        for ( int i = 0; i < framework.length; i++ )
        {
            log.debug( "Checking for static html resources in framework => "
                + framework[i].getName() );
            File webServerResources =
                new File( framework[i], "WebServerResources" );
            if ( webServerResources.isDirectory() )
            {
                String frameworkName = framework[i].getName();
                frameworkName = frameworkName.substring( 0,
                    frameworkName.length() - ".framework".length() );
                String frameworkLastUpdated = configurationProperties().
                    getProperty( frameworkName + ".version.date", "00000000" );
                if ( lastUpdated.compareTo( frameworkLastUpdated ) <= 0 )
                {
                    log.info(
                        "Copying static html resources from framework => "
                        + framework[i].getName() );
                    try
                    {
                        File target = new File( appBase, framework[i].getName()
                            + "/" + webServerResources.getName() );
                        if ( target.exists() )
                        {
                            if ( target.isDirectory() )
                            {
                                net.sf.webcat.archives.FileUtilities
                                    .deleteDirectory( target );
                            }
                            else
                            {
                                target.delete();
                            }
                        }
                        target.mkdirs();
                        net.sf.webcat.archives.FileUtilities
                            .copyDirectoryContentsIfNecessary(
                                webServerResources, target );
                    }
                    catch ( java.io.IOException e )
                    {
                        log.error(
                            "Exception copying static html resource from '"
                            + webServerResources + "' to '" + appBase + "'",
                            e );
                    }
                }
            }
        }

        // Attempt to update the "last saved" info
        NSTimestampFormatter formatter = new NSTimestampFormatter( "%Y%m%d" );
        configurationProperties().setProperty( "static.HTML.date",
            formatter.format( new NSTimestamp() ) );
        if ( !configurationProperties().attemptToSave() )
        {
            log.warn( "Could not save configuration settings to "
                + configurationProperties().file() );
        }

        String staticHtmlBase = configurationProperties().getProperty(
            "static.html.baseURL" );
        if ( staticHtmlBase == null )
        {
            staticHtmlBase = configurationProperties().getProperty(
                "base.url" );
            if ( staticHtmlBase != null )
            {
                if ( staticHtmlBase.endsWith( ".woa" ) )
                {
                    int loc = staticHtmlBase.lastIndexOf( '/' );
                    if ( loc > 0 )
                    {
                        staticHtmlBase = staticHtmlBase.substring( 0, loc );
                    }
                }
                if ( staticHtmlBase.endsWith( "WebObjects" ) )
                {
                    int loc = staticHtmlBase.lastIndexOf( '/' );
                    if ( loc > 0 )
                    {
                        staticHtmlBase = staticHtmlBase.substring( 0, loc );
                    }
                }
                if ( !staticHtmlBase.endsWith( "/" ) )
                {
                    staticHtmlBase = staticHtmlBase + "/";
                }
            }
        }
        if ( staticHtmlBase != null )
        {
            log.debug(
                "attempting to set frameworks Base URL = " + staticHtmlBase );
            setFrameworksBaseURL( staticHtmlBase );
            staticHtmlResourcesNeedInitializing = false;
            // Dump any cached data using the previous frameworks base url
            resourceManager().flushDataCache();
        }
        log.debug( "frameworks Base URL = " + frameworksBaseURL() );
    }


    // ----------------------------------------------------------
    private static void loadArchiveManagers()
    {
        NSArray handlers = configurationProperties().arrayForKey(
            "Core.archive.handler.list" );
        if ( handlers != null )
        {
            ArchiveManager manager = ArchiveManager.getInstance();
            for ( int i = 0; i < handlers.count(); i++ )
            {
                String name = (String)handlers.objectAtIndex( i );
                try
                {
                    manager.addHandler(
                        (IArchiveHandler)DelegatingUrlClassLoader
                        .getClassLoader().loadClass( name ).newInstance() );
                }
                catch ( Exception e )
                {
                    log.error( "Exception loading archive handler: ", e );
                }
            }
        }
    }


    //~ Instance/static variables .............................................

    // Force the ERXExtensions bundle to be initialized before this class by
    // referencing it here.
    private static er.extensions.ERXExtensions forcedInitialization1 = null;
    private static er.extensions.ERXProperties forcedInitialization2 = null;

    public static int userCount = 0;

    private static final int maxAttachmentSize = 100000; // bytes

    // Add more options here (e.g. for IIS, NSAPI, etc.), if neccessary...
    private static final NSArray HOST_NAME_KEYS = new NSArray(new Object[]
        {"x-webobjects-server-name", "server_name", "Host", "http_host"});
        
    // Add more options here (e.g. for IIS, NSAPI, etc.), if neccessary...
    private static final NSArray SERVER_PORT_KEYS = new NSArray(new Object[]
        {"x-webobjects-server-port", "SERVER_PORT"});

    private static String version;
    private static NSTimestamp startTime = new NSTimestamp();
    private static NSTimestamp dieTime = null;
    private static String deathMessage = null;
    private static WCConfigurationFile __properties;
    private static String __appIdentifier;
    private static SubsystemManager __subsystemManager;
    private static String cmdShell;
    
    private boolean needsInstallation = true;
    private boolean staticHtmlResourcesNeedInitializing = true;

    static Logger log = Logger.getLogger( Application.class );
}
