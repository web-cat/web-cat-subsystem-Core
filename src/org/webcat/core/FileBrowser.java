/*==========================================================================*\
 |  $Id: FileBrowser.java,v 1.2 2010/10/30 02:37:20 stedwar2 Exp $
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

import java.io.File;
import org.apache.log4j.Logger;
import org.webcat.ui.util.ComponentIDGenerator;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;

//-------------------------------------------------------------------------
/**
 *  A directory contents table.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2010/10/30 02:37:20 $
 */
public class FileBrowser
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new FileBrowser object.
     *
     * @param context the context for this component instance
     */
    public FileBrowser( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public File                  file;
    public boolean               isEditable            = false;
    public boolean               allowSelection        = false;
    public boolean               applyChangesOnMod     = false;
    public int                   index;
    public boolean               includeSeparator      = false;
    public Boolean               isExpanded            = null;
    public Integer               initialExpansionDepth = null;
    public FileSelectionListener fileSelectionListener = null;
    public boolean               allowSelectDir        = false;
    public NSArray<String>       allowSelectExtensions = null;
    public String                currentSelection;
    public ComponentIDGenerator  idFor;
    public String                formId;
    public String                alsoRefresh;
    public NSArray<String>       focusedFiles;

    public EditFilePage.FileEditListener fileEditListener = null;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Adds to the response of the page
     *
     * @param response The response being built
     * @param context  The context of the request
     */
    public void appendToResponse(WOResponse response, WOContext context)
    {
        idFor = new ComponentIDGenerator(this);
        log.debug("file = " + file);
        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public String browserId()
    {
        return idFor.valueForKey("browser").toString();
    }


    // ----------------------------------------------------------
    /**
     * This property is read-only, so the setter does nothing and is
     * provided only for synchronized binding pushing.
     */
    public void setBrowserId(String value)
    {
        // ignore values
    }


    // ----------------------------------------------------------
    public String buttonGroup()
    {
        return idFor.valueForKey("group").toString();
    }


    // ----------------------------------------------------------
    /**
     * This property is read-only, so the setter does nothing and is
     * provided only for synchronized binding pushing.
     */
    public void setButtonGroup(String value)
    {
        // ignore values
    }


    // ----------------------------------------------------------
    public String restartRowNumbering()
    {
        index = 0;
        return null;
    }


    // ----------------------------------------------------------
    public static interface FileSelectionListener
    {
        WOComponent selectFile(String filePath);
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger(FileBrowser.class);
}
