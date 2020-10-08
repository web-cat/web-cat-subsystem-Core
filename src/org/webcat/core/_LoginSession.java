/*==========================================================================*\
 |  _LoginSession.java
 |*-------------------------------------------------------------------------*|
 |  Created by eogenerator
 |  DO NOT EDIT.  Make changes to LoginSession.java instead.
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

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXKey;
import org.apache.log4j.Logger;
import org.webcat.core.EOBasedKeyGenerator;
import org.webcat.woextensions.WCFetchSpecification;

// -------------------------------------------------------------------------
/**
 * An automatically generated EOGenericRecord subclass.  DO NOT EDIT.
 * To change, use EOModeler, or make additions in
 * LoginSession.java.
 *
 * @author Generated by eogenerator
 * @version version suppressed to control auto-generation
 */
public abstract class _LoginSession
    extends org.webcat.core.EOBase
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new _LoginSession object.
     */
    public _LoginSession()
    {
        super();
    }


    // ----------------------------------------------------------
    /**
     * A static factory method for creating a new
     * LoginSession object given required
     * attributes and relationships.
     * @param editingContext The context in which the new object will be
     * inserted
     * @param usagePeriodValue
     * @return The newly created object
     */
    public static LoginSession create(
        EOEditingContext editingContext,
        org.webcat.core.UsagePeriod usagePeriodValue
        )
    {
        LoginSession eoObject = (LoginSession)
            EOUtilities.createAndInsertInstance(
                editingContext,
                _LoginSession.ENTITY_NAME);
        eoObject.setUsagePeriodRelationship(usagePeriodValue);
        return eoObject;
    }


    // ----------------------------------------------------------
    /**
     * Get a local instance of the given object in another editing context.
     * @param editingContext The target editing context
     * @param eo The object to import
     * @return An instance of the given object in the target editing context
     */
    public static LoginSession localInstance(
        EOEditingContext editingContext, LoginSession eo)
    {
        return (eo == null)
            ? null
            : (LoginSession)EOUtilities.localInstanceOfObject(
                editingContext, eo);
    }


    // ----------------------------------------------------------
    /**
     * Look up an object by id number.  Assumes the editing
     * context is appropriately locked.
     * @param ec The editing context to use
     * @param id The id to look up
     * @return The object, or null if no such id exists
     */
    public static LoginSession forId(
        EOEditingContext ec, int id)
    {
        LoginSession obj = null;
        if (id > 0)
        {
            NSArray<LoginSession> objects =
                objectsMatchingValues(ec, "id", new Integer(id));
            if (objects != null && objects.count() > 0)
            {
                obj = objects.objectAtIndex(0);
            }
        }
        return obj;
    }


    // ----------------------------------------------------------
    /**
     * Look up an object by id number.  Assumes the editing
     * context is appropriately locked.
     * @param ec The editing context to use
     * @param id The id to look up
     * @return The object, or null if no such id exists
     */
    public static LoginSession forId(
        EOEditingContext ec, EOGlobalID id)
    {
        return (LoginSession)ec.faultForGlobalID(id, ec);
    }


    // ----------------------------------------------------------
    /**
     * Look up an object by id number.  Assumes the editing
     * context is appropriately locked.
     * @param ec The editing context to use
     * @param id The id to look up
     * @return The object, or null if no such id exists
     */
    public static LoginSession forId(
        EOEditingContext ec, String id)
    {
        return forId(ec, er.extensions.foundation.ERXValueUtilities.intValue(id));
    }


    //~ Constants (for key names) .............................................

    // Attributes ---
    public static final String EXPIRATION_TIME_KEY = "expirationTime";
    public static final ERXKey<NSTimestamp> expirationTime =
        new ERXKey<NSTimestamp>(EXPIRATION_TIME_KEY);
    public static final String SESSION_ID_KEY = "sessionId";
    public static final ERXKey<String> sessionId =
        new ERXKey<String>(SESSION_ID_KEY);
    // To-one relationships ---
    public static final String USAGE_PERIOD_KEY = "usagePeriod";
    public static final ERXKey<org.webcat.core.UsagePeriod> usagePeriod =
        new ERXKey<org.webcat.core.UsagePeriod>(USAGE_PERIOD_KEY);
    public static final String USER_KEY = "user";
    public static final ERXKey<org.webcat.core.User> user =
        new ERXKey<org.webcat.core.User>(USER_KEY);
    // To-many relationships ---
    // Fetch specifications ---
    public static final String SESSIONS_WITH_USER_PID_FSPEC = "sessionsWithUserPid";
    public static final String ENTITY_NAME = "LoginSession";

    public transient final EOBasedKeyGenerator generateKey =
        new EOBasedKeyGenerator(this);


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Get a local instance of this object in another editing context.
     * @param editingContext The target editing context
     * @return An instance of this object in the target editing context
     */
    public LoginSession localInstance(EOEditingContext editingContext)
    {
        return (LoginSession)EOUtilities.localInstanceOfObject(
            editingContext, this);
    }


    // ----------------------------------------------------------
    /**
     * Refetch this object from the database.
     * @param editingContext The target editing context
     * @return An instance of this object in the target editing context
     */
    public LoginSession refetch(EOEditingContext editingContext)
    {
        return (LoginSession)refetchObjectFromDBinEditingContext(
            editingContext);
    }


    // ----------------------------------------------------------
    /**
     * Get a list of changes between this object's current state and the
     * last committed version.
     * @return a dictionary of the changes that have not yet been committed
     */
    @SuppressWarnings("unchecked")
    public NSDictionary<String, Object> changedProperties()
    {
        return changesFromSnapshot(
            editingContext().committedSnapshotForObject(this));
    }


    // ----------------------------------------------------------
    /**
     * Retrieve this object's <code>id</code> value.
     * @return the value of the attribute
     */
    public Number id()
    {
        try
        {
            return (Number)EOUtilities.primaryKeyForObject(
                editingContext() , this).objectForKey("id");
        }
        catch (Exception e)
        {
            return er.extensions.eof.ERXConstant.ZeroInteger;
        }
    }


    // ----------------------------------------------------------
    /**
     * Retrieve this object's <code>expirationTime</code> value.
     * @return the value of the attribute
     */
    public NSTimestamp expirationTime()
    {
        return (NSTimestamp)storedValueForKey( "expirationTime" );
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>expirationTime</code>
     * property.
     *
     * @param value The new value for this property
     */
    public void setExpirationTime( NSTimestamp value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "setExpirationTime("
                + value + "): was " + expirationTime() );
        }
        takeStoredValueForKey( value, "expirationTime" );
    }


    // ----------------------------------------------------------
    /**
     * Retrieve this object's <code>sessionId</code> value.
     * @return the value of the attribute
     */
    public String sessionId()
    {
        return (String)storedValueForKey( "sessionId" );
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>sessionId</code>
     * property.
     *
     * @param value The new value for this property
     */
    public void setSessionId( String value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "setSessionId("
                + value + "): was " + sessionId() );
        }
        takeStoredValueForKey( value, "sessionId" );
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the entity pointed to by the <code>usagePeriod</code>
     * relationship.
     * @return the entity in the relationship
     */
    public org.webcat.core.UsagePeriod usagePeriod()
    {
        return (org.webcat.core.UsagePeriod)storedValueForKey( "usagePeriod" );
    }


    // ----------------------------------------------------------
    /**
     * Set the entity pointed to by the <code>usagePeriod</code>
     * relationship (DO NOT USE--instead, use
     * <code>setUsagePeriodRelationship()</code>.
     * This method is provided for WebObjects use.
     *
     * @param value The new entity to relate to
     */
    public void setUsagePeriod( org.webcat.core.UsagePeriod value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "setUsagePeriod("
                + value + "): was " + usagePeriod() );
        }
        takeStoredValueForKey( value, "usagePeriod" );
    }


    // ----------------------------------------------------------
    /**
     * Set the entity pointed to by the <code>usagePeriod</code>
     * relationship.  This method is a type-safe version of
     * <code>addObjectToBothSidesOfRelationshipWithKey()</code>.
     *
     * @param value The new entity to relate to
     */
    public void setUsagePeriodRelationship(
        org.webcat.core.UsagePeriod value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "setUsagePeriodRelationship("
                + value + "): was " + usagePeriod() );
        }
        if ( value == null )
        {
            org.webcat.core.UsagePeriod object = usagePeriod();
            if ( object != null )
                removeObjectFromBothSidesOfRelationshipWithKey( object, "usagePeriod" );
        }
        else
        {
            addObjectToBothSidesOfRelationshipWithKey( value, "usagePeriod" );
        }
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the entity pointed to by the <code>user</code>
     * relationship.
     * @return the entity in the relationship
     */
    public org.webcat.core.User user()
    {
        return (org.webcat.core.User)storedValueForKey( "user" );
    }


    // ----------------------------------------------------------
    /**
     * Set the entity pointed to by the <code>user</code>
     * relationship (DO NOT USE--instead, use
     * <code>setUserRelationship()</code>.
     * This method is provided for WebObjects use.
     *
     * @param value The new entity to relate to
     */
    public void setUser( org.webcat.core.User value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "setUser("
                + value + "): was " + user() );
        }
        takeStoredValueForKey( value, "user" );
    }


    // ----------------------------------------------------------
    /**
     * Set the entity pointed to by the <code>user</code>
     * relationship.  This method is a type-safe version of
     * <code>addObjectToBothSidesOfRelationshipWithKey()</code>.
     *
     * @param value The new entity to relate to
     */
    public void setUserRelationship(
        org.webcat.core.User value )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "setUserRelationship("
                + value + "): was " + user() );
        }
        if ( value == null )
        {
            org.webcat.core.User object = user();
            if ( object != null )
                removeObjectFromBothSidesOfRelationshipWithKey( object, "user" );
        }
        else
        {
            addObjectToBothSidesOfRelationshipWithKey( value, "user" );
        }
    }


    // ----------------------------------------------------------
    /**
     * Retrieve objects using a fetch specification.
     *
     * @param context The editing context to use
     * @param fspec The fetch specification to use
     *
     * @return an NSArray of the entities retrieved
     */
    @SuppressWarnings("unchecked")
    public static NSArray<LoginSession> objectsWithFetchSpecification(
        EOEditingContext context,
        EOFetchSpecification fspec)
    {
        return context.objectsWithFetchSpecification(fspec);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve all objects of this type.
     *
     * @param context The editing context to use
     *
     * @return an NSArray of the entities retrieved
     */
    public static NSArray<LoginSession> allObjects(
        EOEditingContext context)
    {
        return objectsMatchingQualifier(context, null, null);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve objects using a qualifier.
     *
     * @param context The editing context to use
     * @param qualifier The qualifier to use
     *
     * @return an NSArray of the entities retrieved
     */
    public static NSArray<LoginSession> objectsMatchingQualifier(
        EOEditingContext context,
        EOQualifier qualifier)
    {
        return objectsMatchingQualifier(context, qualifier, null);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve objects using a qualifier and sort orderings.
     *
     * @param context The editing context to use
     * @param qualifier The qualifier to use
     * @param sortOrderings The sort orderings to use
     *
     * @return an NSArray of the entities retrieved
     */
    public static NSArray<LoginSession> objectsMatchingQualifier(
        EOEditingContext context,
        EOQualifier qualifier,
        NSArray<EOSortOrdering> sortOrderings)
    {
        WCFetchSpecification<LoginSession> fspec =
            new WCFetchSpecification<LoginSession>(
                ENTITY_NAME, qualifier, sortOrderings);
        fspec.setUsesDistinct(true);
        fspec.setRefreshesRefetchedObjects(true);
        return objectsWithFetchSpecification(context, fspec);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the first object that matches a qualifier, when
     * sorted with the specified sort orderings.
     *
     * @param context The editing context to use
     * @param qualifier The qualifier to use
     * @param sortOrderings the sort orderings
     *
     * @return the first entity that was retrieved, or null if there was none
     */
    public static LoginSession firstObjectMatchingQualifier(
        EOEditingContext context,
        EOQualifier qualifier,
        NSArray<EOSortOrdering> sortOrderings)
    {
        WCFetchSpecification<LoginSession> fspec =
            new WCFetchSpecification<LoginSession>(
                ENTITY_NAME, qualifier, sortOrderings);
        fspec.setUsesDistinct(true);
        fspec.setRefreshesRefetchedObjects(true);
        fspec.setFetchLimit(1);
        NSArray<LoginSession> objects =
            objectsWithFetchSpecification(context, fspec);
        return (objects.size() > 0)
            ? objects.get(0)
            : null;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve a single object using a list of keys and values to match.
     *
     * @param context The editing context to use
     * @param qualifier The qualifier to use
     *
     * @return the single entity that was retrieved
     *
     * @throws EOUtilities.MoreThanOneException
     *     if there is more than one matching object
     */
    public static LoginSession uniqueObjectMatchingQualifier(
        EOEditingContext context,
        EOQualifier qualifier) throws EOUtilities.MoreThanOneException
    {
        NSArray<LoginSession> objects =
            objectsMatchingQualifier(context, qualifier);
        if (objects.size() > 1)
        {
            String msg = "fetching LoginSession";
            try
            {
                if (qualifier != null)
                {
                    msg += " where " + qualifier;
                }
                msg += ", result = " + objects;
            }
            catch (Exception e)
            {
                log.error("Exception building MoreThanOneException message, "
                    + "contents so far: " + msg, e);
            }
            throw new EOUtilities.MoreThanOneException(msg);
        }
        return (objects.size() > 0)
            ? objects.get(0)
            : null;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve objects using a list of keys and values to match.
     *
     * @param context The editing context to use
     * @param keysAndValues a list of keys and values to match, alternating
     *     "key", "value", "key", "value"...
     *
     * @return an NSArray of the entities retrieved
     */
    public static NSArray<LoginSession> objectsMatchingValues(
        EOEditingContext context,
        Object... keysAndValues)
    {
        if (keysAndValues.length % 2 != 0)
        {
            throw new IllegalArgumentException("There should a value "
                + "corresponding to every key that was passed. Args = "
                + java.util.Arrays.toString(keysAndValues));
        }

        NSMutableDictionary<String, Object> valueDictionary =
            new NSMutableDictionary<String, Object>();

        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];

            if (key == null)
            {
                throw new IllegalArgumentException(
                    "Found null where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }
            else if (!(key instanceof String))
            {
                throw new IllegalArgumentException(
                    "Found a " + key.getClass().getName() + " [" + key + "]"
                    + " where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }

            valueDictionary.setObjectForKey(value, (String)key);
        }

        return objectsMatchingValues(context, valueDictionary);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve objects using a dictionary of keys and values to match.
     *
     * @param context The editing context to use
     * @param keysAndValues a dictionary of keys and values to match
     *
     * @return an NSArray of the entities retrieved
     */
    @SuppressWarnings("unchecked")
    public static NSArray<LoginSession> objectsMatchingValues(
        EOEditingContext context,
        NSDictionary<String, Object> keysAndValues)
    {
        return EOUtilities.objectsMatchingValues(context, ENTITY_NAME,
            keysAndValues);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the first object that matches a set of keys and values, when
     * sorted with the specified sort orderings.
     *
     * @param context The editing context to use
     * @param sortOrderings the sort orderings
     * @param keysAndValues a list of keys and values to match, alternating
     *     "key", "value", "key", "value"...
     *
     * @return the first entity that was retrieved, or null if there was none
     */
    public static LoginSession firstObjectMatchingValues(
        EOEditingContext context,
        NSArray<EOSortOrdering> sortOrderings,
        Object... keysAndValues)
    {
        if (keysAndValues.length % 2 != 0)
        {
            throw new IllegalArgumentException("There should a value "
                + "corresponding to every key that was passed. Args = "
                + java.util.Arrays.toString(keysAndValues));
        }

        NSMutableDictionary<String, Object> valueDictionary =
            new NSMutableDictionary<String, Object>();

        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];

            if (key == null)
            {
                throw new IllegalArgumentException(
                    "Found null where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }
            else if (!(key instanceof String))
            {
                throw new IllegalArgumentException(
                    "Found a " + key.getClass().getName() + " [" + key + "]"
                    + " where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }

            valueDictionary.setObjectForKey(value, (String)key);
        }

        return firstObjectMatchingValues(
            context, sortOrderings, valueDictionary);
    }


    // ----------------------------------------------------------
    /**
     * Retrieves the first object that matches a set of keys and values, when
     * sorted with the specified sort orderings.
     *
     * @param context The editing context to use
     * @param sortOrderings the sort orderings
     * @param keysAndValues a dictionary of keys and values to match
     *
     * @return the first entity that was retrieved, or null if there was none
     */
    public static LoginSession firstObjectMatchingValues(
        EOEditingContext context,
        NSArray<EOSortOrdering> sortOrderings,
        NSDictionary<String, Object> keysAndValues)
    {
        WCFetchSpecification<LoginSession> fspec =
            new WCFetchSpecification<LoginSession>(
                ENTITY_NAME,
                EOQualifier.qualifierToMatchAllValues(keysAndValues),
                sortOrderings);
        fspec.setUsesDistinct(true);
        fspec.setRefreshesRefetchedObjects(true);
        fspec.setFetchLimit(1);

        NSArray<LoginSession> objects =
            objectsWithFetchSpecification( context, fspec );

        if ( objects.count() == 0 )
        {
            return null;
        }
        else
        {
            return objects.objectAtIndex(0);
        }
    }


    // ----------------------------------------------------------
    /**
     * Retrieve a single object using a list of keys and values to match.
     *
     * @param context The editing context to use
     * @param keysAndValues a list of keys and values to match, alternating
     *     "key", "value", "key", "value"...
     *
     * @return the single entity that was retrieved, or null if there was none
     *
     * @throws EOUtilities.MoreThanOneException
     *     if there is more than one matching object
     */
    public static LoginSession uniqueObjectMatchingValues(
        EOEditingContext context,
        Object... keysAndValues) throws EOUtilities.MoreThanOneException
    {
        if (keysAndValues.length % 2 != 0)
        {
            throw new IllegalArgumentException("There should a value "
                + "corresponding to every key that was passed. Args = "
                + java.util.Arrays.toString(keysAndValues));
        }

        NSMutableDictionary<String, Object> valueDictionary =
            new NSMutableDictionary<String, Object>();

        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];

            if (key == null)
            {
                throw new IllegalArgumentException(
                    "Found null where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }
            else if (!(key instanceof String))
            {
                throw new IllegalArgumentException(
                    "Found a " + key.getClass().getName() + " [" + key + "]"
                    + " where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }

            valueDictionary.setObjectForKey(value, (String)key);
        }

        return uniqueObjectMatchingValues(context, valueDictionary);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve an object using a dictionary of keys and values to match.
     *
     * @param context The editing context to use
     * @param keysAndValues a dictionary of keys and values to match
     *
     * @return the single entity that was retrieved, or null if there was none
     *
     * @throws EOUtilities.MoreThanOneException
     *     if there is more than one matching object
     */
    public static LoginSession uniqueObjectMatchingValues(
        EOEditingContext context,
        NSDictionary<String, Object> keysAndValues)
        throws EOUtilities.MoreThanOneException
    {
        try
        {
            return (LoginSession)EOUtilities.objectMatchingValues(
                context, ENTITY_NAME, keysAndValues);
        }
        catch (EOObjectNotAvailableException e)
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the count of all objects of this type.
     *
     * @param context The editing context to use
     *
     * @return the count of all objects
     */
    public static int countOfAllObjects(EOEditingContext context)
    {
        return countOfObjectsMatchingQualifier(context, null);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the count of objects that match a qualifier.
     *
     * @param context The editing context to use
     * @param qualifier The qualifier to use
     *
     * @return the count of objects matching the qualifier
     */
    public static int countOfObjectsMatchingQualifier(
        EOEditingContext context, EOQualifier qualifier)
    {
        return ERXEOControlUtilities.objectCountWithQualifier(
                context, ENTITY_NAME, qualifier);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the count of objects using a list of keys and values to match.
     *
     * @param context The editing context to use
     * @param keysAndValues a list of keys and values to match, alternating
     *     "key", "value", "key", "value"...
     *
     * @return the count of objects that match the specified values
     */
    public static int countOfObjectsMatchingValues(
        EOEditingContext context,
        Object... keysAndValues)
    {
        if (keysAndValues.length % 2 != 0)
        {
            throw new IllegalArgumentException("There should a value "
                + "corresponding to every key that was passed. Args = "
                + java.util.Arrays.toString(keysAndValues));
        }

        NSMutableDictionary<String, Object> valueDictionary =
            new NSMutableDictionary<String, Object>();

        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];

            if (key == null)
            {
                throw new IllegalArgumentException(
                    "Found null where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }
            else if (!(key instanceof String))
            {
                throw new IllegalArgumentException(
                    "Found a " + key.getClass().getName() + " [" + key + "]"
                    + " where a String key was expected, arguments = "
                    + java.util.Arrays.toString(keysAndValues));
            }

            valueDictionary.setObjectForKey(value, (String)key);
        }

        return countOfObjectsMatchingValues(context, valueDictionary);
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the count of objects using a dictionary of keys and values to
     * match.
     *
     * @param context The editing context to use
     * @param keysAndValues a dictionary of keys and values to match
     *
     * @return the count of objects that matched the specified values
     */
    public static int countOfObjectsMatchingValues(
        EOEditingContext context,
        NSDictionary<String, Object> keysAndValues)
    {
        return countOfObjectsMatchingQualifier(context,
                EOQualifier.qualifierToMatchAllValues(keysAndValues));
    }


    // ----------------------------------------------------------
    /**
     * Retrieve objects according to the <code>sessionsWithUserPid</code>
     * fetch specification.
     *
     * @param context The editing context to use
     * @param userNameBinding fetch spec parameter
     * @return an NSArray of the entities retrieved
     */
    public static NSArray<LoginSession> sessionsWithUserPid(
            EOEditingContext context,
            String userNameBinding)
    {
        EOFetchSpecification spec = WCFetchSpecification
            .fetchSpecificationNamed("sessionsWithUserPid", "LoginSession");

        NSMutableDictionary<String, Object> bindings =
            new NSMutableDictionary<String, Object>();

        if (userNameBinding != null)
        {
            bindings.setObjectForKey(userNameBinding,
                                     "userName");
        }
        spec = spec.fetchSpecificationWithQualifierBindings(bindings);

        NSArray<LoginSession> objects =
            objectsWithFetchSpecification(context, spec);
        if (log.isDebugEnabled())
        {
            log.debug("sessionsWithUserPid(ec"
                + ", " + userNameBinding
                + "): " + objects);
        }
        return objects;
    }


    // ----------------------------------------------------------
    /**
     * Produce a string representation of this object.  This implementation
     * calls UserPresentableDescription(), which uses WebObjects' internal
     * mechanism to print out the visible fields of this object.  Normally,
     * subclasses would override userPresentableDescription() to change
     * the way the object is printed.
     *
     * @return A string representation of the object's value
     */
    public String toString()
    {
        return userPresentableDescription();
    }


    // ----------------------------------------------------------
    /**
     * Hack to workaround bugs in ERXEOAccessUtilities.reapplyChanges().
     *
     * @param value the new value of the key
     * @param key the key to access
     */
    public void takeValueForKey(Object value, String key)
    {
        // if (ERXValueUtilities.isNull(value))
        if (value == NSKeyValueCoding.NullValue
            || value instanceof NSKeyValueCoding.Null)
        {
            value = null;
        }

        if (value instanceof NSData)
        {
            super.takeStoredValueForKey(value, key);
        }
        else
        {
            super.takeValueForKey(value, key);
        }
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger(LoginSession.class);
}
