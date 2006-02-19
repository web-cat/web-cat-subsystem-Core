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

package net.sf.webcat.core;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Implements the login UI functionality of the system.
 *
 *  @author Stephen Edwards
 *  @version $Id$
 */
public class LoginPage
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new LoginPage object.
     * 
     * @param context The context to use
     */
    public LoginPage( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public String               userName;
    public String               password;
    public NSMutableDictionary  errors;
    public WODisplayGroup       domainDisplayGroup;
    public AuthenticationDomain domain;
    public AuthenticationDomain domainItem;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see com.webobjects.appserver.WOComponent#awake()
     */
    public void awake()
    {
        super.awake();
        log.debug( "hasSession = " + hasSession() );
        if ( hasSession() )
            log.debug( "session = " + session().sessionID() );
        log.debug( "errors = " + errors );

        domainDisplayGroup.setObjectArray(
            AuthenticationDomain.authDomains() );
        log.debug( "domain = " + domain );
        if ( domain == null )
        {
            // Set up a default domain, if possible
            String defaultDomain = Application.configurationProperties()
                .getProperty( "authenticator.default" );
            if ( defaultDomain != null )
            {
                try
                {
                    log.debug( "looking up default domain" );
                    domain =
                        AuthenticationDomain.authDomainByName( defaultDomain );
                }
                catch ( EOObjectNotAvailableException e )
                {
                    log.error( "Default authentication domain ("
                             + defaultDomain + ") does not exist." );
                }
                catch ( EOUtilities.MoreThanOneException e )
                {
                    log.error( "Multiple entries for default authentication "
                             + "domain (" + defaultDomain + ")" );
                }
            }
        }
        log.debug( "domain = " + domain );
    }


    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see com.webobjects.appserver.WOComponent#sleep()
     */
    public void sleep()
    {
        log.debug( "hasSession = " + hasSession() );
        if ( hasSession() )
            log.debug( "session = " + session().sessionID() );
        super.sleep();
    }


//    // ----------------------------------------------------------
//    /**
//     * Adds to the response of the page
//     * 
//     * @param response The response being built
//     * @param context  The context of the request
//     */
//    public void appendToResponse( WOResponse response, WOContext context )
//    {
//        super.appendToResponse( response, context );
//    }


//    // ----------------------------------------------------------
//    /**
//     * Return the URL for a direct action to process this form
//     * submission.
//     */
//    public String loginDirectAction()
//    {
//        return "";
//    }


//    // ----------------------------------------------------------
//    /**
//     * Clears the login form.
//     */
//    public void clear()
//    {
//        pidname  = null;
//        password = null;
//	domain   = null;
//        errMsg   = null;
//    }
//
//
//    // ----------------------------------------------------------
//    /**
//     * Returns to page title.
//     * 
//     * @return The title to use
//     */
//    public String title()
//    {
//        return super.title() + ": Login";
//    }
//
//
//    // ----------------------------------------------------------
//    /**
//     * Response function to attempt a login.  It tries to authenticate
//     * the entered username and password.  If successful, it checks
//     * for an existing session to connect with and logs the user in.
//     * 
//     * @return The page to go to after logging in
//     */
//    public WOComponent tryLogin()
//    {
//	log.debug( "authlist: "
//		   + domainDisplayGroup.allObjects().count()
//		   + " entries" );
//	for ( int i = 0; i < domainDisplayGroup.allObjects().count(); i++ )
//	{
//	    log.debug( ( (AuthenticationDomain)
//			 domainDisplayGroup.allObjects().objectAtIndex( i )
//		       ).displayableName() );
//	}
//	
//	if ( pidname == null )
//	{
//	    errMsg = "Please enter your PID/user name.";
//	    return null;
//	}
//	if ( password == null )
//	{
//	    errMsg = "Please enter your password.";
//	    return null;
//	}
//	if ( domain == null )
//	{
//	    errMsg = "Please select your institution.";
//	    return null;
//	}
//
//	pidname = pidname.toLowerCase();
//	User user = User.validate(
//		pidname,
//		password,
//		domain,
//		session().defaultEditingContext()
//	    );
//	if ( user == null )
//	{
//	    log.info( "Failed login attempt: " + pidname
//		      + " (" + domain.displayableName() + ")" );
//	    password = null;
//	    errMsg = "Your login information could not be validated.  "
//		+ "Be sure you typed your user name/pid and password "
//		+ "correctly.";
//	    return null;
//	}
//	else
//	{
//	    String sessionID = wcSession().setUser( user );
//	    Application.userCount++;
//	    log.info( "login: "
//		      + pidname
//		      + " (" + domain.displayableName() + ")"
//		      + " (now "
//		      + Application.userCount
//		      + " users)" );
//	    if ( sessionID.equals( session().sessionID() ) )
//            {
//		log.debug( "re-entering " + sessionID );
//		return context().page();
//	    }
//	    else
//            {
//		log.info( "redirecting to " + sessionID );
//		wcSession().userLogout();
//		WORedirect redirect = (WORedirect)pageWithName( "WORedirect" );
//		String dest = Application.completeURLWithRequestHandlerKey(
//			context(),
//			"wa",
//			"default",
//			"wosid=" + sessionID,
//			false,
//			0
//		    );
//		log.debug( "destination = " + dest );
//		redirect.setUrl( dest );
//		return redirect;
//	    }
//	}
//    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( LoginPage.class );
}
