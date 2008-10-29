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

import com.sun.org.apache.xerces.internal.util.URI.MalformedURIException;
import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.foundation.ERXValueUtilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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

    public static final String DATABASE_STRATEGY = "Built-in database";
    public static final String LDAP_STRATEGY = "LDAP";
    public static final String CUSTOM_STRATEGY = "Custom authenticator plug-in";
    public static final Map<String, Class<? extends UserAuthenticator>>
        STRATEGY_MAP =
            new HashMap<String, Class<? extends UserAuthenticator>>();
    {
        STRATEGY_MAP.put(DATABASE_STRATEGY, DatabaseAuthenticator.class);
        STRATEGY_MAP.put(LDAP_STRATEGY, LdapAuthenticator.class);
    }
    public final NSArray<String> authStrategies = new NSArray(new String[] {
        DATABASE_STRATEGY, LDAP_STRATEGY, CUSTOM_STRATEGY
    });

    public String authStrategy;
    public String chosenAuthStrategy;


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
        chosenAuthStrategy = configuration.getProperty("authStrategyChoice");
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

            // For LDAP
            setConfigDefault( configuration, "LdapAuthFilter",
                configuration.getProperty(
                    "authenticator." + defaultAuth + ".ldap.authFilter" ) );
            setConfigDefault( configuration, "LdapBindDN",
                configuration.getProperty(
                    "authenticator." + defaultAuth + ".ldap.bindDN" ) );
            setConfigDefault( configuration, "LdapBindPassword",
                configuration.getProperty(
                    "authenticator." + defaultAuth + ".ldap.bindPassword" ) );
            setConfigDefault( configuration, "LdapContext",
                configuration.getProperty(
                    "authenticator." + defaultAuth + ".ldap.context" ) );
            setConfigDefault( configuration, "LdapUserField",
                configuration.getProperty(
                    "authenticator." + defaultAuth + ".ldap.userField" ) );

            String hostUrl = configuration.getProperty(
                    "authenticator." + defaultAuth + ".ldap.hostUrl" );
            if (hostUrl != null)
            {
                try
                {
                    URL url = new URL(hostUrl);
                    setConfigDefault(configuration, "LdapHost", url.getHost());
                    int port = url.getPort();
                    if (port != -1)
                    {
                        setConfigDefault(configuration, "LdapPort",
                            Integer.toString(port));
                    }
                    if ("ldaps".equals(url.getProtocol()))
                    {
                        setConfigDefault( configuration, "LdapProtocol", "0");
                    }
                    else
                    {
                        String useTLS = configuration.getProperty(
                            "authenticator." + defaultAuth + ".ldap.useTLS");
                        setConfigDefault( configuration, "LdapProtocol",
                            ERXValueUtilities.booleanValue(useTLS)
                                ? "1"
                                : "2");
                    }
                }
                catch (MalformedURLException e)
                {
                    // Ignore it, and hope it gets fixed.
                }
            }
        }
        // TODO: search subtrees setting is not restored from config correctly
        setConfigDefault( configuration, "authenticator.default.class",
            net.sf.webcat.core.DatabaseAuthenticator.class.getName() );
        setConfigDefault( configuration, "LdapUserField", "cn");
        setConfigDefault( configuration, "LdapProtocol", "0");
    }


    // ----------------------------------------------------------
    public void takeFormValues( NSDictionary formValues )
    {
        log.debug("hello");
        chosenAuthStrategy =
            storeFormValueToConfig( formValues, "authStrategyChoice",
                "Please select your authentication method.");
        String defaultAuth =
            storeFormValueToConfig( formValues, "authenticator.default",
                "Please specify a short name for your institution." );
//        String authClass =
//            storeFormValueToConfig( formValues, "authenticator.default.class",
//                "Please select your authentication method." );
        if ( defaultAuth != null && chosenAuthStrategy != null )
        {
            Class<? extends UserAuthenticator> authClass =
                STRATEGY_MAP.get(chosenAuthStrategy);
            String authClassName = (authClass == null)
                ? storeFormValueToConfig( formValues,
                    "authenticator.default.class.custom",
                    "authenticator." + defaultAuth,
                    "You must specify a custom authentication class name.")
                : authClass.getName();
            if (authClassName != null)
            {
                // Check to see that it is indeed on the classpath
                try
                {
                    Class.forName( authClassName );
                }
                catch ( ClassNotFoundException e )
                {
                    error( e.getMessage() );
                }
                Application.configurationProperties().setProperty(
                    "authenticator." + defaultAuth, authClassName);

            }

            if (LDAP_STRATEGY.equals(chosenAuthStrategy))
            {
                // LDAP attributes
                storeFormValueToConfig(formValues, "LdapContext",
                    "authenticator." + defaultAuth + ".ldap.context",
                    "You must specify the LDAP context used to look up "
                    + "account information.");
                storeFormValueToConfig(formValues, "LdapUserField",
                    "authenticator." + defaultAuth + ".ldap.userField",
                    "You must specify the LDAP user field.");
                storeFormValueToConfig(formValues, "LdapAuthFilter",
                    "authenticator." + defaultAuth + ".ldap.authFilter",
                    null);
                String bindDN = storeFormValueToConfig(formValues,
                    "LdapBindDN",
                    "authenticator." + defaultAuth + ".ldap.bindDN",
                    null);
                storeFormValueToConfig(formValues, "LdapBindPassword",
                    "authenticator." + defaultAuth + ".ldap.bindPassword",
                    (bindDN == null) ? null :
                        "You must provide a password for the LDAP bind account."
                    );
                String protocol = storeFormValueToConfig(formValues,
                    "LdapProtocol",
                    "authenticator." + defaultAuth + ".ldap.protocol",
                    "You must select an LDAP protocol.");
                Application.configurationProperties().setProperty(
                    "authenticator." + defaultAuth + ".ldap.useTLS",
                    Boolean.toString("1".equals(protocol)));
                Application.configurationProperties().setProperty(
                    "authenticator." + defaultAuth + ".ldap.searchSubtrees",
                    Boolean.toString("1".equals(
                        storeFormValueToConfig(
                            formValues, "LdapSearchSubtrees", null))));

                String hostName = storeFormValueToConfig(formValues,
                    "LdapHost",
                    "You must provide the DNS name for your LDAP server.");
                int port = -1;
                String protocolName = "0".equals(protocol)
                    ? "ldaps:"
                    : "ldap:";
                try
                {
                    String hostPort = storeFormValueToConfig(formValues,
                        "LdapPort", null);
                    if (hostPort != null)
                    {
                        port = Integer.parseInt(hostPort);
                    }
                }
                catch (NumberFormatException e)
                {
                    error("The LDAP port must be an integer.");
                }
                if (hostName != null)
                {
                    String hostUrl = protocolName + "//" + hostName;
                    if (port != -1)
                    {
                        hostUrl += ":" + port;
                    }
                    Application.configurationProperties().setProperty(
                        "authenticator." + defaultAuth + ".ldap.hostUrl",
                        hostUrl);
                }
            }

            // Common attributes that all authenticators share.
            String value = storeFormValueToConfig(formValues,
                "InstitutionName",
                "authenticator." + defaultAuth + ".displayableName",
                null);
            if ( value == null )
            {
                Application.configurationProperties().setProperty(
                    "authenticator." + defaultAuth + ".displayableName",
                    defaultAuth);
            }
            storeFormValueToConfig(formValues, "InstitutionEmailDomain",
                "authenticator." + defaultAuth + ".defaultEmailDomain",
                null);
            storeFormValueToConfig(formValues, "InstitutionEmailDomain",
                "mail.default.domain",
                null);
            if ( !hasMessages() )
            {
                net.sf.webcat.core.AuthenticationDomain.refreshAuthDomains();
            }
        }
        String oldAuthStrategy = storeFormValueToConfig(formValues,
            "oldAuthStrategyChoice", null);
        log.debug("chosenAuthStrategy = " + chosenAuthStrategy);
        log.debug("oldAuthStrategy = " + oldAuthStrategy);
        if (oldAuthStrategy == null
            || !oldAuthStrategy.equals(chosenAuthStrategy))
        {
            // Assume the drop-down changed
            clearAllMessages();
        }
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( InstallPage5.class );
}
