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

package net.sf.webcat.archives.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import net.sf.webcat.archives.ArchiveEntry;
import net.sf.webcat.archives.IArchiveEntry;
import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

//-------------------------------------------------------------------------
/**
 * Contains the common functionality used by both the TarArchiveHandler and
 * TarGzArchiveHandler.
 * 
 * @author Tony Allowatt
 */
public class TarUtil
{
    // ----------------------------------------------------------
	public static IArchiveEntry[] getContents( InputStream stream )
		throws IOException
	{
		TarInputStream tarStream = new TarInputStream( stream );

		ArrayList entryList = new ArrayList();

		TarEntry tarEntry = tarStream.getNextEntry();
		while ( tarEntry != null )
		{
			ArchiveEntry entry = new ArchiveEntry(
					tarEntry.getName(), tarEntry.isDirectory(),
					tarEntry.getModTime(), tarEntry.getSize() );
			entryList.add( entry );

			tarEntry = tarStream.getNextEntry();
		}
		
		IArchiveEntry[] entryArray = new IArchiveEntry[entryList.size()];
		entryList.toArray( entryArray );
		return entryArray;
	}


    // ----------------------------------------------------------
	public static void unpack( File destPath, InputStream stream )
		throws IOException
	{
		TarInputStream tarStream = new TarInputStream( stream );

		TarEntry tarEntry = tarStream.getNextEntry();
		while ( tarEntry != null )
		{
			if ( tarEntry.isDirectory() )
			{
				File destDir = new File( destPath, tarEntry.getName() );
				
				if ( !destDir.exists() )
                {
					destDir.mkdir();
                }
			}
			else
			{
				File destFile = new File( destPath, tarEntry.getName() );
				File destParent = destFile.getParentFile();
				
				if ( destParent != null  &&  !destParent.exists() )
                {
					destParent.mkdir();
                }

				FileOutputStream destStream = new FileOutputStream( destFile );
				tarStream.copyEntryContents( destStream );
				destStream.flush();
				destStream.close();
			}

			tarEntry = tarStream.getNextEntry();
		}
	}
}
