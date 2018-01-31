/*==========================================================================*\
 |  $Id: WCFilteringSelect.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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

package org.webcat.ui;

import java.util.List;
import org.webcat.ui._base.DojoSingleSelectionListFormElement;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSDictionary;

// ------------------------------------------------------------------------
/**
 * An enhanced version of the HTML {@code select} tag.
 *
 * <h2>Bindings</h2>
 *
 * <dl>
 *
 * <dt>fixedSize</dt>
 * <dd>Do not resize the widget based on the widths of the elements inside it.
 * Default value is false.</dd>
 *
 * <dt>minimumWidth</dt>
 * <dd>If resizing to fit (the default behavior), the minimum width that the
 * widget will be allowed to take.</dd>
 *
 * <dt>maximumWidth</dt>
 * <dd>If resizing to fit (the default behavior), the maximum width that the
 * widget will be allowed to take.</dd>
 *
 * </dl>
 *
 * @author Tony Allevato
 * @version $Id: WCFilteringSelect.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class WCFilteringSelect extends DojoSingleSelectionListFormElement
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public WCFilteringSelect(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("span", someAssociations, template);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return "webcat.FilteringSelect";
    }


    // ----------------------------------------------------------
    @Override
    public void takeValuesFromRequest(WORequest request, WOContext context)
    {
        if(_selection != null &&
                !isDisabledInContext(context) && context.wasFormSubmitted())
        {
            Object selection = null;
            String valueString =
                request.stringFormValueForKey(nameInContext(context));

            if(valueString != null)
            {
                valueString = valueString.trim();

                if(!NO_SELECTION_PLACEHOLDER.equals(valueString))
                {
                    int index = Integer.parseInt(valueString);
                    List<?> optionList = listInContext(context);

                    if(optionList != null && index < optionList.size())
                        selection = optionList.get(index);
                }
            }

            _selection.setValue(selection, context.component());
        }
    }
}
