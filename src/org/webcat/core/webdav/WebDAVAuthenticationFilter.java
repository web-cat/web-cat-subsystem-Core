/*==========================================================================*\
 |  $Id: WebDAVAuthenticationFilter.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

package org.webcat.core.webdav;

import org.webcat.core.Application;
import org.webcat.core.RepositoryManager;
import org.webcat.core.User;
import org.webcat.core.http.BasicAuthenticationFilter;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

//-------------------------------------------------------------------------
/**
 * A request filter that performs HTTP basic authentication on a Git repository
 * URL, to validate the user against the Web-CAT user database.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class WebDAVAuthenticationFilter extends BasicAuthenticationFilter
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    protected String realmForContext(WOContext context)
    {
        return "Web-CAT WebDAV access on " + Application.wcApplication().host();
    }


    // ----------------------------------------------------------
    @Override
    protected boolean userHasAccess(User user)
    {
        NSArray<? extends EOEnterpriseObject> providers =
            RepositoryManager.getInstance().repositoriesPresentedToUser(
                    user, user.editingContext());

        return !providers.isEmpty();
    }
}
