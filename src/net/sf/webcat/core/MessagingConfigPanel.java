/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2009 Virginia Tech
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

import java.util.HashMap;
import java.util.Map;
import net.sf.webcat.core.messaging.Message;
import net.sf.webcat.core.messaging.MessageDescriptor;
import net.sf.webcat.core.messaging.Protocol;
import net.sf.webcat.ui.generators.JavascriptGenerator;
import net.sf.webcat.ui.validation.ValidatingAction;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOCustomObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

//-------------------------------------------------------------------------
/**
 * This component encapsulates the message subscription matrix for either a
 * user or for the system broadcast messages.
 *
 * @author Tony Allevato
 * @version $Id$
 */
public class MessagingConfigPanel extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public MessagingConfigPanel(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public boolean isShowingBroadcast = false;

    public NSArray<MessageDescriptor> messageDescriptors;
    public MessageDescriptor messageDescriptor;
    public int indexOfMessage;

    public NSArray<Protocol> protocols;
    public Protocol protocol;
    public int indexOfProtocol;

    public ProtocolSettings protocolSettings;
    public MutableDictionary optionValues;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (isShowingBroadcast)
        {
            messageDescriptors = Message.registeredBroadcastMessages();
        }
        else
        {
            messageDescriptors = Message.registeredUserMessages(user());
        }

        protocols = Message.registeredProtocols(isShowingBroadcast);

        int messageCount = messageDescriptors.count();
        int protocolCount = protocols.count();

        messageToIndexMap = new HashMap<String, Integer>();
        protocolToIndexMap = new HashMap<String, Integer>();

        for (int i = 0; i < messageCount; i++)
        {
            MessageDescriptor md = messageDescriptors.objectAtIndex(i);
            messageToIndexMap.put(md.className(), i);
        }

        for (int j = 0; j < protocolCount; j++)
        {
            Protocol pr = protocols.objectAtIndex(j);
            protocolToIndexMap.put(pr.getClass().getCanonicalName(), j);
        }

        objectMatrix = new EOCustomObject[messageCount][protocolCount];
        selectionMatrix = new Boolean[messageCount][protocolCount];

        if (isShowingBroadcast)
        {
            protocolSettings = ProtocolSettings.systemSettings(localContext());

            NSArray<BroadcastMessageSubscription> subscriptions =
                BroadcastMessageSubscription.allObjects(localContext());

            for (BroadcastMessageSubscription subscription : subscriptions)
            {
                String messageType = subscription.messageType();
                String protocolType = subscription.protocolType();

                int messageIndex = messageToIndexMap.get(messageType);
                int protocolIndex = protocolToIndexMap.get(protocolType);

                objectMatrix[messageIndex][protocolIndex] =
                    subscription;
                selectionMatrix[messageIndex][protocolIndex] =
                    subscription.isEnabled();
            }
        }
        else
        {
            protocolSettings = user().protocolSettings();

            if (protocolSettings == null)
            {
                protocolSettings = ProtocolSettings.create(localContext(),
                        false);
                user().setProtocolSettingsRelationship(protocolSettings);
                applyLocalChanges();
            }

            NSArray<UserMessageSubscription> subscription =
                UserMessageSubscription.subscriptionsForUser(
                        localContext(), user());

            for (UserMessageSubscription selection : subscription)
            {
                String messageType = selection.messageType();
                String protocolType = selection.protocolType();

                int messageIndex = messageToIndexMap.get(messageType);
                int protocolIndex = protocolToIndexMap.get(protocolType);

                objectMatrix[messageIndex][protocolIndex] =
                    selection;
                selectionMatrix[messageIndex][protocolIndex] =
                    selection.isEnabled();
            }
        }

        if (protocolSettings.settings() != null)
        {
            optionValues = new MutableDictionary(protocolSettings.settings());
        }
        else
        {
            optionValues = new MutableDictionary();
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public boolean isProtocolSelectedForMessage()
    {
        int mIndex = indexOfMessageDescriptor();
        int pIndex = indexOfProtocol();

        if (selectionMatrix[mIndex][pIndex] != null)
        {
            return selectionMatrix[mIndex][pIndex];
        }
        else
        {
            return protocol.isEnabledByDefault();
        }
    }


    // ----------------------------------------------------------
    public void setProtocolSelectedForMessage(boolean value)
    {
        int mIndex = indexOfMessageDescriptor();
        int pIndex = indexOfProtocol();

        selectionMatrix[mIndex][pIndex] = value;
    }


    // ----------------------------------------------------------
    public WOActionResults saveSettings()
    {
        for (int mIndex = 0; mIndex < messageDescriptors.count(); mIndex++)
        {
            for (int pIndex = 0; pIndex < protocols.count(); pIndex++)
            {
                EOCustomObject object = objectMatrix[mIndex][pIndex];
                Boolean _newValue = selectionMatrix[mIndex][pIndex];
                boolean defaultValue = protocols.objectAtIndex(pIndex)
                    .isEnabledByDefault();
                boolean newValue;
                if (_newValue == null)
                {
                    newValue = defaultValue;
                }
                else
                {
                    newValue = _newValue;
                }

                if (object != null)
                {
                    if (newValue == defaultValue)
                    {
                        localContext().deleteObject(object);
                        objectMatrix[mIndex][pIndex] = null;
                    }
                    else
                    {
                        setIsEnabledForObject(object, newValue);
                    }
                }
                else
                {
                    if (newValue != defaultValue)
                    {
                        String messageType = messageDescriptors.objectAtIndex(
                                mIndex).className();
                        String protocolType = protocols.objectAtIndex(pIndex)
                            .getClass().getCanonicalName();

                        objectMatrix[mIndex][pIndex] =
                            createSelectionObject(messageType, protocolType,
                                    newValue);
                    }
                }
            }
        }

        applyLocalChanges();

        return null;
    }


    //~ Private methods .......................................................

    // ----------------------------------------------------------
    private int indexOfMessageDescriptor()
    {
        return messageToIndexMap.get(messageDescriptor.className());
    }


    // ----------------------------------------------------------
    private int indexOfProtocol()
    {
        return protocolToIndexMap.get(protocol.getClass().getCanonicalName());
    }


    // ----------------------------------------------------------
    private void setIsEnabledForObject(EOCustomObject object, boolean value)
    {
        if (object instanceof BroadcastMessageSubscription)
        {
            ((BroadcastMessageSubscription) object).setIsEnabled(value);
        }
        else if (object instanceof UserMessageSubscription)
        {
            ((UserMessageSubscription) object).setIsEnabled(value);
        }
    }


    // ----------------------------------------------------------
    private EOCustomObject createSelectionObject(String messageType,
            String protocolType, boolean isEnabled)
    {
        if (isShowingBroadcast)
        {
            BroadcastMessageSubscription selection =
                BroadcastMessageSubscription.create(localContext(), isEnabled);
            selection.setMessageType(messageType);
            selection.setProtocolType(protocolType);
            return selection;
        }
        else
        {
            UserMessageSubscription selection = UserMessageSubscription.create(
                    localContext(), isEnabled);
            selection.setUserRelationship(user());
            selection.setMessageType(messageType);
            selection.setProtocolType(protocolType);
            return selection;
        }
    }


    // ----------------------------------------------------------
    public NSArray<NSDictionary<String, Object>> protocolOptions()
    {
        if (isShowingBroadcast)
        {
            return protocol.globalOptions();
        }
        else
        {
            return protocol.options();
        }
    }


    // ----------------------------------------------------------
    public WOActionResults optionsEdited()
    {
        if (optionValues.isEmpty())
        {
            protocolSettings.setSettings(null);
        }
        else
        {
            protocolSettings.setSettings(optionValues);
        }

        applyLocalChanges();

        return null;
    }


    //~ Static/instance variables .............................................

    private Map<String, Integer> messageToIndexMap;
    private Map<String, Integer> protocolToIndexMap;

    private EOCustomObject[][] objectMatrix;
    private Boolean[][] selectionMatrix;
}
