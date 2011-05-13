/*==========================================================================*\
 |  $Id$
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

package org.webcat.core.git;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefComparator;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;

//-------------------------------------------------------------------------
/**
 * Provides functionality to clone a repository, creating it for the first time
 * if necessary.
 *
 * @author Tony Allevato
 * @author Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class GitCloner
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitCloner(File repositoryToClone, File destination)
    {
        this.repositoryToClone = repositoryToClone;
        this.destination = destination;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public Repository cloneRepository(boolean forcePull) throws IOException
    {
        if (!destination.exists())
        {
            forcePull = true;
            log.debug("Working copy created for the first time: " + destination);
        }

        Repository wcRepository = createWorkingCopyRepositoryIfNecessary(
                destination, repositoryToClone);

        if (forcePull)
        {
            FetchResult fetchResult = doFetch(wcRepository);
            Ref head = guessHead(fetchResult);
            doCheckout(wcRepository, head);

            log.debug("Pulled to update working copy: " + destination);
        }

        return wcRepository;
    }


    // ----------------------------------------------------------
    private Repository createWorkingCopyRepositoryIfNecessary(
            File location, File remoteDir) throws IOException
    {
        Repository wcRepository;

        try
        {
            wcRepository = RepositoryCache.open(FileKey.lenient(location,
                    FS.DETECTED));
        }
        catch (RepositoryNotFoundException e)
        {
            // Create the repository from scratch.

            if (!location.exists())
            {
                location.mkdirs();
            }

            InitCommand init = Git.init();
            init.setDirectory(location);
            init.setBare(false);
            wcRepository = init.call().getRepository();

            StoredConfig config = wcRepository.getConfig();
            config.setBoolean("core", null, "bare", false);

            try
            {
                RefSpec refSpec = new RefSpec().setForceUpdate(true)
                    .setSourceDestination(Constants.R_HEADS + "*",
                            Constants.R_REMOTES + "origin" + "/*");

                RemoteConfig remoteConfig = new RemoteConfig(config, "origin");
                remoteConfig.addURI(new URIish(remoteDir.toString()));
                remoteConfig.addFetchRefSpec(refSpec);
                remoteConfig.update(config);
            }
            catch (URISyntaxException e2)
            {
                // Do nothing.
            }

            config.save();
        }

        return wcRepository;
    }


    // ----------------------------------------------------------
    private FetchResult doFetch(Repository repository)
            throws NotSupportedException, TransportException
    {
        FetchResult result = null;

        try
        {
            Transport transport = Transport.open(repository, "origin");

            try
            {
                result = transport.fetch(NullProgressMonitor.INSTANCE, null);
            }
            finally
            {
                transport.close();
            }
        }
        catch (URISyntaxException e)
        {
            // Do nothing.
        }

        return result;
    }


    // ----------------------------------------------------------
    private void doCheckout(Repository repository, Ref branch)
            throws IOException
    {
        if (!Constants.HEAD.equals(branch.getName()))
        {
            RefUpdate refUpdate = repository.updateRef(Constants.HEAD);
            refUpdate.disableRefLog();
            refUpdate.link(branch.getName());
        }

        RevCommit commit = parseCommit(repository, branch);
        RefUpdate refUpdate = repository.updateRef(Constants.HEAD);
        refUpdate.setNewObjectId(commit);
        refUpdate.forceUpdate();

        DirCache dirCache = repository.lockDirCache();
        DirCacheCheckout checkout = new DirCacheCheckout(
                repository, dirCache, commit.getTree());
        checkout.checkout();
    }


    // ----------------------------------------------------------
    private RevCommit parseCommit(Repository repository, Ref branch)
            throws MissingObjectException, IncorrectObjectTypeException,
            IOException
    {
        RevWalk rw = new RevWalk(repository);
        RevCommit commit;

        try
        {
            commit = rw.parseCommit(branch.getObjectId());
        }
        finally
        {
            rw.release();
        }

        return commit;
    }


    // ----------------------------------------------------------
    private Ref guessHead(FetchResult result)
    {
        Ref idHEAD = result.getAdvertisedRef(Constants.HEAD);
        List<Ref> availableRefs = new ArrayList<Ref>();
        Ref head = null;

        for (Ref ref : result.getAdvertisedRefs())
        {
            String name = ref.getName();

            if (!name.startsWith(Constants.R_HEADS))
            {
                continue;
            }

            availableRefs.add(ref);

            if (idHEAD == null || head != null)
            {
                continue;
            }

            if (ref.getObjectId().equals(idHEAD.getObjectId()))
            {
                head = ref;
            }
        }

        Collections.sort(availableRefs, RefComparator.INSTANCE);

        if (idHEAD != null && head == null)
        {
            head = idHEAD;
        }

        return head;
    }


    //~ Static/instance variables .............................................

    private File repositoryToClone;
    private File destination;

    private static final Logger log = Logger.getLogger(GitCloner.class);
}
