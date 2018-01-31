/*==========================================================================*\
 |  $Id: WOResponseOutputStream.java,v 1.2 2011/05/13 19:46:57 aallowat Exp $
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

package org.webcat.core;

import java.io.IOException;
import java.io.OutputStream;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;

//-------------------------------------------------------------------------
/**
 * An output stream that appends its content to a {@code WOResponse}.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2011/05/13 19:46:57 $
 */
public class WOResponseOutputStream extends OutputStream
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new {@code WOResponseOutputStream} that appends to the
     * specified {@code WOResponse}.
     *
     * @param response the response to append to
     */
    public WOResponseOutputStream(WOResponse response)
    {
        this.response = response;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void close()
    {
        // Do nothing; overridden to eliminate spurious checked exceptions.
    }


    // ----------------------------------------------------------
    @Override
    public void flush()
    {
        // Do nothing; overridden to eliminate spurious checked exceptions.
    }


    // ----------------------------------------------------------
    @Override
    public void write(int b) throws IOException
    {
        response.appendContentData(new NSData(new byte[] { (byte) b }));
    }


    // ----------------------------------------------------------
    @Override
    public void write(byte[] bytes, int offset, int count)
    {
        response.appendContentData(new NSData(bytes, offset, count));
    }


    //~ Static/instance variables .............................................

    private WOResponse response;
}
