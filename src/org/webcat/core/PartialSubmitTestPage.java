/*==========================================================================*\
 |  $Id: PartialSubmitTestPage.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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
import org.webcat.core.WCComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;

public class PartialSubmitTestPage extends WCComponent
{
    public PartialSubmitTestPage(WOContext context)
    {
        super(context);
    }


    public StringBuffer message;
    public String fieldA;
    public String fieldB;
    public String fieldC;
    public String dialogField;
    public String radioSelection = "one";


    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        message = new StringBuffer(100);

        message.append("Request sent:\n");

        for(String key : request.formValues().allKeys())
        {
            message.append("   ");
            message.append(key);
            message.append(" = ");
            message.append(request.formValuesForKey(key));
            message.append("\n");
        }
        message.append("\n");

        super.takeValuesFromRequest(request, context);
    }


    public void setFieldA(String value)
    {
        message.append("setFieldA: was '");
        message.append(fieldA);
        message.append("', now '");
        message.append(value);
        message.append("'\n");

        fieldA = value;
    }


    public void setFieldB(String value)
    {
        message.append("setFieldB: was '");
        message.append(fieldB);
        message.append("', now '");
        message.append(value);
        message.append("'\n");

        fieldB = value;
    }


    public void setFieldC(String value)
    {
        message.append("setFieldC: was '");
        message.append(fieldC);
        message.append("', now '");
        message.append(value);
        message.append("'\n");

        fieldC = value;
    }


    public void setRadioSelection(String value)
    {
        message.append("setRadioSelection: was '");
        message.append(radioSelection);
        message.append("', now '");
        message.append(value);
        message.append("'\n");

        radioSelection = value;
    }


    public void setDialogField(String value)
    {
        message.append("setDialogField: was '");
        message.append(dialogField);
        message.append("', now '");
        message.append(value);
        message.append("'\n");

        dialogField = value;
    }


    private void summarizeFields(String caller)
    {
        message.append("Invoked ");
        message.append(caller);
        message.append("()\n");
        message.append("   fieldA = ");
        message.append(fieldA);
        message.append("\n");
        message.append("   fieldB = ");
        message.append(fieldB);
        message.append("\n");
        message.append("   fieldC = ");
        message.append(fieldC);
        message.append("\n");
        message.append("   radioSelection = ");
        message.append(radioSelection);
        message.append("\n");
        message.append("   dialogField = ");
        message.append(dialogField);
        message.append("\n");
    }


    public JavascriptGenerator submitAllFields()
    {
        summarizeFields("submitAllFields");
        return new JavascriptGenerator().refresh("message");
    }


    public JavascriptGenerator submitFieldsBC1()
    {
        summarizeFields("submitFieldsBC1");
        return new JavascriptGenerator().refresh("message");
    }


    public JavascriptGenerator submitFieldsBC2()
    {
        summarizeFields("submitFieldsBC2");
        return new JavascriptGenerator().refresh("message");
    }


    public JavascriptGenerator submitFieldA()
    {
        summarizeFields("submitFieldA");
        return new JavascriptGenerator().refresh("message");
    }


    public JavascriptGenerator submitFieldC()
    {
        summarizeFields("submitFieldC");
        return new JavascriptGenerator().refresh("message");
    }


    public JavascriptGenerator submitDialogField()
    {
        summarizeFields("submitDialogField");
        return new JavascriptGenerator().refresh("message");
    }
}
