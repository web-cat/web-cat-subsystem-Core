/*==========================================================================*\
 |  $Id: GitRepository.java,v 1.3 2012/06/22 16:23:18 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011-2012 Virginia Tech
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.jfree.util.Log;
import org.webcat.archives.IWritableContainer;
import org.webcat.core.EOBase;
import org.webcat.core.NSMutableDataOutputStream;
import org.webcat.core.git.GitUtilities.RefFilter;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.3 $, $Date: 2012/06/22 16:23:18 $
 */
public class GitRepository
    implements NSKeyValueCodingAdditions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitRepository(Repository repository)
    {
        this.repository = repository;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public static GitRepository repositoryForObject(EOBase eo)
    {
        Repository repo = GitUtilities.repositoryForObject(eo);
        GitRepository gr = new GitRepository(repo);
        gr.provider = eo;
        return gr;
    }


    // ----------------------------------------------------------
    public EOBase provider()
    {
        return provider;
    }


    // ----------------------------------------------------------
    public Repository repository()
    {
        return repository;
    }


    // ----------------------------------------------------------
    private NSArray<GitRef> filteredRefs(RefFilter filter)
    {
        NSMutableArray<GitRef> result = new NSMutableArray<GitRef>();

        Map<String, Ref> refs = repository.getAllRefs();

        for (Ref ref : RefComparator.sort(refs.values()))
        {
            if (filter == null || filter.accepts(ref))
            {
                result.addObject(new GitRef(this, ref));
            }
        }

        return result;
    }


    // ----------------------------------------------------------
    public NSArray<GitRef> headRefs()
    {
        return filteredRefs(new RefFilter() {
            public boolean accepts(Ref ref)
            {
                return ref.getName().startsWith(Constants.R_HEADS);
            }
        });
    }


    // ----------------------------------------------------------
    public NSArray<GitRef> tagRefs()
    {
        return filteredRefs(new RefFilter() {
            public boolean accepts(Ref ref)
            {
                return ref.getName().startsWith(Constants.R_TAGS);
            }
        });
    }


    // ----------------------------------------------------------
    public NSArray<GitRef> allRefs()
    {
        return filteredRefs(null);
    }


    // ----------------------------------------------------------
    public GitRef refWithName(String name)
    {
        Ref ref = null;

        try
        {
            ref = repository.getRef(name);
        }
        catch (IOException e)
        {
            // Do nothing.
        }

        return ref != null ? new GitRef(this, ref) : null;
    }


    // ----------------------------------------------------------
    public ObjectId resolve(String revstr)
    {
        try
        {
            return repository.resolve(revstr);
        }
        catch (Exception e)
        {
            Log.error("An error occurred when resolving the string " + revstr
                    + " in the repository at " + repository.getDirectory(), e);
            return null;
        }
    }


    // ----------------------------------------------------------
    public NSArray<GitCommit> commitsWithId(ObjectId commitId, String path)
    {
        if (commitId != null)
        {
            return commitsWithIds(new NSArray<ObjectId>(commitId), path);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Parses an array of Git commits and returns an array of commits that
     * affected the specified object.
     *
     * @param repository the Git repository
     * @param commitIds the Git commit IDs to parse
     * @param path the object affected
     * @return an array of {@link GitCommit} objects representing the commits
     */
    public NSArray<GitCommit> commitsWithIds(NSArray<ObjectId> commitIds,
            String path)
    {
        try
        {
            RevWalk rw = new RevWalk(repository);

            try
            {
                rw.sort(RevSort.COMMIT_TIME_DESC);

                if (path != null)
                {
                    rw.setTreeFilter(AndTreeFilter.create(
                            PathSuffixFilter.create(path),
                            TreeFilter.ANY_DIFF));
                }
                else
                {
                    rw.setTreeFilter(TreeFilter.ALL);
                }

                for (ObjectId commitId : commitIds)
                {
                    rw.markStart(rw.parseCommit(commitId));
                }

                NSMutableArray<GitCommit> commits =
                    new NSMutableArray<GitCommit>();

                for (RevCommit commit : rw)
                {
                    commits.add(new GitCommit(commit));
                }

                return commits;
            }
            finally
            {
                rw.release();
            }
        }
        catch (Exception e)
        {
            log.error("An exception occurred while parsing the commit: ", e);
            return null;
        }
    }


    // ----------------------------------------------------------
    public NSDictionary<GitRef, NSArray<GitCommit>> commitsForRefs(
            NSArray<GitRef> refs)
    {
        NSMutableDictionary<GitRef, NSArray<GitCommit>> refsToCommits =
            new NSMutableDictionary<GitRef, NSArray<GitCommit>>();

        for (GitRef ref : refs)
        {
            refsToCommits.setObjectForKey(ref.commits(), ref);
        }

        return refsToCommits;
    }


    // ----------------------------------------------------------
    /**
     * Gets the type of the object with the specified ID (either a tree, tag,
     * blob, or commit).
     *
     * @param id the object ID
     * @return the object type, one of the {@code OBJ_*} constants in the
     *     {@link Constants} class.
     */
    public int typeOfObject(ObjectId id)
    {
        ObjectReader reader = repository.newObjectReader();

        try
        {
            ObjectLoader loader = reader.open(id);
            return loader.getType();
        }
        catch (Exception e)
        {
            return Constants.OBJ_BAD;
        }
        finally
        {
            reader.release();
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the contents of the specified blob as raw data.
     *
     * @param objectId the id of the blob
     * @return an {@code NSData} object containing the raw data from the blob
     */
    public NSData contentForBlob(ObjectId objectId)
    {
        ObjectReader reader = repository.newObjectReader();

        try
        {
            ObjectLoader loader = reader.open(objectId);

            NSMutableDataOutputStream output = new NSMutableDataOutputStream();
            loader.copyTo(output);
            output.close();

            return output.data();
        }
        catch (IOException e)
        {
            log.error("An exception occurred while getting the blob "
                    + "contents: ", e);
            return null;
        }
        finally
        {
            reader.release();
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the contents of the specified blob as a string.
     *
     * @param objectId the id of the blob
     * @return a string containing the data from the blob
     */
    public String stringContentForBlob(ObjectId objectId)
    {
        try
        {
            return new String(contentForBlob(objectId).bytes(), "UTF-8");
        }
        catch (IOException e)
        {
            log.error("An exception occurred while getting the blob "
                    + "contents: ", e);
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Writes the contents of the specified blob to an output stream.
     *
     * @param objectId the id of the blob
     * @param stream the stream to write the blob to
     */
    public void writeBlobToStream(ObjectId objectId, OutputStream stream)
    {
        ObjectReader reader = repository.newObjectReader();

        try
        {
            ObjectLoader loader = reader.open(objectId);
            loader.copyTo(stream);
        }
        catch (IOException e)
        {
            log.error("An exception occurred while getting the blob "
                    + "contents: ", e);
        }
        finally
        {
            reader.release();
        }
    }


    // ----------------------------------------------------------
    /**
     * Copies an item from the repository into a container. If the item is a
     * blob, it will be copied into the destination with the specified name; if
     * the item is a tree or commit, its children will be recursively copied.
     *
     * @param objectId the id of the object to copy
     * @param name the destination name to use (only when the item is a blob)
     * @param container the container where the items should be copied
     * @throws IOException if an I/O error occurred
     */
    public void copyItemToContainer(ObjectId objectId, String name,
            IWritableContainer container) throws IOException
    {
        int type = typeOfObject(objectId);

        if (type == Constants.OBJ_BLOB)
        {
            OutputStream fileStream = container.createFile(name, -1);
            writeBlobToStream(objectId, fileStream);
            fileStream.close();
        }
        else if (type == Constants.OBJ_TREE || type == Constants.OBJ_COMMIT)
        {
            GitTreeIterator iterator = new GitTreeIterator(this, objectId);

            try
            {
                for (GitTreeEntry entry : iterator)
                {
                    if (entry.isTree())
                    {
                        IWritableContainer childContainer =
                            container.createContainer(entry.name());

                        copyItemToContainer(entry.objectId(), entry.name(),
                                childContainer);

                        childContainer.finish();
                    }
                    else
                    {
                        copyItemToContainer(entry.objectId(), entry.name(),
                                container);
                    }
                }
            }
            finally
            {
                iterator.release();
            }
        }
    }


    // ----------------------------------------------------------
    public GitRef createTagForObject(String name, ObjectId objectId)
    {
        TagCommand tag = new Git(repository).tag();
        tag.setName(name);

        if (objectId == null)
        {
            tag.setObjectId(null);
        }
        else
        {
            RevWalk revWalk = new RevWalk(repository);

            try
            {
                RevCommit commit = revWalk.parseCommit(objectId);
                tag.setObjectId(commit);
            }
            catch (Exception e)
            {
                return null;
            }
            finally
            {
                revWalk.release();
            }
        }

        try
        {
            tag.call();
            return refWithName(Constants.R_TAGS + name);
        }
        catch (Exception e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    @Override
    public int hashCode()
    {
        return repository.hashCode() ^ 0xEA70BEEF;
    }


    // ----------------------------------------------------------
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof GitRepository)
        {
            GitRepository otherRepository = (GitRepository) other;
            return repository == otherRepository.repository;
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    public void takeValueForKeyPath(Object value, String keyPath)
    {
        NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(
                this, value, keyPath);
    }


    // ----------------------------------------------------------
    public Object valueForKeyPath(String keyPath)
    {
        return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(
                this, keyPath);
    }


    // ----------------------------------------------------------
    public void takeValueForKey(Object value, String key)
    {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(
                this, value, key);
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }


    //~ Static/instance variables .............................................

    private Repository repository;
    private EOBase provider;

    private static final Logger log = Logger.getLogger(GitRepository.class);
}
