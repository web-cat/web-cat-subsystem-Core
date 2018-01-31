/*==========================================================================*\
 |  $Id: SubsystemFragmentCollector.java,v 1.4 2012/03/28 13:48:08 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2012 Virginia Tech
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

import com.webobjects.appserver.*;
import com.webobjects.foundation.NSArray;
import org.webcat.core.Application;
import org.webcat.core.SubsystemFragmentCollector;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  Traverses all installed plug-ins and collects components for a given
 *  property.  Used to allow "plug-in" of subsystem informational displays
 *  in pages/components defined elsewhere in the application (like in Core).
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.4 $, $Date: 2012/03/28 13:48:08 $
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

            NSArray<Class<? extends WOComponent>> fragments =
                Application.wcApplication().subsystemManager()
                    .subsystemFragmentsForKey(fragmentKey);

            StringBuffer htmlBuffer = new StringBuffer();
            StringBuffer wodBuffer = new StringBuffer();
            StringBuffer bindingBuffer = new StringBuffer();

            for (String key : bindingKeys())
            {
                if (!key.equals(FRAGMENT_KEY_KEY))
                {
                    bindingBuffer.append(key);
                    bindingBuffer.append("=");
                    bindingBuffer.append(key);
                    bindingBuffer.append(";");
                }
            }

            String bindings = bindingBuffer.toString();

            if (fragments != null)
            {
                int i = 1;
                for (Class<? extends WOComponent> fragmentClass : fragments)
                {
                    String fullName = fragmentClass.getCanonicalName();
                    String simpleName = fragmentClass.getSimpleName();

                    htmlBuffer.append("<wo name=\"");
                    htmlBuffer.append(simpleName);
                    htmlBuffer.append(i);
                    htmlBuffer.append("\"/>\n");

                    wodBuffer.append(simpleName);
                    wodBuffer.append(i);
                    wodBuffer.append(": ");
                    wodBuffer.append(fullName);
                    wodBuffer.append("{");
                    wodBuffer.append(bindings);
                    wodBuffer.append("}\n");

                    i++;
                }
            }

            htmlTemplate = htmlBuffer.toString();
            bindingDefinitions = wodBuffer.toString();

            if (log.isDebugEnabled())
            {
                log.debug("htmlTemplate =\n" + htmlTemplate);
                log.debug("bindingDefinitions =\n" + bindingDefinitions);
            }
        }

        return templateWithHTMLString( null, null,
                htmlTemplate, bindingDefinitions, null,
                Application.application().associationFactoryRegistry(),
                Application.application().namespaceProvider());
    }


    // ----------------------------------------------------------
    public Object valueForKey( String key )
    {
        if (log.isDebugEnabled())
        {
            log.debug( "valueForKey(" + key + ")" );
        }
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
        if (log.isDebugEnabled())
        {
            log.debug( "takeValueForKey(" + value + ", " + key + ")" );
        }
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
