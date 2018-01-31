/*==========================================================================*\
 |  $Id: WCTreeTestPage.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2009-2012 Virginia Tech
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

import org.webcat.ui.WCTree;
import org.webcat.ui.WCTreeModel;
import org.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSet;

//-------------------------------------------------------------------------
/**
 * A test page for the WCTree component.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class WCTreeTestPage
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTreeTestPage(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public String anObject;
    public boolean isExpanded;
    public String lastClickedObject;
    public TestTreeModel testModel = new TestTreeModel();


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String renderGridBusyScript()
    {
        return WCTree.renderTableBusyScript("testTree");
    }


    // ----------------------------------------------------------
    public WOActionResults doSomethingWithSelectedItems()
    {
        NSSet<String> selection = testModel.selectedObjects();
        System.out.println(selection.toString());

        testModel.clearSelection();
        return new JavascriptGenerator().refresh("testTree");
    }


    // ----------------------------------------------------------
    public WOActionResults doSomethingWithAnItem()
    {
        lastClickedObject = anObject;
        return new JavascriptGenerator().refresh("notice");
    }


    //~ Nested classes ........................................................

    // ----------------------------------------------------------
    private static class TestTreeModel extends WCTreeModel<String>
    {
        // ----------------------------------------------------------
        @Override
        public NSArray<String> childrenOfObject(String item)
        {
            NSMutableArray<String> children = new NSMutableArray<String>();

            if (item == null || item.length() < 10)
            {
                for (int i = 0; i < 3; i++)
                {
                    if (item == null)
                    {
                        children.addObject("Item " + i);
                    }
                    else
                    {
                        children.addObject(item + "." + i);
                    }
                }

                return children;
            }
            else
            {
                return null;
            }
        }


        // ----------------------------------------------------------
        @Override
        public String persistentIdOfObject(String item)
        {
            return item;
        }
    }


    //~ Static/instance variables .............................................
}
