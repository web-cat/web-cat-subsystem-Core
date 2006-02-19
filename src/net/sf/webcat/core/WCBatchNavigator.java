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
import com.webobjects.foundation.*;
import java.util.*;

//-------------------------------------------------------------------------
/**
 *  A custom version of a batch navigator that has a different look than
 *  the one in WOExtensions.
 *
 *  @author Stephen Edwards
 *  @version $Id$
 */
public class WCBatchNavigator
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new WOBatchNavigationBar object.
     * 
     * @param aContext the context
     */
    public WCBatchNavigator( WOContext aContext )
    {
        super( aContext );
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Set the page number (batch number) to display.
     * 
     * @param index The page number
     */
    public void setBatchIndex( Integer index )
    {
        int batchIndex;

        //Treat a null index as a 0 index. Negative numbers are handled
        //by the display group.
        batchIndex = ( index != null )
                          ? index.intValue()
                          : 0;
        ( (WODisplayGroup)valueForBinding( "displayGroup" ) ).
            setCurrentBatchIndex( batchIndex );
    }


    // ----------------------------------------------------------
    /**
     * Set the number of objects shown on each page/batch.
     * 
     * @param number The number of objects to show
     */
    public void setNumberOfObjectsPerBatch( Integer number )
    {
        int num;

        //If a negative number is provided we default the number
        //of objects per batch to 0.
        num = ( ( number != null ) && ( number.intValue() > 0 ) )
                      ? number.intValue()
                      : 0;
        ( (WODisplayGroup)valueForBinding( "displayGroup" ) ).
            setNumberOfObjectsPerBatch( num );
    }


    // ----------------------------------------------------------
    public boolean isStateless()
    {
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Access the bound display group's current batch position.
     * 
     * @return The current batch index
     */
    public int batchIndex()
    {
        return ( (WODisplayGroup)valueForBinding( "displayGroup" ) ).
            currentBatchIndex();
    }


    // ----------------------------------------------------------
    /**
     * The current number of objects displayed for each page/batch.
     * 
     * @return The number of objects per batch
     */
    public int numberOfObjectsPerBatch()
    {
        return ( (WODisplayGroup)valueForBinding( "displayGroup" ) ).
            numberOfObjectsPerBatch();
    }
}
