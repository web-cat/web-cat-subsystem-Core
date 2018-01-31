/*==========================================================================*\
 |  $Id: GitRef.java,v 1.2 2011/11/08 14:06:07 aallowat Exp $
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

package org.webcat.core.git;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2011/11/08 14:06:07 $
 */
public class GitRef implements NSKeyValueCodingAdditions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitRef(GitRepository repository, Ref ref)
    {
        this.repository = repository;
        this.ref = ref;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public GitRepository repository()
    {
        return repository;
    }


    // ----------------------------------------------------------
    public Ref ref()
    {
        return ref;
    }


    // ----------------------------------------------------------
    public String name()
    {
        return ref.getName();
    }


    // ----------------------------------------------------------
    public boolean isHead()
    {
        return name().startsWith(Constants.R_HEADS);
    }


    // ----------------------------------------------------------
    public boolean isTag()
    {
        return name().startsWith(Constants.R_TAGS);
    }


    // ----------------------------------------------------------
    public boolean isMaster()
    {
        return name().equals(Constants.R_HEADS + Constants.MASTER);
    }


    // ----------------------------------------------------------
    public String shortName()
    {
        if (isHead())
        {
            return name().substring(Constants.R_HEADS.length());
        }
        else if (isTag())
        {
            return name().substring(Constants.R_TAGS.length());
        }
        else
        {
            return name();
        }
    }


    // ----------------------------------------------------------
    public ObjectId objectId()
    {
        return ref.getObjectId();
    }


    // ----------------------------------------------------------
    public NSArray<GitCommit> commits()
    {
        return repository.commitsWithId(ref.getObjectId(), null);
    }


    // ----------------------------------------------------------
    public int hashCode()
    {
        return 0xBAD0F00D ^ ref.hashCode();
    }


    // ----------------------------------------------------------
    public boolean equals(Object other)
    {
        if (other instanceof GitRef)
        {
            GitRef otherRef = (GitRef) other;
            return ref.getObjectId().equals(otherRef.ref.getObjectId());
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    public void takeValueForKeyPath(Object value, String keyPath)
    {
        NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(
                this, value, keyPath);
    }


    // ----------------------------------------------------------
    public Object valueForKeyPath(String keyPath)
    {
        return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(
                this, keyPath);
    }


    // ----------------------------------------------------------
    public void takeValueForKey(Object value, String key)
    {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(
                this, value, key);
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }


    //~ Static/instance variables .............................................

    private GitRepository repository;
    private Ref ref;
}
