/*==========================================================================*\
 |  $Id: AbstractArchiveHandler.java,v 1.2 2011/03/07 18:39:42 stedwar2 Exp $
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

package org.webcat.archives;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

//-------------------------------------------------------------------------
/**
 * Provides a simplified implementation of IArchiveHandler that handles the
 * File-based variants of the function by opening them as FileInputStreams and
 * passing control to the (abstract) InputStream functions.
 * <p>
 * Most clients can extend this class if they do not need different
 * implementations of their unpacking code depending on whether a File or
 * InputStream is the source.
 *
 * @author Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2011/03/07 18:39:42 $
 */
public abstract class AbstractArchiveHandler
    implements IArchiveHandler
{
    // ----------------------------------------------------------
	/**
	 * Returns true if this handler can unpack a file with the given name.
	 *
	 * @param name The name of the file to check for acceptance.
	 *
	 * @return true if this handler can unpack the file; otherwise, false.
	 */
	public abstract boolean acceptsFile(String name);


    // ----------------------------------------------------------
	/**
	 * Gets the contents of the archive pointed to by the specified File. No
	 * guarantees are made as to whether an IArchiveEntry for a directory in
	 * the archive will be returned before its children; this behavior is
	 * dependent on the underlying archive handler.
	 *
	 * @param archiveFile A File object representing the archive.
	 *
	 * @return An array of IArchiveEntry objects describing the contents of
	 * the archive.
	 *
	 * @throws IOException
	 */
	public IArchiveEntry[] getContents(File archiveFile)
        throws IOException
	{
		InputStream stream =
		    new BufferedInputStream( new FileInputStream(archiveFile));
		IArchiveEntry[] entryArray = getContents(stream);
		stream.close();

		return entryArray;
	}


    // ----------------------------------------------------------
	/**
	 * Gets the contents of the archive to be read from the specified
	 * InputStream. No guarantees are made as to whether an IArchiveEntry
	 * for a directory in the archive will be returned before its children;
	 * this behavior is dependent on the underlying archive handler.
	 *
	 * @param stream An InputStream from which the archive will be read.
	 *
	 * @return An array of IArchiveEntry objects describing the contents of
	 * the archive.
	 *
	 * @throws IOException
	 */
	public abstract IArchiveEntry[] getContents(InputStream stream)
        throws IOException;


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
	public void unpack(File destPath, File archiveFile)
        throws IOException
	{
		InputStream stream =
            new BufferedInputStream(new FileInputStream(archiveFile));
		unpack(destPath, stream);
		stream.close();
	}


    // ----------------------------------------------------------
	/**
	 * Unpacks an archive to the specified destination directory. The
	 * destination directory must already exist; any nested directories
	 * in the archive will be created as necessary.
	 *
	 * @param destPath A File object representing the directory to which the
	 * archive will be unpacked.
	 * @param stream An InputStream from which the archive will be read.
	 *
	 * @throws IOException
	 */
	public abstract void unpack(File destPath, InputStream stream)
        throws IOException;
}
