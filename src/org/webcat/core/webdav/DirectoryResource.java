/*==========================================================================*\
 |  $Id: DirectoryResource.java,v 1.3 2011/10/25 12:59:33 stedwar2 Exp $
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.webcat.core.Application;
import org.webcat.core.FileUtilities;
import org.webcat.core.User;
import org.webcat.core.git.GitUtilities;
import org.webcat.core.git.http.RepositoryRequestUtils;
import org.webcat.woextensions.WCContext;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockingCollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * A DAV resources that represents a directory.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.3 $, $Date: 2011/10/25 12:59:33 $
 */
public class DirectoryResource
    extends AbstractFSResource
    implements MakeCollectionableResource, PutableResource, CopyableResource,
        DeletableResource, MoveableResource, PropFindableResource,
        LockingCollectionResource, GetableResource
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public DirectoryResource(WorkingCopyResourceFactory factory, DAVPath path,
            Repository workingCopy)
    {
        this(factory, path, workingCopy, null);
    }


    // ----------------------------------------------------------
    public DirectoryResource(WorkingCopyResourceFactory factory, DAVPath path,
            Repository workingCopy, String shadowName)
    {
        super(factory, path, workingCopy);

        this.shadowName = shadowName;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public boolean canModify()
    {
        return path().path().length() > 0;
    }


    // ----------------------------------------------------------
    public boolean canModifyContents()
    {
        return true;
    }


    // ----------------------------------------------------------
    public CollectionResource createCollection(String name)
        throws NotAuthorizedException
    {
        if (!canModifyContents())
        {
            throw new NotAuthorizedException(this);
        }

        log.debug("createCollection: " + path() + ", " + name);

        DAVPath fnew = new DAVPath(path(), name);
        boolean ok = fnew.toFile().mkdir();

        if (!ok)
        {
            throw new RuntimeException("Failed to create: "
                    + fnew);
        }

        return new DirectoryResource(factory(), fnew, workingCopy());
    }


    // ----------------------------------------------------------
    public Resource child(String name)
    {
        DAVPath fchild = new DAVPath(path(), name);
        return factory().resolveFile(fchild, workingCopy());
    }


    // ----------------------------------------------------------
    public List<? extends Resource> getChildren()
    {
        ArrayList<AbstractFSResource> list =
            new ArrayList<AbstractFSResource>();
        File[] files = path().toFile().listFiles();
        if (files != null)
        {
            for (File fchild : files)
            {
                DAVPath childPath = new DAVPath(path(), fchild.getName());
                AbstractFSResource res = factory().resolveFile(childPath,
                        workingCopy());

                if (res != null)
                {
                    list.add(res);
                }
                else
                {
                    log.error("Couldnt resolve file " + childPath);
                }
            }
        }
        return list;
    }


    // ----------------------------------------------------------
    /**
     * Will redirect if a default page has been specified on the factory
     *
     * @param request
     * @return a URL to redirect to, or null if no redirect should occur
     */
    public String checkRedirect(Request request)
    {
        return null;
    }


    // ----------------------------------------------------------
    public Resource createNew(String name, InputStream in, Long length,
            String contentType) throws IOException, NotAuthorizedException
    {
        if (!canModifyContents())
        {
            throw new NotAuthorizedException(this);
        }

        log.debug("createNew: " + path() + ", " + name);

        DAVPath destPath = new DAVPath(path(), name);
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(destPath.toFile());
            FileUtilities.copyStream(in, out);
        }
        finally
        {
            FileUtilities.closeQuietly(out);
        }

        pushWorkingCopy();

        return factory().resolveFile(destPath, workingCopy());

    }


    // ----------------------------------------------------------
    @Override
    protected void doCopy(File dest)
    {
        try
        {
            File newDir = new File(dest, path().name());
            newDir.mkdir();
            FileUtilities.copyDirectoryContents(path().toFile(), newDir);

            pushWorkingCopy();
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Failed to copy to:"
                    + dest.getAbsolutePath(), ex);
        }
    }


    // ----------------------------------------------------------
    public LockToken createAndLock(String name, LockTimeout timeout,
            LockInfo lockInfo) throws NotAuthorizedException
    {
        if (!canModifyContents())
        {
            throw new NotAuthorizedException(this);
        }

        log.debug("createAndLock: " + path() + ", " + name);

        DAVPath dest = new DAVPath(path(), name);
        createEmptyFile(dest);
        FileResource newRes = new FileResource(factory(), dest, workingCopy());
        LockResult res = newRes.lock(timeout, lockInfo);

        pushWorkingCopy();

        return res.getLockToken();
    }


    // ----------------------------------------------------------
    private void createEmptyFile(DAVPath path)
    {
        FileOutputStream fout = null;
        try
        {
            fout = new FileOutputStream(path.toFile());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            FileUtilities.closeQuietly(fout);
        }
    }


    // ----------------------------------------------------------
    @Override
    public String getName()
    {
        return (shadowName != null) ? shadowName : super.getName();
    }


    // ----------------------------------------------------------
    /**
     * Will generate a listing of the contents of this directory, unless the
     * factory's allowDirectoryBrowsing has been set to false.
     *
     * If so it will just output a message saying that access has been disabled.
     *
     * @param out
     * @param range
     * @param params
     * @param contentType
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public void sendContent(OutputStream out, Range range,
            Map<String, String> params, String contentType)
        throws IOException, NotAuthorizedException
    {
        boolean endsWithSlash =
            MiltonRequestWrapper.currentRequest().uri().endsWith("/");

        XmlWriter w = new XmlWriter(out);
        w.open("html");
        w.open("body");
        w.begin("h1").open().writeText(this.getName()).close();
        w.open("table");
        for (Resource r : getChildren())
        {
            String href = (!endsWithSlash ? this.getName() + "/" : "")
                + r.getName();

            if (r instanceof CollectionResource)
            {
                href += "/";
            }

            w.open("tr");

            w.open("td");
            w.begin("a").writeAtt("href", href).open()
                    .writeText(r.getName()).close();
            w.close("td");

            w.begin("td").open().writeText(r.getModifiedDate() + "").close();
            w.close("tr");
        }
        w.close("table");
        w.close("body");
        w.close("html");
        w.flush();

        /*WOContext context = Application.wcApplication().createContextForRequest(
                MiltonRequestWrapper.currentRequest());

        DirectoryListingPage page = new DirectoryListingPage(context);
        page.directory = this;

        WOResponse response = page.generateResponse();
        response.content().writeToStream(out);*/
    }


    // ----------------------------------------------------------
    public Long getMaxAgeSeconds(Auth auth)
    {
        return null;
    }


    // ----------------------------------------------------------
    public String getContentType(String accepts)
    {
        return "text/html";
    }


    // ----------------------------------------------------------
    public Long getContentLength()
    {
        return null;
    }


    //~ Static/instance variables .............................................

    private String shadowName;

    private static final Logger log = Logger.getLogger(DirectoryResource.class);
}
