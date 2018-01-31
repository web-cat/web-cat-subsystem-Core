/*==========================================================================*\
 |  $Id: GitCommitListComponent.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

package org.webcat.core.git.http;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.eclipse.jgit.lib.ObjectId;
import org.webcat.core.TimeUtilities;
import org.webcat.core.git.GitCommit;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class GitCommitListComponent extends GitWebComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitCommitListComponent(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public boolean showsOnlyLatestCommit;
    public NSArray<GitCommit> commits;
    public GitCommit commit;
    public int commitIndex;
    public boolean isInTable;
    public GitCommit parentCommit;
    public DateFormat commitHeaderFormat = new SimpleDateFormat("yyyy-MM-dd");


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        isInTable = false;

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public NSArray<GitCommit> commits()
    {
        if (commits != null && showsOnlyLatestCommit)
        {
            return new NSArray<GitCommit>(commits.objectAtIndex(0));
        }
        else
        {
            return commits;
        }
    }


    // ----------------------------------------------------------
    public boolean testAndSetIsInTable()
    {
        boolean oldValue = isInTable;
        isInTable = true;
        return oldValue;
    }


    // ----------------------------------------------------------
    public void setCommit(GitCommit commit)
    {
        this.previousCommit = this.commit;
        this.commit = commit;
    }


    // ----------------------------------------------------------
    public boolean shouldDisplayDate()
    {
        return (showsOnlyLatestCommit == false &&
                (previousCommit == null
                        || !TimeUtilities.sameCalendarDay(
                                previousCommit.commitTime(),
                                commit.commitTime())));
    }


    // ----------------------------------------------------------
    public String commitId()
    {
        return abbreviatedId(commit.revCommit());
    }


    // ----------------------------------------------------------
    public String commitURL()
    {
        GitWebContext newContext = gitContext().clone();
        newContext.setMode(GitWebMode.COMMIT);
        newContext.setObjectId(commit.revCommit());
        return newContext.toURL(context());
    }


    // ----------------------------------------------------------
    public String commitTreeId()
    {
        return abbreviatedId(commit.revCommit().getTree());
    }


    // ----------------------------------------------------------
    public String commitTreeURL()
    {
        GitWebContext newContext = gitContext().clone();
        newContext.setMode(GitWebMode.TREE);
        newContext.setObjectId(commit.revCommit());
        newContext.setPath(null);
        return newContext.toURL(context());
    }


    // ----------------------------------------------------------
    public String parentCommitId()
    {
        return abbreviatedId(parentCommit.revCommit());
    }


    // ----------------------------------------------------------
    public String parentCommitURL()
    {
        GitWebContext newContext = gitContext().clone();
        newContext.setMode(GitWebMode.COMMIT);
        newContext.setObjectId(parentCommit.revCommit());
        return newContext.toURL(context());
    }


    // ----------------------------------------------------------
    private String abbreviatedId(ObjectId id)
    {
        return id.abbreviate(20).name();
    }


    //~ Static/instance variables .............................................

    private GitCommit previousCommit;
}
