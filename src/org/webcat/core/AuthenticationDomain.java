/*==========================================================================*\
 |  $Id: AuthenticationDomain.java,v 1.7 2012/05/16 13:31:34 stedwar2 Exp $
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

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import er.extensions.foundation.*;
import java.io.File;
import java.util.*;
import org.webcat.core.Application;
import org.webcat.core.AuthenticationDomain;
import org.webcat.core.UserAuthenticator;
import org.webcat.core.WCProperties;
import org.webcat.core._AuthenticationDomain;
import org.webcat.woextensions.ECAction;
import static org.webcat.woextensions.ECAction.run;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Represents an institution or organization, and an associated authentication
 * policy.  Some institutions may have multiple AuthenticationDomain objects
 * that each represent a different authentication policy/mechanism for
 * different classes of user names.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.7 $, $Date: 2012/05/16 13:31:34 $
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


    //~ Constants .............................................................

    public static final String COOKIE_LAST_USED_INSTITUTION =
        "org.webcat.core.AuthenticationDomain.lastUsed";


    //~ Methods ...............................................................

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
     * Change the value of this object's <code>propertyName</code>
     * property.  Takes care of renaming the associated subdirectories
     * for this domain as well.
     *
     * @param value The new value for this property
     */
    public void setPropertyName(String value)
    {
        if (value != null && propertyName() != null
            && !value.equals( propertyName()))
        {
            String oldSubdir = subdirName();
            cachedName = null;
            cachedSubdirName = null;
            super.setPropertyName(value);
            String newSubdir = subdirName();
            // rename the three key directories
            {
                File parent = new File(Application.configurationProperties()
                    .getProperty("grader.submissiondir"));
                File oldDir = new File(parent, oldSubdir);
                oldDir.renameTo(new File(parent, newSubdir));
            }
            {
                File parent = new File(Application.configurationProperties()
                    .getProperty("grader.workarea"));
                File oldDir = new File(parent, oldSubdir);
                oldDir.renameTo(new File(parent, newSubdir));
            }
            {
                String scriptRoot = Application.configurationProperties()
                    .getProperty("grader.scriptsroot");
                if (scriptRoot == null)
                {
                    scriptRoot = Application.configurationProperties()
                        .getProperty("grader.submissiondir")
                        + "/UserScripts";
                }
                File parent = new File(scriptRoot);
                File oldDir = new File(parent, oldSubdir);
                oldDir.renameTo(new File(parent, newSubdir));

                String scriptDataRoot = Application.configurationProperties()
                    .getProperty("grader.scriptsdataroot");
                if (scriptDataRoot == null)
                {
                    scriptDataRoot = scriptRoot + "Data";
                }
                parent = new File(scriptDataRoot);
                oldDir = new File(parent, oldSubdir);
                oldDir.renameTo(new File(parent, newSubdir));
            }
        }
        else
        {
            cachedSubdirName = null;
            super.setPropertyName(value);
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
        return theAuthenticatorMap.get(propertyName());
    }


    // ----------------------------------------------------------
    /**
     * Get the name of this authenticator (the property name, without the
     * "authenticator." prefix).
     * @return the name as a string
     */
    public String name()
    {
        if (cachedName == null)
        {
            String name = propertyName();
            final String propertyPrefix = "authenticator.";
            if (name != null)
            {
                // strip the prefix on the property name
                if (name.startsWith(propertyPrefix))
                {
                    name = name.substring(propertyPrefix.length());
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
        if (cachedSubdirName == null)
        {
            cachedSubdirName = subdirNameOf(name());
        }
        return cachedSubdirName;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the name of the directory where all submissions for any
     * user in this authentication domain are stored.
     * @return the directory name as a string buffer, so that additional
     *     subdir components can be appended easily.
     */
    public StringBuffer submissionBaseDirBuffer()
    {
        StringBuffer dir = new StringBuffer(50);
        // Strictly speaking, this info is associated with the Grader
        // subsystem and should be located there, but it has leaked into
        // core because it is needed in too many places.
        dir.append(Application.configurationProperties()
            .getProperty("grader.submissiondir"));
        dir.append('/');
        dir.append(subdirName());
        return dir;
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
        if (result == null || result.equals(""))
        {
            result = NSTimeZone.getDefault().getID();
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * An instance method wrapper for {@link #availableTimeZones()} to
     * provide KVC access to that static method.
     * @return an NSArray of strings representing the date formats available
     */
    public NSArray<TimeZoneDescriptor> timeZones()
    {
        return availableTimeZones();
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
        if (result == null || result.equals(""))
        {
            result = globalDefaultTimeFormat();
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * An instance method wrapper for {@link #availableTimeFormats()} to
     * provide KVC access to that static method.
     * @return an NSArray of strings representing the time formats available
     */
    public NSArray<String> timeFormats()
    {
        return availableTimeFormats();
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
        if (result == null || result.equals(""))
        {
            result = globalDefaultDateFormat();
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * An instance method wrapper for {@link #availableDateFormats()} to
     * provide KVC access to that static method.
     * @return an NSArray of strings representing the date formats available
     */
    public NSArray<String> dateFormats()
    {
        return availableDateFormats();
    }


    //~ Public Static Methods .................................................

    // ----------------------------------------------------------
    /**
     * Look up and return an authentication domain object by its
     * (partial) property name.  If you use a property called
     * <code>authenticator.<i>MyAuthenticator</i></code> to define
     * an authentication domain in a property file, then you can retrieve
     * this authenticator using the name "<i>MyAuthenticator</i>".
     *
     * @param name the property name of the authenticator
     * @return The matching AuthenticationDomain object
     */
    public static AuthenticationDomain authDomainByName(String name)
    {
        ensureAuthDomainsLoaded();
        return firstObjectMatchingQualifier(
            EOSharedEditingContext.defaultSharedEditingContext(),
            propertyName.eq("authenticator." + name),
            null);
    }


    // ----------------------------------------------------------
    /**
     * Look up and return an authentication domain object by its
     * (partial) property name.  If you use a property called
     * <code>authenticator.<i>MyAuthenticator</i></code> to define
     * an authentication domain in a property file, then you can retrieve
     * this authenticator using the name "<i>MyAuthenticator</i>".
     *
     * @param name the property name of the authenticator
     * @return The matching AuthenticationDomain object
     */
    public static AuthenticationDomain authDomainBySubdirName(String name)
    {
        ensureAuthDomainsLoaded();

        if (authDomainsBySubdirName == null)
        {
            authDomainsBySubdirName =
                new HashMap<String, AuthenticationDomain>();

            for (AuthenticationDomain domain : authDomains)
            {
                authDomainsBySubdirName.put(domain.subdirName(), domain);
            }
        }

        return authDomainsBySubdirName.get(name);
    }


    // ----------------------------------------------------------
    /**
     * Get a list of shared authentication domain objects that have
     * already been loaded into the shared editing context.
     * @return an array of all AuthenticationDomain objects
     */
    public static NSArray<AuthenticationDomain> authDomains()
    {
        ensureAuthDomainsLoaded();
        return authDomains;
    }


    // ----------------------------------------------------------
    /**
     * Get a list of shared authentication domain objects that have
     * already been loaded into the shared editing context.
     * @return an array of all AuthenticationDomain objects
     */
    public static AuthenticationDomain defaultDomain()
    {
        if (defaultDomain == null)
        {
            ensureAuthDomainsLoaded();
            // Set up a default domain, if possible
            String defaultDomainName = Application.configurationProperties()
                .getProperty("authenticator.default");
            if (defaultDomainName != null)
            {
                try
                {
                    log.debug("looking up default domain");
                    defaultDomain = authDomainByName(defaultDomainName);
                }
                catch (EOObjectNotAvailableException e)
                {
                    log.error("Default authentication domain ("
                        + defaultDomainName + ") does not exist.");
                }
                catch (EOUtilities.MoreThanOneException e)
                {
                    log.error("Multiple entries for default authentication "
                        + "domain (" + defaultDomainName + ")");
                }
            }
            if (defaultDomain == null)
            {
                defaultDomain = authDomains().objectAtIndex(0);
            }
        }
        return defaultDomain;
    }


    // ----------------------------------------------------------
    /**
     * Load the list of shared authentication domain objects, if it hasn't
     * been done already.
     */
    public static void ensureAuthDomainsLoaded()
    {
        if (authDomains == null)
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
        log.debug("refreshAuthDomains()");
        theAuthenticatorMap = new TreeMap<String, UserAuthenticator>();
        defaultDomain = null;

        final WCProperties properties = Application.configurationProperties();
        @SuppressWarnings("unchecked")
        final Enumeration<String> propertyNames =
            (Enumeration<String>)properties.propertyNames();
        final String prefix = "authenticator.";

        run(new ECAction() { public void action() {
            log.debug("searching property list");
            // Disconnect from the shared editing context, since we're actually
            // making changes to objects in the shared context here.
            ec.setSharedEditingContext(null);
            while (propertyNames.hasMoreElements())
            {
                AuthenticationDomain domain = null;
                String base = propertyNames.nextElement();
                // log.debug ( "checking property: " + base );
                if (   base.startsWith(prefix)
                    && !base.equals("authenticator.default")
                    && base.substring(prefix.length()).indexOf('.') == -1)
                {
                    log.debug ("trying to register: " + base);
                    UserAuthenticator ua = null;
                    String uaClassName   = properties.getProperty(base);
                    if (uaClassName == null || uaClassName.equals(""))
                    {
                        uaClassName = properties.getProperty(
                            "authenticator.default.class",
                            DatabaseAuthenticator.class.getName());
                    }
                    try
                    {
                        ua = (UserAuthenticator)
                            Class.forName(uaClassName).newInstance();
                        log.info(
                            "registering " + base + " with " + uaClassName);
                        ua.configure(base, properties);
                        theAuthenticatorMap.put(base, ua);
                        try
                        {
                            int oldDebugLevel =
                                NSLog.debug.allowedDebugLevel();
                            NSLog.debug.setAllowedDebugLevel(
                                NSLog.DebugLevelInformational);
                            domain = uniqueObjectMatchingQualifier(ec,
                                propertyName.eq(base));
                            NSLog.debug.setAllowedDebugLevel(oldDebugLevel);
                            if (domain != null)
                            {
                                log.debug("domain is already in database");
                                if (domain.propertyName() != base)
                                {
                                    domain.setPropertyName(base);
                                }
                                String thisDisplayableName =
                                    properties.getProperty(
                                        base + "." + DISPLAYABLE_NAME_KEY);
                                if (thisDisplayableName != null
                                    && domain.displayableName()
                                       != thisDisplayableName)
                                {
                                    domain.setDisplayableName(
                                        thisDisplayableName);
                                }
                                String emailDomain =
                                    properties.getProperty(
                                        base + "." + DEFAULT_EMAIL_DOMAIN_KEY);
                                if (emailDomain != null
                                    && domain.defaultEmailDomain() !=
                                        emailDomain)
                                {
                                    domain.setDefaultEmailDomain(emailDomain);
                                }
                            }
                            else
                            {
                                log.info("Adding to AuthenticationDomain "
                                    + "table: " + base);
                                domain = new AuthenticationDomain();
                                ec.insertObject(domain);
                                domain.setPropertyName(base);
                                domain.setDisplayableName(
                                    properties.getProperty(
                                        base + "." + DISPLAYABLE_NAME_KEY));
                                domain.setDefaultEmailDomain(
                                    properties.getProperty(base
                                        + "." + DEFAULT_EMAIL_DOMAIN_KEY));
                            }
                        }
                        catch (EOUtilities.MoreThanOneException e)
                        {
                            log.error("Error updating AuthenticationDomain "
                                + "table for " + base, e);
                        }
                        if (domain != null)
                        {
                            String emailDomain = properties.getProperty(
                                base + "." + DEFAULT_EMAIL_DOMAIN_KEY);
                            if (emailDomain != null)
                            {
                                domain.setDefaultEmailDomain(emailDomain);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        log.error("refreshAuthDomains(): cannot create "
                            + uaClassName + ":", e);
                    }
                }
            }
            ec.saveChanges();
        }});

        log.debug("refreshing shared authentication domain objects");
        authDomains = allObjectsOrderedByDisplayableName(
            EOSharedEditingContext.defaultSharedEditingContext());

        // TODO: can't do this yet, since the domain's authenticator class
        // and config settings are not stored in the database!
        // We'll need to change that, eventually.

        // confirm that  all domains are registered in the map
//        for (int i = 0; i < authDomains.count(); i++)
//        {
//            AuthenticationDomain domain = authDomains.objectAtIndex( i );
//            String name = domain.propertyName();
//            if (theAuthenticatorMap.get(name) == null)
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
    public static NSArray<String> availableTimeFormats()
    {
        if (timeFormats == null)
        {
            try
            {
                @SuppressWarnings("unchecked")
                NSArray<String> propValues = (NSArray<String>)Application
                    .configurationProperties().arrayForKey("timeFormats");
                timeFormats = propValues;
            }
            catch (Exception e)
            {
                log.error("Exception parsing \"timeFormats\" property "
                    + "setting as an NSArray:", e);
            }
            if (timeFormats == null)
            {
                timeFormats =
                    new NSArray<String>(new String[]{"%I:%M%p", "%H:%M"});
            }
        }
        return timeFormats;
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
        return availableTimeFormats().objectAtIndex(0);
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
    public static NSArray<String> availableDateFormats()
    {
        if (dateFormats == null)
        {
            try
            {
                @SuppressWarnings("unchecked")
                NSArray<String> propValues = (NSArray<String>)Application
                .configurationProperties().arrayForKey( "dateFormats" );
                dateFormats = propValues;
            }
            catch (Exception e)
            {
                log.error("Exception parsing \"dateFormats\" property "
                    + "setting as an NSArray:", e);
            }
            if (dateFormats == null)
            {
                dateFormats = new NSArray<String>(new String[]{
                    "%m/%d/%y", "%m/%d/%Y", "%d.%m.%Y", "%d.%m.%y",
                    "%y-%m-%d", "%Y-%m-%d", "%d-%b-%y", "%d-%b-%Y" });
            }
        }
        return dateFormats;
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
        return availableDateFormats().objectAtIndex(0);
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

        public TimeZoneDescriptor(String id)
        {
            this.id = id;
            printableName = id.replaceAll("_", " ").replaceAll("/", ": ");
        }

        public NSTimeZone timeZone()
        {
            return NSTimeZone.timeZoneWithName(id, true);
        }

        public boolean equals(Object obj)
        {
            boolean result = false;
            if (obj == null)
            {
                // result = false
            }
            else if (obj == this)
            {
                result = true;
            }
            else if (obj instanceof TimeZoneDescriptor)
            {
                TimeZoneDescriptor rhs = (TimeZoneDescriptor)obj;
                result = (id == null)
                    ? (rhs.id == null)
                    : (id.equals(rhs.id));
                result = result && ((printableName == null)
                    ? (rhs.printableName == null)
                    : (printableName.equals(rhs.printableName)));
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
    public static NSArray<TimeZoneDescriptor> availableTimeZones()
    {
        if (timeZones == null)
        {
            NSMutableArray<TimeZoneDescriptor> zones =
                new NSMutableArray<TimeZoneDescriptor>();
            for (Object name : NSTimeZone.knownTimeZoneNames())
            {
                zones.add(new TimeZoneDescriptor(name.toString()));
            }
            ERXArrayUtilities.sortArrayWithKey(zones, "printableName");
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
    public static TimeZoneDescriptor descriptorForZone(String id)
    {
        for (TimeZoneDescriptor tzd : availableTimeZones())
        {
            if (tzd.id.equals(id))
            {
                return tzd;
            }
        }
        return null;
    }


    // ----------------------------------------------------------
    public static String subdirNameOf(String name)
    {
        String result = null;
        if (name != null)
        {
            char[] chars = new char[name.length()];
            int  pos   = 0;
            for (int i = 0; i < name.length(); i++)
            {
                char c = name.charAt(i);
                if (Character.isLetterOrDigit(c) ||
                    c == '_'                     ||
                    c == '-')
                {
                    chars[pos] = c;
                    pos++;
                }
            }
            result = new String(chars, 0, pos);
        }
        return result;
    }


    //~ Instance/static variables .............................................

    private static NSArray<AuthenticationDomain> authDomains;
    private static AuthenticationDomain defaultDomain;
    private static Map<String, UserAuthenticator> theAuthenticatorMap;
    private static Map<String, AuthenticationDomain> authDomainsBySubdirName;
    private String cachedSubdirName = null;
    private String cachedName = null;
    private static NSArray<String> timeFormats;
    private static NSArray<String> dateFormats;
    private static NSArray<TimeZoneDescriptor> timeZones;

    static Logger log = Logger.getLogger(AuthenticationDomain.class);
}
