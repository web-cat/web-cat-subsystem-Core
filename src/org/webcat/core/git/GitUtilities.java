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
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.util.FS;
import org.webcat.core.Application;
import org.webcat.core.FileUtilities;
import org.webcat.core.RepositoryProvider;
import org.webcat.core.User;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSMutableArray;

//-------------------------------------------------------------------------
/**
 * This class provides utility methods for working with Git repositories and
 * objects.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class GitUtilities
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Prevent instantiation.
     */
    private GitUtilities()
    {
        // Do nothing.
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the Git repository that is located in the file store area for the
     * specified object, creating it if necessary.
     *
     * @param object the EO object whose file store contains the Git repository
     * @return the repository
     */
    /*package*/ static Repository repositoryForObject(EOEnterpriseObject object)
    {
        File fsDir =
            Application.wcApplication().repositoryPathForObject(object);
        File wcDir =
            Application.wcApplication().workingCopyPathForObject(object);

        Repository repository = null;

        if (fsDir != null)
        {
            try
            {
                try
                {
                    repository = RepositoryCache.open(
                            FileKey.lenient(fsDir, FS.DETECTED), true);
                }
                catch (RepositoryNotFoundException e)
                {
                    log.info("Creating repository at " + fsDir
                            + " for the first time");

                    repository = setUpNewRepository(object, fsDir);
                }
            }
            catch (IOException e)
            {
                log.error("An exception occurred while trying to get the "
                        + "repository for object " + object, e);
            }
        }

        RepositoryInfo repoInfo = new RepositoryInfo();
        repoInfo.repositoryDir = fsDir;
        repoInfo.workingCopyDir = wcDir;
        repositoryInfos.put(repository, repoInfo);

        return repository;
    }


    // ----------------------------------------------------------
    /**
     * Gets a repository that represents a working copy of the specified
     * bare repository, creating it for the first time by cloning it if
     * necessary.
     *
     * @param repository the bare repository whose working copy should be
     *     retrieved
     * @param forcePull if true, always perform a pull from the bare repository
     *     to update it even if it already exists; if false, only pull when the
     *     working copy is first being created
     * @return a {@code Repository} object representing the working copy
     */
    public static Repository workingCopyForRepository(Repository repository,
            boolean forcePull)
    {
        RepositoryInfo repoInfo = repositoryInfos.get(repository);

        GitCloner cloner = new GitCloner(repoInfo.repositoryDir,
                repoInfo.workingCopyDir);

        Repository wcRepository = null;

        try
        {
            wcRepository = cloner.cloneRepository(forcePull);
        }
        catch (IOException e)
        {
            log.error("An exception occurred while trying to get the "
                    + "working copy repository for repository "
                    + repoInfo.repositoryDir, e);
        }

        return wcRepository;
    }


    // ----------------------------------------------------------
    /**
     * Push any changes to the specified working copy to the main repository
     * associated with the working copy.
     *
     * @param workingCopy the working copy
     * @param user the user who is making the push
     * @param commitMessage the commit message to associate with the push
     */
    public static void pushWorkingCopy(Repository workingCopy, User user,
            String commitMessage)
    {
        synchronized (workingCopyPushTimers)
        {
            Timer timer = workingCopyPushTimers.get(workingCopy);

            if (timer != null)
            {
                timer.cancel();
                workingCopyPushTimers.remove(workingCopy);
            }

            timer = new Timer();
            PushWorkingCopyTask task = new PushWorkingCopyTask(
                    workingCopy, user, commitMessage);
            timer.schedule(task, 5000);

            workingCopyPushTimers.put(workingCopy, timer);
        }
    }


    // ----------------------------------------------------------
    public static void pushWorkingCopyImmediately(Repository workingCopy,
            User user, String commitMessage)
    {
        pushWorkingCopyImmediately(workingCopy, user.name_LF(),
                user.email(), commitMessage);
    }


    // ----------------------------------------------------------
    @SuppressWarnings("deprecation")
    public static void pushWorkingCopyImmediately(Repository workingCopy,
            String authorName, String emailAddress, String commitMessage)
    {
        try
        {
            boolean amend = false;

            GitRepository gitRepo = new GitRepository(workingCopy);
            GitRef ref = gitRepo.refWithName(
                    Constants.R_HEADS + Constants.MASTER);
            NSArray<GitCommit> commits = ref.commits();

            if (commits != null && !commits.isEmpty())
            {
                GitCommit commit = commits.objectAtIndex(0);
                if (commitMessage.equals(commit.shortMessage()) &&
                        commit.commitTime().timeIntervalSinceNow()
                            > -3 * 60 * 60)
                {
                    amend = true;
                }
            }

            Git git = new Git(workingCopy);

            git.add()
                .addFilepattern(".")
                .setUpdate(false)
                .call();

            git.commit()
                .setAuthor(authorName, emailAddress)
                .setCommitter(authorName, emailAddress)
                .setMessage(commitMessage)
                .setAmend(amend)
                .call();

            RefSpec allHeadsSpec = new RefSpec()
                .setForceUpdate(true)
                .setSourceDestination(
                        Constants.R_HEADS + Constants.MASTER,
                        Constants.R_HEADS + Constants.MASTER);

            git.push()
                .setRefSpecs(allHeadsSpec)
                .call();
        }
        catch (Exception e)
        {
            log.error("Error updating repository: ", e);
        }
    }


    // ----------------------------------------------------------
    private static class PushWorkingCopyTask extends TimerTask
    {
        //~ Constructors ......................................................

        // ----------------------------------------------------------
        public PushWorkingCopyTask(Repository workingCopy, User user,
                String commitMessage)
        {
            this.workingCopy = workingCopy;
            this.authorName = user.name_LF();
            this.emailAddress = user.email();
            this.commitMessage = commitMessage;
        }


        //~ Methods ...........................................................

        // ----------------------------------------------------------
        @Override
        public void run()
        {
            synchronized (workingCopyPushTimers)
            {
                workingCopyPushTimers.remove(workingCopy);

                pushWorkingCopyImmediately(workingCopy, authorName,
                        emailAddress, commitMessage);
            }
        }


        //~ Static/instance variables .........................................

        private Repository workingCopy;
        private String authorName;
        private String emailAddress;
        private String commitMessage;
    }


    // ----------------------------------------------------------
    /**
     * Sorts an array of {@link GitTreeEntry} objects with a case-insensitive
     * ascending sort by name.
     *
     * @param entries the array of entries to sort
     */
    public static void sortEntries(NSMutableArray<GitTreeEntry> entries)
    {
        try
        {
            entries.sortUsingComparator(new NSComparator() {
                @Override
                public int compare(Object _lhs, Object _rhs)
                {
                    GitTreeEntry lhs = (GitTreeEntry) _lhs;
                    GitTreeEntry rhs = (GitTreeEntry) _rhs;

                    if (lhs.isTree() && !rhs.isTree())
                    {
                        return -1;
                    }
                    else if (rhs.isTree() && !lhs.isTree())
                    {
                        return 1;
                    }
                    else
                    {
                        return lhs.path().compareToIgnoreCase(rhs.path());
                    }
                }
            });
        }
        catch (NSComparator.ComparisonException e)
        {
            // Do nothing.
        }
    }


    // ----------------------------------------------------------
    /**
     * Sets up a new base Git repository for the specified object.
     *
     * @param object the object whose file store is desired
     * @param location the file system location for the repository
     * @return the newly created repository
     */
    private static Repository setUpNewRepository(EOEnterpriseObject object,
            File location) throws IOException
    {
        // This method performs the following actions to set up a new
        // repository:
        //
        // 1) Creates a new bare repository in the location requested by the
        //    method argument.
        // 2) Creates a temporary non-bare repository in the system temp
        //    directory, which is then configured to use the bare repository
        //    as a remote repository.
        // 3) Creates a README.txt file in the temporary repository's working
        //    directory, which contains a welcome message determined by the
        //    type of object that created the repo.
        // 4) Adds the README.txt to the repository, commits the change, and
        //    then pushes the changes to the bare repository.
        // 5) Finally, the temporary repository is deleted.
        //
        // This results in a usable bare repository being created, which a user
        // can now clone locally in order to manage their Web-CAT file store.

        // Create the bare repository.
        InitCommand init = new InitCommand();
        init.setDirectory(location);
        init.setBare(true);
        Repository bareRepository = init.call().getRepository();

        // Create the temporary repository.
        File tempRepoDir = File.createTempFile("newgitrepo", null);
        tempRepoDir.delete();
        tempRepoDir.mkdirs();

        init = new InitCommand();
        init.setDirectory(tempRepoDir);
        Repository tempRepository = init.call().getRepository();

        // Create the welcome files in the temporary repo.

        if (object instanceof RepositoryProvider)
        {
            RepositoryProvider provider = (RepositoryProvider) object;
            provider.initializeRepositoryContents(tempRepoDir);
        }

        // Make sure we created at least one file, since we can't do much with
        // an empty repository. If the object didn't put anything in the
        // staging area, we'll just create a dummy README file.

        File[] files = tempRepoDir.listFiles();
        boolean foundFile = false;

        for (File file : files)
        {
            String name = file.getName();
            if (!".".equals(name) && !"..".equals(name)
                    && !".git".equalsIgnoreCase(name))
            {
                foundFile = true;
                break;
            }
        }

        if (!foundFile)
        {
            PrintWriter writer = new PrintWriter(
                    new File(tempRepoDir, "README.txt"));
            writer.println(
              "This readme file is provided so that the initial repository\n"
            + "has some content. You may delete it when you push other files\n"
            + "into the repository, if you wish.");
            writer.close();
        }

        // Create an appropriate default .gitignore file.
        PrintWriter writer = new PrintWriter(
                new File(tempRepoDir, ".gitignore"));
        writer.println("~*");
        writer.println("._*");
        writer.println(".TemporaryItems");
        writer.println(".DS_Store");
        writer.println("Thumbs.db");
        writer.close();

        // Add the files to the temporary repository.
        AddCommand add = new AddCommand(tempRepository);
        add.addFilepattern(".");
        add.setUpdate(false);

        try
        {
            add.call();
        }
        catch (NoFilepatternException e)
        {
            log.error("An exception occurred when adding the welcome files "
                    + "to the repository: ", e);
            return bareRepository;
        }

        // Commit the changes.
        String email = Application.configurationProperties().getProperty(
                "coreAdminEmail");
        CommitCommand commit = new Git(tempRepository).commit();
        commit.setAuthor("Web-CAT", email);
        commit.setCommitter("Web-CAT", email);
        commit.setMessage("Initial repository setup.");

        try
        {
            commit.call();
        }
        catch (Exception e)
        {
            log.error("An exception occurred when committing the welcome files "
                    + "to the repository: ", e);
            return bareRepository;
        }

        // Push the changes to the bare repository.
        PushCommand push = new Git(tempRepository).push();
        push.setRemote(location.toURL().toString());
        push.setRefSpecs(new RefSpec("master"), new RefSpec("master"));

        try
        {
            push.call();
        }
        catch (Exception e)
        {
            log.error("An exception occurred when pushing the welcome files "
                    + "to the repository: ", e);
            return bareRepository;
        }

        // Cleanup after ourselves.
        FileUtilities.deleteDirectory(tempRepoDir);

        return bareRepository;
    }


    //~ Inner classes .........................................................

    // ----------------------------------------------------------
    /**
     * Used to filter refs that should be returned by the
     * {@link #refsForRepository()} method.
     */
    public interface RefFilter
    {
        // ------------------------------------------------------
        public boolean accepts(Ref ref);
    }


    // ----------------------------------------------------------
    /**
     * A small class to hold a mapping from a bare repository location to its
     * working copy location.
     */
    private static class RepositoryInfo
    {
        public File repositoryDir;
        public File workingCopyDir;
    }


    //~ Static/instance variables .............................................

    private static Map<Repository, RepositoryInfo> repositoryInfos =
        new HashMap<Repository, RepositoryInfo>();
    private static Map<Repository, Timer> workingCopyPushTimers =
        new HashMap<Repository, Timer>();

    private static final Logger log = Logger.getLogger(GitUtilities.class);
}
