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

import java.io.*;
import java.util.*;

import org.apache.log4j.*;

import sun.security.action.*;

// -------------------------------------------------------------------------
/**
 *  This extension of WCProperties adds a few small features that are useful
 *  for managing an installation configuration file.
 *
 *  @author  stedwar2
 *  @version $Id$
 */
public class WCConfigurationFile
    extends WCProperties
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new WCConfigurationFile object stored in a given
     * file.  Note that this subclass does not support any inherited
     * defaults from other Properties objects.  In actuality, it always
     * inherits from System.properties(), which is maintained by the
     * ERXProperties and ERXConfigurationManager classes.
     * 
     * @param filename The file to load from and store to
     */
    public WCConfigurationFile( String filename )
    {
        // We're not using the two-arg superclass constructor, since
        // we're doing the loading ourselves down below.
        super( System.getProperties() );
        this.configFile = new java.io.File( filename );
        log.info( "Atttempting to load configuration from "+ filename );
        attemptToLoad( filename );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Access the file object that corresponding to the on-disk image of
     * these configuration settings.
     * @return the configuration file object
     */
    public File file()
    {
        return configFile;
    }


    // ----------------------------------------------------------
    /**
     * Check to see if the configuration file exists.
     * 
     * @return true if this configuration file exists
     */
    public boolean exists()
    {
        return configFile.exists();
    }


    // ----------------------------------------------------------
    /**
     * Check to see if the configuration file exists.
     * 
     * @return true if this configuration file exists
     */
    public boolean hasUsableConfiguration()
    {
        boolean oldIsInstalling = isInstalling();
        setIsInstalling( true );
        boolean result =
            getProperty( "configStep" ) == null
            && getProperty( "applicationBaseURL" ) != null
            && getProperty( "dbConnectURLGLOBAL" ) != null
            && getProperty( "dbConnectUserGLOBAL" ) != null
            && getProperty( "dbConnectPasswordGLOBAL" ) != null
            && booleanForKey( "installComplete" );
        setIsInstalling( oldIsInstalling );
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Check to see if the configuration file can be written to.
     * 
     * @return true if this configuration file is writeable
     */
    public boolean isWriteable()
    {
        if ( configFile.exists() )
        {
            return configFile.canWrite();
        }
        File parent = configFile.getParentFile();
        return parent.exists() && parent.canWrite();
    }


    // ----------------------------------------------------------
    public synchronized void store( OutputStream out ) throws IOException
    {
        super.store( out, header );
    }


    // ----------------------------------------------------------
    /**
     * Save properties to the corresponding file if possible.
     * @return true if contents were saved, false otherwise.
     */
    public boolean attemptToSave()
    {
        if ( !isWriteable() ) return false;
        try
        {
            OutputStream out = new FileOutputStream( configFile );
            log.info( "Saving configuration properties to "
                      + configFile.getAbsolutePath() );
            store( out );
            out.close();
            return true;
        }
        catch ( IOException e )
        {
            log.error( "Error saving configuration properties to "
                       + configFile.getAbsolutePath()
                       + ":",
                       e );
        }
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Save properties to the corresponding file if possible.
     */
    public void updateToSystemProperties()
    {
        for ( Iterator i = localEntrySet().iterator(); i.hasNext(); )
        {
            Map.Entry e = (Map.Entry)i.next();
            System.setProperty(
                e.getKey().toString(), e.getValue().toString() );
        }
        er.extensions.ERXLogger.configureLoggingWithSystemProperties();
    }


    // ----------------------------------------------------------
    public void setIsInstalling( boolean value )
    {
        isInstalling = value;
    }


    // ----------------------------------------------------------
    public boolean isInstalling()
    {
        return isInstalling;
    }


    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see net.sf.webcat.core.WCProperties#applicationNameForAppending()
     * Override so that "property.Web-CAT"-style keys aren't actually
     * searched in the config file while installing
     */
    protected String applicationNameForAppending()
    {
        return isInstalling ? null : super.applicationNameForAppending();
    }


    // ----------------------------------------------------------
    public String configSettingsAsString()
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            store( out );
            out.close();
            return new String( out.toByteArray() );
        }
        catch ( IOException e )
        {
            String msg = "Error saving configuration properties to string: ";
            log.error( msg, e );
            return msg + e.getMessage();
        }
    }


    //~ Instance/static variables .............................................

    protected File configFile;
    private   boolean isInstalling = false;

    public static final String header =
        " Web-CAT configuration settings\n"
        + "# WARNING: do not edit this file.  It is automatically generatedd.\n"
        + "# Instead, use the Administer tab via Web-CAT's web interface to\n"
        + "# make any changes.";
    static Logger log = Logger.getLogger( WCConfigurationFile.class );
}
