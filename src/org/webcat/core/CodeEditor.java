/*==========================================================================*\
 |  $Id: CodeEditor.java,v 1.1 2012/02/23 19:21:27 aallowat Exp $
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

import org.webcat.ui.util.ComponentIDGenerator;
import org.webcat.woextensions.WCResourceManager;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

//-------------------------------------------------------------------------
/**
 * A syntax-coloring, high quality code editing text area component, using the
 * CodeMirror Javascript library (http://codemirror.net/).
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2012/02/23 19:21:27 $
 */
public class CodeEditor extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new {@code PrettyTextComponent} object.
     *
     * @param context the context
     */
    public CodeEditor(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public ComponentIDGenerator idFor = new ComponentIDGenerator(this);

    /** The DOM id of the text area that will be a child of this component. */
    public String id;

    /** The text content to display. */
    public String value;

    /** The MIME type of the content to display -- determines which syntax
        highlighting scheme to use. */
    public String mimeType;

    /** Set to true to hide the line numbers in the code editor. */
    public boolean suppressLineNumbers;

    /** Set to true to make the editor read-only. */
    public boolean readOnly;

    /** Set to true to make the code editor size to fit its contents. If the
        editor is not read-only, this will cause the size to change live as its
        contents are edited as well. */
    public boolean sizeToFit;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (id == null)
        {
            id = idFor.get("codeArea");
        }

        if (mimeType == null)
        {
            mimeType = "text/plain";
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public String resourceUrl(String partialUrl)
    {
        return WCResourceManager.resourceURLFor(
                partialUrl, context().request());
    }


    // ----------------------------------------------------------
    public String containerCssClasses()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("WCCodeEditor");

        if (sizeToFit)
        {
            buffer.append(" WCCodeEditor-sizeToFit");
        }

        return buffer.toString();
    }


    //~ Static/instance variables .............................................

}
