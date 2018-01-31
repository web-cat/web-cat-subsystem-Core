/*==========================================================================*\
 |  $Id: WCPropertiesTest.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2008 Virginia Tech
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

import org.webcat.core.*;
import com.webobjects.foundation.*;

import org.webcat.core.WCProperties;

// -------------------------------------------------------------------------
/**
 *  Test class for WCProperties.  These test cases focus mostly on
 *  property expansion/substitution within getProperty.
 *
 *  @author  Stephen Edwards
 *  @version $Id: WCPropertiesTest.java,v 1.1 2010/05/11 14:51:59 aallowat Exp $
 */
public class WCPropertiesTest
extends com.codefab.wounittest.WOUTTestCase
{
    //~ Test case setup .......................................................

    // ----------------------------------------------------------
    /**
     * Create a sample property files to use for testing.
     * @throws Exception
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();
        properties = new WCProperties();
        properties.setProperty( "key1", "value1" );
        properties.setProperty( "key2", "${key1} value2" );
        properties.setProperty( "key3", "${key1} hello ${key2}" );
        properties.setProperty( "infinite.recursion", "${infinite.recursion}" );
        properties.setProperty( "no.value", "${undefined.key}" );
    }


    //~ Test cases ............................................................

    public void testKey1()
    {
        assertEquals( "value1", properties.getProperty( "key1" ) );
    }

    public void testKey2()
    {
        assertEquals( "value1 value2", properties.getProperty( "key2" ) );
    }

    public void testKey3()
    {
        assertEquals( "value1 hello value1 value2",
                      properties.getProperty( "key3" ) );
    }

    public void testInfiniteRecursion()
    {
        assertEquals( "${infinite.recursion:RECURSION-TOO-DEEP}",
                      properties.getProperty( "infinite.recursion" ) );
    }

    public void testNoValue()
    {
        assertEquals( "${undefined.key}",
                      properties.getProperty( "no.value" ) );
    }

    //~ Instance/static variables .............................................
    private WCProperties properties;
}
