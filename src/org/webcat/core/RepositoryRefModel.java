/*==========================================================================*\
 |  $Id$
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
public class RepositoryRefModel
    extends WCTreeModel<Object>
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public RepositoryRefModel(NSArray<? extends EOBase> providers)
    {
        this.providers = providers;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public NSArray<Object> childrenOfObject(Object object)
    {
        if (object == null)
        {
            @SuppressWarnings("unchecked")
            NSArray<Object> result = (NSArray<Object>)providers;
            return result;
        }
        else if (object instanceof EOBase)
        {
            EOBase provider = (EOBase) object;
            GitRepository repository =
                GitRepository.repositoryForObject(provider);

            NSMutableArray<GitRef> refs = new NSMutableArray<GitRef>();
            refs.addObjectsFromArray(repository.headRefs());
            refs.addObjectsFromArray(repository.tagRefs());

            @SuppressWarnings("unchecked")
            NSArray<Object> result = (NSArray)refs;
            return result;
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public String pathForObject(Object object)
    {
        if (object instanceof EOBase)
        {
            return ((EOBase) object).apiId();
        }
        else if (object instanceof GitRef)
        {
            GitRef ref = (GitRef) object;
            String provider = ref.repository().provider().apiId();

            return provider + "/" + ref.name().replace('/', '$');
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public Object childWithPathComponent(Object object, String component)
    {
        NSArray<?> children = childrenOfObject(object);

        if (object == null)
        {
            for (Object child : children)
            {
                EOBase obj = (EOBase) child;

                if (obj.apiId().equals(component))
                {
                    return child;
                }
            }
        }
        else if (object instanceof EOBase)
        {
            for (Object child : children)
            {
                GitRef ref = (GitRef) child;

                if (ref.name().equals(component.replace('$', '/')))
                {
                    return child;
                }
            }
        }

        return null;
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
