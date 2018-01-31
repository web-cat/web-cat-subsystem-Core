/*==========================================================================*\
 |  $Id: MetaRequestHandler.java,v 1.6 2012/06/22 16:23:18 aallowat Exp $
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

package org.webcat.core.http;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.webcat.core.Application;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * <p>
 * A WebObjects request handler that allows requests to be delegated to any of
 * a set of sub-handlers that are specified using path suffixes or regular
 * expressions.
 * </p><p>
 * This class is based on similar code found in the example servlet found in
 * the JGit library.
 * </p>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.6 $, $Date: 2012/06/22 16:23:18 $
 */
public abstract class MetaRequestHandler
    extends WORequestHandler
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new {@code MetaRequestHandler}.
     */
    public MetaRequestHandler()
    {
        bindings = new ArrayList<RequestHandlerBinderImpl>();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Handles the request by delegating it to a matching request handler.
     *
     * @param request the request
     * @return the response
     */
    @Override
    public final WOResponse handleRequest(WORequest request)
    {
        WOContext context;

        if (request.context() != null)
        {
            context = request.context();
        }
        else
        {
            context = Application.application().createContextForRequest(
                    request);
        }

        if (log.isDebugEnabled())
        {
            log.debug("> REQUEST:  " + request.toString());
        }

        WOResponse response =
            WOApplication.application().createResponseInContext(context);
        context._setResponse(response);

        String wosid = request.cookieValueForKey("wosid");
        context._setRequestSessionID(wosid);

        try
        {
            UrlPipeline pipeline = findPipeline(request);

            if (pipeline != null)
            {
                pipeline.handleRequest(request, response);
            }
            else
            {
                new ErrorRequestHandler(WOMessage.HTTP_STATUS_NOT_FOUND)
                    .handleRequest(request, response);
            }
        }
        catch (Exception e)
        {
            log.error("(500) An unhandled exception occurred", e);

            WOResponse errorResponse =
                Application.wcApplication().handleException(e, context);

            response.setContent(errorResponse.content());
            response.setStatus(WOMessage.HTTP_STATUS_INTERNAL_ERROR);
        }

        response._finalizeInContext(context);

        if (log.isDebugEnabled())
        {
            String responseString = response.toString();

            if (response.content().length() > 512)
            {
                int index = responseString.indexOf("formValues={");
                if (index == -1)
                {
                    log.debug("< RESPONSE: " + responseString);
                }
                else
                {
                    log.debug("< RESPONSE: "
                            + responseString.substring(0, index));
                }
            }
            else
            {
                log.debug("< RESPONSE: " + responseString);
            }
        }

        return response;
    }


    // ----------------------------------------------------------
    /**
     * Returns a binder that can be used to configure how requests that match
     * the specified path are serviced.
     *
     * @param path the path, which must currently be a suffix path that starts
     *     with "*"
     * @return a binder that can be used to configure the request handler
     */
    public RequestHandlerBinder serve(String path)
    {
        if (path.startsWith("*"))
        {
            return register(new SuffixPipeline.Binder(path.substring(1)));
        }
        else
        {
            throw new IllegalArgumentException("Paths not starting with \"*\""
                    + " are not currently supported.");
        }
    }


    // ----------------------------------------------------------
    /**
     * Returns a binder that can be used to configure how requests that match
     * the specified regular expression are serviced.
     *
     * @param regex the regular expression
     * @return a binder that can be used to configure the request handler
     */
    public RequestHandlerBinder serveRegex(String regex)
    {
        return register(new RegexPipeline.Binder(regex));
    }


    // ----------------------------------------------------------
    private UrlPipeline findPipeline(WORequest request)
    {
        for (UrlPipeline pipeline : pipelines())
        {
            if (pipeline.matches(request))
            {
                return pipeline;
            }
        }

        return null;
    }


    // ----------------------------------------------------------
    private RequestHandlerBinder register(RequestHandlerBinderImpl binder)
    {
        synchronized (bindings)
        {
            if (pipelines != null)
            {
                throw new IllegalStateException("Request handler has already "
                        + "been initialized.");
            }
            else
            {
                bindings.add(binder);
            }
        }

        return register((RequestHandlerBinder) binder);
    }


    // ----------------------------------------------------------
    /**
     * Called when a configured binder is being registered with the request
     * handler. Subclasses can override this to provide common manipulation for
     * all request handlers; for example, a user may want to insert filters
     * that disable caching on all requests, or require authentication.
     *
     * @param binder the binder
     * @return the binder that should be used instead of the specified binder
     */
    protected RequestHandlerBinder register(RequestHandlerBinder binder)
    {
        return binder;
    }


    // ----------------------------------------------------------
    private UrlPipeline[] pipelines()
    {
        UrlPipeline[] r = pipelines;

        if (r == null)
        {
            synchronized (bindings)
            {
                r = pipelines;

                if (r == null)
                {
                    r = createPipelines();
                    pipelines = r;
                }
            }
        }

        return r;
    }


    // ----------------------------------------------------------
    private UrlPipeline[] createPipelines()
    {
        UrlPipeline[] array = new UrlPipeline[bindings.size()];

        for (int i = 0; i < bindings.size(); i++)
        {
            array[i] = bindings.get(i).create();
        }

        return array;
    }


    //~ Static/instance variables .............................................

    public static final String REGEX_FILTER_PATH_KEY =
        "org.webcat.core.http.RegexFilterPathKey";
    public static final String REGEX_CAPTURE_GROUPS_KEY =
        "org.webcat.core.http.RegexCaptureGroupsKey";

    private List<RequestHandlerBinderImpl> bindings;
    private volatile UrlPipeline[] pipelines;

    private static final Logger log = Logger.getLogger(
            MetaRequestHandler.class);
}
