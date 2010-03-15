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

import net.sf.webcat.core.FileUtilities;

//-------------------------------------------------------------------------
/**
 * An internal factory class used to create appropriate subclass instances of
 * {@link WCFile} depending on the type of file being operated on.
 *
 * @author  Tony Allevato
 * @version $Id$
 */
/*package*/ class WCFileFactory
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Prevent instantiation.
     */
    private WCFileFactory()
    {
        // Do nothing.
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the single instance of the WCFileFactory class.
     *
     * @return the single instance of the WCFileFactory class
     */
    public static WCFileFactory instance()
    {
        if (instance == null)
        {
            instance = new WCFileFactory();
        }

        return instance;
    }


    // ----------------------------------------------------------
    /**
     * Creates a {@link WCFile} subclass instance for a specific native Java
     * File object.
     *
     * @param parent the parent {@link WCFile} instance
     * @param file the Java File object
     * @return a {@link WCFile} subclass instance representing the file
     */
    public WCFile wcFileForJavaFile(WCFile parent, java.io.File file)
    {
        if (file.isDirectory())
        {
            return new WCNativeFileOrFolder(parent, file);
        }
        else
        {
            // TODO add support for archives
/*            if (FileUtilities.isArchiveFile(file))
            {
                return new WCArchiveFile(parent, file);
            }
            else*/
            {
                return new WCNativeFile(parent, file);
            }
        }
    }


    //~ Static/instance variables .............................................

    private static WCFileFactory instance;
}
