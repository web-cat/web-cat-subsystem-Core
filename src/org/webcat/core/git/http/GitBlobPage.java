/*==========================================================================*\
 |  $Id: GitBlobPage.java,v 1.4 2012/03/28 13:48:08 stedwar2 Exp $
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Repository;
import org.webcat.core.FileUtilities;
import org.webcat.core.git.GitCommit;
import org.webcat.core.git.GitUtilities;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORedirect;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.4 $, $Date: 2012/03/28 13:48:08 $
 */
public class GitBlobPage
    extends GitWebComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitBlobPage(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public NSArray<GitCommit> commits;

    public String blobContentString;

    public String commitMessageForChanges;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (commits == null)
        {
            commits = gitContext().repository().commitsWithId(
                    gitContext().headObjectId(), gitContext().path());
        }

        if (commitMessageForChanges == null)
        {
            commitMessageForChanges = DEFAULT_COMMIT_MESSAGE;
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public String mimeType()
    {
        if (gitContext().path() != null)
        {
            return FileUtilities.mimeType(gitContext().path());
        }
        else
        {
            return "text/plain";
        }
    }


    // ----------------------------------------------------------
    public boolean isImage()
    {
        return mimeType().startsWith("image");
    }


    // ----------------------------------------------------------
    public NSData blobContent()
    {
        return gitContext().repository().contentForBlob(
                gitContext().objectId());
    }


    // ----------------------------------------------------------
    public String blobContentString()
    {
        if (blobContentString == null)
        {
            blobContentString = gitContext().repository().stringContentForBlob(
                    gitContext().objectId());
        }

        return blobContentString;
    }


    // ----------------------------------------------------------
    public void setBlobContentString(String newContent)
    {
        blobContentString = newContent;
    }


    // ----------------------------------------------------------
    public String blobMimeType()
    {
        return FileUtilities.mimeType(gitContext().lastPathComponent());
    }


    // ----------------------------------------------------------
    public String historyURL()
    {
        GitWebContext newContext = gitContext().clone();
        newContext.setMode(GitWebMode.COMMITS);
        return newContext.toURL(context());
    }


    // ----------------------------------------------------------
    public String rawURL()
    {
        GitWebContext newContext = gitContext().clone();
        newContext.setMode(GitWebMode.RAW);
        return newContext.toURL(context());
    }


    // ----------------------------------------------------------
    public WOActionResults commitChanges()
    {
        if (commitMessageForChanges == null ||
            commitMessageForChanges.length() == 0)
        {
            error("Please provide a commit message for your file upload");
        }
        else
        {
            Repository workingCopy = GitUtilities.workingCopyForRepository(
                gitContext().repository().repository(), true);
            File file = new File(workingCopy.getWorkTree(), gitContext().path());

            try
            {
                FileWriter writer = new FileWriter(file);
                writer.write(blobContentString);
                writer.close();

                GitUtilities.pushWorkingCopyImmediately(
                    workingCopy, user(), commitMessageForChanges);
            }
            catch (IOException e)
            {
                log.error("error committing changes to " + file + ": ", e);
                error("The following error occurred trying to commit the "
                    + "file \"" + gitContext().path() + "\": "
                    + e.getMessage());
            }
        }

        // Refresh the page but do a redirect to make sure we get the proper
        // URL in the address bar.
        WORedirect redirect = new WORedirect(context());
        redirect.setUrl(gitContext().toURL(context()));
        return redirect.generateResponse();
    }


    //~ Instance/static variables .............................................

    private static final String DEFAULT_COMMIT_MESSAGE =
        "Updated from my web browser";

    private static Logger log = Logger.getLogger(GitBlobPage.class);
}
