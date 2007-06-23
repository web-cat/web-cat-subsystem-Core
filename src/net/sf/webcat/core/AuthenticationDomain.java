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

import com.sun.org.apache.xml.internal.serializer.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;

import er.extensions.*;

import java.io.File;
import java.util.*;

import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Represents an institution or organization, and an associated authentication
 * policy.  Some institutions may have multiple AuthenticationDomain objects
 * that each represent a different authentication policy/mechanism for
 * different classes of user names.
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class AuthenticationDomain
    extends _AuthenticationDomain
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new AuthenticationDomain object.
     */
    public AuthenticationDomain()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Look up and return an authentication domain object by its
     * (partial) property name.  If you use a property called
     * <code>authenticator.<i>MyAuthenticator</i></code> to define
     * an authentication domain in a property file, then you can retrieve
     * this authenticator using the name "<i>MyAuthenticator</i>".
     *
     * @param name the property name of the authenticator
     * @return an array of all AuthenticationDomain objects
     */
    public static AuthenticationDomain authDomainByName( String name )
    {
        ensureAuthDomainsLoaded();
        return (AuthenticationDomain)EOUtilities.objectMatchingKeyAndValue(
                EOSharedEditingContext.defaultSharedEditingContext(),
                ENTITY_NAME,
                PROPERTY_NAME_KEY,
                "authenticator." + name
            );
    }


    // ----------------------------------------------------------
    /**
     * Get a list of shared authentication domain objects that have
     * already been loaded into the shared editing context.
     * @return an array of all AuthenticationDomain objects
     */
    public static NSArray authDomains()
    {
        ensureAuthDomainsLoaded();
        return authDomains;
    }


    // ----------------------------------------------------------
    /**
     * Load the list of shared authentication domain objects, if it hasn't
     * been done already.
     */
    public static void ensureAuthDomainsLoaded()
    {
        if ( authDomains == null )
        {
            refreshAuthDomains();
        }
    }


    // ----------------------------------------------------------
    /**
     * Force reloading of the list of shared authentication domain objects.
     */
    public static void refreshAuthDomains()
    {
        log.debug( "refreshAuthDomains()" );
        theAuthenticatorMap = new TreeMap();

        WCProperties     properties    = Application.configurationProperties();
        Enumeration      propertyNames = properties.propertyNames();
        final String     prefix        = "authenticator.";
        EOEditingContext ec            = Application.newPeerEditingContext();

        try
        {
            ec.lock();
            log.debug( "searching property list" );
            // Disconnect from the shared editing context, since we're actually
            // making changes to objects in the shared context here.
            ec.setSharedEditingContext( null );
            while ( propertyNames.hasMoreElements() )
            {
                AuthenticationDomain domain = null;
                String base = (String)propertyNames.nextElement();
                // log.debug ( "checking property: " + base );
                if (    base.startsWith( prefix )
                     && !base.equals( "authenticator.default" )
                     && base.substring( prefix.length() ).indexOf( '.' ) == -1 )
                {
                    log.debug ( "trying to register: " + base );
                    UserAuthenticator ua = null;
                    String uaClassName   = properties.getProperty( base );
                    if ( uaClassName == null || uaClassName.equals( "" ) )
                    {
                        uaClassName = properties.getProperty(
                            "authenticator.default.class" );
                    }
                    try
                    {
                        ua = (UserAuthenticator)
                            Class.forName( uaClassName ).newInstance();
                        log.info( "registering " + base + " with " + uaClassName );
                        ua.configure( base, properties );
                        theAuthenticatorMap.put( base, ua );
                        try
                        {
                            domain = (AuthenticationDomain)
                                EOUtilities.objectMatchingKeyAndValue(
                                    ec,
                                    AuthenticationDomain.ENTITY_NAME,
                                    AuthenticationDomain.PROPERTY_NAME_KEY,
                                    base
                                    );
                            log.debug( "domain is already in database" );
                            if ( domain.propertyName() != base )
                            {
                                domain.setPropertyName( base );
                            }
                            String displayableName =
                                properties.getProperty( base + "."
                                    + AuthenticationDomain.DISPLAYABLE_NAME_KEY );
                            if ( displayableName != null
                                 && domain.displayableName()
                                    != displayableName )
                            {
                                domain.setDisplayableName( displayableName );
                            }
                            String emailDomain =
                                properties.getProperty( base + "."
                                    + AuthenticationDomain.DEFAULT_EMAIL_DOMAIN_KEY );
                            if ( emailDomain != null
                                 && domain.defaultEmailDomain() != emailDomain )
                            {
                                domain.setDefaultEmailDomain( emailDomain );
                            }
                        }
                        catch ( EOObjectNotAvailableException e )
                        {
                            log.info( "Adding to AuthenticationDomain table: "
                                      + base );
                            domain = new AuthenticationDomain();
                            ec.insertObject( domain );
                            domain.setPropertyName( base );
                            domain.setDisplayableName(
                                properties.getProperty( base + "."
                                    + AuthenticationDomain.DISPLAYABLE_NAME_KEY ));
                            domain.setDefaultEmailDomain(
                                properties.getProperty( base + "."
                                + AuthenticationDomain.DEFAULT_EMAIL_DOMAIN_KEY ) );
                        }
                        catch ( EOUtilities.MoreThanOneException e )
                        {
                            log.error( "Error updating AuthenticationDomain table "
                                       + "for " + base, e );
                        }
                        if ( domain != null )
                        {
                            String emailDomain = properties.getProperty( base + "."
                                + AuthenticationDomain.DEFAULT_EMAIL_DOMAIN_KEY );
                            if ( emailDomain != null )
                            {
                                domain.setDefaultEmailDomain( emailDomain );
                            }
                        }
                    }
                    catch ( Exception e )
                    {
                        log.error( "refreshAuthDomains(): "
                                   + "cannot create "
                                   + uaClassName
                                   + ":",
                                   e
                                   );
                    }
                }
            }
            ec.saveChanges();
        }
        finally
        {
            ec.unlock();
            Application.releasePeerEditingContext( ec );
        }

        log.debug( "refreshing shared authentication domain objects" );
        authDomains = objectsForFetchAll(
            EOSharedEditingContext.defaultSharedEditingContext() );

        // TODO: can't do this yet, since the domain's authenticator class
        // and config settings are not stored in the database!
        // We'll need to change that, eventually.

        // confirm that  all domains are registered in the map
//        for ( int i = 0; i < authDomains.count(); i++ )
//        {
//            AuthenticationDomain domain = (AuthenticationDomain)
//                authDomains.objectAtIndex( i );
//            String name = domain.propertyName();
//            if ( theAuthenticatorMap.get( name ) == null )
//            {
//
//            }
//        }
    }


    // ----------------------------------------------------------
    /**
     * Get the list of available time formats for users to choose from.
     * This value is loaded/parsed from the <code>timeFormats</code>
     * configuration property, set under the Core subsystem's configuration
     * settings.  The list should be a list of strings containing patterns
     * acceptable by {@link NSTimestampFormatter}.
     * @return an NSArray of strings representing the time formats available
     */
    public static NSArray availableTimeFormats()
    {
        if ( timeFormats == null )
        {
            try
            {
                timeFormats = Application.configurationProperties()
                    .arrayForKey( "timeFormats" );
            }
            catch ( Exception e )
            {
                log.error( "Exception parsing \"timeFormats\" property "
                    + "setting as an NSArray:", e );
            }
            if ( timeFormats == null )
            {
                timeFormats = new NSArray( new String[]{"%I:%M%p", "%H:%M"} );
            }
        }
        return timeFormats;
    }


    // ----------------------------------------------------------
    /**
     * An instance method wrapper for {@link #availableTimeFormats()} to
     * provide KVC access to that static method.
     * @return an NSArray of strings representing the time formats available
     */
    public NSArray timeFormats()
    {
        return availableTimeFormats();
    }


    // ----------------------------------------------------------
    /**
     * Get the default time format pattern for users to choose from.
     * The default is the first entry in the {@link #availableTimeFormats()}
     * list.
     * @return the default time format pattern
     */
    public static String globalDefaultTimeFormat()
    {
        return (String)availableTimeFormats().objectAtIndex( 0 );
    }


    // ----------------------------------------------------------
    /**
     * Get the list of available date formats for users to choose from.
     * This value is loaded/parsed from the <code>dateFormats</code>
     * configuration property, set under the Core subsystem's configuration
     * settings.  The list should be a list of strings containing patterns
     * acceptable by {@link NSTimestampFormatter}.
     * @return an NSArray of strings representing the date formats available
     */
    public static NSArray availableDateFormats()
    {
        if ( dateFormats == null )
        {
            try
            {
                dateFormats = Application.configurationProperties()
                    .arrayForKey( "dateFormats" );
            }
            catch ( Exception e )
            {
                log.error( "Exception parsing \"dateFormats\" property "
                    + "setting as an NSArray:", e );
            }
            if ( dateFormats == null )
            {
                dateFormats = new NSArray( new String[]{
                    "%m/%d/%y", "%m/%d/%Y", "%d.%m.%Y", "%d.%m.%y",
                    "%y-%m-%d", "%Y-%m-%d", "%d-%b-%y", "%d-%b-%Y" } );
            }
        }
        return dateFormats;
    }


    // ----------------------------------------------------------
    /**
     * An instance method wrapper for {@link #availableDateFormats()} to
     * provide KVC access to that static method.
     * @return an NSArray of strings representing the date formats available
     */
    public NSArray dateFormats()
    {
        return availableDateFormats();
    }


    // ----------------------------------------------------------
    /**
     * Get the default date format pattern for users to choose from.
     * The default is the first entry in the {@link #availableDateFormats()}
     * list.
     * @return the default date format pattern
     */
    public static String globalDefaultDateFormat()
    {
        return (String)availableDateFormats().objectAtIndex( 0 );
    }


    // ----------------------------------------------------------
    /**
     * Get the time format pattern associated with this authentication
     * domain.
     * @return the time format pattern
     */
    public String timeFormat()
    {
        String result = super.timeFormat();
        if ( result == null || result.equals( "" ) )
        {
            result = globalDefaultTimeFormat();
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Get the date format pattern associated with this authentication
     * domain.
     * @return the date format pattern
     */
    public String dateFormat()
    {
        String result = super.dateFormat();
        if ( result == null || result.equals( "" ) )
        {
            result = globalDefaultDateFormat();
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * A simple class that combines a time zone ID with a printable
     * time zone name.
     */
    public static class TimeZoneDescriptor
    {
        public String id;
        public String printableName;

        public TimeZoneDescriptor( String id )
        {
            this.id = id;
            printableName = id.replaceAll( "_", " " ).replaceAll( "/", ": " );
        }

        public NSTimeZone timeZone()
        {
            return NSTimeZone.timeZoneWithName( id, true );
        }

        public boolean equals( Object obj )
        {
            boolean result = false;
            if ( obj instanceof TimeZoneDescriptor )
            {
                TimeZoneDescriptor rhs = (TimeZoneDescriptor)obj;
                result = ( id == null )
                    ? ( rhs.id == null )
                    : ( id.equals( rhs.id ) );
                result = result && ( ( printableName == null )
                    ? ( rhs.printableName == null )
                    : ( printableName.equals( rhs.printableName ) ) );
            }
            return result;
        }
    }


    // ----------------------------------------------------------
    /**
     * Get the list of available date formats for users to choose from.
     * This value is loaded/parsed from the <code>dateFormats</code>
     * configuration property, set under the Core subsystem's configuration
     * settings.  The list should be a list of strings containing patterns
     * acceptable by {@link NSTimestampFormatter}.
     * @return an NSArray of strings representing the date formats available
     */
    public static NSArray availableTimeZones()
    {
        if ( timeZones == null )
        {
            NSMutableArray zones =
                new NSMutableArray( NSTimeZone.knownTimeZoneNames() );
            for ( int i = 0; i < zones.count(); i++ )
            {
                zones.replaceObjectAtIndex(
                    new TimeZoneDescriptor( (String)zones.objectAtIndex( i ) ),
                    i );
            }
            ERXArrayUtilities.sortArrayWithKey( zones, "printableName" );
            timeZones = zones;
        }
        return timeZones;
    }


    // ----------------------------------------------------------
    /**
     * Get the {@link TimeZoneDescriptor} associated with the specified
     * time zone name (id).
     * @param id the time zone name (id) to look for
     * @return The matching descriptor from the {@link #availableTimeZones()}
     * list
     */
    public static TimeZoneDescriptor descriptorForZone( String id )
    {
        NSArray zones = availableTimeZones();
        for ( int i = 0; i < zones.count(); i++ )
        {
            TimeZoneDescriptor tzd =
                (TimeZoneDescriptor)zones.objectAtIndex( i );
            if ( tzd.id.equals( id ) )
            {
                return tzd;
            }
        }
        return null;
    }


    // ----------------------------------------------------------
    /**
     * An instance method wrapper for {@link #availableTimeZones()} to
     * provide KVC access to that static method.
     * @return an NSArray of strings representing the date formats available
     */
    public NSArray timeZones()
    {
        return availableTimeZones();
    }


    // ----------------------------------------------------------
    /**
     * Get the time zone name associated with this authentication
     * domain.
     * @return the time zone name
     */
    public String timeZoneName()
    {
        String result = super.timeZoneName();
        if ( result == null || result.equals( "" ) )
        {
            result = NSTimeZone.getDefault().getID();
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Get the name of this authenticator (the property name, without the
     * "authenticator." prefix).
     * @return the name as a string
     */
    public String name()
    {
        if ( cachedName == null )
        {
            String name = propertyName();
            final String propertyPrefix = "authenticator.";
            if ( name != null )
            {
                // strip the prefix on the property name
                if ( name.startsWith( propertyPrefix ) )
                {
                    name = name.substring( propertyPrefix.length() );
                }
            }
            cachedName = name;
        }
        return cachedName;
    }


    // ----------------------------------------------------------
    /**
     * Generate a name usable as a subdirectory name from this
     * objects property name.
     * @return a subdirectory name
     */
    public String subdirName()
    {
        if ( cachedSubdirName == null )
        {
            String name = name();
            if ( name != null )
            {
                // strip out irregular characters
                char[] chars = new char[ name.length() ];
                int  pos   = 0;
                for ( int i = 0; i < name.length(); i++ )
                {
                    char c = name.charAt( i );
                    if ( Character.isLetterOrDigit( c ) ||
                         c == '_'                       ||
                         c == '-' )
                    {
                        chars[ pos ] = c;
                        pos++;
                    }
                }
                cachedSubdirName = new String( chars, 0, pos );
            }
        }
        return cachedSubdirName;
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>propertyName</code>
     * property.  Takes care of renaming the associated subdirectories
     * for this domain as well.
     *
     * @param value The new value for this property
     */
    public void setPropertyName( String value )
    {
        if ( value != null && propertyName() != null
             && !value.equals( propertyName() ) )
        {
            String oldSubdir = subdirName();
            cachedName = null;
            cachedSubdirName = null;
            super.setPropertyName( value );
            String newSubdir = subdirName();
            // rename the three key directories
            {
                File parent = new File( Application.configurationProperties()
                                        .getProperty( "grader.submissiondir" ) );
                File oldDir = new File( parent, oldSubdir );
                oldDir.renameTo( new File( parent, newSubdir ) );
            }
            {
                File parent = new File( Application.configurationProperties()
                                        .getProperty( "grader.workarea" ) );
                File oldDir = new File( parent, oldSubdir );
                oldDir.renameTo( new File( parent, newSubdir ) );
            }
            {
                String scriptRoot = Application.configurationProperties()
                    .getProperty( "grader.scriptsroot" );
                if ( scriptRoot == null )
                {
                    scriptRoot = net.sf.webcat.core.Application.configurationProperties()
                        .getProperty( "grader.submissiondir" )
                        + "/UserScripts";
                }
                File parent = new File( scriptRoot );
                File oldDir = new File( parent, oldSubdir );
                oldDir.renameTo( new File( parent, newSubdir ) );

                String scriptDataRoot = Application.configurationProperties()
                    .getProperty( "grader.scriptsdataroot" );
                if ( scriptDataRoot == null )
                {
                    scriptDataRoot = scriptRoot + "Data";
                }
                parent = new File( scriptDataRoot );
                oldDir = new File( parent, oldSubdir );
                oldDir.renameTo( new File( parent, newSubdir ) );
            }
        }
        else
        {
            cachedSubdirName = null;
            super.setPropertyName( value );
        }
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the user authenticator associated with this domain.
     * @return an authenticator object to use when validating credentials
     *         for users in this authentication domain
     */
    public UserAuthenticator authenticator()
    {
        return (UserAuthenticator)theAuthenticatorMap.get( propertyName() );
    }


    // ----------------------------------------------------------
    /**
     * Get a human-readable representation of this authenticator, which is
     * the same as {@link #name()}.
     * @return this authenticator's property name
     */
    public String userPresentableDescription()
    {
        return name();
    }


    // ----------------------------------------------------------
    /**
     * Get a human-readable representation of this authenticator, which is
     * the same as {@link #userPresentableDescription()}.
     * @return this authenticator's property name
     */
    public String toString()
    {
        return userPresentableDescription();
    }


// If you add instance variables to store property values you
// should add empty implementions of the Serialization methods
// to avoid unnecessary overhead (the properties will be
// serialized for you in the superclass).

//    // ----------------------------------------------------------
//    /**
//     * Serialize this object (an empty implementation, since the
//     * superclass handles this responsibility).
//     * @param out the stream to write to
//     */
//    private void writeObject( java.io.ObjectOutputStream out )
//        throws java.io.IOException
//    {
//    }
//
//
//    // ----------------------------------------------------------
//    /**
//     * Read in a serialized object (an empty implementation, since the
//     * superclass handles this responsibility).
//     * @param in the stream to read from
//     */
//    private void readObject( java.io.ObjectInputStream in )
//        throws java.io.IOException, java.lang.ClassNotFoundException
//    {
//    }


    //~ Instance/static variables .............................................

    private static NSArray authDomains;
    private static Map theAuthenticatorMap;
    private String cachedSubdirName = null;
    private String cachedName = null;
    private static NSArray timeFormats;
    private static NSArray dateFormats;
    private static NSArray timeZones;

    static Logger log = Logger.getLogger( AuthenticationDomain.class );
}
