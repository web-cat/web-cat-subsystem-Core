/*==========================================================================*\
 |  $Id: RepositoryEntryRef.java,v 1.6 2012/06/22 16:23:17 aallowat Exp $
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

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.webcat.core.git.GitRef;
import org.webcat.core.git.GitRepository;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableDictionary;

//-------------------------------------------------------------------------
/**
 * Represents the selection in a file picker. The selected file or folder is
 * represented by three parts: the repository the file is in, the path to the
 * file, and the branch representing which "snapshot" of the file should be
 * used. This class makes it easier to translate file picker selections back
 * and forth between a convenient object format and a dictionary format that is
 * stored in the database.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.6 $, $Date: 2012/06/22 16:23:17 $
 */
public class RepositoryEntryRef
    implements NSKeyValueCodingAdditions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public RepositoryEntryRef(String repository, String path, String branch)
    {
        this.repository = repository;
        this.path = path;
        this.branch = branch;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public static RepositoryEntryRef fromDictionary(
            NSDictionary<String, Object> dict)
    {
        return new RepositoryEntryRef(
                (String) dict.objectForKey(REPOSITORY_KEY),
                (String) dict.objectForKey(PATH_KEY),
                (String) dict.objectForKey(BRANCH_KEY));
    }


    // ----------------------------------------------------------
    public static RepositoryEntryRef fromOldStylePath(String path)
    {
        Pattern pattern = Pattern.compile("^([^/]+)/([^/]+)(?:/(.*))?$");
        Matcher matcher = pattern.matcher(path);

        if (matcher.matches())
        {
            String authDomain = matcher.group(1);
            String username = matcher.group(2);
            String repoPath = matcher.group(3);

            // Make sure a slash is added to the end of old-style paths that
            // point to directories.
            File oldPath = new File(User.userDataRoot(), path);
            if (oldPath.isDirectory())
            {
                repoPath += "/";
            }

            return new RepositoryEntryRef(
                    "User/" + authDomain + "." + username, repoPath,
                    Constants.R_HEADS + Constants.MASTER);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public NSDictionary<String, Object> dictionaryRepresentation()
    {
        NSMutableDictionary<String, Object> dict =
            new NSMutableDictionary<String, Object>();

        dict.setObjectForKey(repository, REPOSITORY_KEY);
        dict.setObjectForKey(path, PATH_KEY);
        dict.setObjectForKey(branch, BRANCH_KEY);

        return dict;
    }


    // ----------------------------------------------------------
    public boolean isDirectory()
    {
        return path.endsWith("/");
    }


    // ----------------------------------------------------------
    public String repositoryName()
    {
        return repository;
    }


    // ----------------------------------------------------------
    public String path()
    {
        return path;
    }


    // ----------------------------------------------------------
    public String branch()
    {
        return branch;
    }


    // ----------------------------------------------------------
    public String name()
    {
        if (path == null)
        {
            return null;
        }

        String pathMod = path;
        if (pathMod.endsWith("/"))
        {
            pathMod = pathMod.substring(0, pathMod.length() - 1);
        }

        int lastSlash = pathMod.lastIndexOf('/');

        if (lastSlash == -1)
        {
            return pathMod;
        }
        else
        {
            return pathMod.substring(lastSlash + 1);
        }
    }


    // ----------------------------------------------------------
    public String iconPath()
    {
        if (isDirectory())
        {
            return FileUtilities.folderIconURL();
        }
        else
        {
            return FileUtilities.iconURL(path);
        }
    }


    // ----------------------------------------------------------
    public void resolve(EOEditingContext ec)
    {
        String[] parts = repository.split("/");

        provider = EOBase.objectWithApiId(ec, parts[0], parts[1]);
        gitRepository = GitRepository.repositoryForObject(provider);
        ref = gitRepository.refWithName(branch);

        if (ref != null)
        {
            objectId = gitRepository.resolve(ref.objectId().getName() + ":" + path);
        }
        else
        {
            objectId = null;
        }
    }


    // ----------------------------------------------------------
    public EOEnterpriseObject provider()
    {
        if (gitRepository == null)
        {
            throw new IllegalStateException("You must resolve the entry ref "
                    + "first.");
        }

        return provider;
    }


    // ----------------------------------------------------------
    public GitRepository repository()
    {
        if (gitRepository == null)
        {
            throw new IllegalStateException("You must resolve the entry ref "
                    + "first.");
        }

        return gitRepository;
    }


    // ----------------------------------------------------------
    public GitRef ref()
    {
        if (gitRepository == null)
        {
            throw new IllegalStateException("You must resolve the entry ref "
                    + "first.");
        }

        return ref;
    }


    // ----------------------------------------------------------
    public ObjectId objectId()
    {
        if (gitRepository == null)
        {
            throw new IllegalStateException("You must resolve the entry ref "
                    + "first.");
        }

        return objectId;
    }


    // ----------------------------------------------------------
    @Override
    public int hashCode()
    {
        return repository.hashCode()
            ^ (path.hashCode() << 11)
            ^ (branch.hashCode() << 22);
    }


    // ----------------------------------------------------------
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof RepositoryEntryRef)
        {
            RepositoryEntryRef otherItem = (RepositoryEntryRef) other;

            return repository.equals(otherItem.repository)
                && path.equals(otherItem.path)
                && branch.equals(otherItem.branch);
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    public void takeValueForKeyPath(Object value, String keyPath)
    {
        NSKeyValueCodingAdditions.DefaultImplementation
            .takeValueForKeyPath(this, value, keyPath);
    }


    // ----------------------------------------------------------
    public Object valueForKeyPath(String keyPath)
    {
        return NSKeyValueCodingAdditions.DefaultImplementation
            .valueForKeyPath(this, keyPath);
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
        return NSKeyValueCoding.DefaultImplementation.valueForKey(
                this, key);
    }


    //~ Static/instance variables .............................................

    private static final String REPOSITORY_KEY = "repository";
    private static final String PATH_KEY = "path";
    private static final String BRANCH_KEY = "branch";

    private String repository;
    private String path;
    private String branch;

    private EOBase provider;
    private GitRepository gitRepository;
    private GitRef ref;
    private ObjectId objectId;
}
