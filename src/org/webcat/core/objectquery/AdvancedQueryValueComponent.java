/*==========================================================================*\
 |  $Id: AdvancedQueryValueComponent.java,v 1.2 2012/11/15 13:45:54 stedwar2 Exp $
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

package org.webcat.core.objectquery;

import java.text.ParseException;
import org.webcat.core.EntityUtils;
import org.webcat.core.WCComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.formatters.ERXTimestampFormatter;

//-------------------------------------------------------------------------
/**
 * Represents a component for editing the value used in a query--it uses
 * different lower-level components depending on the type of value used
 * in this particular query clause.
 *
 * @author Tony Allevato
 * @author Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/11/15 13:45:54 $
 */
public class AdvancedQueryValueComponent
    extends WCComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Create a new object.
     * @param context the page's context
     */
    public AdvancedQueryValueComponent(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

	/*
	 * The value being edited in this component. It will be converted to the
	 * appropriate type by using one of the typed getter/setter method pairs.
	 */
	public Object representedValue;

	/*
	 * The Java class type of the key path being edited in this component. If
	 * this does not match the type of the actual representedValue object,
	 * conversions will be made. (This supports making changes to the key path
	 * while editing without losing the requested value, unless the conversion
	 * is impossible.)
	 */
	public Class<?> valueType;

	/*
	 * If true, this component will permit multiple values to be selected
	 * (strings and numbers use comma-delimitation, entities become a multi-
	 * select list, booleans and timestamps are not supported).
	 *
	 * When true, representedValue is assumed to be an NSArray of values.
	 * Otherwise, it is just a simple value.
	 */
	public boolean multipleSelect = false;

	public Boolean booleanValueInList;
	public EOEnterpriseObject entityValueInList;
	public String calendarFieldId;
	public String calendarButtonId;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
	public void appendToResponse(WOResponse response, WOContext context)
	{
		calendarFieldId = context.elementID() + "_calendarField";
		calendarButtonId = context.elementID() + "_calendarButton";

		super.appendToResponse(response, context);
	}


	/*
	 * Methods for string values.
	 */

    // ----------------------------------------------------------
	public boolean isValueTypeString()
	{
		return AdvancedQueryUtils.typeOfClass(valueType) ==
			AdvancedQueryUtils.TYPE_STRING;
		//return String.class.isAssignableFrom(valueType);
	}


    // ----------------------------------------------------------
    public String stringValueOfRepresentedValue()
    {
    	if (representedValue != null)
        {
    		return representedValue.toString();
        }
    	else
        {
    		return null;
        }
    }


    // ----------------------------------------------------------
    public void setStringValueOfRepresentedValue(String value)
    {
    	representedValue = value;
    }


    // ----------------------------------------------------------
    public String commaDelimitedStringValuesOfRepresentedValueArray()
    {
        @SuppressWarnings("unchecked")
    	NSArray<Object> array = (NSArray<Object>)representedValue;

    	StringBuffer buffer = new StringBuffer();

    	if (array != null && array.count() > 0)
    	{
    		buffer.append(array.objectAtIndex(0));

    		for (int i = 1; i < array.count(); i++)
    		{
    			buffer.append(',');
    			buffer.append(array.objectAtIndex(i).toString());
    		}
    	}

    	return buffer.toString();
    }


    // ----------------------------------------------------------
    public void setCommaDelimitedStringValuesOfRepresentedValueArray(
    		String value)
    {
    	if (value != null)
    	{
	    	String[] values = value.split(",");

	    	NSMutableArray<String> array = new NSMutableArray<String>();

	    	for (String item : values)
	    	{
	    		array.addObject(item.trim());
	    	}

	    	representedValue = array;
    	}
    	else
    	{
    		representedValue = null;
    	}
    }


	/*
	 * Methods for integer values.
	 */

    // ----------------------------------------------------------
	public boolean isValueTypeInteger()
	{
		return AdvancedQueryUtils.typeOfClass(valueType) ==
			AdvancedQueryUtils.TYPE_INTEGER;
//		return Integer.class.isAssignableFrom(valueType) ||
//			valueType == Integer.TYPE;
	}


    // ----------------------------------------------------------
	public Integer integerValueOfObject(Object object)
	{
		if (object == null)
		{
			return null;
		}
		else if (object instanceof Number)
    	{
    		return ((Number)object).intValue();
    	}
    	else
    	{
    		try
    		{
    			return Integer.parseInt(object.toString().trim());
    		}
    		catch (NumberFormatException e)
    		{
    			return null;
    		}
    	}
	}


    // ----------------------------------------------------------
    public Integer integerValueOfRepresentedValue()
    {
    	return integerValueOfObject(representedValue);
    }


    // ----------------------------------------------------------
    public void setIntegerValueOfRepresentedValue(Integer value)
    {
    	representedValue = value;
    }


    // ----------------------------------------------------------
    public String commaDelimitedIntegerValuesOfRepresentedValueArray()
    {
        @SuppressWarnings("unchecked")
    	NSArray<Object> array = (NSArray<Object>)representedValue;
    	StringBuffer buffer = new StringBuffer();

    	if (array != null && array.count() > 0)
    	{
    		buffer.append(array.objectAtIndex(0));

    		for (int i = 1; i < array.count(); i++)
    		{
    			Integer ival = integerValueOfObject(array.objectAtIndex(i));

    			if (ival != null)
    			{
        			buffer.append(',');
        			buffer.append(ival.toString());
    			}
    		}
    	}

    	return buffer.toString();
    }


    // ----------------------------------------------------------
    public void setCommaDelimitedIntegerValuesOfRepresentedValueArray(
        String value)
    {
    	if (value != null)
    	{
	    	String[] values = value.split(",");
	    	NSMutableArray<Integer> array = new NSMutableArray<Integer>();

	    	for (String item : values)
	    	{
	    		Integer ival = integerValueOfObject(item);

	    		if (ival != null)
                {
	    			array.addObject(ival);
                }
	    	}

	    	representedValue = array;
    	}
    	else
    	{
    		representedValue = null;
    	}
    }


	/*
	 * Methods for double values.
	 */

    // ----------------------------------------------------------
	public boolean isValueTypeDouble()
	{
		return AdvancedQueryUtils.typeOfClass(valueType) ==
			AdvancedQueryUtils.TYPE_DOUBLE;
//		return Double.class.isAssignableFrom(valueType) ||
//			valueType == Double.TYPE;
	}


    // ----------------------------------------------------------
	public Double doubleValueOfObject(Object object)
	{
		if (object == null)
		{
			return null;
		}
		else if (object instanceof Number)
    	{
    		return ((Number)object).doubleValue();
    	}
    	else
    	{
    		try
    		{
    			return Double.parseDouble(object.toString().trim());
    		}
    		catch (NumberFormatException e)
    		{
    			return null;
    		}
    	}
	}


    // ----------------------------------------------------------
	public Double doubleValueOfRepresentedValue()
    {
		return doubleValueOfObject(representedValue);
    }


    // ----------------------------------------------------------
    public void setDoubleValueOfRepresentedValue(Double value)
    {
    	representedValue = value;
    }


    // ----------------------------------------------------------
    public String commaDelimitedDoubleValuesOfRepresentedValueArray()
    {
        @SuppressWarnings("unchecked")
    	NSArray<Object> array = (NSArray<Object>)representedValue;
    	StringBuffer buffer = new StringBuffer();

    	if (array != null && array.count() > 0)
    	{
    		buffer.append(array.objectAtIndex(0));

    		for (int i = 1; i < array.count(); i++)
    		{
    			Double dval = doubleValueOfObject(array.objectAtIndex(i));

    			if (dval != null)
    			{
        			buffer.append(',');
        			buffer.append(dval.toString());
    			}
    		}
    	}

    	return buffer.toString();
    }


    // ----------------------------------------------------------
    public void setCommaDelimitedDoubleValuesOfRepresentedValueArray(
    		String value)
    {
    	if (value != null)
    	{
	    	String[] values = value.split(",");
	    	NSMutableArray<Double> array = new NSMutableArray<Double>();

	    	for (String item : values)
	    	{
	    		Double dval = doubleValueOfObject(item);

	    		if (dval != null)
                {
	    			array.addObject(dval);
                }
	    	}

	    	representedValue = array;
    	}
    	else
    	{
    		representedValue = null;
    	}
    }


    /*
	 * Methods for boolean values.
	 */

    // ----------------------------------------------------------
	public boolean isValueTypeBoolean()
	{
		return AdvancedQueryUtils.typeOfClass(valueType) ==
			AdvancedQueryUtils.TYPE_BOOLEAN;
//		return Boolean.class.isAssignableFrom(valueType) ||
//			valueType == Boolean.TYPE;
	}


    // ----------------------------------------------------------
    public NSArray<Boolean> booleanValues()
    {
    	return BOOLEAN_VALUES;
    }


    // ----------------------------------------------------------
    public String displayStringForBooleanValue()
    {
    	return booleanValueInList ? "true" : "false";
    }


    // ----------------------------------------------------------
    public Boolean booleanValueOfRepresentedValue()
    {
    	if (representedValue instanceof Boolean)
        {
    		return (Boolean)representedValue;
        }
    	else
        {
    		return Boolean.FALSE;
        }
    }


    // ----------------------------------------------------------
    public void setBooleanValueOfRepresentedValue(Boolean value)
    {
    	representedValue = value;
    }


    /*
	 * Methods for timestamp values.
	 */

    // ----------------------------------------------------------
	public boolean isValueTypeTimestamp()
	{
		return AdvancedQueryUtils.typeOfClass(valueType) ==
			AdvancedQueryUtils.TYPE_TIMESTAMP;
//		return java.util.Date.class.isAssignableFrom(valueType);
	}


    // ----------------------------------------------------------
    public NSTimestamp timestampValueOfRepresentedValue()
    {
    	if (representedValue == null)
    	{
    		return null;
    	}
    	else if (representedValue instanceof NSTimestamp)
    	{
    		return (NSTimestamp)representedValue;
    	}
    	else if (representedValue instanceof java.util.Date)
    	{
    		return new NSTimestamp((java.util.Date)representedValue);
    	}
    	else
    	{
    		ERXTimestampFormatter formatter = new ERXTimestampFormatter();

    		try
    		{
				return (NSTimestamp)formatter.parseObject(
						representedValue.toString());
			}
    		catch (ParseException e)
    		{
				return null;
			}
    	}
    }


    // ----------------------------------------------------------
    public void setTimestampValueOfRepresentedValue(java.util.Date value)
    {
    	if (value instanceof NSTimestamp)
        {
    		representedValue = value;
        }
    	else
        {
    		representedValue = new NSTimestamp(value);
        }
    }


    /*
	 * Methods for entity values.
	 */

    // ----------------------------------------------------------
	public boolean isValueTypeEntity()
	{
//		return EOEnterpriseObject.class.isAssignableFrom(valueType);
		return AdvancedQueryUtils.typeOfClass(valueType) ==
			AdvancedQueryUtils.TYPE_ENTITY;
	}


    // ----------------------------------------------------------
    public NSArray<EOEnterpriseObject> entityValues()
    {
    	EOClassDescription classDesc =
    		EOClassDescription.classDescriptionForClass(valueType);
    	String entityName = classDesc.entityName();

    	NSArray<EOSortOrdering> orderings =
    	    EntityUtils.sortOrderingsForEntityNamed(entityName);

    	EOFetchSpecification fetchSpec = new EOFetchSpecification(
    		entityName, null, orderings);
    	fetchSpec.setFetchLimit(1000);

        @SuppressWarnings("unchecked")
    	NSArray<EOEnterpriseObject> objects =
    		localContext().objectsWithFetchSpecification(fetchSpec);

    	return objects;
    }


    // ----------------------------------------------------------
    public String displayStringForEntityValue()
    {
    	return entityValueInList.toString();
    }


    // ----------------------------------------------------------
    public EOEnterpriseObject entityValueOfRepresentedValue()
    {
    	if (representedValue instanceof EOEnterpriseObject)
        {
    		return (EOEnterpriseObject)representedValue;
        }
    	else
        {
    		return null;
        }
    }


    // ----------------------------------------------------------
    public void setEntityValueOfRepresentedValue(EOEnterpriseObject value)
    {
    	representedValue = value;
    }


    // ----------------------------------------------------------
    public NSArray<EOEnterpriseObject> entityValuesOfRepresentedValueArray()
    {
    	NSMutableArray<EOEnterpriseObject> values =
    		new NSMutableArray<EOEnterpriseObject>();

        @SuppressWarnings("unchecked")
    	NSArray<Object> array = (NSArray<Object>)representedValue;

    	if (array != null)
    	{
	    	for (Object object : array)
	    	{
	    		if (object instanceof EOEnterpriseObject
                    && valueType.isAssignableFrom(object.getClass()))
	    		{
	    			values.addObject(object);
	    		}
	    	}
    	}

    	return values;
    }


    // ----------------------------------------------------------
    public void setEntityValuesOfRepresentedValueArray(
        NSArray<EOEnterpriseObject> values)
    {
    	representedValue = values;
    }


    //~ Instance/static variables .............................................

    private static NSMutableArray<Boolean> BOOLEAN_VALUES;

    static
    {
        BOOLEAN_VALUES = new NSMutableArray<Boolean>();
        BOOLEAN_VALUES.addObject(true);
        BOOLEAN_VALUES.addObject(false);
    }
}
