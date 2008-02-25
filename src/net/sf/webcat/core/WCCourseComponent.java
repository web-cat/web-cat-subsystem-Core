/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License
 |  along with Web-CAT; if not, write to the Free Software
 |  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 |
 |  Project manager: Stephen Edwards <edwards@cs.vt.edu>
 |  Virginia Tech CS Dept, 660 McBryde Hall (0106), Blacksburg, VA 24061 USA
\*==========================================================================*/

package net.sf.webcat.core;

import com.webobjects.appserver.*;

import org.apache.log4j.*;

//-------------------------------------------------------------------------
/**
 * This specialized subclass of WCComponent represents pages that have
 * a notion of a currently-selected course offering and/or course.
 *
 * @author Stephen Edwards
 * @version $Id$
 */
public class WCCourseComponent
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new object.
     *
     * @param context The page's context
     */
    public WCCourseComponent( WOContext context )
    {
        super( context );
    }

    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    /**
     * Grab user's current selections when waking, if necessary.
     */
    @Override
    public void awake()
    {
        if (log.isDebugEnabled())
        {
            log.debug("awake(): begin " + getClass().getName());
        }
        super.awake();
        if (csm == null)
        {
            Object inheritedCsm = transientState().valueForKey( CSM_KEY );
            if (inheritedCsm == null)
            {
                csm = new CoreSelectionsManager(
                    user().getMyCoreSelections(), ecManager());
            }
            else
            {
                csm = (CoreSelectionsManager)
                    ((CoreSelectionsManager)inheritedCsm).clone();
            }
        }
        if (log.isDebugEnabled())
        {
            log.debug("awake(): end " + getClass().getName());
        }
    }


    // ----------------------------------------------------------
    /**
     * Access the user's current core selections.
     * @return the core selections manager for this page
     */
    public CoreSelectionsManager coreSelections()
    {
        return csm;
    }


    // ----------------------------------------------------------
    @Override
    public WOComponent pageWithName( String name )
    {
        if (csm != null)
        {
            transientState().takeValueForKey( csm, CSM_KEY );
        }
        WOComponent result = super.pageWithName( name );
        return result;
    }


    //~ Private Methods .......................................................

    // ----------------------------------------------------------
    private IndependentEOManager.ECManager ecManager()
    {
        IndependentEOManager.ECManager result = (IndependentEOManager.ECManager)
            transientState().valueForKey(ECMANAGER_KEY);
        if (result == null)
        {
            result = new IndependentEOManager.ECManager();
            transientState().takeValueForKey(result, ECMANAGER_KEY);
        }
        return result;
    }


    //~ Instance/static variables .............................................

    private CoreSelectionsManager csm;
    private static final String CSM_KEY =
        CoreSelectionsManager.class.getName();
    private static final String ECMANAGER_KEY =
        IndependentEOManager.ECManager.class.getName();

    static Logger log = Logger.getLogger( WCCourseComponent.class );
}
