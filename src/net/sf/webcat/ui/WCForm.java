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

import org.apache.log4j.Logger;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import er.extensions.components._private.ERXWOForm;

//--------------------------------------------------------------------------
/**
 * This class used to have more functionality, until we removed the idea of
 * "shadow buttons" and instead create buttons dynamically for elements that
 * need to submit forms but aren't buttons themselves. We leave it here for
 * future enhancements.
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class WCForm extends ERXWOForm
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCForm(String name,
            NSDictionary<String, WOAssociation> associations,
            WOElement element)
    {
        super(name, associations, element);
    }

    
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public static String scriptToPerformFakeFullSubmit(WOContext context,
            String fieldName)
    {
        String formName = formName(context, null);
        if (formName == null)
        {
            log.warn("An element that uses a faked full form submit must be "
                    + "contained within a form.");
        }
        
        return "webcat.fakeFullSubmit('" + formName + "', '"
            + fieldName + "');";
    }
    
    
    //~ Static/instance variables .............................................

    private static Logger log = Logger.getLogger(WCForm.class);
}
