/*==========================================================================*\
 |  $Id: FileBasedLockManager.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.LockResult.FailureReason;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.http.fs.LockManager;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class FileBasedLockManager implements LockManager
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public FileBasedLockManager()
    {
        locksByPath = new HashMap<DAVPath, CurrentLock>();
        locksByToken = new HashMap<String, CurrentLock>();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public synchronized LockResult lock(LockTimeout timeout, LockInfo lockInfo,
            LockableResource r)
    {
        if (!(r instanceof AbstractFSResource))
        {
            return LockResult.failed(FailureReason.PRECONDITION_FAILED);
        }

        AbstractFSResource resource = (AbstractFSResource) r;

        LockToken currentLock = currentLock(resource);
        if (currentLock != null)
        {
            return LockResult.failed(LockResult.FailureReason.ALREADY_LOCKED);
        }

        LockToken newToken = new LockToken(
                UUID.randomUUID().toString(), lockInfo, timeout);

        CurrentLock newLock = new CurrentLock(
                resource.path(), newToken, lockInfo.lockedByUser);

        locksByPath.put(resource.path(), newLock);
        locksByToken.put(newToken.tokenId, newLock);

        return LockResult.success(newToken);
    }


    // ----------------------------------------------------------
    public synchronized LockResult refresh(String tokenId,
            LockableResource resource)
    {
        CurrentLock curLock = locksByToken.get(tokenId);

        if (curLock == null)
        {
            log.debug("can't refresh because no lock");

            return LockResult.failed(FailureReason.PRECONDITION_FAILED);
        }
        else
        {
            curLock.token.setFrom(new Date());
            return LockResult.success(curLock.token);
        }
    }


    // ----------------------------------------------------------
    public synchronized void unlock(String tokenId, LockableResource r)
            throws NotAuthorizedException
    {
        if (!(r instanceof AbstractFSResource))
        {
            return;
        }

        AbstractFSResource resource = (AbstractFSResource) r;

        LockToken lockToken = currentLock(resource);

        if (lockToken == null)
        {
            log.debug("not locked");
            return;
        }

        if (lockToken.tokenId.equals(tokenId))
        {
            removeLock(lockToken);
        }
        else
        {
            throw new NotAuthorizedException(resource);
        }
    }


    // ----------------------------------------------------------
    public LockToken getCurrentToken(LockableResource r)
    {
        if (!(r instanceof AbstractFSResource))
        {
            return null;
        }

        AbstractFSResource resource = (AbstractFSResource) r;

        CurrentLock lock = locksByPath.get(resource.path());
        if (lock == null)
        {
            return null;
        }

        LockToken token = new LockToken();
        token.info = new LockInfo(
                LockInfo.LockScope.EXCLUSIVE,
                LockInfo.LockType.WRITE,
                lock.lockedByUser,
                LockInfo.LockDepth.ZERO);
        token.info.lockedByUser = lock.lockedByUser;
        token.timeout = lock.token.timeout;
        token.tokenId = lock.token.tokenId;

        return token;
    }


    // ----------------------------------------------------------
    /**
     * Looks for a lock token for the specified resource in the lock table and
     * returns it if it was found and has not yet expired.
     *
     * @param resource the resource
     * @return the lock token for the resource, or null if the lock did not
     *     exist or had expired
     */
    private LockToken currentLock(AbstractFSResource resource)
    {
        CurrentLock curLock = locksByPath.get(resource.path());
        if (curLock == null)
        {
            return null;
        }

        LockToken token = curLock.token;

        if (token.isExpired())
        {
            removeLock(token);
            return null;
        }
        else
        {
            return token;
        }
    }


    // ----------------------------------------------------------
    /**
     * Removes the lock with the specified {@code LockToken} from the manager's
     * lock tables.
     *
     * @param token the lock token to remove
     */
    private void removeLock(LockToken token)
    {
        log.debug("removeLock: " + token.tokenId);

        CurrentLock currentLock = locksByToken.get(token.tokenId);

        if (currentLock != null)
        {
            locksByPath.remove(currentLock.path);
            locksByToken.remove(currentLock.token.tokenId);
        }
        else
        {
            log.warn("removeLock: Couldn't find lock: " + token.tokenId);
        }
    }


    //~ Inner classes .........................................................

    // ----------------------------------------------------------
    private static class CurrentLock
    {
        //~ Constructors ......................................................

        // ----------------------------------------------------------
        public CurrentLock(DAVPath path, LockToken token, String lockedByUser)
        {
            this.path = path;
            this.token = token;
            this.lockedByUser = lockedByUser;
        }


        //~ Static/instance variables .........................................

        public final DAVPath path;
        public final LockToken token;
        public final String lockedByUser;
    }


    //~ Static/instance variables .............................................

    private Map<DAVPath, CurrentLock> locksByPath;
    private Map<String, CurrentLock> locksByToken;

    private static final Logger log = LoggerFactory.getLogger(
            FileBasedLockManager.class);
}
