/*==========================================================================*\
 |  $Id: RepositoryProvider.java,v 1.2 2012/06/22 16:23:18 aallowat Exp $
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
 * repositories. The API identifier of the object (see {@link EOBase#apiId()}
 * is also used to refer to the repository for the object.
 * </p><p>
 * In addition to these methods, any class implementing this interface must
 * also provide the following static method (where {@code [Type]} represents
 * the concrete EO type):
 * </p>
 * <dl>
 * <dt>{@code NSArray<[Type]> repositoriesPresentedToUser(User, EOEditingContext)}</dt>
 * <dd>This method should return an array of all EOs that have repositories
 * that the given user may access. This is used to provide a repository list on
 * the user's profile page, as well as to provide the virtual root directory
 * for WebDAV access.</dd>
 * </dl>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2012/06/22 16:23:18 $
 */
public interface RepositoryProvider
{
    //~ Methods ...............................................................

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
