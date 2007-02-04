/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License
 |  along with Web-CAT; if not, write to the Free Software
 |  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 |
 |  Project manager: Stephen Edwards <edwards@cs.vt.edu>
 |  Virginia Tech CS Dept, 660 McBryde Hall (0106), Blacksburg, VA 24061 USA
\*==========================================================================*/

package net.sf.webcat.core;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.extensions.ERXFileUtilities;
import java.io.File;
import java.io.FilenameFilter;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 *  One row in a directory contents table.
 *
 *  @author  Stephen Edwards
 *  @version $Id$
 */
public class FileBrowserRow
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new FileBrowserRow object.
     *
     * @param context the context for this component instance
     */
    public FileBrowserRow( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public Boolean isExpanded     = null;
    public boolean isEditable     = false;
    public boolean allowSelection = false;
    public boolean isLast         = true;
    public File    file;
    public File    baseFile;
    public int     depth          = 0;
    public int     myRowNumber    = -1;
    public int     index;
    public Integer initialExpansionDepth;
    public boolean allowSelectDir        = false;
    public NSArray allowSelectExtensions = null;
    public boolean applyChangesOnMod     = false;
    public String  currentSelection;

    // For the spacer repetition
    public int     spacerIndex;
    public NSMutableArray isLastEntry;
    public IsLastEntryAtThisLevel spacerWalker;

    // For the sub-file repetition
    public int     subFileIndex;
    public NSArray contents = emptyContents;
    public File    subFileWalker;

    public FileBrowser.FileSelectionListener fileSelectionListener = null;
    public EditFilePage.FileEditListener     fileEditListener      = null;


    //~ Methods ...............................................................

    private static class NotDotOrDotDot
        implements FilenameFilter
    {
        public boolean accept( File file, String name )
        {
            return !name.equals( "." )
                && !name.equals( ".." );
        }
    }
    private static final FilenameFilter notDotOrDotDot = new NotDotOrDotDot();
    private static final NSArray emptyContents = new NSArray();
    
    
    // ----------------------------------------------------------
    /**
     * Adds to the response of the page
     *
     * @param response The response being built
     * @param context  The context of the request
     */
    public void appendToResponse( WOResponse response, WOContext context )
    {
        contents = emptyContents;
        myRowNumber = index;
        if ( isExpanded == null )
        {
            if ( initialExpansionDepth != null )
            {
                log.debug( "initialExpansionDepth = " + initialExpansionDepth );
                log.debug( "my depth = " + depth );
                isExpanded = ( initialExpansionDepth.intValue() >= depth )
                    ? Boolean.TRUE
                    : Boolean.FALSE;
                log.debug( "isExpanded = " + isExpanded );
            }
            else
            {
                isExpanded = isArchive() ? Boolean.FALSE : Boolean.TRUE;
            }
        }
        if ( isLastEntry == null )
        {
            isLastEntry = new NSMutableArray();
        }
        while ( isLastEntry.count() <= depth )
        {
            isLastEntry.addObject( new IsLastEntryAtThisLevel() );
        }
        ( (IsLastEntryAtThisLevel)isLastEntry.objectAtIndex( depth ) )
            .last = isLast;
        if ( isExpanded.booleanValue() )
        {
            if ( file.isDirectory() )
            {
                contents = new NSArray( file.listFiles( notDotOrDotDot ) );
            }
            else
            {
                // Don't handle zip/jar archives yet!
            }
        }
        else
        {
            contents = emptyContents;
        }
        super.appendToResponse( response, context );
        while ( isLastEntry.count() > depth )
        {
            isLastEntry.removeLastObject();
        }
    }


    // ----------------------------------------------------------
//    /**
//     * <!-- DOCUMENT ME! -->
//     *
//     * @return <!-- DOCUMENT ME! -->
//     */
//    public WOComponent deleteDir()
//    {
//        if ( !getIsRootDir() )
//        {
//            EOEditingContext ec = session().defaultEditingContext();
//            ec.deleteObject( dirPath );
//            dirPath.removeFromSubdirectories( dirPath );
//            ec.saveChanges();
//            System.out.println( "Deleted directory " + dirPath.name() );
//        }
//        else
//        {
//            System.out.println( "don't delete me! I'm a root directory!" );
//        }
//        return null;
//    }


    // ----------------------------------------------------------
//    /**
//     * <!-- DOCUMENT ME! -->
//     *
//     * @return <!-- DOCUMENT ME! -->
//     */
//    public WOComponent deleteFile()
//    {
//        session().defaultEditingContext().deleteObject( curFile );
//        dirPath.removeFromFiles( curFile );
//        return null;
//    }


    // ----------------------------------------------------------
//    /**
//     * <!-- DOCUMENT ME! -->
//     *
//     * @return <!-- DOCUMENT ME! -->
//     */
//    public WOComponent newDir()
//    {
//        WCDirectory      newD = new WCDirectory();
//        EOEditingContext ec = session().defaultEditingContext();
//        ec.insertObject( newD );
//        dirPath.addToSubdirectories( newD );
//
//        // find a new directory name
//        String fname = "untitled";
//        if ( dirPath.dirWithName( fname ) != null )
//        {
//            fname += "-";
//            int i = 1;
//            while ( dirPath.dirWithName( fname + i ) != null )
//            {
//                i++;
//            }
//            fname += i;
//        }
//        newD.setName( fname );
//        newD.setParent( dirPath );
//        newD.setUser( ( (Session)session() ).getUser() );
//        newD.setCreate_time( new NSTimestamp() );
//        ec.saveChanges();
//        return null;
//    }


    // ----------------------------------------------------------
//    /**
//     * <!-- DOCUMENT ME! -->
//     *
//     * @return <!-- DOCUMENT ME! -->
//     */
//    public WOComponent newFile()
//    {
//        WCFile           newF = new WCFile();
//        EOEditingContext ec = session().defaultEditingContext();
//        ec.insertObject( newF );
//        dirPath.addToFiles( newF );
//
//        // find a new filename.
//        String fname = "untitled";
//        if ( dirPath.fileWithName( fname ) != null )
//        {
//            fname += "-";
//            int i = 1;
//            while ( dirPath.fileWithName( fname + i ) != null )
//            {
//                i++;
//            }
//            fname += i;
//        }
//        newF.setFilename( fname );
//        newF.setText( new NSData() );
//        newF.setCreate_time( new NSTimestamp() );
//        newF.setLast_mod_time( new NSTimestamp() );
//        newF.setDirectory( dirPath );
//        newF.setUser( ( (Session)session() ).getUser() );
//        ec.saveChanges();
//        return null;
//    }


    // ----------------------------------------------------------
//    /**
//     * <!-- DOCUMENT ME! -->
//     *
//     * @return <!-- DOCUMENT ME! -->
//     */
//    public WOComponent uploadNewFile()
//    {
//        return null;
//    }


    // ----------------------------------------------------------
//    /**
//     * <!-- DOCUMENT ME! -->
//     *
//     * @return <!-- DOCUMENT ME! -->
//     */
//    public WOComponent viewFile()
//    {
//        return EditFile.edit( curFile, this );
//    }


    // ----------------------------------------------------------
//    /**
//     * <!-- DOCUMENT ME! -->
//     *
//     * @return <!-- DOCUMENT ME! -->
//     */
//    public WOComponent zipDir()
//    {
//        try
//        {
//            WOResponse            response = context().response();
//            ByteArrayOutputStream boas = new ByteArrayOutputStream();
//            ZipOutputStream       zos  = new ZipOutputStream( boas );
//            dirPath.appendToZip( zos );
//            zos.close();
//            response.setContent( new NSData( boas.toByteArray() ) );
//            response.setHeader( "Content-Type", "application/x-zip" );
//        }
//        catch ( IOException ex )
//        {
//            NSLog.err.appendln(
//                    "Exception while trying to build a zip of the directory: "
//                    + ex.toString() );
//        }
//        return null;
//    }


    // ----------------------------------------------------------
//    /**
//     * <!-- DOCUMENT ME! -->
//     *
//     * @return <!-- DOCUMENT ME! -->
//     */
//    public WOComponent zipRoot()
//    {
//        return null;
//    }


    // ----------------------------------------------------------
    /**
     * <!-- DOCUMENT ME! -->
     *
     * @return <!-- DOCUMENT ME! -->
     */
    public int innerDepth()
    {
        return depth + 1;
    }

    
    // ----------------------------------------------------------
    /**
     * <!-- DOCUMENT ME! -->
     *
     * @return <!-- DOCUMENT ME! -->
     */
    public boolean currentEntryIsLastAtItsLevel()
    {
        return ( subFileIndex == contents.count() - 1 );
    }

    
    // ----------------------------------------------------------
    /**
     * A mutable boolean value holder for storing in the
     * isLastEntry array.
     */
    public class IsLastEntryAtThisLevel
    {
        public boolean last = false;
    }

    
    // ----------------------------------------------------------
    /**
     * Return this row's number.
     * @return this row's number
     */
    public int rowNumber()
    {
        return myRowNumber;
    }


    // ----------------------------------------------------------
    /**
     * Return true on the final spacer rep.
     * @return true for the last spacer in a sequence
     */
    public boolean isFinalSpacerRep()
    {
        return spacerIndex == depth;
    }

    
    // ----------------------------------------------------------
    /**
     * Return true if this file represents an archive file.
     * @return true if this file is a zip or jar archive
     */
    public boolean isArchive()
    {
        String name = file.getName().toLowerCase();
        return file.isFile() &&
            ( name.endsWith( ".zip" ) || name.endsWith( ".jar" ) );
    }

    
    // ----------------------------------------------------------
    /**
     * Return true if this file represents an archive or a directory.
     * @return true if this file is a directory or archive file
     */
    public boolean isDirectory()
    {
        return file.isDirectory() || isArchive();
    }


    // ----------------------------------------------------------
    /**
     * Determine if a file can be viewed inline.
     * @return true if the file can be viewed in the browser
     */
    public boolean isViewable()
    {
        return WCFile.showInline( file );
    }


    // ----------------------------------------------------------
    /**
     * Determine if a file can be edited in a browser window.
     * @return true if this file can be  edited as a text file
     */
    public boolean canEdit()
    {
        return isEditable && WCFile.isEditable( file );
    }


    // ----------------------------------------------------------
    /**
     * Determine if a file can be deleted.
     * @return true if this file can be  deleted
     */
    public boolean canDelete()
    {
        return isEditable;
    }


    // ----------------------------------------------------------
    /**
     * View the selected file.
     * @return a view page for the selected file
     */
    public WOComponent viewFile()
    {
        DeliverFile nextPage = (DeliverFile)pageWithName(
            DeliverFile.class.getName() );
        nextPage.setFileName( file );
        nextPage.setContentType( WCFile.mimeType( file ) );
        nextPage.setStartDownload( !WCFile.showInline( file ) );
        return nextPage;
    }


    // ----------------------------------------------------------
    /**
     * Edit the selected file.
     * @return an edit page for the selected file
     */
    public WOComponent editFile()
    {
        if ( applyChangesOnMod )
        {
            ( (Session)session() ).commitLocalChanges();
        }
        EditFilePage nextPage = (EditFilePage)pageWithName(
            EditFilePage.class.getName() );
        nextPage.file = file;
        nextPage.baseFile = baseFile;
        nextPage.nextPage = (WCComponent)context().page();
        nextPage.fileEditListener = fileEditListener;
        return nextPage;
    }


    // ----------------------------------------------------------
    /**
     * Delete the selected file.
     * @return the current page, which will be reloaded
     */
    public WOComponent deleteFile()
    {
        if ( applyChangesOnMod )
        {
            ( (Session)session() ).commitLocalChanges();
        }
        log.debug( "delete: " + file.getPath() );
        if ( file.isDirectory() )
        {
            ERXFileUtilities.deleteDirectory( file );
        }
        else
        {
            file.delete();
        }
        return null;
    }


    // ----------------------------------------------------------
    public String deleteFileTitle()
    {
        if ( isArchive() )
        {
            return "Delete this archive and its contents";
        }
        else if ( file.isDirectory() )
        {
            return "Delete this directory and its contents";
        }
        else
        {
            return "Delete this file";
        }
    }


    // ----------------------------------------------------------
    public String deleteFileOnClick()
    {
        String message = "Are you sure you want to delete ";
        if ( isArchive() )
        {
            message += "the archive file " + file.getName()
                + " and all its contents?";
        }
        else if ( file.isDirectory() )
        {
            message += "the directory " + file.getName()
                + " and all its contents?";
        }
        else
        {
            message += "the file " + file.getName() + "?";
        }
        return "return confirm('" + message + "')";
    }


    // ----------------------------------------------------------
    /**
     * View or download the selected file.
     * @return a download page for the selected file
     * @throws java.io.IOException if an error occurs reading the file
     */
    public WOComponent downloadFile()
        throws java.io.IOException
    {
        DeliverFile nextPage = (DeliverFile)pageWithName(
            DeliverFile.class.getName() );
        if ( file.isDirectory() )
        {
            File zipFile = new File( file.getName() + ".zip" );
            nextPage.setFileName( zipFile );
            nextPage.setContentType( WCFile.mimeType( zipFile ) );
            ByteArrayOutputStream boas = new ByteArrayOutputStream();
            ZipOutputStream       zos  = new ZipOutputStream( boas );
            WCFile.appendToZip(
                file,
                zos,
                file.getCanonicalPath().length() );
            zos.close();
            nextPage.setFileData( new NSData( boas.toByteArray() ) );
            boas.close();
        }
        else
        {
            nextPage.setFileName( file );
            nextPage.setContentType( WCFile.mimeType( file ) );
        }
        nextPage.setStartDownload( true );
        return nextPage;
    }

    
    // ----------------------------------------------------------
    /**
     * Return the URL for the download icon to use for this file.
     * @return the Core framework file name for the desired download
     *  icon, based on whether or not this file is a directory
     */
    public String downloadIcon()
    {
        if ( file.isDirectory() )
        {
            return "icons/archive.gif";
        }
        else
        {
            return "icons/download.gif";
        }
    }

    
//    // ----------------------------------------------------------
//    public WOActionResults invokeAction( WORequest arg0, WOContext arg1 )
//    {
//        log.debug( "invokeAction(): request.formValues = " + arg0.formValues() );
////        log.debug( "invokeAction(): context = " + arg1 );
//        log.debug( "invokeAction(): senderID = " + arg1.senderID() );
//        log.debug( "invokeAction(): elementID = " + arg1.elementID() );
//        return super.invokeAction( arg0, arg1 );
//    }


    // ----------------------------------------------------------
    /**
     * Toggle this entry between open and closed, if it is a directory.
     * @return this page, so that it reloads
     */
    public WOComponent toggleExpansion()
    {
        isExpanded = ( isExpanded.booleanValue() || isArchive() )
                        ? Boolean.FALSE
                        : Boolean.TRUE;
        log.debug( "toggleExpansion(): now isExpanded = " + isExpanded );
        return context().page();
    }


    // ----------------------------------------------------------
    /**
     * Return the URL for the icon representing this file's type.
     * @return the icon file URL
     */
    public String iconURL()
    {
        String result = WCFile.iconURL( file );
        log.debug( "iconURL(" + file + ") = " + result );
        return result;
    }

    
    // ----------------------------------------------------------
    public WOComponent select()
    {
        WOComponent result = null;
        log.debug( "select = " + file.getPath() );
        if ( fileSelectionListener != null )
        {
            String selection = file.getPath();
            if ( baseFile != null )
            {
                String parent = baseFile.getParent();
                if ( parent != null && parent.length() > 0 )
                {
                    selection = selection.substring( parent.length() + 1 );
                }
            }
            selection = selection.replaceAll( "\\\\", "/" );
            result = fileSelectionListener.selectFile( selection );
        }
        return result;
    }

    
    // ----------------------------------------------------------
    public boolean isSelected()
    {
        boolean result = false;
        if ( currentSelection != null )
        {
            String myPath = file.getPath().replaceAll( "\\\\", "/" );
            result = myPath.endsWith( currentSelection );
            log.debug( "comparing " + myPath + " with " + currentSelection
                + " = " + result );
        }
        else
        {
            log.debug( "isSelected(): current selection is null" );
        }
        return result;
    }

    
    // ----------------------------------------------------------
    public boolean canSelectThis()
    {
        boolean result = false;
        if ( allowSelection && !isSelected() )
        {
            if ( file.isDirectory() )
            {
                result = allowSelectDir;                
            }
            else if ( allowSelectExtensions == null )
            {
                result = true;
            }
            else
            {
                String myExt = WCFile.extensionOf( file ).toLowerCase();
                for ( int i = 0; i < allowSelectExtensions.count(); i++ )
                {
                    if ( myExt.equals(
                            allowSelectExtensions.objectAtIndex( i ) ) )
                    {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( FileBrowserRow.class );
}
