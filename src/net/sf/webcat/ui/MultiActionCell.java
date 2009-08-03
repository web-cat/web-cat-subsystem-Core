/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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

import java.text.Format;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver._private.WOFormatterRepository;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

//------------------------------------------------------------------------
/**
 * A cell that contains a row of icons that can be clicked to perform actions.
 *
 * <p><b>Cell Properties</b></p>
 * <ul>
 * <li><b>icon#.framework</b> (where # is a number starting from 1): the
 * framework from which to obtain the image for the #th icon.</li>
 * <li><b>icon#.filename</b> (where # is a number starting from 1): the
 * path, relative to WebServerResources, of the image for the #th icon.</li>
 * <li><b>icon#.action</b> (where # is a number starting from 1): the name of
 * the action to be performed on the component that contains the table.</li>
 * </ul>
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class MultiActionCell extends WCTableCell
{
    //~ Constructors ..........................................................
    
    // ----------------------------------------------------------
    public MultiActionCell(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public NSDictionary<String, Object> iconInRepetition;
    

    //~ Methods ...............................................................

    public NSArray<NSDictionary<String, Object>> icons()
    {
        return icons;
    }
    
    
    // ----------------------------------------------------------
    @Override
    public void setProperties(NSDictionary<String, Object> props)
    {
        super.setProperties(props);
        
        icons = new NSMutableArray<NSDictionary<String, Object>>();
        
        int index = 1;
        
        String actionKey = "icon" + index + ".action";
        String actionName = (String) props.objectForKey(actionKey);

        while (actionName != null)
        {
            String filename = (String) props.objectForKey(
                    "icon" + index + ".filename");
            String framework = (String) props.objectForKey(
                    "icon" + index + ".framework");
            String title = (String) props.objectForKey(
                    "icon" + index + ".title");

            NSMutableDictionary<String, Object> icon =
                new NSMutableDictionary<String, Object>();
            icon.setObjectForKey(actionName, "action");

            if (filename != null)
            {
                icon.setObjectForKey(filename, "filename");
            }
            
            if (framework != null)
            {
                icon.setObjectForKey(framework, "framework");
            }

            if (title != null)
            {
                icon.setObjectForKey(title, "title");
            }

            icons.addObject(icon);
            
            index++;

            actionKey = "icon" + index + ".action";
            actionName = (String) props.objectForKey(actionKey);
        }
    }
    

    // ----------------------------------------------------------
    /**
     * Performs the action named in the "action" property on the component that
     * contains the ObjectTable.
     * 
     * @return the results of the action
     */
    public WOActionResults performAction()
    {
        if (iconInRepetition != null)
        {
            String action = (String) iconInRepetition.objectForKey("action");
            return parent().performParentAction(action);
        }
        else
        {
            return null;
        }
    }
    
    
    //~ Static/instance variables .............................................

    private NSMutableArray<NSDictionary<String, Object>> icons;
}
