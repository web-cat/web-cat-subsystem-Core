/*==========================================================================*\
 |  $Id: DelegatingUrlClassLoader.java,v 1.2 2012/03/28 13:48:08 stedwar2 Exp $
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

import java.net.URL;
import java.net.URLClassLoader;
import org.webcat.core.DelegatingUrlClassLoader;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 * The central loading point for all Web-CAT subsystem classes.
 * [Note--this class is intended for supporting plugin-style jar'ed
 * subsystems instead of WebObjects Framework-based subsystems.  Since we
 * aren't using such jar'ed subsystems at present, this is really just a
 * shell for future expandability/flexibility.]
 *
 * <p>
 * Web-CAT needs a centralized class loader for all the JAR files to simplify
 * life, since class names are stored in the database and we need a way to
 * instantiate them.  We can't use the system class loader, since it offers
 * no way to "plug-in" additional class loaders.
 * </p>
 *
 * <p>
 * Instead, we have this delegating class loader.  It searches through all
 * URLs (as specified through addURL()) to find a class.  If it isn't found,
 * then the System class loader is consulted.  If it still isn't found, the
 * class wasn't loaded.
 * </p>
 *
 * <p>
 * Additionally, to keep Web-CAT simple, we only have one of these class
 * loaders running around.  This allows a single class loader to be used to
 * load all classes used in Web-CAT.
 * </p>
 *
 *  @author  Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2012/03/28 13:48:08 $
 */
public class DelegatingUrlClassLoader
    extends URLClassLoader
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Keep anyone from instantiating this class loader.
     */
    protected DelegatingUrlClassLoader()
    {
        super( new URL[] {  },
               // ClassLoader.getSystemClassLoader()
               // Modified to work with WO 5.2 WOBootstrap code
               // Thread.currentThread().getContextClassLoader()
               DelegatingUrlClassLoader.class.getClassLoader()
               );

        if (log.isDebugEnabled())
        {
            log.debug( "constructor: my chain" );
            ClassLoader c = this;
            while ( c != null )
            {
                log.debug( "\tloader => " + c );
                c = c.getParent();
            }
            log.debug( "from class:" );
            c = DelegatingUrlClassLoader.class.getClassLoader();
            while ( c != null )
            {
                log.debug( "\tloader => " + c );
                c = c.getParent();
            }
            log.debug( "from thread:" );
            c = Thread.currentThread().getContextClassLoader();
            while ( c != null )
            {
                log.debug( "\tloader => " + c );
                c = c.getParent();
            }
        }
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * @return Singleton instance of the delegating URL class loader.
     */
    public static DelegatingUrlClassLoader getClassLoader()
    {
        if ( me == null )
        {
//            log.debug( "Before creation, from class:" );
//            ClassLoader c = DelegatingUrlClassLoader.class.getClassLoader();
//            while ( c != null )
//            {
//                log.debug( "\tloader => " + c );
//                c = c.getParent();
//            }
            me = new DelegatingUrlClassLoader();
        }
        return me;
    }


    // ----------------------------------------------------------
    /**
     * Add a URL to the list of URLs searched for classes.
     *
     * @param u The URL to add.
     */
    public void addURL( URL u )
    {
        super.addURL( u );
    }


    //~ Instance/static variables .............................................

    private static DelegatingUrlClassLoader me;
    static Logger log = Logger.getLogger( DelegatingUrlClassLoader.class );
}
