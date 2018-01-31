/*==========================================================================*\
 |  $Id: DAVRootResource.java,v 1.3 2012/06/22 16:23:18 aallowat Exp $
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.webcat.core.EOBase;
import org.webcat.core.RepositoryManager;
import org.webcat.core.RepositoryProvider;
import org.webcat.core.Session;
import org.webcat.core.User;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
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
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

//-------------------------------------------------------------------------
/**
 * A virtual root for WebDAV access that contains collections for all of the
 * file repositories that a user can access.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.3 $, $Date: 2012/06/22 16:23:18 $
 */
public class DAVRootResource extends AbstractDAVResource
    implements MakeCollectionableResource, PutableResource, CopyableResource,
    DeletableResource, MoveableResource, PropFindableResource,
    LockingCollectionResource, GetableResource
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public DAVRootResource(WorkingCopyResourceFactory factory)
    {
        super(factory);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public boolean canModify()
    {
        return false;
    }


    // ----------------------------------------------------------
    public boolean canModifyContents()
    {
        return false;
    }


    // ----------------------------------------------------------
    public Resource child(String name)
    {
        return factory().resolvePath(name);
    }


    // ----------------------------------------------------------
    public List<? extends Resource> getChildren()
    {
        List<Resource> roots = new ArrayList<Resource>();

        WORequest request = MiltonRequestWrapper.currentRequest();
        Session session = (Session) request.context().session();

        EOEditingContext ec = session.defaultEditingContext();
        User user = session.primeUser();

        NSArray<? extends EOBase> objects =
            RepositoryManager.getInstance().repositoriesPresentedToUser(
                    user, ec);

        for (EOBase object : objects)
        {
            String path = object.entityName() + " "
                + object.apiId();

            roots.add(factory().resolvePath(path));
        }

        return roots;
    }


    // ----------------------------------------------------------
    public String checkRedirect(Request request)
    {
        return null;
    }


    // ----------------------------------------------------------
    public Date getCreateDate()
    {
        return null;
    }


    // ----------------------------------------------------------
    public Date getModifiedDate()
    {
        return new Date();
    }


    // ----------------------------------------------------------
    public String getName()
    {
        return MiltonRequestWrapper.currentRequest()._serverName();
    }


    // ----------------------------------------------------------
    public String getUniqueId()
    {
        return getName();
    }


    // ----------------------------------------------------------
    public Long getContentLength()
    {
        return null;
    }


    // ----------------------------------------------------------
    public String getContentType(String paramString)
    {
        return "text/html";
    }


    // ----------------------------------------------------------
    public Long getMaxAgeSeconds(Auth paramAuth)
    {
        return null;
    }


    // ----------------------------------------------------------
    public void sendContent(OutputStream out, Range range,
            Map<String, String> params, String contentType)
            throws IOException, NotAuthorizedException, BadRequestException
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
            String href = (!endsWithSlash ? "dav/" : "")
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
    }


    // ----------------------------------------------------------
    public CollectionResource createCollection(String paramString)
            throws NotAuthorizedException
    {
        throw new NotAuthorizedException(this);
    }


    // ----------------------------------------------------------
    public Resource createNew(String arg0, InputStream arg1, Long arg2,
            String arg3) throws NotAuthorizedException
    {
        throw new NotAuthorizedException(this);
    }


    // ----------------------------------------------------------
    public void copyTo(CollectionResource arg0, String arg1)
            throws NotAuthorizedException, BadRequestException,
            ConflictException
    {
        throw new NotAuthorizedException(this);
    }


    // ----------------------------------------------------------
    public void delete() throws NotAuthorizedException
    {
        throw new NotAuthorizedException(this);
    }


    // ----------------------------------------------------------
    public void moveTo(CollectionResource arg0, String arg1)
            throws ConflictException, NotAuthorizedException,
            BadRequestException
    {
        throw new NotAuthorizedException(this);
    }


    // ----------------------------------------------------------
    public LockToken createAndLock(String arg0, LockTimeout arg1, LockInfo arg2)
            throws NotAuthorizedException
    {
        throw new NotAuthorizedException(this);
    }


    //~ Static/instance variables .............................................
}
