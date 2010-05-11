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

package org.webcat.core;

import com.webobjects.eocontrol.EOEditingContext;
import er.extensions.eof.ERXEC;

//-------------------------------------------------------------------------
/**
 * An editing context that is used to handle migration of attribute values in
 * EOs at the beginning of their life cycle. This class does not actually
 * contain any new functionality of its own; it acts merely as an "instanceof"
 * check to prevent an infinite loop in the EO's awake* methods when the object
 * has to be loaded into a child context for migration.
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class MigratingEditingContext extends ERXEC
{
    // ----------------------------------------------------------
    public MigratingEditingContext()
    {
        super();
    }
    

    // ----------------------------------------------------------
    public MigratingEditingContext(EOEditingContext parent)
    {
        super(parent);
    }
}
