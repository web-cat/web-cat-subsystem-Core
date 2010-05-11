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

package org.webcat.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.log4j.Logger;
import org.webcat.core.vfs.IReadWritableFile;
import org.webcat.core.vfs.IReadableFile;
import org.webcat.core.vfs.IWritableFile;
import org.webcat.core.vfs.WCFile;
import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.ComponentIDGenerator;
import org.webcat.core.FileViewerDialog;
import org.webcat.core.WCComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import er.extensions.foundation.ERXStringUtilities;

public class FileViewerDialog extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public FileViewerDialog(WOContext context)
    {
        super(context);
    }


    //~ External KVC attributes (must be public) ..............................

    public WCFile file;
    public boolean isEditor;


    //~ Internal KVC attributes (must be public) ..............................

    public String fileContents;
    public ComponentIDGenerator idFor;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse(WOResponse response, WOContext context)
    {
        idFor = new ComponentIDGenerator(this);

        if (file != null)
        {
            IReadableFile readable = file.adaptTo(IReadableFile.class);
            if (readable != null)
            {
                InputStream is = null;

                try
                {
                    is = readable.inputStream();
                    fileContents = ERXStringUtilities.stringFromInputStream(is,
                            "UTF-8");
                }
                catch (IOException e)
                {
                    fileContents = null;
                    log.error("Error reading contents of file " +
                            file.toString(), e);
                }
                finally
                {
                    if (is != null)
                    {
                        try
                        {
                            is.close();
                        }
                        catch (IOException e)
                        {
                            // Do nothing.
                        }
                    }
                }
            }
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public JavascriptGenerator saveAndClose()
    {
        IWritableFile writable = file.adaptTo(IWritableFile.class);

        if (writable != null)
        {
            OutputStream os = null;

            try
            {
                byte[] bytes = fileContents.getBytes("UTF-8");

                os = writable.outputStream();
                os.write(bytes);
            }
            catch (IOException e)
            {
                log.error("Error writing contents of file " + file.toString(),
                        e);
            }
            finally
            {
                if (os != null)
                {
                    try
                    {
                        os.close();
                    }
                    catch (IOException e)
                    {
                        // Do nothing.
                    }
                }
            }
        }

        return null;
    }


    //~ Static/instance variables .............................................

    private static final Logger log = Logger.getLogger(FileViewerDialog.class);
}
