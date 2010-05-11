/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.webcat.core.vfs.IDeletableFile;
import org.webcat.core.vfs.IModifiableContainer;
import org.webcat.core.vfs.IReadableFile;
import org.webcat.core.vfs.IWritableFile;
import org.webcat.core.vfs.WCFile;
import org.webcat.core.vfs.WCRootedFolder;
import org.webcat.ui.WCDialog;
import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.ComponentIDGenerator;
import org.webcat.ui.util.JSHash;
import org.webcat.core.FileUtilities;
import org.webcat.core.ValidatingAction;
import org.webcat.core.WCComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

//--------------------------------------------------------------------------
/**
 * A component that pops up a dialog that allows the user to select and/or
 * manage files.
 *
 * <h2>Bindings</h2>
 *
 * <dl>
 * <dt>id</dt>
 * <dd>The Dijit identifier of the dialog that will be created. To display the
 * dialog, call <tt>dijit.byId("this id").show()</tt>.</dd>
 *
 * <dt>title</dt>
 * <dd>The title to display in the dialog.</dd>
 *
 * <dt>root</dt>
 * <dd>A {@link org.webcat.core.vfs.WCFile} object representing the root of
 * the file area that can be navigated in this dialog. This might be, for
 * example, the location of a user's script data files. For security reasons,
 * the user will not be permitted to navigate above this root.</dd>
 *
 * <dt>selectedFile</dt>
 * <dd>The file that was selected in the dialog. This binding will be read when
 * the dialog is displayed to select the item and will be updated when the
 * dialog is dismissed.</dd>
 *
 * <dt>showFoldersOnly</dt>
 * <dd>A Boolean value indicating whether the dialog will only show folders or
 * if it will show files as well. Defaults to false. If true, this also forces
 * <tt>canSelectFolders</tt> to be true.</dd>
 *
 * <dt>canSelectFolders</dt>
 * <dd>A Boolean value indicating whether the dialog can be dismissed with a
 * folder as the selection, or if only files can be selected. Defaults to false
 * (only files can be selected).</dd>
 *
 * </dl>
 *
 * @author Tony Allevato
 * @version $Id$
 */
public class FileBrowserDialog extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public FileBrowserDialog(WOContext context)
    {
        super(context);
    }


    //~ External KVC attributes (must be public) ..............................

    public String id;
    public String title;
    public WCFile root;
    public WCFile selectedFile;
    public boolean showFoldersOnly;
    public boolean canSelectFolders;


    //~ Internal KVC attributes (must be public) ..............................

    public NSArray<WCFile> ancestorLocations;
    public WCFile oneAncestorLocation;
    public WCFile currentLocation;
    public NSArray<WCFile> filesAtCurrentLocation;
    public WCFile oneFile;
    public int fileIndex;

    public String newFolderName;
    public String createNewFolderOkButtonId;

    public NSData uploadedFileData;
    public String uploadedFilename;
    public boolean mustRenameUpload;
    public boolean expandArchiveContents;
    public String uploadFileDialogOkButtonId;

    public ComponentIDGenerator idFor;


    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (showFoldersOnly)
        {
            canSelectFolders = true;
        }

        idFor = new ComponentIDGenerator(this);

        //        baseDir = GradingPlugin.userScriptDirName( user(), true );
        String scriptDataRoot = org.webcat.core.Application
            .configurationProperties()
            .getProperty( "grader.scriptsdataroot" );
        StringBuffer dir = new StringBuffer( 50 );
        dir.append( scriptDataRoot );
        dir.append( '/' );
        dir.append( user().authenticationDomain().subdirName() );
        dir.append( '/' );
        dir.append( user().userName() );
        //baseDir = new File(dir.toString());
        root = new WCRootedFolder("/Users/allevato/Documents");

        setCurrentLocation(root);

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    private JavascriptGenerator fileListContainerRefresher()
    {
        return new JavascriptGenerator()
            .block(idFor.get("fileListContainer"))
            .refresh(idFor.get("fileListContainer"));
    }


    // ----------------------------------------------------------
    private JavascriptGenerator ioExceptionAlert(String messagePrefix,
            IOException e)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<p>");
        buffer.append(messagePrefix);
        buffer.append(" because the following I/O error occurred:</p>");
        buffer.append("<p style='");
        buffer.append("padding: 4px 8px; ");
        buffer.append("border: 1px solid red; ");
        buffer.append("background-color: #FFE0E0; ");
        buffer.append("-moz-border-radius: 4px 4px; ");
        buffer.append("-webkit-border-radius: 4px 4px;'>");
        buffer.append(WOMessage.stringByEscapingHTMLString(e.getMessage()));
        buffer.append("</p>");

        JSHash options = new JSHash();
        options.put("title", "Error");
        options.put("message", buffer.toString());

        return new JavascriptGenerator().alert(options);
    }


    // ----------------------------------------------------------
    public void setCurrentLocation(WCFile current)
    {
        currentLocation = current;

        NSMutableArray<WCFile> locations = new NSMutableArray<WCFile>();

        while (!current.isRoot())
        {
            locations.addObject(current);
            current = current.parent();
        }

        locations.addObject(current);
        ancestorLocations = locations;

        NSMutableArray<WCFile> files = new NSMutableArray<WCFile>();

        NSArray<WCFile> childFiles = currentLocation.children();
        for (WCFile childFile : childFiles)
        {
            files.addObject(childFile);
        }

        filesAtCurrentLocation = files;
    }


    // ----------------------------------------------------------
    public String displayStringForAncestorLocation()
    {
        return oneAncestorLocation.name();
    }


    // ----------------------------------------------------------
    public String iconURLOfOneFile()
    {
        return oneFile.iconURL();
    }


    // ----------------------------------------------------------
    public String nameOfOneFile()
    {
        return oneFile.name();
    }


    // ----------------------------------------------------------
    public String sizeOfOneFile()
    {
        return oneFile.humanReadableLength();
    }


    // ----------------------------------------------------------
    public String fileBrowserInfoOfOneFile()
    {
        JSONObject info = new JSONObject();

        try
        {
            JSONObject features = new JSONObject();

            boolean isReadable = (oneFile.adaptTo(IReadableFile.class) != null);
            boolean isWritable = (oneFile.adaptTo(IWritableFile.class) != null);
            boolean isDeletable = (oneFile.adaptTo(IDeletableFile.class) != null);
            boolean isContainer = oneFile.isContainer();
            boolean isEditableType = FileUtilities.isEditable(oneFile.name());

            if (isReadable && (!isWritable || !isEditableType))
            {
                features.put("view", true);
            }

            if (isReadable && isWritable && isEditableType)
            {
                features.put("edit", true);
            }

            if (isContainer || isReadable)
            {
                features.put("download", true);
            }

            if (isDeletable)
            {
                features.put("rename", true);
                features.put("delete", true);
            }

            info.put("features", features);
        }
        catch (JSONException e)
        {
            // Do nothing.
        }

        return info.toString();
    }


    // ----------------------------------------------------------
    public String modificationDateOfOneFile()
    {
        NSTimestamp modDate = oneFile.lastModified();

        NSTimestampFormatter fmt = new NSTimestampFormatter(
                user().dateFormat() + " " + user().timeFormat());
        return fmt.format(modDate);
    }


    // ----------------------------------------------------------
    public String selectedFileIndex()
    {
        return Integer.toString(selectedFileIndex);
    }


    // ----------------------------------------------------------
    public void setSelectedFileIndex(String value)
    {
        try
        {
            selectedFileIndex = Integer.parseInt(value);

            if (selectedFileIndex > 0 &&
                    selectedFileIndex < filesAtCurrentLocation.count())
            {
                selectedFile = filesAtCurrentLocation.objectAtIndex(
                        selectedFileIndex);
            }
            else
            {
                selectedFile = null;
            }
        }
        catch (NumberFormatException e)
        {
            selectedFileIndex = -1;
            selectedFile = null;
        }
    }


    // ----------------------------------------------------------
    public JavascriptGenerator okPressed()
    {
        return JavascriptGenerator.NO_OP;
    }


    // ----------------------------------------------------------
    public JavascriptGenerator fileWasDoubleClicked()
    {
        if (selectedFileIndex != -1)
        {
            WCFile selectedFile = filesAtCurrentLocation.objectAtIndex(
                    selectedFileIndex);

            if (selectedFile.isContainer())
            {
                setCurrentLocation(selectedFile);
            }
            else
            {
                // Do something appropriate when a file is double-clicked
                // (like dismiss the dialog).
            }
        }

        return fileListContainerRefresher()
            .refresh(idFor.get("ancestorListContainer"));
    }


    // ----------------------------------------------------------
    public JavascriptGenerator deleteSelectedFile()
    {
        if (selectedFileIndex != -1)
        {
            WCFile selectedFile = filesAtCurrentLocation.objectAtIndex(
                    selectedFileIndex);

            IDeletableFile df = selectedFile.adaptTo(IDeletableFile.class);

            JavascriptGenerator js;

            if (df != null)
            {
                try
                {
                    df.delete();
                    setCurrentLocation(currentLocation);
                    js = fileListContainerRefresher();
                }
                catch (IOException e)
                {
                    js = ioExceptionAlert("This file could not be deleted", e);
                }
            }
            else
            {
                js = new JavascriptGenerator().alert(
                        "Error", "This file cannot be deleted.");
            }

            return js;
        }

        return null;
    }


    // ----------------------------------------------------------
    public JavascriptGenerator ancestorWasSelected()
    {
        // currentLocation is already updated here, since it is bound to the
        // selection in the ancestor list.

        return fileListContainerRefresher()
            .refresh(idFor.get("ancestorListContainer"));
    }


    // ----------------------------------------------------------
    public JavascriptGenerator goToParent()
    {
        if (currentLocation.isRoot())
        {
            return new JavascriptGenerator().unblock(
                    idFor.get("fileListContainer"));
        }
        else
        {
            setCurrentLocation(currentLocation.parent());

            return fileListContainerRefresher()
                .refresh(idFor.get("ancestorListContainer"));
        }
    }


    // ----------------------------------------------------------
    public String validateNewFolderName()
    {
        if (currentLocation.childWithName(newFolderName) != null)
        {
            return "A file or folder with this name already exists.";
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public String validateUploadedFilename()
    {
        if (currentLocation.childWithName(uploadedFilename) != null)
        {
            return "A file or folder with this name already exists.";
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public ValidatingAction createNewFolder()
    {
        return new ValidatingAction(this)
        {
            protected void validationDidSucceed(JavascriptGenerator page)
            {
                IModifiableContainer mc = currentLocation.adaptTo(
                        IModifiableContainer.class);

                if (mc != null)
                {
                    WCFile newFolder = mc.createFolderWithName(newFolderName);
                    setCurrentLocation(currentLocation);

                    newFolderName = null;

                    page.block(idFor.get("fileListContainer"));
                    page.refresh(idFor.get("ancestorListContainer"),
                            idFor.get("fileListContainer"));

                    page.dijit(idFor.get("createFolderDialog")).call("hide");
                    page.dijit(createNewFolderOkButtonId).attr("disabled", false);
                }
            }

            protected void validationDidFail(JavascriptGenerator page)
            {
                page.dijit(createNewFolderOkButtonId).attr("disabled", false);
            }
        };
    }


    // ----------------------------------------------------------
    public JavascriptGenerator fileWasUploaded()
    {
        if (currentLocation.childWithName(uploadedFilename) != null)
        {
            mustRenameUpload = true;
        }
        else
        {
            mustRenameUpload = false;
        }

        return new JavascriptGenerator()
            .refresh(idFor.get("mustRenameContainer"));
    }


    // ----------------------------------------------------------
    public ValidatingAction saveUploadedFile()
    {
        return new ValidatingAction(this)
        {
            protected void validationDidSucceed(JavascriptGenerator page)
            {
                IModifiableContainer mc = currentLocation.adaptTo(
                        IModifiableContainer.class);

                if (mc != null)
                {
                    WCFile newFile = mc.createFileWithName(uploadedFilename);

                    if (newFile != null)
                    {
                        IWritableFile wf = newFile.adaptTo(IWritableFile.class);

                        try
                        {
                            wf.setData(uploadedFileData);
                        }
                        catch (IOException e)
                        {
                            // TODO delete the file
                        }

                        // Force a refresh of the file list.
                        setCurrentLocation(currentLocation);
                    }

                    uploadedFilename = null;
                    uploadedFileData = null;
                    expandArchiveContents = false;

                    page.block(idFor.get("fileListContainer"));
                    page.refresh(idFor.get("ancestorListContainer"),
                            idFor.get("fileListContainer"));

                    page.dijit(idFor.get("uploadFileDialog")).call("hide");
                    page.dijit(uploadFileDialogOkButtonId).attr("disabled", false);
                }
            }

            protected void validationDidFail(JavascriptGenerator page)
            {
                page.dijit(uploadFileDialogOkButtonId).attr("disabled", false);
            }
        };
    }


    private int selectedFileIndex;
}
