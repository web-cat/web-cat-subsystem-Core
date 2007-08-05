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
import com.webobjects.foundation.*;
import er.extensions.ERXArrayUtilities;
import java.net.URLEncoder;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
* Represents a standard Web-CAT page that has not yet been implemented
* (is "to be defined").
*
*  @author Stephen Edwards
*  @version $Id$
*/
public class MyProfilePage
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new TBDPage object.
     *
     * @param context The context to use
     */
    public MyProfilePage( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public String              newPassword1;
    public String              newPassword2;
    public WODisplayGroup      enrolledInDisplayGroup;
    public User                instructor;
    public WODisplayGroup      TADisplayGroup;
    public WODisplayGroup      teachingDisplayGroup;
    public CourseOffering      courseOffering;
    public int                 index;
    public AuthenticationDomain.TimeZoneDescriptor zone;
    public AuthenticationDomain.TimeZoneDescriptor selectedZone;
    public String              aFormat;
    public NSTimestamp         now;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse( WOResponse response, WOContext context )
    {
        now = new NSTimestamp();
        enrolledInDisplayGroup.setMasterObject( wcSession().user() );
        teachingDisplayGroup.setMasterObject( wcSession().user() );
        TADisplayGroup.setMasterObject( wcSession().user() );
        if ( selectedZone == null )
        {
            selectedZone = AuthenticationDomain.descriptorForZone(
                wcSession().user().timeZoneName() );
            if ( selectedZone == null )
            {
                // !!!
                selectedZone = AuthenticationDomain.descriptorForZone(
                    NSTimeZone.getDefault().getID() );
            }
        }
        super.appendToResponse( response, context );
    }


    // ----------------------------------------------------------
    public WOComponent applyTimeFormats()
    {
        log.debug( "applyTimeFormats()" );
        wcSession().user().setTimeZoneName( selectedZone.id );
        wcSession().commitLocalChanges();
        wcSession().clearCachedTimeFormatter();
        return null;
    }


    // ----------------------------------------------------------
    public String formattedCurrentTime()
    {
        NSTimestampFormatter formatter = new NSTimestampFormatter( aFormat );
        formatter.setDefaultFormatTimeZone(
            wcSession().timeFormatter().defaultFormatTimeZone() );
        return formatter.format( now );
    }


    // ----------------------------------------------------------
    public boolean applyLocalChanges()
    {
        log.debug( "applyLocalChanges()" );
        User u = wcSession().localUser();
        String lcPassword = ( newPassword1 == null )
            ? null
            : newPassword1.toLowerCase();
        if (  u.canChangePassword()
           && (  newPassword1 != null
              || newPassword2 != null ) )
        {
            if ( newPassword1 == null || newPassword2 == null )
            {
                error(
                    "To change your password, complete both password fields." );
            }
            else if ( !newPassword1.equals( newPassword2 ) )
            {
                error(
                    "The two password fields do not match.  "
                    + "Please re-enter your password." );
            }
            else if ( newPassword1.length() < 6 )
            {
                error(
                    "Your password must be at least six characters long." );
            }
            else if (  lcPassword.equals( u.userName().toLowerCase() )
                    || lcPassword.equals( u.firstName().toLowerCase() )
                    || lcPassword.equals( u.lastName().toLowerCase() ) )
            {
                error(
                    "You may not use your name as a password.  "
                    + "Please enter a different password." );
            }
            else if ( newPassword1.equals( u.password() ) )
            {
                error( "The password you have specified is already "
                       + "your current password." );
            }
            if ( !hasMessages() )
            {
                u.changePassword( newPassword1 );
                confirmationMessage( "Your password has been changed." );
            }
        }
        newPassword1 = null;
        newPassword2 = null;
        // TODO: include user validation here
//        wcSession().commitLocalChanges();
//        return pageWithName( wcSession().currentTab().selectDefaultSibling()
//                             .selectedPageName() );
        return super.applyLocalChanges();
    }


    // ----------------------------------------------------------
    public String bluejUrl()
    {
        String institution = wcSession().user().authenticationDomain().name();
        try
        {
            institution = URLEncoder.encode( institution, "UTF-8" );
        }
        catch ( java.io.UnsupportedEncodingException e )
        {
            log.error( "Cannot encode in UTF-8", e );
        }
        return Application.completeURLWithRequestHandlerKey(
            context(),
            Application.application().directActionRequestHandlerKey(),
            "assignments/bluej?institution=" + institution
                + ( ( wcSession().user().accessLevel() > 0 )
                                ? "&staff=true" : "" ),
            null,
            false,
            0,
            true // force to use http, not https
            );
    }


    // ----------------------------------------------------------
    public String eclipseUrl()
    {
        String institution = wcSession().user().authenticationDomain().name();
        try
        {
            institution = URLEncoder.encode( institution, "UTF-8" );
        }
        catch ( java.io.UnsupportedEncodingException e )
        {
            log.error( "Cannot encode in UTF-8", e );
        }
        return Application.completeURLWithRequestHandlerKey(
            context(),
            Application.application().directActionRequestHandlerKey(),
            "assignments/eclipse?institution=" + institution
                + ( ( wcSession().user().accessLevel() > 0 )
                                ? "&staff=true" : "" ),
            null,
            true,
            0
            );
    }


    // ----------------------------------------------------------
    public String icalUrl()
    {
        if ( icalUrl == null )
        {
            String crnList = null;
            User me = wcSession().user();
            NSMutableArray offerings = me.enrolledIn().mutableClone();
            ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates( offerings,
                me.TAFor() );
            ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates( offerings,
                me.teaching() );
            for ( int i = 0; i < offerings.count(); i++ )
            {
                if ( i == 0 )
                {
                    crnList = "";
                }
                else
                {
                    crnList += ',';
                }
                crnList +=
                    ( (CourseOffering)offerings.objectAtIndex( i ) ).crn();
            }
            if ( crnList!= null )
            {
                try
                {
                    crnList = URLEncoder.encode( crnList, "UTF-8" );
                }
                catch ( java.io.UnsupportedEncodingException e )
                {
                    log.error( "Cannot encode in UTF-8", e );
                }
                crnList = "?crns=" + crnList;
                if ( me.accessLevel() > 0
                     && ( me.TAFor().count() > 0 || me.teaching().count() > 0 ) )
                {
                    crnList += "&staff=true";
                }
            }
            icalUrl = Application.completeURLWithRequestHandlerKey(
                context(),
                Application.application().directActionRequestHandlerKey(),
                "assignments/ical.ics" + crnList,
                null,
                true,
                0
                );
        }
        return icalUrl;
    }


    // ----------------------------------------------------------
    public String webcalUrl()
    {
        String url = icalUrl();
        int pos = url.indexOf( ':' );
        url = "webcal" + url.substring( pos );
        return url;
    }


    //~ Instance/static variables .............................................

    private String icalUrl;
    static Logger log = Logger.getLogger( MyProfilePage.class );
}
