/*==========================================================================*\
 |  $Id: DeliverFile.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import java.io.*;
import org.webcat.core.DeliverFile;
import org.webcat.core.Session;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  A page for deliverying a file to the user.  The file can either
 *  be presented in a new browser window, or the user can be prompted
 *  to save it on his or her machine.  See the setFileData() method
 *  for information about delivering content that is not located in
 *  a physical file (such as content stored in the database).  See
 *  setStartDownload() for information on controlling whether the
 *  content will be displayed or downloaded.
 *
 *  @author  Stephen Edwards
 *  @version $Id: DeliverFile.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public class DeliverFile
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * This is the default constructor
     *
     * @param context The page's context
     */
    public DeliverFile( WOContext context )
    {
        super( context );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Set the file to be delivered by this page.  If the file data
     * has not been set, then this value will be used to locate the
     * file on disk and load its contents for delivery.  For downloaded
     * files, this value also determines the file name used in the
     * "Save As..." dialog if setDeliveredName is never called.  Only the
     * last name in the pathname sequence is visible to the user.
     *
     * @param name the file name to use
     */
    public void setFileName( File name )
    {
        fileName = name;
    }


    // ----------------------------------------------------------
    /**
     * Set the filename that will be presented to the user in the browser
     * file download dialog. This method can be used to deliver the file
     * to the user with a different name than it has internally on the
     * server.
     *
     * If this is null (the default value) then the file will be
     * presented with the last segment of the actual filename as passed
     * into setFileName.
     *
     * @param name the file name to be presented to the user
     */
    public void setDeliveredName( String name )
    {
        deliveredName = name;
    }

    // ----------------------------------------------------------
    /**
     * Set the file content to deliver.  If this method is not called
     * (i.e., the internal file data member is left null), then the
     * file content will be read directly from the file system using
     * the associated file name.  Alternatively, if the file content
     * data is set to a non-null NSData value, then that data will be
     * used rather than reading from the file system.  This allows
     * dynamically generated content or content stored in the database
     * to be delivered to the user just like a plain file.
     *
     * @param data the file data to deliver
     */
    public void setFileData( NSData data )
    {
        fileData = data;
    }


    // ----------------------------------------------------------
    /**
     * Set the MIME type for the associated file content.  Any
     * MIME content type is acceptable.  The two simplified type names
     * "text" and "html" are also supported.  They are automatically
     * converted to the MIME content types "text/plain" and "text/html",
     * respectively.
     *
     * @param type the content type to use
     */
    public void setContentType( String type )
    {
        if ( type == null )
        {
            log.debug( "setContentType called with null parameter" );
            contentType = null;
        }
        else if ( type == "text" )
        {
            contentType = "text/plain";
        }
        else if ( type == "html" )
        {
            contentType = "text/html";
        }
        else
        {
            contentType = type;
        }
        log.debug( "setContentType("
                   + ( ( type == null ) ? "<null>" : type )
                   + ") = "
                   + ( ( contentType == null ) ? "<null>" : contentType )
                   );
    }


    // ----------------------------------------------------------
    /**
     * Determine whether the delivery action is a file download, instead
     * of viewing the file in a browser window.  If the parameter is set
     * to true, then the browser will prompt the user to save the file
     * to disk.  If the parameter is false, then the browser will simply
     * display the file content in a new window.
     *
     * @param download true to force a file download action
     */
    public void setStartDownload( boolean download )
    {
        startDownload = download;
    }


    // ----------------------------------------------------------
    /**
     * Adds to the response of the page
     *
     * @param response The response being built
     * @param context  The context of the request
     */
    public void appendToResponse( WOResponse response, WOContext context )
    {
        if ( fileName == null )
        {
            if ( fileData == null )
            {
                log.error( "no file name or file data specified" );
            }
            fileName = new File( "file.dat" );
        }

        if ( fileData == null )
        {
            log.debug( "no file data provided, attempting to read from "
                       + fileName );
            try
            {
                FileInputStream stream = new FileInputStream( fileName );
                fileData = new NSData( stream, (int)fileName.length() );
                stream.close();
            }
            catch ( Exception e )
            {
                log.error( "cannot read data from file", e );
            }
        }

        if ( contentType == null )
        {
            log.debug( "no content type provided, using default" );
            contentType = defaultContentType;
        }
        response.setContent( fileData );
        response.setHeader( contentType, "content-type" );

        // TA: Allow the download to be presented to the user with a different
        // filename than the one it has on the server
        String attachmentName;
        if (deliveredName != null)
        {
            attachmentName = deliveredName;
        }
        else
        {
            attachmentName = fileName.getName();
        }

        response.setHeader( (startDownload ? "attachment;" : "")
                                + "filename=\"" + attachmentName +"\"",
                            "content-disposition" );
        // TODO: test out caching downloads on IE
        // Work around bug in IE that prevents downloads over SSL when
        // file isn't supposed to be cached.  Here, we'll explicitly set
        // headers to allow caching for file downloads, if the browser being
        // used is IE.
        Session mySession = (Session)session();
        if ( mySession.browser().isIE() )
        {
            // Cache for 5 minutes (300 seconds)
            response.setHeader( "max-age=300", "cache-control" );
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "response:\n" + response );
            log.debug( "headers = "+ response.headers() );
            log.debug( "time = " + new NSTimestamp() );
        }
    }


    //~ Instance/static variables .............................................

    /**
     * The file data to deliver.  If it is not provided using
     * setFileData(), then the contents will be read directly from
     * the given file on disk.
     */
    protected NSData  fileData;
    /** The name (and location) of the file to deliver. */
    protected File    fileName;
    /** If not null, use this name instead of fileName as the destination name
     * shown to the user in the browser download dialog */
    protected String  deliveredName;
    /** True if the file should be delivered via a download action; false
     *  if the file should simply be displayed in a browser window. */
    protected boolean startDownload = false;
    /** The MIME type of the file's content. */
    protected String  contentType;

    private static final String defaultContentType = "text/plain";
        // "application/octet-stream";

    static Logger log = Logger.getLogger( DeliverFile.class );
}
