/*==========================================================================*\
 |  $Id: GitRequestHandler.java,v 1.2 2012/06/22 16:23:17 aallowat Exp $
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

package org.webcat.core.git.http;

import org.eclipse.jgit.lib.Constants;
import org.webcat.core.Application;
import org.webcat.core.EOBase;
import org.webcat.core.RepositoryProvider;
import org.webcat.core.http.MetaRequestHandler;
import org.webcat.core.http.NoCacheRequestFilter;
import org.webcat.core.http.RequestHandlerBinder;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

//-------------------------------------------------------------------------
/**
 * A request handler that provides Git access to file repositories on Web-CAT
 * (such as a user's storage area, the storage area for a course, and
 * so forth) using Git's smart HTTP protocol.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2012/06/22 16:23:17 $
 */
public class GitRequestHandler extends MetaRequestHandler
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new GitRequestHandler.
     */
    public GitRequestHandler()
    {
        // Enable Smart HTTP receive and upload.

        serve("*/git-receive-pack").with(new ReceivePackRequestHandler());
        serve("*/git-upload-pack").with(new UploadPackRequestHandler());

        // Filter accesses to the Git info/refs file through our advertisers
        // so that clients will use Smart HTTP functionality.

        serve("*/" + Constants.INFO_REFS)
            .through(new UploadPackRequestHandler.InfoRefs())
            .through(new ReceivePackRequestHandler.InfoRefs())
            .with(new InfoRefsRequestHandler());

        // Finally, serve any request that doesn't match the above with a
        // standard Web-DAV response.

        serve("*").with(new GitWebRequestHandler());
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets an absolute URL to access an EO file store.
     *
     * @param context the request context
     * @param eo the EO whose file store should be accessed
     * @param path the entity-relative path to the resource
     * @return the file store URL
     */
    public static String completeURLForRepositoryPath(WOContext context,
            EOBase eo, String path)
    {
        String requestPath = requestPathForRepositoryPath(eo, path);

        if (requestPath == null)
        {
            return null;
        }
        else
        {
            return Application.completeURLWithRequestHandlerKey(
                    context, REQUEST_HANDLER_KEY,
                    requestPath, null, true, 0);
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets a relative URL to access an EO file store.
     *
     * @param context the request context
     * @param eo the EO whose file store should be accessed
     * @param path the entity-relative path to the resource
     * @return the file store URL
     */
    public static String urlForRepositoryPath(WOContext context,
            EOBase eo, String path)
    {
        String requestPath = requestPathForRepositoryPath(eo, path);

        if (requestPath == null)
        {
            return null;
        }
        else
        {
            return context.urlWithRequestHandlerKey(REQUEST_HANDLER_KEY,
                    requestPath, null);
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the part of an EO file store URL that follows the request handler
     * key.
     *
     * @param eo the EO whose file store should be accessed
     * @param path the entity-relative path to the resource
     * @return the file store URL suffix
     */
    private static String requestPathForRepositoryPath(EOBase eo,
            String path)
    {
        if (!(eo instanceof RepositoryProvider))
        {
            return null;
        }

        String entityName = eo.entityName();
        String repoId = eo.apiId();

        StringBuffer buffer = new StringBuffer();

        buffer.append(entityName);
        buffer.append("/");
        buffer.append(repoId);

        if (path != null && path.length() > 0)
        {
            buffer.append("/");
            buffer.append(path);
        }

        return buffer.toString();
    }


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
                     .through(new GitAuthenticationFilter())
                     .through(new RepositoryRequestFilter());
    }


    //~ Static/instance variables .............................................

    /**
     * The request handler key that is used to register the handler.
     */
    public static final String REQUEST_HANDLER_KEY = "git";
}
