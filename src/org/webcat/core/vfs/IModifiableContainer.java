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

//-------------------------------------------------------------------------
/**
 * This interface provides operations to modify a folder or folder-like
 * container.
 *
 * @author  Tony Allevato
 * @version $Id$
 */
public interface IModifiableContainer
{
    // ----------------------------------------------------------
    /**
     * Creates a folder with the specified name in this container, and returns
     * a WCFile object that represents the new folder.
     *
     * @param name the name of the new folder
     * @return a WCFile that represents the new folder
     */
    WCFile createFolderWithName(String name);


    // ----------------------------------------------------------
    /**
     * Creates an empty file with the specified name in this container, and
     * returns a WCFile object that represents the new file. The file should be
     * created such that it is writable (see {@link WCFile#isWritable()}), so
     * calling code can cast it to {@link IWritableFile} in order to provide
     * the new file with data.
     *
     * @param name the name of the new file
     * @return a WCFile that represents the new file
     */
    WCFile createFileWithName(String name);
}
