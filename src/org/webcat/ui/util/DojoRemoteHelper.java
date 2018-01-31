/*==========================================================================*\
 |  $Id: DojoRemoteHelper.java,v 1.2 2010/10/11 14:25:35 aallowat Exp $
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

package org.webcat.ui.util;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.webcat.ui.WCForm;
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
 * @version $Id: DojoRemoteHelper.java,v 1.2 2010/10/11 14:25:35 aallowat Exp $
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
    public String remoteSubmitCall(
            String sendingWidget,
            JSHash initialOptions,
            WOContext context)
    {
        WOComponent component = context.component();

        WOAssociation _responseType = associationWithName("responseType");
        WOAssociation _submit = associationWithName("submit");
        WOAssociation _synchronous = associationWithName("synchronous");
        WOAssociation _suppressBusyCursor =
            associationWithName("suppressBusyCursor");

        JSHash options = new JSHash();
        if (initialOptions != null)
        {
            options.merge(initialOptions);
        }

        // If the user has provided a remote.responseType, use it; otherwise,
        // default to "javascript", so that JavascriptGenerators returned by
        // the remote action will be executed.

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

        options.put("handleAs", responseType);

        if (_suppressBusyCursor != null &&
                _suppressBusyCursor.booleanValueInComponent(component))
        {
            options.put("suppressBusyCursor", true);
        }

        // Handle a partial submit.

        if (_submit != null)
        {
            String submitId = _submit.valueInComponent(component).toString();

            if (submitId != null)
            {
                options.put("submit", submitId);
            }
        }

        // Whether or not this is a partial submit, we pass the form reference
        // to the remote submit function; this is so we can access the form's
        // action URL if necessary.

        String formName = ERXWOForm.formName(context, null);
        if (formName != null)
        {
            options.put("form",
                    JSHash.code(WCForm.formElementByName(formName)));
        }

        // Append the synchronous flag.
        boolean synchronous = false;
        if (_synchronous != null)
        {
            synchronous = _synchronous.booleanValueInComponent(component);
        }

        if (synchronous)
        {
            options.put("sync", true);
        }

        // Append the options dictionary to the script buffer.

        StringBuffer buffer = new StringBuffer();

        buffer.append("webcat.remoteSubmit(");
        buffer.append(sendingWidget);
        buffer.append(", ");
        buffer.append(options.toString());
        buffer.append(");");

        return buffer.toString();
    }


    //~ Static/instance variables .............................................

    private WOAssociation _remote;
    private NSDictionary<String, WOAssociation> _remoteAssociations;
}
