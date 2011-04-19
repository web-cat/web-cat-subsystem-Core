/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2008 Virginia Tech
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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 * Contains some common file copying operations used by the various archive
 * handlers.
 *
 * @author Tony Allowatt
 */
public class FileUtilities
{
    // ----------------------------------------------------------
    /**
     * Copies the source file to the destination file.
     *
     * @param srcFile A File object representing the path of the source file.
     * @param destFile A File object representing the path (and name) of the
     * destination file.
     *
     * @throws IOException
     */
    public static void copyFileToFile( File srcFile, File destFile )
        throws IOException
    {
        FileInputStream stream = new FileInputStream( srcFile );
        FileUtilities.copyStreamToFile(
            stream, destFile, srcFile.lastModified() );
        stream.close();
    }


    // ----------------------------------------------------------
    /**
     * Copies a file into a specified directory
     *
     * @param oldFile the file to copy
     * @param outputDir the destination directory
     * @throws IOException if there are IO errors
     */
    public static void copyFileToDir( File oldFile, File outputDir )
        throws IOException
    {
        FileInputStream  in  = new FileInputStream( oldFile );
        File destFile = new File( outputDir, oldFile.getName() );
        FileOutputStream out = new FileOutputStream( destFile );
        copyStream( in, out );
        in.close();
        out.close();
        destFile.setLastModified( oldFile.lastModified() );
    }


    // ----------------------------------------------------------
    /**
     * Copies a file into a specified directory if it does not already
     * exist there, or if the source is newer than the destination.
     *
     * @param oldFile the file to copy
     * @param outputDir the destination directory
     * @throws IOException if there are IO errors
     */
    public static void copyFileToDirIfNecessary( File oldFile, File outputDir )
        throws IOException
    {
        File target = new File( outputDir, oldFile.getName() );
        if ( !target.exists()
             || target.lastModified() < oldFile.lastModified() )
        {
            FileInputStream  in  = new FileInputStream( oldFile );
            FileOutputStream out = new FileOutputStream( target );
            copyStream( in, out );
            in.close();
            out.close();
            target.setLastModified( oldFile.lastModified() );
        }
    }


    // ----------------------------------------------------------
    /**
     * Recursively copies the contents of the source directory into the
     * destination directory.
     *
     * @param source the source directory
     * @param destination the destination directory
     * @throws IOException if there are IO errors
     */
    public static void copyDirectoryContents( File source, File destination )
        throws IOException
    {
        File[] fileList = source.listFiles();
        for ( int i = 0; i < fileList.length; i++ )
        {
            if ( fileList[i].isDirectory() )
            {
                // Copy the whole directory
                File newDir = new File( destination, fileList[i].getName() );
                newDir.mkdir();
                copyDirectoryContents( fileList[i], newDir );
            }
            else if (fileList[i].getName().equals(".DS_Store"))
            {
                // ignore these for Mac OSX
            }
            else
            {
                copyFileToDir( fileList[i], destination );
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Recursively copies the contents of the source directory into the
     * destination directory, only updating files that are missing or
     * outdated.
     *
     * @param source the source directory
     * @param destination the destination directory
     * @throws IOException if there are IO errors
     */
    public static void copyDirectoryContentsIfNecessary(
        File source, File destination )
        throws IOException
    {
        File[] fileList = source.listFiles();
        for ( int i = 0; i < fileList.length; i++ )
        {
            if ( fileList[i].isDirectory() )
            {
                // Copy the whole directory
                File newDir = new File( destination, fileList[i].getName() );
                newDir.mkdir();
                copyDirectoryContentsIfNecessary( fileList[i], newDir );
            }
            else if (fileList[i].getName().equals(".DS_Store"))
            {
                // ignore these for Mac OSX
            }
            else
            {
                copyFileToDirIfNecessary( fileList[i], destination );
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Recursively deletes a directory
     *
     * @param dirName the path of the directory
     */
    public static void deleteDirectory( String dirName )
    {
        deleteDirectory( new File( dirName ) );
    }


    // ----------------------------------------------------------
    /**
     * Recursively deletes a directory
     *
     * @param dir the File object for the directory
     */
    public static void deleteDirectory( File dir )
    {
        File[] files = dir.listFiles();
        for ( int i = 0; i < files.length; i++ )
        {
            if ( files[i].isDirectory() )
            {
                deleteDirectory( files[i] );
            }
            files[i].delete();
        }
        dir.delete();
    }


    // ----------------------------------------------------------
    /**
     * Move the specified file to a new location (path + filename).  First
     * tries to rename the file, and then does a copy/delete if renaming won't
     * work.
     *
     * @param source the file to move
     * @param destination the new file name to rename it to
     * @throws IOException if there are IO errors
     */
    public static void moveFileToFile( File source, File destination )
        throws IOException
    {
        if ( source.renameTo( destination ) )
        {
            // if renaming worked, then we're done
            return;
        }

        copyFileToFile( source, destination );
        source.delete();
    }


    // ----------------------------------------------------------
    /**
     * Move the specified file into the destination directory.  First tries
     * to rename the file, and then does a copy/delete if renaming won't
     * work.
     *
     * @param source the file to move
     * @param destDir the destination directory to move it to
     * @throws IOException if there are IO errors
     */
    public static void moveFileToDir( File source, File destDir )
        throws IOException
    {
        File destFile = new File( destDir, source.getName() );
        moveFileToFile( source, destFile );
    }


    // ----------------------------------------------------------
    /**
     * Recursively copies the contents of the source directory into the
     * destination directory.
     *
     * @param source the source directory
     * @param destination the destination directory
     * @throws IOException if there are IO errors
     */
    public static void moveDirectoryContents( File source, File destination )
        throws IOException
    {
        File[] fileList = source.listFiles();
        for ( int i = 0; i < fileList.length; i++ )
        {
            if ( fileList[i].isDirectory() )
            {
                // Copy the whole directory
                File newDir = new File( destination, fileList[i].getName() );
                newDir.mkdir();
                moveDirectoryContents( fileList[i], newDir );
                fileList[i].delete();
            }
            else
            {
                moveFileToDir( fileList[i], destination );
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Copies the contents of an input stream onto an existing output
     * stream.  The output stream is flushed when the operation
     * is complete.
     *
     * @param in  the input stream to copy
     * @param out the destination
     * @throws IOException if there are IO errors
     */
    public static void copyStream( InputStream in, OutputStream out )
        throws IOException
    {
        final int BUFFER_SIZE = 65536;

        // read in increments of BUFFER_SIZE
        byte[] b = new byte[BUFFER_SIZE];
        int count = in.read( b );
        while ( count > -1 )
        {
            out.write( b, 0, count );
            count = in.read( b );
        }
        out.flush();
    }


    // ----------------------------------------------------------
    /**
     * Copies data from the specified input stream to a file.
     *
     * @param stream An InputStream containing the data to be copied.
     * @param destFile A File object representing the path (and name) of the
     * destination file.
     *
     * @throws IOException
     */
    public static void copyStreamToFile( InputStream stream, File destFile )
        throws IOException
    {
        OutputStream outStream = new FileOutputStream( destFile );
        copyStream( stream, outStream );
        outStream.flush();
        outStream.close();
    }


    // ----------------------------------------------------------
    /**
     * Copies data from the specified input stream to a file and sets the
     * file's timestamp.
     *
     * @param stream An InputStream containing the data to be copied.
     * @param destFile A File object representing the path (and name) of the
     * destination file.
     * @param fileTime The timestamp to use for the destination file
     *
     * @throws IOException
     */
    public static void copyStreamToFile(
        InputStream stream, File destFile, long fileTime )
        throws IOException
    {
        copyStreamToFile( stream, destFile );
        if ( fileTime != -1 )
        {
            destFile.setLastModified( fileTime );
        }
    }


    // ----------------------------------------------------------
    /**
     * If the specified object is an InputStream or OutputStream, then it is
     * closed and any exceptions are ignored. Otherwise, this method does
     * nothing.
     *
     * @param object the object to close
     */
    public static void closeQuietly(Object object)
    {
        try
        {
            if (object instanceof InputStream)
            {
                ((InputStream) object).close();
            }
            else if (object instanceof OutputStream)
            {
                ((OutputStream) object).close();
            }
        }
        catch (Exception e)
        {
            // Do nothing.
        }
    }


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
                copyStream( stream, zos );
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
        return FileUtilities.fileProperties.getFileProperty(
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
     * Gets the URL for the icon to use to represent a folder. This method
     * always returns a closed folder image; to choose between open and closed
     * images (for example, in an interactive tree), use the
     * {@link #folderIconURL(boolean)} method instead.
     *
     * @return the folder icon URL
     */
    public static String folderIconURL()
    {
        return folderIconURL(false);
    }


    // ----------------------------------------------------------
    /**
     * Gets the URL for the icon to use to represent an open or closed folder.
     *
     * @param isOpen true to return an open folder, false to return a closed
     *     folder
     * @return the folder icon URL
     */
    public static String folderIconURL(boolean isOpen)
    {
        return isOpen ? FOLDER_OPEN_ICON : FOLDER_CLOSED_ICON;
    }


    // ----------------------------------------------------------
    /**
     * Return the URL for the icon to use to represent this file's type.
     * The check is performed based on the file's extension,
     * using settings loaded from an external properties file containing
     * file type definitions.
     *
     * @param  fileName the file name to check
     * @return the icon URL
     */
    public static String iconURL( String fileName )
    {
        return FileUtilities.fileProperties.getFileProperty(
            extensionOf(fileName), "icon", DEFAULT_FILE_ICON);
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
        return iconURL(file.getName());
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
        return FileUtilities.fileProperties.getFileFlag(
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
        return FileUtilities.fileProperties.getFileFlag(
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
        return FileUtilities.fileProperties.getFileFlag(
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


    // ----------------------------------------------------------
    /**
     * Returns a string version of the given file size.
     *
     * @param  size the file size to convert
     * @return the file size as a string
     */
    public static String fileSizeAsString( long size )
    {
        StringBuffer result = new StringBuffer( 10 );
        if ( size < 1024L )
        {
            result.append( size );
            result.append( " bytes" );
        }
        else if ( size < 1048576L )
        {
            double sz = size / 1024.0;
            DecimalFormat fmt = new DecimalFormat( "0.0" );
            fmt.format( sz, result,
                        new FieldPosition( DecimalFormat.FRACTION_FIELD ) );
            result.append( "kb" );
        }
        else
        {
            double sz = size / 1048576.0;
            DecimalFormat fmt = new DecimalFormat( "0.0" );
            fmt.format( sz, result,
                        new FieldPosition( DecimalFormat.FRACTION_FIELD ) );
            result.append( "mb" );
        }
        return result.toString();
    }


    // ----------------------------------------------------------
    /**
     * Reads the contents of the specified file into a string and returns it.
     * The file is assumed to be encoded in UTF-8.
     *
     * @param path the path to the file
     * @return the contents of the file
     */
    public static String stringWithContentsOfFile(String path)
    {
        return stringWithContentsOfFile(new File(path));
    }


    // ----------------------------------------------------------
    /**
     * Reads the contents of the specified file into a string and returns it.
     * The file is assumed to be encoded in UTF-8.
     *
     * @param file the path to the file
     * @return the contents of the file
     */
    public static String stringWithContentsOfFile(File file)
    {
        try
        {
            byte[] buffer = new byte[(int) file.length()];
            BufferedInputStream stream =
                new BufferedInputStream(new FileInputStream(file));
            stream.read(buffer);
            stream.close();

            return new String(buffer, "UTF-8");
        }
        catch (IOException e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Writes a string to the file at the specified path, overwriting it if it
     * already exists.
     *
     * @param string the string
     * @param path the path of the file to write
     */
    public static void writeStringToFile(String string, String path)
    {
        writeStringToFile(string, new File(path));
    }


    // ----------------------------------------------------------
    /**
     * Writes a string to the file at the specified path, overwriting it if it
     * already exists.
     *
     * @param string the string
     * @param file the path of the file to write
     */
    public static void writeStringToFile(String string, File file)
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(string);
            writer.close();
        }
        catch (IOException e)
        {
            // Do nothing.
        }
    }


    //~ Static variables ......................................................

    @SuppressWarnings( "deprecation" )
    static WCFileProperties fileProperties =
        new WCFileProperties(
            Application.configurationProperties().getProperty(
                "filetype.properties",
                Application.application().resourceManager()
                .pathForResourceNamed( "filetype.properties", "Core", null ) ),
            Application.configurationProperties() );

    private static final String FOLDER_OPEN_ICON =
        "icons/filetypes/folder-open.png";

    private static final String FOLDER_CLOSED_ICON =
        "icons/filetypes/folder.png";

    private static final String DEFAULT_FILE_ICON =
        "icons/filetypes/document.png";

    static Logger log = Logger.getLogger(FileUtilities.class);
}
