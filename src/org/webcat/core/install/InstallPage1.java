/*==========================================================================*\
 |  $Id: InstallPage1.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
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

package org.webcat.core.install;

import com.webobjects.appserver.*;
import org.webcat.core.Application;
import org.webcat.core.WCConfigurationFile;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * Implements the login UI functionality of the system.
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class InstallPage1
    extends InstallPage
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new PreCheckPage object.
     *
     * @param context The context to use
     */
    public InstallPage1( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................



    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public int stepNo()
    {
        return 1;
    }


    // ----------------------------------------------------------
    public void setDefaultConfigValues( WCConfigurationFile configuration )
    {
        if ( configuration.getProperty( "installComplete" ) != null )
        {
            configuration.remove( "installComplete" );
        }
        super.setDefaultConfigValues( configuration );
    }


    // ----------------------------------------------------------
    public boolean configExists()
    {
        return Application.configurationProperties().exists();
    }


    // ----------------------------------------------------------
    public String configLocation()
    {
        try
        {
            return Application.configurationProperties().file()
                .getCanonicalPath();
        }
        catch ( java.io.IOException e )
        {
            log.error( "exception looking up configuration file location:", e );
            return e.getMessage();
        }
    }


    // ----------------------------------------------------------
    public boolean configIsWriteable()
    {
        return Application.configurationProperties().isWriteable();
    }


    //~ Instance/static variables .............................................

    static Logger log = Logger.getLogger( InstallPage1.class );
}
