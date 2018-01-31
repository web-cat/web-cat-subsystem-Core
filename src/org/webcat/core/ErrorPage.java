/*==========================================================================*\
 |  $Id: ErrorPage.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
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

package org.webcat.core;

import com.webobjects.appserver.*;
import com.webobjects.woextensions.*;
import java.io.*;
import org.webcat.core.Application;
import org.webcat.core.ErrorPage;
import org.webcat.core.Session;
import org.webcat.core.User;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * This is the generic error page shown to users whenever an exception
 * occurs.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class ErrorPage
    extends WOExceptionPage
{
    //~ Constructors ..........................................................

     // ----------------------------------------------------------
   /**
     * Creates a new ErrorPage object.
     *
     * @param context The context
     */
    public ErrorPage( WOContext context )
    {
        super( context );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Returns the user to their Web-CAT home page.
     * @return The home page component
     */
    public WOComponent gotoMainPage()
    {
        /*
           // Should probably call WCPlainPage.logout() instead ...
           if ( context.hasSession() &&
                ( (Session)session() ).getUser() != null )
           {
               log.info( "user "
                     + ( (Session)session() ).getUser().pid()
                     + " logging out" );
               ( (Session)session() ).userLogout();
           }
           WORedirect new_login = new WORedirect( context() );
           new_login.setUrl( "/cgi-bin/WebObjects.exe/Main.woa" );
               return new_login;
         */
        String pageName =
            ( (Session)session() ).tabs.selectDefault().pageName();
        log.debug( "returning to " + pageName );
        return pageWithName( pageName );
    }


    // ----------------------------------------------------------
    /**
     * Determine whether the user (if there is one) has admin privileges.
     * @return True if the user is an admin
     */
    public boolean isAdminUser()
    {
        if ( hasSession() )
        {
            Session session = (Session)session();
            User user = session.primeUser();
            return user != null && user.hasAdminPrivileges();
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    /**
     * Determine whether there is a user currently logged in.
     * @return True if the user is logged in
     */
    public boolean isLoggedIn()
    {
        log.debug( "context = " + context() );
        log.debug( "action url = " + context().componentActionURL() );
        if ( hasSession() )
        {
            Session session = (Session)session();
            session.cancelSessionChanges();
            return session.isLoggedIn();
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    /**
     * Get a formatted reason and stack trace message from the given
     * exception.
     * @return The exception details and stack trace as a string
     */
    public String exceptionStackTrace()
    {
        Throwable reason = exception;
        if ( reason == null ) return null;
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter( sw );
        reason.printStackTrace( out );
        reason = reason.getCause();
        while ( reason != null )
        {
            out.println( "\nCaused by:\n" );
            reason.printStackTrace( out );
            reason = reason.getCause();
        }
        out.close();
        return sw.toString();
    }


    // ----------------------------------------------------------
    /**
     * Logs the user out.
     * @return The login page
     */
    public WOComponent logout()
    {
        if ( hasSession() )
        {
            ( (Session)session() ).userLogout();
        }
        return ( (Application)application() ).gotoLoginPage( context() );
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( ErrorPage.class );
}
