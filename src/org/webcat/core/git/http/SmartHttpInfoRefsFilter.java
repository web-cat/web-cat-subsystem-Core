/*==========================================================================*\
 |  $Id: SmartHttpInfoRefsFilter.java,v 1.1 2011/05/13 19:46:57 aallowat Exp $
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

import java.io.IOException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.RefAdvertiser.PacketLineOutRefAdvertiser;
import org.eclipse.jgit.util.HttpSupport;
import org.jfree.util.Log;
import org.webcat.core.NSMutableDataOutputStream;
import org.webcat.core.http.RequestFilter;
import org.webcat.core.http.RequestFilterChain;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * A base class for filters that implement Git Smart HTTP services.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2011/05/13 19:46:57 $
 */
public abstract class SmartHttpInfoRefsFilter implements RequestFilter
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new {@code SmartHTTPInfoRefsFilter} for the specified service.
     *
     * @param service the service
     */
    public SmartHttpInfoRefsFilter(String service)
    {
        this.service = service;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Delegates to the
     * {@link #advertise(WORequest, Repository, PacketLineOutRefAdvertiser)}
     * method on the class and then appends the advertisement to the response.
     *
     * @param request the request
     * @param response the response
     * @param filterChain the filter chain
     * @throws Exception if an error occurred
     */
    public void filterRequest(WORequest request, WOResponse response,
            RequestFilterChain filterChain) throws Exception
    {
        if (service.equals(request.formValueForKey("service")))
        {
            try
            {
                Repository repo =
                    RepositoryRequestUtils.repositoryFromRequest(request);

                response.setHeader("application/x-" + service
                        + "-advertisement", HttpSupport.HDR_CONTENT_TYPE);

                NSMutableDataOutputStream output =
                    new NSMutableDataOutputStream();
                PacketLineOut lineOut = new PacketLineOut(output);

                lineOut.writeString("# service=" + service + "\n");
                lineOut.end();

                advertise(request, repo,
                        new PacketLineOutRefAdvertiser(lineOut));

                output.close();
                response.appendContentData(output.data());
            }
            catch (IOException e)
            {
                Log.error("(403) An exception occurred when advertising the "
                        + "repository", e);
                response.setStatus(WOMessage.HTTP_STATUS_FORBIDDEN);
            }
        }
        else
        {
            filterChain.filterRequest(request, response);
        }
    }


    // ----------------------------------------------------------
    /**
     * Generates the advertisements for the service.
     *
     * @param request the request
     * @param repository the repository
     * @param advertiser the object used to generate the advertisements
     * @throws IOException if an I/O error occurred
     */
    protected abstract void advertise(WORequest request, Repository repository,
            PacketLineOutRefAdvertiser advertiser) throws IOException;


    //~ Static/instance variables .............................................

    private String service;
}
