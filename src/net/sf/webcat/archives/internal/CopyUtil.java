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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//-------------------------------------------------------------------------
/**
 * Contains some common file copying operations used by the various archive
 * handlers.
 * 
 * @author Tony Allowatt
 */
public class CopyUtil
{
    // ----------------------------------------------------------
	/**
	 * Copies the source file to the destination file.
	 * 
	 * @param destFile A File object representing the path (and name) of the
	 * destination file.
	 * @param srcFile A File object representing the path of the source file.
	 * 
	 * @throws IOException
	 */
	public static void copyFile( File destFile, File srcFile )
		throws IOException
	{
		FileInputStream stream = new FileInputStream( srcFile );
		CopyUtil.copyStream( destFile, stream );
		stream.close();
	}


    // ----------------------------------------------------------
	/**
	 * Copies data from the specified input stream to a file.
	 * 
	 * @param destFile A File object representing the path (and name) of the
	 * destination file.
	 * @param stream An InputStream containing the data to be copied.
	 * 
	 * @throws IOException
	 */
	public static void copyStream( File destFile, InputStream stream )
		throws IOException
	{
		final int BUFFER_SIZE = 2048;
		
		int count;
		byte data[] = new byte[BUFFER_SIZE];

		OutputStream outStream = new BufferedOutputStream(
				new FileOutputStream( destFile ), BUFFER_SIZE );
		
		while ( ( count = stream.read( data, 0, BUFFER_SIZE ) ) != -1 )
		{
			outStream.write( data, 0, count );
		}

		outStream.flush();
		outStream.close();
	}
	

    // ----------------------------------------------------------
	/**
	 * Recursively copies the contents of the specified source directory into
	 * the destination directory.
	 * 
	 * @param destPath A File object representing the destination directory
	 * into which the files will be copied.
	 * @param srcDir A File object representing the directory containing the
	 * files to be copied.
	 * 
	 * @throws IOException
	 */
	public static void recursiveCopy( File destPath, File srcDir )
		throws IOException
	{
		File[] children = srcDir.listFiles();
		
		for ( int i = 0; i < children.length; i++ )
		{
			File file = children[i];
			File destFile = new File( destPath, file.getName() );
			
			if ( file.isDirectory() )
			{
				destFile.mkdir();
				recursiveCopy( destFile, file );
			}
			else
			{
				copyFile( destFile, file );
			}
		}
	}
}
