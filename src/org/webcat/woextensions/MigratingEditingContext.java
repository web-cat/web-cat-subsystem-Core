/*==========================================================================*\
 |  $Id: MigratingEditingContext.java,v 1.2 2014/08/25 15:28:01 stedwar2 Exp $
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

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;

//-------------------------------------------------------------------------
/**
 * An editing context that is used to handle migration of attribute values in
 * EOs at the beginning of their life cycle. This class does not actually
 * contain any new functionality of its own; it acts merely as an "instanceof"
 * check to prevent an infinite loop in the EO's awake* methods when the object
 * has to be loaded into a child context for migration.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2014/08/25 15:28:01 $
 */
public class MigratingEditingContext
    extends WCEC
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    public MigratingEditingContext(EOObjectStore store)
    {
        super(store);
    }

    // ----------------------------------------------------------
    /**
     * Creates a new peer editing context, typically used to make
     * changes outside of a session's editing context.
     * @return the new editing context
     */
    public static MigratingEditingContext newEditingContext()
    {
        return (MigratingEditingContext)factory()._newEditingContext();
    }


    // ----------------------------------------------------------
    public static class MigratingFactory
        extends WCECFactory
    {
        protected EOEditingContext _createEditingContext(EOObjectStore parent)
        {
            return new MigratingEditingContext(parent == null
                ? EOEditingContext.defaultParentObjectStore()
                : parent);
        }

        public EOEditingContext _newEditingContext(
            EOObjectStore objectStore, boolean validationEnabled)
        {
            EOEditingContext result =
                super._newEditingContext(objectStore, validationEnabled);
//            result.setSharedEditingContext(null);
            return result;
        }
    }


    // ----------------------------------------------------------
    public static Factory factory() {
        if (factory == null) {
            factory = new MigratingFactory();
        }
        return factory;
    }


    //~ Static/instance variables .............................................

    private static Factory factory;
}
