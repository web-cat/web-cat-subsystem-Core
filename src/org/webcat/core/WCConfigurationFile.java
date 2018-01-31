/*==========================================================================*\
 |  $Id: WCConfigurationFile.java,v 1.2 2011/03/07 18:44:37 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2011 Virginia Tech
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

import java.io.*;
import java.util.*;
import org.webcat.core.WCConfigurationFile;
import org.webcat.core.WCProperties;
import org.apache.log4j.*;

// -------------------------------------------------------------------------
/**
 *  This extension of WCProperties adds a few small features that are useful
 *  for managing an installation configuration file.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2011/03/07 18:44:37 $
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
        return getProperty( "configStep" ) == null
            && getProperty( "base.url" ) != null
            && getProperty( "dbConnectURLGLOBAL" ) != null
            && getProperty( "dbConnectUserGLOBAL" ) != null
            && getProperty( "dbConnectPasswordGLOBAL" ) != null
            && booleanForKey( "installComplete" );
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
        for (Map.Entry<Object, Object> e : localEntrySet())
        {
            System.setProperty((String)e.getKey(), e.getValue().toString());
        }
        er.extensions.foundation.ERXSystem.updateProperties();
        er.extensions.logging.ERXLogger.configureLoggingWithSystemProperties();
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

    public static final String header =
        " Web-CAT configuration settings\n"
        + "# WARNING: do not edit this file.  It is automatically generated.\n"
        + "# Instead, use the Administer tab via Web-CAT's web interface to\n"
        + "# make any changes.";
    static Logger log = Logger.getLogger( WCConfigurationFile.class );
}
