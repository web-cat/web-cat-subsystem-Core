package org.webcat.core.objectquery;

import org.webcat.core.ObjectQuery;
import org.webcat.core.User;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSKeyValueCoding;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author Tony Allevato
 * @version $Id: ObjectQuerySurrogate.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
 */
public class ObjectQuerySurrogate implements NSKeyValueCoding
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public ObjectQuerySurrogate(String objectType)
    {
        this.objectType = objectType;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String objectType()
    {
        return objectType;
    }


    // ----------------------------------------------------------
    public void switchToAssistantPicker()
    {
        isEditing = false;
        queryAssistant = null;
    }


    // ----------------------------------------------------------
    public void switchToQueryAssistant(QueryAssistantDescriptor qad)
    {
        isEditing = true;

        EOQualifier q = null;

        if (savedQuery != null)
        {
            q = savedQuery.qualifier();
            savedQuery = null;
        }
        else if (model != null)
        {
            q = model.qualifierFromValues();
        }

        queryAssistant = qad;
        model = qad.createModel();

        if (q != null)
        {
            model.takeValuesFromQualifier(q);
        }
    }


    // ----------------------------------------------------------
    public void switchToSavedQuery(ObjectQuery query)
    {
        isEditing = true;

        savedQuery = query;

        queryAssistant = null;
        model = null;
    }


    // ----------------------------------------------------------
    public boolean isEditing()
    {
        return isEditing;
    }


    // ----------------------------------------------------------
    public QueryAssistantDescriptor queryAssistant()
    {
        return queryAssistant;
    }


    // ----------------------------------------------------------
    public AbstractQueryAssistantModel model()
    {
        return model;
    }


    // ----------------------------------------------------------
    public String descriptionToSave()
    {
        return descriptionToSave;
    }


    // ----------------------------------------------------------
    public void setDescriptionToSave(String description)
    {
        descriptionToSave = description;
    }


    // ----------------------------------------------------------
    public ObjectQuery savedQuery()
    {
        return savedQuery;
    }


    // ----------------------------------------------------------
    public ObjectQuery commitAndGetQuery(EOEditingContext ec, User user)
    {
        if (savedQuery != null)
        {
            return savedQuery;
        }
        else if (model != null)
        {
            ObjectQuery query = ObjectQuery.create(ec, false);

            query.setQueryAssistantId(queryAssistant.id());
            query.setDescription(descriptionToSave);
            query.setUserRelationship(user.localInstance(ec));
            query.setQualifier(model.qualifierFromValues());
            query.setObjectType(objectType);

            return query;
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public String displayableSummary()
    {
        if (savedQuery != null)
        {
            return "Use the previously saved query named <b>" +
                savedQuery.description() + "</b>";
        }
        else if (queryAssistant != null)
        {
            return queryAssistant.description().replace("|", "");
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public void takeValueForKey(Object value, String key)
    {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(
                this, value, key);
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }


    //~ Static/instance variables .............................................

    private boolean isEditing;
    private String objectType;
    private QueryAssistantDescriptor queryAssistant;
    private AbstractQueryAssistantModel model;
    private String descriptionToSave;
    private ObjectQuery savedQuery;
}
