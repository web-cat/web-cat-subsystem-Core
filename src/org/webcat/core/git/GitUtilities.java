/*==========================================================================*\
 |  $Id: GitUtilities.java,v 1.9 2012/11/29 16:31:25 aallowat Exp $
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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.util.FS;
import org.webcat.core.Application;
import org.webcat.core.EOBase;
import org.webcat.core.FileUtilities;
import org.webcat.core.RepositoryProvider;
import org.webcat.core.User;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSMutableArray;

//-------------------------------------------------------------------------
/**
 * This class provides utility methods for working with Git repositories and
 * objects.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.9 $, $Date: 2012/11/29 16:31:25 $
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
    /*package*/ static Repository repositoryForObject(EOBase object)
    {
        File fsDir =
            Application.wcApplication().repositoryPathForObject(object);
        File wcDir =
            Application.wcApplication().workingCopyPathForObject(object);
        if (fsDir == null)
        {
            String details = (object == null)
                ? "null"
                : (object.getClass().getSimpleName() + " " + object);
            log.error("repositoryForObject(): repositoryPathForObject() "
                + "returned null for " + details);
        }
        if (wcDir == null)
        {
            String details = (object == null)
                ? "null"
                : (object.getClass().getSimpleName() + " " + object);
            log.error("repositoryForObject(): workingCopyPathForObject() "
                + "returned null for " + details);
        }

        Repository repository = null;

        if (fsDir != null)
        {

            try
            {
                try
                {
                    repository = RepositoryCache.open(
                            FileKey.lenient(fsDir, FS.DETECTED), true);

                    if (repository == null)
                    {
                        log.error("RepositoryCache.open returned null for "
                                + "object " + object);
                    }
                }
                catch (RepositoryNotFoundException e)
                {
                    log.info("Creating repository at " + fsDir
                            + " for the first time");

                    repository = setUpNewRepository(object, fsDir);
                }
                catch (Exception e)
                {
                    log.error("An exception occurred while trying to get the "
                            + "repository for object " + object, e);
                }
            }
            catch (IOException e)
            {
                log.error("An exception occurred while trying to get the "
                        + "repository for object " + object, e);
            }
            catch (GitAPIException e)
            {
                log.error("A git API exception occurred while trying to get "
                        + "the repository for object " + object, e);
            }
        }

        if (repository == null)
        {
            String details = (object == null)
                ? "null"
                : (object.getClass().getSimpleName() + " " + object);
            log.error("repositoryForObject(): repository == null for "
                + details);
        }
        RepositoryInfo repoInfo = new RepositoryInfo();
        repoInfo.repositoryDir = fsDir;
        repoInfo.workingCopyDir = wcDir;
        String repoKey = canonicalPath(fsDir);
        if (repoKey != null)
        {
            repositoryInfos.put(repoKey, repoInfo);
        }
        String wcKey = canonicalPath(wcDir);
        if (wcKey != null)
        {
            repositoryInfos.put(wcKey, repoInfo);
        }

        return repository;
    }


    // ----------------------------------------------------------
    /**
     * Gets the canonical path of a file, falling back to absolute path on error.
     *
     * @param file the file
     * @return canonical path string or null if file is null
     */
    public static String canonicalPath(File file)
    {
        if (file == null)
        {
            return null;
        }
        try
        {
            return file.getCanonicalPath();
        }
        catch (IOException e)
        {
            return file.getAbsolutePath();
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the canonical path for a repository directory or its worktree.
     *
     * @param repo the repository
     * @return canonical path string or null if repo is null
     */
    public static String canonicalPath(Repository repo)
    {
        if (repo == null)
        {
            return null;
        }
        File dir = repo.isBare() ? repo.getDirectory() : repo.getWorkTree();
        return canonicalPath(dir);
    }


    // ----------------------------------------------------------
    /**
     * Gets or creates a reentrant lock for synchronizing operations on the
     * specified working copy directory.
     *
     * @param workingCopyDir the working copy directory
     * @return the ReentrantLock associated with the working copy, or null if directory is null
     */
    public static ReentrantLock lockForWorkingCopy(File workingCopyDir)
    {
        if (workingCopyDir == null)
        {
            return null;
        }
        String key = canonicalPath(workingCopyDir);
        ReentrantLock lock = workingCopyLocks.get(key);
        if (lock == null)
        {
            ReentrantLock newLock = new ReentrantLock();
            ReentrantLock existing = workingCopyLocks.putIfAbsent(key, newLock);
            lock = (existing != null) ? existing : newLock;
        }
        return lock;
    }


    // ----------------------------------------------------------
    /**
     * Atomically moves a source file to a destination file, falling back to a
     * standard replace if atomic move is not supported by the underlying filesystem.
     *
     * @param source the source file
     * @param destination the destination file
     * @throws IOException if an error occurs
     */
    public static void atomicMove(File source, File destination)
        throws IOException
    {
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists())
        {
            parent.mkdirs();
        }
        try
        {
            java.nio.file.Files.move(source.toPath(), destination.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        }
        catch (java.nio.file.AtomicMoveNotSupportedException e)
        {
            java.nio.file.Files.move(source.toPath(), destination.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
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
        String key = canonicalPath(repository);
        RepositoryInfo repoInfo = (key != null) ? repositoryInfos.get(key) : null;
        if (repoInfo == null && repository != null)
        {
            key = canonicalPath(repository.getDirectory());
            repoInfo = (key != null) ? repositoryInfos.get(key) : null;
        }

        if (repoInfo == null)
        {
            log.error("workingCopyForRepository(): no RepositoryInfo found for "
                    + repository);
            return null;
        }

        GitCloner cloner = new GitCloner(repoInfo.repositoryDir,
                repoInfo.workingCopyDir);

        Repository wcRepository = null;
        ReentrantLock lock = lockForWorkingCopy(repoInfo.workingCopyDir);
        if (lock != null)
        {
            lock.lock();
        }

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
        catch (GitAPIException e)
        {
            log.error("A git API exception occurred while trying to get the "
                    + "working copy repository for repository "
                    + repoInfo.repositoryDir, e);
        }
        finally
        {
            if (lock != null)
            {
                lock.unlock();
            }
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
        if (workingCopy == null)
        {
            return;
        }
        String key = canonicalPath(workingCopy);
        synchronized (workingCopyPushTimers)
        {
            Timer timer = workingCopyPushTimers.get(key);

            if (timer != null)
            {
                timer.cancel();
                workingCopyPushTimers.remove(key);
            }

            timer = new Timer();
            PushWorkingCopyTask task = new PushWorkingCopyTask(
                    workingCopy, user, commitMessage);
            timer.schedule(task, 5000);

            workingCopyPushTimers.put(key, timer);
        }
    }


    // ----------------------------------------------------------
    public static RevCommit pushWorkingCopyImmediately(Repository workingCopy,
            User user, String commitMessage)
    {
        return pushWorkingCopyImmediately(workingCopy, user.name_LF(),
                user.email(), commitMessage);
    }


    // ----------------------------------------------------------
    public static RevCommit pushWorkingCopyImmediately(Repository workingCopy,
            String authorName, String emailAddress, String commitMessage)
    {
        ReentrantLock lock = (workingCopy != null)
            ? lockForWorkingCopy(workingCopy.getWorkTree()) : null;
        if (lock != null)
        {
            lock.lock();
        }

        try
        {
            Git git = new Git(workingCopy);

            git.add()
                .addFilepattern(".")
                .setUpdate(false)
                .call();

            RevCommit commit = git.commit()
                .setAuthor(authorName, emailAddress)
                .setCommitter(authorName, emailAddress)
                .setMessage(commitMessage)
                .setAmend(false)
                .call();

            RefSpec allHeadsSpec = new RefSpec()
                .setForceUpdate(true)
                .setSourceDestination(
                        Constants.R_HEADS + Constants.MASTER,
                        Constants.R_HEADS + Constants.MASTER);

            git.push()
                .setRefSpecs(allHeadsSpec)
                .call();

            return commit;
        }
        catch (Exception e)
        {
            log.error("Error updating repository: ", e);
        }
        finally
        {
            if (lock != null)
            {
                lock.unlock();
            }
        }

        return null;
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
            String key = canonicalPath(workingCopy);
            synchronized (workingCopyPushTimers)
            {
                if (key != null)
                {
                    workingCopyPushTimers.remove(key);
                }

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
            File location) throws IOException, GitAPIException
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

            try
            {
                provider.initializeRepositoryContents(tempRepoDir);
            }
            catch (Exception e)
            {
                log.error("The following exception occurred when trying to "
                        + "initialize the repository contents at "
                        + location.getAbsolutePath() + ", but I'm continuing "
                        + "anyway to ensure that the repository isn't corrupt.",
                        e);
            }
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
        @SuppressWarnings("deprecation")
        String url = location.toURL().toString();
        push.setRemote(url);
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

        RepositoryCache.register(bareRepository);

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

    private static final Map<String, RepositoryInfo> repositoryInfos =
        new ConcurrentHashMap<String, RepositoryInfo>();
    private static final Map<String, Timer> workingCopyPushTimers =
        new ConcurrentHashMap<String, Timer>();
    private static final Map<String, ReentrantLock> workingCopyLocks =
        new ConcurrentHashMap<String, ReentrantLock>();

    private static final Logger log = Logger.getLogger(GitUtilities.class);
}
