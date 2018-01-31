/*==========================================================================*\
 |  $Id: MigratoryAttributeOwner.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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

//-------------------------------------------------------------------------
/**
 * Enterprise objects that implement this interface will automatically have
 * their data "migrated" such that any new columns that are added and need to
 * have semantically correct values (perhaps derived from values in other
 * columns) will be updated on-demand when the object is first retrieved from
 * the data store.
 *
 * To make use of this functionality, create a new attribute in your EOModel
 * and place "needsMigration = true" in its userInfo dictionary. When the
 * EOGenerator runs on the model, this interface will automatically be
 * implemented by any classes with such attributes. The underscore-prefixed
 * class for the EO will implement its {@link #migrateAttributeValuesIfNeeded}
 * method by simply checking if the each property is null and, if so, invoking
 * its accessor. It is assumed that the non-prefixed subclass will override
 * this accessor to either compute the property (migrating it) or, if already
 * migrated, just return its value.
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public interface MigratoryAttributeOwner
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Called as part of an object's life cycle to compute on-demand any
     * migratory attributes that might be necessary.
     */
    void migrateAttributeValuesIfNeeded();
}
