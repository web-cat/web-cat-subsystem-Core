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

import java.io.File;
import java.io.IOException;

//-------------------------------------------------------------------------
/**
 * <p>
 * An interface implemented by EOs that want to provide their own file
 * repositories.
 * </p><p>
 * In addition to these methods, any class implementing this interface must
 * also provide the following static methods (where {@code [Type]} represents
 * the concrete EO type):
 * </p>
 * <dl>
 * <dt>{@code [Type] objectWithRepositoryIdentifier(String, EOEditingContext)}</dt>
 * <dd>This method should process the repository identifier specified by the
 * first parameter, then fetch and return the actual EO that represents that
 * repository. Note that the search performed by this method can be more
 * flexible than just a 1:1 mapping of the names returned by
 * {@link #repositoryIdentifier()}. For example, while it would accept "VT.hokie" as an
 * authdomain/user combo, it may also accept just the username "hokie".</dd>
 * <dt>{@code NSArray<[Type]> repositoriesPresentedToUser(User, EOEditingContext)}</dt>
 * <dd>This method should return an array of all EOs that have repositories
 * that the given user may access. This is used to provide a repository list on
 * the user's profile page, as well as to provide the virtual root directory
 * for WebDAV access.</dd>
 * </dl>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public interface RepositoryProvider
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the unique identifier that should be used for the repository. For
     * example, a User would use a combination of their authentication domain
     * and user name, while a Course might use the department name and course
     * number.
     *
     * @return the unique identifier for the repository
     */
    public String repositoryIdentifier();


    // ----------------------------------------------------------
    /**
     * Allows the object to initialize default repository contents the first
     * time the repository is created. Typically this would be something like
     * a README file with a descriptive welcome message.
     *
     * @param location the file system location where the files should be
     *     created; subdirectories are permitted
     * @throws IOException if an I/O error occurs
     */
    public void initializeRepositoryContents(File location) throws IOException;


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the specified user is permitted to
     * access this repository. For example, a user would only be allowed to
     * access his or her own repository, but any staff members of a course
     * would be allowed to access the repository for that course.
     *
     * @param user the user to check for access
     * @return true if the user can access the repository, otherwise false
     */
    public boolean userCanAccessRepository(User user);
}
