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

import org.webcat.core.git.GitRef;
import org.webcat.core.git.GitRepository;
import org.webcat.ui.WCTreeModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

//-------------------------------------------------------------------------
/**
 * A tree model that displays a list of repositories at the top level and the
 * repositories' refs (tags and branches) as their children.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class RepositoryRefModel extends WCTreeModel
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public RepositoryRefModel(NSArray<? extends EOEnterpriseObject> providers)
    {
        this.providers = providers;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public NSArray<?> childrenOfObject(Object object)
    {
        if (object == null)
        {
            return providers;
        }
        else if (object instanceof EOEnterpriseObject)
        {
            EOEnterpriseObject provider = (EOEnterpriseObject) object;
            GitRepository repository =
                GitRepository.repositoryForObject(provider);

            NSMutableArray<GitRef> refs = new NSMutableArray<GitRef>();
            refs.addObjectsFromArray(repository.headRefs());
            refs.addObjectsFromArray(repository.tagRefs());
            return refs;
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public void setSelectionFromEntryRef(RepositoryEntryRef entryRef,
            EOEditingContext ec, User user)
    {
        entryRef.resolve(ec);

        RepositoryProvider provider = (RepositoryProvider) entryRef.provider();
        if (provider.userCanAccessRepository(user))
        {
            setSelectedObject(entryRef.ref());
        }
        else
        {
            clearSelection();
        }
    }


    //~ Static/instance variables .............................................

    private NSArray<? extends EOEnterpriseObject> providers;
}
