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

package net.sf.webcat.ui;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;
import net.sf.webcat.ui._base.DojoActionFormElement;

//------------------------------------------------------------------------
/**
 * A simple push-button.
 * 
 * <h2>Bindings</h2>
 * See also the bindings for {@link net.sf.webcat.ui.DojoActionFormElement}.
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
 * @version $Id$
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
        if (_remoteHelper.isRemoteInContext(context))
        {
            return "button";
        }
        else
        {
            return "submit";
        }
    }
}
