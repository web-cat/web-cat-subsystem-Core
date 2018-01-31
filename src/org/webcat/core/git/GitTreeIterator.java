/*==========================================================================*\
 |  $Id: GitTreeIterator.java,v 1.2 2011/11/08 14:06:07 aallowat Exp $
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

import java.io.IOException;
import java.util.Iterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

//-------------------------------------------------------------------------
/**
 * An iterator that iterates over a Git tree or commit object.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2011/11/08 14:06:07 $
 */
public class GitTreeIterator
    implements Iterator<GitTreeEntry>, Iterable<GitTreeEntry>
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new non-recursive {@code GitTreeIterator} for the specified
     * tree or commit object.
     *
     * @param repository the repository
     * @param treeOrCommitId the id of the tree or commit   object
     */
    public GitTreeIterator(GitRepository repository, ObjectId treeOrCommitId)
    {
        this(repository, treeOrCommitId, null);
    }


    // ----------------------------------------------------------
    /**
     * Creates a new non-recursive {@code GitTreeIterator} for the specified
     * tree or commit object.
     *
     * @param repository the repository
     * @param treeOrCommitId the id of the tree or commit object
     * @param pathPrefix a prefix to be prepended to the paths of the returned
     *     entries, or null
     */
    public GitTreeIterator(GitRepository repository, ObjectId treeOrCommitId,
            String pathPrefix)
    {
        this(repository, treeOrCommitId, false);
        this.pathPrefix = pathPrefix;
    }


    // ----------------------------------------------------------
    /**
     * Creates a new {@code GitTreeIterator} for the specified tree or commit
     * object.
     *
     * @param repository the repository
     * @param treeOrCommitId the id of the tree or commit object
     * @param recursive true if the iterator should be recursive, otherwise
     *     false
     */
    public GitTreeIterator(GitRepository repository, ObjectId treeOrCommitId,
            boolean recursive)
    {
        this.repository = repository;
        this.recursive = recursive;

        int type = repository.typeOfObject(treeOrCommitId);

        if (type == Constants.OBJ_TREE)
        {
            initializeFromTree(treeOrCommitId);
        }
        else if (type == Constants.OBJ_COMMIT || type == Constants.OBJ_TAG)
        {
            initializeFromCommit(treeOrCommitId);
        }
        else
        {
            treeWalk = null;
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether or not the iterator is recursive.
     *
     * @return true if the iterator is recursive, otherwise false
     */
    public boolean isRecursive()
    {
        return recursive;
    }


    // ----------------------------------------------------------
    /**
     * Gets an array of {@link GitTreeEntry} objects that represent the entire
     * contents of the tree. This method exhausts the iterator and may only be
     * called on a newly-created iterator; it may not be called once
     * {@link #next()} has been called.
     *
     * @return an {@code NSArray} containing all of the {@link GitTreeEntry}
     *     objects in the tree
     */
    public NSArray<GitTreeEntry> allEntries()
    {
        if (nextWasCalled)
        {
            throw new IllegalStateException("allEntries() cannot be called "
                    + "once next() has been called.");
        }

        NSMutableArray<GitTreeEntry> entries =
            new NSMutableArray<GitTreeEntry>();

        for (GitTreeEntry entry : this)
        {
            entries.addObject(entry);
        }

        GitUtilities.sortEntries(entries);
        return entries;
    }


    // ----------------------------------------------------------
    /**
     * Releases any resources in use by the iterator.
     */
    public void release()
    {
        if (revWalk != null)
        {
            revWalk.release();
        }

        if (treeWalk != null)
        {
            treeWalk.release();
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns this iterator. This method is provided so that the iterator
     * itself can be used in a for-each loop.
     *
     * @return this iterator
     */
    public Iterator<GitTreeEntry> iterator()
    {
        return this;
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether or not the iterator has another entry.
     *
     * @return true if the iterator has another entry, or false if it does not
     */
    public boolean hasNext()
    {
        try
        {
            return treeWalk != null && treeWalk.next();
        }
        catch (IOException e)
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the next entry in the tree.
     *
     * @return a {@link GitTreeEntry} representing the next entry in the tree
     */
    public GitTreeEntry next()
    {
        nextWasCalled = true;
        return GitTreeEntry.fromTreeWalk(treeWalk, repository, pathPrefix);
    }


    // ----------------------------------------------------------
    /**
     * Removal is not supported by {@code GitTreeIterator}.
     *
     * @throws UnsupportedOperationException
     */
    public void remove()
    {
        throw new UnsupportedOperationException("remove() is not supported on "
                + "GitTreeIterators.");
    }


    // ----------------------------------------------------------
    /**
     * Initializes the iterator from an object ID that represents a tree.
     *
     * @param treeId the tree id
     */
    private void initializeFromTree(ObjectId treeId)
    {
        treeWalk = new TreeWalk(repository.repository());
        treeWalk.setRecursive(recursive);

        try
        {
            treeWalk.addTree(treeId);
        }
        catch (IOException e)
        {
            // Do nothing.
        }
    }


    // ----------------------------------------------------------
    /**
     * Initializes the iterator from an object ID that represents a commit.
     *
     * @param treeId the commit id
     */
    private void initializeFromCommit(ObjectId commitId)
    {
        revWalk = new RevWalk(repository.repository());
        treeWalk = new TreeWalk(repository.repository());
        treeWalk.setRecursive(recursive);

        try
        {
            CanonicalTreeParser parser = new CanonicalTreeParser();
            ObjectReader reader = repository.repository().newObjectReader();
            parser.reset(reader, revWalk.parseTree(commitId));
            treeWalk.addTree(parser);
        }
        catch (IOException e)
        {
            // Do nothing.
        }
    }


    //~ Static/instance variables .............................................

    private GitRepository repository;
    private String pathPrefix;
    private boolean recursive;
    private boolean nextWasCalled;
    private TreeWalk treeWalk;
    private RevWalk revWalk;
}
