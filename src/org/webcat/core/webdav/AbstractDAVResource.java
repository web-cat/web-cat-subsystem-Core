/*==========================================================================*\
 |  $Id: AbstractDAVResource.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

import org.apache.log4j.Logger;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

//-------------------------------------------------------------------------
/**
 * An abstract class for all DAV resources.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public abstract class AbstractDAVResource
    implements Resource, LockableResource, GetableResource
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public AbstractDAVResource(WorkingCopyResourceFactory factory)
    {
        this.factory = factory;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public WorkingCopyResourceFactory factory()
    {
        return factory;
    }


    // ----------------------------------------------------------
    public Object authenticate(String user, String password)
    {
        return factory().securityManager().authenticate(user, password);
    }


    // ----------------------------------------------------------
    public boolean authorise(Request request, Method method, Auth auth)
    {
        return factory().securityManager().authorise(
                request, method, auth, this);
    }


    // ----------------------------------------------------------
    public abstract boolean canModify();


    // ----------------------------------------------------------
    public abstract boolean canModifyContents();


    // ----------------------------------------------------------
    public LockToken getCurrentLock()
    {
        if (factory.getLockManager() != null)
        {
            return factory.getLockManager().getCurrentToken(this);
        }
        else
        {
            log.warn("getCurrentLock called, but no lock manager");
            return null;
        }
    }


    // ----------------------------------------------------------
    public String getRealm()
    {
        return null;
    }


    // ----------------------------------------------------------
    public LockResult lock(LockTimeout timeout, LockInfo lockInfo)
        throws NotAuthorizedException
    {
        return factory.getLockManager().lock(timeout, lockInfo, this);
    }


    // ----------------------------------------------------------
    public LockResult refreshLock(String token) throws NotAuthorizedException
    {
        return factory.getLockManager().refresh(token, this);
    }


    // ----------------------------------------------------------
    public void unlock(String tokenId) throws NotAuthorizedException
    {
        factory.getLockManager().unlock(tokenId, this);
    }


    //~ Static/instance variables .............................................

    private WorkingCopyResourceFactory factory;

    private static final Logger log = Logger.getLogger(
            AbstractDAVResource.class);
}
