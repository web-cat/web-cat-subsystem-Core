/*==========================================================================*\
 |  $Id: GitTreeComponent.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
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

package org.webcat.core.git.http;

import org.webcat.core.FileUtilities;
import org.webcat.core.git.GitCommit;
import org.webcat.core.git.GitTreeEntry;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

//-------------------------------------------------------------------------
/**
 * This component renders a Git tree object as a table, with a row for each of
 * its immediate children, along with information about those children such as
 * their size, last commit date, last committer, and last commit message.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class GitTreeComponent
    extends GitWebComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new {@code GitTreeComponent}.
     *
     * @param context the context
     */
    public GitTreeComponent(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public NSArray<GitTreeEntry> entries;

    public GitTreeEntry          anEntry;
    public int                   index;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (commitsForEntries == null)
        {
            commitsForEntries =
                new NSMutableDictionary<GitTreeEntry, NSArray<GitCommit>>();

            if (entries != null)
            {
                for (GitTreeEntry entry : entries)
                {
                    NSArray<GitCommit> commits =
                        entry.repository().commitsWithId(
                            gitContext().headObjectId(), entry.path());

                    if (commits != null)
                    {
                        commitsForEntries.setObjectForKey(commits, entry);
                    }
                }
            }
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    /**
     * Gets the icon URL for the current entry, relative to
     * Core.framework/WebServerResources.
     *
     * @return the icon URL for the current entry
     */
    public String anEntryIconURL()
    {
        if (anEntry.isTree())
        {
            return FileUtilities.folderIconURL(true);
        }
        else
        {
            return FileUtilities.iconURL(anEntry.name());
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the URL to be used in the href for the current entry's link.
     *
     * @return the current entry's href URL
     */
    public String anEntryURL()
    {
        GitWebContext newContext = gitContext().clone();

        newContext.setMode(anEntry.isTree() ?
                GitWebMode.TREE : GitWebMode.BLOB);
        newContext.appendToPath(anEntry.name());

        return newContext.toURL(context());
    }


    // ----------------------------------------------------------
    /**
     * Gets the URL to be used in the href for the current entry's link.
     *
     * @return the current entry's href URL
     */
    public String parentURL()
    {
        GitWebContext newContext = gitContext().clone();

        newContext.setMode(GitWebMode.TREE);
        newContext.removeLastPathComponent();
        newContext.setObjectId(null);

        return newContext.toURL(context());
    }


    // ----------------------------------------------------------
    public String anEntryCommitLink()
    {
        GitCommit latestCommit = anEntryLatestCommit();

        if (latestCommit != null)
        {
            GitWebContext newContext = gitContext().clone();

            newContext.setMode(GitWebMode.COMMIT);
            newContext.setObjectId(latestCommit.revCommit());
            newContext.setPath(null);

            return newContext.toURL(context());
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the latest commit object for the current entry.
     *
     * @return the latest commit object for the current entry
     */
    public GitCommit anEntryLatestCommit()
    {
        NSArray<GitCommit> commits = anEntryCommits();

        if (commits == null || commits.size() == 0)
        {
            return null;
        }
        else
        {
            return commits.objectAtIndex(0);
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the array of commits for the current entry.
     *
     * @return the commits for the current entry
     */
    public NSArray<GitCommit> anEntryCommits()
    {
        return commitsForEntries.objectForKey(anEntry);
    }


    //~ Static/instance variables .............................................

    private NSMutableDictionary<GitTreeEntry, NSArray<GitCommit>>
        commitsForEntries;
}
