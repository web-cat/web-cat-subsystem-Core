/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2010 Virginia Tech
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import org.webcat.core.Application;
import org.webcat.core.EntityResourceRequestHandler;
import org.webcat.core.EntityResourceHandler;
import org.webcat.core.Session;
import org.apache.log4j.Logger;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXQ;

//-------------------------------------------------------------------------
/**
 * <p>
 * A request handler that allows subsystems to make file-system resources that
 * they generate and associate with EOs directly visible on the web and allow
 * resources associated with the same EO to be relatively linked.
 * </p><p>
 * URLs should be of the form:
 * <pre>http://server/Web-CAT.wo/er/[session id]/[EO type]/[EO id]/[path/to/the/file]</pre>
 * where "er" is this request handler's key, "session id" is the session
 * identifier (which is optional), "EO type" is the name of the entity
 * whose resources are being requested, "EO id" is the numeric ID of the
 * entity, and "path/to/the/file" is the path to the resource, relative to
 * whatever file-system location the particular entity deems fit for its
 * related resources.
 * </p>
 *
 * @author  Tony Allevato
 * @version $Id$
 */
public class EntityResourceRequestHandler extends WORequestHandler
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets a relative URL to access an entity-related resource.
     *
     * @param context the request context
     * @param eo the EO whose resource should be accessed
     * @param relativePath the entity-relative path to the resource
     * @return the URL to the resource
     */
    public static String urlForEntityResource(WOContext context,
            EOEnterpriseObject eo, String relativePath)
    {
        String entityName = eo.entityName();
        Number id = (Number) eo.valueForKey("id");

        StringBuffer buffer = new StringBuffer();
        String sessionString = null;

        if (context.session().storesIDsInURLs())
        {
            sessionString = "?wosid=" + context.session().sessionID();
        }

        buffer.append(entityName);
        buffer.append("/");
        buffer.append(id);

        if (relativePath != null && relativePath.length() > 0)
        {
            buffer.append("/");
            buffer.append(relativePath);
        }

        return context.urlWithRequestHandlerKey(REQUEST_HANDLER_KEY,
                buffer.toString(), sessionString);
    }


    // ----------------------------------------------------------
    /**
     * Registers an entity resource handler with the request handler.
     *
     * @param entityClass the Java class of the EO
     * @param handler a resource handler
     */
    public static void registerHandler(Class<?> entityClass,
                                       EntityResourceHandler handler)
    {
        resourceHandlers.setObjectForKey(handler, entityClass);
    }


    // ----------------------------------------------------------
    /**
     * Finds the entity-related resource from the given request and creates a
     * response containing its data.
     *
     * @param request the request
     * @return the response
     */
    @Override
    public WOResponse handleRequest(WORequest request)
    {
        WOContext context =
            Application.application().createContextForRequest(request);
        WOResponse response =
            Application.application().createResponseInContext(context);

        String handlerPath = request.requestHandlerPath();

        Scanner scanner = new Scanner(handlerPath);
        scanner.useDelimiter("/");

        Session session = null;
        String sessionId = request.sessionID();

        if (sessionId != null)
        {
            try
            {
                session = (Session)
                    Application.application().restoreSessionWithID(
                        sessionId, context);
            }
            catch (Exception e)
            {
                // Do nothing; will be handled below.
            }
        }

        {
            String entity = null;
            long id = 0;
            String path = null;

            if (scanner.hasNext())
            {
                entity = scanner.next();
            }

            if (scanner.hasNext())
            {
                String idString = scanner.next();

                try
                {
                    id = Long.valueOf(idString);
                }
                catch (NumberFormatException e)
                {
                    id = 0;
                }
            }

            scanner.skip("/");
            scanner.useDelimiter("\0");
            if (scanner.hasNext())
            {
                path = scanner.next();

                if (!validatePath(path))
                {
                    path = null;
                }
            }

            if (entity != null && id != 0 && path != null)
            {
                EOEditingContext ec;

                if (session != null)
                {
                    ec = session.sessionContext();
                }
                else
                {
                    ec = Application.newPeerEditingContext();
                }

                EOEntity ent = EOUtilities.entityNamed(ec, entity);
                Class<?> entityClass = null;

                try
                {
                    entityClass = Class.forName(ent.className());
                }
                catch (ClassNotFoundException e)
                {
                    // Do nothing; error will be handled below.
                }

                EntityResourceHandler handler =
                    resourceHandlers.objectForKey(entityClass);

                if (handler != null)
                {
                    if (handler.requiresLogin() && session == null)
                    {
                        log.warn("No session found with id " + sessionId);
                        response.setStatus(WOResponse.HTTP_STATUS_FORBIDDEN);
                    }

                    EOFetchSpecification fspec = new EOFetchSpecification(
                            entity, ERXQ.is("id", id), null);
                    NSArray<? extends EOEnterpriseObject> objects =
                        ec.objectsWithFetchSpecification(fspec);

                    if (objects != null && objects.count() > 0)
                    {
                        EOEnterpriseObject object = objects.objectAtIndex(0);

                        boolean canAccess = !handler.requiresLogin()
                            || (session != null &&
                                    (session.user().hasAdminPrivileges()
                                            || handler.userCanAccess(object,
                                                    session.user())));

                        if (canAccess)
                        {
                            generateResponse(response, handler, object, path);
                        }
                        else
                        {
                            String userName = (session != null
                                    ? session.user().userName() : "<null>");

                            log.warn("User " + userName + " tried to access "
                                    + "entity resource without permission");
                            response.setStatus(WOResponse.HTTP_STATUS_FORBIDDEN);
                        }
                    }
                    else
                    {
                        log.warn("Attempted to access entity resource for "
                                + "an object that does not exist: " + entity
                                + ":" + id);

                        response.setStatus(WOResponse.HTTP_STATUS_NOT_FOUND);
                    }
                }
                else
                {
                    log.warn("No entity request handler was found for "
                            + entity);

                    response.setStatus(WOResponse.HTTP_STATUS_NOT_FOUND);
                }

                if (session == null)
                {
                    Application.releasePeerEditingContext(ec);
                }
            }
            else
            {
                response.setStatus(WOResponse.HTTP_STATUS_NOT_FOUND);
            }
        }

        if (session != null)
        {
            Application.application().saveSessionForContext(context);
        }

        return response;
    }


    // ----------------------------------------------------------
    /**
     * A somewhat brainless check to make sure that the path provided does not
     * go higher up into the file-system than it should. We verify that it is
     * a relative path and that it does not contain any parent directory
     * references that would move it above its origin.
     */
    private boolean validatePath(String path)
    {
        File file = new File(path);

        if (file.isAbsolute())
        {
            log.warn("Attempted to access absolute path (" + path + ") in "
                    + "entity resource handler");
            return false;
        }
        else
        {
            int level = 0;

            String[] components = file.getPath().split(File.separator);
            for (String component : components)
            {
                if (component.equals(".."))
                {
                    level--;
                }
                else if (!component.equals("."))
                {
                    level++;
                }

                if (level < 0)
                {
                    log.warn("Attempted to access bad relative path (" + path
                            + ") in entity resource handler");
                    return false;
                }
            }
        }

        return true;
    }


    // ----------------------------------------------------------
    private void generateResponse(WOResponse response,
                                  EntityResourceHandler handler,
                                  EOEnterpriseObject object,
                                  String path)
    {
        File absolutePath = handler.pathForResource(object, path);

        if (absolutePath.exists())
        {
            try
            {
                FileInputStream stream = new FileInputStream(absolutePath);
                response.setContentStream(stream, 0, absolutePath.length());
                response.setStatus(WOResponse.HTTP_STATUS_OK);
            }
            catch (IOException e)
            {
                response.setStatus(WOResponse.HTTP_STATUS_NOT_FOUND);
            }
        }
        else
        {
            response.setStatus(WOResponse.HTTP_STATUS_NOT_FOUND);
        }
    }


    //~ Static/instance variables .............................................

    public static final String REQUEST_HANDLER_KEY = "er";

    private static final NSMutableDictionary<Class<?>, EntityResourceHandler>
        resourceHandlers = new NSMutableDictionary<Class<?>, EntityResourceHandler>();

    private static final Logger log = Logger.getLogger(
            EntityResourceRequestHandler.class);
}
