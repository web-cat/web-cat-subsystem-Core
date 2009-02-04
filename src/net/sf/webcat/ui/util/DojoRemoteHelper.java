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
 * This class maintains the "remote.*" associations for an action element, and 
 * also handles the generation of callback scripts that involve values of
 * multiple associations.
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class DojoRemoteHelper
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public DojoRemoteHelper(
            NSMutableDictionary<String, WOAssociation> associations)
    {
        _remote = associations.removeObjectForKey("remote");

        _remoteAssociations =
            _NSDictionaryUtilities.extractObjectsForKeysWithPrefix(
                    associations, "remote.", true);
        
        if (_remoteAssociations == null || _remoteAssociations.count() <= 0)
        {
            _remoteAssociations = null;
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Indicates whether the element should use remote (Ajax) requests.
     * 
     * @param context 
     * 
     * @return true if the element should use Ajax; false if it should use
     *     standard page-load actions
     */
    public boolean isRemoteInContext(WOContext context)
    {
        return ((_remote != null &&
                _remote.booleanValueInComponent(context.component())) ||
                _remoteAssociations != null);
    }


    // ----------------------------------------------------------
    private WOAssociation associationWithName(String name)
    {
        if (_remoteAssociations == null)
        {
            return null;
        }
        else
        {
            return _remoteAssociations.objectForKey(name);
        }
    }
    

    // ----------------------------------------------------------
    public String xhrMethodCallWithURL(String sender, String url,
            WOContext context)
    {
        return xhrMethodCallWithURL(sender, url, null, context);
    }


    // ----------------------------------------------------------
    public String xhrMethodCallWithURL(String sender, String url,
            NSDictionary<String, String> contentOptions,
            WOContext context)
    {
        WOComponent component = context.component();
        StringBuffer buffer = new StringBuffer();

        buffer.append("webcat.invokeRemoteAction(");
        buffer.append(sender);
        buffer.append(", {");

        WOAssociation _responseType = associationWithName("responseType");
        WOAssociation _refreshPanes = associationWithName("refreshPanes");
        WOAssociation _form = associationWithName("form");
        WOAssociation _synchronous = associationWithName("synchronous");

        // TODO this is part of the Ajax partial submit problem
        if (contentOptions != null)
        {
            buffer.append("content:{");
            
            NSArray<String> keys = contentOptions.allKeys();
            for (int i = 0; i < keys.count(); i++)
            {
                String key = keys.objectAtIndex(i);
                String value = contentOptions.objectForKey(key);

                if (key.startsWith("^"))
                {
                    buffer.append("\"");
                    buffer.append(key.substring(1));
                    buffer.append("\"");
                    buffer.append(":");
                    buffer.append(value);
                }
                else
                {
                    buffer.append("\"");
                    buffer.append(key);
                    buffer.append("\"");
                    buffer.append(":");
                    buffer.append("\"");
                    buffer.append(value);
                    buffer.append("\"");
                }

                if (i < keys.count() - 1)
                {
                    buffer.append(",");
                }
            }
            
            buffer.append("},");
        }

        String responseType = null;
        if (_responseType != null)
        {
            responseType =
                _responseType.valueInComponent(component).toString();
        }
        
        if (responseType != null)
        {
            buffer.append("handleAs:'");
            buffer.append(responseType);
            buffer.append("',");
        }

        String formId = null;
        if (_form != null)
        {
            formId = _form.valueInComponent(component).toString();

            if (formId != null)
            {
                buffer.append("form:dojo.byId('");
                buffer.append(formId);
                buffer.append("'),");
            }
        }
        else
        {
            formId = ERXWOForm.formName(context, null);

            if (formId != null)
            {
                buffer.append("form:dojo.query('form[name=");
                buffer.append(formId);
                buffer.append("]')[0],");
            }
        }
        
        buffer.append("url:'");
        buffer.append(url);
        buffer.append("'");
        
        // Append the synchronous flag.
        boolean synchronous = false;
        if (_synchronous != null)
        {
            synchronous = _synchronous.booleanValueInComponent(component);
        }

        if (synchronous)
        {
            buffer.append(",sync:true");
        }

        buffer.append("}");
        
        String refreshPanes = null;
        if (_refreshPanes != null)
        {
            Object panes = _refreshPanes.valueInComponent(component);
            
            if (panes instanceof List)
            {
                refreshPanes = DojoUtils.doubleToSingleQuotes(
                        new JSONArray((List<?>) panes).toString());
            }
            else
            {
                try
                {
                    refreshPanes =
                        DojoUtils.doubleToSingleQuotes(
                                new JSONArray(panes.toString()).toString());
                }
                catch (JSONException e)
                {
                    refreshPanes = "'"
                        + DojoUtils.doubleToSingleQuotes(panes.toString())
                        + "'";
                }
            }
        }

        if (refreshPanes != null && refreshPanes.length() > 0)
        {
            buffer.append(", " + refreshPanes);
        }
        
        buffer.append(");");

        return buffer.toString();
    }
    
    
    //~ Static/instance variables .............................................
    
    private WOAssociation _remote;
    private NSDictionary<String, WOAssociation> _remoteAssociations;
}
