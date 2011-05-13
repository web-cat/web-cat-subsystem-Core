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

import java.io.IOException;
import java.io.OutputStream;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.util.HttpSupport;
import org.webcat.core.FileUtilities;
import org.webcat.core.WOResponseOutputStream;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class GitRawComponent extends GitWebComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitRawComponent(WOContext context)
    {
        super(context);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        ObjectId oid = gitContext().objectId();

        WOResponseOutputStream output = new WOResponseOutputStream(response);
        gitContext().repository().writeBlobToStream(oid, output);
        output.close();

        String mimeType = FileUtilities.mimeType(gitContext().path());
        response.setHeader(mimeType, HttpSupport.HDR_CONTENT_TYPE);
    }
}
