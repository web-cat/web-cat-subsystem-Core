/*==========================================================================*\
 |  $Id: PasswordChangeRequest.java,v 1.2 2011/03/07 18:44:37 stedwar2 Exp $
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

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import java.security.*;
import java.util.*;
import org.webcat.core.Application;
import org.webcat.core.PasswordChangeRequest;
import org.webcat.core.User;
import org.webcat.core.WCProperties;
import org.webcat.core._PasswordChangeRequest;
import org.apache.log4j.*;

// -------------------------------------------------------------------------
/**
 * Represents a user's request to change their password, because they forgot
 * the old one.
 *
 * @author Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2011/03/07 18:44:37 $
 */
public class PasswordChangeRequest
    extends _PasswordChangeRequest
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new PasswordChangeRequest object.
     */
    public PasswordChangeRequest()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Look up the password change request identified by the given code,
     * checking it for expiration first.
     * @param ec   The editing context to use
     * @param code The code to look up
     * @return The password change request object, if found (and not expired),
     * or null otherwise
     */
    public boolean hasExpired()
    {
        NSTimestamp myExpireTime = expireTime();
        return myExpireTime != null && myExpireTime.before(new NSTimestamp());
    }


    // ----------------------------------------------------------
    /**
     * Look up the password change request identified by the given code,
     * checking it for expiration first.
     * @param ec   The editing context to use
     * @param requestCode The code to look up
     * @return The password change request object, if found (and not expired),
     * or null otherwise
     */
    public static PasswordChangeRequest requestForCode(
        EOEditingContext ec, String requestCode )
    {
        PasswordChangeRequest request = null;
        NSArray<PasswordChangeRequest> results =
            requestsForCode(ec, requestCode);
        if ( results.count() > 0 )
        {
            request = results.objectAtIndex( 0 );
            if ( request.hasExpired() )
            {
                // Expired timestamp, so delete it!
                request.delete();
                ec.saveChanges();
                request = null;
            }
        }
        return request;
    }


    // ----------------------------------------------------------
    /**
     * Delete any pending password change requests for the given user.
     * @param ec   The editing context to use
     * @param forUser The user requesting the password reset instructions
     * @return True if any pending requests were found and deleted, or
     * false if none were found
     */
    public static boolean clearPendingUserRequests(
        EOEditingContext ec, User forUser )
    {
        NSArray<PasswordChangeRequest> results = requestsForUser(ec, forUser);
        boolean result = results.count() > 0;
        for ( int i = 0; i < results.count(); i++ )
        {
            PasswordChangeRequest pcr = results.objectAtIndex( i );
            pcr.delete();
        }
        if ( result )
        {
            ec.saveChanges();
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Generate a new PasswordChangeRequest object for the given user, store
     * it in the database, and then e-mail the user instructions on how to
     * change their password.
     * @param ec   The editing context to use
     * @param forUser The user requesting the password reset instructions
     */
    public static void sendPasswordResetEmail(
        EOEditingContext ec, User forUser )
    {
        log.info( "Creating password change ticket for: "
            + forUser.nameAndUid() );
        PasswordChangeRequest pcr = (PasswordChangeRequest)
        EOUtilities.createAndInsertInstance( ec, ENTITY_NAME );
        pcr.setUserRelationship( forUser );
        NSTimestamp now = new NSTimestamp();
        pcr.setExpireTime( now.timestampByAddingGregorianUnits(
            0, 0, 1, 0, 0, 0 ) );
        pcr.setCode(digestString(forUser.userName() + formatter.format(now)
            + forUser.email() + Integer.toString(random.nextInt())));
        ec.saveChanges();

        WCProperties properties =
            new WCProperties( Application.configurationProperties() );
        forUser.addPropertiesTo( properties );
        if ( properties.getProperty( "login.url" ) == null )
        {
            String dest = Application.application().servletConnectURL();
            properties.setProperty( "login.url", dest );
        }
        properties.setProperty( "password.change.url",
            properties.getProperty( "base.url" )
            + "/wa/passwordChangeRequest?code="
            + pcr.code() );
        Application.sendSimpleEmail(
            forUser.email() ,
            properties.stringForKeyWithDefault(
                "PasswordChangeRequest.email.title",
                "How to change your Web-CAT password" ),
            properties.stringForKeyWithDefault(
                "PasswordChangeRequest.email.message",
                "Web-CAT has received a password change request for your "
                + "account with\nuser name ${user.userName}.\n\nTo change "
                + "your Web-CAT password, open the following link in your "
                + "Web\nbrowser to view your account properties and enter "
                + "your new password:\n\n${password.change.url}\n\n"
                + "This personalized password reset URL will only be active "
                + "for 24 hours\nand can only be used once, after which it "
                + "will no longer be valid.\n\nIf you did not make this "
                + "request, please visit the URL above and\nthen click the "
                + "logout link on the upper right of the resulting page\n"
                + "without changing your password.  This will cancel the "
                + "reset request."
                )
            );
    }


    // ----------------------------------------------------------
    /**
     * Produces a Base64-encoded SHA digest for <code>aString</code>.
     *
     * @param aString the text to digest
     * @return the corresponding SHA digest
     */
    public static String digestString( String aString )
    {
        String digestedString = null;

        try
        {
            // Make sure no one else is trying to use the static data
            // members, since the MessageDigest includes internal state
            // and is not synchronized.  Lock on the encoder, however, since
            // it is possible that the digester has not yet been initialized.
            synchronized ( encoder )
            {
                if ( digester == null )
                {
                    digester = MessageDigest.getInstance( "SHA" );
                }

                // Convert to text by encoding it in Base64
                digestedString =
                    encoder.encode( digester.digest( aString.getBytes() ) );

                // Trim any trailing equals sign(s) for URL param safety
                int pos = digestedString.indexOf(  '=' );
                if ( pos >= 0 )
                {
                    digestedString = digestedString.substring( 0, pos );
                }

                // Substitute + by * and / by - for URL convenience
                digestedString = digestedString.replaceAll( "[+]", "*" )
                    .replaceAll( "/", "-" );
            }
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new NSForwardException( e );
        }

        return digestedString;
    }


    //~ Instance/static variables .............................................

    private static MessageDigest digester;
    private static sun.misc.BASE64Encoder encoder =
        new sun.misc.BASE64Encoder();
    @SuppressWarnings("deprecation")
    private static NSTimestampFormatter formatter =
        new NSTimestampFormatter( "%Y%m%d%H%M%S%F" );
    private static Random random = new Random();

    static Logger log = Logger.getLogger( PasswordChangeRequest.class );
}
