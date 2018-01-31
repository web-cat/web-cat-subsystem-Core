/*==========================================================================*\
 |  $Id: GitWebContext.java,v 1.4 2012/06/22 16:23:17 aallowat Exp $
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

package org.webcat.core.git.http;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.webcat.core.EOBase;
import org.webcat.core.git.GitRef;
import org.webcat.core.git.GitRepository;
import com.webobjects.appserver.WOContext;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.4 $, $Date: 2012/06/22 16:23:17 $
 */
public class GitWebContext
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitWebContext(EOBase originator,
            GitRepository repository, String repositoryName, GitWebMode mode)
    {
        this.originator = originator;
        this.repository = repository;
        this.repositoryName = repositoryName;
        this.mode = mode;
        setHeadName(Constants.MASTER);
    }


    // ----------------------------------------------------------
    public GitWebContext(EOBase originator,
            GitRepository repository, String repositoryName, GitWebMode mode,
            String pathSuffix)
    {
        this(originator, repository, repositoryName, mode);

        ObjectId id = null;

        try
        {
            id = ObjectId.fromString(pathSuffix);
        }
        catch (Exception e)
        {
            int firstSlash = pathSuffix.indexOf('/');

            if (firstSlash == -1)
            {
                setHeadName(pathSuffix);
            }
            else
            {
                setHeadName(pathSuffix.substring(0, firstSlash));
                path = pathSuffix.substring(firstSlash + 1);
            }

            id = repository.resolve(headObjectId.getName() + ":" + path);
        }

        objectId = id;
    }


    // ----------------------------------------------------------
    private GitWebContext(GitWebContext source)
    {
        originator = source.originator;
        repository = source.repository;
        repositoryName = source.repositoryName;
        mode = source.mode;
        headName = source.headName;
        headObjectId = source.headObjectId;
        objectId = source.objectId;
        path = source.path;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public EOBase originator()
    {
        return originator;
    }


    // ----------------------------------------------------------
    public GitRepository repository()
    {
        return repository;
    }


    // ----------------------------------------------------------
    public String repositoryName()
    {
        return repositoryName;
    }


    // ----------------------------------------------------------
    public GitWebMode mode()
    {
        return mode;
    }


    // ----------------------------------------------------------
    public void setMode(GitWebMode mode)
    {
        this.mode = mode;

        // Clear out any unnecessary context options (for example, COMMIT
        // doesn't need a path, just an object ID).

        switch (mode)
        {
            case BRANCHES:
            case COMPARE:
                path = null;
                objectId = null;
                break;

            case COMMIT:
                path = null;
                break;

            default:
                // Do nothing.
                break;
        }
    }


    // ----------------------------------------------------------
    public String headName()
    {
        return headName;
    }


    // ----------------------------------------------------------
    private void setHeadName(String headName)
    {
        this.headName = headName;

        computeHeadObjectId();
    }


    // ----------------------------------------------------------
    public ObjectId headObjectId()
    {
        return headObjectId;
    }


    // ----------------------------------------------------------
    public void setHeadObjectId(ObjectId objectId)
    {
        headObjectId = objectId;
    }


    // ----------------------------------------------------------
    public void computeHeadObjectId()
    {
        GitRef ref = repository.refWithName(headName);

        if (ref != null)
        {
            headObjectId = ref.ref().getObjectId();
        }
    }


    // ----------------------------------------------------------
    public GitRef headRef()
    {
        return repository.refWithName(headName);
    }


    // ----------------------------------------------------------
    public void setHeadRef(GitRef ref)
    {
        if (ref != null)
        {
            setHeadName(ref.shortName());
        }
        else
        {
            setHeadName(null);
        }
    }


    // ----------------------------------------------------------
    public ObjectId objectId()
    {
        return objectId;
    }


    // ----------------------------------------------------------
    public void setObjectId(ObjectId objectId)
    {
        // TODO recompute the path here?

        this.objectId = objectId;
    }


    // ----------------------------------------------------------
    public String path()
    {
        return path;
    }


    // ----------------------------------------------------------
    public void setPath(String path)
    {
        // TODO recompute the object ID here?

        this.path = path;
    }


    // ----------------------------------------------------------
    public void appendToPath(String pathComponent)
    {
        String newPath = path();

        if (newPath == null || newPath.length() == 0)
        {
            newPath = pathComponent;
        }
        else
        {
            if (newPath.endsWith("/"))
            {
                newPath += pathComponent;
            }
            else
            {
                newPath += "/" + pathComponent;
            }
        }

        setPath(newPath);
    }


    // ----------------------------------------------------------
    public String lastPathComponent()
    {
        String thePath = path();

        if (thePath != null)
        {
            int lastSlash = thePath.lastIndexOf('/');

            if (lastSlash == -1)
            {
                return thePath;
            }
            else
            {
                String component = thePath.substring(lastSlash + 1);

                if (component.length() == 0)
                {
                    return null;
                }
                else
                {
                    return component;
                }
            }
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public void removeLastPathComponent()
    {
        String thePath = path();

        if (thePath != null)
        {
            int lastSlash = thePath.lastIndexOf('/');

            if (lastSlash == -1)
            {
                thePath = null;
            }
            else
            {
                thePath = thePath.substring(0, lastSlash);
            }

            setPath(thePath);
        }
    }


    // ----------------------------------------------------------
    public static GitWebContext parse(EOBase originator,
            GitRepository repository, String repositoryName, String path)
    {
        GitWebContext newContext = null;

        if (path == null || path.length() == 0)
        {
            return new GitWebContext(originator, repository,
                    repositoryName, GitWebMode.TREE, Constants.MASTER);
        }

        String typeString = null;
        String remainder = null;

        int firstSlash = path.indexOf('/');
        if (firstSlash == -1)
        {
            typeString = path;
        }
        else
        {
            typeString = path.substring(0, firstSlash);
            remainder = path.substring(firstSlash + 1);
        }

        GitWebMode type = GitWebMode.valueOf(
                typeString.toUpperCase());

        if (type != null)
        {
            switch (type)
            {
                case TREE:
                case COMMITS:
                case BLOB:
                case RAW:
                    newContext = new GitWebContext(originator, repository,
                            repositoryName, type, remainder);
                    break;

                case BRANCHES:
                    newContext = new GitWebContext(originator, repository,
                            repositoryName, type);
                    break;

                case COMMIT:
                    newContext = new GitWebContext(originator, repository,
                            repositoryName, type);
                    newContext.headObjectId = ObjectId.fromString(remainder);
                    break;

                case COMPARE:
                    // TODO implement
                    break;
            }
        }

        return newContext;
    }


    // ----------------------------------------------------------
    public GitWebContext clone()
    {
        return new GitWebContext(this);
    }


    // ----------------------------------------------------------
    public String toURL(WOContext context)
    {
        String pathAfterType = urlPathAfterRequestType();
        String fullPath = mode.name().toLowerCase();

        if (pathAfterType != null)
        {
            fullPath += "/" + pathAfterType;
        }

        return GitRequestHandler.urlForRepositoryPath(context, originator,
                fullPath);
    }


    // ----------------------------------------------------------
    public String description()
    {
        String description;

        switch (mode)
        {
            case BLOB:
            case TREE:
                if (path() == null)
                {
                    description = repositoryName() + " at " + headName();
                }
                else
                {
                    description = path() + " at " + headName()
                        + " from " + repositoryName();
                }
                break;

            case COMMIT:
                description = "Commit " + headObjectId().getName()
                    + " to " + repositoryName();
                break;

            case COMMITS:
                description = "Commit History for " + repositoryName();
                break;

            case BRANCHES:
                description = "All Branches for " + repositoryName();
                break;

            default:
                // FIXME
                description = null;
                break;
        }

        return description;
    }


    // ----------------------------------------------------------
    public void setDescription(String description)
    {
        // Do nothing; keep KVC happy.
    }


    // ----------------------------------------------------------
    private String urlPathAfterRequestType()
    {
        if (headName() != null && path != null)
        {
            return headName() + "/" + path;
        }
        else if (objectId != null)
        {
            return objectId.getName();
        }
        else if (headName() != null)
        {
            return headName();
        }
        else
        {
            return null;
        }
    }


    //~ Static/instance variables .............................................

    private EOBase originator;
    private GitWebMode mode;
    private GitRepository repository;
    private String repositoryName;
    private String headName;
    private ObjectId headObjectId;
    private ObjectId objectId;
    private String path;
}
