/*==========================================================================*\
 |  $Id: SubsystemEOMRedirector.java,v 1.2 2011/03/07 18:44:37 stedwar2 Exp $
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

package org.webcat.core;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/*
 * Looks up EO classes referenced in EOModels; serves as a delegate for the
 * default EOModelGroup.
 *
 * <p>
 * The main operation defined here is
 * {@link #failedToLookupClassNamed(EOEntity,String)}.  The rest of the
 * <code>EOModelGroup.Delegate</code> functions implemented here just
 * return null or some default value.
 * </p>
 *
 * @author Lally Singh
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2011/03/07 18:44:37 $
 */
public class SubsystemEOMRedirector
    implements EOModelGroup.Delegate
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Loads an EO class from the Web-CAT {@link DelegatingUrlClassLoader}.
     * This method is called when the EOModelGroup couldn't find a class.
     * We load it here and return it, using the DelegatingUrlClassLoader.
     *
     * @param entity
     * @param className The class to find
     * @return          The resulting <code>Class</code> object
     */
    public Class<?> failedToLookupClassNamed(EOEntity entity, String className)
    {
        try
        {
            return DelegatingUrlClassLoader.getClassLoader().loadClass(
                           className);
        }
        catch (Exception e)
        {
            log.error("Couldn't load class with name: " + className);
        }
        return null;
    }


    // ----------------------------------------------------------
    public Class<?> classForObjectWithGlobalID(EOEntity entity,
                                               EOGlobalID gid)
    {
        return null;
    }


    // ----------------------------------------------------------
    public EOModel modelGroupEntityNamed(EOModelGroup modelGroup,
                                         String name)
    {
        return null;
    }


    // ----------------------------------------------------------
    public EOEntity relationshipFailedToLookupDestinationWithName(
             EORelationship relationship,
             String         name)
    {
        return null;
    }


    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public EORelationship relationshipForRow(EOEntity entity,
                                             NSDictionary dictionary,
                                             EORelationship relationship)
    {
        return relationship;
    }


    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public EOEntity subEntityForEntity(EOEntity entity, NSDictionary dic)
    {
        return null;
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger(SubsystemEOMRedirector.class);
}
