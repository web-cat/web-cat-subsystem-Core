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

package net.sf.webcat.archives;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

//-------------------------------------------------------------------------
/**
 * The interface that must be implemented by all archive handlers in the
 * archive manager.  It is recommended that clients instead extend the
 * AbstractArchiveHandler class, which requires only overriding the
 * InputStream variants of the functions.
 * <p>
 * However, if you need to handle Files differently than InputStreams, you
 * can implement this interface in full.
 * 
 * @author Tony Allowatt
 */
public interface IArchiveHandler
{
    // ----------------------------------------------------------
	/**
	 * Returns true if this handler can unpack a file with the given name.
	 * 
	 * @param name The name of the file to check for acceptance.
	 * 
	 * @return true if this handler can unpack the file; otherwise, false.
	 */
	boolean acceptsFile(String name);


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
	IArchiveEntry[] getContents(File archiveFile) throws IOException;


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
	IArchiveEntry[] getContents(InputStream stream) throws IOException;
	

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
	void unpack(File destPath, File archiveFile) throws IOException;


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
	void unpack(File destPath, InputStream stream) throws IOException;
}