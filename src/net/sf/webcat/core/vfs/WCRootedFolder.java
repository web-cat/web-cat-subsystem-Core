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

package net.sf.webcat.core.vfs;

import java.io.File;

//-------------------------------------------------------------------------
/**
 * A virtual file system object that lets any folder on the native file system
 * act as the root of a virtual file system.
 *
 * @author  Tony Allevato
 * @version $Id$
 */
public class WCRootedFolder extends WCNativeFileOrFolder
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a virtual file system folder rooted at the specified native
     * file system location.
     *
     * @param fileObj the Java File object representing the root of the virtual
     *     file system
     */
    public WCRootedFolder(File fileObj)
    {
        super(null, fileObj);
    }


    // ----------------------------------------------------------
    /**
     * Creates a virtual file system folder rooted at the specified native
     * file system location.
     *
     * @param path the path to a folder representing the root of the virtual
     *     file system
     */
    public WCRootedFolder(String path)
    {
        super(null, new java.io.File(path));
    }
}
