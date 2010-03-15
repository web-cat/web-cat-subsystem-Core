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

package net.sf.webcat.archives;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.sf.webcat.core.FileUtilities;


//-------------------------------------------------------------------------
/**
 * This class allows clients to register handlers for various archive formats,
 * in order to list their contents or unpack them in a type-independent manner.
 * <p>
 * The archive manager can also handle directories and files similiarly:
 * <ul>
 * <li>
 *   For directories, the getContents() method recursively lists its
 *   contents, and unpack() performs a recursive file-copy operation of the
 *   directory's contents to the destination path.
 * </li>
 * <li>
 *   For a file that is not handled by any registered archive handlers,
 *   getContents() simply returns the name of the file itself, and unpack()
 *   performs a single file-copy operation of that file to the destination
 *   path.
 * </li>
 * </ul>
 *
 * @author Tony Allowatt
 */
public class ArchiveManager
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
	/*
	 * Initializes an object of the ArchiveManager class.
	 */
	private ArchiveManager()
	{
		archiveHandlers = new ArrayList();
	}


    // ----------------------------------------------------------
	/**
	 * Returns the single instance of the ArchiveManager class.
	 *
	 * @return A reference to the ArchiveManager singleton.
	 */
	public static ArchiveManager getInstance()
	{
		if ( instance == null )
        {
			instance = new ArchiveManager();
        }
		return instance;
	}


    //~ Methods ...............................................................

    // ----------------------------------------------------------
	/**
	 * Add an archive handler to the manager.  Handlers are asked in the order
	 * they were added whether they can handle a given archive.
	 *
	 * @param handler An archive handler object that implements the
	 * IArchiveHandler interface.
	 */
	public void addHandler( IArchiveHandler handler )
	{
		archiveHandlers.add( handler );
	}


    // ----------------------------------------------------------
	/**
	 * Gets the contents of the archive pointed to by the specified File. No
	 * guarantees are made as to whether an IArchiveEntry for a directory in
	 * the archive will be returned before its children; this behavior is
	 * dependent on the underlying archive handler.
	 *
	 * @param file A File object representing the archive.
	 *
	 * @return An array of IArchiveEntry objects describing the contents of
	 * the archive.
	 *
	 * @throws IOException
	 */
	public IArchiveEntry[] getContents( File file )
        throws IOException
	{
		IArchiveHandler handler = findHandler( file.getName() );

		if ( handler != null )
		{
			return handler.getContents( file );
		}
		else
		{
			if ( file.isDirectory() )
			{
				ArrayList entryList = new ArrayList();
				getDirectoryContents( file, file, entryList );

				IArchiveEntry[] entryArray =
					new IArchiveEntry[entryList.size()];
				entryList.toArray( entryArray );
				return entryArray;
			}
			else
			{
				return new IArchiveEntry[] {
					new ArchiveEntry( file.getName(), file.isDirectory(),
							new Date( file.lastModified() ), file.length() )
				};
			}
		}
	}


    // ----------------------------------------------------------
	/**
	 * Gets the contents of the archive to be read from the specified
	 * InputStream. A filename must be associated with the stream in order to
	 * properly detect the type of archive being passed. No guarantees are
	 * made as to whether an IArchiveEntry for a directory in the archive will
	 * be returned before its children; this behavior is dependent on the
	 * underlying archive handler.
	 *
	 * @param name The name of the archive being passed.
	 * @param stream An InputStream from which the archive will be read.
	 *
	 * @return An array of IArchiveEntry objects describing the contents of
	 * the archive.
	 *
	 * @throws IOException
	 */
	public IArchiveEntry[] getContents( String name, InputStream stream )
        throws IOException
	{
        return getContents( name, stream, -1 );
    }


    // ----------------------------------------------------------
    /**
     * Gets the contents of the archive to be read from the specified
     * InputStream. A filename must be associated with the stream in order to
     * properly detect the type of archive being passed. No guarantees are
     * made as to whether an IArchiveEntry for a directory in the archive will
     * be returned before its children; this behavior is dependent on the
     * underlying archive handler.
     *
     * @param name   The name of the archive being passed.
     * @param stream An InputStream from which the archive will be read.
     * @param size   The size of the input stream, if known.
     *
     * @return An array of IArchiveEntry objects describing the contents of
     * the archive.
     *
     * @throws IOException
     */
    public IArchiveEntry[] getContents( String      name,
                                        InputStream stream,
                                        long        size )
        throws IOException
    {
		IArchiveHandler handler = findHandler( name );

		if ( handler != null )
		{
			return handler.getContents( stream );
		}
		else
		{
			return new IArchiveEntry[] {
				new ArchiveEntry( name, false, new Date(), size )
			};
		}
	}


    // ----------------------------------------------------------
	/**
	 * Unpacks an archive to the specified destination directory. The
	 * destination directory must already exist; any nested directories
	 * in the archive will be created as necessary.
	 *
	 * @param destPath A File object representing the directory to which the
	 * archive will be unpacked.
	 * @param archiveFile A File object representing the archive to be
	 * unpacked.
	 *
	 * @throws IOException
	 */
	public void unpack( File destPath, File archiveFile )
        throws IOException
	{
		IArchiveHandler handler = findHandler( archiveFile.getName() );

		if ( handler != null )
		{
			handler.unpack( destPath, archiveFile );
		}
		else
		{
			if ( archiveFile.isDirectory() )
			{
				FileUtilities.copyDirectoryContents( archiveFile, destPath );
			}
			else
			{
				File destFile = new File( destPath, archiveFile.getName() );
				FileUtilities.copyFileToFile( archiveFile, destFile );
			}
		}
	}


    // ----------------------------------------------------------
    /**
	 * Unpacks an archive to the specified destination directory. The
	 * destination directory must already exist; any nested directories
	 * in the archive will be created as necessary.
	 *
	 * @param destPath A File object representing the directory to which the
	 * archive will be unpacked.
	 * @param name The name of the archive being passed.
	 * @param stream An InputStream from which the archive will be read.
	 *
	 * @throws IOException
	 */
	public void unpack( File destPath, String name, InputStream stream )
        throws IOException
	{
		IArchiveHandler handler = findHandler( name );

		if ( handler != null )
		{
			handler.unpack( destPath, stream );
		}
		else
		{
			File destFile = new File( destPath, name );
			FileUtilities.copyStreamToFile( stream, destFile );
		}
	}


    // ----------------------------------------------------------
	/*
	 * Finds the first archive handler that will accept a file with the
	 * specified name. If no handler currently registered will accept it,
	 * this function returns null.
	 */
	private IArchiveHandler findHandler( String name )
	{
		for ( int i = 0; i < archiveHandlers.size(); i++ )
		{
			IArchiveHandler handler = (IArchiveHandler)archiveHandlers.get( i );
			if ( handler.acceptsFile( name ) )
            {
				return handler;
            }
		}
		return null;
	}


    // ----------------------------------------------------------
	/*
	 * Recursively drills down into a directory and populates a List with
	 * its IArchiveEntry objects representing its contents.
	 */
	private void getDirectoryContents( File root, File dir, List entryList )
	{
		File[] children = dir.listFiles();
		for ( int i = 0; i < children.length; i++ )
		{
			File file = children[i];

			String rootPath = root.getPath();
			String remPath = file.getPath().substring( rootPath.length() );
			if ( remPath.startsWith( File.separator ) )
            {
				remPath = remPath.substring( 1 );
            }

			entryList.add( new ArchiveEntry( remPath, file.isDirectory(),
					new Date( file.lastModified() ), file.length() ) );

			if ( file.isDirectory() )
            {
				getDirectoryContents( root, file, entryList );
            }
		}
	}


    //~ Instance/static variables .............................................

    /** The single instance of the ArchiveManager class. */
    private static ArchiveManager instance;

    /** A list of archive handlers currently registered in the manager. */
    private List archiveHandlers;
}