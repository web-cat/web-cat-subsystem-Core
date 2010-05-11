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

package org.webcat.ui;

import org.json.JSONException;
import org.json.JSONObject;

// ------------------------------------------------------------------------
/**
 * This interface defines a decoration method that allows data items in a model
 * to be annotated with extra properties before the item is marshalled into
 * JavaScript.
 * <p>
 * Currently only the {@link WCTree} component makes use of this functionality, in
 * the {@link WCCheckTree} subclass in order to attach the checked states of each
 * item to the item when it is fetched.
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public interface IItemDecorator
{
    // ----------------------------------------------------------
    /**
     * Attaches properties of the specified model item to a JSON properties
     * object.
     * 
     * @param modelItem
     *            the model item being decorated
     * @param properties
     *            the JSON object that describes the properties of the item
     * @throws JSONException
     */
    public void decorateItem(Object modelItem, JSONObject properties)
            throws JSONException;
}
