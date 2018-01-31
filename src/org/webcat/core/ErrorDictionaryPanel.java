/*==========================================================================*\
 |  $Id: ErrorDictionaryPanel.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
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

import java.util.Enumeration;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.ERXExtensions;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.localization.ERXLocalizer;

//-------------------------------------------------------------------------
/**
 * Provides a standard component for displaying error message(s) on
 * pages.  This class is based significantly on
 * {@link er.extensions.components.ERXErrorDictionaryPanel}, but cannot be a
 * subclass of that component because of some of the behaviors it supports (the
 * other class' appendToResponse() method includes handling that cannot be
 * easily overridden, and assumes the content of the error map is essentially
 * a string-to-string map).  As a result, there is a fair amount of
 * code duplication, but that can't be avoided without refactoring the
 * er.extensions version.
 *
 * @author Stephen Edwards
 * @version $Id: ErrorDictionaryPanel.java,v 1.1 2010/05/11 14:51:55 aallowat Exp $
 */
public class ErrorDictionaryPanel
    extends er.extensions.components.ERXStatelessComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Footer object.
     *
     * @param context The page's context
     */
    public ErrorDictionaryPanel( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public String errorKey;


    // ----------------------------------------------------------
    /**
     * A class to represent encoded error messages with category info
     * that .
     */
    public static class ErrorMessage
    {
        // ----------------------------------------------------------
        /**
         * Create a new error message object.
         * @param category The type of message--use the constants defined
         *                 in the {@link Status} class
         * @param message  The content of the message
         * @param sticky   True if this message should persist until it
         *                 it explicitly cleared; otherwise, it will be
         *                 cleared automatically after it has been displayed
         */
        public ErrorMessage(
            byte category, String message, boolean sticky )
        {
            this.category = category;
            this.message  = message;
            this.sticky   = sticky;
        }


        // ----------------------------------------------------------
        /**
         * Retrieve this message's category.
         * @return this message's category
         */
        public byte category()
        {
            return category;
        }


        // ----------------------------------------------------------
        /**
         * Retrieve the string message associated with this object.
         * @return the message as a string
         */
        public String message()
        {
            return message;
        }


        // ----------------------------------------------------------
        /**
         * Determine whether this message is sticky.
         * @return true if this is a sticky message
         */
        public boolean sticky()
        {
            return sticky;
        }


        // ----------------------------------------------------------
        /**
         * Retrieve the string message associated with this object.  No
         * category or sticky information is encoded in this
         * representation--only the object's string message is used.
         * @return the message as a string
         */
        public String toString()
        {
            return message;
        }


        //~ Instance Fields ...................................................

        private byte    category;
        private String  message;
        private boolean sticky;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public NSMutableDictionary<String, ErrorMessage> errorMessages()
    {
        if ( errorMessages == null )
        {
            errorMessages = (NSMutableDictionary<String, ErrorMessage>)
                valueForBinding( "errorMessages" );
            if ( errorMessages == null )
            {
                errorMessages =
                    new NSMutableDictionary<String, ErrorMessage>();
            }
        }
        return errorMessages;
    }


    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public NSMutableArray<String> errorKeyOrder()
    {
        if ( errorKeyOrder == null )
        {
            errorKeyOrder =
                (NSMutableArray<String>)valueForBinding( "errorKeyOrder" );
        }
        return errorKeyOrder;
    }


    // ----------------------------------------------------------
    public String extraErrorMessage()
    {
        if ( extraErrorMessage == null )
        {
            extraErrorMessage = (String)valueForBinding( "extraErrorMessage" );
            if ( extraErrorMessage != null )
            {
                extraErrorMessage =
                    massageErrorMessage( extraErrorMessage, null );
            }
        }
        return extraErrorMessage;
    }


    // ----------------------------------------------------------
    public boolean hasErrors()
    {
        return errorMessages().count() > 0
            || ( extraErrorMessage() != null
                 && extraErrorMessage().length() > 0 );
    }


    // ----------------------------------------------------------
    public NSArray<String> errorKeys()
    {
        return errorKeyOrder() != null
            ? errorKeyOrder()
            : errorMessages().allKeys();
    }


    // ----------------------------------------------------------
    public String errorMessageItem()
    {
        Object obj = errorMessages().objectForKey( errorKey );
        String msg = ( obj instanceof Throwable )
            ? ( (Throwable) obj).getMessage()
            : obj.toString();
        return massageErrorMessage( msg, errorKey );
    }


    // ----------------------------------------------------------
    public byte errorMessageCategory( Object value )
    {
        if ( value instanceof Throwable )
        {
            return Status.ERROR;
        }
        else if ( value instanceof ErrorMessage )
        {
            return ( (ErrorMessage)value ).category();
        }
        else
        {
            return Status.WARNING;
        }
    }


    // ----------------------------------------------------------
    public byte errorMessageCategory()
    {
        return errorMessageCategory( errorMessages().objectForKey( errorKey ) );
    }


    // ----------------------------------------------------------
    public String errorMessageCssClass()
    {
        return Status.statusCssClass( errorMessageCategory() );
    }


    // ----------------------------------------------------------
    public String dictionaryCssClass()
    {
        String style = "";
        if ( extraErrorMessage() != null )
        {
            style += " error"; // must include the space at start
        }
        else
        {
            NSDictionary<String, ErrorMessage> dict = errorMessages();
            for ( Enumeration<String> e = dict.keyEnumerator();
                  e.hasMoreElements();)
            {
                byte category = errorMessageCategory(
                    errorMessages().objectForKey( e.nextElement() ) );
                switch ( category )
                {
                    case Status.ERROR:
                    case Status.WARNING:
                    case Status.UNFINISHED:
                        style += " error"; // must include the space at start
                }
            }
        }
        if ( er.extensions.foundation.ERXValueUtilities.booleanValue(
            valueForBinding( "shouldShowNewlineAbove" ) ) )
        {
            style += " nlbefore";
        }
        if ( er.extensions.foundation.ERXValueUtilities.booleanValue(
            valueForBinding( "shouldShowNewlineBelow" ) ) )
        {
            style += " nlafter";
        }
        return style;
    }


    // ----------------------------------------------------------
    public static String massageErrorMessage( String initialMessage,
                                              String displayErrorKey )
    {
        String result = ERXExtensions.substituteStringByStringInString(
            "EOValidationException:", "", initialMessage );
        if ( displayErrorKey != null )
        {
            result = ERXExtensions.substituteStringByStringInString(
                ERXGenericRecord.KEY_MARKER, displayErrorKey, result );
        }

        if ( result != null )
        {
            if ( result.endsWith( "is not allowed to be null." )
                 || ( result.startsWith( " The " )
                      && result.indexOf( " property " ) != -1
                      && result.indexOf( " must have a " ) !=-1
                      && result.endsWith( " assigned" ) ) )
            {
                char c;
                if ( displayErrorKey == null )
                {
                    result = result.substring(
                        result.indexOf( "'" ) + 1,
                        result.indexOf( "is not allowed to be null." ) - 2 );
                    c = result.charAt( 0 );
                }
                else
                {
                    result = displayErrorKey;
                    c = result.toLowerCase().charAt( 0 );
                }
                String article =
                    ( c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u')
                    ? "an" : "a";
                result = "Please provide " + article
                    + " <b>" + result + "</b>.";
            }
            else if ( result.indexOf( ": Invalid number" ) != -1 )
            {
                int colon = result.indexOf( ':' );
                result = "<b>" + ( displayErrorKey == null
                                     ?  result.substring( 0, colon - 1 )
                                     : displayErrorKey );
                result += "</b>: I could not understand the number you typed.";
            }
            else if ( result.indexOf( eliminable ) > 0 )
            {
                result = result.substring(
                    eliminable.length() + 1, result.length() );
            }
            if ( result.indexOf( couldNotSave ) > 0 )
            {
                String replace = ERXLocalizer.currentLocalizer()
                    .localizedStringForKey( couldNotSave );
                if ( replace != null )
                {
                    result = replace + result.substring(
                        couldNotSave.length() + 1, result.length() );
                }
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    public String panelClass()
    {
        if (panelClass == null)
        {
            byte category = Status.INFORMATION;
            for (String key : errorKeys())
            {
                byte thisCategory = errorMessageCategory(
                    errorMessages().objectForKey(key));
                if (thisCategory < category)
                {
                    category = thisCategory;
                }
            }
            panelClass = Status.statusCssClass(category);
        }
        return "NoticePanel " + panelClass;
    }


    // ----------------------------------------------------------
    public void reset()
    {
        super.reset();
        errorMessages = null;
        errorKeyOrder = null;
        extraErrorMessage = null;
        panelClass = null;
    }


    //~ Instance/static variables .............................................

    protected NSMutableDictionary<String, ErrorMessage> errorMessages;
    protected NSMutableArray<String> errorKeyOrder;
    protected String extraErrorMessage;
    protected String panelClass;

    private final static String eliminable =
        "Could not save your changes: null";
    private final static String couldNotSave =
        "Could not save your changes: ";
}
