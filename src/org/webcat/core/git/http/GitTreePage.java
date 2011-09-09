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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.webcat.archives.ArchiveManager;
import org.webcat.archives.IWritableContainer;
import org.webcat.core.DeliverFile;
import org.webcat.core.FileUtilities;
import org.webcat.core.NSMutableDataOutputStream;
import org.webcat.core.git.GitTreeEntry;
import org.webcat.core.git.GitTreeIterator;
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
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class GitTreePage extends GitWebComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitTreePage(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public NSArray<GitTreeEntry> entries;

    public NSData fileDataToUpload;
    public String filePathToUpload;
    public String commitMessageForUpload;
    public boolean expandIfArchive;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (entries == null)
        {
            GitTreeIterator iterator;

            if (gitContext().path() == null
                    || gitContext().path().length() == 0)
            {
                iterator = new GitTreeIterator(gitContext().repository(),
                        gitContext().headObjectId());
            }
            else
            {
                iterator = new GitTreeIterator(gitContext().repository(),
                        gitContext().objectId());
            }

            entries = iterator.allEntries();
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public WOActionResults downloadTree()
    {
        DeliverFile file = new DeliverFile(context());

        String filename;
        if (gitContext().path() != null && gitContext().path().length() > 0)
        {
            filename = gitContext().lastPathComponent();
        }
        else
        {
            filename = gitContext().repositoryName();
        }

        ObjectId treeId = gitContext().objectId();
        if (treeId == null)
        {
            treeId = gitContext().headObjectId();
        }

        try
        {
            // FIXME right now this writes the tree to a zip file in memory.
            // Maybe a better approach would be to write it to a file in
            // temporary storage and then deliver that file.

            NSMutableDataOutputStream out = new NSMutableDataOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(out);
            IWritableContainer container =
                ArchiveManager.getInstance().writableContainerForZip(zipOut);

            gitContext().repository().copyItemToContainer(
                    treeId, null, container);

            container.finish();
            zipOut.close();

            file.setDeliveredName(filename + ".zip");
            file.setContentType("application/zip");
            file.setStartDownload(true);
            file.setFileData(out.data());

            return file;
        }
        catch (IOException e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public String readmeName()
    {
        if (!checkedForReadme)
        {
            for (GitTreeEntry entry : entries)
            {
                String name = entry.name();

                if (name.equalsIgnoreCase("readme")
                        || name.equalsIgnoreCase("readme.txt"))
                {
                    readmeEntry = entry;
                    break;
                }
            }

            checkedForReadme = true;
        }

        return readmeEntry != null ? readmeEntry.name() : null;
    }


    // ----------------------------------------------------------
    public String readmeContent()
    {
        String name = readmeName();

        if (name != null)
        {
            return readmeEntry.repository().stringContentForBlob(
                    readmeEntry.objectId());
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public WOActionResults uploadFile()
    {
        Repository workingCopy = GitUtilities.workingCopyForRepository(
                gitContext().repository().repository(), true);
        File file = workingCopy.getWorkTree();
        File destDir = file;

        if (gitContext().path() != null)
        {
            destDir = new File(file, gitContext().path());
        }

        File destFile = new File(destDir, filePathToUpload);

        try
        {
            if (expandIfArchive && FileUtilities.isArchiveFile(filePathToUpload))
            {
                ArchiveManager.getInstance().unpack(
                        destDir, filePathToUpload, fileDataToUpload.stream());
            }
            else
            {
                FileOutputStream os = new FileOutputStream(destFile);
                fileDataToUpload.writeToStream(os);
                os.close();
            }

            GitUtilities.pushWorkingCopyImmediately(
                    workingCopy, user(), commitMessageForUpload);
        }
        catch (IOException e)
        {
            log.error("The following error occurred while uploading the file: ",
                    e);

            // TODO Display the error to the user
        }

        // Refresh the page but do a redirect to make sure we get the proper
        // URL in the address bar.
        WORedirect redirect = new WORedirect(context());
        redirect.setUrl(gitContext().toURL(context()));
        return redirect.generateResponse();
    }


    //~ Static/instance variables .............................................

    private boolean checkedForReadme;
    private GitTreeEntry readmeEntry;

    private static final Logger log = Logger.getLogger(GitTreePage.class);
}
