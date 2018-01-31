/*==========================================================================*\
 |  $Id: PasswordChangeRequestPage.java,v 1.3 2011/12/25 02:24:54 stedwar2 Exp $
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

package org.webcat.core;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import org.webcat.core.AuthenticationDomain;
import org.webcat.core.ErrorDictionaryPanel;
import org.webcat.core.PasswordChangeRequest;
import org.webcat.core.PasswordChangeRequestPage;
import org.webcat.core.Status;
import org.webcat.core.User;
import org.webcat.woextensions.WCEC;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Implements the login UI functionality of the system.
 *
 *  @author Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.3 $, $Date: 2011/12/25 02:24:54 $
 */
public class PasswordChangeRequestPage
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new LoginPage object.
     *
     * @param context The context to use
     */
    public PasswordChangeRequestPage( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public String               email;
    public boolean              emailSent;
    public WODisplayGroup       domainDisplayGroup;
    public AuthenticationDomain domain;
    public AuthenticationDomain domainItem;

    public NSMutableDictionary<String, Object> errors =
        new NSMutableDictionary<String, Object>();


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see com.webobjects.appserver.WOComponent#awake()
     */
    public void awake()
    {
        super.awake();
        if ( log.isDebugEnabled() )
        {
            log.debug( "awake(): hasSession = " + hasSession() );
            if ( hasSession() )
                log.debug( "awake(): session = " + session().sessionID() );
            log.debug( "awake(): errors = " + errors );
            log.debug( "awake(): domain = " + domain );
            log.debug( "awake(): parameters = "
                + context().request().formValues() );
        }

        domainDisplayGroup.setObjectArray(
            AuthenticationDomain.authDomains() );
        if ( domain == null )
        {
            domain = AuthenticationDomain.defaultDomain();
        }
        log.debug( "awake(): domain = " + domain );

        sendEmailIfNecessary();
        if ( log.isDebugEnabled() )
        {
            log.debug( "awake(): errors = " + errors );
        }
    }


    // ----------------------------------------------------------
    private void sendEmailIfNecessary()
    {
        email = context().request().stringFormValueForKey( "email" );
        if ( email != null )
        {
            if ( hasSpecificAuthDomain() )
            {
                // Then the call in the condition just set domain correctly
            }
            else if ( domainDisplayGroup.allObjects().count() == 1 )
            {
                domain = (AuthenticationDomain)domainDisplayGroup.allObjects()
                    .objectAtIndex( 0 );
            }
            else
            {
                errors.setObjectForKey(
                    new ErrorDictionaryPanel.ErrorMessage( Status.ERROR,
                        "Please select the institution under which your "
                        + "account is registered.", false ), "1" );
            }

            if ( errors.count() == 0 )
            {
                // Try to look up the user
                EOEditingContext ec = WCEC.newEditingContext();
                try
                {
                    ec.lock();
                    User u = User.lookupUserByEmail( ec, email, domain );
                    if ( u == null )
                    {
                        errors.setObjectForKey(
                            new ErrorDictionaryPanel.ErrorMessage(
                                Status.ERROR,
                                "Unable to find your account.  Please "
                                + "check that you have entered your "
                                + "e-mail address "
                                + ((domainDisplayGroup.allObjects().count()
                                   == 1) ? "" : "and institution " )
                                + "correctly.", false ), "4" );
                    }
                    else if ( !u.authenticationDomain().authenticator()
                                  .canChangePassword() )
                    {
                        errors.setObjectForKey(
                            new ErrorDictionaryPanel.ErrorMessage(
                                Status.ERROR,
                                "Web-CAT does not manage the password for "
                                + "your account.  Contact your Web-CAT"
                                + "administrator for instructions on how to"
                                + "change your password.", false ),
                                "7" );
                    }
                    else
                    {
                        if ( PasswordChangeRequest
                                 .clearPendingUserRequests( ec, u ) )
                        {
                            errors.setObjectForKey(
                                new ErrorDictionaryPanel.ErrorMessage(
                                    Status.WARNING,
                                    "Any pending password reset links for "
                                    + "your account that you received in "
                                    + "the past are no longer valid.", false ),
                                    "6" );
                        }
                        PasswordChangeRequest.sendPasswordResetEmail( ec, u );
                        emailSent = true;
                    }
                }
                catch ( User.MultipleUsersFoundException e )
                {
                    log.error( "e-mail address '" + email + "' for domain "
                               + domain + " is not unique!", e );
                    errors.setObjectForKey(
                        new ErrorDictionaryPanel.ErrorMessage(
                            Status.ERROR,
                            "Multiple accounts are registered for your "
                            + "e-mail address!  Contact your Web-CAT "
                            + "administrator for help.", false ), "5" );
                }
                finally
                {
                    ec.unlock();
                    ec.dispose();
                }
                if ( !emailSent && errors.count() == 0 )
                {
                    errors.setObjectForKey(
                        new ErrorDictionaryPanel.ErrorMessage(
                            Status.ERROR,
                            "Unable to process your request.", false ), "3" );
                }
            }
        }
    }


    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see com.webobjects.appserver.WOComponent#sleep()
     */
    public void sleep()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "sleep(): hasSession = " + hasSession() );
            if ( hasSession() )
                log.debug( "sleep(): session = " + session().sessionID() );
        }
        super.sleep();
    }


    // ----------------------------------------------------------
    public boolean multipleAuthDomains()
    {
        return domainDisplayGroup.allObjects().count() > 1;
    }


    // ----------------------------------------------------------
    public boolean hasSpecificAuthDomain()
    {
        WORequest request = context().request();
        String auth = request.stringFormValueForKey( "institution" );
        if ( auth == null )
        {
            auth = request.stringFormValueForKey( "d" );
        }
        if ( auth != null )
        {
            try
            {
                log.debug( "looking up domain: " + auth );
                domain = AuthenticationDomain.authDomainByName( auth );
                specificAuthDomainName = auth;
            }
            catch ( EOObjectNotAvailableException e )
            {
                log.error( "Unrecognized institution parameter provided: '"
                    + auth + "'", e );
            }
            catch ( EOUtilities.MoreThanOneException e )
            {
                log.error( "Ambiguous institution parameter provided: '"
                    + auth + "'", e );
            }
        }
        return specificAuthDomainName != null;
    }


    // ----------------------------------------------------------
    public String specificAuthDomainName()
    {
        return specificAuthDomainName;
    }


    //~ Instance/static variables .............................................

    private String specificAuthDomainName;
    static Logger log = Logger.getLogger( PasswordChangeRequestPage.class );
}
