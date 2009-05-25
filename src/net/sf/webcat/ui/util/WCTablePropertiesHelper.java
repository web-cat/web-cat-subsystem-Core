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

package net.sf.webcat.ui.util;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSDictionaryUtilities;
import er.extensions.components._private.ERXWOForm;

//------------------------------------------------------------------------
/**
 * This class maintains the "properties.*" associations for a WCDataTableColumn
 * element.
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class WCTablePropertiesHelper
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public WCTablePropertiesHelper(
            NSMutableDictionary<String, WOAssociation> associations)
    {
        _propertiesAssociations =
            _NSDictionaryUtilities.extractObjectsForKeysWithPrefix(
                    associations, "properties.", true);
        
        if (_propertiesAssociations == null
                || _propertiesAssociations.count() <= 0)
        {
            _propertiesAssociations = null;
        }
    }


    //~ Methods ...............................................................

    public NSDictionary<String, Object> propertiesInContext(WOContext context)
    {
        NSMutableDictionary<String, Object> props =
            new NSMutableDictionary<String, Object>();

        if (_propertiesAssociations != null)
        {
            for (String key : _propertiesAssociations.allKeys())
            {
                WOAssociation assoc = _propertiesAssociations.objectForKey(key);
                Object value = assoc.valueInComponent(context.component());
                
                props.setObjectForKey(value, key);
            }
        }

        return props;
    }
    

    //~ Static/instance variables .............................................
    
    private NSDictionary<String, WOAssociation> _propertiesAssociations;
}
