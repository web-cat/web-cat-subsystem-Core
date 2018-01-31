/*==========================================================================*\
 |  $Id: AdvancedQueryAssistant.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
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

import org.json.JSONException;
import org.json.JSONObject;
import org.webcat.core.KeyPathParser;
import org.webcat.core.WCComponent;
import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.ComponentIDGenerator;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * Provides an interface for incrementally building an advanced query
 * by adding/combining key/value qualifiers of various kinds.
 *
 * @author aallowat
 * @version $Id: AdvancedQueryAssistant.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
 */
public class AdvancedQueryAssistant
    extends WCComponent
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     * @param context The page's context
     */
    public AdvancedQueryAssistant(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public String                 objectType;
    public AdvancedQueryModel     model;

    /* Repetition variables */
    public AdvancedQueryCriterion criterion;
    public int                    comparandType;
    public Class<?>               castType;
    public int                    index;

    public ComponentIDGenerator   idFor;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        idFor = new ComponentIDGenerator(this);

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
/*        for(String key : request.formValues().keySet())
        {
            System.out.println(key + " = " + request.formValues().objectForKey(key));
        }
        System.out.println("-----------");*/

        super.takeValuesFromRequest(request, context);
    }


    // ----------------------------------------------------------
    public String contentAssistActionURL()
    {
        NSMutableDictionary<String, Object> queryParameters =
            new NSMutableDictionary<String, Object>();

        queryParameters.setObjectForKey(Integer.toString(256 * 256 + 256),
                "designerVersion");

        return context().directActionURLForActionNamed(
                "contentAssist/entityDescriptions", queryParameters);
    }


    // ----------------------------------------------------------
    public JSONObject contentAssistDataStoreQuery()
    {
        JSONObject query = new JSONObject();

        try
        {
            query.put("rootType", objectType);
        }
        catch (JSONException e)
        {
            // Do nothing.
        }

        return query;
    }


    // ----------------------------------------------------------
    public String currentKeyPath()
    {
        String keypath = criterion.keyPath();

        if (keypath == null)
        {
            return "";
        }
        else
        {
            return keypath;
        }
    }


    // ----------------------------------------------------------
    public void immediatelySetKeyPathAtIndex(String value, int index)
    {
        AdvancedQueryCriterion criterion =
            model.criteria().objectAtIndex(index);

        criterion.setKeyPath(value);

        if (criterion.comparison() == null)
        {
            criterion.setComparison(
                comparisonsForCurrentKeyPath().objectAtIndex(0));
        }
    }


    // ----------------------------------------------------------
    public void setCurrentKeyPath(String value)
    {
        immediatelySetKeyPathAtIndex(value, index);
    }


    // ----------------------------------------------------------
    public boolean isCurrentKeyPathValid()
    {
        String keypath = currentKeyPath();

        KeyPathParser kpp = new KeyPathParser(objectType, keypath);

        return (kpp.theClass() != null);
    }


    // ----------------------------------------------------------
    public boolean doesCurrentKeyPathNeedCast()
    {
        KeyPathParser kpp = new KeyPathParser(objectType, currentKeyPath());

        return (kpp.theClass() == Object.class);
    }


    // ----------------------------------------------------------
    public NSArray<Class<?>> castTypes()
    {
        return new NSArray<Class<?>>(new Class<?>[] {
            String.class,
            Integer.class,
            Double.class,
            Boolean.class,
            NSTimestamp.class
        });
    }


    // ----------------------------------------------------------
    public Class<?> currentCastType()
    {
        if (criterion.castType() == null)
        {
            return String.class;
        }
        else
        {
            return criterion.castType();
        }
    }


    // ----------------------------------------------------------
    public void immediatelySetCastTypeAtIndex(int castTypeIndex, int index)
    {
        AdvancedQueryCriterion criterion =
            model.criteria().objectAtIndex(index);

        criterion.setCastType(castTypes().objectAtIndex(castTypeIndex));
    }


    // ----------------------------------------------------------
    public void setCurrentCastType(Class<?> value)
    {
        if (value == null)
        {
            criterion.setCastType(String.class);
        }
        else
        {
            criterion.setCastType(value);
        }
    }


    // ----------------------------------------------------------
    public String displayStringForCastType()
    {
        Class<?> type = castType;

        if (type == String.class)
        {
            return "string";
        }
        else if (type == Integer.class)
        {
            return "integer";
        }
        else if (type == Double.class)
        {
            return "float";
        }
        else if (type == Boolean.class)
        {
            return "boolean";
        }
        else if (type == NSTimestamp.class)
        {
            return "timestamp";
        }
        else
        {
            return "";
        }
    }


    // ----------------------------------------------------------
    public NSArray<AdvancedQueryComparison> comparisonsForCurrentKeyPath()
    {
        String keypath = currentKeyPath();
        return comparisonsForKeyPath(keypath, criterion.castType());
    }


    // ----------------------------------------------------------
    public NSArray<AdvancedQueryComparison> comparisonsForKeyPath(
            String keypath, Class<?> castType)
    {
        KeyPathParser kpp = new KeyPathParser(objectType, keypath);

        Class<?> klass = kpp.theClass();

        if (klass == Object.class)
        {
            return AdvancedQueryComparison.comparisonsForType(castType);
        }
        else
        {
            return AdvancedQueryComparison.comparisonsForType(klass);
        }
    }


    // ----------------------------------------------------------
    public NSArray<Integer> validComparandTypesForCurrentComparison()
    {
        NSMutableArray<Integer> comparands = new NSMutableArray<Integer>();
        comparands.addObject(AdvancedQueryCriterion.COMPARAND_LITERAL);

        AdvancedQueryComparison comparison = criterion.comparison();

        if (comparison != null && comparison.doesSupportKeyPaths())
        {
            comparands.addObject(AdvancedQueryCriterion.COMPARAND_KEYPATH);
        }

        return comparands;
    }


    // ----------------------------------------------------------
    public AdvancedQueryComparison currentComparison()
    {
        return criterion.comparison();
    }


    // ----------------------------------------------------------
    public void immediatelySetComparisonAtIndex(int comparisonIndex,
            int castTypeIndex, int index)
    {
        AdvancedQueryCriterion criterion =
            model.criteria().objectAtIndex(index);

        Class<?> castType = castTypes().objectAtIndex(castTypeIndex);

        AdvancedQueryComparison comparison =
            comparisonsForKeyPath(criterion.keyPath(), castType).objectAtIndex(
                    comparisonIndex);

        setComparisonForCriterion(comparison, criterion);
    }


    // ----------------------------------------------------------
    public void setCurrentComparison(AdvancedQueryComparison comparison)
    {
        setComparisonForCriterion(comparison, criterion);
    }


    // ----------------------------------------------------------
    public void setComparisonForCriterion(AdvancedQueryComparison comparison,
            AdvancedQueryCriterion criterion)
    {
        /*
         * If the keypath operand support for either the old or the new
         * comparison differ, we reset the comparand back to LITERAL so that
         * it doesn't cause problems later.
         */

        if (comparison == null)
        {
            comparison = comparisonsForCurrentKeyPath().objectAtIndex(0);
        }

        if (criterion.comparison() == null
            || criterion.comparison().doesSupportKeyPaths() !=
                   comparison.doesSupportKeyPaths())
        {
            criterion.setComparandType(
                AdvancedQueryCriterion.COMPARAND_LITERAL);
            criterion.setValue(null);
        }

        criterion.setComparison(comparison);
    }


    // ----------------------------------------------------------
    public boolean doesCurrentComparisonHaveSecondOperand()
    {
        return currentComparison().hasSecondOperand();
    }


    // ----------------------------------------------------------
    public Integer currentComparandType()
    {
        return criterion.comparandType();
    }


    // ----------------------------------------------------------
    public void immediatelySetComparandTypeAtIndex(int comparandType, int index)
    {
        AdvancedQueryCriterion criterion =
            model.criteria().objectAtIndex(index);

        setComparandTypeForCriterion(comparandType, criterion);
    }


    // ----------------------------------------------------------
    public void setCurrentComparandType(Integer type)
    {
        setComparandTypeForCriterion(type, criterion);
    }


    // ----------------------------------------------------------
    public void setComparandTypeForCriterion(Integer type,
            AdvancedQueryCriterion criterion)
    {
        if (type == null)
        {
            criterion.setComparandType(
                AdvancedQueryCriterion.COMPARAND_LITERAL);
        }
        else
        {
            criterion.setComparandType(type);
        }
    }


    // ----------------------------------------------------------
    public boolean doesCurrentCriterionUseComparand()
    {
        return currentComparison().doesSupportKeyPaths();
    }


    // ----------------------------------------------------------
    public String displayStringForComparandType()
    {
        if (comparandType == AdvancedQueryCriterion.COMPARAND_LITERAL)
        {
            if (doesCurrentComparisonSupportMultipleValues())
            {
                return "values";
            }
            else
            {
                return "value";
            }
        }
        else
        {
            return "key path";
        }
    }


    // ----------------------------------------------------------
    public boolean doesCurrentComparisonSupportMultipleValues()
    {
        return currentComparison().doesSupportMultipleValues();
    }


    // ----------------------------------------------------------
    public Class<?> typeOfCurrentKeyPath()
    {
        String keypath = currentKeyPath();

        KeyPathParser kpp = new KeyPathParser(objectType, keypath);

        Class<?> klass = kpp.theClass();

        if (klass != null && klass != Object.class)
        {
            return klass;
        }
        else
        {
            return currentCastType();
        }
    }


    // ----------------------------------------------------------
    public boolean isCurrentCriterionOperandSimple()
    {
        return (currentComparandType() !=
                    AdvancedQueryCriterion.COMPARAND_KEYPATH)
            && (currentComparison() != AdvancedQueryComparison.IS_BETWEEN)
            && (currentComparison() != AdvancedQueryComparison.IS_NOT_BETWEEN);
    }


    // ----------------------------------------------------------
    public boolean isCurrentCriterionOperandKeyPath()
    {
        return currentComparandType() ==
            AdvancedQueryCriterion.COMPARAND_KEYPATH;
    }


    // ----------------------------------------------------------
    public boolean isCurrentCriterionOperandBetween()
    {
        return (currentComparison() == AdvancedQueryComparison.IS_BETWEEN)
            || (currentComparison() == AdvancedQueryComparison.IS_NOT_BETWEEN);
    }


    // ----------------------------------------------------------
    public Object currentRepresentedValue()
    {
        return criterion.value();
    }


    // ----------------------------------------------------------
    public void setCurrentRepresentedValue(Object value)
    {
        criterion.setValue(value);
    }


    // ----------------------------------------------------------
    public Object minimumValueOfCurrentRepresentedValue()
    {
        if (criterion.value() instanceof NSDictionary)
        {
            return ((NSDictionary<String, Object>)criterion.value())
                .objectForKey("minimumValue");
        }
        else
        {
            return null;
        }
    }

    public void setMinimumValueOfCurrentRepresentedValue(Object value)
    {
        if (value == null)
        {
            return;
        }

        if (criterion.value() instanceof NSMutableDictionary)
        {
            ((NSMutableDictionary<String, Object>)criterion.value())
                .setObjectForKey(value, "minimumValue");
        }
        else
        {
            NSMutableDictionary<String, Object> dict =
                new NSMutableDictionary<String, Object>();
            dict.setObjectForKey(value, "minimumValue");
            criterion.setValue(dict);
        }
    }


    // ----------------------------------------------------------
    public Object maximumValueOfCurrentRepresentedValue()
    {
        if (criterion.value() instanceof NSDictionary)
        {
            return ((NSDictionary<String, Object>)criterion.value())
                .objectForKey("maximumValue");
        }
        else
        {
            return null;
        }
    }


    // ----------------------------------------------------------
    public void setMaximumValueOfCurrentRepresentedValue(Object value)
    {
        if (value == null)
        {
            return;
        }

        if (criterion.value() instanceof NSMutableDictionary)
        {
            ((NSMutableDictionary<String, Object>)criterion.value())
                .setObjectForKey(value, "maximumValue");
        }
        else
        {
            NSMutableDictionary<String, Object> dict =
                new NSMutableDictionary<String, Object>();
            dict.setObjectForKey(value, "maximumValue");
            criterion.setValue(dict);
        }
    }


    // ----------------------------------------------------------
    public JavascriptGenerator addCriterion()
    {
        model.insertNewCriterionAtIndex(index + 1);

        return new JavascriptGenerator()
            .refresh((String) idFor.valueForKey("criteriaContainer"));
    }


    // ----------------------------------------------------------
    public JavascriptGenerator removeCriterion()
    {
        model.removeCriterionAtIndex(index);

        return new JavascriptGenerator()
            .refresh((String) idFor.valueForKey("criteriaContainer"));
    }
}
