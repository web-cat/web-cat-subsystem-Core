/*==========================================================================*\
 |  $Id: FilteringUserSelector.java,v 1.3 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2010-2012 Virginia Tech
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

import java.util.Arrays;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.ComponentIDGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import er.extensions.eof.ERXFetchSpecificationBatchIterator;
import er.extensions.eof.ERXQ;
import er.extensions.eof.ERXSortOrdering.ERXSortOrderings;
import er.extensions.foundation.ERXArrayUtilities;

//-------------------------------------------------------------------------
/**
 * A reusable component that allows a user to select a set of users, for things
 * like course enrollment or assigning partners to a submission.
 *
 * @binding simpleLayout if true, the user list will be a simple list of names
 *     separated by BR tags, with the edit button underneath, suitable for
 *     embedding in a small page element such as a table cell. If false (the
 *     default), a fuller UI will be displayed
 * @binding actionTitle the string displayed in the edit button and in the
 *     title of the dialog
 * @binding dialogMessage a message displayed in the dialog box
 * @binding hidesTableOnEmptySelection if true, the master list of users will
 *     be hidden if there are no users selected; if false, the list will be
 *     shown as a table that reads "No users are currently selected." Defaults
 *     to false.
 * @binding selectedListTitle the string to display for the title of the
 *     list of selected users; if omitted, "Selected Users" will be used
 * @binding availableListTitle the string to display for the title of the list
 *     of available users; if omitted, "Available Users" will be used
 * @binding qualifier a qualifier used to filter the master list of available
 *     users
 * @binding selectedUsers the array of users who are selected in the list
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.3 $, $Date: 2012/03/28 13:48:08 $
 */
public class FilteringUserSelector
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public FilteringUserSelector(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    // Public bindings
    public boolean simpleLayout;
    public String actionTitle;
    public String dialogMessage;
    public String selectedListTitle;
    public String availableListTitle;
    public boolean hidesTableOnEmptySelection;

    // Used internally
    public ComponentIDGenerator idFor;
    public NSMutableArray<User> editingUsers;
    public User aSelectedUser;
    public User anAvailableUser;
    public int index;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        idFor = new ComponentIDGenerator(this);

        if (selectedUsers == null)
        {
            selectedUsers = new NSMutableArray<User>();
        }

        if (editingUsers == null)
        {
            editingUsers = selectedUsers.mutableClone();
        }

        if (availableUsers == null)
        {
            resetAvailableUsers();
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    private void resetAvailableUsers()
    {
        EOFetchSpecification fspec = new EOFetchSpecification(
                User.ENTITY_NAME, fullQualifier(), USER_SORT_CRITERIA);

        batchIterator = new ERXFetchSpecificationBatchIterator(
                fspec, localContext(), BATCH_SIZE);

        availableUsers = new NSMutableArray<User>();

        addNextBatchToAvailableUsers();
    }


    // ----------------------------------------------------------
    private void addNextBatchToAvailableUsers()
    {
        if (batchIterator.hasNextBatch())
        {
            @SuppressWarnings("unchecked")
            NSArray<User> batch = batchIterator.nextBatch();

            availableUsers.addObjectsFromArray(batch);
        }
    }


    // ----------------------------------------------------------
    public boolean isEmptySelectionAndHide()
    {
        return hidesTableOnEmptySelection && selectedUsers.count() == 0;
    }


    // ----------------------------------------------------------
    /**
     * Gets the full JavaScript reference to the proxy object that is used to
     * make RPC calls to the server-side component.
     *
     * @return the full JavaScript reference to the proxy object
     */
    public String proxyReference()
    {
        return idFor.valueForKey("jsonrpc") + ".userSelector";
    }


    // ----------------------------------------------------------
    public String actionTitle()
    {
        if (actionTitle == null)
        {
            return DEFAULT_ACTION_TITLE;
        }
        else
        {
            return actionTitle;
        }
    }


    // ----------------------------------------------------------
    public String selectedListTitle()
    {
        if (selectedListTitle == null)
        {
            return DEFAULT_SELECTED_LIST_TITLE;
        }
        else
        {
            return selectedListTitle;
        }
    }


    // ----------------------------------------------------------
    public String availableListTitle()
    {
        if (availableListTitle == null)
        {
            return DEFAULT_AVAILABLE_LIST_TITLE;
        }
        else
        {
            return availableListTitle;
        }
    }


    // ----------------------------------------------------------
    public NSArray<User> selectedUsers()
    {
        return selectedUsers;
    }


    // ----------------------------------------------------------
    public void setSelectedUsers(NSArray<User> someUsers)
    {
        @SuppressWarnings("unchecked")
        NSMutableArray<User> result =
            new NSMutableArray<User>(
                ERXArrayUtilities.arrayWithoutDuplicates(someUsers));
        selectedUsers = result;
    }


    // ----------------------------------------------------------
    public EOQualifier qualifier()
    {
        return qualifier;
    }


    // ----------------------------------------------------------
    public void setQualifier(EOQualifier aQualifier)
    {
        qualifier = aQualifier;
    }


    // ----------------------------------------------------------
    public EOQualifier fullQualifier()
    {
        EOQualifier filter = nameFilteringQualifier();

        if (qualifier != null && filter != null)
        {
            return ERXQ.and(qualifier, filter);
        }
        else if (filter != null)
        {
            return filter;
        }
        else
        {
            return qualifier;
        }
    }


    // ----------------------------------------------------------
    private EOQualifier nameFilteringQualifier()
    {
        EOQualifier filter = null;

        if (filterString != null)
        {
            String likeString = "*" + filterString + "*";

            filter = ERXQ.or(
                        User.userName.likeInsensitive(likeString),
                        User.firstName.likeInsensitive(likeString),
                        User.lastName.likeInsensitive(likeString));
        }

        return filter;
    }


    // ----------------------------------------------------------
    public NSArray<User> availableUsers()
    {
        return availableUsers;
    }


    // ----------------------------------------------------------
    public boolean areMoreUsersAvailable()
    {
        if (batchIterator != null)
        {
            return batchIterator.hasNextBatch();
        }
        else
        {
            return false;
        }
    }


    // ----------------------------------------------------------
    public WOActionResults okPressed()
    {
        selectedUsers = editingUsers.mutableClone();
//        pushValuesToParent();

        JavascriptGenerator script = new JavascriptGenerator();
        script.refresh(idFor.get("masterPane")).
               dijit(idFor.get("dialog")).call("hide");

        return script;
    }


    // ----------------------------------------------------------
    public void updateFilter(String aFilterString)
    {
        filterString = aFilterString;
        resetAvailableUsers();
    }


    // ----------------------------------------------------------
    public void addToSelectedUsers(JSONArray availableIndices)
    {
        for (int i = 0; i < availableIndices.length(); i++)
        {
            try
            {
                int availableIndex = availableIndices.getInt(i);

                User user = availableUsers.objectAtIndex(availableIndex);

                if (!editingUsers.containsObject(user))
                {
                    editingUsers.addObject(user);
                }
            }
            catch (JSONException e)
            {
                log.warn(e);
            }
        }

        USER_SORT_CRITERIA.sort(editingUsers);
    }


    // ----------------------------------------------------------
    public void deleteFromSelectedUsers(JSONArray selectedIndices)
    {
        int[] indices = new int[selectedIndices.length()];

        for (int i = 0; i < selectedIndices.length(); i++)
        {
            try
            {
                indices[i] = selectedIndices.getInt(i);
            }
            catch (JSONException e)
            {
                log.warn(e);
            }
        }

        Arrays.sort(indices);

        int displacement = 0;

        for (int i = 0; i < indices.length; i++)
        {
            editingUsers.removeObjectAtIndex(indices[i] - displacement);
            displacement++;
        }
    }


    // ----------------------------------------------------------
    public JavascriptGenerator showMoreUsers()
    {
        addNextBatchToAvailableUsers();

        return new JavascriptGenerator().refresh(
                idFor.get("availableUsersPane"));
    }


    // ----------------------------------------------------------
    public String openDialogScript()
    {
        return "dijit.byId('" + idFor.get("dialog") + "').show();";
    }


    //~ Static/instance variables .............................................

    private static final int BATCH_SIZE = 12;

    private static final String DEFAULT_ACTION_TITLE =
        "Edit Selected Users...";

    private static final String DEFAULT_SELECTED_LIST_TITLE =
        "Selected users";

    private static final String DEFAULT_AVAILABLE_LIST_TITLE =
        "Available users";

    private static final ERXSortOrderings USER_SORT_CRITERIA =
        User.lastName.ascInsensitive().then(
                User.userName.ascInsensitive());

    private ERXFetchSpecificationBatchIterator batchIterator;

    private NSMutableArray<User> selectedUsers;
    private EOQualifier qualifier;

    private String filterString;
    private NSMutableArray<User> availableUsers;

    private static final Logger log = Logger.getLogger(
            FilteringUserSelector.class);
}
