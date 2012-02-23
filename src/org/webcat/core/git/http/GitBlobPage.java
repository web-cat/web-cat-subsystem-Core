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

package org.webcat.core.git.http;

import org.webcat.core.FileUtilities;
import org.webcat.core.git.GitCommit;
import org.webcat.core.git.GitUtilities;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class GitBlobPage extends GitWebComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitBlobPage(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public NSArray<GitCommit> commits;


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
        return gitContext().repository().stringContentForBlob(
                gitContext().objectId());
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
}
