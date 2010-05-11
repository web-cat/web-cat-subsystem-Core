/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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
import org.webcat.archives.IArchiveEntry;
import org.webcat.core.FileUtilities;
import com.webobjects.foundation.NSTimestamp;

/*package*/ class WCArchiveEntry extends WCFile
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCArchiveEntry(WCArchiveFile parent, IArchiveEntry archiveEntry)
    {
        super(parent);

        parentArchive = parent;
        this.archiveEntry = archiveEntry;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public boolean equals(WCFile otherFile_)
    {
        if (otherFile_ instanceof WCArchiveEntry)
        {
            WCArchiveEntry otherFile = (WCArchiveEntry) otherFile_;

            return parent().equals(otherFile.parent()) &&
                archiveEntry.getName().equals(otherFile.archiveEntry.getName());
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    @Override
    public String iconURL()
    {
        return FileUtilities.iconURL(name());
    }


    // ----------------------------------------------------------
    @Override
    public NSTimestamp lastModified()
    {
        return new NSTimestamp(archiveEntry.lastModified());
    }


    // ----------------------------------------------------------
    @Override
    public boolean isPhysicalFile()
    {
        return false;
    }


    // ----------------------------------------------------------
    @Override
    public java.io.File externalizeFile()
    {
        // TODO support file externalization
        return null;
    }


    // ----------------------------------------------------------
    @Override
    public long length()
    {
        return archiveEntry.length();
    }


    // ----------------------------------------------------------
    @Override
    public String name()
    {
        String fullName = archiveEntry.getName();
        int index = fullName.lastIndexOf('/');

        if (index >= 0)
        {
            return fullName.substring(index + 1);
        }
        else
        {
            return fullName;
        }
    }


    // ----------------------------------------------------------
    @Override
    public boolean isContainer()
    {
        return archiveEntry.isDirectory();
    }


    // ----------------------------------------------------------
    @Override
    protected void visitChildren(IChildVisitor visitor)
    {
        // TODO Iterate over the children if it is a directory, otherwise
        // do nothing.
    }


    // ----------------------------------------------------------
    @Override
    public <T> T adaptTo(Class<? extends T> adapterType)
    {
        if (adapterType.isAssignableFrom(IReadableFile.class))
        {
            if (!archiveEntry.isDirectory())
            {
                if (readAdapter == null)
                {
                    readAdapter = new ArchiveEntryReadAdapter();
                }

                return adapterType.cast(readAdapter);
            }
        }

        return super.adaptTo(adapterType);
    }


    //~ Private classes .......................................................

    // ----------------------------------------------------------
    private class ArchiveEntryReadAdapter extends ReadWritableFileAdapter
    {
        // ----------------------------------------------------------
        public InputStream inputStream() throws IOException
        {
            // TODO return an input stream for reading the data from this entry
            return null;
        }


        // ----------------------------------------------------------
        public OutputStream outputStream() throws IOException
        {
            return null;
        }
    }


    //~ Static/instance variables .............................................

    private WCArchiveFile parentArchive;
    private IArchiveEntry archiveEntry;
    private ArchiveEntryReadAdapter readAdapter;
}
