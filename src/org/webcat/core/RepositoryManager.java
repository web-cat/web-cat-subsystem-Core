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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSComparator.ComparisonException;

//-------------------------------------------------------------------------
/**
 * A class that provides methods for accessing file repositories maintained by
 * EOs.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class RepositoryManager
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new {@code RepositoryManager}.
     */
    private RepositoryManager()
    {
        repositoryProviders = new NSMutableArray<String>();

        for (EOModel model : EOModelGroup.defaultGroup().models())
        {
            for (EOEntity entity : model.entities())
            {
                String className = entity.className();

                try
                {
                    Class<?> klass = Class.forName(className);

                    if (RepositoryProvider.class.isAssignableFrom(klass))
                    {
                        repositoryProviders.addObject(entity.name());
                    }
                }
                catch (Exception e)
                {
                    // Do nothing.
                }
            }
        }

        try
        {
            repositoryProviders.sortUsingComparator(
                    NSComparator.AscendingCaseInsensitiveStringComparator);
        }
        catch (ComparisonException e)
        {
            // Do nothing.
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the sole instance of the repository manager.
     *
     * @return the repository manager
     */
    public static RepositoryManager getInstance()
    {
        if (instance == null)
        {
            instance = new RepositoryManager();
        }

        return instance;
    }


    // ----------------------------------------------------------
    /**
     * Gets the full repository name for the specified object. The repository
     * name is represented as "[entity type]/[repository id of object]".
     *
     * @param object the object
     * @return the full repository name
     */
    public String repositoryNameForObject(EOEnterpriseObject object)
    {
        if (!(object instanceof RepositoryProvider))
        {
            return null;
        }
        else
        {
            RepositoryProvider rp = (RepositoryProvider) object;

            return object.entityName() + "/" + rp.repositoryIdentifier();
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the EO that has the specified repository identifier.
     *
     * @param entityName the type of EO to get
     * @param repositoryId the unique repository identifier
     * @param ec the editing context
     * @return the EO that has the file repository with the specified
     *     identifier, or null if there isn't one
     * @throws EOUtilities.MoreThanOneException if more than one object was
     *     found
     */
    public EOEnterpriseObject objectWithRepositoryId(
            String entityName, String repositoryId,
            EOEditingContext ec) throws EOUtilities.MoreThanOneException
    {
        try
        {
            EOEntity entity =
                EOUtilities.entityNamed(ec, entityName);
            String className = entity.className();

            Class<?> klass = Class.forName(className);
            Method method = klass.getMethod("objectWithRepositoryIdentifier",
                    String.class, EOEditingContext.class);

            return (EOEnterpriseObject) method.invoke(null, repositoryId, ec);
        }
        catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof
                    EOUtilities.MoreThanOneException)
            {
                throw (EOUtilities.MoreThanOneException) e.getTargetException();
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets an array of all repositories that the specified user can access.
     *
     * @param user the user
     * @param ec the editing context
     * @return an array of all repositories that the specified user can access
     */
    public NSArray<? extends EOEnterpriseObject> repositoriesPresentedToUser(
            User user, EOEditingContext ec)
    {
        NSMutableArray<EOEnterpriseObject> providers =
            new NSMutableArray<EOEnterpriseObject>();

        for (String entity : repositoryProviders)
        {
            NSArray<? extends EOEnterpriseObject> providersForEntity =
                repositoriesPresentedToUser(entity, user, ec);

            providers.addObjectsFromArray(providersForEntity);
        }

        try
        {
            providers.sortUsingComparator(new NSComparator() {
                @Override
                public int compare(Object _lhs, Object _rhs)
                {
                    EOEnterpriseObject lhs = (EOEnterpriseObject) _lhs;
                    EOEnterpriseObject rhs = (EOEnterpriseObject) _rhs;

                    return repositoryNameForObject(lhs).compareTo(
                            repositoryNameForObject(rhs));
                }
            });
        }
        catch (ComparisonException e)
        {
            // Do nothing.
        }

        return providers;
    }


    // ----------------------------------------------------------
    /**
     * Gets an array of all repositories for the specified entity that a user
     * can access.
     *
     * @param entityName the entity name
     * @param user the user
     * @param ec the editing context
     * @return an array of all repositories of the specified entity type that
     *     the user can access
     */
    public NSArray<? extends EOEnterpriseObject> repositoriesPresentedToUser(
            String entityName, User user, EOEditingContext ec)
    {
        try
        {
            EOEntity entity =
                EOUtilities.entityNamed(ec, entityName);
            String className = entity.className();

            Class<?> klass = Class.forName(className);
            Method method = klass.getMethod("repositoriesPresentedToUser",
                    User.class, EOEditingContext.class);

            @SuppressWarnings("unchecked")
            NSArray<? extends EOEnterpriseObject> result =
                (NSArray<? extends EOEnterpriseObject>) method.invoke(
                    null, user, ec);
            return result;
        }
        catch (Exception e)
        {
            return NSArray.<EOEnterpriseObject> emptyArray();
        }
    }


    //~ Static/instance variables .............................................

    private static RepositoryManager instance;

    private NSMutableArray<String> repositoryProviders;
}
