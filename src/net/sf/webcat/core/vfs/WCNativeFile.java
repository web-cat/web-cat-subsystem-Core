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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.sf.webcat.core.FileUtilities;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * A file-like object that represents a concrete file on the file system. This
 * extension of {@link WCNativeFileOrFolder} is not meant to be used by client
 * code (it has package visibility), but exists to implement the readable and
 * writable data interfaces so that the contents of the files can be accessed.
 *
 * @author  Tony Allevato
 * @version $Id$
 */
/*package*/ class WCNativeFile extends WCNativeFileOrFolder
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new instance of the WCNativeFile object.
     *
     * @param parent the parent file
     * @param fileObj the Java File object
     */
    public WCNativeFile(WCFile parent, java.io.File fileObj)
    {
        super(parent, fileObj);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public <T> T adaptTo(Class<? extends T> adapterType)
    {
        if ((adapterType.isAssignableFrom(IReadableFile.class) &&
                javaFile().canRead()) ||
            (adapterType.isAssignableFrom(IWritableFile.class) &&
                javaFile().canWrite()) ||
            (adapterType.isAssignableFrom(IReadWritableFile.class) &&
                javaFile().canRead() && javaFile().canWrite()))
        {
            if (readWriteAdapter == null)
            {
                readWriteAdapter = new JavaFileReadWriteAdapter();
            }

            return adapterType.cast(readWriteAdapter);
        }

        return super.adaptTo(adapterType);
    }


    //~ Private classes .......................................................

    // ----------------------------------------------------------
    private class JavaFileReadWriteAdapter extends ReadWritableFileAdapter
    {
        // ----------------------------------------------------------
        public InputStream inputStream() throws IOException
        {
            return new FileInputStream(javaFile());
        }


        // ----------------------------------------------------------
        public OutputStream outputStream() throws IOException
        {
            return new FileOutputStream(javaFile());
        }
    }


    //~ Static/instance variables .............................................

    private JavaFileReadWriteAdapter readWriteAdapter;
}
