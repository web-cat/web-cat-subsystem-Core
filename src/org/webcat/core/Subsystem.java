/*==========================================================================*\
 |  $Id: Subsystem.java,v 1.6 2012/03/07 03:03:41 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.log4j.Logger;
import net.sf.webcat.FeatureDescriptor;
import net.sf.webcat.WCServletAdaptor;
import org.webcat.dbupdate.UpdateEngine;
import org.webcat.dbupdate.UpdateSet;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPropertyListSerialization;

// -------------------------------------------------------------------------
/**
 *  The subsystem interface that defines the API used by the Core to
 *  communicate with subsystems.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.6 $, $Date: 2012/03/07 03:03:41 $
 */
public class Subsystem
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Subsystem object.  The constructor is called when
     * creating the subsystem, but subclasses should <b>NOT</b> include
     * startup actions in their constructors--only basic data initialization.
     * Instead, all startup actions should be placed in the subclass
     * {@link #init()} method, which will be called to "start up" each
     * subsystem <em>after</em> all subsystems have been created in the
     * proper order.
     */
    public Subsystem()
    {
        // Nothing to initialize here
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Get the short (one-word) human-readable name for this
     * subsystem.
     *
     * @return The short name
     */
    public String name()
    {
        return name;
    }


    // ----------------------------------------------------------
    /**
     * Set the short (one-word) human-readable name for this subsystem.
     * @param newName the name to use
     */
    public void setName(String newName)
    {
        name = newName;
    }


    // ----------------------------------------------------------
    /**
     * Get the FeatureDescriptor for this subsystem.
     * @return this subsystem's descriptor
     */
    public FeatureDescriptor descriptor()
    {
        if (descriptor == null)
        {
            // First, look to see if there is an appropriate subsystem updater
            WCServletAdaptor adaptor = WCServletAdaptor.getInstance();
            if (adaptor != null)
            {
                for (FeatureDescriptor sd : adaptor.subsystems())
                {
                    if (name.equals(sd.name()))
                    {
                        // found it!
                        descriptor = sd;
                        break;
                    }
                }
            }
            // Otherwise, try to create one directly from properties
            if (descriptor == null)
            {
                log.debug("Unable to find feature descriptor for " + name()
                    + " via adaptor.  Creating one from properties.");
                descriptor = new FeatureDescriptor(
                    name(), Application.configurationProperties(), false);
            }
        }
        return descriptor;
    }


    // ----------------------------------------------------------
    /**
     * Clear the cached feature descriptor for this subsystem.
     */
    public void refreshDescriptor()
    {
        descriptor = null;
    }


    // ----------------------------------------------------------
    /**
     * Get a list of WO components that should be instantiated and presented
     * on the front page.
     *
     * @return The list of names, as strings
     */
    public NSArray<String> frontPageStatusComponents()
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Get a list of in-jar paths of the EOModels contained in
     * this subsystem's jar file.  If no EOModel(s) are contained
     * in this subsystem, this method returns null.
     *
     * @return The list of paths, as strings
     */
    public NSArray<String> EOModelPathsInJar()
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Carry out any subsystem-specific initialization actions.  This method
     * is called once all subsystems have been created, so any dependencies
     * on services provided by other subsystems are fulfilled.  Subsystems
     * are init'ed in the same order they are created.  The default
     * implementation calls {@link #updateDbIfNecessary()} to update
     * the subsystem's database tables, and then {@link #loadTabs()} to
     * load the subsystem's tab definitions.
     */
    public void init()
    {
        log.debug("init() for " + name());
        updateDbIfNecessary();
        subsystemTabTemplate = loadTabs();
        if (log.isDebugEnabled())
        {
            log.debug("tabs for " + name() + " = " + subsystemTabTemplate);
        }
    }


    // ----------------------------------------------------------
    /**
     * Determine if this subsystem has completed its initialization.
     * @return True if this subsystem has been initialized.
     */
    public boolean isInitialized()
    {
        return isInitialized;
    }


    // ----------------------------------------------------------
    /**
     * Carry out any subsystem-specific startup actions.  This method is
     * called once all subsystems have been initialized.  Subsystems
     * are started in the same order they are created.  Subclasses should
     * override this method to perform custom startup actions.
     */
    public void start()
    {
        // Subclasses should override this as necessary
    }


    // ----------------------------------------------------------
    /**
     * Determine if this subsystem has started.
     * @return True if this subsystem has been started.
     */
    public boolean hasStarted()
    {
        return hasStarted;
    }


    // ----------------------------------------------------------
    /**
     * Access the set of parameter definitions that prescribe the
     * configuration interface for this subsystem.  The default implementation
     * attempts to read the config.plist file from the subsystem's
     * resources directory.
     * @return the parameter definitions as an NSDictionary, or
     * null if none are found
     */
    @SuppressWarnings("deprecation")
    public NSDictionary<String, Object> parameterDescriptions()
    {
        if (options == null)
        {
            File configFile = new File(myResourcesDir() + "/config.plist");
            log.debug("Attempting to locate parameter descriptions in: "
                + configFile.getPath());
            if (!configFile.exists())
            {
                // If not found, try looking directly in the bundle, in case
                // the resources dir was overridden by properties (like on
                // the main development machine!).  This is purely to support
                // development-mode hacks, and probably won't ever be used
                // in production.  See the comments in myResourcesDir()
                // regarding the resourcePath() method being deprecated.
                NSBundle myBundle = myBundle();
                if (myBundle != null)
                {
                    configFile = new File(
                        myBundle.resourcePath() + "/config.plist");
                    log.debug(
                        "Attempting to locate parameter descriptions in: "
                        + configFile.getPath());
                }
            }
            if (configFile.exists())
            {
                try
                {
                    log.debug("loading parameter descriptions from: "
                        + configFile.getPath());
                    FileInputStream in = new FileInputStream(configFile);
                    NSData data = new NSData(in, (int)configFile.length());
                    @SuppressWarnings("unchecked")
                    NSDictionary<String, Object> newOptions =
                        (NSDictionary<String, Object>)
                        NSPropertyListSerialization
                            .propertyListFromData(data, "UTF-8");
                    options = newOptions;
                    in.close();
                }
                catch (java.io.IOException e)
                {
                    log.error(
                        "error reading from subsystem configuration file "
                        + configFile.getPath(),
                        e);
                }
            }
            if (log.isDebugEnabled())
            {
                log.debug("loaded parameter descriptions for subsystem "
                    + name() + ":\n" + options);
            }
        }
        return options;
    }


    // ----------------------------------------------------------
    /**
     * Initialize the subsystem-specific session data in a newly created
     * session object.  This method is called once by the core for
     * each newly created session object.
     *
     * @param s The new session object
     */
    public void initializeSessionData(Session s)
    {
        s.tabs.mergeClonedChildren(subsystemTabTemplate);
    }


    // ----------------------------------------------------------
    /**
     * Gets the component class that this subsystem wants to plug-in to another
     * page on the system. This mapping is defined in the
     * SubsystemFragments.plist file located in the Resources of the
     * subsystem.
     *
     * @param fragmentKey the unique fragment identifier
     * @return a component class that is plugged into the page
     */
    public final Class<? extends WOComponent> subsystemFragmentForKey(
            String fragmentKey)
    {
        if (!subsystemFragmentsLoaded)
        {
            subsystemFragmentsLoaded = true;

            File file = new File(myResourcesDir(),
                    SUBSYSTEM_FRAGMENTS_PLIST_FILENAME);

            if (!file.exists())
            {
                return null;
            }

            try
            {
                subsystemFragments = new NSMutableDictionary
                    <String, Class<? extends WOComponent>>();

                @SuppressWarnings("unchecked")
                NSDictionary<String, Object> plist =
                    (NSDictionary<String, Object>)
                    NSPropertyListSerialization.propertyListFromData(
                        new NSData(new FileInputStream(file), 0), "UTF-8");

                for (String key : plist.allKeys())
                {
                    String className = (String) plist.objectForKey(key);

                    try
                    {
                        Class<?> klass = Class.forName(className);
                        Class<? extends WOComponent> compKlass =
                            klass.asSubclass(WOComponent.class);

                        subsystemFragments.setObjectForKey(compKlass, key);
                    }
                    catch (ClassNotFoundException e)
                    {
                        log.warn("The class " + className + " for the "
                                + "subsystem fragment '" + key + "' "
                                + "could not be found.");
                    }
                    catch (ClassCastException e)
                    {
                        log.warn("The class " + className + " for the "
                                + "subsystem fragment '" + key + "' "
                                + "is not a subclass of WOComponent.");
                    }
                }
            }
            catch (IOException e)
            {
                subsystemFragments = null;
            }
        }

        if (subsystemFragments != null)
        {
            return subsystemFragments.objectForKey(fragmentKey);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Generate the component definitions and bindings for a given
     * pre-defined information fragment, so that the result can be
     * plugged into other pages defined elsewhere in the system.
     * @param fragmentKey the identifier for the fragment to generate
     *        (see the keys defined in {@link SubsystemFragmentCollector}
     * @param htmlBuffer add the html template for the subsystem's fragment
     *        to this buffer
     * @param wodBuffer add the binding definitions (the .wod file contents)
     *        for the subsystem's fragment to this buffer
     */
    public final void collectSubsystemFragments(
        String fragmentKey, StringBuffer htmlBuffer, StringBuffer wodBuffer)
    {
        // Subclasses should override this as necessary
    }


    // ----------------------------------------------------------
    /**
     * Add any subsystem-specific command-line environment variable bindings
     * to the given dictionary.  The default implementation does nothing,
     * but subclasses can extend this behavior as needed.
     * @param env the dictionary to add environment variable bindings to;
     * the full set of currently available bindings are passed in.
     */
    public void addEnvironmentBindings(NSMutableDictionary<String, String> env)
    {
        // Subclasses should override this as necessary
    }


    // ----------------------------------------------------------
    /**
     * Add any subsystem-specific plug-in property bindings
     * to the given dictionary.  The default implementation does nothing,
     * but subclasses can extend this behavior as needed.
     * @param properties the dictionary to add new properties to;
     * individual plug-in information may override these later.
     */
    public void addPluginPropertyBindings(
        NSMutableDictionary<String, String> properties)
    {
        // Subclasses should override this as necessary
    }


    // ----------------------------------------------------------
    /**
     * Handle a direct action request.  The user's login session will be
     * passed in as well.
     *
     * @param request the request to respond to
     * @param session the user's session
     * @param context the context for this request
     * @return The response page or contents
     */
    public WOActionResults handleDirectAction(
            WORequest request,
            Session   session,
            WOContext context)
    {
        throw new RuntimeException(
            "invalid subsystem direct action request: "
            + "\n---request---\n" + request
            + "\n\n---session---\n" + session
            + "\n\n---context---\n" + context);
    }


    // ----------------------------------------------------------
    /**
     * Get the string path name for this subsystem's Resources directory.
     * This is designed for use by subclasses that want to locate internal
     * resources for use in setting up environment variable or plug-in
     * property values.
     *
     * @return The Resources directory name as a string
     */
    @SuppressWarnings("deprecation")
    public String myResourcesDir()
    {
        if (myResourcesDir == null)
        {
            // First, look for an overriding property, like those that
            // might be used for non-servlet deployment scenarios.
            myResourcesDir = Application.configurationProperties()
                .getProperty(name() + ".Resources");
        }
        if (myResourcesDir == null)
        {
            NSBundle myBundle = myBundle();
            if (myBundle != null)
            {
                // Note that the resourcePath() method is deprecated, but it
                // is the best way to get what we need here, so we'll use it
                // anyway, rather than re-implementing it.
                myResourcesDir = myBundle.resourcePath();
            }
        }
        return myResourcesDir;
    }


    //~ Protected Methods .....................................................

    // ----------------------------------------------------------
    /**
     * Get the NSBundle for this subsystem.
     *
     * @return This subsystem's NSBundle
     */
    protected NSBundle myBundle()
    {
        NSBundle result = NSBundle.bundleForName(name());
        if (result == null  && getClass() != Subsystem.class)
        {
            result = NSBundle.bundleForClass(getClass());
        }
        if (result == null && !"webcat".equals(name()))
        {
            log.error("cannot find bundle for subsystem " + name());
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Loads this subsystem's tab definitions.  The default implementation
     * pulls them from the subsystem's Tabs.plist resource file.
     * @return the loaded tab definitions, or null if the subsystem has none
     */
    protected NSArray<TabDescriptor> loadTabs()
    {
        NSBundle bundle = myBundle();
        if (bundle != null)
        {
            byte[] bytes = myBundle().bytesForResourcePath(
                TabDescriptor.TAB_DEFINITIONS);
            if (bytes != null && bytes.length > 0)
            {
                return TabDescriptor.tabsFromPropertyList(new NSData(bytes));
            }
        }
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Applies any necessary database updates to the table structures using
     * the class returned by {@link #databaseUpdaterClass()}.  Subclasses
     * typically do not need to override this, unless they want to hook
     * special behaviors before/after database updating.  To change the
     * class containing your subsystem's database updates, override
     * {@link #databaseUpdaterClass()} instead.
     */
    protected void updateDbIfNecessary()
    {
        Class<? extends UpdateSet> updaterClass = databaseUpdaterClass();
        if (updaterClass != null)
        {
            try
            {
                log.debug("Applying updates for subsystem " + name()
                    + " using " + updaterClass);
                // Apply any pending database updates for this subsystem
                UpdateEngine.instance().applyNecessaryUpdates(
                    updaterClass.newInstance());
            }
            catch (Exception e)
            {
                log.error(
                    "Unable to apply updates from database updater class "
                    + updaterClass, e);
            }
        }
        else
        {
            log.debug("no database updater class for subsystem " + name());
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns the database update set class for this subsystem.  The
     * default implementation takes the subsystem's class name and adds
     * "DatabaseUpdates" onto the end, then finds the corresponding class
     * by name.  Subclasses can override this if they use different
     * conventions.  The result of this method is typically used by
     * {@link #updateDbIfNecessary()} to apply database updates during
     * {@link #init()}.
     * @return The class for this subsystem's update set
     */
    protected Class<? extends UpdateSet> databaseUpdaterClass()
    {
        Class<? extends UpdateSet> result = null;

        // Ignore subsystems that do not define their own subclasses.
        // Note that this also ignores Core, which has its updates applied
        // first by Application.initializeApplication().
        if (this.getClass().equals(Subsystem.class))
        {
            return result;
        }

        // Otherwise, try to look up the database update set based on
        // the class name of the subsystem itself
        String className = this.getClass().getName() + "DatabaseUpdates";
        try
        {
            Class<?> updaterClass = DelegatingUrlClassLoader.getClassLoader()
                .loadClass(className);
            result = updaterClass.asSubclass(UpdateSet.class);
        }
        catch (ClassCastException e)
        {
            log.error("Cannot cast " + className + " to UpdateSet", e);
        }
        catch (ClassNotFoundException e)
        {
            log.debug("no class found: " + className);
        }
        catch (Exception e)
        {
            log.error("unable to load database updater class: " + className, e);
        }
        log.debug("database updater for " + this.getClass().getName()
            + " is " + result);
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Called periodically (say, daily) by the subsystem manager to give
     * subsystems a chance to perform periodic maintenance tasks.
     */
    protected void performPeriodicMaintenance()
    {
        // Nothing by default
    }


    // ----------------------------------------------------------
    /**
     * Add a file resource definition to a dictionary, overridden by an
     * optional user-specified value.  This method is a helper for subsystems
     * that wish to add subsystem-specific file resources to ENV variable
     * definitions or plug-in properties.
     * @param map the dictionary to add the binding to
     * @param key the key to define in the map
     * @param userSettingKey the name of a property to look up in the
     * application's configuration settings; if a value is found, this value
     * will be bound to the key in the given map; if no value is found in
     * the application configuration settings, then the relativePath
     * will be resolved instead
     * @param relativePath the relative path name for the file or directory
     * to resolve in the current subsystem's framework
     * @return true if the binding was added using either the userSettingKey
     * or the relativePath, or false otherwise
     */
    protected boolean addFileBinding(
        NSMutableDictionary<String, String> map,
        String key,
        String userSettingKey,
        String relativePath)
    {
        String userSetting = Application.configurationProperties()
            .getProperty(userSettingKey);
        if (userSetting != null)
        {
            map.takeValueForKey(userSetting, key);
            return true;
        }
        else
        {
            return addFileBinding(map, key, relativePath);
        }
    }


    // ----------------------------------------------------------
    /**
     * Add a file resource definition to a dictionary.  This method is a
     * helper for subsystems that wish to add subsystem-specific file
     * resources to ENV variable definitions or plug-in properties.
     * @param map the dictionary to add the binding to
     * @param key the key to define in the map
     * @param relativePath the relative path name for the file or directory
     * to resolve in the current subsystem's framework
     * @return true if the relative path name exists and the binding was
     * added, or false otherwise
     */
    protected boolean addFileBinding(
        NSMutableDictionary<String, String> map,
        String key,
        String relativePath)
    {
        String rawPath = myResourcesDir() + "/" + relativePath;
        File file = new File(rawPath);
        if (file.exists())
        {
            try
            {
                String path = file.getCanonicalPath();
                map.takeValueForKey(path, key);
                return true;
            }
            catch (java.io.IOException e)
            {
                log.error("Attempting to get canonical path for " + rawPath
                    + " in " + getClass().getName(),
                    e);
            }
        }
        else
        {
            log.error("Cannot locate " + relativePath
                + " in Resources directory for " + getClass().getName());
        }
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Called by the subsystem manager to indicate this subsystem has
     * been initialized--this method should <b>not</b> be called by
     * anything else.
     */
    protected final void subsystemInitCompleted()
    {
        // This isn't called inside init() because we don't want this
        // value set until after subclasses have performed their
        // overridden init() actions as well (which is likely after
        // they call super.init()).
        isInitialized = true;
    }


    // ----------------------------------------------------------
    /**
     * Called by the subsystem manager to indicate this subsystem has
     * been initialized--this method should <b>not</b> be called by
     * anything else.
     */
    protected final void subsystemHasStarted()
    {
        // This isn't called inside start() because we don't want this
        // value set until after subclasses have performed their
        // overridden start() actions as well (which is likely after
        // they call super.start(), if they call it at all).
        hasStarted = true;
    }


    //~ Instance/static variables .............................................

    private String            name = getClass().getName();
    private String            myResourcesDir;
    private FeatureDescriptor descriptor;
    private NSDictionary<String, Object> options;
    private boolean           isInitialized;
    private boolean           hasStarted;

    private NSArray<TabDescriptor> subsystemTabTemplate;

    private NSMutableDictionary<String, Class<? extends WOComponent>>
        subsystemFragments;
    private boolean subsystemFragmentsLoaded;

    private static final String SUBSYSTEM_FRAGMENTS_PLIST_FILENAME =
        "SubsystemFragments.plist";

    static Logger log = Logger.getLogger(Subsystem.class);
}
