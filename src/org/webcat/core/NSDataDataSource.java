/*==========================================================================*\
 |  $Id: NSDataDataSource.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import com.webobjects.foundation.NSData;

//-------------------------------------------------------------------------
/**
 * A data source used by the e-mail implementation to provide attachments that
 * come from NSData objects.
 *
 * @author  Tony Allevato
 * @version $Id: NSDataDataSource.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public class NSDataDataSource implements DataSource
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public NSDataDataSource(String name, NSData data)
    {
        this.name = name;
        this.data = data;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String getContentType()
    {
        return FileTypeMap.getDefaultFileTypeMap().getContentType(name);
    }


    // ----------------------------------------------------------
    public InputStream getInputStream() throws IOException
    {
        return data.stream();
    }


    // ----------------------------------------------------------
    public String getName()
    {
        return name;
    }


    // ----------------------------------------------------------
    public OutputStream getOutputStream() throws IOException
    {
        return null;
    }


    //~ Static/instance variables .............................................

    private String name;
    private NSData data;
}
