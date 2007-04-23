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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.sf.webcat.archives.*;
import net.sf.webcat.archives.AbstractArchiveHandler;
import net.sf.webcat.archives.ArchiveEntry;
import net.sf.webcat.archives.IArchiveEntry;

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

		ArrayList entryList = new ArrayList();

		ZipEntry zipEntry = zipStream.getNextEntry();
		while ( zipEntry != null )
		{
            zipStream.closeEntry();
			ArchiveEntry entry = new ArchiveEntry(
					zipEntry.getName(), zipEntry.isDirectory(),
					new Date( zipEntry.getTime() ), zipEntry.getSize() );
			entryList.add( entry );
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
			if ( zipEntry.isDirectory() )
			{
				File destDir = new File( destPath, zipEntry.getName() );
				
				if ( !destDir.exists() )
                {
					destDir.mkdirs();
                }
			}
			else
			{
				File destFile = new File( destPath, zipEntry.getName() );
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
