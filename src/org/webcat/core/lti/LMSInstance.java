/*==========================================================================*\
 |  Copyright (C) 2018 Virginia Tech
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

package org.webcat.core.lti;

import static org.webcat.woextensions.ECAction.run;
import java.util.UUID;
import org.webcat.core.AuthenticationDomain;
import org.webcat.woextensions.ECAction;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

// -------------------------------------------------------------------------
/**
 * Represents one LTI consumer registered with this LTI tool.
 *
 * @author Stephen Edwards
 */
public class LMSInstance
    extends _LMSInstance
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new LMSInstance object.
     */
    public LMSInstance()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public String userPresentableDescription()
    {
        return consumerKey();
    }


    // ----------------------------------------------------------
    @Override
    public void willInsert()
    {
        if (consumerKey() == null || consumerKey().isEmpty())
        {
            setConsumerKey(UUID.randomUUID().toString());
        }
        if (consumerSecret() == null || consumerSecret().isEmpty())
        {
            setConsumerSecret(UUID.randomUUID().toString());
        }
        super.willInsert();
    }


    // ----------------------------------------------------------
    public static String lmsNameFor(LMSInstance instance)
    {
        AuthenticationDomain d = null;
        LMSType t = null;
        if (instance != null)
        {
            d = instance.authenticationDomain();
            t = instance.lmsType();
        }
        return lmsNameFor(d, t);
    }


    // ----------------------------------------------------------
    public static String lmsNameFor(
        AuthenticationDomain institution, LMSType type)
    {
        String iName = null;
        String tName = null;
        if (institution != null)
        {
            iName = institution.name();
        }
        if (type != null)
        {
            tName = type.name();
        }
        return lmsNameFor(iName, tName);
    }


    // ----------------------------------------------------------
    public static String lmsNameFor(String institution, String type)
    {
        String name = null;
        if (institution != null)
        {
            name = institution + " ";
            if (type != null)
            {
                name += type;
            }
            else
            {
                name += "LMS";
            }
        }
        else if (type != null)
        {
            name = type;
        }
        else
        {
            name = "Course Management System";
        }
        return name;
    }


    // ----------------------------------------------------------
    public static void ensureLMSInstancesForInstitutions()
    {
        log.debug("ensureLMSInstancesForInstitutions()");
        LMSType.ensureDefaultLMSTypes();
        run(new ECAction() { public void action() {
            ec.setSharedEditingContext(null);
            NSMutableArray<AuthenticationDomain> institutions =
                AuthenticationDomain.allObjects(ec).mutableClone();
            NSArray<LMSInstance> instances = allObjects(ec);
            for (LMSInstance lms : instances)
            {
                if (lms.authenticationDomain() != null)
                {
                    institutions.removeObject(lms.authenticationDomain());
                }
            }

            if (institutions.size() > 0)
            {
                LMSType defaultType = LMSType.allObjects(ec).get(0);
                for (AuthenticationDomain institution : institutions)
                {
                    create(ec,
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        institution,
                        defaultType);
                }
                ec.saveChanges();
            }
        }});
        log.debug("ensureLMSInstancesForInstitutions() finished");
    }

}
