/*==========================================================================*\
 |  $Id: GitBranchesPage.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
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

import org.webcat.core.InlineStatusIndicator;
import org.webcat.core.git.GitCommit;
import org.webcat.core.git.GitRef;
import org.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class GitBranchesPage
    extends GitWebComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new {@code GitSummaryPage}.
     *
     * @param context the context
     */
    public GitBranchesPage(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public NSArray<GitRef> headRefs;
    public GitRef          headRef;
    public int             headRefIndex;
    public NSDictionary<GitRef, NSArray<GitCommit>> headRefCommits;

    public NSArray<GitRef> tagRefs;
    public GitRef          tagRef;
    public int             tagRefIndex;
    public NSDictionary<GitRef, NSArray<GitCommit>> tagRefCommits;

    public String          newTagName;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (headRefs == null)
        {
            headRefs = gitContext().repository().headRefs();
        }

        if (headRefCommits == null)
        {
            headRefCommits = gitContext().repository().commitsForRefs(
                    headRefs);
        }

        gatherTags();

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    private void gatherTags()
    {
        tagRefs = gitContext().repository().tagRefs();
        tagRefCommits = gitContext().repository().commitsForRefs(tagRefs);
    }


    // ----------------------------------------------------------
    public String branchTreeURL()
    {
        GitWebContext newContext = gitContext().clone();
        newContext.setMode(GitWebMode.TREE);
        newContext.setHeadRef(headRef);
        return newContext.toURL(context());
    }


    // ----------------------------------------------------------
    public String tagTreeURL()
    {
        GitWebContext newContext = gitContext().clone();
        newContext.setMode(GitWebMode.TREE);
        newContext.setHeadRef(tagRef);
        return newContext.toURL(context());
    }


    // ----------------------------------------------------------
    public GitCommit latestCommitForHeadRef()
    {
        NSArray<GitCommit> commits = headRefCommits.objectForKey(headRef);

        if (commits != null && commits.count() > 0)
        {
            return commits.objectAtIndex(0);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public GitCommit latestCommitForTagRef()
    {
        NSArray<GitCommit> commits = tagRefCommits.objectForKey(tagRef);

        if (commits != null && commits.count() > 0)
        {
            return commits.objectAtIndex(0);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public WOActionResults createTag()
    {
        GitRef aTagRef =
            gitContext().repository().createTagForObject(newTagName, null);

        JavascriptGenerator js;

        if (aTagRef != null)
        {
            gatherTags();
            js = new JavascriptGenerator().refresh("tagModule");
        }
        else
        {
            js = new JavascriptGenerator();
            InlineStatusIndicator.updateWithState(js, "createTagStatus",
                    InlineStatusIndicator.ERROR,
                    "The tag <strong>" + newTagName + "</strong> could not be "
                    + "created. It may already exist, or the name was "
                    + "invalid.");
        }

        newTagName = null;
        return js;
    }
}
