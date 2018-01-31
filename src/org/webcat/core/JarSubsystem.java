/*==========================================================================*\
 |  $Id: JarSubsystem.java,v 1.2 2011/03/07 18:44:37 stedwar2 Exp $
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

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import java.io.*;
import java.net.*;
import java.util.jar.*;
import java.lang.reflect.Constructor;
import org.webcat.core.DelegatingUrlClassLoader;
import org.webcat.core.JarSubsystem;
import org.webcat.core.Subsystem;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  Represents a Web-CAT Subsystem maintained within a JAR file.  All
 *  jar-based subsystems should inherit from this base to get access
 *  to jar-oriented functions.  Such classes should only implement
 *  a single constructor corresponding to (and calling) the
 *  <code>JarSubsystem(JarFile)</code> constructor in this class.
 *
 *  @author lally
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2011/03/07 18:44:37 $
 */
public abstract class JarSubsystem
    extends Subsystem
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initialize a subsystem from a jar file.
     *
     * @param jarFile The file that contains the subsystem being loaded
     */
    public JarSubsystem(JarFile jarFile)
    {
        super();
        this.jarFile = jarFile;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Initialize a subsystem from a jar file.
     *
     * @param jarFile The file that contains the subsystem to load
     * @return the new subsystem object
     * @throws IOException
     */
    @SuppressWarnings("deprecation")
    static public JarSubsystem initializeSubsystemFromJar(File jarFile)
        throws IOException
    {
        JarFile      file   = new JarFile(jarFile);
        URL          u      = jarFile.toURL();
        JarSubsystem result = null;

        log.info("Loading subsystem: " + u.toString());
        DelegatingUrlClassLoader.getClassLoader().addURL(u);
        Class<?> subsysClass = loadMainClass(file);
        if (subsysClass != null)
        {
            try
            {
                Constructor<?> constructor = subsysClass.getConstructor(
                        new Class[] { JarFile.class }
                    );
                result = (JarSubsystem)constructor.newInstance(
                        new Object[] { file }
                    );
            }
            catch (Exception e)
            {
                log.error("Exception loading subsystem: ", e);
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Get the subsystem's main class.
     *
     * @param jarFile the jar file from which to retrieve the main class
     * @return The class that represents the subsystem
     */
    static public Class<?> loadMainClass(JarFile jarFile)
    {
        try
        {
            String mainClass =
                jarFile.getManifest().getMainAttributes().getValue(
                    Attributes.Name.MAIN_CLASS);
            return DelegatingUrlClassLoader.getClassLoader().loadClass(
                mainClass);
        }
        catch (Exception e)
        {
            log.error("Exception loading subsystem: ", e);
        }
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Get the jar file for this subsystem.
     *
     * @return The jar file containing the subsystem
     */
    public JarFile jarFile()
    {
        return jarFile;
    }


    // ----------------------------------------------------------
    /**
     * Get a file ("entry") that resides within the subsystem's JAR file.
     *
     * @param entry The file to get
     * @return      An InputStream to the requested entry
     * @throws IOException If an error occurs getting the entry
     */
    public InputStream jarFileEntry(String entry)
        throws IOException
    {
        return jarFile.getInputStream(jarFile.getEntry(entry));
    }


    // ----------------------------------------------------------
    /**
     * Returns a mimetype for a given pathname.  This is only valid for
     * <b>image</b> files.  Everything else returns <code>null</code>.
     * TODO: integrate this with the primary MIME decoding logic elsewhere
     * in the project.
     *
     * @param path  The path to test
     * @return      The MIME type of the given path
     */
    public static String mimeTypeForPath(String path)
    {
        if (path.endsWith(".jpg"))
        {
            return "image/jpeg";
        }
        else if (path.endsWith(".gif"))
        {
            return "image/gif";
        }
        else if (path.endsWith(".png"))
        {
            return "image/png";
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns whether or not the given path is safe to return.  This denies
     * the downloading of files other than those that should be. Example:
     * .class files shouldn't be downloaded, but .gif files should.
     *
     * @param path The path inside the jar to be verified
     * @return     True if the path describes an allowed file type
     */
    public static boolean pathIsSafeToReturn(String path)
    {
        return mimeTypeForPath(path) != null;
    }


    // ----------------------------------------------------------
    /**
     * Returns a WOResponse containing the data from the given jarpath,
     * which contains the file <b>iff</b> the path is safe, as determined
     * by {@link #pathIsSafeToReturn(String)}.
     *
     * @param url The path of the file within the JAR.  Example:
     *            /images/foo.gif
     * @return The WOResponse to display to the user
     */
    public WOResponse jarResponseFromUrl(String url)
    {
        WOResponse response = new WOResponse();

        if (pathIsSafeToReturn(url))
        {
            try
            {
                // TODO: check to see if this leaks file handles or other
                // resources!
                response.setContent(
                    new NSData(jarFileEntry(url), CHUNK_SIZE));
                response.setHeader(
                    "Content-Type", mimeTypeForPath(url));
            }
            catch (Exception e)
            {
                response.setStatus(WOMessage.HTTP_STATUS_NOT_FOUND);
                e.printStackTrace();
            }
        }
        else
        {
            response.setStatus(WOMessage.HTTP_STATUS_FORBIDDEN);
        }
        return response;
    }


    //~ Instance/static variables .............................................

    private static int  CHUNK_SIZE  = 16384;
    /** The jar file containing this subsystem. */
    private JarFile     jarFile     = null;

    static Logger log = Logger.getLogger(JarSubsystem.class);
}
