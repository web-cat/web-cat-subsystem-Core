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
 * @binding selectedListTitle the string to display for the title of the
 *     list of selected users; if omitted, "Selected Users" will be used
 * @binding availableListTitle the string to display for the title of the list
 *     of available users; if omitted, "Available Users" will be used
 * @binding qualifier a qualifier used to filter the master list of available
 *     users
 * @binding selectedUsers the array of users who are selected in the list
 *
 * @author Tony Allevato
 * @author Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class FilteringUserSelector extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public FilteringUserSelector(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    // Public bindings
    public String selectedListTitle;
    public String availableListTitle;

    // Used internally
    public ComponentIDGenerator idFor;
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

        if (availableUsers == null)
        {
            resetAvailableUsers();
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public WOActionResults okAction()
    {
        return (WOActionResults) valueForBinding("okAction");
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
            NSArray<User> batch = batchIterator.nextBatch();

            availableUsers.addObjectsFromArray(batch);
        }
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
        selectedUsers = new NSMutableArray<User>(
                ERXArrayUtilities.arrayWithoutDuplicates(someUsers));

        USER_SORT_CRITERIA.sort(selectedUsers);
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

                if (!selectedUsers.containsObject(user))
                {
                    selectedUsers.addObject(user);
                }
            }
            catch (JSONException e)
            {
                log.warn(e);
            }
        }

        USER_SORT_CRITERIA.sort(selectedUsers);
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
            selectedUsers.removeObjectAtIndex(indices[i] - displacement);
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


    //~ Static/instance variables .............................................

    private static final int BATCH_SIZE = 12;

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
