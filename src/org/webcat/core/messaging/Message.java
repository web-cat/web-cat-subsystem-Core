/*==========================================================================*\
 |  $Id: Message.java,v 1.9 2014/06/16 16:01:24 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2010-2011 Virginia Tech
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

package org.webcat.core.messaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jfree.util.Log;
import org.webcat.core.Application;
import org.webcat.core.MutableDictionary;
import org.webcat.core.SentMessage;
import org.webcat.core.Subsystem;
import org.webcat.core.User;
import org.webcat.woextensions.ECAction;
import static org.webcat.woextensions.ECAction.run;
import org.webcat.woextensions.WCEC;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * <p>
 * The abstract base class for all notification messages in Web-CAT. Subclasses
 * should localize their registration by providing a static register() method
 * that calls the {@link #registerMessage(Class, String, boolean, int)} with
 * the appropriate parameters, and then the subsystem can all these register()
 * methods in its {@link Subsystem#init()}.
 * </p><p>
 * Some methods below are denoted as "only called during the message sending
 * cycle". This means that during the execution of these methods, the editing
 * context returned by the {@link #editingContext()} method is valid.
 * </p>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.9 $, $Date: 2014/06/16 16:01:24 $
 */
public abstract class Message
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Registers the specified message type in the messaging system.
     *
     * @param messageClass the class of message being registered
     * @param category a human-readable name of a category for this message,
     *     used for grouping
     * @param description a human-readable name of the message type, suitable
     *     for display in the Web-CAT user interface
     * @param isBroadcast true if messages of this type should be broadcast
     *     to any system-wide notification protocols (such as Twitter feeds),
     *     in addition to being sent to the message's list of users
     * @param accessLevel the lowest access level of users who can receive
     *     this message type (used to control whether the user can view it in
     *     the message/protocol enablement matrix)
     */
    public static void registerMessage(
        Class<? extends Message> messageClass,
        String category,
        String description,
        boolean isBroadcast,
        int accessLevel)
    {
        MessageDescriptor descriptor = new MessageDescriptor(
            messageClass.getCanonicalName(),
            category, description, isBroadcast, accessLevel);

        synchronized (descriptors)
        {
            descriptors.put(messageClass, descriptor);
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets the descriptors for the currently registered direct-to-user
     * messages. This method only returns those messages that can be sent to
     * the specified user, based on access level.
     *
     * @param user the user
     * @return an array of message descriptors
     */
    public static NSArray<MessageDescriptor> registeredUserMessages(User user)
    {
        NSMutableArray<MessageDescriptor> descs =
            new NSMutableArray<MessageDescriptor>();

        synchronized (descriptors)
        {
            for (MessageDescriptor descriptor : descriptors.values())
            {
                if (user.accessLevel() >= descriptor.accessLevel())
                {
                    descs.addObject(descriptor);
                }
            }
        }

        return descs;
    }


    // ----------------------------------------------------------
    /**
     * Gets the descriptors for the currently registered broadcast messages.
     *
     * @return an array of message descriptors
     */
    public static NSArray<MessageDescriptor> registeredBroadcastMessages()
    {
        NSMutableArray<MessageDescriptor> descs =
            new NSMutableArray<MessageDescriptor>();

        synchronized (descriptors)
        {
            for (MessageDescriptor descriptor : descriptors.values())
            {
                if (descriptor.isBroadcast())
                {
                    descs.addObject(descriptor);
                }
            }
        }

        return descs;
    }


    // ----------------------------------------------------------
    /**
     * A shorthand method for getting the message type name, which is the
     * canonical name of the message class.
     *
     * @return the canonical class name of the message
     */
    public final String messageType()
    {
        return getClass().getCanonicalName();
    }


    // ----------------------------------------------------------
    /**
     * Gets the message descriptor for messages of this type.
     *
     * @return the message descriptor
     */
    public final MessageDescriptor messageDescriptor()
    {
        MessageDescriptor result = null;
        synchronized (descriptors)
        {
            result = descriptors.get(getClass());
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Gets the message descriptor for messages of the given type.
     *
     * @param messageType the type of the message
     * @return the message descriptor
     */
    public static MessageDescriptor messageDescriptorForMessageType(
        String messageType)
    {
        try
        {
            Class<?> klass = Class.forName(messageType);
            MessageDescriptor result = null;
            synchronized (descriptors)
            {
                result = descriptors.get(klass);
            }
            return result;
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether this message should also be sent to the
     * system administrator e-mail addresses specified in the Web-CAT
     * installation wizard. The default behavior returns false, except for
     * messages that extend SysAdminMessage, which returns true.
     *
     * @return true if the message should also be sent to system
     *     administrators, false otherwise
     */
    public boolean isSevere()
    {
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Gets an object that implements {@link IMessageSettings} that contains
     * the settings to use for sending this message if it is a broadcast
     * message. The default behavior returns null, which indicates that the
     * system default settings should be used. Subclasses can override this
     * and return another object (usually a {@link ProtocolSettings}) to modify
     * the way the message is broadcast; for example, to post it to a
     * course-specific Twitter feed instead of the system feed.
     *
     * @return an object that implements {@link IMessageSettings}, or null to
     *     use the system settings
     */
    public IMessageSettings broadcastSettings()
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets the list of users who should receive this message. If the message
     * is meant to be broadcast system-wide only, then this method may return
     * null.
     * </p><p>
     * This method is only called during the message sending cycle.
     * </p>
     *
     * @return an array of Users who should receive the message, or null if it
     *     should only be broadcast system-wide
     */
    public abstract NSArray<User> users();


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets the title of the message. The message title is used in situations
     * where it is useful to differentiate a very brief description of the
     * message from its longer-form content (such as the subject line of an
     * e-mail). Titles are typically very brief, even shorter than the
     * {@link #shortBody()} of the message.
     * </p><p>
     * This method is only called during the message sending cycle.
     * </p>
     *
     * @return the title of the message
     */
    public abstract String title();


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets the short-form body content of the message. The short form is used
     * in the pop-up notification list, and is available for messaging
     * protocols that have constraints on the length of the content that they
     * can send (such as Twitter and SMS).
     * </p><p>
     * Subclasses should attempt to keep their short form content under 140
     * characters as a rule of thumb, but no guarantees are made to ensure
     * this, so specific protocols may need to further elide this content if
     * necessary.
     * </p><p>
     * This method is only called during the message sending cycle.
     * </p>
     *
     * @return the short form of the message body
     */
    public abstract String shortBody();


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets the full-form body content of the message. The full form can be an
     * arbitrary length and should be used by protocols where there are no
     * tight constraints on message content length, such as the body of an
     * e-mail or description of an item in an RSS feed.
     * </p><p>
     * This method is only called during the message sending cycle.
     * </p>
     *
     * @return the full form of the message body
     */
    public String fullBody()
    {
        return shortBody();
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets a set of attachments that should be sent along with the message, if
     * the protocol (such as e-mail) supports attachments. Since many messaging
     * protocols do not support attachments, there should not be any critical
     * information included here. This method may return null if there are no
     * attachments.
     * </p><p>
     * This method is only called during the message sending cycle.
     * </p>
     *
     * @return a dictionary of attachments, keyed by the name of the attachment
     */
    public List<File> attachments()
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Used by the message dispatcher to load the attachment data into memory.
     * Clients should never call this method directly.
     * </p>
     *
     * @return a dictionary of attachments, keyed by the name of the attachment
     */
    final public NSDictionary<String, NSData> attachmentData()
    {
        List<File> attachmentFiles = attachments();

        NSMutableDictionary<String, NSData> attachmentData =
            new NSMutableDictionary<String, NSData>();

        for (File file : attachmentFiles)
        {
            try
            {
                NSData data = dataFromContentsOfFile(file);
                attachmentData.setObjectForKey(data, file.getName());
            }
            catch (IOException e)
            {
                Log.error("Exception occurred while loading the attachment "
                        + file, e);
            }
        }

        return attachmentData;
    }


    // ----------------------------------------------------------
    /**
     * Returns an NSData object with the contents of the specified file.
     *
     * @param file the file
     * @return an NSData object containing the contents of the file
     * @throws IOException if an I/O error occurred
     */
    private NSData dataFromContentsOfFile(File file) throws IOException
    {
        FileInputStream stream = new FileInputStream(file);
        NSData data = new NSData(stream, 0);
        stream.close();
        return data;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Gets a set of URLs that link to relevant content for the message. This
     * method may return null if there are no links for a particular message.
     * </p><p>
     * This method is only called during the message sending cycle.
     * </p>
     *
     * @return a dictionary containing links to relevant content, keyed by
     *     plain-text labels that describe each link
     */
    public NSDictionary<String, String> links()
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Sends the message, then disposes of it.
     */
    public final synchronized void send()
    {
        if (disposed)
        {
            throw new IllegalStateException(getClass().getSimpleName()
                + ": send() called on disposed message.  Each message can "
                + "only be sent once.");
        }

        run(new ECAction(editingContext()) { public void action() {
            // Use the application's registered message dispatcher to send
            // the message.
            Application.wcApplication().messageDispatcher()
                .sendMessage(Message.this);

            // Store the message in the database.
            storeMessage();
        }});

        // Dispose of message and its EC
        dispose();
    }


    // ----------------------------------------------------------
    /**
     * Stores the contents of this message in the database.
     *
     * @param descriptor the message descriptor for this message
     */
    private synchronized void storeMessage()
    {
        final MessageDescriptor descriptor = messageDescriptor();

        // The editing context is already locked by the caller
        // (or it will auto-lock).
        EOEditingContext ec = editingContext();

        // Create a representation of the message in the database so that it
        // can be viewed by users later.
        SentMessage message = SentMessage.create(ec, false);

        message.setSentTime(new NSTimestamp());
        message.setMessageType(messageType());
        message.setTitle(title());
        message.setShortBody(shortBody());
        message.setIsBroadcast(descriptor.isBroadcast());

        if (links() != null)
        {
            message.setLinks(new MutableDictionary(links()));
        }

        NSArray<User> users = users();
        if (users != null)
        {
            for (User user : users)
            {
                // Sanity check to ensure that messages don't get sent to
                // users who shouldn't receive them based on their access
                // level.

                if (user.accessLevel() >= descriptor.accessLevel())
                {
                    message.addToUsersRelationship(user.localInstance(ec));
                }
            }
        }

        ec.saveChanges();
    }


    // ----------------------------------------------------------
    /**
     * Gets the editing context used during the sending of this message.
     * Subclasses can access this editing context during the message sending
     * cycle if they need to fetch EOs.
     *
     * @return an editing context
     */
    public synchronized EOEditingContext editingContext()
    {
        if (editingContext == null)
        {
            editingContext = WCEC.newAutoLockingEditingContext();
        }
        return editingContext;
    }


    // ----------------------------------------------------------
    /**
     * Release the resources associated with this message (e.g., its internal
     * editing context) because this message will not be used again.
     */
    protected synchronized void dispose()
    {
        if (disposed)
        {
            throw new IllegalStateException(getClass().getSimpleName()
                + ": dispose() called on disposed message.");
        }
        if (editingContext != null)
        {
            editingContext.dispose();
            editingContext = null;
        }
        disposed = true;
    }


    //~ Static/instance variables .............................................

    private EOEditingContext editingContext;
    private boolean disposed = false;

    private static Map<Class<? extends Message>, MessageDescriptor> descriptors
        = new HashMap<Class<? extends Message>, MessageDescriptor>();
}
