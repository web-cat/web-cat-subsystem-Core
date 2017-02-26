/*==========================================================================*\
 |  $Id$
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

import java.util.EnumMap;
import org.apache.log4j.Logger;
import org.eclipse.jgit.util.HttpSupport;
import org.webcat.core.Application;
import org.webcat.core.EOBase;
import org.webcat.core.EntityRequestInfo;
import org.webcat.core.Session;
import org.webcat.core.git.GitRepository;
import org.webcat.core.http.RequestHandlerWithResponse;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

//-------------------------------------------------------------------------
/**
 * <p>
 * The default handler for Git requests that don't satisfy the Smart HTTP
 * protocol. This handler provides a web interface for browsing the Git
 * repository, similar to existing Gitweb scripts in existence.
 * </p><p>
 * The URLs generated and handled by this request handler aim to be a subset of
 * those in some Gitweb scripts used elsewhere on the web, but no guarantee is
 * made that the schemes are compatible in any way.
 * </p>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class GitWebRequestHandler implements RequestHandlerWithResponse
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Handles the request.
     *
     * @param request the request
     * @param response the response
     * @throws Exception if an exception occurs
     */
    public void handleRequest(WORequest request, WOResponse response)
            throws Exception
    {
        long start = System.currentTimeMillis();
        String path = request.requestHandlerPath();
        EntityRequestInfo requestInfo =
            EntityRequestInfo.fromRequestHandlerPath(path);

        Session session = (Session) request.context().session();
        EOEditingContext ec = session.defaultEditingContext();

        EOBase object = requestInfo.requestedObject(ec);
        GitRepository repository = GitRepository.repositoryForObject(object);

        String repoName = requestInfo.entityName() + "/"
            + requestInfo.objectID();

        GitWebContext gitContext = GitWebContext.parse(object, repository,
                repoName, requestInfo.resourcePath());

        generateResponse(gitContext, request, response);
        long end = System.currentTimeMillis();
        if (log.isDebugEnabled())
        {
            double time = (end - start)/1000.0;
            log.debug("handleRequest(): " + time + "s\n    url = "
                + request.uri());
        }
    }


    // ----------------------------------------------------------
    /**
     * Generates an appropriate response for the specified object, depending on
     * its type.
     *
     * @param repository the repository
     * @param commitId the commit id
     * @param objectId the object id
     * @param request the request
     * @param response the response
     * @throws Exception if an error occurs
     */
    private void generateResponse(GitWebContext gitContext, WORequest request,
            WOResponse response) throws Exception
    {
        GitWebComponent page = null;

        Class<? extends GitWebComponent> pageClass =
            actionPages.get(gitContext.mode());

        if (pageClass != null)
        {
            page = Application.wcApplication().pageWithName(
                    pageClass, request.context());
        }

        if (page != null)
        {
            page.setGitContext(gitContext);

            WOResponse pageResponse = page.generateResponse();
            response.appendContentData(pageResponse.content());

            if (gitContext.mode() != GitWebMode.RAW)
            {
                response.setHeader("text/html", HttpSupport.HDR_CONTENT_TYPE);
            }
        }
        else
        {
            response.setStatus(WOMessage.HTTP_STATUS_FORBIDDEN);
        }
    }


    //~ Static/instance variables .............................................

    private static final EnumMap<GitWebMode,
        Class<? extends GitWebComponent>> actionPages;

    private static final Logger log =
        Logger.getLogger(GitWebRequestHandler.class);


    // ----------------------------------------------------------
    static
    {
        actionPages = new EnumMap<GitWebMode,
            Class<? extends GitWebComponent>>(GitWebMode.class);

        actionPages.put(GitWebMode.BRANCHES, GitBranchesPage.class);
        actionPages.put(GitWebMode.TREE, GitTreePage.class);
        actionPages.put(GitWebMode.BLOB, GitBlobPage.class);
        actionPages.put(GitWebMode.RAW, GitRawComponent.class);
        actionPages.put(GitWebMode.COMMIT, GitCommitPage.class);
        actionPages.put(GitWebMode.COMMITS, GitCommitsPage.class);
    }
}
