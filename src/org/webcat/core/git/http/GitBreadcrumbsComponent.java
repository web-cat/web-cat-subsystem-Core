/*==========================================================================*\
 |  $Id: GitBreadcrumbsComponent.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

package org.webcat.core.git.http;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public class GitBreadcrumbsComponent extends GitWebComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitBreadcrumbsComponent(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public NSArray<GitWebContext> breadcrumbs;
    public GitWebContext aBreadcrumb;
    public int index;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (breadcrumbs == null)
        {
            NSMutableArray<GitWebContext> someBreadcrumbs =
                new NSMutableArray<GitWebContext>();

            GitWebContext breadcrumb = gitContext();

            String lastPathComponent = breadcrumb.lastPathComponent();
            while (lastPathComponent != null)
            {
                someBreadcrumbs.insertObjectAtIndex(breadcrumb, 0);

                breadcrumb = breadcrumb.clone();
                breadcrumb.setMode(GitWebMode.TREE);
                breadcrumb.removeLastPathComponent();
                lastPathComponent = breadcrumb.lastPathComponent();
            }

            breadcrumb.setObjectId(null);
            someBreadcrumbs.insertObjectAtIndex(breadcrumb, 0);

            breadcrumbs = someBreadcrumbs;
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public boolean isLastBreadcrumb()
    {
        return aBreadcrumb == breadcrumbs.lastObject();
    }


    // ----------------------------------------------------------
    public String aBreadcrumbName()
    {
        if (aBreadcrumb.path() != null)
        {
            return aBreadcrumb.lastPathComponent();
        }
        else
        {
            return aBreadcrumb.repositoryName();
        }
    }


    // ----------------------------------------------------------
    public String aBreadcrumbURL()
    {
        return aBreadcrumb.toURL(context());
    }
}
