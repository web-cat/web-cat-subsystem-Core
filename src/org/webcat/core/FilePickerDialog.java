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

package org.webcat.core;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.webcat.core.git.GitCommit;
import org.webcat.core.git.GitRef;
import org.webcat.core.git.GitRepository;
import org.webcat.core.git.GitTreeEntry;
import org.webcat.core.git.GitTreeIterator;
import org.webcat.ui.WCTreeModel;
import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.ComponentIDGenerator;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * A modal dialog that lets the user choose a file from any of the repositories
 * that they have access to.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class FilePickerDialog extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public FilePickerDialog(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public String id;
    public String title;

    public RepositoryRefModel refModel;
    public Object repositoryItem;

    public RepositoryEntryModel entryModel;
    public GitTreeEntry entry;

    public RepositoryEntryRef initialSelection;

    public ComponentIDGenerator idFor = new ComponentIDGenerator(this);


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (id == null)
        {
            id = idFor.get();
        }

        updateRefModel();

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public String onLoad()
    {
        updateRefModel();
        return null;
    }


    // ----------------------------------------------------------
    private void updateRefModel()
    {
        NSArray<? extends EOEnterpriseObject> providers =
            RepositoryManager.getInstance().repositoriesPresentedToUser(
                    user(), localContext());

        refModel = new RepositoryRefModel(providers);

        if (initialSelection != null)
        {
            refModel.setSelectionFromEntryRef(initialSelection,
                    localContext(), user());
        }

        updateEntryModel();
    }


    // ----------------------------------------------------------
    private GitRef refForModelObject(Object object)
    {
        GitRef ref;

        if (object instanceof EOEnterpriseObject)
        {
            GitRepository repo = GitRepository.repositoryForObject(
                    (EOEnterpriseObject) object);
            ref = repo.refWithName(Constants.R_HEADS + Constants.MASTER);
        }
        else
        {
            ref = (GitRef) object;
        }

        return ref;
    }


    // ----------------------------------------------------------
    private void updateEntryModel()
    {
        NSSet<?> selectedRefs = refModel.selectedObjects();
        if (selectedRefs.isEmpty())
        {
            entryModel = new RepositoryEntryModel(null);
        }
        else
        {
            entryModel = new RepositoryEntryModel(
                    refForModelObject(selectedRefs.anyObject()))
            {
                @Override
                public boolean canSelectObject(GitTreeEntry entry)
                {
                    return delegate.fileCanBeSelected(
                            filePickerItemForEntry(entry), entry.isTree());
                }
            };

            if (initialSelection != null)
            {
                entryModel.setSelectionFromEntryRef(initialSelection,
                        localContext());
            }
        }
    }


    // ----------------------------------------------------------
    public boolean canSelectEntry()
    {
        return entryModel.canSelectObject(entry);
    }


    // ----------------------------------------------------------
    public String cssStyleForItem()
    {
        if (canSelectEntry())
        {
            return null;
        }
        else
        {
            return "opacity: 0.4;";
        }
    }


    // ----------------------------------------------------------
    public JavascriptGenerator refWasSelected()
    {
        updateEntryModel();

        JavascriptGenerator js = new JavascriptGenerator();
        js.refresh(idFor.get("entryTree"));
        return js;
    }


    // ----------------------------------------------------------
    public JavascriptGenerator entryWasDoubleClicked()
    {
        JavascriptGenerator js = new JavascriptGenerator();
        js.dijit(id).call("hide");
        js.append(okPressed());
        return js;
    }


    // ----------------------------------------------------------
    public String displayNameForRepositoryItem()
    {
        if (repositoryItem instanceof EOEnterpriseObject)
        {
            return RepositoryManager.getInstance().repositoryNameForObject(
                    (EOEnterpriseObject) repositoryItem);
        }
        else
        {
            return ((GitRef) repositoryItem).shortName();
        }
    }


    // ----------------------------------------------------------
    public String iconPathForRepositoryItem()
    {
        if (repositoryItem instanceof EOEnterpriseObject)
        {
            return "icons/filetypes/drawer.png";
        }
        else
        {
            return ((GitRef) repositoryItem).isTag() ?
                    "icons/tag.png" : "icons/node.png";
        }
    }


    // ----------------------------------------------------------
    public Long sizeForEntry()
    {
        return entry.isTree() ? null : entry.size();
    }


    // ----------------------------------------------------------
    public String iconPathForEntry()
    {
        return entry.isTree() ? "icons/filetypes/folder.png" :
            FileUtilities.iconURL(entry.name());
    }


    // ----------------------------------------------------------
    public NSTimestamp lastModifiedTimeForEntry()
    {
        /*GitRef ref = refForModelObject(refModel.selectedObjects().anyObject());

        NSArray<GitCommit> commits =
            entry.repository().commitsWithId(ref.objectId(), entry.path());

        if (commits != null && commits.count() > 0)
        {
            return commits.objectAtIndex(0).commitTime();
        }
        else*/
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public FilePickerDelegate delegate()
    {
        return delegate;
    }


    // ----------------------------------------------------------
    public void setDelegate(FilePickerDelegate delegate)
    {
        this.delegate = delegate;
    }


    // ----------------------------------------------------------
    private RepositoryEntryRef filePickerItemForEntry(GitTreeEntry entry)
    {
        NSSet<?> selectedRepos = refModel.selectedObjects();

        if (!selectedRepos.isEmpty())
        {
            GitRef ref = refForModelObject(selectedRepos.anyObject());

            String providerName = RepositoryManager.getInstance()
                .repositoryNameForObject(ref.repository().provider());

            String path = entry.path();

            if (entry.isTree())
            {
                path += "/";
            }

            RepositoryEntryRef item = new RepositoryEntryRef(
                    providerName, path, ref.name());

            return item;
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public JavascriptGenerator okPressed()
    {
        NSSet<GitTreeEntry> selectedEntries = entryModel.selectedObjects();

        if (selectedEntries.isEmpty())
        {
            return null;
        }
        else
        {
            GitTreeEntry entry = selectedEntries.anyObject();

            if (delegate != null)
            {
                return delegate.fileWasSelected(filePickerItemForEntry(entry));
            }
            else
            {
                return null;
            }
        }
    }


    //~ Static/instance variables .............................................

    private FilePickerDelegate delegate;
}
