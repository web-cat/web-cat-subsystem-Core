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
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  Provides support functions for manipulating files.
 *
 *  @author  Stephen Edwards
 *  @version $Id$
 */
public class WCFile
{
    // ----------------------------------------------------------
    /**
     * Appends the contents of this file or directory to the given
     * ZipOutputStream.
     *
     * @param file          The file/directory to append
     * @param zos           The output stream to append to
     * @param parentNameLen How much of the file's canonical path name prefix
     *                      to truncate when creating the zip entry
     */
    public static void appendToZip( File            file,
                                    ZipOutputStream zos,
                                    int             parentNameLen )
    {
        try
        {
            if ( file.isDirectory()
                 &&  !file.getName().equals( "." )
                 &&  !file.getName().equals( ".." ) )
            {
                File[] files = file.listFiles();
                if ( files != null )
                {
                    for ( int i = 0; i < files.length; i++ )
                    {
                        appendToZip( files[i], zos, parentNameLen );
                    }
                }
            }
            else
            {
                String fileName =
                    file.getCanonicalPath().substring( parentNameLen + 1 );
                if ( fileName.length() > 1
                     && ( fileName.charAt( 0 ) == '/'
                          || fileName.charAt( 0 ) == '\\' ) )
                {
                    fileName = fileName.substring( 1 );
                }
                // If we're on a Windows-style system, be sure to switch
                // to forward slashes for path names inside the zip file
                if ( System.getProperty( "file.separator" ).equals( "\\" ) )
                {
                    fileName = fileName.replace( '\\', '/' );
                }
                ZipEntry e = new ZipEntry( fileName );
                e.setSize( file.length() );
                zos.putNextEntry( e );
                FileInputStream stream = new FileInputStream( file );
                net.sf.webcat.archives.FileUtilities
                    .copyStream( stream, zos );
                stream.close();
            }
        }
        catch ( java.io.IOException e )
        {
            log.error( "exception trying to create zip output stream", e );
        }
    }


    // ----------------------------------------------------------
    /**
     * Return the extension of this file name (the characters after the
     * last dot in the name).
     *
     * @param  fileName the file name
     * @return its extension
     */
    public static String extensionOf( String fileName )
    {
        int pos = fileName.lastIndexOf( '.' );
        return ( pos < 0 ) ? fileName : fileName.substring( pos + 1 );
    }


    // ----------------------------------------------------------
    /**
     * Return the extension of this file name (the characters after the
     * last dot in the name).
     *
     * @param  file the file
     * @return its extension
     */
    public static String extensionOf( File file )
    {
        return extensionOf( file.getName() );
    }


    // TODO: should refactor into ArchiveManager ... possibly other
    // static methods need refactoring, too.
    // ----------------------------------------------------------
    /**
     * Determine if this is an archive file.
     *
     * @param  fileName the file name
     * @return True if it is an archive file (currently, a zip or jar file)
     */
    public static boolean isArchiveFile( String fileName )
    {
        String ext = extensionOf( fileName ).toLowerCase();
        return ext.equals( "zip" ) || ext.equals( "jar" );
    }


    // ----------------------------------------------------------
    /**
     * Determine if this is an archive file.
     *
     * @param  file the file
     * @return True if it is an archive file (currently, a zip or jar file)
     */
    public static boolean isArchiveFile( File file )
    {
        return isArchiveFile( file.getName() );
    }


    // ----------------------------------------------------------
    /**
     * Return the MIME type associated with this file. 
     * The check is performed based on the file's extension,
     * using settings loaded from an external properties file containing
     * file type definitions.
     *
     * @param  fileName the file name to check
     * @return the MIME type
     */
    public static String mimeType( String fileName )
    {
        return fileProperties.getFileProperty(
            extensionOf( fileName ), "mimeType", "application/octet-stream" );
    }


    // ----------------------------------------------------------
    /**
     * Return the MIME type associated with this file. 
     * The check is performed based on the file's extension,
     * using settings loaded from an external properties file containing
     * file type definitions.
     *
     * @param  file the file to check
     * @return the MIME type
     */
    public static String mimeType( File file )
    {
        return mimeType( file.getName() );
    }


    // ----------------------------------------------------------
    /**
     * Return the URL for the icon to use for to represent this file's type. 
     * The check is performed based on the file's extension,
     * using settings loaded from an external properties file containing
     * file type definitions.
     *
     * @param  fileName the file name to check
     * @return the icon URL
     */
    public static String iconURL( String fileName )
    {
        return fileProperties.getFileProperty(
            extensionOf( fileName ), "icon", "/icons/filetypes/unknown.gif" );
    }


    // ----------------------------------------------------------
    /**
     * Return the URL for the icon to use for to represent this file's type. 
     * The check is performed based on the file's extension,
     * using settings loaded from an external properties file containing
     * file type definitions.
     *
     * @param  file the file to check
     * @return the icon URL
     */
    public static String iconURL( File file )
    {
        return iconURL( file.getName() );
    }


    // ----------------------------------------------------------
    /**
     * Return true if this file's type can be executed on the server. 
     * The check is performed based on the file's extension,
     * using settings loaded from an external properties file containing
     * file type definitions.
     *
     * @param  fileName the file name to check
     * @return true if this file can be executed
     */
    public static boolean isExecutable( String fileName )
    {
        return fileProperties.getFileFlag(
            extensionOf( fileName ), "executable", false );
    }


    // ----------------------------------------------------------
    /**
     * Return true if this file's type can be executed on the server. 
     * The check is performed based on the file's extension,
     * using settings loaded from an external properties file containing
     * file type definitions.
     *
     * @param  file the file to check
     * @return true if this file can be executed
     */
    public static boolean isExecutable( File file )
    {
        return isExecutable( file.getName() );
    }


    // ----------------------------------------------------------
    /**
     * Return true if this file's type can be edited as a text file. 
     * The check is performed based on the file's extension,
     * using settings loaded from an external properties file containing
     * file type definitions.
     *
     * @param  fileName the file name to check
     * @return true if this file can be edited
     */
    public static boolean isEditable( String fileName )
    {
        return fileProperties.getFileFlag(
            extensionOf( fileName ), "editable", false );
    }


    // ----------------------------------------------------------
    /**
     * Return true if this file's type can be edited as a text file. 
     * The check is performed based on the file's extension,
     * using settings loaded from an external properties file containing
     * file type definitions.
     *
     * @param  file the file to check
     * @return true if this file can be edited
     */
    public static boolean isEditable( File file )
    {
        return isEditable( file.getName() );
    }


    // ----------------------------------------------------------
    /**
     * Return true if this file's type should be shown in a browser
     * window.  The check is performed based on the file's extension,
     * using settings loaded from an external properties file containing
     * file type definitions.
     *
     * @param  fileName the file name to check
     * @return true if this file's type should be shown in a browser
     */
    public static boolean showInline( String fileName )
    {
        return fileProperties.getFileFlag(
            extensionOf( fileName ), "showInline", false );
    }


    // ----------------------------------------------------------
    /**
     * Return true if this file's type should be shown in a browser
     * window.  The check is performed based on the file's extension,
     * using settings loaded from an external properties file containing
     * file type definitions.
     *
     * @param  file the file to check
     * @return true if this file's type should be shown in a browser
     */
    public static boolean showInline( File file )
    {
        return showInline( file.getName() );
    }


    //~ Instance/static variables .............................................

    @SuppressWarnings( "deprecation" )
    private static WCFileProperties fileProperties =
        new WCFileProperties(
            Application.configurationProperties().getProperty(
                "filetype.properties",
                Application.application().resourceManager()
                .pathForResourceNamed( "filetype.properties", "Core", null ) ),
            Application.configurationProperties() );
    
    static Logger log = Logger.getLogger( WCFile.class );
}
