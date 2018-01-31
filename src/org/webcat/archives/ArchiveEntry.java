/*==========================================================================*\
 |  $Id: ArchiveEntry.java,v 1.2 2011/03/07 18:39:42 stedwar2 Exp $
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

import java.util.Date;

//-------------------------------------------------------------------------
/**
 * Provides a concrete implementation of the IArchiveEntry interface.
 *
 * @author Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2011/03/07 18:39:42 $
 */
public class ArchiveEntry
    implements IArchiveEntry
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
	/**
	 * Constructs an ArchiveEntry object with the specified parameters.
	 *
	 * @param name The name (relative path) of the archive entry.
	 * @param directory true if the entry is a directory; otherwise, false.
	 * @param lastMod A Date object representing the last modified time of the
	 * archive entry.
	 * @param entryLength The uncompressed file size of the entry.
	 */
	public ArchiveEntry(String  name,
                        boolean directory,
                        Date    lastMod,
                        long    entryLength)
	{
		this.name = name;
		this.directory = directory;
		this.lastMod = lastMod;
		this.entryLength = entryLength;
	}


    //~ Methods ...............................................................

    // ----------------------------------------------------------
	/**
	 * Gets the name of this entry. If the entry is nested, this name will
	 * be a path relative to the root of the archive, such as "dir/file.txt".
	 *
	 * @return A String containing the name of the entry.
	 */
	public String getName()
	{
		return name;
	}


    // ----------------------------------------------------------
	/**
	 * Returns a value indicating if this archive entry is a directory.
	 *
	 * @return true if this entry is a directory; otherwise, false.
	 */
	public boolean isDirectory()
	{
		return directory;
	}


    // ----------------------------------------------------------
	/**
	 * Returns the time at which the file represented by this entry was last
	 * modified.
	 *
	 * @return A Date object containing the last-modified time of the entry.
	 */
	public Date lastModified()
	{
		return lastMod;
	}


    // ----------------------------------------------------------
	/**
	 * Returns the length of the file represented by this entry.
	 *
	 * @return The length of the entry.
	 */
	public long length()
	{
		return entryLength;
	}


    //~ Instance/static variables .............................................

    private String  name;
    private boolean directory;
    private Date    lastMod;
    private long    entryLength;
}
