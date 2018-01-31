/*==========================================================================*\
 |  $Id: TarGzArchiveHandler.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
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
import java.util.zip.GZIPInputStream;
import org.webcat.archives.AbstractArchiveHandler;
import org.webcat.archives.IArchiveEntry;


//-------------------------------------------------------------------------
/**
 * An archive handler that unpacks GZIP-compressed Unix TAR archives.
 *
 * @author Tony Allowatt
 */
public class TarGzArchiveHandler extends AbstractArchiveHandler
{
    // ----------------------------------------------------------
	public boolean acceptsFile( String name )
	{
		return ( name.toLowerCase().endsWith( ".tar.gz" ) ||
				 name.toLowerCase().endsWith( ".tgz" ) );
	}


    // ----------------------------------------------------------
	public IArchiveEntry[] getContents( InputStream stream )
        throws IOException
	{
		GZIPInputStream gzipStream = new GZIPInputStream( stream );
		return TarUtil.getContents( gzipStream );
	}


    // ----------------------------------------------------------
	public void unpack( File destPath, InputStream stream )
	    throws IOException
	{
		GZIPInputStream gzipStream = new GZIPInputStream( stream );
		TarUtil.unpack( destPath, gzipStream );
	}
}
