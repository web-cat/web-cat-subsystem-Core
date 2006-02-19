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
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Manages the Subsystem's stored on disk. A subsystem is either a WebObjects
 * framework or a separate jar file that contains a framework.
 *
 *  @author Stephen Edwards
 *  @version $Id$
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
    public SubsystemManager( WCProperties properties )
    {
        log.debug( "creating subsystem manager" );
        if ( properties != null )
        {
            String jarDir = properties.getProperty( "subsystem.jar.dir" );
            if ( jarDir != null )
            {
                addSubsystemJarsFromDirectory( jarDir );
            }

            NSArray subs =
                properties.arrayForKey( "subsystem.unjarred.classes" );
            if ( subs != null )
            {
                for ( int i = 0; i < subs.count(); i++ )
                {
                    addSubsystemFromClassName(
                        (String)subs.objectAtIndex( i ) );
                }
            }
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Search a given directory for jars, and load the subsystem
     * contained in each one.
     * 
     * @param directory  The root directory for loading the jar'ed subsystems
     */
    public void addSubsystemJarsFromDirectory( String directory )
    {
        File root = new File( directory );

        // Get list of all .jar files in the directory
        File[] jars = root.listFiles( new FilenameFilter() {
            public boolean accept( File dir, String name )
            {
                return name.endsWith( ".jar" );
            }
        } );

        if ( jars != null )
        {
            // Add the jar files
            for ( int i = 0; i < jars.length; ++i )
            {
                try
                {
                    addSubsystemFromJarFile( jars[i] );
                }
                catch ( Exception e )
                {
                    log.error( "Exception adding subsystem: "
                               + jars[i].toString() + ":", e );
                }
            }
        }

    }


    // ----------------------------------------------------------
    /**
     * Get the requested subsystem.
     * 
     * @param name  The name of the subsystem to get
     * @return      The corresponding JarSubsystem
     */
    public Subsystem subsystem( String name )
    {
        return (Subsystem)subsystems.get( name );
    }


    // ----------------------------------------------------------
    /**
     * Get an iterator for all loaded subsystems by name.
     *
     * @return An iterator for the names of all loaded subsystems
     */
    public Iterator subsystems()
    {
        return subsystems.keySet().iterator();
    }


    // ----------------------------------------------------------
    /**
     * Add a JarSubsystem to the manager, given a file name.
     * 
     * @param name  The file name of the JAR file that contains the subsystem
     * @throws IOException
     */
    public void addSubsystemFromJarFile( String name )
        throws IOException
    {
        addSubsystemFromJarFile( new File( name ) );
    }


    // ----------------------------------------------------------
    /**
     * Add a JarSubsystem to the manager, given a file object.
     * 
     * @param file  The file of the JAR file that contains the subsystem
     * @throws IOException
     */
    public void addSubsystemFromJarFile( File file )
         throws IOException
    {
        JarSubsystem s = JarSubsystem.initializeSubsystemFromJar( file );
        if ( s != null )
        {
            addSubsystem( s );
        }
    }


    // ----------------------------------------------------------
    /**
     * Add a Subsystem to the manager, given a class name.
     * 
     * @param name the class name to load
     */
    public void addSubsystemFromClassName( String name )
    {
        log.debug( "attempting to load subsystem from class '" + name + "'");
        try
        {
            addSubsystem( (Subsystem)DelegatingUrlClassLoader.getClassLoader()
                            .loadClass( name ).newInstance() );
        }
        catch ( Exception e )
        {
            log.error( "Exception loading subsystem:", e );
        }
    }


    // ----------------------------------------------------------
    /**
     * Add a Subsystem to the manager, given a subsystem object.
     * 
     * @param s  The subsystem object to add
     */
    public void addSubsystem( Subsystem s )
    {
        String className = s.getClass().getName();
        if ( !subsystems.containsKey( className ) )
        {
            log.info( "Registering subsystem: " + className );
            subsystems.put( className, s );
        }
        else
        {
            log.error( "Subsystem already registered: " + className );
        }
    }


    // ----------------------------------------------------------
    /**
     * Calls {@link Subsystem#initializeSessionData(Session)} for
     * all registered subsystems.
     * @param s the session to initialize
     */
    public void initializeSessionData( Session s )
    {
        Iterator keys = subsystems();
        while ( keys.hasNext() )
        {
            Object nextKey = keys.next();
            ( (Subsystem)subsystems.get( nextKey ) )
                .initializeSessionData( s );
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
    public void collectSubsystemFragments(
        String fragmentKey, StringBuffer htmlBuffer, StringBuffer wodBuffer )
    {
        Iterator keys = subsystems();
        while ( keys.hasNext() )
        {
            Object nextKey = keys.next();
            ( (Subsystem)subsystems.get( nextKey ) )
                .collectSubsystemFragments(
                    fragmentKey, htmlBuffer, wodBuffer );
        }
    }


    //~ Instance/static variables .............................................

    /** Map&lt;String, JarSubsystem&gt;: holds the loaded subsystems. */
    private Map subsystems = new HashMap();

    static Logger log = Logger.getLogger( SubsystemManager.class );
}
