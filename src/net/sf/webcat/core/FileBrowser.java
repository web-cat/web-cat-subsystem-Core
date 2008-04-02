/*==========================================================================*\
 |  $Id$
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

package net.sf.webcat.core;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import java.io.*;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 *  A directory contents table.
 *
 *  @author  Stephen Edwards
 *  @version $Id$
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

    public String                title;
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
    public NSArray               allowSelectExtensions = null;
    public String                currentSelection;

    public EditFilePage.FileEditListener fileEditListener = null;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Adds to the response of the page
     *
     * @param response The response being built
     * @param context  The context of the request
     */
    public void appendToResponse( WOResponse response, WOContext context )
    {
        index = 0;
        log.debug( "file = " + file );
        super.appendToResponse( response, context );
    }


    public static interface FileSelectionListener
    {
        WOComponent selectFile( String filePath );
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( FileBrowser.class );
}
