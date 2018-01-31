/*==========================================================================*\
 |  $Id: Language.java,v 1.3 2014/06/16 16:00:12 stedwar2 Exp $
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

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

import org.webcat.core.Language;
import org.webcat.core._Language;
import org.apache.log4j.*;

// -------------------------------------------------------------------------
/**
 * Represents a supported programming language in this Web-CAT installation.
 *
 * @author Stephen Edwards
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.3 $, $Date: 2014/06/16 16:00:12 $
 */
public class Language
    extends _Language
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Language object.
     */
    public Language()
    {
        super();
    }


    //~ Methods ...............................................................


    // ----------------------------------------------------------
    /**
     * Get a list of shared authentication domain objects that have
     * already been loaded into the shared editing context.
     * @return an array of all AuthenticationDomain objects
     */
    public static NSArray<Language> languages()
    {
        if (languages == null)
        {
            refreshLanguages();
        }
        return languages;
    }


    // ----------------------------------------------------------
    /**
     * Force reloading of the list of shared authentication domain objects.
     */
    public static void refreshLanguages()
    {
        log.debug("refreshing shared language objects");
        languages = allObjects(
            EOSharedEditingContext.defaultSharedEditingContext());
    }


    //~ Instance/static variables .............................................
    private static NSArray<Language> languages;

    static Logger log = Logger.getLogger(Language.class);
}
