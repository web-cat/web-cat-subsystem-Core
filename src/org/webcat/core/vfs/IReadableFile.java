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
import com.webobjects.foundation.NSData;

//-------------------------------------------------------------------------
/**
 * This interface provides read access to the contents of a file. If the
 * {@link WCFile#isReadable} method returns true on an instance of
 * {@link WCFile}, then it is safe to cast the reference to this interface to
 * access the file's data.
 *
 * @author  Tony Allevato
 * @version $Id$
 */
public interface IReadableFile
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Reads the contents of the file and returns them in a data object.
     *
     * @return an NSData object with the contents of the file
     * @throws IOException if an I/O exception occurred
     */
    NSData data() throws IOException;


    // ----------------------------------------------------------
    /**
     * Opens an input stream that can be used to read the contents of the file.
     *
     * @return an InputStream to the file
     * @throws IOException if an I/O exception occurred
     */
    InputStream inputStream() throws IOException;
}
