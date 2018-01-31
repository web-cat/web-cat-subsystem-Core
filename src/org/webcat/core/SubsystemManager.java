/*==========================================================================*\
 |  $Id: SubsystemManager.java,v 1.6 2012/03/07 03:03:41 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2010 Virginia Tech
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.sf.webcat.FeatureProvider;
import org.apache.log4j.Logger;
import com.webobjects.appserver.WOComponent;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

// -------------------------------------------------------------------------
/**
 * Manages the Subsystem's stored on disk. A subsystem is either a WebObjects
 * framework or a separate jar file that contains a framework.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.6 $, $Date: 2012/03/07 03:03:41 $
 */
public class SubsystemManager
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initialize the <code>SubsystemManager</code>.  The
     * <code>SubsystemManager</code> will look for two properties in
     * the application's property settings.  First, it will look for
     * the property "subsystem.jar.dir".  If found, it will load all
     * jar'ed subsystems from this directory.  Second, it will look
     * for "subsystem.unjarred.classes", which is a "|"-separated list
     * of subsystem classes to register--subsystems that are already on
     * the classpath instead of in subsystem jars.  If this property is
     * defined, the corresponding classes will be registered as
     * subsystems.  Either property may be undefined, in which case it
     * will be ignored.
     *
     * @param properties The application's property settings
     */
    public SubsystemManager(WCProperties properties)
    {
        if (log.isDebugEnabled())
        {
            log.debug("creating subsystem manager", new Exception("from here"));
        }
        if (properties != null)
        {
            ArrayList<String> subsystemNames = new ArrayList<String>();
            // Have to look in the system properties, because that is where
            // all subsystem info will go, not in the config file
            for (Enumeration<Object> e = System.getProperties().keys();
                  e.hasMoreElements();)
            {
                String key = (String)e.nextElement();
                if ( key.startsWith(SUBSYSTEM_KEY_PREFIX)
                   && key.indexOf('.', SUBSYSTEM_KEY_PREFIX.length()) == -1)
                {
                    String name =
                        key.substring(SUBSYSTEM_KEY_PREFIX.length());
                    subsystemNames.add(name);
                }
            }
            addSubsystemsInOrder(
                subsystemNames,
                null,
                new HashMap<String, String>(),
                properties);
        }
        initAllSubsystems();
        envp();
        pluginProperties();
        startAllSubsystems();

        // Start up a thread to run periodic maintenance tasks every day
        new Thread(new Runnable() {
            // ----------------------------------------------------------
            public void run()
            {
                performPeriodicMaintenance();
                try
                {
                    Thread.sleep(1000 * 60 * 60 * 24);
                }
                catch (InterruptedException e)
                {
                    log.info("periodic maintenance task interrupted", e);
                }
            }
        }).start();
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Get the requested subsystem.
     *
     * @param name  The name of the subsystem to get
     * @return      The corresponding JarSubsystem
     */
    public Subsystem subsystem(String name)
    {
        return subsystems.get(name);
    }


    // ----------------------------------------------------------
    /**
     * Get an iterator for all loaded subsystems by name.
     *
     * @return An iterator for the names of all loaded subsystems
     */
    public NSArray<Subsystem> subsystems()
    {
        return subsystemArray;
    }


    // ----------------------------------------------------------
    /**
     * Add a Subsystem to the manager, given a class name.
     *
     * @param name the symbolic name to use for this subsystem
     * @param className the class name to load
     */
    public void addSubsystemFromClassName(String name, String className)
    {
        log.debug("attempting to load subsystem " + name + " using class '"
            + className + "'");
        try
        {
            addSubsystem(name,
                (Subsystem)DelegatingUrlClassLoader.getClassLoader()
                    .loadClass(className).newInstance());
        }
        catch (Exception e)
        {
            log.error("Exception loading subsystem "
                + name + " from class " + className + ":", e);
        }
    }


    // ----------------------------------------------------------
    /**
     * Add a Subsystem to the manager.
     *
     * @param name  The subsystem's name
     * @param s     The subsystem object to add
     */
    public void addSubsystem(String name, Subsystem s)
    {
        if (name == null) name = s.name();
        if (!subsystems.containsKey(name))
        {
            log.info("Registering subsystem " + s.name() + " as " + name);
            s.setName(name);
            subsystems.put(name, s);
            subsystemArray.addObject(s);
            clearSubsystemPropertyCache();
        }
        else
        {
            log.error("Subsystem already registered: " + name);
        }
    }


    // ----------------------------------------------------------
    /**
     * Calls {@link Subsystem#initializeSessionData(Session)} for
     * all registered subsystems.
     * @param s the session to initialize
     */
    public void initializeSessionData(Session s)
    {
        NSArray<Subsystem> subs = subsystems();
        for (int i = 0; i < subs.count(); i++)
        {
            subs.objectAtIndex(i).initializeSessionData(s);
        }
    }


    // ----------------------------------------------------------
    /**
     * Collects the subsystem fragments for the specified fragment key from all
     * currently loaded subsystems.
     *
     * @param fragmentKey the unique identifier of the fragment
     * @return an array of component classes that should be plugged in for the
     *     specified fragment
     */
    public NSArray<Class<? extends WOComponent>> subsystemFragmentsForKey(
            String fragmentKey)
    {
        NSMutableArray<Class<? extends WOComponent>> fragments =
            new NSMutableArray<Class<? extends WOComponent>>();

        for (Subsystem sub : subsystems())
        {
            Class<? extends WOComponent> frag =
                sub.subsystemFragmentForKey(fragmentKey);

            if (frag != null)
            {
                fragments.addObject(frag);
            }
        }

        return fragments;
    }


    // ----------------------------------------------------------
    /**
     * Take a list of subsystem names (typically, requirements needed to
     * support some feature) and determine if they are present.
     * @param names a list of subsystem names to look for
     * @return true if all of the named subsystems are installed
     */
    public boolean subsystemsAreInstalled(NSArray<String> names)
    {
        for (String name : names)
        {
            if (subsystems.get(name) == null)
            {
                return false;
            }
        }
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Get the command line environment variables used for executing
     * external commands.
     * @return a dictionary of ENV bindings
     */
    public NSDictionary<String, String> environment()
    {
        if (envCache == null)
        {
            NSMutableDictionary<String, String> env = inheritedEnvironment();
            for (Subsystem sub : subsystems())
            {
                sub.addEnvironmentBindings(env);
            }
            envCache = env;
            if (log.isDebugEnabled())
            {
                log.debug("plug-in ENV = " + env);
            }
        }
        return envCache;
    }


    // ----------------------------------------------------------
    /**
     * Get the command line environment variables used for executing
     * external commands in a form suitable for passing to
     * {@link Runtime#exec(String,String[])}.
     * @return a string array of ENV bindings, each in the form:
     * <i>NAME=value</i>.
     */
    public String[] envp()
    {
        if (envpCache == null)
        {
            NSDictionary<String, String> env = environment();
            ArrayList<String> envpList = new ArrayList<String>();
            for (String key : env.keySet())
            {
                String val = env.objectForKey(key).toString();
                envpList.add(key + "=" + val);
            }
            String[] envp = envpList.toArray(new String[envpList.size()]);
            envpCache = envp;
            if (log.isDebugEnabled())
            {
                log.debug("envp = " + arrayToString(envp));
            }
        }
        return envpCache;
    }


    // ----------------------------------------------------------
    /**
     * Get the plug-in property definitions to be passed to plug-ins.
     * @return a dictionary of plug-in properties
     */
    public NSDictionary<String, String> pluginProperties()
    {
        if (pluginPropertiesCache == null)
        {
            NSMutableDictionary<String, String> properties =
                new NSMutableDictionary<String, String>();
            for (Subsystem sub : subsystems())
            {
                sub.addPluginPropertyBindings(properties);
            }
            pluginPropertiesCache = properties;
            if (log.isDebugEnabled())
            {
                log.debug("plug-in properties = " + properties);
            }
        }
        return pluginPropertiesCache;
    }


    // ----------------------------------------------------------
    /**
     * Clear the cache of any subsystem-provided environment definitions
     * or plug-in property definitions.
     */
    public void clearSubsystemPropertyCache()
    {
        envCache = null;
        envpCache = null;
        pluginPropertiesCache = null;
    }


    // ----------------------------------------------------------
    /**
     * Refreshes cached information about subsystems and their providers.
     */
    public void refreshSubsystemDescriptorsAndProviders()
    {
        for (Iterator<?> i = FeatureProvider.providers().iterator();
            i.hasNext();)
        {
            ((FeatureProvider)i.next()).refresh();
        }
        if (subsystemArray != null)
        {
            for (Subsystem sub : subsystemArray)
            {
                sub.refreshDescriptor();
            }
        }
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    /**
     * Calls {@link Subsystem#init()} for all registered subsystems.
     */
    private void initAllSubsystems()
    {
        for (Subsystem sub : subsystems())
        {
            log.debug("initializing subsystem " + sub.name());
            sub.init();
            sub.subsystemInitCompleted();
            log.debug("subsystem " + sub.name() + " initialized");
        }
    }


    // ----------------------------------------------------------
    /**
     * Calls {@link Subsystem#start()} for all registered subsystems.
     */
    private void startAllSubsystems()
    {
        for (Subsystem sub : subsystems())
        {
            log.debug("starting subsystem " + sub.name());
            sub.start();
            sub.subsystemHasStarted();
            log.debug("subsystem " + sub.name() + " started");
        }
    }


    // ----------------------------------------------------------
    /**
     * Calls {@link Subsystem#performPeriodicMaintenance()} for all
     * registered subsystems.
     */
    private void performPeriodicMaintenance()
    {
        for (Subsystem sub : subsystems())
        {
            log.debug("periodic maintenance on subsystem " + sub.name());
            try
            {
                sub.performPeriodicMaintenance();
            }
            catch (Exception e)
            {
                log.error("Exception performing periodic maintenance on "
                    + sub.name(), e);
            }
            catch (Error e)
            {
                log.error("Error performing periodic maintenance on "
                    + sub.name(), e);
            }
            log.debug("subsystem " + sub.name()
                + " periodic maintenance finished");
        }
    }


    // ----------------------------------------------------------
    /**
     * Convert a (possibly null) comma-separated string into an array of
     * strings.
     * @param rawList the comma-separated string to split (can be null)
     * @return an array of the items in rawList, after separating on commas;
     * any whitespace surrounding commas are ignored; if rawList is null or
     * empty, then null is returned.
     */
    private String[] featureList(String rawList)
    {
        String[] result = null;
        if (rawList != null)
        {
            rawList = rawList.trim();
            if (!rawList.equals(""))
            {
                result = rawList.split("\\s*,\\s*");
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Check to see if any of the strings in the featureList are stored
     * in theSet.  If theSet is null, returns true.  If featureList is
     * null, returns false.
     * @param featureList an array of strings to test
     * @param theSet a set to check in
     * @return true if any string in featureList is found in theSet, or if
     * theSet is null
     */
    private boolean foundIn(String[] featureList, Map<String, String> theSet)
    {
        if (featureList == null) return false;
        if (theSet == null) return true;
        for (String feature : featureList)
        {
            if (theSet.containsKey(feature))
            {
                return true;
            }
        }
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Check to see if all of the strings in the featureList are stored
     * in theSet.  If featureList is null, returns false.  Otherwise, if
     * theSet is null, returns true.
     * @param featureList an array of strings to test
     * @param theSet a set to check in
     * @return false if any string in featureList is missing in theSet
     */
    private boolean missingFrom(
        String[] featureList, Map<String, String> theSet)
    {
        if (featureList == null) return false;
        if (theSet == null) return true;
        for (String key : featureList)
        {
            if (!theSet.containsKey(key))
            {
                return true;
            }
        }
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Add all of the strings in featureList to theSet.
     * @param featureList an array of strings to add
     * @param theSet the map to add them to
     */
    private void addTo(String[] featureList, Map<String, String> theSet)
    {
        if (featureList == null) return;
        for (String feature : featureList)
        {
            theSet.put(feature, feature);
        }
    }


    // ----------------------------------------------------------
    /**
     * Generate a string from an array of strings.
     * @param array the array to convert
     */
    private String arrayToString(String[] array)
    {
        if (array == null) return null;
        StringBuffer buffer = new StringBuffer();
        buffer.append("[ ");
        for (int i = 0; i < array.length; i++)
        {
            if (i > 0)
            {
                buffer.append(", ");
            }
            buffer.append(array[i]);
        }
        buffer.append(" ]");
        return buffer.toString();
    }


    // ----------------------------------------------------------
    /**
     * Install/load a list of subsystems, searching for dependencies and
     * using them to install subsystems in the correct order.  Since
     * dependencies are fairly rare, a dumb O(n^2) algorithm is used for
     * the topological sort.
     * @param names a list of subsystem names that need to be
     *     loaded/installed
     * @param pendingFeatures a map that contains the names of defined
     *     features that have not yet been loaded, for dependency tracking
     * @param addedFeatures a map that contains the names of defined features
     *     that have been installed (or at least partially installed)
     * @param properties The application's property settings
     */
    private void addSubsystemsInOrder(
        ArrayList<String>   names,
        Map<String, String> pendingFeatures,
        Map<String, String> addedFeatures,
        WCProperties        properties)
    {
        if (names.size() == 0) return;
        int oldSize = names.size();
        Map<String, String> incompleteFeatures = new HashMap<String, String>();
        log.debug("starting subsystem list traversal: " + names);
        for (int i = 0; i < names.size(); i++)
        {
            String name = names.get(i);
            String[] depends = featureList(
                properties.getProperty(name + DEPENDS_SUFFIX));
            String[] requires = featureList(
                properties.getProperty(name + REQUIRES_SUFFIX));
            if (foundIn(depends, pendingFeatures)
                 || foundIn(requires, pendingFeatures))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("skipping " + name + ": depends = "
                        + arrayToString(depends) + "; requires = "
                        + arrayToString(requires));
                }
                incompleteFeatures.put(name, name);
                addTo(featureList(
                    properties.getProperty(name + PROVIDES_SUFFIX)),
                    incompleteFeatures);
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("loading " + name + ": depends = "
                        + arrayToString(depends) + "; requires = "
                        + arrayToString(requires));
                }
                names.remove(i);
                i--;
                if (missingFrom(requires, addedFeatures))
                {
                    log.error("unable to load subsystem '" + name
                        + "': one or more required subsystems are missing: "
                        + arrayToString(requires));
                }
                else
                {
                    addedFeatures.put(name, name);
                    addTo(featureList(
                        properties.getProperty(name + PROVIDES_SUFFIX)),
                        addedFeatures);
                    String className =
                        properties.getProperty(SUBSYSTEM_KEY_PREFIX + name);

                    // Use a default class if no class name is specified
                    // in the property
                    if (className == null || className.equals(""))
                    {
                        className = Subsystem.class.getName();
                    }

                    addSubsystemFromClassName(name, className);
                }
            }
        }
        if (oldSize == names.size())
        {
            log.error(
                "cyclic or missing dependencies among subsystems detected: "
                + names);
        }
        else
        {
            addSubsystemsInOrder(
                names, incompleteFeatures, addedFeatures, properties);
        }
    }


    // ----------------------------------------------------------
    /**
     * Load this application's current ENV bindings into a dictionary.
     * @return the current ENV bindings
     */
    private NSMutableDictionary<String, String> inheritedEnvironment()
    {
        if (inheritedEnvCache == null)
        {
            NSMutableDictionary<String, String> env =
                new NSMutableDictionary<String, String>();
            // Fill it up

            // First, try Unix command
            try
            {
                Process process = Runtime.getRuntime().exec(
                    Application.isRunningOnWindows()
                        ? "cmd /c set" : "printenv");
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
                String line = in.readLine();
                while (line != null)
                {
                    int pos = line.indexOf('=');
                    if (pos > 0)
                    {
                        String key = line.substring(0, pos);
                        String val = line.substring(pos + 1);
                        env.takeValueForKey(val, key);
                    }
                    line = in.readLine();
                }
            }
            catch (java.io.IOException e)
            {
                log.error(
                    "Error attempting to parse default ENV settings:", e);
            }

            inheritedEnvCache = env;
            if (log.isDebugEnabled())
            {
                log.debug("inherited ENV = " + env);
            }
        }
        return inheritedEnvCache.mutableClone();
    }


    //~ Instance/static variables .............................................

    /** Map&lt;String, JarSubsystem&gt;: holds the loaded subsystems. */
    private Map<String, Subsystem> subsystems =
        new HashMap<String, Subsystem>();
    private NSMutableArray<Subsystem> subsystemArray =
        new NSMutableArray<Subsystem>();
    private NSDictionary<String, String> inheritedEnvCache = null;
    private NSDictionary<String, String> envCache = null;
    private NSDictionary<String, String> pluginPropertiesCache = null;
    private String[] envpCache = null;

    private static final String SUBSYSTEM_KEY_PREFIX = "subsystem.";
    private static final String DEPENDS_SUFFIX       = ".depends";
    private static final String REQUIRES_SUFFIX      = ".requires";
    private static final String PROVIDES_SUFFIX      = ".provides";

    static Logger log = Logger.getLogger(SubsystemManager.class);
}
