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
import er.extensions.ERXDirectAction;
import er.extensions.ERXValueUtilities;
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
        "1",            // 3
        "2",            // 4
        "3",            // 5
        "4",            // 6
        "5",            // 7
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
        if ( log.isDebugEnabled() )
        {
            log.debug( "defaultAction(): form values = "
                + request().formValues() );
            log.debug( app.configurationProperties().configSettingsAsString() );
        }
        if ( !app.needsInstallation() )
        {
            return app.gotoLoginPage( context() );
        }
        int step = app.configurationProperties().intForKey( "configStep" );
        if ( step > 0 && step <= steps.length )
        {
            InstallPage oldPage = (InstallPage)
                pageWithName( InstallPage.class.getName() + step );
            oldPage.takeFormValues( request().formValues() );
            if ( oldPage.hasErrors() )
            {
                return oldPage;
            }
        }
        if ( step == steps.length && request().formValueForKey( "next" ) != null )
        {
            // Replace this with logging in under the newly created
            // administrator account and then going directly to
            // the Administer tab
            return app.gotoLoginPage( context() );
        }
        else if ( step == 0 || request().formValueForKey( "next" ) != null )
        {
            step++;
        }
        else if ( step > 1 && request().formValueForKey( "back" ) != null )
        {
            step--;
        }
        app.configurationProperties().setProperty(
            "configStep", Integer.toString( step ) );
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
