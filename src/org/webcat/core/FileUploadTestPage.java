/*==========================================================================*\
 |  $Id: FileUploadTestPage.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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

import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.core.ValidatingAction;
import org.webcat.core.WCComponent;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableDictionary;

public class FileUploadTestPage extends WCComponent
{
    public FileUploadTestPage(WOContext context)
    {
        super(context);
    }

    public NSMutableDictionary<String, Object> formValues =
        new NSMutableDictionary<String, Object>();


    public String validateFileUpload()
    {
        if (formValues.objectForKey("fileData") == null)
        {
            return "You must upload a file.";
        }
        else
        {
            return null;
        }
    }


    public WOActionResults submit()
    {
        return new ValidatingAction(this);
    }


    public JavascriptGenerator fileWasUploaded()
    {
        return new JavascriptGenerator().refresh("fileInfo");
    }
}
