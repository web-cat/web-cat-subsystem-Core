/*==========================================================================*\
 |  $Id$
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

package net.sf.webcat.ui;

import java.util.Iterator;
import net.sf.webcat.ui.util.WCTableFilterBuilder;
import net.sf.webcat.ui.util.WCTableLayoutBuilder;
import ognl.webobjects.WOOgnl;
import org.jfree.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import er.extensions.appserver.ERXWOContext;
import er.extensions.foundation.ERXStringUtilities;

//------------------------------------------------------------------------
/**
 * <p>
 * The ObjectTable component supports displaying complex data and objects in
 * a tabular format. The data source is a WODisplayGroup that contains the
 * array of objects to be displayed. These objects can be anything that supports
 * key-value coding, such as enterprise objects or dictionaries.
 * </p><p>
 * The column layout is provided either in markup (by nesting
 * {@link WCTableColumn} elements) or by explicitly setting the
 * <i>columns</i> binding; this should be an array of dictionaries constructed
 * directly in code, loaded from a plist file, or obtained from the
 * {@link WCTableLayoutBuilder} utility class.
 * </p><p> 
 * Each entry in the columns array is a dictionary with the following keys:
 * </p><p>
 * <ul>
 * <li><b>name:</b> a String, the title of the column to be displayed in the
 * table header</li>
 * <li><b>componentName:</b> a String, the name of the component that will be
 * used to render the cell's content</li>
 * <li><b>keyPath:</b> the key path, rooted from each object in the display
 * group, that specifies the value for the column. If omitted, the object
 * itself is passed in as the value (to permit complex rendering that may
 * involve multiple properties of the object)</li>
 * <li><b>sortingKeyPath:</b> the key path used to access the value that will
 * be used when sorting this column, if different from the keyPath above</li>
 * <li><b>properties:</b> cell-specific properties that are passed along to
 * the cell component</li>
 * </p>
 * 
 * @binding id the DOM identifier of the table, also used as a prefix for
 *     other elements created by the component. If not provided, one is
 *     auto-generated
 * @binding canSort true if the rows in the table can be sorted by clicking
 *     the column title (defaults to false)
 * @binding displayGroup a WODisplayGroup that contains the data to be
 *     displayed in the table and also manages the current selection
 * @binding columns an array of dictionaries that describe the columns in
 *     the table; this can be loaded from a plist file or generated in code
 *     with the {@link WCTableLayoutBuilder} class
 * @binding isBatched true if the table displays a fixed-size batch of
 *     objects and provides controls for changing the batch size and
 *     navigating the pages of the table (defaults to false)
 * @binding batchSize the number of items to display on each page of the
 *     table; this value overrides the batch size of the associated
 *     display group (defaults to 10)
 * @binding showsRowNumbers true if the table should display an extra
 *     column at the far left that shows the row number of each item
 *     in the table (defaults to false)
 * @binding allowsSelection true if the user should be able to select
 *     rows in the table; the current selection is reflected by the
 *     display group (defaults to false)
 * @binding multipleSelection true if the user should be able to select
 *     multiple items from the table; false if only a single item can be
 *     selected at a time (defaults to false)     
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class WCTable extends WOComponent
{
    //~ Constructors ..........................................................
    
    // ----------------------------------------------------------
    /**
     * Initializes a new ObjectTable.
     * 
     * @param context
     */
    public WCTable(WOContext context)
    {
        super(context);
    }
    
    
    //~ KVC attributes (must be public) .......................................

    public String id;
    public boolean canSort = false;
    public WODisplayGroup displayGroup;
    public NSArray<NSDictionary<String, Object>> columns;
    public NSArray<NSDictionary<String, Object>> filters;
    public boolean isBatched = false;
    public int batchSize = 0;
    public boolean showsRowNumbers = false;
    public boolean allowsSelection = false;
    public boolean multipleSelection = false;
    
    public Object objectInRepetition;
    public int indexInRepetition;
    public NSDictionary<String, Object> columnInRepetition;
    public int columnIndexInRepetition;    
    public NSDictionary<String, Object> filterInRepetition;
    public int filterIndexInRepetition;

    private NSMutableDictionary<String, JSONObject> currentFilters;
    private int sortedColumnIndex = -1;
    private boolean sortedColumnAscending;
    

    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (currentFilters == null)
        {
            currentFilters = new NSMutableDictionary<String, JSONObject>();
        }

        if (id == null)
        {
            id = ERXStringUtilities.safeIdentifierName(context.elementID());
        }
        
        if (!isBatched)
        {
        	batchSize = -1;
        }

        if (batchSize == 0)
        {
        	batchSize = 10;
        }

        displayGroup.setNumberOfObjectsPerBatch(
        		(batchSize == -1) ? displayGroup.allObjects().count() :
        			batchSize);

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    /**
     * Gets a page-unique identifier used as a prefix to generate DOM element
     * identifiers for the controls and nodes used in the table.
     * 
     * @returns the DOM identifier prefix
     */
    public String id()
    {
        return id;
    }

    
    // ----------------------------------------------------------
    /**
     * Gets the internal JavaScript identifier of the JSON bridge that will be
     * used to communicate with the server to handle user interaction events
     * and model data requests.
     */
    public String JSONBridgeName()
    {
        return "__JSONBridge_" + id();
    }


    // ----------------------------------------------------------
    /**
     * Gets the internal JavaScript identifier that will refer to the actual
     * component inside an AjaxProxy.
     */
    public String componentProxyName()
    {
        return "table";
    }


    // ----------------------------------------------------------
    /**
     * Gets the full JavaScript reference to the proxy object that is used to
     * make RPC calls to the server-side component.
     */
    public String proxyReference()
    {
        return JSONBridgeName() + "." + componentProxyName();
    }
    

    // ----------------------------------------------------------
    /**
     * Gets the DOM identifier for the Ajax update container that holds the
     * table.
     */
    public String updateContainerID()
    {
        return id() + "_updateContainer";
    }
    
    
    // ----------------------------------------------------------
    /**
     * Gets the DOM identifier for the checkbox in the table header that
     * allows for the quick selection/deselection of every object in the
     * current batch.
     */
    public String allSelectionCheckboxID()
    {
        return id() + "_allSelectionState";
    }

    
    // ----------------------------------------------------------
    /**
     * Gets the form name of the checkboxes or radio buttons used to
     * allow for the selection/deselection of an object in the table.
     */
    public String selectionCheckboxName()
    {
        return id() + "_selectionState";
    }
    

    // ----------------------------------------------------------
    /**
     * Gets the JavaScript identifier of the webcat.ObjectTable wrapper
     * object that is used to communicate from JavaScript to this component.
     */
    public String jsWrapperID()
    {
    	return id() + "_wrapper";
    }
    
    
    // ----------------------------------------------------------
    public String idForFilterDialog()
    {
        return id() + "_filterDialog";
    }
    
    
    // ----------------------------------------------------------
    /**
     * Called from within a columns repetition nested in a rows repetition,
     * this method gets the value for the current row and column that will be
     * passed to the cell component.
     */
    public Object columnValueOfObjectInRepetition()
    {
        String keyPath = (String) columnInRepetition.objectForKey("keyPath");
        Object value;
        
        if (keyPath == null)
        {
            value = objectInRepetition;
        }
        else
        {
            if (keyPath.startsWith("~"))
            {
                String expression = keyPath.substring(1);
                value = WOOgnl.factory().getValue(expression,
                        objectInRepetition);
            }
            else
            {
                value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(
                        objectInRepetition, keyPath);
            }
        }
        
        return value;
    }
    

    // ----------------------------------------------------------
    /**
     * 
     */
    public boolean columnInRepetitionCanBeSorted()
    {
        String keyPath = (String) columnInRepetition.objectForKey("keyPath");
        String sortingKeyPath = (String) columnInRepetition.objectForKey(
                "sortingKeyPath");
        
        return (sortingKeyPath != null || !keyPath.startsWith("~"));
    }


    // ----------------------------------------------------------
    public String cssClassOfRowInRepetition()
    {
        return (indexInRepetition % 2 == 0) ? "e" : "o";
    }
    
    
    // ----------------------------------------------------------
    public boolean isRowInRepetitionSelected()
    {
        int index = indexInRepetition +
            displayGroup.indexOfFirstDisplayedObject() - 1;
        
        return displayGroup.selectionIndexes().containsObject(index);
    }
    
    
    // ----------------------------------------------------------
    public boolean isEntireBatchSelected()
    {
        for (int i = displayGroup.indexOfFirstDisplayedObject() - 1;
            i <= displayGroup.indexOfLastDisplayedObject() - 1;
            i++)
        {
            if (!displayGroup.selectionIndexes().containsObject(i))
            {
                return false;
            }
        }
        
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Gets the actual row number of the current row, displayed when the
     * showsRowNumbers binding is set to true.
     */
    public int actualRowNumber()
    {
    	return indexInRepetition + displayGroup.indexOfFirstDisplayedObject();
    }
    

    // ----------------------------------------------------------
    /**
     * Selects or deselects the object at the specified index in the current
     * batch in the display group.
     * 
     * @param index the object of the item in the current batch
     * @param state true to select the item, false to deselect it
     */
    @SuppressWarnings("unchecked")
    public synchronized void selectObjectAtIndexInBatch(
            int index, boolean state)
    {
        index += displayGroup.indexOfFirstDisplayedObject() - 1;

        if (multipleSelection)
        {
	        NSMutableArray<Integer> selection =
	            new NSMutableArray<Integer>(displayGroup.selectionIndexes());
	        
	        if (state)
	        {
	            if (!selection.containsObject(index))
	            {
	                selection.addObject(index);
	            }
	        }
	        else
	        {
	            selection.removeObject(index);
	        }

	        displayGroup.setSelectionIndexes(selection);
        }
        else
        {
        	if (state)
        	{
        		displayGroup.setSelectionIndexes(
            		new NSArray<Integer>(index));
        	}
        	else
        	{
        		displayGroup.clearSelection();
        	}
        }
    }
    
    
    // ----------------------------------------------------------
    /**
     * Selects or deselects all the items in the current batch.
     * 
     * @param state true to select the items, false to deselect them
     */
    public synchronized void selectAllObjectsInBatch(boolean state)
    {
        if (state)
        {
            NSMutableArray<Integer> selection = new NSMutableArray<Integer>();
            
            for (int i = displayGroup.indexOfFirstDisplayedObject() - 1;
                i <= displayGroup.indexOfLastDisplayedObject() - 1;
                i++)
            {
                selection.addObject(i);
            }
            
            displayGroup.setSelectionIndexes(selection);
        }
        else
        {
            // We never allow objects to be selected that aren't in the
            // current batch anyway, so clearing the entire selection is the
            // easiest way to deal with this case.
            
            displayGroup.clearSelection();
        }
    }

    
    // ----------------------------------------------------------
    public synchronized void changeSortOrdering(int index)
    {
        if (index == sortedColumnIndex)
        {
            sortedColumnAscending = !sortedColumnAscending;
        }
        else
        {
            sortedColumnIndex = index;
            sortedColumnAscending = true;
        }

        NSDictionary<String, Object> column = columns.objectAtIndex(index);

        String sortingKeyPath = (String) column.objectForKey("sortingKeyPath");
        if (sortingKeyPath == null)
            sortingKeyPath = (String) column.objectForKey("keyPath");

        NSSelector<?> selector = sortedColumnAscending ?
                EOSortOrdering.CompareCaseInsensitiveAscending :
                EOSortOrdering.CompareCaseInsensitiveDescending;
        
        EOSortOrdering so = new EOSortOrdering(sortingKeyPath, selector);

        displayGroup.setSortOrderings(new NSArray<EOSortOrdering>(so));
        displayGroup.clearSelection();
        displayGroup.updateDisplayedObjects();
        displayGroup.setCurrentBatchIndex(1);
    }
    
    
    // ----------------------------------------------------------
    public synchronized void changeBatchSize(int size)
    {
    	batchSize = size;
    	
        displayGroup.setNumberOfObjectsPerBatch(
        		(batchSize == -1) ? displayGroup.allObjects().count() :
        			batchSize);
    }
    
    
    // ----------------------------------------------------------
    public synchronized void changeFilter(String keyPath, JSONObject changes)
    {
        JSONObject filter = currentFilters.objectForKey(keyPath);
        
        if (filter == null)
        {
            filter = new JSONObject();
            currentFilters.setObjectForKey(filter, keyPath);
        }
        
        Iterator<String> it = changes.keys();
        while (it.hasNext())
        {
            String changeKey = it.next();
            
            try
            {
                Object value = changes.get(changeKey);
                filter.put(changeKey, value);
            }
            catch (JSONException e)
            {
                // Do nothing.
            }
        }

        updateDisplayGroupQualifier();
    }

    
    private void updateDisplayGroupQualifier()
    {
        NSMutableArray<EOQualifier> quals = new NSMutableArray<EOQualifier>();
        
        for (String keyPath : currentFilters.allKeys())
        {
            JSONObject filterArgs = currentFilters.objectForKey(keyPath);
            
            try
            {
                if (filterArgs.getBoolean("enabled"))
                {
                    String relation = filterArgs.getString("relation");
                    Object value = filterArgs.get("value");

                    String format = keyPath + " %s %%@";
                    String qualString = null;
                    
                    if ("is".equals(relation))
                    {
                            qualString = String.format(format, "=");
                    }
                    else if ("is not".equals(relation))
                    {
                            qualString = String.format(format, "<>");
                    }
                    else if ("starts with".equals(relation))
                    {
                            value = value.toString() + "*"; 
                            qualString = String.format(format,
                                    "caseInsensitiveLike");
                    }
                    else if ("ends with".equals(relation))
                    {
                            value = "*" + value.toString(); 
                            qualString = String.format(format,
                                    "caseInsensitiveLike");
                    }
                    else if ("contains".equals(relation))
                    {
                            value = "*" + value.toString() + "*"; 
                            qualString = String.format(format,
                                    "caseInsensitiveLike");
                    }
                    
                    quals.add(EOQualifier.qualifierWithQualifierFormat(
                            qualString, new NSArray<Object>(value)));
                }
            }
            catch (JSONException e)
            {
                // Do nothing.
            }
        }

        if (quals.isEmpty())
        {
            displayGroup.setQualifier(null);
        }
        else
        {
            displayGroup.setQualifier(new EOAndQualifier(quals));
        }
        
        displayGroup.updateDisplayedObjects();
    }
    
    
    public boolean currentFilterIsEnabled()
    {
        String keyPath = (String) filterInRepetition.objectForKey("keyPath");
        
        JSONObject filter = currentFilters.objectForKey(keyPath);

        if (filter != null)
        {
            try
            {
                return filter.getBoolean("enabled");
            }
            catch (JSONException e)
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
    
    public String currentFilterComparison()
    {
        String keyPath = (String) filterInRepetition.objectForKey("keyPath");
        
        JSONObject filter = currentFilters.objectForKey(keyPath);

        if (filter != null)
        {
            try
            {
                return filter.getString("relation");
            }
            catch (JSONException e)
            {
                return "is";
            }
        }
        else
        {
            return "is";
        }
    }

    
    public void setCurrentFilterIsEnabled(boolean value)
    {
        String keyPath = (String) filterInRepetition.objectForKey("keyPath");
        
        JSONObject changes = new JSONObject();
        try
        {
            changes.put("enabled", value);
        }
        catch (JSONException e)
        {
            // Do nothing.
        }
        
        changeFilter(keyPath, changes);
    }
    
    
    public Object currentFilterValue()
    {
        String keyPath = (String) filterInRepetition.objectForKey("keyPath");
        
        JSONObject filter = currentFilters.objectForKey(keyPath);

        if (filter != null)
        {
            try
            {
                return filter.get("value");
            }
            catch (JSONException e)
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    
    
    public void setCurrentFilterValue(Object value)
    {
        String keyPath = (String) filterInRepetition.objectForKey("keyPath");
        
        JSONObject changes = new JSONObject();
        try
        {
            changes.put("value", value);
        }
        catch (JSONException e)
        {
            // Do nothing.
        }
        
        changeFilter(keyPath, changes);
    }
    

    public String idForCurrentFilterEnabledCheckbox()
    {
        return id() + "_filter_" + filterIndexInRepetition + "_enabled";
    }


    public String idForCurrentFilterComparisonButton()
    {
        return id() + "_filter_" + filterIndexInRepetition + "_comparison";
    }


    public String idForCurrentFilterValueWidget()
    {
        return id() + "_filter_" + filterIndexInRepetition + "_value";
    }


    // ----------------------------------------------------------
    public boolean columnInRepetitionIsSorted()
    {
        return (columnIndexInRepetition == sortedColumnIndex);
    }


    // ----------------------------------------------------------
    public boolean columnInRepetitionIsAscending()
    {
        return sortedColumnAscending;
    }

    
    // ----------------------------------------------------------
    /**
     * @return
     */
    public synchronized WOActionResults goToFirstBatch()
    {
        displayGroup.clearSelection();
        displayGroup.setCurrentBatchIndex(1);
        return null;
    }

    
    // ----------------------------------------------------------
    /**
     * @return
     */
    public synchronized WOActionResults goToPreviousBatch()
    {
        displayGroup.clearSelection();
        displayGroup.displayPreviousBatch();
        return null;
    }


    // ----------------------------------------------------------
    /**
     * @return
     */
    public synchronized WOActionResults goToNextBatch()
    {
        displayGroup.clearSelection();
        displayGroup.displayNextBatch();
        return null;
    }
    
    
    // ----------------------------------------------------------
    /**
     * @return
     */
    public synchronized WOActionResults goToLastBatch()
    {
        displayGroup.clearSelection();
        displayGroup.setCurrentBatchIndex(displayGroup.batchCount());
        return null;
    }

    
    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public synchronized void performActionOnObjectAtIndexInBatch(
            int index, int columnIndex)
    {
        displayGroup.clearSelection();
        displayGroup.setSelectionIndexes(new NSArray<Integer>(
                index + displayGroup.indexOfFirstDisplayedObject() - 1));
        
        NSDictionary<String, Object> column =
            columns.objectAtIndex(columnIndex);

        NSDictionary<String, String> properties =
            (NSDictionary<String, String>) column.objectForKey("properties");

        String actionName = properties.objectForKey("action"); 
        performParentAction(actionName);
    }


    // ----------------------------------------------------------
    public synchronized void selectOnlyObjectAtIndexInBatch(int index)
    {
        displayGroup.clearSelection();
        displayGroup.setSelectionIndexes(new NSArray<Integer>(
                index + displayGroup.indexOfFirstDisplayedObject() - 1));
    }
    
    
    @Override
    public void awake()
    {
        super.awake();

        if (currentTable() != null)
        {
            Log.error("WCTable elements cannot be nested! The layout of "
                    + "your table will not work as expected.");
        }

        // Clear out the previous state when the table component is awoken
        // (this is important if we have multiple tables on the same page).
        ERXWOContext.contextDictionary().removeObjectForKey(
                CURRENT_DATA_TABLE_LAYOUT_KEY);
        ERXWOContext.contextDictionary().removeObjectForKey(
                CURRENT_DATA_TABLE_FILTERS_KEY);

        setCurrentTable(this);
    }
    

    @Override
    public void sleep()
    {
        setCurrentTable(null);

        super.sleep();
    }


    public static WCTable currentTable()
    {
        return (WCTable) ERXWOContext.contextDictionary().objectForKey(
                CURRENT_DATA_TABLE_KEY);
    }
    
    
    public static void setCurrentTable(WCTable table)
    {
        if (table == null)
        {
            ERXWOContext.contextDictionary().removeObjectForKey(
                    CURRENT_DATA_TABLE_KEY);
        }
        else
        {
            ERXWOContext.contextDictionary().setObjectForKey(table,
                    CURRENT_DATA_TABLE_KEY);
        }
    }


    public static WCTableLayoutBuilder currentTableLayout()
    {
        WCTable table = currentTable();
        
        if (table == null)
        {
            return null;
        }
        else
        {
            WCTableLayoutBuilder layout =
                (WCTableLayoutBuilder) ERXWOContext.contextDictionary().objectForKey(
                    CURRENT_DATA_TABLE_LAYOUT_KEY);
            
            if (layout == null)
            {
                layout = new WCTableLayoutBuilder();
                ERXWOContext.contextDictionary().setObjectForKey(layout,
                        CURRENT_DATA_TABLE_LAYOUT_KEY);
            }

            return layout;
        }
    }
    
    
    public static void commitTableLayoutChanges()
    {
        WCTable table = currentTable();
        
        if (table != null)
        {
            WCTableLayoutBuilder layout =
                (WCTableLayoutBuilder) ERXWOContext.contextDictionary().objectForKey(
                    CURRENT_DATA_TABLE_LAYOUT_KEY);
            
            if (layout != null)
            {
                table.columns = layout.asArray();
            }
        }
    }
    
    
    public static WCTableFilterBuilder currentFilters()
    {
        WCTable table = currentTable();
        
        if (table == null)
        {
            return null;
        }
        else
        {
            WCTableFilterBuilder filters =
                (WCTableFilterBuilder) ERXWOContext.contextDictionary().objectForKey(
                    CURRENT_DATA_TABLE_FILTERS_KEY);
            
            if (filters == null)
            {
                filters = new WCTableFilterBuilder();
                ERXWOContext.contextDictionary().setObjectForKey(filters,
                        CURRENT_DATA_TABLE_FILTERS_KEY);
            }

            return filters;
        }
    }
    
    
    public static void commitFilterChanges()
    {
        WCTable table = currentTable();
        
        if (table != null)
        {
            WCTableFilterBuilder filters =
                (WCTableFilterBuilder) ERXWOContext.contextDictionary().objectForKey(
                    CURRENT_DATA_TABLE_FILTERS_KEY);
            
            if (filters != null)
            {
                table.filters = filters.asArray();
            }
        }
    }
    
    
    //~ Static/instance variables .............................................
    
    private static final String CURRENT_DATA_TABLE_KEY =
        "net.sf.webcat.ui.table.WCDataTable.currentTable";
    private static final String CURRENT_DATA_TABLE_LAYOUT_KEY =
        "net.sf.webcat.ui.table.WCDataTable.currentTableLayout";
    private static final String CURRENT_DATA_TABLE_FILTERS_KEY =
        "net.sf.webcat.ui.table.WCDataTable.currentTableFilters";
}
