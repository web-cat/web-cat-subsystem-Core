/*==========================================================================*\
 |  $Id: TabDescriptorTest.java,v 1.2 2011/03/07 18:45:00 stedwar2 Exp $
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

package org.webcat.core.tests;

import com.webobjects.foundation.*;
import org.webcat.core.TabDescriptor;


// -------------------------------------------------------------------------
/**
 *  Test class for TabDescriptor.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2011/03/07 18:45:00 $
 */
public class TabDescriptorTest
    extends com.codefab.wounittest.WOUTTestCase
{
    //~ Test case setup .......................................................

    // ----------------------------------------------------------
    /**
     * Create a sample tab set to use for testing.
     * @throws Exception
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        tabs = new TabDescriptor( "TBDPage", "root" );
        tabs.addChildren( new NSArray<TabDescriptor>(
            new TabDescriptor( "TBDPage", "Home", 0, 10, true,
                new NSArray<TabDescriptor>(
                    new TabDescriptor( "TBDPage", "My Profile", 0, 1001, true,
                        new NSArray<TabDescriptor>( new TabDescriptor[] {
                            new TabDescriptor( "TBDPage",
                                               "Pick the assignment",
                                               0, 1 ),
                            new TabDescriptor( "TBDPage",
                                               "Upload your file(s)",
                                               0, 2, true ),
                            new TabDescriptor( "TBDPage",
                                               "Confirm your submission",
                                               0, 3 )
                        } ), null, null ) ), null, null ) ) );
        System.out.println( "Before selecting default:\n" + tabs );
        tabs.selectDefault();
    }


    //~ Test cases ............................................................

    public void testDefaultSelection()
    {
        System.out.println( tabs );
        assertTrue( tabs.isSelected() );
        assertTrue( tabs.childAt( 0 ).isSelected() );
        assertEquals( 1, tabs.children().count() );
        assertEquals( 1, tabs.childAt( 0 ).children().count() );
        assertTrue(  tabs.childAt( 0 ).childAt( 0 ).isSelected() );
        assertEquals( 3, tabs.childAt( 0 ).childAt( 0 ).children().count() );
        assertTrue( !tabs.childAt( 0 ).childAt( 0 ).childAt( 0 ).isSelected() );
        assertTrue(  tabs.childAt( 0 ).childAt( 0 ).childAt( 1 ).isSelected() );
        assertTrue( !tabs.childAt( 0 ).childAt( 0 ).childAt( 2 ).isSelected() );
    }

    //~ Instance/static variables .............................................
    private TabDescriptor tabs;
}
