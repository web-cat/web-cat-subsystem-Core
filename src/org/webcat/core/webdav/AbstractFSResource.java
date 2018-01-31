/*==========================================================================*\
 |  $Id: AbstractFSResource.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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
import java.util.Date;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Repository;
import org.webcat.core.Session;
import org.webcat.core.User;
import org.webcat.core.git.GitUtilities;
import org.webcat.core.git.http.RepositoryRequestUtils;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.LockedException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.PreConditionFailedException;
import com.webobjects.appserver.WORequest;

//-------------------------------------------------------------------------
/**
 * A base class for all DAV resources that directly represent a file or
 * directory on the file system.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public abstract class AbstractFSResource extends AbstractDAVResource
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public AbstractFSResource(WorkingCopyResourceFactory factory, DAVPath path,
            Repository workingCopy)
    {
        super(factory);

        this.path = path;
        this.workingCopy = workingCopy;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    protected abstract void doCopy(File dest);


    // ----------------------------------------------------------
    public DAVPath path()
    {
        return path;
    }


    // ----------------------------------------------------------
    public Repository workingCopy()
    {
        return workingCopy;
    }


    // ----------------------------------------------------------
    public Date getCreateDate()
    {
        return null;
    }


    // ----------------------------------------------------------
    public Date getModifiedDate()
    {
        return new Date(path.toFile().lastModified());
    }


    // ----------------------------------------------------------
    public String getName()
    {
        return path.name();
    }


    // ----------------------------------------------------------
    public String getUniqueId()
    {
        return (path.toFile().lastModified() + "_"
                + path.toFile().length()).hashCode() + "";
    }


    // ----------------------------------------------------------
    protected void pushWorkingCopy()
    {
        WORequest request = MiltonRequestWrapper.currentRequest();
        Session session = (Session) request.context().session();
        User user = session.primeUser();

        GitUtilities.pushWorkingCopy(workingCopy(),
                user, WEBDAV_COMMIT_MESSAGE);
    }


    // ----------------------------------------------------------
    public void moveTo(CollectionResource newParent, String newName)
        throws NotAuthorizedException
    {
        if (!canModify())
        {
            throw new NotAuthorizedException(this);
        }
        else if (newParent instanceof DirectoryResource)
        {
            DirectoryResource newDirParent = (DirectoryResource) newParent;

            if (!newDirParent.canModifyContents())
            {
                throw new NotAuthorizedException(this);
            }

            log.debug("copyTo: " + path.toFile() + ", " + newDirParent.path()
                    + ", " + newName);

            DAVPath destPath = new DAVPath(newDirParent.path(), newName);

            boolean ok = path.toFile().renameTo(destPath.toFile());

            if (!ok)
            {
                throw new RuntimeException("Failed to move to: "
                        + destPath);
            }

            path = destPath;

            pushWorkingCopy();
        }
        else
        {
            throw new RuntimeException("Destination is an unknown type. "
                    + "Must be a DirectoryResource, is a: "
                    + newParent.getClass());
        }
    }


    // ----------------------------------------------------------
    public void copyTo(CollectionResource newParent, String newName)
        throws NotAuthorizedException
    {
        if (newParent instanceof DirectoryResource)
        {
            DirectoryResource newFsParent = (DirectoryResource) newParent;

            if (!newFsParent.canModifyContents())
            {
                throw new NotAuthorizedException(this);
            }

            log.debug("copyTo: " + path.toFile() + ", " + newFsParent.path()
                    + ", " + newName);

            File dest = new File(newFsParent.path().toFile(), newName);
            doCopy(dest);

            pushWorkingCopy();
        }
        else
        {
            throw new RuntimeException("Destination is an unknown type. "
                    + "Must be a DirectoryResource, is a: "
                    + newParent.getClass());
        }
    }


    // ----------------------------------------------------------
    public void delete() throws NotAuthorizedException
    {
        if (!canModify())
        {
            throw new NotAuthorizedException(this);
        }

        log.debug("delete: " + path.toFile());

        boolean ok = path.toFile().delete();
        if (!ok)
        {
            throw new RuntimeException("Failed to delete");
        }

        pushWorkingCopy();
    }


    //~ Static/instance variables .............................................

    private DAVPath path;
    private Repository workingCopy;

    private static final String WEBDAV_COMMIT_MESSAGE = "Modified via Web-DAV.";

    private static final Logger log = Logger.getLogger(AbstractFSResource.class);
}
