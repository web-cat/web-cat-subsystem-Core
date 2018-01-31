/*==========================================================================*\
 |  $Id: WCSimpleTextArea.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
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

import org.webcat.ui._base.DojoFormElement;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

//------------------------------------------------------------------------
/**
 * A basic text area that automatically takes on Dojo styling.
 * 
 * <h2>Bindings</h2>
 * <dl>
 * <dt>rows</dt>
 * <dd>The number of rows to display in the text area.</dd>
 * <dt>cols</dt>
 * <dd>The number of columns wide to make the text area.</dd>
 * </dl>
 * 
 * @author Tony Allevato
 * @version $Id: WCSimpleTextArea.java,v 1.1 2010/05/11 14:51:58 aallowat Exp $
 */
public class WCSimpleTextArea extends WCTextBox
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCSimpleTextArea(String name,
            NSDictionary<String, WOAssociation> someAssociations,
            WOElement template)
    {
        super("input", someAssociations, template);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public String dojoType()
    {
        return "dijit.form.SimpleTextarea";
    }
}
