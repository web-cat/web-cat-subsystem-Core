/*==========================================================================*\
 |  $Id: FileResource.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Repository;
import org.webcat.core.FileUtilities;
import com.bradmcevoy.common.ContentTypeUtils;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropPatchableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;

//-------------------------------------------------------------------------
/**
 * A DAV resource that represents a file.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class FileResource
    extends AbstractFSResource
    implements CopyableResource, DeletableResource, GetableResource,
        MoveableResource, PropFindableResource, PropPatchableResource
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public FileResource(WorkingCopyResourceFactory factory, DAVPath path,
            Repository workingCopy)
    {
        super(factory, path, workingCopy);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public boolean canModify()
    {
        return true;
    }


    // ----------------------------------------------------------
    public boolean canModifyContents()
    {
        return true;
    }


    // ----------------------------------------------------------
    public Long getContentLength()
    {
        return path().toFile().length();
    }


    // ----------------------------------------------------------
    public String getContentType(String preferredList)
    {
        String mimeType = ContentTypeUtils.findContentTypes(path().toFile());
        String acceptableType = ContentTypeUtils.findAcceptableContentType(
                mimeType, preferredList);

        return acceptableType;
    }


    // ----------------------------------------------------------
    public String checkRedirect(Request request)
    {
        return null;
    }


    // ----------------------------------------------------------
    public void sendContent(OutputStream out, Range range,
            Map<String, String> params, String contentType) throws IOException
    {
        FileInputStream in = null;
        try
        {
            in = new FileInputStream(path().toFile());
            FileUtilities.copyStream(in, out);
            out.flush();
        }
        finally
        {
            FileUtilities.closeQuietly(in);
        }
    }


    // ----------------------------------------------------------
    public Long getMaxAgeSeconds(Auth auth)
    {
        //return factory().maxAgeSeconds( this );
        //FIXME
        return 60L;
    }


    // ----------------------------------------------------------
    @Override
    protected void doCopy(File dest)
    {
        try
        {
            FileUtilities.copyFileToDir(path().toFile(), dest);
        }
        catch(IOException e)
        {
            throw new RuntimeException("Failed doing copy to: "
                    + dest.getAbsolutePath(), e);
        }
    }


    // ----------------------------------------------------------
    @Deprecated
    public void setProperties(Fields fields)
    {
        // MIL-50
        // not implemented. Just to keep MS Office sweet
    }


    //~ Static/instance variables .............................................

    static final Logger log = Logger.getLogger(FileResource.class);
}
