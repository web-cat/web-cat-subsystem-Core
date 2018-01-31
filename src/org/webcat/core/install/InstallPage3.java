/*==========================================================================*\
 |  $Id: InstallPage3.java,v 1.3 2011/10/25 12:57:03 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2011 Virginia Tech
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

package org.webcat.core.install;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;
import org.webcat.dbupdate.*;
import org.webcat.woextensions.WCEC;
import er.extensions.foundation.ERXValueUtilities;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import org.webcat.core.Application;
import org.webcat.core.CoreDatabaseUpdates;
import org.webcat.core.WCConfigurationFile;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Implements the login UI functionality of the system.
 *
 *  @author Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.3 $, $Date: 2011/10/25 12:57:03 $
 */
public class InstallPage3
    extends InstallPage
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new PreCheckPage object.
     *
     * @param context The context to use
     */
    public InstallPage3( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................



    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public int stepNo()
    {
        return 3;
    }


    // ----------------------------------------------------------
    public void setDefaultConfigValues( WCConfigurationFile configuration )
    {
        log.debug( "setDefaultConfigValues()" );
        String connectUrl = configuration.getProperty( "dbConnectURLGLOBAL" );
        if ( connectUrl != null )
        {
            if ( connectUrl.startsWith( JDBC_PREFIX ) )
            {
                connectUrl = connectUrl.substring( JDBC_PREFIX.length() );
            }
            try
            {
                // jdbc:mysql://localhost:3306/WebCAT?capitalizeTypeNames=true
                URI uri = new URI( connectUrl );
                configuration.setProperty( "DBhostname", uri.getHost() );
                configuration.setProperty( "DBhostport",
                    Integer.toString( uri.getPort() ) );
                String value = uri.getPath();
                if ( value != null )
                {
                    if ( value.startsWith( "/" ) )
                    {
                        value = value.substring( 1 );
                    }
                    configuration.setProperty( "DBname", value );
                }
                if ( log.isDebugEnabled() )
                {
                    log.debug( "properties = " + configuration );
                }
            }
            catch ( Exception e )
            {
                log.warn( "exception parsing dbConnectURLGLOBAL config value '"
                    + connectUrl + "':", e );
            }
        }
        setConfigDefault( configuration, "DBhostname" , "localhost" );
        setConfigDefault( configuration, "DBhostport" , "3306" );
        setConfigDefault( configuration, "DBname" ,     "WebCAT" );
        if ( log.isDebugEnabled() )
        {
            log.debug( "configuration = " + configuration );
        }
    }


    // ----------------------------------------------------------
    public void takeFormValues( NSDictionary<?, ?> formValues )
    {
        storeFormValueToConfig( formValues, "dbConnectUserGLOBAL",
            "You must specify a user name." );
        storeFormValueToConfig( formValues, "dbConnectPasswordGLOBAL",
            "You must specify a password." );
        String hostname = storeFormValueToConfig( formValues, "DBhostname",
            "You must specify a host name." );
        String hostportString = storeFormValueToConfig(
            formValues, "DBhostport", "You must specify a host port number." );
        String name = storeFormValueToConfig( formValues, "DBname",
            "You must specify a database name." );
        if ( hostname != null && hostportString != null && name != null )
        {
            Application.configurationProperties().setProperty(
                "dbConnectURLGLOBAL",
                databaseURL( hostname, hostportString, name ) );
            setUpDatabase( ERXValueUtilities.booleanValue(
                extractFormValue( formValues, "DBDelete" ) ),
                hostname,
                hostportString,
                name );
        }
    }


    // ----------------------------------------------------------
    protected String databaseURL( String host, String port, String dbname )
    {
        return "jdbc:mysql://" + host + ":" + port
        + "/" + ( dbname == null ? "" : dbname ) + "?capitalizeTypeNames=true";
    }


    // ----------------------------------------------------------
    protected void setUpDatabase(
        boolean dropOld,
        String  host,
        String  port,
        String  dbname )
    {
        Database db = new MySQLDatabase();
        try
        {
            db.setConnectionInfoFromProperties(
                Application.configurationProperties() );
            db.setConnectionUrlString( databaseURL( host, port, null ) );
            if ( dropOld )
            {
                try
                {
                    db.executeSQL( "drop database `" + dbname + "`" );
                }
                catch ( SQLException e )
                {
                    log.info( "cannot drop old database: "
                        + e.getMessage() );
                }
            }
            boolean databaseExists = false;
            try
            {
                db.executeSQL( "use `" + dbname + "`" );
                databaseExists = true;
            }
            catch ( SQLException e )
            {
                log.info( "looking for existing database: " + e.getMessage() );
            }
            if ( !databaseExists )
            {
                db.executeSQL( "create database `" + dbname + "`" );
                db.executeSQL( "use `" + dbname + "`" );
            }
            // Initialize database
            Application.configurationProperties().updateToSystemProperties();
            updateEOModels();

            // Instead of calling initializeApplication(), let's just repeat
            // the first few steps so that we can get the database updates
            // done.
            WCEC.installWOECFactory();

            // Apply any pending database updates for the core
            UpdateEngine.instance().database().setConnectionInfoFromProperties(
                            Application.configurationProperties() );
            UpdateEngine.instance().applyNecessaryUpdates(
                            new CoreDatabaseUpdates() );

            // We'll do this later, once the admin account is set up
//          ( (Application)Application.application() ).initializeApplication();
        }
        catch ( Exception e )
        {
            log.error( "exception initializing application:", e );
            error( e.getMessage() );
        }
        finally
        {
            db.close();
        }
    }


    // ----------------------------------------------------------
    protected void updateEOModels()
    {
        // remove all of the existing EOModels and re-install them, so that
        // they pick up the newly configured connection info.
        NSArray<?> models = EOModelGroup.defaultGroup().models();
        if ( models != null )
        {
            URL[] modelPaths = new URL[models.count()];
            for ( int i = 0; i < models.count(); i++ )
            {
                EOModel model = (EOModel)models.objectAtIndex( i );
                modelPaths[i] = model.pathURL();
                EOModelGroup.defaultGroup().removeModel( model );
            }
            for ( int i = 0; i < modelPaths.length; i++ )
            {
                EOModelGroup.defaultGroup()
                    .addModelWithPathURL( modelPaths[i] );
            }
        }
    }


    // ----------------------------------------------------------
    public Object validateValueForKey( Object value, String key )
        throws ValidationException
    {
        if ( "DBhostport".equals( key ) )
        {
            try
            {
                int hostport = Integer.parseInt( value.toString() );
                if ( hostport < 1 )
                {
                    throw new NSValidation.ValidationException(
                        "You must specify a host port number." );
                }
            }
            catch ( NumberFormatException e )
            {
                throw new NSValidation.ValidationException(
                    "The port number must be an integer." );
            }
            return value;
        }
        return super.validateValueForKey( value, key );
    }


    //~ Instance/static variables .............................................

    private static final String JDBC_PREFIX = "jdbc:";

    static Logger log = Logger.getLogger( InstallPage3.class );
}
