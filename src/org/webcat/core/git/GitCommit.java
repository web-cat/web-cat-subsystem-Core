/*==========================================================================*\
 |  $Id: GitCommit.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

import org.eclipse.jgit.revwalk.RevCommit;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * Wraps a Git commit object with KVC support and some other convenience
 * methods.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class GitCommit implements NSKeyValueCodingAdditions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new {@code GitCommit} with the specified Git commit object.
     *
     * @param commit the Git commit object
     */
    public GitCommit(RevCommit commit)
    {
        this.commit = commit;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the Git commit object wrapped by this object.
     *
     * @return the Git commit object wrapped by this object
     */
    public RevCommit revCommit()
    {
        return commit;
    }


    // ----------------------------------------------------------
    public GitCommit parent()
    {
        if (parents().count() > 0)
        {
            return parents().objectAtIndex(0);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public NSArray<GitCommit> parents()
    {
        if (parents == null)
        {
            NSMutableArray<GitCommit> _parents =
                new NSMutableArray<GitCommit>();

            for (int i = 0; i < commit.getParentCount(); i++)
            {
                _parents.addObject(new GitCommit(commit.getParent(i)));
            }

            parents = _parents;
        }

        return parents;
    }

    // ----------------------------------------------------------
    public String hashAsString()
    {
        return commit.getName();
    }


    // ----------------------------------------------------------
    public String treeHashAsString()
    {
        return commit.getTree().getName();
    }


    // ----------------------------------------------------------
    /**
     * Gets the full message associated with this commit.
     *
     * @return the full message associated with this commit
     */
    public String fullMessage()
    {
        try
        {
            return commit.getFullMessage();
        }
        catch (Exception e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the short (summary) message associated with this commit.
     *
     * @return the short (summary) message associated with this commit
     */
    public String shortMessage()
    {
        try
        {
            return commit.getShortMessage();
        }
        catch (Exception e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the time when this commit occurred.
     *
     * @return the time when this commit occurred
     */
    public NSTimestamp commitTime()
    {
        try
        {
            long millis = commit.getCommitTime() * 1000L;
            return new NSTimestamp(millis);
        }
        catch (Exception e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the identity of the person who authored this commit.
     *
     * @return a {link GitPerson} object representing the author of the commit
     */
    public GitPerson author()
    {
        if (author == null)
        {
            author = new GitPerson(commit.getAuthorIdent());
        }

        return author;
    }


    // ----------------------------------------------------------
    /**
     * Gets the identity of the person who committed this commit.
     *
     * @return a {@link GitPerson} object representing the committer
     */
    public GitPerson committer()
    {
        if (committer == null)
        {
            committer = new GitPerson(commit.getCommitterIdent());
        }

        return committer;
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

    private RevCommit commit;
    private GitPerson author;
    private GitPerson committer;
    private NSArray<GitCommit> parents;
}
