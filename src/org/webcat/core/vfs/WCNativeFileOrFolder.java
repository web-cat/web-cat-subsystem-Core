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
import org.webcat.core.FileUtilities;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSComparator.ComparisonException;

//-------------------------------------------------------------------------
/**
 * A file-like object that wraps a <tt>java.io.File</tt>. This class has
 * package visibility because it is not intended to be used directly by client
 * code. All operations on folders (and archives, when treated as containers)
 * can be performed on the {@link WCFile} reference directly, and performing
 * operations on the file or folder can be done by adapting the object to the
 * appropriate interfaces.
 *
 * @author  Tony Allevato
 * @version $Id$
 */
/*package*/ class WCNativeFileOrFolder extends WCFile
    implements IModifiableContainer, IDeletableFile
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the WCNativeFileOrFolder class.
     *
     * @param parent the parent of this file
     * @param fileObj the Java File object representing this file
     */
    public WCNativeFileOrFolder(WCFile parent, java.io.File fileObj)
    {
        super(parent);

        this.javaFile = fileObj;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public boolean equals(WCFile otherFile_)
    {
        if (otherFile_ instanceof WCNativeFileOrFolder)
        {
            WCNativeFileOrFolder otherFile = (WCNativeFileOrFolder) otherFile_;
            return javaFile.equals(otherFile.javaFile);
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    @Override
    protected void visitChildren(IChildVisitor visitor)
    {
        java.io.File[] childFiles = javaFile.listFiles();

        if (childFiles != null)
        {
            for (java.io.File childFile : childFiles)
            {
                WCFile f = childFile.isDirectory() ?
                        new WCNativeFileOrFolder(this, childFile) :
                        new WCNativeFile(this, childFile);

                visitor.visit(f);
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the Java File object representing this file.
     *
     * @return a {@link java.io.File} object
     */
    protected java.io.File javaFile()
    {
        return javaFile;
    }


    // ----------------------------------------------------------
    @Override
    public boolean isContainer()
    {
        return javaFile.isDirectory();
    }


    // ----------------------------------------------------------
    @Override
    public boolean isPhysicalFile()
    {
        return true;
    }


    // ----------------------------------------------------------
    @Override
    public java.io.File externalizeFile()
    {
        return javaFile();
    }


    // ----------------------------------------------------------
    @Override
    public NSTimestamp lastModified()
    {
        return new NSTimestamp(javaFile.lastModified());
    }


    // ----------------------------------------------------------
    @Override
    public long length()
    {
        return javaFile.isFile() ? javaFile.length() : LENGTH_NOT_APPLICABLE;
    }


    // ----------------------------------------------------------
    @Override
    public String name()
    {
        return javaFile.getName();
    }


    // ----------------------------------------------------------
    @Override
    public String iconURL()
    {
        if (javaFile.isDirectory())
        {
            return "icons/dir.gif";
        }
        else
        {
            return FileUtilities.iconURL(name());
        }
    }


    // ----------------------------------------------------------
    @Override
    public <T> T adaptTo(Class<? extends T> adapterType)
    {
        if (adapterType.isAssignableFrom(IModifiableContainer.class))
        {
            if (javaFile.isDirectory())
            {
                return adapterType.cast(this);
            }
        }
        else if (adapterType.isAssignableFrom(IDeletableFile.class))
        {
            return adapterType.cast(this);
        }

        return super.adaptTo(adapterType);
    }


    // ----------------------------------------------------------
    public WCFile createFileWithName(String name)
    {
        java.io.File newFile = new java.io.File(javaFile, name);

        try
        {
            if (newFile.createNewFile())
            {
                return WCFileFactory.instance().wcFileForJavaFile(
                        this, newFile);
            }
            else
            {
                return null;
            }
        }
        catch (IOException e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public WCFile createFolderWithName(String name)
    {
        java.io.File newFolder = new java.io.File(javaFile, name);
        if (newFolder.mkdir())
        {
            return WCFileFactory.instance().wcFileForJavaFile(this, newFolder);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public void delete() throws IOException
    {
        if (javaFile.isDirectory())
        {
            FileUtilities.deleteDirectory(javaFile);
        }
        else
        {
            javaFile.delete();
        }
    }


    //~ Static/instance methods ...............................................

    private java.io.File javaFile;
}
