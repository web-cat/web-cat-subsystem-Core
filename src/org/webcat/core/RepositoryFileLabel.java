/*==========================================================================*\
 |  $Id: RepositoryFileLabel.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
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

import org.eclipse.jgit.lib.Constants;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class RepositoryFileLabel
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public RepositoryFileLabel(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public String noFilePlaceholder;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public Object value()
    {
        return value;
    }


    // ----------------------------------------------------------
    public void setValue(Object value)
    {
        this.value = value;

        if (value == null)
        {
            fileItem = null;
        }
        else if (value instanceof String)
        {
            fileItem = RepositoryEntryRef.fromOldStylePath((String) value);
        }
        else
        {
            @SuppressWarnings("unchecked")
            NSDictionary<String, Object> dict =
                (NSDictionary<String, Object>)value;
            fileItem = RepositoryEntryRef.fromDictionary(dict);
        }
    }


    // ----------------------------------------------------------
    public boolean hasFile()
    {
        return fileItem != null;
    }


    // ----------------------------------------------------------
    public String repositoryAndPath()
    {
        return fileItem.repositoryName() + "/" + fileItem.path();
    }


    // ----------------------------------------------------------
    public String iconPath()
    {
        return fileItem.iconPath();
    }


    // ----------------------------------------------------------
    public String branch()
    {
        String branch = fileItem.branch();

        if (branch.startsWith(Constants.R_HEADS))
        {
            return branch.substring(Constants.R_HEADS.length());
        }
        else if (branch.startsWith(Constants.R_TAGS))
        {
            return branch.substring(Constants.R_TAGS.length());
        }
        else
        {
            return branch;
        }
    }


    // ----------------------------------------------------------
    public boolean isTag()
    {
        return fileItem.branch().startsWith(Constants.R_TAGS);
    }


    //~ Static/instance variables .............................................

    private Object value;
    private RepositoryEntryRef fileItem;
}
