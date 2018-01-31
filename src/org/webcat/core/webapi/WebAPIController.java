package org.webcat.core.webapi;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.webcat.core.EOBase;
import org.webcat.core.EntityRequestInfo;
import org.webcat.core.Session;
import org.webcat.core.User;
import org.webcat.core.http.MetaRequestHandler;
import org.webcat.core.http.RequestHandlerWithResponse;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

//-------------------------------------------------------------------------
/**
 * <p>
 * The base class for a web API controller, inspired by controllers in Ruby on
 * Rails. This class should be subclassed by subsystems that want to provide a
 * RESTful API for some of their objects.
 * </p><p>
 * Like direct actions in WebObjects, web API controllers do not need to be
 * explicitly registered with any application component. If a request of the
 * format <code>Web-CAT.woa/api/[EntityName]/[id]/[action]</code> is made, then
 * the system will automatically search in any package for a class named
 * <code>[EntityName]WebAPIController</code>, create an instance of it if
 * necessary, and then call the appropriate action method.
 * </p><p>
 * Methods that handle actions should be named "[...]Action", where [...] is
 * the name of the action to be handled. If the action acts on a collection of
 * objects, then it should be parameterless (it is up to the action method to
 * determine which collection to fetch). If the action acts on a single object,
 * then it should take a single parameter that is a reference to that object.
 * For example:
 * </p>
 * <ul>
 * <li><code>.../api/Course/foo</code> calls
 *     <code>CourseWebAPIController.fooAction()</code></li>
 * <li><code>.../api/Course/5/foo</code> calls
 *     <code>CourseWebAPIController.fooAction(Course)</code></li>
 * </ul>
 * <p>
 * If no action name is specified, then <code>"index"</code> is assumed for
 * collections and <code>"show"</code> is assumed for objects:
 * </p>
 * <ul>
 * <li><code>.../api/Course</code> calls
 *     <code>CourseWebAPIController.indexAction()</code></li>
 * <li><code>.../api/Course/5</code> calls
 *     <code>CourseWebAPIController.showAction(Course)</code></li>
 * </ul>
 * <p>
 * The following action methods are reserved. Aside from these, subclasses can
 * provide their own arbitrary methods to handle custom actions:
 * </p>
 * <ul>
 * <li><code>indexAction():</code> a GET request on a collection,
 *     used to list resources</li>
 * <li><code>showAction(Object):</code> a GET request on an object,
 *     used to retrieve a resource</li>
 * <li><code>createAction():</code> a POST request on a collection,
 *     used to create new resources</li>
 * <li><code>updateAction(Object):</code> a PUT request on an object,
 *     used to update resources</li>
 * <li><code>deleteAction(Object):</code> a DELETE request on an object,
 *     used to delete a resource</li>
 * </ul>
 * <p>
 * In the event that the API identifier of an object and an action might
 * collide, then the action is assumed to be a collection action with that name
 * rather than an object action with that ID.
 * </p>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2012/06/22 16:23:17 $
 */
public abstract class WebAPIController implements RequestHandlerWithResponse
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new {@code WebAPIController}.
     */
    public WebAPIController()
    {
        cacheActionMethods();
        initializeFormatters();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Handles the request. Subclasses should not override this method; they
     * should write custom action methods as described in the Javadoc for this
     * class.
     *
     * @param request the request
     * @param response the response
     */
    public void handleRequest(WORequest request, WOResponse response)
    {
        try
        {
            this.request = request;
            this.response = response;

            session = (Session) request.context().session();
            editingContext = session.defaultEditingContext();

            Object result = dispatchAction();

            if (response.status() < 400)
            {
                formatResult(result);
            }
        }
        catch (Exception e)
        {
            log.error("(500) There was an error performing the action", e);

            response.setContent("");
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }


    // ----------------------------------------------------------
    /**
     * A convenience method to produce a 403 (Forbidden) response.
     */
    public void forbid()
    {
        response.setContent("");
        response.setStatus(HttpStatus.SC_FORBIDDEN);
    }


    // ----------------------------------------------------------
    /**
     * Gets the request associated with the current action.
     *
     * @return the request
     */
    public WORequest request()
    {
        return request;
    }


    // ----------------------------------------------------------
    /**
     * Gets the response associated with the current action.
     *
     * @return the response
     */
    public WOResponse response()
    {
        return response;
    }


    // ----------------------------------------------------------
    /**
     * Gets the session associated with the current action.
     *
     * @return the session
     */
    public Session session()
    {
        return session;
    }


    // ----------------------------------------------------------
    /**
     * Gets the user who requested the current action.
     *
     * @return the user
     */
    public User user()
    {
        return session.primeUser();
    }


    // ----------------------------------------------------------
    /**
     * Gets the session's editing context that can be used to fetch objects
     * during the current action.
     *
     * @return the editing context
     */
    public EOEditingContext editingContext()
    {
        return editingContext;
    }


    // ----------------------------------------------------------
    /**
     * Cache the reflection method handles for the actions in the class that
     * subclasses this controller class.
     */
    private void cacheActionMethods()
    {
        collectionActions = new NSMutableDictionary<String, Method>();
        objectActions = new NSMutableDictionary<String, Method>();

        for (Method method : getClass().getMethods())
        {
            String methodName = method.getName();

            if (methodName.endsWith(ACTION_SUFFIX))
            {
                String actionName = methodName.substring(0,
                        methodName.length() - ACTION_SUFFIX.length());

                Class<?>[] params = method.getParameterTypes();

                if (params.length == 0)
                {
                    collectionActions.setObjectForKey(method, actionName);
                }
                else if (params.length == 1)
                {
                    objectActions.setObjectForKey(method, actionName);
                }
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Initializes the formatters that can be used for responses from actions.
     */
    private void initializeFormatters()
    {
        formatters = new NSMutableDictionary<String, ResponseFormatter>();

        formatters.setObjectForKey(new XmlResponseFormatter(), "xml");
        formatters.setObjectForKey(new JSONResponseFormatter(), "json");
    }


    // ----------------------------------------------------------
    /**
     * Dispatches the action by calling the appropriate method on the concrete
     * controller class.
     *
     * @throws InvocationTargetException if the action method throws an
     *     exception
     * @throws IllegalAccessException if the action method is not public
     */
    private Object dispatchAction()
        throws InvocationTargetException, IllegalAccessException
    {
        String path = request.requestHandlerPath();
        EntityRequestInfo info =
            EntityRequestInfo.fromRequestHandlerPath(path, true);

        String actionOrId = info.objectID();

        if (actionOrId == null)
        {
            Method method = collectionActions.objectForKey(
                    defaultCollectionActionForRequest());

            if (method != null)
            {
                return method.invoke(this);
            }
        }
        else
        {
            // If an action and an ID happen to collide, treat it as a
            // collection action rather than an object action, because this is
            // more efficient (it doesn't require a database hit to find this
            // out).

            Method method = collectionActions.objectForKey(actionOrId);

            if (method != null)
            {
                return method.invoke(this);
            }
            else
            {
                String action = info.resourcePath();

                if (action == null)
                {
                    action = defaultObjectActionForRequest();
                }

                method = objectActions.objectForKey(action);

                if (method != null)
                {
                    EOBase object = info.requestedObject(editingContext());

                    if (object == null || !object.accessibleByUser(user()))
                    {
                        forbid();
                    }
                    else
                    {
                        return method.invoke(this, object);
                    }
                }
            }
        }

        return null;
    }


    // ----------------------------------------------------------
    /**
     * Formats the result of the action into the handler's response.
     *
     * @param result the result of the action
     * @throws Exception if an error occurs
     */
    private void formatResult(Object result) throws Exception
    {
        ResponseFormatter formatter = null;

        NSArray<String> groups = (NSArray<String>) request.userInfoForKey(
                MetaRequestHandler.REGEX_CAPTURE_GROUPS_KEY);

        if (groups.size() > WebAPIRequestHandler.FORMAT_CAPTURE_GROUP
                && groups.objectAtIndex(
                        WebAPIRequestHandler.FORMAT_CAPTURE_GROUP).length() > 0)
        {
            String format = groups.objectAtIndex(
                    WebAPIRequestHandler.FORMAT_CAPTURE_GROUP).substring(1);

            formatter = formatters.objectForKey(format);
        }

        // Use JSON by default if no format specifier is given.
        if (formatter == null)
        {
            formatter = new JSONResponseFormatter();
        }

        formatter.setResult(session().sessionID(), result);

        StringWriter writer = new StringWriter();
        formatter.formatToWriter(writer);
        response.setContent(writer.toString());
    }


    // ----------------------------------------------------------
    /**
     * Gets the default action name for requests on collections based on the
     * HTTP method used in the request.
     *
     * @return the default action name
     */
    private String defaultCollectionActionForRequest()
    {
        String method = request().method().toUpperCase();

        if (method.equals("POST"))
        {
            return CREATE_ACTION;
        }
        else
        {
            return INDEX_ACTION;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the default action name for requests on individual objects based on
     * the HTTP method used in the request.
     *
     * @return the default action name
     */
    private String defaultObjectActionForRequest()
    {
        String method = request().method().toUpperCase();

        if (method.equals("PUT"))
        {
            return UPDATE_ACTION;
        }
        else if (method.equals("DELETE"))
        {
            return DELETE_ACTION;
        }
        else
        {
            return SHOW_ACTION;
        }
    }


    //~ Static/instance variables .............................................

    private static Logger log = Logger.getLogger(WebAPIController.class);

    private static final String ACTION_SUFFIX = "Action";

    private static final String INDEX_ACTION = "index";
    private static final String CREATE_ACTION = "create";

    private static final String SHOW_ACTION = "show";
    private static final String UPDATE_ACTION = "update";
    private static final String DELETE_ACTION = "delete";

    private WORequest request;
    private WOResponse response;
    private Session session;
    private EOEditingContext editingContext;

    private NSMutableDictionary<String, Method> collectionActions;
    private NSMutableDictionary<String, Method> objectActions;
    private NSMutableDictionary<String, ResponseFormatter> formatters;
}
