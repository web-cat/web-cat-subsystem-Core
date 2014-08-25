/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2014 Virginia Tech
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

import static org.webcat.woextensions.ECAction.run;
import org.apache.log4j.Logger;
import org.webcat.woextensions.ECAction;
import org.webcat.woextensions.WCFetchSpecification;
import com.webobjects.foundation.NSArray;

//-------------------------------------------------------------------------
/**
 * The Core subsystem class.
 *
 * @author  edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class Core
    extends Subsystem
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Create a new Core object.
     */
    public Core()
    {
        super();
    }

    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    protected void performPeriodicMaintenance()
    {
        run(new ECAction() {
            // ----------------------------------------------------------
            @Override
            public void action()
            {
                WCFetchSpecification<User> needsMigration =
                    new WCFetchSpecification<User>(
                        User.ENTITY_NAME,
                        User.salt.isNull()
                            .and(User.password.isNotNull())
                            .and(User.password.ne("")),
                        null);
                needsMigration.setRefreshesRefetchedObjects(true);
                needsMigration.setFetchLimit(500);

                NSArray<User> migrated = User
                    .objectsWithFetchSpecification(ec, needsMigration);
                while (migrated.size() > 0)
                {
                    log.info("performPeriodicMaintenance(): migrated "
                        + migrated.size() + " users");
                    try
                    {
                        // Sleep for 2 seconds
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e)
                    {
                        // ignore
                    }
                    migrated = User
                        .objectsWithFetchSpecification(ec, needsMigration);
                }
            }
        });
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger(Core.class);
}
