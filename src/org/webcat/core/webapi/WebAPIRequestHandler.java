package org.webcat.core.webapi;

import org.apache.http.HttpStatus;
import org.webcat.core.EOBase;
import org.webcat.core.EntityRequestInfo;
import org.webcat.core.User;
import org.webcat.core.http.BasicAuthenticationFilter;
import org.webcat.core.http.MetaRequestHandler;
import org.webcat.core.http.NoCacheRequestFilter;
import org.webcat.core.http.RequestHandlerBinder;
import org.webcat.core.http.RequestHandlerWithResponse;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSUtilities;

//-------------------------------------------------------------------------
/**
 * The request handler for the Web-CAT external web API.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2012/06/22 16:23:17 $
 */
public class WebAPIRequestHandler extends MetaRequestHandler
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new {@code WebAPIRequestHandler}.
     */
    public WebAPIRequestHandler()
    {
        controllerCache = new NSMutableDictionary<String, WebAPIController>();

        serveRegex("(.+" + FORMAT_NEG
                + "(?:/.+" + FORMAT_NEG
                + "(?:/.+" + FORMAT_NEG
                + ")?)?)" + FORMAT_CAPTURE + "$").with(new WebAPIDispatcher());
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Overrides the {@link MetaRequestHandler#register(RequestHandlerBinder)}
     * method to ensure that helper filters are attached to every service
     * binder.
     *
     * @param binder the incoming binder
     * @return the possibly modified binder
     */
    @Override
    protected RequestHandlerBinder register(RequestHandlerBinder binder)
    {
        return binder.through(new NoCacheRequestFilter())
                     .through(new ValidUserOnlyAuthenticationFilter());
    }


    // ----------------------------------------------------------
    /**
     * Retrieves the {@link WebAPIController} for the specified entity type,
     * creating it for the first time if necessary.
     *
     * @param entityName the type of entity for which to retrieve the
     *     controller
     * @return the controller, or null if none was found
     */
    private WebAPIController controllerForType(String entityName)
    {
        WebAPIController controller = controllerCache.objectForKey(entityName);

        if (controller == null)
        {
            try
            {
                Class<?> klass = _NSUtilities.classWithName(
                        entityName + CONTROLLER_SUFFIX);

                controller = (WebAPIController) klass.newInstance();
                controllerCache.setObjectForKey(controller, entityName);
            }
            catch (Exception e)
            {
                return null;
            }
        }

        return controller;
    }


    //~ Inner classes .........................................................

    // ----------------------------------------------------------
    /**
     * This authentication filter only verifies that the user is a valid user
     * (that is, it always returns true). It is up to the individual actions to
     * determine whether the action should be allowed based on the access
     * privileges of the user making the request, and to filter the response
     * based on that user.
     */
    private class ValidUserOnlyAuthenticationFilter
        extends BasicAuthenticationFilter
    {
        // ----------------------------------------------------------
        @Override
        protected String realmForContext(WOContext context)
        {
            return "Web-CAT Web API";
        }


        // ----------------------------------------------------------
        @Override
        protected boolean userHasAccess(User user)
        {
            return true;
        }
    }


    // ----------------------------------------------------------
    /**
     * Dispatches the API call to the appropriate controller class and action
     * method.
     */
    private class WebAPIDispatcher implements RequestHandlerWithResponse
    {
        // ----------------------------------------------------------
        public void handleRequest(WORequest request, WOResponse response)
                throws Exception
        {
            String path = request.requestHandlerPath();
            EntityRequestInfo info =
                EntityRequestInfo.fromRequestHandlerPath(path, true);

            if (info.entityName() != null)
            {
                WebAPIController controller =
                    controllerForType(info.entityName());

                if (controller != null)
                {
                    controller.handleRequest(request, response);
                }
                else
                {
                    response.setContent("");
                    response.setStatus(HttpStatus.SC_FORBIDDEN);
                }
            }
            else
            {
                response.setContent("");
                response.setStatus(HttpStatus.SC_FORBIDDEN);
            }
        }
    }


    //~ Static/instance variables .............................................

    /**
     * The request handler key that is used to register the handler.
     */
    public static final String REQUEST_HANDLER_KEY = "api";

    private static final String FORMAT_PATTERN = "(?:xml|json)";
    private static final String FORMAT_CAPTURE = "(\\." + FORMAT_PATTERN + ")?";
    private static final String FORMAT_NEG = "(?<!\\." + FORMAT_PATTERN + ")";

    // The index of the capture group that contains the response format (since
    // Java doesn't support named capture groups until 1.7). We must make sure
    // that all of the patterns in the methods above are arranged so that this
    // is always the same.
    public static final int FORMAT_CAPTURE_GROUP = 2;

    private static final String CONTROLLER_SUFFIX = "WebAPIController";

    private NSMutableDictionary<String, WebAPIController> controllerCache;
}
