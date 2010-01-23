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
import net.sf.webcat.ui.WCForm;
import org.json.JSONArray;
import org.json.JSONException;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSDictionaryUtilities;
import er.extensions.components._private.ERXWOForm;

//------------------------------------------------------------------------
/**
 * <p>
 * This class maintains the "remote.*" associations for an action element, and
 * also handles the generation of callback scripts that involve values of
 * multiple associations.
 * </p>
 * <h2>Bindings</h2>
 * <dl>
 * <dt>responseType</dt>
 * <dd>The expected response type of the action being invoked remotely. In most
 * cases this can be omitted.</dd>
 *
 * <dt>refreshPanes</dt>
 * <dd>One or more WCContentPane identifiers that will be refreshed when the
 * action returns successfully. This can be specified as a string containing
 * a single identifier ("paneId"), a string containing an array of identifiers
 * in JSON notation ("[ 'paneId1', 'paneId2' ]"; note the single quotes around
 * the identifiers in this case), or bound to something that implements
 * java.util.List.</dd>
 *
 * <dt>block</dt>
 * <dd>Specifies whether a blocking overlay should be placed somewhere on the
 * page while the action is being executed. This can be set to the boolean
 * "true", in which case overlays will be placed over each of the content
 * panes specified by the refreshPanes binding, or it can be one or more
 * explicit identifiers of DOM elements on the page that will be blocked.</dd>
 *
 * <dt>form</dt>
 * <dd>The DOM identifier of the form to serialize with this request. By
 * default, this will be the form that contains the element in question.</dd>
 *
 * <dt>synchronous</dt>
 * <dd>If true, the action will be invoked synchronously instead of
 * asynchronously. Use with caution, as this will block the browser until the
 * server responds.</dd>
 *
 * </dl>
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
    public String partialSubmitCall(String sender,
            String componentName,
            DojoOptions contentOptions,
            WOContext context)
    {
        WOComponent component = context.component();

        WOAssociation _responseType = associationWithName("responseType");
        WOAssociation _refreshPanes = associationWithName("refreshPanes");
        WOAssociation _form = associationWithName("form");
        WOAssociation _synchronous = associationWithName("synchronous");
        WOAssociation _block = associationWithName("block");

        DojoOptions options = new DojoOptions();

        if (contentOptions != null && !contentOptions.isEmpty())
        {
            options.putOptions("content", contentOptions);
        }

        String responseType = null;
        if (_responseType != null)
        {
            responseType =
                _responseType.valueInComponent(component).toString();
        }
        else
        {
            responseType = "javascript";
        }

        if (responseType != null)
        {
            options.putValue("handleAs", responseType);
        }

        String formId = null;
        if (_form != null)
        {
            formId = _form.valueInComponent(component).toString();

            if (formId != null)
            {
                String formString = "dojo.byId('" + formId + "')";
                options.putExpression("form", formString);
            }
        }
        else
        {
            formId = ERXWOForm.formName(context, null);

            if (formId != null)
            {
                options.putExpression("form",
                        WCForm.formElementByName(formId));
            }
        }

        // Append the synchronous flag.
        boolean synchronous = false;
        if (_synchronous != null)
        {
            synchronous = _synchronous.booleanValueInComponent(component);
        }

        if (synchronous)
        {
            options.putValue("sync", true);
        }

        // Append the options dictionary to the script buffer.

        StringBuffer buffer = new StringBuffer();

        buffer.append("webcat.partialSubmit(");
        buffer.append(sender);
        buffer.append(", '");
        buffer.append(componentName);
        buffer.append("', ");
        buffer.append(options.toString());

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
        else
        {
            buffer.append(", null");
        }

        String block = null;
        if (_block != null)
        {
            Object blockIds = _block.valueInComponent(component);

            if (blockIds instanceof Boolean && (Boolean) blockIds)
            {
                block = "true";
            }
            else if (blockIds instanceof List)
            {
                block = DojoUtils.doubleToSingleQuotes(
                        new JSONArray((List<?>) blockIds).toString());
            }
            else
            {
                try
                {
                    block =
                        DojoUtils.doubleToSingleQuotes(
                                new JSONArray(blockIds.toString()).toString());
                }
                catch (JSONException e)
                {
                    block = "'"
                        + DojoUtils.doubleToSingleQuotes(blockIds.toString())
                        + "'";
                }
            }
        }

        if (block != null && block.length() > 0)
        {
            buffer.append(", " + block);
        }

        buffer.append(");");

        return buffer.toString();
    }


    // ----------------------------------------------------------
    public String invokeRemoteActionCall(String sender, String url,
            DojoOptions contentOptions, WOContext context)
    {
        WOComponent component = context.component();

        WOAssociation _responseType = associationWithName("responseType");
        WOAssociation _refreshPanes = associationWithName("refreshPanes");
        WOAssociation _form = associationWithName("form");
        WOAssociation _synchronous = associationWithName("synchronous");
        WOAssociation _block = associationWithName("block");

        DojoOptions options = new DojoOptions();

        if (contentOptions != null && !contentOptions.isEmpty())
        {
            options.putOptions("content", contentOptions);
        }

        String responseType = null;
        if (_responseType != null)
        {
            responseType =
                _responseType.valueInComponent(component).toString();
        }
        else
        {
            responseType = "javascript";
        }

        if (responseType != null)
        {
            options.putValue("handleAs", responseType);
        }

        String formId = null;
        if (_form != null)
        {
            formId = _form.valueInComponent(component).toString();

            if (formId != null)
            {
                options.putExpression("form", "dojo.byId('" + formId + "')");
            }
        }
        else
        {
            formId = ERXWOForm.formName(context, null);

            if (formId != null)
            {
                options.putExpression("form",
                        WCForm.formElementByName(formId));
            }
        }

        options.putValue("url", url);

        // Append the synchronous flag.
        boolean synchronous = false;
        if (_synchronous != null)
        {
            synchronous = _synchronous.booleanValueInComponent(component);
        }

        if (synchronous)
        {
            options.putValue("sync", true);
        }

        StringBuffer buffer = new StringBuffer();

        buffer.append("webcat.invokeRemoteAction(");
        buffer.append(sender);
        buffer.append(", ");
        buffer.append(options.toString());

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
        else
        {
            buffer.append(", null");
        }

        String block = null;
        if (_block != null)
        {
            Object blockIds = _block.valueInComponent(component);

            if (blockIds instanceof Boolean && (Boolean) blockIds)
            {
                block = "true";
            }
            else if (blockIds instanceof List)
            {
                block = DojoUtils.doubleToSingleQuotes(
                        new JSONArray((List<?>) blockIds).toString());
            }
            else
            {
                try
                {
                    block =
                        DojoUtils.doubleToSingleQuotes(
                                new JSONArray(blockIds.toString()).toString());
                }
                catch (JSONException e)
                {
                    block = "'"
                        + DojoUtils.doubleToSingleQuotes(blockIds.toString())
                        + "'";
                }
            }
        }

        if (block != null && block.length() > 0)
        {
            buffer.append(", " + block);
        }

        buffer.append(");");

        return buffer.toString();
    }


    //~ Static/instance variables .............................................

    private WOAssociation _remote;
    private NSDictionary<String, WOAssociation> _remoteAssociations;
}
