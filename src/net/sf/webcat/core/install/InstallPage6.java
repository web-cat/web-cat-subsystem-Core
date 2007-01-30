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

package net.sf.webcat.core.install;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import net.sf.webcat.core.*;

import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Implements the login UI functionality of the system.
 *
 *  @author Stephen Edwards
 *  @version $Id$
 */
public class InstallPage6
    extends InstallPage
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new PreCheckPage object.
     * 
     * @param context The context to use
     */
    public InstallPage6( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public boolean usingDatabaseAuthentication = true;
    public boolean canSetPassword              = true;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public int stepNo()
    {
        return 6;
    }


    // ----------------------------------------------------------
    public void setDefaultConfigValues( WCConfigurationFile configuration )
    {
        String username = configuration.getProperty( "AdminUsername" );
        String authDomainName =
            configuration.getProperty( "authenticator.default" );
        String className = configuration.getProperty(
            "authenticator." + authDomainName );
        usingDatabaseAuthentication = className == null
            || className.equals( DatabaseAuthenticator.class.getName() );
        canSetPassword = usingDatabaseAuthentication;
        if ( authDomainName != null && !authDomainName.equals( "" ) )
        {
            EOEditingContext ec = Application.newPeerEditingContext();
            AuthenticationDomain domain =
                AuthenticationDomain.authDomainByName( authDomainName );
            canSetPassword = domain.authenticator().canChangePassword();
            if ( username != null && !username.equals( "" ) )
            {
                NSArray users = EOUtilities.objectsMatchingValues(
                    ec,
                    User.ENTITY_NAME,
                    new NSDictionary(
                        new Object[] {
                            username,
                            domain
                        },
                        new Object[] {
                            User.USER_NAME_KEY,
                            User.AUTHENTICATION_DOMAIN_KEY
                        }
                    )
                );
                if ( users.count() > 0 )
                {
                    if ( users.count() > 1 )
                    {
                        error( "Multiple accounts with the user name '"
                            + username + "' detected!" );
                    }
                    User admin = (User)users.objectAtIndex( 0 );
                    if ( admin.firstName() != null )
                    {
                        setConfigDefault( configuration, "AdminFirstName",
                            admin.firstName() );
                    }
                    if ( admin.lastName() != null )
                    {
                        setConfigDefault( configuration, "AdminLastName",
                            admin.lastName() );
                    }
                    if ( admin.email() != null )
                    {
                        setConfigDefault( configuration, "coreAdminEmail",
                            admin.email() );
                    }
                    if ( canSetPassword && admin.password() != null )
                    {
                        setConfigDefault( configuration, "AdminPassword",
                            admin.password() );
                    }
                }
            }
            Application.releasePeerEditingContext( ec );
        }
    }


    // ----------------------------------------------------------
    public void takeFormValues( NSDictionary formValues )
    {
        WCConfigurationFile configuration =
            Application.configurationProperties();
        if ( log.isDebugEnabled() )
        {
            log.debug( "takeFormValues(): initial config = " );
            log.debug( configuration.configSettingsAsString() );
        }
        String email = storeFormValueToConfig( formValues, "coreAdminEmail",
            "Please specify the administrator's e-mail address." );
        storeFormValueToConfig( formValues, "adminNotifyAddrs", null );
        String authDomainName =
            configuration.getProperty( "authenticator.default" );
        String username =
            storeFormValueToConfig( formValues, "AdminUsername", 
                "Please specify the administrator's user name." );
        if ( log.isDebugEnabled() )
        {
            log.debug( "takeFormValues(): middle = " );
            log.debug( configuration.configSettingsAsString() );
        }
        if ( authDomainName == null || authDomainName.equals( "" ) )
        {
            error( "Cannot identify default institution's "
                + "authentication configuration." );
        }
        else if ( username != null && !hasMessages() )
        {
            EOEditingContext ec = Application.newPeerEditingContext();
            AuthenticationDomain domain =
                AuthenticationDomain.authDomainByName( authDomainName );
            NSArray users = EOUtilities.objectsMatchingValues(
                ec,
                User.ENTITY_NAME,
                new NSDictionary(
                    new Object[] {
                        username,
                        domain
                    },
                    new Object[] {
                        User.USER_NAME_KEY,
                        User.AUTHENTICATION_DOMAIN_KEY
                    }
                )
            );
            User admin;
            if ( users.count() > 0 )
            {
                admin = (User)users.objectAtIndex( 0 );
                admin.setEmail( email );
                String first = extractFormValue( formValues, "AdminFirstName" );
                if ( first != null && !first.equals( "" ) )
                {
                    admin.setFirstName( first );
                }
                String last = extractFormValue( formValues, "AdminLastName" );
                if ( last != null && !last.equals( "" ) )
                {
                    admin.setLastName( last );
                }
                ec.saveChanges();
            }
            else
            {
                String password =
                    storeFormValueToConfig( formValues, "AdminPassword", 
                    "An administrator password is required." );
                if ( authDomainName.equals(
                    DatabaseAuthenticator.class.getName() )
                    && ( password == null || password.equals( "" ) ) )
                {
                    // Don't need this anymore, since the error message is
                    // posted by storeFormValuesToConfig() above.

                    // errorMessage( "An administrator password is required." );
                }
                else
                {
                    admin = User.createUser(
                        username, password, domain, (byte)100, ec);
                    admin.setEmail( email );
                    String first = extractFormValue( formValues, "AdminFirstName" );
                    if ( first != null && !first.equals( "" ) )
                    {
                        admin.setFirstName( first );
                    }
                    String last = extractFormValue( formValues, "AdminLastName" );
                    if ( last != null && !last.equals( "" ) )
                    {
                        admin.setLastName( last );
                    }
                    ec.saveChanges();
                }
            }
            Application.releasePeerEditingContext( ec );            
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "takeFormValues(): near end = " );
            log.debug( configuration.configSettingsAsString() );
        }
        if ( !hasMessages() )
        {
            configuration.remove( "AdminFirstName" );
            configuration.remove( "AdminLastName" );
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "takeFormValues(): finals = " );
            log.debug( configuration.configSettingsAsString() );
        }
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( InstallPage6.class );
}
