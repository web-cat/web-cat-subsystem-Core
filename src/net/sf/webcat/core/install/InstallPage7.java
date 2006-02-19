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

package net.sf.webcat.core.install;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import java.util.Calendar;

import net.sf.webcat.core.*;

import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Implements the login UI functionality of the system.
 *
 *  @author Stephen Edwards
 *  @version $Id$
 */
public class InstallPage7
    extends InstallPage
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new PreCheckPage object.
     * 
     * @param context The context to use
     */
    public InstallPage7( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public NSArray periods = new NSArray( Semester.integers );
    public Integer period;
    public Integer selectedPeriod;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public int stepNo()
    {
        return 7;
    }


    // ----------------------------------------------------------
    public void setDefaultConfigValues( WCConfigurationFile configuration )
    {
        java.util.Calendar now = new java.util.GregorianCalendar();
        int thisYear = now.get( java.util.Calendar.YEAR );
        int thisMonth = now.get( java.util.Calendar.MONTH ) + 1;
        int semester = Semester.defaultSemesterFor( now );
        selectedPeriod = Semester.integers[semester];
        int startMonth = Semester.defaultStartingMonth( semester );
        int startYear = thisYear;
        if ( startMonth > thisMonth ) startYear--;
        int endMonth = Semester.defaultEndingMonth( semester );
        int endYear = thisYear;
        if ( endMonth < thisMonth ) endYear++;
        int startDay = 1;
        int endDay = 30;
        switch ( endMonth )
        {
            case 2: endDay = 28; break;
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12: endDay = 31; break;
        }

        setConfigDefault( configuration, "StartDate",
            "" + startMonth + "/" + startDay + "/" + startYear );
        setConfigDefault( configuration, "EndDate",
            "" + endMonth + "/" + endDay + "/" + endYear );
    }


    // ----------------------------------------------------------
    public String periodName()
    {
        return (String)Semester.names.objectAtIndex( period.intValue() );
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( InstallPage7.class );
}
