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
import java.io.OutputStream;
import com.webobjects.foundation.NSData;

//-------------------------------------------------------------------------
/**
 * This interface provides write access to a file. If the
 * {@link WCFile#isWritable} method returns true on an instance of
 * {@link WCFile}, then it is safe to cast the reference to this interface to
 * access the file's data.
 *
 * @author  Tony Allevato
 * @version $Id$
 */
public interface IWritableFile
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Overwrites the file with the contents of the specified data object.
     *
     * @param data an NSData object with the new contents of the file
     * @throws IOException if an I/O exception occurred
     */
    void setData(NSData data) throws IOException;


    // ----------------------------------------------------------
    /**
     * Opens an output stream that can be used to write to the file.
     *
     * @return an OutputStream to the file
     * @throws IOException if an I/O exception occurred
     */
    OutputStream outputStream() throws IOException;
}
