/*==========================================================================*\
 |  $Id: DAVPath.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

package org.webcat.core.webdav;

import java.io.File;
import java.lang.reflect.Method;
import org.eclipse.jgit.lib.Repository;
import org.webcat.core.Application;
import org.webcat.core.RepositoryProvider;
import com.webobjects.eocontrol.EOEnterpriseObject;

//-------------------------------------------------------------------------
/**
 * Encapsulates the path to an entity-based file on Web-CAT, which is
 * determined by the enterprise object that acts as a root and the path
 * relative to that object.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
/*package*/ class DAVPath
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public DAVPath(Repository workingCopy, String path)
    {
        if (path == null)
        {
            path = "";
        }

        this.workingCopy = workingCopy;
        this.path = normalizedPath(path);
    }


    // ----------------------------------------------------------
    public DAVPath(DAVPath start, String subPath)
    {
        this.workingCopy = start.workingCopy;
        this.path = normalizedPath(start.path + "/" + subPath);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public boolean isRoot()
    {
        return path.length() == 0;
    }


    // ----------------------------------------------------------
    public String path()
    {
        return path;
    }


    // ----------------------------------------------------------
    public String name()
    {
        int lastSlash = path.lastIndexOf('/');

        if (lastSlash == -1)
        {
            return path;
        }
        else
        {
            return path.substring(lastSlash + 1);
        }
    }


    // ----------------------------------------------------------
    public DAVPath parent()
    {
        if (isRoot())
        {
            return null;
        }
        else
        {
            int lastSlash = path.lastIndexOf('/');

            if (lastSlash == -1)
            {
                return new DAVPath(workingCopy, "");
            }
            else
            {
                String parentPath = path.substring(0, lastSlash);
                return new DAVPath(workingCopy, parentPath);
            }
        }
    }


    // ----------------------------------------------------------
    public File toFile()
    {
        if (cachedFile == null)
        {
            cachedFile = new File(workingCopy.getWorkTree(), path);
        }

        return cachedFile;
    }


    // ----------------------------------------------------------
    public String toString()
    {
        return workingCopy.getWorkTree().getParentFile().getName() + "/"
            + workingCopy.getWorkTree().getName() + "/" + path;
    }


    // ----------------------------------------------------------
    public int hashCode()
    {
        return workingCopy.hashCode() ^ path.hashCode();
    }


    // ----------------------------------------------------------
    public boolean equals(Object other)
    {
        if (other instanceof DAVPath)
        {
            DAVPath otherPath = (DAVPath) other;

            return workingCopy == otherPath.workingCopy
                && path.equalsIgnoreCase(otherPath.path);
        }
        else
        {
            return false;
        }
    }


    //~ Private methods .......................................................

    // ----------------------------------------------------------
    private static String normalizedPath(String path)
    {
        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }

        if (path.endsWith("/"))
        {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }


    //~ Static/instance variables .............................................

    private final Repository workingCopy;
    private final String path;

    private File cachedFile;
}
