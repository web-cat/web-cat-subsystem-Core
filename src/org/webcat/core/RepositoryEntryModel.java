/*==========================================================================*\
 |  $Id: RepositoryEntryModel.java,v 1.3 2012/11/29 16:31:59 aallowat Exp $
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

import java.util.Iterator;
import org.eclipse.jgit.lib.ObjectId;
import org.webcat.core.git.GitRef;
import org.webcat.core.git.GitTreeEntry;
import org.webcat.core.git.GitTreeIterator;
import org.webcat.ui.WCTreeModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

//-------------------------------------------------------------------------
/**
 * A tree model that represents the entries of a ref (branch or tag) in a
 * repository.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.3 $, $Date: 2012/11/29 16:31:59 $
 */
public class RepositoryEntryModel extends WCTreeModel<GitTreeEntry>
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public RepositoryEntryModel()
    {
        this.ref = null;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public GitRef ref()
    {
        return ref;
    }


    // ----------------------------------------------------------
    public void setRef(GitRef aRef)
    {
        this.ref = aRef;
        rearrangeObjects();
    }


    // ----------------------------------------------------------
    @Override
    public NSArray<GitTreeEntry> childrenOfObject(GitTreeEntry object)
    {
        ObjectId id;
        String pathPrefix = null;

        if (ref == null)
        {
            return null;
        }

        if (object == null)
        {
            id = ref.objectId();
        }
        else
        {
            id = object.objectId();
            pathPrefix = object.path();
        }

        NSArray<GitTreeEntry> entries = new GitTreeIterator(
                ref.repository(), id, pathPrefix).allEntries();

        Iterator<GitTreeEntry> it = entries.iterator();
        while (it.hasNext())
        {
            GitTreeEntry entry = it.next();

            if (".gitignore".equals(entry.name()))
            {
                it.remove();
            }
        }

        return entries;
    }


    // ----------------------------------------------------------
    public boolean objectHasArrangedChildren(GitTreeEntry object)
    {
        return object == null || object.isTree();
    }


    // ----------------------------------------------------------
    public String pathForObject(GitTreeEntry object)
    {
        return object.path();
    }


    // ----------------------------------------------------------
    public GitTreeEntry childWithPathComponent(GitTreeEntry object, String component)
    {
        NSArray<GitTreeEntry> entries = childrenOfObject(object);

        for (GitTreeEntry child : entries)
        {
            if (child.name().equals(component))
            {
                return child;
            }
        }

        return null;
    }


    // ----------------------------------------------------------
    public void setSelectionFromEntryRef(RepositoryEntryRef entryRef,
            EOEditingContext ec)
    {
        GitTreeEntry entry = null;
        String[] parts = entryRef.path().split("/");

        NSArray<GitTreeEntry> entries = arrangedChildrenOfObject(null);
        for (String segment : parts)
        {
            for (GitTreeEntry anEntry : entries)
            {
                if (anEntry.name().equals(segment))
                {
                    entry = anEntry;
                    entries = childrenOfObject(anEntry);
                    break;
                }
            }
        }

        setSelectedObject(entry);
    }


    //~ Static/instance variables .............................................

    private GitRef ref;
}
