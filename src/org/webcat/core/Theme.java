/*==========================================================================*\
 |  Copyright (C) 2008-2018 Virginia Tech
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.webcat.core.messaging.UnexpectedExceptionMessage;
import org.webcat.woextensions.ECAction;
import static org.webcat.woextensions.ECAction.run;
import org.webcat.woextensions.WCResourceManager;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOCookie;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.foundation.ERXValueUtilities;

// -------------------------------------------------------------------------
/**
 * Represents a theme (stored in the Core framework).
 *
 *  @author  Stephen Edwards
 */
public class Theme
    extends _Theme
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Theme object.
     */
    public Theme()
    {
        super();
    }


    // ----------------------------------------------------------
    /**
     * Look up and return a theme object by its directory name (short
     * symbolic name, not its human-readable name).
     *
     * @param themeDirName the subdirectory name of the theme
     * @return The matching theme object
     */
    public static Theme themeFromName(
        EOEditingContext ec, final String themeDirName)
    {
        Theme result = null;
        try
        {
            synchronized (themeIds)
            {
                EOGlobalID id = themeIds.get(themeDirName);
                if (id != null)
                {
                    return Theme.forId(ec, id);
                }
            }

                // If it's not in our ID stash yet
                result = themeForDirName(ec, themeDirName);
                if (result == null)
                {
                    if (defaultThemeName().equals(themeDirName))
                    {
                        // horror! Cannot load default theme ?!?!?!?
                        // could be because ec is dead
                        log.error("themeFromName(\"" + themeDirName + "\"): "
                            + "themeForDirName returned null for default "
                            + "theme name!?!?!?");
                        return null;
                    }
                    // otherwise, fall through and let return statement call
                    // defaultTheme() instead
                }
                else
                {
                    result.parent();
                    EOGlobalID id = result.globalId();
                    synchronized (themeIds)
                    {
                        themeIds.put(themeDirName, result.globalId());
                    }
                }
        }
        catch (Exception e)
        {
            log.error("Unrecognized theme name: \"" + themeDirName + "\"");
            result = null;
        }
        return result == null ? defaultTheme(ec) : result;
    }


    // ----------------------------------------------------------
//    public static Theme globalReadOnlyThemeFromName(final String themeDirName)
//    {
//        synchronized (globalInstances)
//        {
//            Theme result = globalInstances.get(themeDirName);
//            if (result == null)
//            {
//                result = new ECActionWithResult<Theme>(globalContext) {
//                    // ------------------------------------------------------
//                    @Override
//                    public Theme action()
//                    {
//                        return themeFromName(ec, themeDirName);
//                    }
//                }.call();
//                if (result != null)
//                {
//                    globalInstances.put(themeDirName, result);
//                }
//                else if (!defaultThemeName().equals(themeDirName))
//                {
//                    result = globalReadOnlyThemeFromName(defaultThemeName());
//                }
//            }
//            return result;
//        }
//    }


    // ----------------------------------------------------------
    /**
     * Returns the last used theme that was stored in the browser's cookies.
     *
     * @param context the context from which to retrieve the cookie
     * @return The last-used theme stored in the cookie
     */
//    public static Theme lastUsedThemeInContext(WOContext context)
//    {
//        String lastUsedTheme = context.request().cookieValueForKey(
//                COOKIE_LAST_USED_THEME);
//
//        if (lastUsedTheme == null)
//        {
//            lastUsedTheme = DEFAULT_THEME;
//        }
//        Theme result = globalReadOnlyThemeFromName(lastUsedTheme);
//        if (result == null)
//        {
//            log.error("lastUsedThemeInContext(): Unable to retrieve theme '"
//                + lastUsedTheme + "'");
//        }
//        return result;
//    }


    // ----------------------------------------------------------
    /**
     * Stores this theme as the last used theme in the browser's cookies. This
     * is used on pages such as the login page when a user session does not yet
     * exist, but we would still like to present them with the theme they used
     * last (on the same browser/client).
     *
     * @param context the context in which to store the cookie
     */
    public void setAsLastUsedThemeInContext(WOContext context)
    {
        String path = context.urlWithRequestHandlerKey(null, null, null);
        WOCookie cookie = new WOCookie(COOKIE_LAST_USED_THEME, dirName(), path,
                null, ONE_YEAR, false);
        context.response().addCookie(cookie);
    }


    // ----------------------------------------------------------
    /**
     * Return the default theme object to use when users have not
     * chosen one of their own.
     *
     * @return The default theme
     */
    public static String defaultThemeName()
    {
        return DEFAULT_THEME;
    }


    // ----------------------------------------------------------
    /**
     * Return the default theme object to use when users have not
     * chosen one of their own.
     *
     * @return The default theme
     */
    public static Theme defaultTheme(EOEditingContext ec)
    {
        return themeFromName(ec, DEFAULT_THEME);
    }


    // ----------------------------------------------------------
    /**
     * Get a list of shared theme objects that have already been loaded
     * into the shared editing context.
     * @return an array of all theme objects
     */
    public static NSArray<Theme> themes(EOEditingContext ec)
    {
        return allObjectsOrderedByName(ec);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Get a human-readable representation of this theme, which is
     * the same as {@link #name()}.
     * @return this theme's name
     */
    public String userPresentableDescription()
    {
        return name();
    }


    // ----------------------------------------------------------
    public Theme parent()
    {
        if (baseIsNotSet)
        {
            String baseName = properties() != null
                ? (String)properties().valueForKey("extends")
                : null;
            if (baseName != null)
            {
                base = themeFromName(editingContext(), baseName);
                baseIsNotSet = false;
            }
        }
        return base;
    }


    // ----------------------------------------------------------
    /**
     * Provided for OGNL compatibility of the pseudo-key .inherit.
     * WO will correctly use valueForKey()/valueForKeyPath(), but
     * OGNL won't, so we need this stub for evaluating OGNL expressions
     * using the .inherit key.
     */
    public Object inherit()
    {
        return valueForKey(INHERIT_KEY);
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        if (INHERIT_KEY.equals(key))
        {
            if (inheriter == null)
            {
                inheriter = new PropertyInheriter();
            }
            return inheriter;
        }
        else if (key.startsWith(INHERIT_PREFIX))
        {
            key = key.substring(INHERIT_PREFIX_LEN);
            if (inheriter == null)
            {
                inheriter = new PropertyInheriter();
            }
            return inheriter.valueForKey(key);
        }
        else
        {
            return super.valueForKey(key);
        }
    }


    // ----------------------------------------------------------
    public void takeValueForKey(Object value, String key)
    {
        if (key.equals(INHERIT_KEY))
        {
            throw new IllegalArgumentException("cannot set the .inherit key");
        }
        else if (key.startsWith(INHERIT_PREFIX))
        {
            key = key.substring(INHERIT_PREFIX_LEN);
        }
        super.takeValueForKey(value, key);
    }


    // ----------------------------------------------------------
    public Object valueForKeyPath(String keyPath)
    {
        if (keyPath.startsWith(INHERIT_PREFIX))
        {
            keyPath = keyPath.substring(INHERIT_PREFIX_LEN);
            if (inheriter == null)
            {
                inheriter = new PropertyInheriter();
            }
            return inheriter.valueForKeyPath(keyPath);
        }
        else
        {
            return super.valueForKeyPath(keyPath);
        }
    }


    // ----------------------------------------------------------
    public void takeValueForKeyPath(Object value, String keyPath)
    {
        if (keyPath.startsWith(INHERIT_PREFIX))
        {
            keyPath = keyPath.substring(INHERIT_PREFIX_LEN);
        }
        super.takeValueForKey(value, keyPath);
    }


    // ----------------------------------------------------------
    public boolean isDark()
    {
        Object result =
            valueForKeyPath(INHERIT_PREFIX + "properties.isDark");
        return (result == null)
            ? false
            : Boolean.valueOf(result.toString());
    }


    // ----------------------------------------------------------
    public String dojoTheme()
    {
        Object result =
            valueForKeyPath(INHERIT_PREFIX + "properties.dojoTheme");
        return (result == null)
            ? "nihilo"
            : result.toString();
    }


    // ----------------------------------------------------------
    private static String computeLinkTags(String aName, MutableDictionary props)
    {
        String result = "";
        if (props != null)
        {
            try
            {
                Object cssFileList = props.valueForKey("cssOrder");
                if (cssFileList != null && cssFileList instanceof NSArray)
                {
                    @SuppressWarnings("unchecked")
                    NSArray<NSDictionary<String, String>> cssFiles =
                        (NSArray<NSDictionary<String, String>>)cssFileList;
                    String baseLocation =
                        "Core.framework/WebServerResources/theme/"
                        + aName + "/";
                    for (NSDictionary<String, String> css : cssFiles)
                    {
                        result += "<link rel=\"stylesheet\" "
                            + "type=\"text/css\" href=\""
                            + WCResourceManager.resourceURLFor(
                                baseLocation
                                + css.get("file"),
                                null)
                            + "\"";
                        String media = css.get("media");
                        if (media != null)
                        {
                            result += " media=\"" + media + "\"";
                        }
                        result += " />";
                    }
                }
            }
            catch (Exception e)
            {
                new UnexpectedExceptionMessage(e, null, null,
                    "Unexpected exception trying to decode theme "
                    + "properties for theme: " + aName
                    + ".").send();
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    public String linkTags()
    {
        if (linkTags == null)
        {
            linkTags = (parent() == null)
                ? ""
                : parent().linkTags();
            linkTags += computeLinkTags(dirName(), properties());
        }
        return linkTags;
    }


    // ----------------------------------------------------------
    public void refresh()
    {
        File plist = new File(themeBaseDir(), dirName());
        if (plist.exists())
        {
            refreshFrom(plist);
        }
        else
        {
            log.error("Unable to refresh theme " + this + ": file "
                + "not found: " + plist);
        }
    }


    // ----------------------------------------------------------
    public static void refreshThemes()
    {
        log.debug("refreshThemes()");
        if (!themeBaseDir().exists()) return;

//        if (globalContext == null)
//        {
//            globalContext = WCEC.newAutoLockingEditingContext();
//        }
//        synchronized(globalInstances)
        synchronized(themeIds)
        {
//            globalInstances.clear();
            themeIds.clear();
        }
//        run(new ECAction(globalContext) { public void action()
        run(new ECAction() { public void action()
        {
            for (File subdir : themeBaseDir().listFiles())
            {
                if (subdir.isDirectory())
                {
                    File plist = new File(subdir, "theme.plist");
                    if (plist.exists())
                    {
                        Theme themeToUpdate =
                            themeForDirName(ec, subdir.getName());
                        if (themeToUpdate != null)
                        {
                            // Theme already exists, so check to see if
                            // it needs to be updated
                            NSTimestamp modTime = new NSTimestamp(
                                plist.lastModified());
                            if (themeToUpdate.lastUpdate() != null
                                && themeToUpdate.lastUpdate().after(modTime))
                            {
                                // No update needed
                                log.debug("theme " + themeToUpdate.dirName()
                                    + " is up to date");
                                themeToUpdate = null;
                            }
                        }
                        else
                        {
                            // Create it
                            log.info("Registering new theme: "
                                + subdir.getName());
                            themeToUpdate =
                                create(ec, subdir.getName(), false, false);
                        }
                        if (themeToUpdate != null)
                        {
                            themeToUpdate.refreshFrom(plist);
                            ec.saveChangesTolerantly();
                            // don't need to cache in themeIds, since that
                            // was already done in themeFromName() at the
                            // top of this loop
                        }
                    }
                }
            }
        }});
    }


    // ----------------------------------------------------------
    public void setUpdateMutableFields(boolean value)
    {
        // Silently swallow this operation, since Themes are held in
        // the shared editing context and should not be modified, except
        // under very controlled conditions.
    }


    // ----------------------------------------------------------
    public static synchronized NSKeyValueCodingAdditions installTheme()
    {
        if (installTheme == null)
        {
            // Can't use the subsystem manager, since it isn't initialized
            NSBundle core = NSBundle.bundleForName("Core");
            if (core != null)
            {
                @SuppressWarnings("deprecation")
                String themeDir =
                    core.bundlePath() + "/WebServerResources/theme/";
                try
                {
                    MutableDictionary props =
                        MutableDictionary.fromPropertyList(new File(
                            themeDir, DEFAULT_THEME + "/theme.plist"));
                    String parentName = (String)props.get("extends");
                    props.remove("extends");
                    String linkTagContent =
                        computeLinkTags(DEFAULT_THEME, props);
                    MutableArray cssOrder =
                        (MutableArray)props.get("cssOrder");
                    while (parentName != null)
                    {
                        MutableDictionary parentProps = MutableDictionary
                            .fromPropertyList(new File(
                                themeDir, parentName + "/theme.plist"));
                        linkTagContent =
                            computeLinkTags(parentName, parentProps)
                            + linkTagContent;
                        @SuppressWarnings("unchecked")
                        NSArray<NSDictionary<String, String>> cssFiles =
                            (NSArray<NSDictionary<String, String>>)parentProps
                            .get("cssOrder");
                        for (NSDictionary<String, String> css : cssFiles)
                        {
                            css.put("file", "../" + parentName + "/"
                                + css.get("file"));
                            cssOrder.add(css);
                        }
                        if (parentProps.containsKey("dojoTheme") &&
                            ! props.containsKey("dojoTheme"))
                        {
                            props.put(
                                "dojoTheme", parentProps.get("dojoTheme"));
                        }
                        parentName = (String)parentProps.get("extends");
                    }

                    installTheme = props;
                    installTheme.takeValueForKey(linkTagContent, "linkTags");
                    if (!installTheme.containsKey("name"))
                    {
                        installTheme.takeValueForKey(DEFAULT_THEME, "name");
                    }
                    installTheme.takeValueForKey(DEFAULT_THEME, "dirName");
                    // installTheme.takeValueForKey(installTheme, "inherit");
                    installTheme.takeValueForKey(
                        installTheme.mutableClone(), "properties");
                    installTheme.takeValueForKey(
                        installTheme.mutableClone(), "inherit");
                }
                catch (IOException e)
                {
                    log.error("error creating temporary theme", e);
                }
            }
        }
        return installTheme;
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private void refreshFrom(File plist)
    {
        NSTimestamp now = new NSTimestamp();
        try
        {
            log.info("reloading theme settings from: "
                + plist.getCanonicalPath());
            MutableDictionary dict =
                MutableDictionary.fromPropertyList(plist);
            setProperties(dict);
            String themeName = (String)dict.objectForKey("name");
            setName(themeName);
            setLastUpdate(now);
            setIsForThemeDevelopers(ERXValueUtilities.booleanValue(
                dict.valueForKey("isForThemeDevelopers")));
        }
        catch (Exception e)
        {
            log.error("Unable to refresh theme from " + plist, e);
            new UnexpectedExceptionMessage(e, null, null,
                    "Error refreshing theme.").send();
        }
    }


    // ----------------------------------------------------------
    @SuppressWarnings("deprecation")
    private static File themeBaseDir()
    {
        if (themeBaseDir == null)
        {
            // We *cannot* use the subsystem itself to find this
            // information, since this method is called before the
            // subsystem manager has initialized the subsystems!
            themeBaseDir = new File(
                NSBundle.bundleForName("Core").bundlePath(),
                "WebServerResources/theme");
        }
        return themeBaseDir;
    }


    // ----------------------------------------------------------
    private class PropertyInheriter
        implements NSKeyValueCodingAdditions
    {
        // ----------------------------------------------------------
        public void takeValueForKeyPath(Object value, String keyPath)
        {
            Theme.this.takeValueForKeyPath(value, keyPath);
        }


        // ----------------------------------------------------------
        public Object valueForKeyPath(String keyPath)
        {
            Object result = Theme.this.valueForKeyPath(keyPath);
            if (result == null && parent() != null)
            {
                result = parent().valueForKeyPath(INHERIT_PREFIX + keyPath);
            }
            return result;
        }


        // ----------------------------------------------------------
        public void takeValueForKey(Object value, String key)
        {
            Theme.this.takeStoredValueForKey(value, key);
        }


        // ----------------------------------------------------------
        public Object valueForKey(String key)
        {
            Object result = Theme.this.valueForKey(key);
            if (result == null && parent() != null)
            {
                result = parent().valueForKey(INHERIT_PREFIX + key);
            }
            return result;
        }

    }


    //~ Instance/static variables .............................................

    private static NSDictionary<String, Object> installTheme;
//    private static EOEditingContext globalContext;
//    private static Map<String, Theme> globalInstances =
//        new HashMap<String, Theme>();
    private static Map<String, EOGlobalID> themeIds =
        new HashMap<String, EOGlobalID>();
    private String linkTags;
    private Theme base;
    private boolean baseIsNotSet = true;
    private PropertyInheriter inheriter;

    private static File themeBaseDir;
    private static final String INHERIT_KEY    = "inherit";
    private static final String INHERIT_PREFIX = INHERIT_KEY + ".";
    private static final int INHERIT_PREFIX_LEN = INHERIT_PREFIX.length();
    // One year, in seconds
    private static final int ONE_YEAR = 60 * 60 * 24 * 365;

    private static final String COOKIE_LAST_USED_THEME =
        "org.webcat.core.Theme.lastUsed";

    public static final String DEFAULT_THEME = "gentelella";
}
