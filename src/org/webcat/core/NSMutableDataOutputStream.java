/*==========================================================================*\
 |  $Id: NSMutableDataOutputStream.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2008 Virginia Tech
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
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableData;
import com.webobjects.foundation.NSRange;

//-------------------------------------------------------------------------
/**
 * An output stream that uses an NSMutableData instance as its backing store.
 *
 * @author  Tony Allevato
 * @version $Id: NSMutableDataOutputStream.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public class NSMutableDataOutputStream extends OutputStream
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public NSMutableDataOutputStream()
    {
        data = new NSMutableData();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public NSData data()
    {
        return data;
    }


    // ----------------------------------------------------------
    @Override
    public void write(int b) throws IOException
    {
        data.appendByte((byte) b);
    }


    // ----------------------------------------------------------
    @Override
    public void write(byte b[], int off, int len) throws IOException
    {
        data.appendBytes(b, new NSRange(off, len));
    }


    //~ Static/instance variables .............................................

    private NSMutableData data;
}
