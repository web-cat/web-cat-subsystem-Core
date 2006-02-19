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
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.sf.webcat.core.*;

import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Implements the login UI functionality of the system.
 *
 *  @author Stephen Edwards
 *  @version $Id$
 */
public class InstallPage8
    extends InstallPage
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new PreCheckPage object.
     * 
     * @param context The context to use
     */
    public InstallPage8( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public boolean configSaved = false;
    public String  adminNotifyAddrs;
    public String  adminPassword;
    public String  adminUsername;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public int stepNo()
    {
        return 8;
    }


    // ----------------------------------------------------------
    public void setDefaultConfigValues( WCConfigurationFile configuration )
    {
        adminNotifyAddrs = configuration.getProperty( "adminNotifyAddrs" );
        if ( adminNotifyAddrs == null || adminNotifyAddrs.equals( "" ) )
        {
            adminNotifyAddrs = configuration.getProperty( "coreAdminEmail" );
        }
        adminUsername = configuration.getProperty( "AdminUsername" );
        configuration.remove( "AdminUsername" );
        adminPassword = configuration.getProperty( "AdminPassword" );
        configuration.remove( "AdminPassword" );
        configuration.setProperty( "installComplete", "true" );
        configSaved = configuration.attemptToSave();
        Application app = (Application)Application.application();
        app.setNeedsInstallation( false );
        app.notifyAdminsOfStartup();
    }


    // ----------------------------------------------------------
    public void appendToResponse( WOResponse request, WOContext context )
    {
        super.appendToResponse( request, context );
        Application.configurationProperties().setIsInstalling( false );
    }


    // ----------------------------------------------------------
    public boolean configIsWriteable()
    {
        return Application.configurationProperties().isWriteable();
    }


    // ----------------------------------------------------------
    public String configLocation()
    {
        try
        {
            return Application.configurationProperties().file()
                .getCanonicalPath();
        }
        catch ( java.io.IOException e )
        {
            log.error( "exception looking up configuration file location:", e );
            return e.getMessage();
        }
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( InstallPage8.class );
}
