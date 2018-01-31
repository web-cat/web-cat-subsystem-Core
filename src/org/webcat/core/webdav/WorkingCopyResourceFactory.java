/*==========================================================================*\
 |  $Id: WorkingCopyResourceFactory.java,v 1.2 2012/06/22 16:23:18 aallowat Exp $
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
import org.eclipse.jgit.lib.Repository;
import org.webcat.core.EOBase;
import org.webcat.core.EntityRequestInfo;
import org.webcat.core.Session;
import org.webcat.core.git.GitRepository;
import org.webcat.core.git.GitUtilities;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.SecurityManager;
import com.ettrema.http.fs.LockManager;
import com.ettrema.http.fs.NullSecurityManager;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;

//-------------------------------------------------------------------------
/**
 * A Milton resource factory that generates directory- and file-based resources
 * for WebDAV access.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2012/06/22 16:23:18 $
 */
public class WorkingCopyResourceFactory implements ResourceFactory
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WorkingCopyResourceFactory()
    {
        lockManager = new FileBasedLockManager();
        securityManager = new NullSecurityManager();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public LockManager getLockManager()
    {
        return lockManager;
    }


    // ----------------------------------------------------------
    public SecurityManager securityManager()
    {
        return securityManager;
    }


    // ----------------------------------------------------------
    private WorkingCopyRequestInfo requestInfoForPath(String path,
            EOEditingContext ec)
    {
        WorkingCopyRequestInfo info = new WorkingCopyRequestInfo();

        if (path.toLowerCase().equals("personal") ||
                path.toLowerCase().startsWith("personal/"))
        {
            Session session = (Session) MiltonRequestWrapper.currentRequest()
                .context().session();

            info.shadowName = "Personal";
            info.object = session.primeUser();

            int firstSlash = path.indexOf('/');

            if (firstSlash == -1)
            {
                info.path = "";
            }
            else
            {
                info.path = path.substring(firstSlash);
            }

            return info;
        }
        else
        {
            int firstSpace = path.indexOf(' ');

            if (firstSpace != -1)
            {
                String entityName = path.substring(0, firstSpace);
                String repoId = path.substring(firstSpace + 1);

                int firstSlash = path.indexOf('/', firstSpace + 1);

                String resourcePath;

                if (firstSlash == -1)
                {
                    resourcePath = "";
                }
                else
                {
                    resourcePath = path.substring(firstSlash);
                    repoId = path.substring(firstSpace + 1, firstSlash);
                }

                EntityRequestInfo erInfo = new EntityRequestInfo(
                        entityName, repoId, resourcePath);

                info.shadowName = entityName + " " + repoId;
                info.object = erInfo.requestedObject(ec);
                info.path = resourcePath;

                if (info.object == null)
                {
                    return null;
                }
                else
                {
                    return info;
                }
            }
            else
            {
                return null;
            }
        }
    }


    // ----------------------------------------------------------
    public Resource getResource(String host, String url)
    {
        String searchString = "Web-CAT.woa/"
            + WebDAVRequestHandler.REQUEST_HANDLER_KEY;
        int index = url.indexOf(searchString);

        if (index != -1)
        {
            String requestPath = url.substring(index + searchString.length());

            if (requestPath.startsWith("/"))
            {
                requestPath = requestPath.substring(1);
            }

            if (requestPath.length() == 0)
            {
                return new DAVRootResource(this);
            }
            else
            {
                return resolvePath(requestPath);
            }
        }

        return null;
    }


    // ----------------------------------------------------------
    /*package*/ AbstractFSResource resolvePath(String path)
    {
        WORequest request = MiltonRequestWrapper.currentRequest();
        Session session = (Session) request.context().session();
        EOEditingContext ec = session.defaultEditingContext();

        WorkingCopyRequestInfo reqInfo = requestInfoForPath(
                path, ec);

        if (reqInfo != null)
        {
            GitRepository repository = GitRepository.repositoryForObject(
                    reqInfo.object);
            Repository workingCopy =
                GitUtilities.workingCopyForRepository(repository.repository(),
                        false);

            DAVPath davPath = new DAVPath(workingCopy, reqInfo.path);
            return resolveFile(davPath, workingCopy, reqInfo.shadowName);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /*package*/ AbstractFSResource resolveFile(DAVPath path,
            Repository workingCopy)
    {
        return resolveFile(path, workingCopy, null);
    }


    // ----------------------------------------------------------
    /*package*/ AbstractFSResource resolveFile(DAVPath path,
            Repository workingCopy, String shadowName)
    {
        File file = path.toFile();

        if (!file.exists())
        {
            return null;
        }
        else if (file.isDirectory())
        {
            return new DirectoryResource(this, path, workingCopy, shadowName);
        }
        else
        {
            return new FileResource(this, path, workingCopy);
        }
    }


    private class WorkingCopyRequestInfo
    {
        public EOBase object;
        public String path;
        public String shadowName;
    }


    //~ Static/instance variables .............................................

    private LockManager lockManager;
    private SecurityManager securityManager;
}
