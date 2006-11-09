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

import java.io.*;
import java.util.Iterator;
import net.sf.webcat.FeatureDescriptor;
import net.sf.webcat.WCServletAdaptor;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  The subsystem interface that defines the API used by the Core to
 *  communicate with subsystems.
 *
 *  @author Stephen Edwards
 *  @version $Id$
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
    public void setName( String newName )
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
        if ( descriptor == null )
        {
            // First, look to see if there is an appropriate subsystem updater
            WCServletAdaptor adaptor = WCServletAdaptor.getInstance();
            if ( adaptor != null )
            {
                for ( Iterator i = adaptor.subsystems().iterator();
                      i.hasNext(); )
                {
                    FeatureDescriptor sd = (FeatureDescriptor)i.next();
                    if ( name.equals( sd.name() ) )
                    {
                        // found it!
                        descriptor = sd;
                        break;
                    }
                }
            }
            // Otherwise, try to create one directly from properties
            if ( descriptor == null )
            {
                descriptor = new FeatureDescriptor( name(),
                    Application.configurationProperties(), false );
            }
        }
        return descriptor;
    }


    // ----------------------------------------------------------
    /**
     * Get a list of WO components that should be instantiated and presented
     * on the front page.
     * 
     * @return The list of names, as strings
     */
    public NSArray frontPageStatusComponents()
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
    public NSArray EOModelPathsInJar()
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Carry out any subsystem-specific startup actions.  This method is
     * called once all subsystems have been created, so any dependencies
     * on services provided by other subsystems are fulfilled.  Subsystems
     * are init'ed in the same order they are created.
     */
    public void init()
    {
        // Subclasses should override this as necessary
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
    public NSDictionary parameterDescriptions()
    {
        if ( options == null )
        {
            File configFile = new File( myResourcesDir() + "/config.plist" );
            log.debug( "Atempting to locate parameter descriptions in: "
                + configFile.getPath() );
            if ( !configFile.exists() )
            {
                // If not found, try looking directly in the bundle, in case
                // the resources dir was overridden by properties (like on
                // the main development machine!).  This is purely to support
                // development-mode hacks, and probably won't ever be used
                // in production.  See the comments in myResourcesDir()
                // regarding the resourcePath() method being deprecated.
                NSBundle myBundle = myBundle();
                if ( myBundle != null )
                {
                    configFile = new File(
                        myBundle.resourcePath() + "/config.plist" );
                    log.debug(
                        "Atempting to locate parameter descriptions in: "
                        + configFile.getPath() );
                }
            }
            if ( configFile.exists() )
            {
                try
                {
                    log.debug( "loading parameter descriptions from: "
                        + configFile.getPath() );
                    FileInputStream in = new FileInputStream( configFile );
                    NSData data = new NSData( in, (int)configFile.length() );
                    options = (NSDictionary)NSPropertyListSerialization
                        .propertyListFromData( data, "UTF-8" );
                    in.close();
                }
                catch ( java.io.IOException e )
                {
                    log.error(
                        "error reading from subsystem configuration file "
                        + configFile.getPath(),
                        e );
                }
            }
            if ( log.isDebugEnabled() )
            {
                log.debug( "loaded parameter descriptions for subsystem "
                    + name() + ":\n" + options );
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
    public void initializeSessionData( Session s )
    {
        // Subclasses should override this as necessary
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
    public void collectSubsystemFragments(
        String fragmentKey, StringBuffer htmlBuffer, StringBuffer wodBuffer )
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
    public void addEnvironmentBindings( NSMutableDictionary env )
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
    public void addPluginPropertyBindings( NSMutableDictionary properties )
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
            WOContext context )
    {
        throw new RuntimeException(
            "invalid subsystem direct action request: "
            + "\n---request---\n" + request
            + "\n\n---session---\n" + session
            + "\n\n---context---\n" + context
            );
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
        NSBundle result = NSBundle.bundleForName( name() );
        if ( result == null  && getClass() != Subsystem.class )
        {
            result = NSBundle.bundleForClass( getClass() );
        }
        if ( result == null && !"webcat".equals( name() ) )
        {
            log.error( "cannot find bundle for subsystem " + name() );
        }
        return result;
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
    protected String myResourcesDir()
    {
        if ( myResourcesDir == null )
        {
            // First, look for an overriding property, like those that
            // might be used for non-servlet deployment scenarios.
            myResourcesDir = Application.configurationProperties()
                .getProperty( name() + ".Resources" );
        }
        if ( myResourcesDir == null )
        {
            NSBundle myBundle = myBundle();
            if ( myBundle != null )
            {
                // Note that the resourcePath() method is deprecated, but it
                // is the best way to get what we need here, so we'll use it
                // anyway, rather than re-implementing it.
                myResourcesDir = myBundle.resourcePath();
            }
        }
        return myResourcesDir;
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
    protected boolean addFileBinding( NSMutableDictionary map,
                                      String              key,
                                      String              userSettingKey,
                                      String              relativePath )
    {
        String userSetting = Application.configurationProperties()
            .getProperty( userSettingKey );
        if ( userSetting != null )
        {
            map.takeValueForKey( userSetting, key );
            return true;
        }
        else
        {
            return addFileBinding( map, key, relativePath );
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
        NSMutableDictionary map, String key, String relativePath )
    {
        String rawPath = myResourcesDir() + "/" + relativePath;
        File file = new File( rawPath );
        if ( file.exists() )
        {
            try
            {
                String path = file.getCanonicalPath();
                map.takeValueForKey( path, key );
                return true;
            }
            catch ( java.io.IOException e )
            {
                log.error( "Attempting to get canonical path for " + rawPath
                    + " in " + getClass().getName(),
                    e );
            }
        }
        else
        {
            log.error( "Cannot locate " + relativePath
                + " in Resources directory for " + getClass().getName() );
        }
        return false;
    }


    //~ Instance/static variables .............................................

    private String            name = getClass().getName();
    private String            myResourcesDir;
    private FeatureDescriptor descriptor;
    private NSDictionary      options;

    static Logger log = Logger.getLogger( Subsystem.class );
}
