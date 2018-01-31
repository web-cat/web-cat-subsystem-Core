/*==========================================================================*\
 |  $Id: WCPaginationBar.java,v 1.2 2010/10/29 20:36:15 aallowat Exp $
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

package org.webcat.ui;

import org.webcat.core.WCComponent;
import org.webcat.ui.util.ComponentIDGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.components.ERXComponentUtilities;

//-------------------------------------------------------------------------
/**
 * <p>
 * Displays a pagination bar that can be used to navigate a batched display
 * group.
 * </p>
 * <h2>Bindings</h2>
 * <dl>
 * <dt>action (<code>WOActionResults</code>)</dt>
 * <dd>The action to execute when the user causes a change in the state of the
 * pagination bar (selecting a different page, or changing the size of the
 * batch).</dd>
 * <dt>fixedPageSize (<code>boolean</code>)</dt>
 * <dd>If true, the user will not be able to change the page size. This can be
 * desirable if the table is inside a dialog box and should remain small on the
 * screen.</dd>
 * <dt>displayGroup (<code>ERXDisplayGroup&lt;?&gt;</code>)</dt>
 * <dd>The display group that is being paged.</dd>
 * <dt>onChange (<code>String</code>)</dt>
 * <dd>Javascript code that will be executed whenever the user causes a change
 * in the state of the pagination bar (selecting a different page, or changing
 * the size of the batch).</dd>
 * <dt>settingsKey (<code>String</code>)</dt>
 * <dd>The key in the user's preferences where the batch size will be
 * persisted.</dd>
 * </dl>
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.2 $, $Date: 2010/10/29 20:36:15 $
 */
public class WCPaginationBar extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new WCPaginationBar instance.
     *
     * @param context the context
     */
    public WCPaginationBar(WOContext context)
    {
        super(context);
    }


    //~ KVC Attributes (must be public) .......................................

    public ComponentIDGenerator idFor;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void appendToResponse(WOResponse response, WOContext context)
    {
        idFor = new ComponentIDGenerator(this);

        if (needsBatchSizeSet)
        {
            setInitialBatchSize();
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public boolean synchronizesVariablesWithBindings()
    {
        return false;
    }


    // ----------------------------------------------------------
    public ERXDisplayGroup<?> displayGroup()
    {
        return (ERXDisplayGroup<?>) valueForBinding("displayGroup");
    }


    // ----------------------------------------------------------
    public WOActionResults action()
    {
        return (WOActionResults) valueForBinding("action");
    }


    // ----------------------------------------------------------
    public boolean fixedPageSize()
    {
        return ERXComponentUtilities.booleanValueForBinding(
                this, "fixedPageSize", false);
    }


    // ----------------------------------------------------------
    public String onChange()
    {
        Object value = valueForBinding("onChange");
        return (value != null) ? value.toString() : null;
    }


    // ----------------------------------------------------------
    public String settingsKey()
    {
        Object value = valueForBinding("settingsKey");
        return (value != null) ? value.toString() : null;
    }


    // ----------------------------------------------------------
    public Integer aPageNumber()
    {
        return aPageNumber;
    }


    // ----------------------------------------------------------
    public void setAPageNumber(Integer value)
    {
        aPageNumber = value;
    }


    // ----------------------------------------------------------
    public int currentPageNumber()
    {
        return displayGroup().currentBatchIndex();
    }


    // ----------------------------------------------------------
    public int numberOfPages()
    {
        return displayGroup().batchCount();
    }


    // ----------------------------------------------------------
    public int indexOfLastDisplayedObject()
    {
        return Math.min(displayGroup().filteredObjects().count(),
                displayGroup().indexOfLastDisplayedObject());
    }


    // ----------------------------------------------------------
    public int firstSurroundingPageNumber()
    {
        int current = currentPageNumber() - 3;

        if (current < 2)
        {
            current = 2;
        }

        return current;
    }


    // ----------------------------------------------------------
    public int lastSurroundingPageNumber()
    {
        int current = currentPageNumber() + 3;

        if (current > numberOfPages() - 1)
        {
            current = numberOfPages() - 1;
        }

        return current;
    }


    // ----------------------------------------------------------
    public boolean isGapAfterFirstPage()
    {
        return firstSurroundingPageNumber() > 2;
    }


    // ----------------------------------------------------------
    public boolean isGapBeforeLastPage()
    {
        return lastSurroundingPageNumber() < numberOfPages() - 1;
    }


    // ----------------------------------------------------------
    public boolean isFirstPageCurrentPage()
    {
        return 1 == currentPageNumber();
    }


    // ----------------------------------------------------------
    public boolean isLastPageCurrentPage()
    {
        return numberOfPages() == currentPageNumber();
    }


    // ----------------------------------------------------------
    public boolean isAPageNumberCurrentPage()
    {
        return aPageNumber == currentPageNumber();
    }


    // ----------------------------------------------------------
    private String cssClassForPageWithNumber(int page)
    {
        if (page == currentPageNumber())
        {
            return "current";
        }
        else
        {
            return "active";
        }
    }


    // ----------------------------------------------------------
    public String cssClassForFirstPage()
    {
        return cssClassForPageWithNumber(1);
    }


    // ----------------------------------------------------------
    public String cssClassForLastPage()
    {
        return cssClassForPageWithNumber(numberOfPages());
    }


    // ----------------------------------------------------------
    public String cssClassForAPageNumber()
    {
        return cssClassForPageWithNumber(aPageNumber);
    }


    // ----------------------------------------------------------
    public NSArray<Integer> surroundingPageNumbers()
    {
        NSMutableArray<Integer> numbers = new NSMutableArray<Integer>();

        for (int page = firstSurroundingPageNumber();
             page <= lastSurroundingPageNumber(); page++)
        {
            numbers.addObject(page);
        }

        return numbers;
    }


    // ----------------------------------------------------------
    private WOActionResults goToPageWithNumber(int page)
    {
        displayGroup().clearSelection();
        displayGroup().setCurrentBatchIndex(page);

        return action();
    }


    // ----------------------------------------------------------
    public WOActionResults goToFirstPage()
    {
        return goToPageWithNumber(1);
    }


    // ----------------------------------------------------------
    public WOActionResults goToLastPage()
    {
        return goToPageWithNumber(numberOfPages());
    }


    // ----------------------------------------------------------
    public WOActionResults goToAPageNumber()
    {
        return goToPageWithNumber(aPageNumber);
    }


    // ----------------------------------------------------------
    private WOActionResults changePageSize(int pageSize)
    {
        setPersistentBatchSize(pageSize);

        displayGroup().clearSelection();
        displayGroup().setNumberOfObjectsPerBatch(pageSize);

        return action();
    }


    // ----------------------------------------------------------
    public WOActionResults changePageSizeTo10()
    {
        return changePageSize(10);
    }


    // ----------------------------------------------------------
    public WOActionResults changePageSizeTo25()
    {
        return changePageSize(25);
    }


    // ----------------------------------------------------------
    public WOActionResults changePageSizeTo50()
    {
        return changePageSize(50);
    }


    // ----------------------------------------------------------
    public WOActionResults changePageSizeTo100()
    {
        return changePageSize(100);
    }


    // ----------------------------------------------------------
    public WOActionResults changePageSizeTo200()
    {
        return changePageSize(200);
    }


    // ----------------------------------------------------------
    private Integer persistentBatchSize()
    {
        if (settingsKey() == null)
        {
            return null;
        }
        else
        {
            return (Integer) user().preferences().valueForKeyPath(
                    settingsKey() + "_batchSize");
        }
    }


    // ----------------------------------------------------------
    private void setPersistentBatchSize(int batchSize)
    {
        if (settingsKey() != null)
        {
            user().preferences().takeValueForKey(
                    batchSize, settingsKey() + "_batchSize");
            user().savePreferences();
        }
    }


    // ----------------------------------------------------------
    private void setInitialBatchSize()
    {
        needsBatchSizeSet = false;

        Integer batchSize = persistentBatchSize();
        if (batchSize != null)
        {
            displayGroup().setNumberOfObjectsPerBatch(batchSize);
        }
        else
        {
            int size = displayGroup().numberOfObjectsPerBatch();

            // Cap the batch size to one of the fixed amounts that we support.
            if (size != 0)
            {
                if (size <= 10)
                {
                    size = 10;
                }
                else if (size <= 25)
                {
                    size = 25;
                }
                else if (size <= 50)
                {
                    size = 50;
                }
                else if (size <= 100)
                {
                    size = 100;
                }
                else
                {
                    size = 200;
                }

                displayGroup().setNumberOfObjectsPerBatch(size);
            }
        }
    }


    //~ Static/instance variables .............................................

    private Integer aPageNumber;
    private boolean needsBatchSizeSet = true;
}
