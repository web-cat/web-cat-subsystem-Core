/*==========================================================================*\
 |  $Id$
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

package net.sf.webcat.core;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  Traverses all installed plug-ins and collects components for a given
 *  property.  Used to allow "plug-in" of subsystem informational displays
 *  in pages/components defined elsewhere in the application (like in Core).
 *
 *  @author  Stephen Edwards
 *  @version $Id$
 */
public class SubsystemFragmentCollector
    extends WOComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new SubsystemFragmentCollector object.
     *
     * @param context The page's context
     */
    public SubsystemFragmentCollector( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    public static final String HOME_STATUS_KEY        = "homeStatus";
    public static final String SYSTEM_STATUS_ROWS_KEY = "systemStatusRows";
    public static final String FRAGMENT_KEY_KEY       = "fragmentKey";
    public String fragmentKey;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public WOElement template()
    {
        if ( htmlTemplate == null )
        {
            log.debug( "initializing templates" );
            StringBuffer htmlBuffer = new StringBuffer();
            StringBuffer wodBuffer = new StringBuffer();
            ( (Application)Application.application() ).subsystemManager().
                collectSubsystemFragments( fragmentKey, htmlBuffer, wodBuffer );
            htmlTemplate = htmlBuffer.toString();
            log.debug( "htmlTemplate =\n" + htmlTemplate );
            bindingDefinitions = wodBuffer.toString();
            log.debug( "bindingDefinitions =\n" + bindingDefinitions );
        }
        return templateWithHTMLString( htmlTemplate, bindingDefinitions, null );
    }


    // ----------------------------------------------------------
    public Object valueForKey( String key )
    {
        log.debug( "valueForKey(" + key + ")" );
        if ( key.equals( FRAGMENT_KEY_KEY ) )
        {
            return fragmentKey;
        }
        else
        {
            return valueForBinding( key );
        }
    }


    // ----------------------------------------------------------
    public void takeValueForKey( Object value, String key )
    {
        log.debug( "takeValueForKey(" + value + ", " + key + ")" );
        if ( key.equals( FRAGMENT_KEY_KEY ) )
        {
            fragmentKey = (String)value;
        }
        else
        {
            setValueForBinding( value, key );
        }
    }


    //~ Instance/static variables .............................................

    private String htmlTemplate;
    private String bindingDefinitions;

    static Logger log = Logger.getLogger( SubsystemFragmentCollector.class );
}
