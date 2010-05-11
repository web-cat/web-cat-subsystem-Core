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

package org.webcat.core.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.webobjects.foundation.NSData;

//-------------------------------------------------------------------------
/**
 * This abstract partial implementation of {@link IReadWritableFile} simplifies
 * the use of IRead/Writable adapters by expressing {@link IReadableFile#data()}
 * and {@link IWritableFile#setData(NSData)} in terms of the
 * {@link IReadableFile#inputStream()} and {@link IWritableFile#outputStream()}
 * methods, respectively.
 *
 * @author  Tony Allevato
 * @version $Id$
 */
/*package*/ abstract class ReadWritableFileAdapter implements IReadWritableFile
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public NSData data() throws IOException
    {
        InputStream is = null;

        try
        {
            is = inputStream();

            if (is != null)
            {
                return new NSData(is, 4096);
            }
            else
            {
                return null;
            }
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }


    // ----------------------------------------------------------
    public void setData(NSData data) throws IOException
    {
        OutputStream os = null;

        try
        {
            os = outputStream();

            if (os != null)
            {
                data.writeToStream(os);
            }
        }
        finally
        {
            if (os != null)
            {
                os.close();
            }
        }
    }
}
