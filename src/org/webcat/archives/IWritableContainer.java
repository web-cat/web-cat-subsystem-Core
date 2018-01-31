/*==========================================================================*\
 |  $Id: IWritableContainer.java,v 1.1 2011/05/13 19:42:32 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011 Virginia Tech
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

import java.io.IOException;
import java.io.OutputStream;

//-------------------------------------------------------------------------
/**
 * Writable containers provide a common interface for copying files into either
 * a directory on the file system or a zip archive.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:42:32 $
 */
public interface IWritableContainer
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Creates a new container with the specified name in this container and
     * returns a reference to it.
     *
     * @param name the name of the new container
     * @return a reference to the new container
     * @throws IOException if an I/O error occurs
     */
    public IWritableContainer createContainer(String name) throws IOException;


    // ----------------------------------------------------------
    /**
     * Creates a new file with the specified name in this container and returns
     * an {@code OutputStream} that can be used to populate it. It is always
     * safe to, and you must, close this stream when you have finished writing
     * the contents of the file to the stream.
     *
     * @param name the name of the file
     * @param sizeHint the size of the file. Not all types of containers use
     *     this.
     * @return an {@code OutputStream} that can be used to write to the file
     * @throws IOException if an I/O error occurs
     */
    public OutputStream createFile(String name, long sizeHint)
        throws IOException;


    // ----------------------------------------------------------
    /**
     * Finalize the container.
     *
     * @throws IOException is an I/O error occurs
     */
    public void finish() throws IOException;
}
