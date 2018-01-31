/*==========================================================================*\
 |  $Id: GitCommitPage.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
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

import java.io.IOException;
import org.webcat.core.FileUtilities;
import org.webcat.core.InlineStatusIndicator;
import org.webcat.core.git.GitCommit;
import org.webcat.core.git.GitRef;
import org.webcat.core.git.PrettyDiffFormatter;
import org.webcat.core.git.PrettyDiffResult;
import org.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class GitCommitPage
    extends GitWebComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitCommitPage(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public GitCommit commit;

    public String newTagName;

    public NSArray<String> modifiedPaths;
    public String aPath;
    public int index;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (commit == null)
        {
            commit = gitContext().repository().commitsWithId(
                    gitContext().headObjectId(), null).objectAtIndex(0);

            diffFormatter = new PrettyDiffFormatter();
            diffFormatter.setRepository(gitContext().repository().repository());
            diffFormatter.setContext(3);

            try
            {
                if (commit.parent() != null)
                {
                    diffFormatter.format(
                            commit.parent().revCommit(), commit.revCommit());
                }
                else
                {
                    diffFormatter.format(commit.revCommit());
                }
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            modifiedPaths = diffFormatter.modifiedPaths();
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public NSArray<GitCommit> commits()
    {
        return new NSArray<GitCommit>(commit);
    }


    // ----------------------------------------------------------
    public String iconURLForPath()
    {
        return FileUtilities.iconURL(aPath);
    }


    // ----------------------------------------------------------
    public PrettyDiffResult diffResults()
    {
        return diffFormatter.prettyDiffForPath(aPath);
    }


    // ----------------------------------------------------------
    public int totalAdditionCount()
    {
        return diffFormatter.totalAdditionCount();
    }


    // ----------------------------------------------------------
    public int totalDeletionCount()
    {
        return diffFormatter.totalDeletionCount();
    }


    // ----------------------------------------------------------
    public String abbreviatedCommitId()
    {
        return gitContext().headObjectId().abbreviate(6).name();
    }


    // ----------------------------------------------------------
    public String viewFileURL()
    {
        // FIXME
        return "#";
    }


    // ----------------------------------------------------------
    public JavascriptGenerator createNewTag()
    {
        GitRef tagRef = gitContext().repository().createTagForObject(
                newTagName, gitContext().objectId());

        JavascriptGenerator js = new JavascriptGenerator();

        if (tagRef != null)
        {
            InlineStatusIndicator.updateWithState(js, "createTagStatus",
                    InlineStatusIndicator.SUCCESS,
                    "The tag <strong>" + newTagName + "</strong> "
                    + "was created successfully.");

            js.dijit("createTagForm").call("reset");
            js.dijit("createTagButton").call("closeDropDown");
        }
        else
        {
            InlineStatusIndicator.updateWithState(js, "createTagStatus",
                    InlineStatusIndicator.ERROR,
                    "The tag <strong>" + newTagName + "</strong> could not be "
                    + "created. It may already exist, or the name was "
                    + "invalid.");
        }

        return js;
    }


    //~ Static/instance variables .............................................

    private PrettyDiffFormatter diffFormatter;
}
