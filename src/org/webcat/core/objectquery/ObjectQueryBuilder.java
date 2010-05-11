package org.webcat.core.objectquery;

import org.webcat.core.ObjectQuery;
import org.webcat.core.WCComponent;
import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.ComponentIDGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;

public class ObjectQueryBuilder extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public ObjectQueryBuilder(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public ComponentIDGenerator idFor;

    public ObjectQuerySurrogate surrogate;
    public String objectType;
    public String description;

    public NSArray<QueryAssistantDescriptor> queryAssistants;
    public QueryAssistantDescriptor aQueryAssistant;

    public NSArray<ObjectQuery> savedQueries;
    public ObjectQuery aSavedQuery;
    public ObjectQuery selectedSavedQuery;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse(WOResponse response, WOContext context)
    {
        idFor = new ComponentIDGenerator(this);

        queryAssistants =
            QueryAssistantManager.getInstance().assistantsForEntity(objectType);

        savedQueries = ObjectQuery.savedQueriesForObjectTypeAndUser(
                localContext(), objectType, user());

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public String linkPartOfQueryAssistantDescription()
    {
        String desc = aQueryAssistant.description();
        int pipe = desc.indexOf('|');

        if (pipe >= 0)
        {
            return desc.substring(0, pipe);
        }
        else
        {
            return desc;
        }
    }


    // ----------------------------------------------------------
    public String nonLinkPartOfQueryAssistantDescription()
    {
        String desc = aQueryAssistant.description();
        int pipe = desc.indexOf('|');

        if (pipe >= 0)
        {
            return desc.substring(pipe + 1);
        }
        else
        {
            return desc;
        }
    }


    // ----------------------------------------------------------
    public WOActionResults chooseQueryAssistant()
    {
        surrogate.switchToQueryAssistant(aQueryAssistant);
        return new JavascriptGenerator()
            .refresh(idFor.get("queryAssistantContainer"));
    }


    // ----------------------------------------------------------
    public WOActionResults revertEditing()
    {
        surrogate.switchToAssistantPicker();
        return new JavascriptGenerator()
            .refresh(idFor.get("queryAssistantContainer"));
    }


    // ----------------------------------------------------------
    public WOActionResults useSavedQuery()
    {
        surrogate.switchToSavedQuery(selectedSavedQuery);
        return new JavascriptGenerator()
            .refresh(idFor.get("queryAssistantContainer"));
    }
}
