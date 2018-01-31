/*==========================================================================*\
 |  $Id: EntityResourceRequestHandler.java,v 1.11 2011/12/25 02:24:54 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2010-2011 Virginia Tech
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
import org.webcat.core.Application;
import org.webcat.core.EntityResourceRequestHandler;
import org.webcat.core.EntityResourceHandler;
import org.webcat.core.Session;
import org.webcat.woextensions.ECAction;
import static org.webcat.woextensions.ECAction.run;
import org.apache.log4j.Logger;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableDictionary;
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
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.11 $, $Date: 2011/12/25 02:24:54 $
 */
public class EntityResourceRequestHandler
    extends WORequestHandler
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
            sessionString = "wosid=" + context.session().sessionID();
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
                                       EntityResourceHandler<?> handler)
    {
        log.debug("Registering entity resource handler for class "
                + entityClass.getCanonicalName());

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
        log.debug("Received request: " + request.requestHandlerPath());

        WOContext context =
            Application.application().createContextForRequest(request);
        WOResponse response =
            Application.application().createResponseInContext(context);

        // Get the user's session, if possible. We'll use it later for
        // resources that require logins (for user validation) in order to be
        // accessed.

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
                session = null;
            }
        }

        try
        {
            // Perform the actual request handling.

            _handleRequest(request, context, response, session);
        }
        catch (Exception e)
        {
            log.warn("(404) An exception occurred when handling the request "
                    + "for " + request.requestHandlerPath(), e);

            response.setContent("");
            response.setStatus(WOResponse.HTTP_STATUS_NOT_FOUND);
        }
        finally
        {
            if (session != null)
            {
                Application.application().saveSessionForContext(context);
            }
        }

        return response;
    }


    // ----------------------------------------------------------
    private void _handleRequest(
        final WORequest request,
        final WOContext context,
        final WOResponse response,
        final Session session)
    {
        final String handlerPath = request.requestHandlerPath();

        // Parse the request path into its entity, object ID, and resource
        // path.

        final EntityRequestInfo entityRequest =
            EntityRequestInfo.fromRequestHandlerPath(handlerPath);

        if (entityRequest == null
                || !validatePath(entityRequest.resourcePath()))
        {
            log.warn("(404) The request path was malformed: "
                    + request.requestHandlerPath());

            response.setStatus(WOResponse.HTTP_STATUS_NOT_FOUND);
            return;
        }

        run(new ECAction() { public void action() {
            EntityResourceHandler<EOEnterpriseObject> handler =
                handlerForEntityNamed(entityRequest.entityName(), ec);

            if (handler != null)
            {
                log.debug("Found handler for entity "
                        + entityRequest.entityName());

                if (handler.requiresLogin() && session == null)
                {
                    log.warn("(403) Handler requires log-in, but no session "
                            + "found with id " + request.sessionID());
                    response.setStatus(WOResponse.HTTP_STATUS_FORBIDDEN);
                }
                else
                {
                    EOEnterpriseObject object = fetchObject(
                            entityRequest, handler, ec);

                    if (object != null)
                    {
                        if (canAccessObject(object, handler, session))
                        {
                            generateResponse(response, handler, object,
                                    entityRequest.resourcePath());
                        }
                        else
                        {
                            String userName = (session != null
                                    ? session.user().userName() : "<null>");

                            log.warn("(403) User " + userName
                                    + " tried to access entity resource "
                                    + "without permission");

                            response.setStatus(
                                WOResponse.HTTP_STATUS_FORBIDDEN);
                        }
                    }
                    else
                    {
                        log.warn("(404) Attempted to access entity resource "
                                + "for an object that does not exist: "
                                + entityRequest.entityName()
                                + ":" + entityRequest.objectID());

                        response.setStatus(WOResponse.HTTP_STATUS_NOT_FOUND);
                    }
                }
            }
            else
            {
                log.warn("(404) No entity request handler was found for "
                        + entityRequest.entityName());

                response.setStatus(WOResponse.HTTP_STATUS_NOT_FOUND);
            }
        }});
    }


    // ----------------------------------------------------------
    /**
     * Gets the {@link EntityResourceHandler} for the entity with the specified
     * name. This method returns null if the handler could not be found (either
     * because there is no entity with that name or because no handler was
     * registered for that entity).
     *
     * @param entityName the entity name
     * @param ec an editing context
     *
     * @return an entity resource handler, or null
     */
    private EntityResourceHandler<EOEnterpriseObject> handlerForEntityNamed(
        String entityName, EOEditingContext ec)
    {
        EOEntity ent = EOUtilities.entityNamed(ec, entityName);
        Class<?> entityClass = null;

        if (ent != null)
        {
            try
            {
                entityClass = Class.forName(ent.className());
            }
            catch (ClassNotFoundException e)
            {
                // Do nothing; error will be handled below.
            }
        }

        if (entityClass != null)
        {
            return resourceHandlers.objectForKey(entityClass);
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * A somewhat brainless check to make sure that the path provided does not
     * go higher up into the file-system than it should. We verify that it is
     * a relative path and that it does not contain any parent directory
     * references that would move it above its origin.
     */
    private static boolean validatePath(String path)
    {
        if (path == null)
        {
            return true;
        }

        File file = new File(path);

        int level = 0;

        String[] components = file.getPath().split("/");
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

        return true;
    }


    // ----------------------------------------------------------
    private EOEnterpriseObject fetchObject(EntityRequestInfo entityRequest,
                                           EntityResourceHandler<?> handler,
                                           EOEditingContext ec)
    {
        EOFetchSpecification fspec = null;

        try
        {
            long id = Long.parseLong(entityRequest.objectID());

            fspec = new EOFetchSpecification(entityRequest.entityName(),
                    ERXQ.is("id", id), null);
        }
        catch (NumberFormatException e)
        {
            fspec = handler.fetchSpecificationForFriendlyName(
                    entityRequest.objectID());
        }

        if (fspec != null)
        {
            @SuppressWarnings("unchecked")
            NSArray<? extends EOEnterpriseObject> objects =
                ec.objectsWithFetchSpecification(fspec);

            if (objects != null && objects.count() > 0)
            {
                return objects.objectAtIndex(0);
            }
        }

        return null;
    }


    // ----------------------------------------------------------
    private boolean canAccessObject(
        EOEnterpriseObject object,
        EntityResourceHandler<EOEnterpriseObject> handler,
        Session session)
    {
        if (!handler.requiresLogin())
        {
            return true;
        }
        else if (session == null)
        {
            return false;
        }
        else
        {
            User user = session.user();
            return user.hasAdminPrivileges()
                || handler.userCanAccess(object, user);
        }
    }


    // ----------------------------------------------------------
    private void generateResponse(
        WOResponse response,
        EntityResourceHandler<EOEnterpriseObject> handler,
        EOEnterpriseObject object,
        String path)
    {
        File absolutePath = handler.pathForResource(object, path);

        if (absolutePath != null && absolutePath.exists())
        {
            if (!absolutePath.isFile())
            {
                for (String indexFilename : indexFilenames)
                {
                    File indexPath = new File(absolutePath, indexFilename);

                    if (indexPath.isFile())
                    {
                        log.debug("Found index file at "
                                + indexPath.getAbsolutePath()
                                + ", using this as response");

                        absolutePath = indexPath;
                        break;
                    }
                    else
                    {
                        log.debug("Could not find index file "
                                + indexPath.getAbsolutePath());
                    }
                }
            }

            if (!absolutePath.isFile())
            {
                log.warn("(403) Cannot generate response from directory " +
                        absolutePath.getAbsolutePath());

                response.setStatus(WOResponse.HTTP_STATUS_FORBIDDEN);
            }
            else
            {
                log.debug("Generating response from contents of file: "
                        + absolutePath.getAbsolutePath());

                FileInputStream stream = null;

                try
                {
                    stream = new FileInputStream(absolutePath);
                    response.setContent(new NSData(stream, 0));
                    response.setStatus(WOResponse.HTTP_STATUS_OK);
                }
                catch (IOException e)
                {
                    log.warn("(404) An exception occurred when loading the "
                            + "content from " + absolutePath.getAbsolutePath());

                    response.setStatus(WOResponse.HTTP_STATUS_NOT_FOUND);
                }
                finally
                {
                    if (stream != null)
                    {
                        try
                        {
                            stream.close();
                        }
                        catch (IOException e)
                        {
                            // Do nothing.
                        }
                    }
                }
            }
        }
        else
        {
            String absPath = (absolutePath == null ? "<null>"
                    : absolutePath.getAbsolutePath());
            log.warn("(404) The path " + absPath + " does not exist");

            response.setStatus(WOResponse.HTTP_STATUS_NOT_FOUND);
        }
    }


    //~ Static/instance variables .............................................

    public static final String REQUEST_HANDLER_KEY = "er";

    private static final NSMutableDictionary<
        Class<?>, EntityResourceHandler<EOEnterpriseObject>>
        resourceHandlers = new NSMutableDictionary<
            Class<?>, EntityResourceHandler<EOEnterpriseObject>>();

    private static String[] indexFilenames = { "index.html", "index.htm" };

    private static final Logger log = Logger.getLogger(
            EntityResourceRequestHandler.class);
}
