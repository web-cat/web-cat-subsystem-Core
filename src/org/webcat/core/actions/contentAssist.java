/*==========================================================================*\
 |  $Id: contentAssist.java,v 1.3 2011/12/25 02:24:54 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2011 Virginia Tech
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

package org.webcat.core.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webcat.core.Application;
import org.webcat.core.EntityUtils;
import org.webcat.core.KVCAttributeFinder;
import org.webcat.core.KVCAttributeInfo;
import org.webcat.core.Subsystem;
import org.webcat.woextensions.ReadOnlyEditingContext;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSComparator.ComparisonException;
import er.extensions.appserver.ERXDirectAction;

//-------------------------------------------------------------------------
/**
 * A direct action used by the BIRT report designer to request information about
 * entities and key paths, used for content assistance and previewing purposes.
 *
 * @author Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.3 $, $Date: 2011/12/25 02:24:54 $
 */
public class contentAssist
    extends ERXDirectAction
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     * @param request The incoming request
     */
    public contentAssist(WORequest request)
    {
        super(request);
    }


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * An action that returns as its response a list of entities recognized by
     * Web-CAT and the keys that are valid on them.
     *
     * @return the action response
     */
    public WOActionResults entityDescriptionsAction()
    {
        WOResponse response = new WOResponse();

        int designerVersion = designerVersionFromRequest();

        if (designerVersion <= VERSION_1_0_0)
        {
            // Use the old-style response.
            appendEntityDescriptions1_0_0(response);
        }
        else
        {
            try
            {
                appendEntityDescriptions1_1_0(response);
            }
            catch (JSONException e)
            {
                // Ignore it for now.
            }
        }

        return response;
    }


    // ----------------------------------------------------------
    private int designerVersionFromRequest()
    {
        String versionString =
            (String) request().formValueForKey("designerVersion");

        if (versionString == null)
        {
            return 1 * 256 * 256;
        }
        else
        {
            return Integer.parseInt((String)
                    request().formValueForKey("designerVersion"));
        }
    }


    // ----------------------------------------------------------
    private void appendEntityDescriptions1_0_0(WOResponse response)
    {
        NSDictionary<String, String> versions = subsystemVersions();

        for (String subsystem : versions.allKeys())
        {
            response.appendContentString("version:" + subsystem + "," +
                versions.objectForKey(subsystem) + "\n");
        }

        for (EOModel model : EOModelGroup.defaultGroup().models())
        {
            for (EOEntity entity : model.entities())
            {
                String className = entity.className();
                boolean exclude = false;

                for (String toExclude : ENTITIES_TO_EXCLUDE)
                {
                    if (toExclude.equals(className))
                    {
                        exclude = true;
                        break;
                    }
                }

                if (exclude)
                {
                    continue;
                }

                try
                {
                    Class<?> klass = Class.forName(className);

                    response.appendContentString("entity:" +
                        klass.getSimpleName() + "\n");

                    NSArray<KVCAttributeInfo> attributes =
                        KVCAttributeFinder.attributesForClass(klass, "");

                    for (KVCAttributeInfo attr : attributes)
                    {
                        response.appendContentString("attribute:" +
                                attr.name() + "," + attr.type() +
                                "\n");
                    }
                }
                catch (ClassNotFoundException e)
                {
                    // If, for some reason, the class was not found, just don't
                    // output any keys for it.
                }
            }
        }
    }


    // ----------------------------------------------------------
    private void appendEntityDescriptions1_1_0(WOResponse response)
    throws JSONException
    {
        NSDictionary<String, String> versions = subsystemVersions();

        JSONObject root = new JSONObject();

        JSONArray subsystemArray = new JSONArray();

        for (String subsystem : versions.allKeys())
        {
            JSONObject subsObj = new JSONObject();
            subsObj.put("name", subsystem);
            subsObj.put("version", versions.objectForKey(subsystem));
            subsystemArray.put(subsObj);
        }

        root.put("subsystems", subsystemArray);

        JSONArray entityArray = new JSONArray();

        for (EOModel model : EOModelGroup.defaultGroup().models())
        {
            for (EOEntity entity : model.entities())
            {
                String className = entity.className();
                boolean exclude = false;

                for (String toExclude : ENTITIES_TO_EXCLUDE)
                {
                    if (toExclude.equals(className))
                    {
                        exclude = true;
                        break;
                    }
                }

                if (exclude)
                {
                    continue;
                }

                try
                {
                    Class<?> klass = Class.forName(className);

                    JSONObject entityObj = new JSONObject();
                    entityObj.put("name", klass.getSimpleName());

                    JSONArray attributeArray = new JSONArray();

                    NSArray<KVCAttributeInfo> attributes =
                        KVCAttributeFinder.attributesForClass(klass, "");

                    try
                    {
                        attributes =
                            attributes.sortedArrayUsingComparator(new NSComparator() {
                                @Override
                                public int compare(Object _lhs, Object _rhs)
                                        throws ComparisonException
                                {
                                    KVCAttributeInfo lhs = (KVCAttributeInfo) _lhs;
                                    KVCAttributeInfo rhs = (KVCAttributeInfo) _rhs;

                                    return lhs.name().compareTo(rhs.name());
                                }
                            });
                    }
                    catch (ComparisonException e)
                    {
                        // Do nothing.
                    }

                    for (KVCAttributeInfo attr : attributes)
                    {
                        JSONObject attrObj = new JSONObject();
                        attrObj.put("name", attr.name());
                        attrObj.put("type", attr.type());
                        attrObj.put("properties",
                                new JSONObject(attr.allPropertyValues()));

                        attributeArray.put(attrObj);
                    }

                    entityObj.put("attributes", attributeArray);

                    entityArray.put(entityObj);
                }
                catch (ClassNotFoundException e)
                {
                    // If, for some reason, the class was not found, just don't
                    // output any keys for it.
                }
            }
        }

        root.put("entities", entityArray);

        response.appendContentString(root.toString());
    }


    // ----------------------------------------------------------
    /**
     * An action that returns as its response a list of active objects for
     * various entities in Web-CAT, used to give the user choices when
     * constructing a preview query in the designer.
     *
     * @return the action response
     */
    public WOActionResults objectDescriptionsAction()
    {
        WOResponse response = new WOResponse();

        int designerVersion = designerVersionFromRequest();

        if (designerVersion <= VERSION_1_0_0)
        {
            // Use the old-style response.
            appendObjectDescriptions1_0_0(response);
        }
        else
        {
            try
            {
                appendObjectDescriptions1_1_0(response);
            }
            catch (JSONException e)
            {
                // Ignore it for now.
            }
        }

        return response;
    }


    // ----------------------------------------------------------
    public void appendObjectDescriptions1_0_0(WOResponse response)
    {
        ReadOnlyEditingContext ec = ReadOnlyEditingContext.newEditingContext();
        try
        {
            ec.lock();
            for (String entityName : OBJECTS_TO_DESCRIBE)
            {
                NSArray<EOSortOrdering> orderings =
                    EntityUtils.sortOrderingsForEntityNamed(entityName);

                EOFetchSpecification fetchSpec = new EOFetchSpecification(
                    entityName, null, orderings);
                fetchSpec.setFetchLimit(250);

                @SuppressWarnings("unchecked")
                NSArray<EOEnterpriseObject> objects =
                    ec.objectsWithFetchSpecification(fetchSpec);

                response.appendContentString("entity:" + entityName + "\n");

                for (EOEnterpriseObject object : objects)
                {
                    Number id = (Number)EOUtilities.primaryKeyForObject(
                        ec, object).objectForKey( "id" );

                    response.appendContentString("object:" + id.toString() + "," +
                        object.toString() + "\n");
                }
            }
        }
        finally
        {
            ec.unlock();
            ec.dispose();
        }
    }


    // ----------------------------------------------------------
    public void appendObjectDescriptions1_1_0(WOResponse response)
    throws JSONException
    {
        ReadOnlyEditingContext ec = ReadOnlyEditingContext.newEditingContext();
        try
        {
            ec.lock();

            JSONObject root = new JSONObject();

            JSONArray entityArray = new JSONArray();

            for (String entityName : OBJECTS_TO_DESCRIBE)
            {
                NSArray<EOSortOrdering> orderings =
                    EntityUtils.sortOrderingsForEntityNamed(entityName);

                EOFetchSpecification fetchSpec = new EOFetchSpecification(
                    entityName, null, orderings);
                fetchSpec.setFetchLimit(250);

                @SuppressWarnings("unchecked")
                NSArray<EOEnterpriseObject> objects =
                    ec.objectsWithFetchSpecification(fetchSpec);

                JSONObject entityObj = new JSONObject();
                entityObj.put("name", entityName);

                JSONArray objectArray = new JSONArray();

                for (EOEnterpriseObject object : objects)
                {
                    Number id = (Number)EOUtilities.primaryKeyForObject(
                        ec, object).objectForKey( "id" );

                    JSONObject objectObj = new JSONObject();
                    objectObj.put("id", id.intValue());
                    objectObj.put("representation", object.toString());

                    objectArray.put(objectObj);
                }

                entityObj.put("objects", objectArray);

                entityArray.put(entityObj);
            }

            root.put("entities", entityArray);

            response.appendContentString(root.toString());
        }
        finally
        {
            ec.unlock();
            ec.dispose();
        }
    }


    // ----------------------------------------------------------
    /**
     * An action that returns as its response a list of the subsystems installed
     * in this instance of Web-CAT and their versions.
     *
     * @return the action response
     */
    public WOActionResults subsystemVersionCheckAction()
    {
        WOResponse response = new WOResponse();

        NSDictionary<String, String> versions = subsystemVersions();

        for (String subsystem : versions.allKeys())
        {
            response.appendContentString("version:" + subsystem + "," +
                versions.objectForKey(subsystem) + "\n");
        }

        return response;
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    /**
     * Gets a dictionary whose keys are the names of the subsystems in this
     * instance of Web-CAT and values are their versions.
     *
     * @return a dictionary containing the subsystem version information
     */
    private NSDictionary<String, String> subsystemVersions()
    {
        NSMutableDictionary<String, String> subsystemVersions =
            new NSMutableDictionary<String, String>();

        NSArray<Subsystem> subsystems = Application.wcApplication()
            .subsystemManager().subsystems();

        for (Subsystem subsystem : subsystems)
        {
            for (String nameToCheck : SUBSYSTEMS_TO_CHECK)
            {
                if (nameToCheck.equals(subsystem.name()))
                {
                    subsystemVersions.setObjectForKey(
                        subsystem.descriptor().currentVersion(),
                        nameToCheck);
                    break;
                }
            }
        }

        return subsystemVersions;
    }


    //~ Instance/static variables .............................................

    private static final int VERSION_1_0_0 = 256 * 256;
    @SuppressWarnings("unused")
    private static final int VERSION_1_1_0 = 256 * 256 + 256;

    private static final String[] ENTITIES_TO_EXCLUDE = {
        "CoreSelections", "ERXGenericRecord", "GraderPrefs", "LoginSession",
        "PasswordChangeRequest"
    };

    private static final String[] OBJECTS_TO_DESCRIBE = {
        "Assignment", "AssignmentOffering", "AuthenticationDomain",
        "Course", "CourseOffering", "Department", "Semester"
    };

    private static final String[] SUBSYSTEMS_TO_CHECK = {
        "BatchProcessor", "Core", "Grader", "Reporter"
    };
}
