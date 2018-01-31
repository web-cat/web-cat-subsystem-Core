/*==========================================================================*\
 |  $Id: WritableFolderContainer.java,v 1.1 2011/05/13 19:42:32 aallowat Exp $
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

package org.webcat.archives.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.webcat.archives.IWritableContainer;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:42:32 $
 */
public class WritableFolderContainer implements IWritableContainer
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WritableFolderContainer(File path)
    {
        this.path = path;

        if (!path.exists())
        {
            path.mkdirs();
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public IWritableContainer createContainer(String name)
    {
        return new WritableFolderContainer(new File(path, name));
    }


    // ----------------------------------------------------------
    public OutputStream createFile(String name, long sizeHint)
        throws IOException
    {
        return new FileOutputStream(new File(path, name));
    }


    // ----------------------------------------------------------
    public void finish()
    {
        // Do nothing.
    }


    //~ Static/instance variables .............................................

    private File path;
}
