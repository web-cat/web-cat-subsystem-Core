/*==========================================================================*\
 |  $Id: WCButton.java,v 1.2 2011/05/02 16:16:50 aallowat Exp $
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

package org.webcat.ui;

import org.webcat.ui._base.DojoActionFormElement;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

//------------------------------------------------------------------------
/**
 * A simple push-button.
 *
 * <h2>Bindings</h2>
 * See also the bindings for {@link org.webcat.ui._base.DojoActionFormElement}.
 * <table>
 * <tr>
 * <td>{@code iconClass}</td>
 * <td>The CSS class used to attach an icon to this button.</td>
 * </tr>
 * <tr>
 * <td>{@code label}</td>
 * <td>The text to display in the button, if component content is not used.</td>
 * </tr>
 * <tr>
 * <td>{@code showLabel}</td>
 * <td>A boolean value indicating whether the label should be shown.</td>
 * </tr>
 * </table>
 *
 * @author Tony Allevato
 * @version $Id: WCButton.java,v 1.2 2011/05/02 16:16:50 aallowat Exp $
 */
public class WCButton extends DojoActionFormElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public WCButton(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("button", someAssociations, template);

        _label = _associations.removeObjectForKey("label");
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return "dijit.form.Button";
    }


    // ----------------------------------------------------------
    @Override
    public String inputTypeInContext(WOContext context)
    {
        if (_remoteHelper.isRemoteInContext(context) ||
                !hasActionInContext(context))
        {
            return "button";
        }
        else
        {
            return "submit";
        }
    }


    // ----------------------------------------------------------
    @Override
    public void appendAttributesToResponse(WOResponse response,
            WOContext context)
    {
        int start = response.contentString().length();

        super.appendAttributesToResponse(response, context);

        if (_label != null)
        {
            String label = _label.valueInComponent(
                    context.component()).toString();

            if (label != null)
            {
                response._appendTagAttributeAndValue("label", label, true);

                if (response.contentString().indexOf("value=\"", start) == -1)
                {
                    response._appendTagAttributeAndValue("value", label, true);
                }
            }
        }
    }


    //~ Static/instance variables .............................................

    private WOAssociation _label;
}
