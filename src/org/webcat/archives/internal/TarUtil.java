/*==========================================================================*\
 |  $Id: TarUtil.java,v 1.2 2013/09/16 13:48:46 stedwar2 Exp $
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

package org.webcat.archives.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.webcat.archives.ArchiveEntry;
import org.webcat.archives.IArchiveEntry;
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

		ArrayList<IArchiveEntry> entryList = new ArrayList<IArchiveEntry>();

		TarEntry tarEntry = tarStream.getNextEntry();
		while ( tarEntry != null )
		{
		    String name = tarEntry.getName();
            if (name != null
                && !tarEntry.isDirectory()
                && !name.equals(".DS_Store")
                && !name.endsWith("/.DS_Store")
                && !name.startsWith("__MACOSX/"))
            {
                ArchiveEntry entry = new ArchiveEntry(
					tarEntry.getName(), tarEntry.isDirectory(),
					tarEntry.getModTime(), tarEntry.getSize() );
                entryList.add( entry );
            }
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
            String name = tarEntry.getName();
			if ( tarEntry.isDirectory() )
			{
                if (!"__MACOSX".equals(name)
                    && !name.startsWith("__MACOSX/"))
                {
                    File destDir = new File( destPath, name );

                    if ( !destDir.exists() )
                    {
                        destDir.mkdirs();
                    }
                }
			}
            else if (name != null
                     && !(name.equals(".DS_Store")
                          || name.startsWith("__MACOSX/")
                          || name.endsWith("/.DS_Store")))
			{
				File destFile = new File( destPath, name );
				File destParent = destFile.getParentFile();

				if ( destParent != null  &&  !destParent.exists() )
                {
					destParent.mkdirs();
                }

				FileOutputStream destStream = new FileOutputStream( destFile );
				tarStream.copyEntryContents( destStream );
				destStream.flush();
				destStream.close();
                destFile.setLastModified( tarEntry.getModTime().getTime() );
			}

			tarEntry = tarStream.getNextEntry();
		}
	}
}
