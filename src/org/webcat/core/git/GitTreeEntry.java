/*==========================================================================*\
 |  $Id: GitTreeEntry.java,v 1.4 2012/11/29 16:31:25 aallowat Exp $
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

package org.webcat.core.git;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.TreeWalk;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.4 $, $Date: 2012/11/29 16:31:25 $
 */
public class GitTreeEntry
    implements NSKeyValueCodingAdditions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    protected GitTreeEntry(GitRepository repository, ObjectId objectId,
            boolean isTree, String name, String path, long size)
    {
        this.repository = repository;
        this.objectId = objectId;
        this.isTree = isTree;
        this.name = name;
        this.path = path;
        this.size = size;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public static GitTreeEntry fromTreeWalk(TreeWalk walk,
            GitRepository repository)
    {
        return fromTreeWalk(walk, repository, null);
    }


    // ----------------------------------------------------------
    public static GitTreeEntry fromTreeWalk(TreeWalk walk,
            GitRepository repository, String pathPrefix)
    {
        ObjectId oid = walk.getObjectId(0);
        boolean isTree = (walk.getFileMode(0) == FileMode.TREE);
        String name = walk.getNameString();
        String path = (pathPrefix == null) ? walk.getPathString() :
            pathPrefix + "/" + walk.getPathString();
        long size = 0;

        if (!isTree)
        {
            try
            {
                size = walk.getObjectReader().getObjectSize(oid,
                        ObjectReader.OBJ_ANY);
            }
            catch (IOException e)
            {
                log.warn("There was an error getting the size of the object "
                        + oid.getName(), e);
            }
        }

        return new GitTreeEntry(repository, oid, isTree, name, path, size);
    }


    // ----------------------------------------------------------
    public GitRepository repository()
    {
        return repository;
    }


    // ----------------------------------------------------------
    public ObjectId objectId()
    {
        return objectId;
    }


    // ----------------------------------------------------------
    public boolean isTree()
    {
        return isTree;
    }


    // ----------------------------------------------------------
    public String name()
    {
        return name;
    }


    // ----------------------------------------------------------
    public String path()
    {
        return path;
    }


    // ----------------------------------------------------------
    public long size()
    {
        return size;
    }


    // ----------------------------------------------------------
    @Override
    public int hashCode()
    {
        return 0xFACEBEEF ^ objectId.hashCode();
    }


    // ----------------------------------------------------------
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof GitTreeEntry)
        {
            GitTreeEntry otherEntry = (GitTreeEntry) other;
            return path.equals(otherEntry.path)
                && name.equals(otherEntry.name);
                //&& objectId.equals(otherEntry.objectId);
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    @Override
    public String toString()
    {
        return "<" + (isTree ? "tree" : "blob") + ": " + path
            + " [" + objectId.abbreviate(10).name() + "]>";
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
    private ObjectId objectId;
    private boolean isTree;
    private String name;
    private String path;
    private long size;

    private static final Logger log = Logger.getLogger(GitTreeEntry.class);
}
