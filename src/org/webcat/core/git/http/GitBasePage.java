/*==========================================================================*\
 |  $Id: GitBasePage.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011-2012 Virginia Tech
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

package org.webcat.core.git.http;

import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Constants;
import org.webcat.core.Application;
import org.webcat.core.Session;
import org.webcat.core.StatusPage;
import org.webcat.core.WCComponent;
import org.webcat.core.git.GitRef;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

//-------------------------------------------------------------------------
/**
 * TODO real description
 *
 * @author  Tony Allevato
 * @author  Last changed by $Author: stedwar2 $
 * @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class GitBasePage
    extends GitWebComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public GitBasePage(WOContext context)
    {
        super(context);
    }


    //~ KVC attributes (must be public) .......................................

    public WCComponent thisPage;

    public NSArray<GitRef> allBranchesAndTags;
    public GitRef aBranchOrTag;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Set up this component for this response.
     * @see com.webobjects.appserver.WOComponent#awake()
     */
    public void awake()
    {
        super.awake();

        if (thisPage == null)
        {
            WOComponent comp = context().page();

            // initialize thisPage if needed
            if (comp instanceof WCComponent)
            {
                thisPage = (WCComponent) comp;
            }
            else
            {
                thisPage = null; // Will probably force a dirty crash

                if (log.isDebugEnabled())
                {
                    log.debug("top-level component "
                        + (comp == null ? "<null>" : comp.getClass().getName())
                        + " is not a WCComponent");
                }
            }
        }
    }


    // ----------------------------------------------------------
    public void appendToResponse(WOResponse response, WOContext context)
    {
        if (allBranchesAndTags == null)
        {
            NSMutableArray<GitRef> allRefs = new NSMutableArray<GitRef>();

            allRefs.addObjectsFromArray(gitContext().repository().headRefs());
            allRefs.addObjectsFromArray(gitContext().repository().tagRefs());

            allBranchesAndTags = allRefs;
        }

        super.appendToResponse(response, context);
    }


    // ----------------------------------------------------------
    public GitBasePage thisPage()
    {
        return this;
    }


    // ----------------------------------------------------------
    public boolean shouldSelectSourceMenuItem()
    {
        GitWebMode type = gitContext().mode();

        return (type == GitWebMode.BLOB || type == GitWebMode.TREE);
    }


    // ----------------------------------------------------------
    public boolean shouldSelectCommitsMenuItem()
    {
        GitWebMode type = gitContext().mode();

        return (type == GitWebMode.COMMIT || type == GitWebMode.COMMITS);
    }


    // ----------------------------------------------------------
    public boolean shouldSelectBranchesMenuItem()
    {
        GitWebMode type = gitContext().mode();

        return (type == GitWebMode.BRANCHES);
    }


    // ----------------------------------------------------------
    public WOActionResults goToHomePage()
    {
        return pageWithName(StatusPage.class.getCanonicalName());
    }


    // ----------------------------------------------------------
    public WOActionResults goToProfile()
    {
        return pageWithName(
                ((Session) session()).tabs.selectById("Profile").pageName());
    }


    // ----------------------------------------------------------
    public WOActionResults logout()
    {
        if (((Session) session()).user() != null)
        {
            log.info("user "
                      + ((Session) session()).user().userName()
                      + " logging out");

            ((Session) session()).userLogout();
        }

        return Application.wcApplication().gotoLoginPage(context());
    }


    // ----------------------------------------------------------
    public String commitsMenuURL()
    {
        GitWebContext newContext = gitContext().clone();
        newContext.setMode(GitWebMode.COMMITS);
        return newContext.toURL(context());
    }


    // ----------------------------------------------------------
    public String sourceMenuURL()
    {
        GitWebContext newContext = gitContext().clone();
        newContext.setMode((newContext.repository().typeOfObject(
                newContext.objectId()) == Constants.OBJ_BLOB) ?
                        GitWebMode.BLOB : GitWebMode.TREE);
        return newContext.toURL(context());
    }


    // ----------------------------------------------------------
    public String branchesMenuURL()
    {
        GitWebContext newContext = gitContext().clone();
        newContext.setMode(GitWebMode.BRANCHES);
        newContext.setHeadRef(null);
        return newContext.toURL(context());
    }


    // ----------------------------------------------------------
    public String aBranchOrTagURL()
    {
        GitWebContext newContext = gitContext().clone();
        newContext.setHeadRef(aBranchOrTag);
        return newContext.toURL(context());
    }


    //~ Static/instance variables .............................................

    private static final Logger log = Logger.getLogger(GitBasePage.class);
}
