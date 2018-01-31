/*==========================================================================*\
 |  $Id: WCTable.java,v 1.6 2011/10/25 12:58:38 stedwar2 Exp $
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

package org.webcat.ui;

import org.webcat.core.WCComponent;
import org.webcat.ui.generators.JavascriptFunction;
import org.webcat.ui.generators.JavascriptGenerator;
import org.webcat.ui.util.ComponentIDGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.appserver.ERXWOContext;
import er.extensions.eof.ERXQ;
import er.extensions.eof.ERXS;
import er.extensions.eof.ERXSortOrdering.ERXSortOrderings;

//-------------------------------------------------------------------------
/**
 * <p>
 * A reusable table component that is bound to a display group, and provides
 * options to select rows, sort on table headings, and paging.
 * </p>
 * <dl>
 * <dt>id (<code>String</code>)</dt>
 * <dd>The widget id for the content pane that will surround the table. This
 * can be used to refresh the table contents in response to events that occur
 * outside the table.</dd>
 * <dt>displayGroup (<code>ERXDisplayGroup&lt;?&gt;</code>)</dt>
 * <dd>The display group that contains the objects that this table will
 * display.</dd>
 * <dt>fixedPageSize (<code>boolean</code>)</dt>
 * <dd>If true, the user will not be able to change the page size. This can be
 * desirable if the table is inside a dialog box and should remain small on the
 * screen.</dd>
 * <dt>settingsKey (<code>String</code>)</dt>
 * <dd>A key prefix that will be used to persist the table's settings in the
 * current user's preferences.</dd>
 * <dt>canSelectRows (<code>boolean</code>)</dt>
 * <dd>Indicates whether checkbox or radio button controls will be inserted at
 * the front of each table row to allow the user to select them.</dd>
 * <dt>multipleSelection (<code>boolean</code>)</dt>
 * <dd>Indicates whether multiple rows can be selected at once (using
 * checkboxes) or if only one row can be selected at a time (using radio
 * buttons).</dd>
 * </dl>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.6 $, $Date: 2011/10/25 12:58:38 $
 */
public class WCTable extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTable(WOContext context)
    {
        super(context);

        idFor = new ComponentIDGenerator(this);
    }


    //~ KVC attributes (must be public) .......................................

    public String id;

    public ERXDisplayGroup<?> displayGroup;
    public String settingsKey;
    public boolean canSelectRows = false;
    public boolean fixedPageSize = false;
    public boolean multipleSelection = false;
    public String searchOnKeyPaths;

    public ComponentIDGenerator idFor;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (id == null)
        {
            id = idFor.get();
        }

        setInitialSortOrdering();

        WCTable oldTable = setCurrentTable(this);

        super.appendToResponse(response, context);

        setCurrentTable(oldTable);
    }


    // ----------------------------------------------------------
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        WCTable oldTable = setCurrentTable(this);

        super.takeValuesFromRequest(request, context);

        setCurrentTable(oldTable);
    }


    // ----------------------------------------------------------
    @Override
    public WOActionResults invokeAction(WORequest request, WOContext context)
    {
        WCTable oldTable = setCurrentTable(this);

        WOActionResults result = super.invokeAction(request, context);

        setCurrentTable(oldTable);

        return result;
    }


    // ----------------------------------------------------------
    public void setSearchOnKeyPaths(String keyPaths)
    {
        searchOnKeyPaths = keyPaths;

        if (keyPaths == null)
        {
            searchOnKeyPathArray = null;
        }
        else
        {
            searchOnKeyPathArray = new NSMutableArray<String>();

            String[] keyPathArray = searchOnKeyPaths.split(",");
            for (String keyPath : keyPathArray)
            {
                searchOnKeyPathArray.addObject(keyPath);
            }
        }
    }


    // ----------------------------------------------------------
    private EOQualifier qualifierFromSearchString(String searchString)
    {
        if (searchOnKeyPathArray == null || searchString == null)
        {
            return null;
        }
        else
        {
            NSMutableArray<EOQualifier> quals =
                new NSMutableArray<EOQualifier>(searchOnKeyPathArray.count());

            for (String keyPath : searchOnKeyPathArray)
            {
                quals.addObject(ERXQ.contains(keyPath, searchString));
            }

            return new EOOrQualifier(quals);
        }
    }


    // ----------------------------------------------------------
    private void setInitialSortOrdering()
    {
        String keyPaths = persistentSortOrdering();
        Boolean ascending = persistentSortIsAscending();

        if (keyPaths != null)
        {
            sortDisplayGroup(keyPaths, ascending);
            needsInitialSort = false;
        }
        else
        {
            NSArray<EOSortOrdering> orderings = displayGroup.sortOrderings();
            needsInitialSort = (orderings == null || orderings.count() == 0);
        }
    }


    // ----------------------------------------------------------
    private String persistentSortOrdering()
    {
        if (settingsKey == null)
        {
            return null;
        }
        else
        {
            return (String) user().preferences().valueForKeyPath(
                    settingsKey + "_sortOrdering");
        }
    }


    // ----------------------------------------------------------
    private void setPersistentSortOrdering(String ordering, boolean ascending)
    {
        if (settingsKey != null)
        {
            user().preferences().takeValueForKey(
                    ordering, settingsKey + "_sortOrdering");
            user().preferences().takeValueForKey(
                    ascending, settingsKey + "_sortIsAscending");
            user().savePreferences();
        }
    }


    // ----------------------------------------------------------
    private Boolean persistentSortIsAscending()
    {
        if (settingsKey == null)
        {
            return null;
        }
        else
        {
            return (Boolean) user().preferences().valueForKeyPath(
                    settingsKey + "_sortIsAscending");
        }
    }


    // ----------------------------------------------------------
    public String sortOrderingKeyPathsFromDisplayGroup()
    {
        NSArray<EOSortOrdering> orderings = displayGroup.sortOrderings();

        StringBuffer keyPaths = new StringBuffer();

        if (orderings != null && orderings.count() > 0)
        {
            keyPaths.append(orderings.objectAtIndex(0).key());

            for (int i = 1; i < orderings.count(); i++)
            {
                keyPaths.append(",");
                keyPaths.append(orderings.objectAtIndex(1).key());
            }
        }

        return keyPaths.toString();
    }


    // ----------------------------------------------------------
    public boolean isDisplayGroupSortOrderingAscending()
    {
        NSArray<EOSortOrdering> orderings = displayGroup.sortOrderings();

        if (orderings != null && orderings.count() > 0)
        {
            NSSelector<?> selector = orderings.objectAtIndex(0).selector();

            return ERXS.INS_ASC.equals(selector) || ERXS.ASC.equals(selector);
        }
        else
        {
            return true;
        }
    }


    // ----------------------------------------------------------
    protected void sortDisplayGroup(String keyPaths, boolean ascending)
    {
        ERXSortOrderings orderings = new ERXSortOrderings();

        String[] keyPathArray = keyPaths.split(",");
        for (String keyPath : keyPathArray)
        {
            orderings.addObject(ERXS.sortOrder(keyPath.trim(),
                    ascending ? ERXS.INS_ASC : ERXS.INS_DESC));
        }

        displayGroup.clearSelection();
        displayGroup.setSortOrderings(orderings);
        displayGroup.updateDisplayedObjects();
    }


    // ----------------------------------------------------------
    public JavascriptGenerator sortUsingKeyPaths(String keyPaths,
                                                 boolean ascending)
    {
        setPersistentSortOrdering(keyPaths, ascending);
        sortDisplayGroup(keyPaths, ascending);
        return refreshTable();
    }


    // ----------------------------------------------------------
    public String searchText()
    {
        return searchText;
    }


    // ----------------------------------------------------------
    public JavascriptGenerator filterUsingSearchString(String searchString)
    {
        searchText = searchString;

        displayGroup.clearSelection();
        displayGroup.setCurrentBatchIndex(1);
        displayGroup.setQualifier(qualifierFromSearchString(searchString));
        displayGroup.updateDisplayedObjects();
        JavascriptGenerator js = refreshTable(new JavascriptFunction() {
            @Override
            public void generate(JavascriptGenerator g)
            {
                g.dijit(idFor.get("searchField")).call("focus");
            }
        });
        return js;
    }


    // ----------------------------------------------------------
    public static void refresh(JavascriptGenerator js, String id)
    {
        refresh(js, id, null);
    }


    // ----------------------------------------------------------
    public static void refresh(JavascriptGenerator js, String id,
            JavascriptFunction onAfterRefresh)
    {
        js.append(renderTableBusyScript(id));
        js.refresh(onAfterRefresh, id);
    }


    // ----------------------------------------------------------
    public JavascriptGenerator refreshTable()
    {
        return refreshTable(null);
    }


    // ----------------------------------------------------------
    public JavascriptGenerator refreshTable(JavascriptFunction onAfterRefresh)
    {
        if (onAfterRefresh == null)
        {
            return new JavascriptGenerator().refresh(id);
        }
        else
        {
            return new JavascriptGenerator().refresh(onAfterRefresh, id);
        }
    }


    // ----------------------------------------------------------
    public String tableBodyId()
    {
        return id + "__tbody";
    }


    // ----------------------------------------------------------
    public static String renderTableBusyScript(String gridId)
    {
        NSMutableDictionary<String, Object> props =
            new NSMutableDictionary<String, Object>();
        props.setObjectForKey(0.25, "opacity");

        JavascriptGenerator js = new JavascriptGenerator();
        // TODO change this id to be more general
        js.animateProperty(gridId + "__tbody", props, 250, null).play();
        return js.toString(true);
    }


    // ----------------------------------------------------------
    // TODO rewrite this function to use the one above
    public String renderTableBusyScript()
    {
        NSMutableDictionary<String, Object> props =
            new NSMutableDictionary<String, Object>();
        props.setObjectForKey(0.25, "opacity");

        JavascriptGenerator js = new JavascriptGenerator();
        js.animateProperty(tableBodyId(), props, 250, null).play();
        return js.toString(true);
    }


    // ----------------------------------------------------------
    public static WCTable currentTable()
    {
        return (WCTable) ERXWOContext.contextDictionary().objectForKey(
                CURRENT_TABLE_KEY);
    }


    // ----------------------------------------------------------
    public static WCTable setCurrentTable(WCTable table)
    {
        WCTable oldTable =
            (WCTable) ERXWOContext.contextDictionary().objectForKey(
                CURRENT_TABLE_KEY);

        if (table == null)
        {
            ERXWOContext.contextDictionary().removeObjectForKey(
                    CURRENT_TABLE_KEY);
        }
        else
        {
            ERXWOContext.contextDictionary().setObjectForKey(table,
                    CURRENT_TABLE_KEY);
        }

        return oldTable;
    }


    // ----------------------------------------------------------
    public int numberOfColumns()
    {
        return numberOfColumns;
    }


    // ----------------------------------------------------------
    public String passthroughAttributes()
    {
        return passthroughAttributes;
    }


    // ----------------------------------------------------------
    public void handleTakeValueForUnboundKey(Object value, String key)
    {
        if (passthroughAttributes == null)
        {
            passthroughAttributes = "";
        }

        if (value != null)
        {
            passthroughAttributes += " " + key + "=\""
                + WOMessage.stringByEscapingHTMLAttributeValue(
                        value.toString()) + "\"";
        }
    }


    //~ Static/instance variables .............................................

    private static final String CURRENT_TABLE_KEY =
        "org.webcat.ui.WCTable.currentTable";

    private String passthroughAttributes;

    protected boolean needsInitialSort = false;
    protected int numberOfColumns = 0;

    private String searchText;
    private NSMutableArray<String> searchOnKeyPathArray;
}
