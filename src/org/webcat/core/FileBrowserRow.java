/*==========================================================================*\
 |  $Id: FileBrowserRow.java,v 1.4 2013/10/09 00:02:02 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2008 Virginia Tech
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

package org.webcat.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXFileUtilities;

//-------------------------------------------------------------------------
/**
 *  One row in a directory contents table.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.4 $, $Date: 2013/10/09 00:02:02 $
 */
public class FileBrowserRow
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new FileBrowserRow object.
     *
     * @param context the context for this component instance
     */
    public FileBrowserRow( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public Boolean         isExpanded     = null;
    public boolean         isEditable     = false;
    public boolean         allowSelection = false;
    public boolean         isLast         = true;
    public File            file;
    public File            baseFile;
    public int             depth          = 0;
    public int             myRowNumber    = -1;
    public int             index;
    public Integer         initialExpansionDepth;
    public boolean         allowSelectDir        = false;
    public NSArray<String> allowSelectExtensions = null;
    public boolean         applyChangesOnMod     = false;
    public String          currentSelection;
    public String          paneId;
    public String          formId;
    public String          buttonGroup;
    public String          alsoRefresh;
    public NSArray<String> focusedFiles;

    // For the spacer repetition
    public int spacerIndex;
    public NSMutableArray<IsLastEntryAtThisLevel> isLastEntry;
    public IsLastEntryAtThisLevel spacerWalker;

    // For the sub-file repetition
    public int           subFileIndex;
    public NSArray<File> contents = emptyContents;
    public File          subFileWalker;

    public FileBrowser.FileSelectionListener fileSelectionListener = null;
    public EditFilePage.FileEditListener     fileEditListener      = null;


    //~ Methods ...............................................................

    private static class NotDotOrDotDot
        implements FilenameFilter
    {
        public boolean accept(File file, String name)
        {
            return !name.equals(".")
                && !name.equals("..");
        }
    }
    private static final FilenameFilter notDotOrDotDot = new NotDotOrDotDot();
    private static final NSArray<File> emptyContents = new NSArray<File>();


    // ----------------------------------------------------------
    /**
     * Adds to the response of the page
     *
     * @param response The response being built
     * @param context  The context of the request
     */
    public void appendToResponse(WOResponse response, WOContext context)
    {
        contents = emptyContents;
        myRowNumber = index;

        // Check to make sure we expand if we contain the current selection
        if (isExpanded == null && currentSelection != null)
        {
            String prunedPath = pruneBaseFrom(file, true);

            if (log.isDebugEnabled())
            {
                log.debug("checking row: " + prunedPath
                    + " against current selection: " + currentSelection);
            }

            if (currentSelection.startsWith(prunedPath))
            {
                isExpanded = true;
            }
        }

        // Otherwise, expand if we contain any of the other focused files
        if ((isExpanded == null || !isExpanded)
            && focusedFiles != null
            && focusedFiles.size() > 0)
        {
            String prunedPath = pruneBaseFrom(file, false);

            for (String oneFocusedFile : focusedFiles)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("checking row: " + prunedPath
                        + " against focused file: " + oneFocusedFile);
                }
                if (oneFocusedFile.startsWith(prunedPath))
                {
                    isExpanded = true;
                    if (oneFocusedFile.equals(prunedPath))
                    {
                        focusedFiles.remove(oneFocusedFile);
                    }
                    break;
                }
            }
        }

        // Otherwise, check the expansion depth setting
        if (isExpanded == null)
        {
            if (initialExpansionDepth != null)
            {
                log.debug("initialExpansionDepth = " + initialExpansionDepth);
                log.debug("my depth = " + depth );
                isExpanded = (initialExpansionDepth.intValue() >= depth);
                log.debug("isExpanded = " + isExpanded);
            }
            else
            {
//                isExpanded = !isArchive();
                isExpanded = false;
            }
        }
        if (isLastEntry == null)
        {
            isLastEntry = new NSMutableArray<IsLastEntryAtThisLevel>();
        }
        while (isLastEntry.count() <= depth)
        {
            isLastEntry.addObject(new IsLastEntryAtThisLevel());
        }
        isLastEntry.get(depth).last = isLast;
        if (isExpanded)
        {
            if (file.isDirectory())
            {
                NSMutableArray<File> files =
                    new NSMutableArray<File>(file.listFiles(notDotOrDotDot));
                ERXArrayUtilities.sortArrayWithKey(files, "name");
                contents = files;
            }
            else
            {
                // Don't handle zip/jar archives yet!
            }
        }
        else
        {
            contents = emptyContents;
        }
        super.appendToResponse(response, context);
        while (isLastEntry.count() > depth)
        {
            isLastEntry.removeLastObject();
        }
    }


    // ----------------------------------------------------------
    public int innerDepth()
    {
        return depth + 1;
    }


    // ----------------------------------------------------------
    public boolean currentEntryIsLastAtItsLevel()
    {
        return (subFileIndex == contents.count() - 1);
    }


    // ----------------------------------------------------------
    /**
     * A mutable boolean value holder for storing in the
     * isLastEntry array.
     */
    public class IsLastEntryAtThisLevel
    {
        public boolean last = false;
    }


    // ----------------------------------------------------------
    /**
     * Return this row's number.
     * @return this row's number
     */
    public int rowNumber()
    {
        return myRowNumber;
    }


    // ----------------------------------------------------------
    /**
     * Return true on the final spacer rep.
     * @return true for the last spacer in a sequence
     */
    public boolean isFinalSpacerRep()
    {
        return spacerIndex == depth;
    }


    // ----------------------------------------------------------
    /**
     * Return true if this file represents an archive file.
     * @return true if this file is a zip or jar archive
     */
    public boolean isArchive()
    {
        String name = file.getName().toLowerCase();
        return file.isFile() &&
            (name.endsWith(".zip") || name.endsWith(".jar"));
    }


    // ----------------------------------------------------------
    /**
     * Return true if this file represents an archive or a directory.
     * @return true if this file is a directory or archive file
     */
    public boolean isDirectory()
    {
        return file.isDirectory(); // || isArchive();
    }


    // ----------------------------------------------------------
    /**
     * Determine if a file can be viewed inline.
     * @return true if the file can be viewed in the browser
     */
    public boolean isViewable()
    {
        return FileUtilities.showInline(file);
    }


    // ----------------------------------------------------------
    /**
     * Determine if a file can be edited in a browser window.
     * @return true if this file can be  edited as a text file
     */
    public boolean canEdit()
    {
        return isEditable && FileUtilities.isEditable(file);
    }


    // ----------------------------------------------------------
    /**
     * Determine if a file can be deleted.
     * @return true if this file can be  deleted
     */
    public boolean canDelete()
    {
        return isEditable;
    }


    // ----------------------------------------------------------
    /**
     * View the selected file.
     * @return a view page for the selected file
     */
    public WOComponent viewFile()
    {
        DeliverFile nextPage = (DeliverFile)pageWithName(
            DeliverFile.class.getName());
        nextPage.setFileName(file);
        nextPage.setContentType(FileUtilities.mimeType(file));
        nextPage.setStartDownload(!FileUtilities.showInline(file));
        return nextPage;
    }


    // ----------------------------------------------------------
    /**
     * Edit the selected file.
     * @return an edit page for the selected file
     */
    public WOComponent editFile()
    {
        if (applyChangesOnMod)
        {
            // TODO: how should the call to commitLocalChanges() be fixed?
            ((Session)session()).commitSessionChanges();
        }
        EditFilePage nextPage = (EditFilePage)pageWithName(
            EditFilePage.class.getName());
        nextPage.file = file;
        nextPage.baseFile = baseFile;
        nextPage.nextPage = (WCComponent)context().page();
        nextPage.fileEditListener = fileEditListener;
        return nextPage;
    }


    // ----------------------------------------------------------
    /**
     * Delete the selected file.
     * @return the current page, which will be reloaded
     */
    public WOActionResults deleteFile()
    {
        return new ConfirmingAction(this, true)
        {
            @Override
            protected String confirmationTitle()
            {
                return "Delete " + file.getName() + "?";
            }

            @Override
            protected String confirmationMessage()
            {
                String message = "Are you sure you want to delete ";
                if (isArchive())
                {
                    message += "the archive file " + file.getName()
                    + " and all its contents?";
                }
                else if (file.isDirectory())
                {
                    message += "the folder " + file.getName()
                    + " and all its contents?";
                }
                else
                {
                    message += "the file " + file.getName() + "?";
                }
                message += "  This action cannot be undone.";
                return message;
            }

            @Override
            protected WOActionResults actionWasConfirmed()
            {
                if (applyChangesOnMod)
                {
                    // TODO: how should commitLocalChanges() call be fixed?
                    ((Session)session()).commitSessionChanges();
                }
                log.debug("delete: " + file.getPath());
                if (file.isDirectory())
                {
                    ERXFileUtilities.deleteDirectory(file);
                }
                else
                {
                    file.delete();
                }
                return new JavascriptGenerator().refresh(paneId);
            }

            // Can't get ajax-based action to work!
//            @Override
//            protected void actionWasConfirmed(JavascriptGenerator page)
//            {
//                if (applyChangesOnMod)
//                {
//                    // TODO: how should commitLocalChanges() call be fixed?
//                    ((Session)session()).commitSessionChanges();
//                }
//                log.debug("delete: " + file.getPath());
//                if (file.isDirectory())
//                {
//                    ERXFileUtilities.deleteDirectory(file);
//                }
//                else
//                {
//                    file.delete();
//                }
//                page.refresh(paneId);
//            }
        };
    }


    // ----------------------------------------------------------
    public String deleteFileTitle()
    {
        if (isArchive())
        {
            return "Delete this archive and its contents";
        }
        else if (file.isDirectory())
        {
            return "Delete this directory and its contents";
        }
        else
        {
            return "Delete this file";
        }
    }


    // ----------------------------------------------------------
    public NSTimestamp lastModified()
    {
        if (lastModified == null)
        {
            lastModified = new NSTimestamp(file.lastModified());
        }
        return lastModified;
    }


    // ----------------------------------------------------------
    /**
     * View or download the selected file.
     * @return a download page for the selected file
     * @throws java.io.IOException if an error occurs reading the file
     */
    public WOComponent downloadFile()
        throws java.io.IOException
    {
        DeliverFile nextPage = (DeliverFile)pageWithName(
            DeliverFile.class.getName());
        if (file.isDirectory())
        {
            File zipFile = new File(file.getName() + ".zip");
            nextPage.setFileName(zipFile);
            nextPage.setContentType(FileUtilities.mimeType(zipFile));
            ByteArrayOutputStream boas = new ByteArrayOutputStream();
            ZipOutputStream       zos  = new ZipOutputStream(boas);
            FileUtilities.appendToZip(
                file,
                zos,
                file.getCanonicalPath().length());
            zos.close();
            nextPage.setFileData(new NSData(boas.toByteArray()));
            boas.close();
        }
        else
        {
            nextPage.setFileName(file);
            nextPage.setContentType(FileUtilities.mimeType(file));
        }
        nextPage.setStartDownload(true);
        return nextPage;
    }


    // ----------------------------------------------------------
    /**
     * Return the URL for the download icon to use for this file.
     * @return the Core framework file name for the desired download
     *  icon, based on whether or not this file is a directory
     */
    public String downloadIcon()
    {
        if (file.isDirectory())
        {
            return "icons/archive.gif";
        }
        else
        {
            return "icons/download.gif";
        }
    }


    // ----------------------------------------------------------
    /**
     * Toggle this entry between open and closed, if it is a directory.
     * @return this page, so that it reloads
     */
    public WOActionResults toggleExpansion()
    {
        isExpanded = (!isExpanded.booleanValue() && !isArchive());
        log.debug("toggleExpansion(): now isExpanded = " + isExpanded);
        JavascriptGenerator page = new JavascriptGenerator();
        page.refresh(paneId);
        if (alsoRefresh != null)
        {
            page.refresh(alsoRefresh);
        }
        return page;
    }


    // ----------------------------------------------------------
    /**
     * Return the URL for the icon representing this file's type.
     * @return the icon file URL
     */
    public String iconURL()
    {
        String result = FileUtilities.iconURL(file);
        log.debug("iconURL(" + file + ") = " + result);
        return result;
    }


    // ----------------------------------------------------------
    public WOComponent select()
    {
        WOComponent result = null;
        log.debug("select = " + file.getPath());
        if (fileSelectionListener != null)
        {
            log.debug("notifying fileSelectionListener");
            result = fileSelectionListener.selectFile(
                pruneBaseFrom(file, false));
        }
        else
        {
            log.debug("no fileSelectionListener registered");
        }
        return result;
    }


    // ----------------------------------------------------------
    public boolean isSelected()
    {
        boolean result = false;
        if (currentSelection != null)
        {
            String myPath = file.getPath().replaceAll("\\\\", "/");
            result = myPath.endsWith(currentSelection);
            log.debug("comparing " + myPath + " with " + currentSelection
                + " = " + result);
        }
        else
        {
            log.debug("isSelected(): current selection is null");
        }
        return result;
    }


    // ----------------------------------------------------------
    public boolean canSelectThis()
    {
        boolean result = false;
        if (allowSelection) // && !isSelected())
        {
            if (file.isDirectory())
            {
                result = allowSelectDir;
            }
            else if (allowSelectExtensions == null)
            {
                result = true;
            }
            else
            {
                result = allowSelectExtensions.contains(
                    FileUtilities.extensionOf(file).toLowerCase());
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    public String pruneBaseFrom(File aFile, boolean trimParent)
    {
        String path = aFile.getPath();
        if (baseFile != null)
        {
            String parent = trimParent
                ? baseFile.getParentFile().getParent()
                : baseFile.getParent();
            if (parent != null && parent.length() > 0)
            {
                path = path.substring(parent.length() + 1);
            }
        }
        path = path.replaceAll("\\\\", "/");
        return path;
    }


    //~ Instance/static variables .............................................

    private NSTimestamp lastModified;
    static Logger log = Logger.getLogger(FileBrowserRow.class);
}
