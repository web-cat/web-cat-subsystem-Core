/*==========================================================================*\
 |  $Id: ZipArchiveHandler.java,v 1.3 2013/10/09 00:02:22 stedwar2 Exp $
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.webcat.archives.*;
import org.webcat.core.FileUtilities;

//-------------------------------------------------------------------------
/**
 * An archive handler that unpacks ZIP and JAR archives.
 *
 * @author Tony Allowatt
 */
public class ZipArchiveHandler
    extends AbstractArchiveHandler
{
    // ----------------------------------------------------------
	public boolean acceptsFile( String name )
	{
		return ( name.toLowerCase().endsWith( ".zip" ) ||
				 name.toLowerCase().endsWith( ".jar" ) );
	}


    // ----------------------------------------------------------
	public IArchiveEntry[] getContents( InputStream stream )
        throws IOException
	{
		ZipInputStream zipStream = new ZipInputStream( stream );

		ArrayList<IArchiveEntry> entryList = new ArrayList<IArchiveEntry>();

		ZipEntry zipEntry = zipStream.getNextEntry();
		while ( zipEntry != null )
		{
            zipStream.closeEntry();
            String name = zipEntry.getName();
            if (name != null
                && !zipEntry.isDirectory()
                && !name.equals(".DS_Store")
                && !name.endsWith("/.DS_Store")
                && !name.startsWith("__MACOSX/"))
            {
                ArchiveEntry entry = new ArchiveEntry(
					name,
					zipEntry.isDirectory(),
					new Date( zipEntry.getTime() ),
					zipEntry.getSize() );
                entryList.add( entry );
            }
			zipEntry = zipStream.getNextEntry();
		}

		IArchiveEntry[] entryArray = new IArchiveEntry[entryList.size()];
		entryList.toArray( entryArray );

		return entryArray;
	}


    // ----------------------------------------------------------
	public void unpack( File destPath, InputStream stream )
	    throws IOException
	{
		ZipInputStream zipStream = new ZipInputStream( stream );

		ZipEntry zipEntry = zipStream.getNextEntry();
		while ( zipEntry != null )
		{
	        String name = zipEntry.getName();
			if ( zipEntry.isDirectory() )
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

				FileUtilities.copyStreamToFile(
                    zipStream, destFile, zipEntry.getTime() );
			}

			zipStream.closeEntry();
			zipEntry = zipStream.getNextEntry();
		}
	}
}
