/*==========================================================================*\
 |  $Id: OptionEditPanel.java,v 1.5 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.webcat.archives.ArchiveManager;
import org.webcat.archives.IWritableContainer;
import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.ComponentIDGenerator;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.eof.ERXConstant;

//-------------------------------------------------------------------------
/**
 *  A small component that allows editing of one option in an option set.
 *  @see OptionSetEditor
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.5 $, $Date: 2012/03/28 13:48:08 $
 */
public class OptionEditPanel
    extends WCComponent
    implements FileBrowser.FileSelectionListener, FilePickerDelegate
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new OptionEditPanel object.
     *
     * @param context The context to use
     */
    public OptionEditPanel( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public NSDictionary<?, ?>        option;
    public NSKeyValueCodingAdditions optionValues;
    public Boolean                   terse;
    public int                       type = 0;
    public String                    property;
    public NSDictionary<?, ?>        choice;
    public String                    browsePageName;
    public java.io.File              base;
    public RepositoryEntryRef            initialSelectionForFilePicker;

    public ComponentIDGenerator      idFor = new ComponentIDGenerator(this);


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public OptionEditPanel self()
    {
        return this;
    }


    // ----------------------------------------------------------
    public void appendToResponse( WOResponse response, WOContext context )
    {
        // if ( type == 0 )
        {
            String typeName = (String)option.objectForKey( "type" );
            for ( int i = 0; i < types.length; i++ )
            {
                if ( types[i].equals( typeName ) )
                {
                    type = i;
                    break;
                }
            }
        }
        theSelectedChoice = null;
        if ( property == null )
        {
            property = (String)option.objectForKey( "property" );
        }
        log.debug( "option = " + property
            + ", type = " + option.objectForKey( "type" )
            + ", type = " + type );
        if (optionValues != null)
        {
            log.debug("option values hash id = " + optionValues.hashCode());
        }
        super.appendToResponse( response, context );
    }


    // ----------------------------------------------------------
    public String initialStyle()
    {
        String style = "margin-top: 0;";

        if (terse)
        {
            style += " display: none";
        }

        return style;
    }


    // ----------------------------------------------------------
    public boolean isBoolean()
    {
        return type == BOOLEAN_TYPE;
    }


    // ----------------------------------------------------------
    public boolean isInteger()
    {
        return type == INTEGER_TYPE;
    }


    // ----------------------------------------------------------
    public boolean isDouble()
    {
        return type == DOUBLE_TYPE;
    }


    // ----------------------------------------------------------
    public boolean isString()
    {
        return type == STRING_TYPE;
    }


    // ----------------------------------------------------------
    public boolean isListChoice()
    {
        return type == LIST_CHOICE_TYPE;
    }


    // ----------------------------------------------------------
    public boolean isRadioChoice()
    {
        return type == RADIO_CHOICE_TYPE;
    }


    // ----------------------------------------------------------
    public boolean isFile()
    {
        return type == FILE_TYPE;
    }


    // ----------------------------------------------------------
    public boolean isFileOrDir()
    {
        return type == FILE_OR_DIR_TYPE;
    }


    // ----------------------------------------------------------
    public boolean isShortText()
    {
        return type == SHORT_TEXT_TYPE;
    }


    // ----------------------------------------------------------
    public boolean isLongText()
    {
        return type == LONG_TEXT_TYPE;
    }


    // ----------------------------------------------------------
    public boolean isAntBoolean()
    {
        return type == ANT_BOOLEAN_TYPE;
    }


    // ----------------------------------------------------------
    public boolean isPassword()
    {
        return type == PASSWORD_TYPE;
    }


    // ----------------------------------------------------------
    public Object fieldSize()
    {
        Object result = option.objectForKey( "size" );
        if ( result == null )
        {
            // default sizes
            switch ( type )
            {
                case INTEGER_TYPE:
                    result = ERXConstant.integerForInt( 4 );
                    break;
                case DOUBLE_TYPE:
                    result = ERXConstant.integerForInt( 6 );
                    break;
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    public Object value()
    {
        Object result = optionValues.valueForKey( property );
        if ( result == null )
        {
            result = option.objectForKey( "default" );
        }
        return result;
    }


    // ----------------------------------------------------------
    public void setValue( Object value )
    {
        Object oldValue = value();
        log.debug( "set " + property + " = " + value );
        if ( isAntBoolean()
             && value != null
             && !( (Boolean)value ).booleanValue() )
        {
            // For ANT-style booleans, any set value = true, and false means
            // unset, so convert false values into nulls
            value = null;
        }
        if ( value != null
                  && ( oldValue == null ||
                       !value().toString().equals( value.toString() ) ) )
        {
            log.debug( "storing value" );
            optionValues.takeValueForKey( value, property );
        }
        else
        {
            log.debug( "removing value" );
            if ( optionValues instanceof NSMutableDictionary )
            {
                ( (NSMutableDictionary<?, ?>)optionValues )
                    .removeObjectForKey( property );
            }
            else if ( optionValues instanceof Map )
            {
                ( (Map<?, ?>)optionValues ).remove( property );
            }
            else
            {
                log.error( "Unable to remove key from optionValues of class "
                    + optionValues.getClass().getName() );
            }
        }
    }


    // ----------------------------------------------------------
    public boolean hasValue()
    {
        Object oldValue = optionValues.valueForKey( property );
        return oldValue != null;
    }


    // ----------------------------------------------------------
    public JavascriptGenerator clearValue()
    {
        setValue( null );

        return new JavascriptGenerator().refresh(idFor.get("valueContainer"));
    }


    // ----------------------------------------------------------
    public Object choiceLabel()
    {
        Object result = choice == null ? null : choice.objectForKey( "label" );
        if ( result == null )
        {
            result = choiceValue();
        }
        return result;
    }


    // ----------------------------------------------------------
    public Object choiceValue()
    {
        return choice == null ? null : choice.objectForKey( "value" );
    }


    // ----------------------------------------------------------
    public Object selectedChoice()
    {
        Object currentValue = value();
        if ( theSelectedChoice == null && currentValue != null )
        {
            String valueString = currentValue.toString();
            NSArray<?> choices = (NSArray<?>)option.objectForKey( "choices" );
            for ( int i = 0; i < choices.count(); i++ )
            {
                NSDictionary<?, ?> thisChoice =
                    (NSDictionary<?, ?>)choices.objectAtIndex( i );
                if ( valueString.equals(
                        thisChoice.objectForKey( "value" ).toString() ) )
                {
                    theSelectedChoice = thisChoice;
                    break;
                }
            }
        }
        return theSelectedChoice;
    }


    // ----------------------------------------------------------
    public void setSelectedChoice( NSDictionary<?, ?> theChoice )
    {
        setValue( theChoice.objectForKey( "value" ) );
    }


    // ----------------------------------------------------------
    public boolean canBrowse()
    {
        return browsePageName != null && base != null;
    }


    // ----------------------------------------------------------
    public WOComponent browse()
    {
        if ( !canBrowse() )
        {
            error( "File and directory configuration options are" +
                    "not supported on this page!" );
            return null;
        }
        WOComponent newPage = pageWithName( browsePageName );
        myPage = context().page();
        newPage.takeValueForKey( myPage, "nextPage" );
        newPage.takeValueForKey( Boolean.TRUE, "isEditable" );
        if ( ! base.exists() )
        {
            base.mkdirs();
        }
        newPage.takeValueForKey( base, "base" );
        newPage.takeValueForKey( value(), "currentSelection" );
        log.debug( "browse(): current selection = " + value() );
        newPage.takeValueForKey( this, "fileSelectionListener" );
        newPage.takeValueForKey( Boolean.valueOf( type == FILE_OR_DIR_TYPE ),
                                 "allowSelectDir" );
        newPage.takeValueForKey( option.objectForKey( "fileTypes" ),
                                 "allowSelectExtensions" );
        return newPage;
    }


    // ----------------------------------------------------------
    private RepositoryEntryRef repositoryEntryRefValue()
    {
        RepositoryEntryRef entryRef;

        if (value() instanceof String)
        {
            entryRef = RepositoryEntryRef.fromOldStylePath((String) value());
        }
        else if (value() instanceof NSDictionary<?, ?>)
        {
            @SuppressWarnings("unchecked")
            NSDictionary<String, Object> value =
                (NSDictionary<String, Object>) value();
            entryRef = RepositoryEntryRef.fromDictionary(value);
        }
        else
        {
            entryRef = null;
        }

        return entryRef;
    }


    // ----------------------------------------------------------
    public JavascriptGenerator showFileDialog()
    {
        initialSelectionForFilePicker = repositoryEntryRefValue();

        JavascriptGenerator js = new JavascriptGenerator();
        js.dijit(idFor.get("filePickerDialog")).call("show");
        return js;
    }


    // ----------------------------------------------------------
    public boolean fileCanBeSelected(RepositoryEntryRef file, boolean isDirectory)
    {
        if (isDirectory && type == FILE_TYPE)
        {
            return false;
        }
        else if (isDirectory && type == FILE_OR_DIR_TYPE)
        {
            return true;
        }
        else
        {
            NSArray<?> extensions =
                (NSArray<?>) option.objectForKey("fileTypes");

            if (extensions == null)
            {
                return true;
            }
            else
            {
                return extensions.contains(FileUtilities.extensionOf(
                        file.name()).toLowerCase());
            }
        }
    }


    // ----------------------------------------------------------
    public JavascriptGenerator fileWasSelected(RepositoryEntryRef file)
    {
        setValue(file.dictionaryRepresentation());

        JavascriptGenerator js = new JavascriptGenerator();
        js.refresh(idFor.get("valueContainer"));
        return js;
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
        DeliverFile downloadPage = pageWithName(DeliverFile.class);

        RepositoryEntryRef fileRef = repositoryEntryRefValue();
        fileRef.resolve(localContext());

        if (fileRef.isDirectory())
        {
            File zipFile = new File(fileRef.name() + ".zip");
            downloadPage.setFileName(zipFile);
            downloadPage.setContentType(FileUtilities.mimeType(zipFile));

            ByteArrayOutputStream boas = new ByteArrayOutputStream();
            ZipOutputStream zos  = new ZipOutputStream(boas);
            IWritableContainer container =
                ArchiveManager.getInstance().writableContainerForZip(zos);

            fileRef.repository().copyItemToContainer(
                    fileRef.objectId(), fileRef.name(), container);

            container.finish();
            zos.close();
            downloadPage.setFileData(new NSData(boas.toByteArray()));
            boas.close();
        }
        else
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            File file = new File(fileRef.name());

            downloadPage.setFileName(file);
            downloadPage.setContentType(FileUtilities.mimeType(file));

            fileRef.repository().writeBlobToStream(fileRef.objectId(), baos);

            downloadPage.setFileData(new NSData(baos.toByteArray()));
            baos.close();
        }

        downloadPage.setStartDownload(true);

        return downloadPage;
    }


    // ----------------------------------------------------------
    public WOComponent selectFile( String filePath )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug("selectFile(" + filePath + ")");
        }

        // Force enclosing option editor to pull new value for optionValues,
        // in case the file operations caused a MutableDictionary to be
        // flushed and re-read.
        if (parent() != null && parent() instanceof OptionSetEditor)
        {
            OptionSetEditor parent = (OptionSetEditor)parent();
            parent.takeValueForKey(
                parent.valueForBinding("optionValues"), "optionValues");
        }
        // Now pull the new value from the parent, but just for this
        // binding.
        optionValues =
            (NSKeyValueCodingAdditions)valueForBinding("optionValues");

        setValue( user().authenticationDomain().subdirName()
                + "/" + filePath );
        if ( log.isDebugEnabled() )
        {
            log.debug("selectFile(" + filePath + ")");
            log.debug( "new option values: ("
                + (optionValues == null ? "null" : optionValues.hashCode())
                + ")\n" + optionValues );
        }
        return myPage; // context().page();
    }


    //~ Instance/static variables .............................................

    private NSDictionary<?, ?> theSelectedChoice = null;
    private WOComponent myPage;

    // private static int UNKNOWN_TYPE      = 0;
    private static final int BOOLEAN_TYPE      = 1;
    private static final int INTEGER_TYPE      = 2;
    private static final int DOUBLE_TYPE       = 3;
    private static final int STRING_TYPE       = 4;
    private static final int LIST_CHOICE_TYPE  = 5;
    private static final int RADIO_CHOICE_TYPE = 6;
    private static final int FILE_TYPE         = 7;
    private static final int FILE_OR_DIR_TYPE  = 8;
    private static final int SHORT_TEXT_TYPE   = 9;
    private static final int LONG_TEXT_TYPE    = 10;
    private static final int ANT_BOOLEAN_TYPE  = 11;
    private static final int PASSWORD_TYPE     = 12;
    private static final String[] types = new String[] {
        "unknown",
        "boolean",
        "integer",
        "double",
        "string",
        "listChoice",
        "radioChoice",
        "file",
        "fileOrDir",
        "shortText",
        "longText",
        "antBoolean",
        "password"
    };
    static Logger log = Logger.getLogger( OptionEditPanel.class );
}
