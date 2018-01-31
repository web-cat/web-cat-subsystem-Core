/*==========================================================================*\
 |  $Id: InstallPage8.java,v 1.4 2012/05/09 14:26:21 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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
import org.webcat.core.Application;
import org.webcat.core.WCConfigurationFile;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Implements the login UI functionality of the system.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.4 $, $Date: 2012/05/09 14:26:21 $
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


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public int stepNo()
    {
        return 8;
    }


    // ----------------------------------------------------------
    public void setDefaultConfigValues( WCConfigurationFile configuration )
    {
        adminPassword = configuration.getProperty( "AdminPassword" );
        if ( adminPassword != null )
        {
            configuration.remove( "AdminPassword" );
        }
        configuration.remove( "configStep" );
        configuration.setProperty( "installComplete", "true" );
        configSaved = configuration.attemptToSave();
        configuration.updateToSystemProperties();
        // configuration.setProperty( "configStep", "" + stepNo() );
        Application.wcApplication().setNeedsInstallation( false );
        try
        {
            Application.wcApplication().initializeApplication();
            Application.wcApplication().notifyAdminsOfStartup();

            String subject = "New Web-CAT installation now active";
            String body = "Congratulations, "
                + configuration.getProperty("coreAdminEmail")
                + ", you have completed this installation process for\n"
                + "Web-CAT and your server is now active.  If you are "
                + "configuring your\nserver to automatically restart, "
                + "please choose a time other than\n04:00 AM EDT (at "
                + "least 15 minutes or more away, please) to ensure that "
                + "any\nautomatic updates do not get cut off when the "
                + "primary Web-CAT update site\nreboots daily at that "
                + "time.\n\nFor support, see the Web-CAT forums at:\n\n"
                + "http://web-cat.org\n\nOr e-mail "
                + "the Web-CAT staff at: webcat@vt.edu.";

            Application.sendAdminEmail(subject, body);
            Application.sendSimpleEmail("webcat@vt.edu", subject, body);
        }
        catch ( Exception e )
        {
            log.error( "Exception initializing application:", e );
        }
    }


    // ----------------------------------------------------------
    public void appendToResponse( WOResponse request, WOContext context )
    {
        super.appendToResponse( request, context );
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
