/*==========================================================================*\
 |  $Id: WCForcedForm.java,v 1.1 2011/10/25 12:51:37 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2011 Virginia Tech
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

package org.webcat.woextensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import er.extensions.components._private.ERXWOForm;

import org.webcat.woextensions.WCForcedForm;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  This experimental class is just an attempt at creating a form
 *  component that always emits its tag, since form detection appears
 *  to fail in some circumstances; use with extreme caution.
 *
 *  @author  stedwar2
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.1 $, $Date: 2011/10/25 12:51:37 $
 */
public class WCForcedForm
    extends ERXWOForm
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WCForcedForm(
        String name,
        NSDictionary<String, WOAssociation> associations,
        WOElement template)
    {
        super(name, associations, template);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse(WOResponse response, WOContext context)
    {
        log.debug("inForm = " + context.isInForm());
        log.debug("elementName = " + elementName());
        context.setInForm(false);
        super.appendToResponse(response, context);
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger(WCForcedForm.class);
}
