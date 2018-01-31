/*==========================================================================*\
 |  $Id: EntityRequestInfo.java,v 1.4 2012/06/22 16:23:17 aallowat Exp $
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Scanner;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

//-------------------------------------------------------------------------
/**
 * A small class that bundles up the information about an entity resource
 * request (the entity name, the object ID, and the resource path).
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.4 $, $Date: 2012/06/22 16:23:17 $
 */
public class EntityRequestInfo
{
    //~ Constructors ......................................................

    // ----------------------------------------------------------
    private EntityRequestInfo()
    {
        // Do nothing.
    }


    // ----------------------------------------------------------
    public EntityRequestInfo(String entityName, String objectID,
            String resourcePath)
    {
        this.entityName = entityName;
        this.objectID = objectID;
        this.resourcePath = resourcePath;
    }


    //~ Methods ...........................................................

    // ----------------------------------------------------------
    /**
     * Parses the specified handler path and returns an
     * {@code EntityRequestInfo} containing the request information. This
     * method returns null if the request path was malformed or invalid.
     *
     * @param handlerPath the full handler path
     *
     * @return an instance of {@code EntityRequestInfo} with the desired
     *     information, or null
     */
    public static EntityRequestInfo fromRequestHandlerPath(String handlerPath)
    {
        return fromRequestHandlerPath(handlerPath, false);
    }


    // ----------------------------------------------------------
    /**
     * Parses the specified handler path and returns an
     * {@code EntityRequestInfo} containing the request information. This
     * method returns null if the request path was malformed or invalid.
     *
     * @param handlerPath the full handler path
     * @param allowMissingComponents if true, an object will be returned even
     *     if the entity name or object ID are null; if false, null will be
     *     returned in this case
     *
     * @return an instance of {@code EntityRequestInfo} with the desired
     *     information, or null
     */
    public static EntityRequestInfo fromRequestHandlerPath(String handlerPath,
            boolean allowMissingComponents)
    {
        try
        {
            handlerPath = URLDecoder.decode(handlerPath, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // Do nothing.
        }

        EntityRequestInfo request = new EntityRequestInfo();

        Scanner scanner = new Scanner(handlerPath);
        scanner.useDelimiter("/");

        if (scanner.hasNext())
        {
            request.entityName = scanner.next();
        }

        if (scanner.hasNext())
        {
            request.objectID = scanner.next();
        }

        if (scanner.hasNext())
        {
            scanner.skip("/");
        }

        scanner.useDelimiter("\0");

        if (scanner.hasNext())
        {
            request.resourcePath = scanner.next();

            if (request.resourcePath.startsWith("/"))
            {
                request.resourcePath = request.resourcePath.substring(1);
            }
        }

        if (!allowMissingComponents
                && (request.entityName == null || request.objectID == null))
        {
            return null;
        }
        else
        {
            return request;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the entity name of the request.
     *
     * @return the entity name of the request
     */
    public String entityName()
    {
        return entityName;
    }


    // ----------------------------------------------------------
    /**
     * Gets the object ID of the request.
     *
     * @return the object ID of the request
     */
    public String objectID()
    {
        return objectID;
    }


    // ----------------------------------------------------------
    /**
     * Gets the path to the resource in the requested object.
     *
     * @return the path to the resource
     */
    public String resourcePath()
    {
        return resourcePath;
    }


    // ----------------------------------------------------------
    /**
     * Fetches the requested object into the specified editing context and
     * returns it.
     *
     * @param ec the editing context
     * @return the requested object
     */
    public EOBase requestedObject(EOEditingContext ec)
    {
        return EOBase.objectWithApiId(ec, entityName(), objectID());
    }


    //~ Static/instance variables .........................................

    private String entityName;
    private String objectID;
    private String resourcePath;
}
