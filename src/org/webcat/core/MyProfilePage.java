/*==========================================================================*\
 |  $Id: MyProfilePage.java,v 1.7 2013/08/11 01:57:11 stedwar2 Exp $
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

import java.net.URLEncoder;
import org.apache.log4j.Logger;
import org.webcat.core.git.http.GitRequestHandler;
import org.webcat.core.webdav.WebDAVRequestHandler;
import org.webcat.ui.WCBasePage;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.eof.ERXQ;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXValueUtilities;

//-------------------------------------------------------------------------
/**
 * Represents a standard Web-CAT page that has not yet been implemented
 * (is "to be defined").
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.7 $, $Date: 2013/08/11 01:57:11 $
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
    public int                 index;
    public AuthenticationDomain.TimeZoneDescriptor zone;
    public AuthenticationDomain.TimeZoneDescriptor selectedZone;
    public String              backgroundUrl;
    public String              extraCss;

    // Display groups
    public WODisplayGroup      enrolledInDisplayGroup;
    public WODisplayGroup      TADisplayGroup;
    public WODisplayGroup      teachingDisplayGroup;

    // Repetition variables
    public CourseOffering      aCourseOffering;
    public String              aFormat;
    public User                anInstructor;
    public Theme               aTheme;
    public EOBase              aRepositoryProvider;
    public int                 indexOfARepositoryProvider;
    public boolean             openThemes;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse( WOResponse response, WOContext context )
    {
        now = new NSTimestamp();
        enrolledInDisplayGroup.setMasterObject( user() );
        teachingDisplayGroup.setMasterObject( user() );
        TADisplayGroup.setMasterObject( user() );
        if ( selectedZone == null )
        {
            selectedZone = AuthenticationDomain.descriptorForZone(
                user().timeZoneName() );
            if ( selectedZone == null )
            {
                // !!!
                selectedZone = AuthenticationDomain.descriptorForZone(
                    NSTimeZone.getDefault().getID() );
            }
        }
        log.debug("appendToResponse(), theme = " + user().theme());
        super.appendToResponse( response, context );
    }


    // ----------------------------------------------------------
    public WOComponent applyTimeFormats()
    {
        log.debug( "applyTimeFormats()" );
        user().setTimeZoneName( selectedZone.id );
        applyLocalChanges();
        wcSession().clearCachedTimeFormatter();
        openThemes = false;
        return null;
    }


    // ----------------------------------------------------------
    public WOComponent applyThemeCustomizations()
    {
        log.debug( "applyThemeCustomizations()" );
        user().preferences().takeValueForKey(
            backgroundUrl, "customBackgroundUrl");
        user().preferences().takeValueForKey(extraCss, "customCss");
        user().savePreferences();
        openThemes = true;
        return null;
    }


    // ----------------------------------------------------------
    @SuppressWarnings("deprecation")
    public String formattedCurrentTime()
    {
        com.webobjects.foundation.NSTimestampFormatter formatter =
            new com.webobjects.foundation.NSTimestampFormatter( aFormat );
        formatter.setDefaultFormatTimeZone(
            wcSession().timeFormatter().defaultFormatTimeZone() );
        return formatter.format( now );
    }


    // ----------------------------------------------------------
    public boolean applyLocalChanges()
    {
        log.debug( "applyLocalChanges()" );
        openThemes = false;
        User u = user();
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
                    || ( u.firstName() != null
                         && lcPassword.equals( u.firstName().toLowerCase() ) )
                    || ( u.lastName() != null
                         && lcPassword.equals( u.lastName().toLowerCase() ) ) )
            {
                error(
                    "You may not use your first name, last name, or user name "
                    + "as a password.  Please enter a different password." );
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
        String institution = user().authenticationDomain().name();
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
                + ( ( user().accessLevel() > 0 ) ? "&staff=true" : "" ),
            null,
            false,
            0,
            true // force to use http, not https
            );
    }


    // ----------------------------------------------------------
    public String eclipseUrl()
    {
        String institution = user().authenticationDomain().name();
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
                + ( ( user().accessLevel() > 0 ) ? "&staff=true" : "" ),
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
            User me = user();
            NSMutableArray<CourseOffering> offerings =
                me.enrolledIn().mutableClone();
            ERXArrayUtilities.addObjectsFromArrayWithoutDuplicates( offerings,
                me.graderFor() );
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
                crnList += offerings.objectAtIndex(i).crn();
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
                     && ( me.graderFor().count() > 0 || me.teaching().count() > 0 ) )
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


    // ----------------------------------------------------------
    public NSTimestamp now()
    {
        return now;
    }


    // ----------------------------------------------------------
    public NSArray<Theme> allThemes()
    {
        // TODO: Add dojo support to select whether to show developer themes
        if (allThemes == null)
        {
            allThemes = ERXQ.filtered(Theme.themes(),
                ERXQ.equals(Theme.IS_FOR_THEME_DEVELOPERS_KEY, false));
        }
        return allThemes;
    }


    // ----------------------------------------------------------
    public WOComponent previewTheme()
    {
        log.debug("previewTheme(): user theme = " + user().theme());
        log.debug("session theme = " + wcSession().theme());
        wcSession().setTemporaryTheme(
            user().theme() == null
                ? Theme.defaultTheme()
                : user().theme());
        log.debug("after preview, session theme = " + wcSession().theme());
        openThemes = true;
        return null;
    }


    // ----------------------------------------------------------
    public WOComponent changeTheme()
    {
        user().setTheme(aTheme);
        aTheme.setAsLastUsedThemeInContext(context());

        log.debug("Changing theme, before = " + user().theme());
        log.debug("user ec = " + user().editingContext()
            + ", theme ec = "
            + ((user().theme() == null)
                ? "null"
                : user().theme().editingContext().toString()));
        super.applyLocalChanges();
        log.debug("Changing theme, after = " + user().theme());
        log.debug("user ec = " + user().editingContext()
            + ", theme ec = "
            + ((user().theme() == null)
                ? "null"
                : user().theme().editingContext().toString()));
        log.debug("Changing theme, session theme = " + wcSession().theme());
        log.debug("sesssion theme ec = " + wcSession().theme().editingContext());
        log.debug("sharing ");
        openThemes = true;
        return null;
    }


    // ----------------------------------------------------------
    public boolean wantsDevelopmentJavascript()
    {
        return ERXValueUtilities.booleanValueWithDefault(
            user().preferences().valueForKey(
                WCBasePage.UNCOMPRESSED_SCRIPT_PREF_KEY),
                Application.isDevelopmentModeSafe());
    }


    // ----------------------------------------------------------
    public void setWantsDevelopmentJavascript(boolean value)
    {
        user().preferences().takeValueForKey(Boolean.toString(value),
            WCBasePage.UNCOMPRESSED_SCRIPT_PREF_KEY);
    }


    // ----------------------------------------------------------
    public NSArray<? extends EOBase> repositoryProviders()
    {
        return RepositoryManager.getInstance().repositoriesPresentedToUser(user(),
                    localContext());
    }


    // ----------------------------------------------------------
    public String aRepositoryProviderURL()
    {
        return GitRequestHandler.completeURLForRepositoryPath(
                context(), aRepositoryProvider, null);
    }


    // ----------------------------------------------------------
    public String webdavURL()
    {
        return WebDAVRequestHandler.completeURLForPath(context(), null);
    }


    //~ Instance/static variables .............................................

    private String icalUrl;
    private NSTimestamp now;
    private NSArray<Theme> allThemes;
    static Logger log = Logger.getLogger( MyProfilePage.class );
}
