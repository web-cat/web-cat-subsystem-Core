/*==========================================================================*\
 |  $Id: InstallPage4.java,v 1.3 2012/05/09 14:27:40 stedwar2 Exp $
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
import com.webobjects.foundation.*;
import java.io.File;
import org.webcat.core.Application;
import org.webcat.core.WCConfigurationFile;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Implements the login UI functionality of the system.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.3 $, $Date: 2012/05/09 14:27:40 $
 */
public class InstallPage4
    extends InstallPage
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new PreCheckPage object.
     *
     * @param context The context to use
     */
    public InstallPage4( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................



    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public int stepNo()
    {
        return 4;
    }


    // ----------------------------------------------------------
    public void setDefaultConfigValues( WCConfigurationFile configuration )
    {
        log.debug( "setDefaultConfigValues()" );
        setConfigDefault(
            configuration, "coreApplicationIdentifier", "Web-CAT" );
        if ( configuration.getProperty( "base.url" ) == null )
        {
            if ( context() != null )
            {
                configuration.setProperty( "base.url",
                    Application.completeURLWithRequestHandlerKey(
                        context(), null, null, null, false, 0 ) );
            }
            else
            {
                String url =
                    Application.application().servletConnectURL();
                url = url.replaceFirst( "(//[^/]+/)/", "$1" );
                configuration.setProperty( "base.url", url );
            }
        }
        if ( configuration.getProperty( "grader.workarea" ) == null )
        {
            String value = configuration.getProperty( "java.io.tmpdir" );
            if ( value != null && !value.equals( "" ) )
            {
                value = value.replace( '\\', '/' );
                configuration.setProperty( "grader.workarea", value );
            }
        }
        if ( configuration.getProperty( "mail.smtp.host" ) != null )
        {
            configuration.setProperty( "WOSMTPHost",
                configuration.getProperty( "mail.smtp.host" ) );
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "configuration = " + configuration );
        }
    }


    // ----------------------------------------------------------
    public void takeFormValues( NSDictionary<?, ?> formValues )
    {
        storeFormValueToConfig( formValues, "coreApplicationIdentifier",
            "Please specify a name for your Web-CAT instance." );
        storeFormValueToConfig( formValues, "base.url",
            "Please specify the base URL for Web-CAT." );
        storeFormValueToConfig( formValues, "login.url", null );
        storeFormValueToConfig( formValues, "mail.smtp.host", null );
        storeFormValueToConfig(
            formValues, "mail.smtp.host", "WOSMTPHost", null );
        storeFormValueToConfig( formValues, "grader.submissiondir",
            "You must specify a storage directory." );
        storeFormValueToConfig( formValues, "grader.workarea",
            "You must specify a work area directory." );
    }


    // ----------------------------------------------------------
    public Object validateValueForKey( Object value, String key )
    {
        if ( "grader.submissiondir".equals( key ) )
        {
            validateDirectory( value.toString(), "storage directory" );
            return value;
        }
        else if ( "grader.workarea".equals( key ) )
        {
            validateDirectory( value.toString(), "work area" );
            return value;
        }
        return super.validateValueForKey( value, key );
    }


    // ----------------------------------------------------------
    public void validateDirectory( String value, String desc )
    {
        File dir = new File( value );
        if ( dir.exists() )
        {
            if ( !dir.isDirectory() )
            {
                throw new NSValidation.ValidationException(
                    "The " + desc + " already exists as a "
                    + "plain file, but it must be a directory instead." );
            }
            else if ( !dir.canWrite() )
            {
                throw new NSValidation.ValidationException(
                    "Cannot write to the " + desc + "." );
            }
        }
        else
        {
            try
            {
                if ( !dir.mkdirs() )
                {
                    throw new NSValidation.ValidationException(
                        "The " + desc + " specified does not "
                        + "exist, and an error occurred attempting to "
                        + "create it." );
                }
            }
            catch ( SecurityException e )
            {
                throw new NSValidation.ValidationException(
                    "Error attempting to create the " + desc + ": "
                    + e.getMessage() );
            }
        }
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( InstallPage4.class );
}
