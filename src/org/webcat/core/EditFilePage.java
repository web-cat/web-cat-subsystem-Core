/*==========================================================================*\
 |  $Id: EditFilePage.java,v 1.2 2012/02/23 19:21:27 aallowat Exp $
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
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.foundation.ERXFileUtilities;

// -------------------------------------------------------------------------
/**
 * This class presents the list of scripts (grading steps) that
 * are available for selection.
 *
 * @author Stephen Edwards
 * @version $Id: EditFilePage.java,v 1.2 2012/02/23 19:21:27 aallowat Exp $
 */
public class EditFilePage
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * This is the default constructor
     *
     * @param context The page's context
     */
    public EditFilePage( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public File   file;
    public File   baseFile;
    public String fileContents;

    public FileEditListener fileEditListener = null;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse( WOResponse response, WOContext context )
    {
        if ( fileContents == null )
        {
            try
            {
                fileContents = ERXFileUtilities.stringFromFile( file );
            }
            catch ( java.io.IOException e )
            {
                error( e.getMessage() );
            }
        }
        super.appendToResponse( response, context );
    }


    // ----------------------------------------------------------
    public String sideStepTitle()
    {
        return "Edit File: " + file.getName();
    }


    // ----------------------------------------------------------
    public NSTimestamp lastModified()
    {
        return new NSTimestamp( file.lastModified() );
    }


    // ----------------------------------------------------------
    public String fileName()
    {
        String fileName = file.getPath();
        log.debug ( "fileName = " + fileName );
        if ( baseFile != null )
        {
            String parent = baseFile.getParent();
            log.debug ( "parent = " + fileName );
            if ( parent != null && parent.length() > 0 )
            {
                fileName = fileName.substring( parent.length() + 1 );
            }
        }
        log.debug ( "fileName = " + fileName );
        fileName = fileName.replaceAll( "\\\\", "/" );
        return fileName;
    }


    // ----------------------------------------------------------
    public String fileMimeType()
    {
        return FileUtilities.mimeType(file);
    }


    // ----------------------------------------------------------
    public WOComponent cancelEdit()
    {
        log.debug( "cancelEdit(), nextPage = " + nextPage );
        clearMessages();
        return nextPage;
    }


    // ----------------------------------------------------------
    public void save()
    {
        try
        {
            ERXFileUtilities.stringToFile( fileContents, file );
            if ( fileEditListener != null )
            {
                fileEditListener.saveFile( file.getPath() );
            }
        }
        catch ( Exception e )
        {
            log.debug( "save(): ", e );
            error( e.getMessage() );
        }
    }


    // ----------------------------------------------------------
    public WOComponent saveAndDone()
    {
        log.debug( "saveAndDone(), nextPage = " + nextPage );
        save();
        if ( hasMessages() )
        {
            return null;
        }
        else
        {
            return nextPage;
        }
    }


    // ----------------------------------------------------------
    public WOComponent saveAndContinue()
    {
        log.debug( "saveAndContinue()" );
        save();
        return null;
    }


    public static interface FileEditListener
    {
        void saveFile( String filePath );
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( EditFilePage.class );
}
