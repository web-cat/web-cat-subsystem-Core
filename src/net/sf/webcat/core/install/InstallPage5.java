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
public class InstallPage5
    extends InstallPage
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new PreCheckPage object.
     * 
     * @param context The context to use
     */
    public InstallPage5( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................



    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public int stepNo()
    {
        return 5;
    }


    // ----------------------------------------------------------
    public void setDefaultConfigValues( WCConfigurationFile configuration )
    {
        String defaultAuth =
            configuration.getProperty( "authenticator.default" );
        if ( defaultAuth != null && !defaultAuth.equals( "" ) )
        {
            setConfigDefault( configuration, "authenticator.default.class",
                configuration.getProperty(
                    "authenticator." + defaultAuth ) );
            setConfigDefault( configuration, "InstitutionName",
                configuration.getProperty(
                    "authenticator." + defaultAuth + ".displayableName" ) );
            setConfigDefault( configuration, "InstitutionEmailDomain",
                configuration.getProperty(
                    "authenticator." + defaultAuth + ".defaultEmailDomain" ) );
        }
        setConfigDefault( configuration, "authenticator.default.class",
            net.sf.webcat.core.DatabaseAuthenticator.class.getName() );
    }


    // ----------------------------------------------------------
    public void takeFormValues( NSDictionary formValues )
    {
        String defaultAuth =
            storeFormValueToConfig( formValues, "authenticator.default",
                "Please specify a short name for your institution." );
        String authClass =
            storeFormValueToConfig( formValues, "authenticator.default.class",
                "Please select your authentication method." );
        if ( defaultAuth != null )
        {
            if ( authClass != null )
            {
                if ( authClass.equals( "custom-auth-class" ) )
                {
                    String customClass = storeFormValueToConfig( formValues,
                        "authenticator.default.class.custom",
                        "authenticator." + defaultAuth,
                        "You must specify a custom authentication class name."
                        );
                    if ( customClass != null )
                    {
                        // Check to see that it is indeed on the classpath
                        try
                        {
                            Class.forName( customClass );
                        }
                        catch ( ClassNotFoundException e )
                        {
                            errorMessage( e.getMessage() );
                        }
                    }
                }
                else
                {
                    Application.configurationProperties().setProperty(
                        "authenticator." + defaultAuth,
                        authClass );
                }
            }
            String value =
                storeFormValueToConfig( formValues, "InstitutionName",
                    "authenticator." + defaultAuth + ".displayableName",
                    null );
            if ( value == null )
            {
                Application.configurationProperties().setProperty(
                    "authenticator." + defaultAuth + ".displayableName",
                    defaultAuth );
            }
            storeFormValueToConfig( formValues, "InstitutionEmailDomain",
                "authenticator." + defaultAuth + ".defaultEmailDomain",
                null );
            storeFormValueToConfig( formValues, "InstitutionEmailDomain",
                "mail.default.domain",
                null );
            if ( !hasErrors() )
            {
                net.sf.webcat.core.AuthenticationDomain.refreshAuthDomains();
            }
        }
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( InstallPage5.class );
}
