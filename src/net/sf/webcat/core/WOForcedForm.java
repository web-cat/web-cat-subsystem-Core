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

package net.sf.webcat.core;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import er.extensions.*;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  This experimental class is just an attempt at creating a form
 *  component that always emits its tag, since form detection appears
 *  to fail in some circumstances; use with extreme caution.
 *
 *  @author  stedwar2
 *  @version $Id$
 */
public class WOForcedForm
    extends ERXWOForm
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public WOForcedForm( String name,
                         NSDictionary associations,
                         WOElement template )
    {
        super( name, associations, template );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse( WOResponse response, WOContext context )
    {
        log.debug( "inForm = " + context.isInForm() );
        log.debug( "elementName = " + elementName() );
        context.setInForm( false );
        super.appendToResponse( response, context );
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( WOForcedForm.class );
}
