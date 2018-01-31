/*==========================================================================*\
 |  $Id: QueryAssistantDescriptor.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
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

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import org.apache.log4j.Logger;

//-------------------------------------------------------------------------
/**
 * Query assistant support class.
 *
 * @author aallowat
 * @version $Id: QueryAssistantDescriptor.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
 */
public class QueryAssistantDescriptor
    implements NSKeyValueCoding
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * TODO real description
     *
     * @param anId     The unique id of the query assistant
     * @param anInfo   A dictionary containing properties about the query
     *                 assistant
     */
    public QueryAssistantDescriptor(
        String anId, NSDictionary<String, Object> anInfo)
    {
        id = anId;
        info = anInfo;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String id()
    {
        return id;
    }


    // ----------------------------------------------------------
    public NSArray<String> entities()
    {
        return (NSArray<String>) info.objectForKey("entities");
    }


    // ----------------------------------------------------------
    public String modelName()
    {
        return (String) info.objectForKey("modelName");
    }


    // ----------------------------------------------------------
    public String editorComponentName()
    {
        return (String) info.objectForKey("editorComponentName");
    }


    // ----------------------------------------------------------
    public String description()
    {
        return (String) info.objectForKey("description");
    }


    // ----------------------------------------------------------
    public AbstractQueryAssistantModel createModel()
    {
        try
        {
            Class<?> klass = Class.forName(modelName());
            return (AbstractQueryAssistantModel)klass.newInstance();
        }
        catch (Exception e)
        {
            // This shouldn't happen because we check at load time if the
            // class exists.
            log.error(
                "Could not create query assistant model of type "
                    + modelName());

            return null;
        }
    }


    // ----------------------------------------------------------
    public void takeValueForKey(Object value, String key)
    {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(
            this, value, key);
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }


    //~ Instance/static variables .............................................

    private String id;
    private NSDictionary<String, Object> info;

    private static final Logger log =
        Logger.getLogger(QueryAssistantDescriptor.class);
}
