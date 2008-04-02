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
import java.util.Enumeration;

//-------------------------------------------------------------------------
/**
* This class is a base for WCComponent that extracts out the error
* message handling features.
*
* @author Stephen Edwards
* @version $Id$
*/
public class WCComponentWithErrorMessages
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The page's context
     */
    public WCComponentWithErrorMessages( WOContext context )
    {
        super( context );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse( WOResponse response, WOContext context )
    {
        super.appendToResponse( response, context );
        clearMessages();
    }


    // ----------------------------------------------------------
    /**
     * Determines whether this page has any messages.
     * @return True if any messages are present
     */
    public boolean hasMessages()
    {
        NSDictionary msgs = messagesIfPresent();
        return msgs != null && msgs.count() > 0;
    }


    // ----------------------------------------------------------
    /**
     * Determines whether this page has any error messages.
     * TODO: refactor this method by renaming it, since it currently
     * returns true if there are any messages at all, even though some may
     * not be errors.
     * @return True if any error messages are present
     */
    public boolean hasBlockingErrors()
    {
        if ( hasMessages() )
        {
            for ( Enumeration e = messages.keyEnumerator();
                  e.hasMoreElements(); )
            {
                Object key = e.nextElement();
                Object value = messages.objectForKey( key );
                ErrorDictionaryPanel.ErrorMessage msg =
                    ( value instanceof ErrorDictionaryPanel.ErrorMessage )
                        ? (ErrorDictionaryPanel.ErrorMessage)value
                        : null;
                if ( msg == null
                     || msg.category() == Status.ERROR
                     || msg.category() == Status.WARNING )
                {
                    return true;
                }
            }
        }
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Record an error message for this page.
     * @param message the error message
     */
    public void error( String message )
    {
        error( message, null );
    }


    // ----------------------------------------------------------
    /**
     * Record an exception as an error message for this page.
     * @param anException the exception to be posted
     */
    public void error( Throwable anException )
    {
        message( anException, null );
    }


    // ----------------------------------------------------------
    /**
     * Record an error message for this page.
     * @param message the error message
     * @param id a unique id used to distinguish this message from others
     */
    public void error( String message, String id )
    {
        message( Status.ERROR, message, false, id );
    }


    // ----------------------------------------------------------
    /**
     * Record an error message for this page.
     * @param message the error message
     */
    public void warning( String message )
    {
        message( Status.WARNING, message, false, null );
    }


    // ----------------------------------------------------------
    /**
     * Record an error message for this page.
     * @param message the error message
     */
    public void confirmationMessage( String message )
    {
        message( Status.GOOD, message, false, null );
    }


    // ----------------------------------------------------------
    /**
     * Record an error message for this page, if one is provided.  If a
     * null message is provided, nothing happens (no message is recorded).
     * @param message the error message, or null
     */
    public void possibleErrorMessage( String message )
    {
        if ( message != null )
        {
            error( message );
        }
    }


    // ----------------------------------------------------------
    /**
     * Record an error message for this page, encoding it in an
     * {@link ErrorDictionaryPanel.ErrorMessage} object first.
     * @param category The type of message--use the constants defined
     *                 in the {@link Status} class
     * @param message  The content of the message
     * @param sticky   True if this message should persist until it
     *                 it explicitly cleared; otherwise, it will be
     *                 cleared automatically after it has been displayed
     * @param id a unique id used to distinguish this message from others
     */
    public void message(
        byte category, String message, boolean sticky, String id )
    {
        message(
            new ErrorDictionaryPanel.ErrorMessage( category, message, sticky ),
            id
            );
    }


    // ----------------------------------------------------------
    /**
     * Record an error message for this page, without processing.
     * @param message the error message
     * @param id a unique id used to distinguish this message from others
     */
    public void message( Object message, String id )
    {
        if ( id == null )
        {
            id = message.toString();
        }
        messages().setObjectForKey( message, id );
    }


    // ----------------------------------------------------------
    /**
     * Remove all non-sticky messages for this page.
     */
    public void clearMessages()
    {
        NSMutableDictionary dict = messagesIfPresent();
        if ( dict != null )
        {
            NSMutableDictionary keep = new NSMutableDictionary();
            for ( Enumeration e = dict.keyEnumerator(); e.hasMoreElements(); )
            {
                Object key = e.nextElement();
                Object value = dict.objectForKey( key );
                if ( ( value instanceof ErrorDictionaryPanel.ErrorMessage )
                     && ( (ErrorDictionaryPanel.ErrorMessage)value ).sticky() )
                {
                    keep.setObjectForKey( value, key );
                }
            }
            dict.removeAllObjects();
            dict.addEntriesFromDictionary( keep );
        }
    }


    // ----------------------------------------------------------
    /**
     * Remove all messages for this page, including sticky ones.
     */
    public void clearAllMessages()
    {
        NSMutableDictionary dict = messagesIfPresent();
        if ( dict != null )
        {
            dict.removeAllObjects();
        }
    }


    // ----------------------------------------------------------
    /**
     * Remove a specific message from this page.
     * @param id the unique identifier for the message
     */
    public void clearMessage( String id )
    {
        NSMutableDictionary dict = messagesIfPresent();
        if ( dict != null )
        {
            dict.removeObjectForKey( id );
        }
    }


    // ----------------------------------------------------------
    /**
     * Get the current message dictionary for this page, creating one
     * if necessary.  If no messages have been registered yet and no
     * dictionary exists, one is created first.
     * @return the message dictionary
     */
    public NSMutableDictionary messages()
    {
        if ( messages == null )
        {
            messages = new NSMutableDictionary();
        }
        return messages;
    }


    // ----------------------------------------------------------
    /**
     * Access the current message dictionary for this page, if one exists.
     * If no messages have been registered yet and no dictionary exists,
     * null is returned.
     * @return the message dictionary, or null if one has not yet been created
     */
    public NSMutableDictionary messagesIfPresent()
    {
        return messages;
    }


    // ----------------------------------------------------------
    /* (non-Javadoc)
     * @see com.webobjects.appserver.WOComponent#validationFailedWithException(java.lang.Throwable, java.lang.Object, java.lang.String)
     */
    public void validationFailedWithException( Throwable ex,
                                               Object    value,
                                               String    key )
    {
        message( ex, key );
    }


    //~ Instance/static variables .............................................
    private NSMutableDictionary  messages;
}
