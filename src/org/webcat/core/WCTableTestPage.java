/*==========================================================================*\
 |  $Id: WCTableTestPage.java,v 1.1 2010/10/28 00:37:30 aallowat Exp $
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

package org.webcat.core;

import org.webcat.ui.WCTable;
import org.webcat.ui.generators.JavascriptGenerator;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.appserver.ERXDisplayGroup;

//-------------------------------------------------------------------------
/**
 * A test page for the WCTable component.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: aallowat $
 * @version $Revision: 1.1 $, $Date: 2010/10/28 00:37:30 $
 */
public class WCTableTestPage extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCTableTestPage(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public NSDictionary<String, Object> anObject;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public ERXDisplayGroup<NSDictionary<String, Object>> objects()
    {
        if (objects == null)
        {
            NSMutableArray<NSDictionary<String, Object>> objectArray =
                new NSMutableArray<NSDictionary<String, Object>>();

            for (int i = 0; i < 100; i++)
            {
                NSMutableDictionary<String, Object> obj =
                    new NSMutableDictionary<String, Object>();

                obj.setObjectForKey(i, "index");
                obj.setObjectForKey(i * i, "indexSquared");
                obj.setObjectForKey(100 - i, "backwardIndex");

                objectArray.addObject(obj);
            }

            objects = new ERXDisplayGroup<NSDictionary<String, Object>>();
            objects.setObjectArray(objectArray);
            objects.setNumberOfObjectsPerBatch(15);
            objects.clearSelection();
        }

        return objects;
    }


    // ----------------------------------------------------------
    public String renderGridBusyScript()
    {
        return WCTable.renderTableBusyScript("testGrid");
    }


    // ----------------------------------------------------------
    public WOActionResults doSomethingWithSelectedItems()
    {
        objects.deleteSelection();
        objects.clearSelection();
        return new JavascriptGenerator().refresh("testGrid");
    }


    //~ Static/instance variables .............................................

    private ERXDisplayGroup<NSDictionary<String, Object>> objects;
}
