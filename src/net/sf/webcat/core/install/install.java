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

package net.sf.webcat.core.install;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;
import er.extensions.appserver.ERXDirectAction;
import er.extensions.foundation.ERXValueUtilities;
import net.sf.webcat.core.*;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 * The default direct action class for Web-CAT.
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class install
    extends ERXDirectAction
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new install object.
     *
     * @param aRequest The request to respond to
     */
    public install( WORequest aRequest )
    {
        super( aRequest );
    }


    //~ Public Constants ......................................................

    public static final String[] steps = new String[] {
        "pre-check",    // 1
        "license",      // 2
        "step&nbsp;1",  // 3
        "step&nbsp;2",  // 4
        "step&nbsp;3",  // 5
        "step&nbsp;4",  // 6
        "step&nbsp;5",  // 7
        "done!"         // 8
        };


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
        Application app = (Application)Application.application();
        WCConfigurationFile configuration =
        	Application.configurationProperties();
        if ( log.isDebugEnabled() )
        {
            log.debug( "defaultAction(): incoming configuration = "
                + request().formValues() );
            log.debug( configuration.configSettingsAsString() );
        }
        if ( !app.needsInstallation() )
        {
            return app.gotoLoginPage( context() );
        }
        int step = configuration.intForKey( "configStep" );
        if ( step > 0 && step <= steps.length
             && request().formValueForKey( "back" ) == null )
        {
            InstallPage oldPage = (InstallPage)
                pageWithName( InstallPage.class.getName() + step );
            oldPage.takeFormValues( request().formValues() );
            if ( oldPage.hasMessages() )
            {
                return oldPage;
            }
        }
        if ( step == steps.length
             && request().formValueForKey( "next" ) != null )
        {
            // The login action code here is commented out, since it causes
            // session restore errors for some reason.  Instead, we just
            // forward to the login page itself and require the user to
            // login to get in.

            // The following three lines are currently in InstallPage8
//            app.setNeedsInstallation( false );
//            app.notifyAdminsOfStartup();
//            configuration.remove( "configStep" );

//            String authDomainName =
//                configuration.getProperty( "authenticator.default" );
//            Session session = (Session)context().session();
//            try
//            {
//            session.defaultEditingContext().lock();
//            AuthenticationDomain domain =
//                AuthenticationDomain.authDomainByName( authDomainName );
//            log.debug( "domain = " + domain );
//            NSArray users = EOUtilities.objectsMatchingValues(
//                session.localContext(),
//                User.ENTITY_NAME,
//                new NSDictionary(
//                    new Object[] {
//                        configuration.getProperty( "AdminUsername" ),
//                        domain
//                    },
//                    new Object[] {
//                        User.USER_NAME_KEY,
//                        User.AUTHENTICATION_DOMAIN_KEY
//                    }
//                )
//            );
//            log.debug( "users = " + users );
//            if ( users.count() > 0 )
//            {
//                User admin = (User)users.objectAtIndex( 0 );
//                session.setUser( admin );
//                session.tabs.selectById( "AdminHome" );
//                //Application.application().saveSessionForContext( context() );
//                log.debug( "going to = " + session.currentPageName() );
//                return pageWithName( session.currentPageName() );
//            }
//            else
            {
                log.debug( "going to login page" );
//                return ( (Application)Application.application() )
//                    .gotoLoginPage( context() );
                return pageWithName(
                    net.sf.webcat.core.LoginPage.class.getName() );
            }
//            finally
//            {
//                session.defaultEditingContext().unlock();
//            }
        }
        else if ( step == 0 || request().formValueForKey( "next" ) != null )
        {
            step++;
        }
        else if ( step > 1 && request().formValueForKey( "back" ) != null )
        {
            step--;
        }
        configuration.setProperty( "configStep", Integer.toString( step ) );
        if ( log.isDebugEnabled() )
        {
            log.debug( "defaultAction(): configuration for install page = " );
            log.debug( configuration.configSettingsAsString() );
        }
        return pageWithName( InstallPage.class.getName() + step );
    }


    // ----------------------------------------------------------
    /**
     * Display the Web-CAT license.
     *
     * @return The LicensePage
     */
    public WOActionResults licenseAction()
    {
        return pageWithName( LicensePage.class.getName() );
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( install.class );
}
