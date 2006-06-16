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
import java.util.Iterator;
import net.sf.webcat.FeatureDescriptor;
import net.sf.webcat.WCServletAdaptor;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  The subsystem interface that defines the API used by the Core to
 *  communicate with subsystems.
 *
 *  @author Stephen Edwards
 *  @version $Id$
 */
public class Subsystem
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new Subsystem object.
     */
    public Subsystem()
    {
        // Nothing to initialize here
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Get the short (one-word) human-readable name for this
     * subsystem.
     * 
     * @return The short name
     */
    public String name()
    {
        return name;
    }


    // ----------------------------------------------------------
    /**
     * Set the short (one-word) human-readable name for this subsystem.
     * @param newName the name to use
     */
    public void setName( String newName )
    {
        name = newName;
    }


    // ----------------------------------------------------------
    /**
     * Get the FeatureDescriptor for this subsystem.
     * @return this subsystem's descriptor
     */
    public FeatureDescriptor descriptor()
    {
        if ( descriptor == null )
        {
            // First, look to see if there is an appropriate subsystem updater
            WCServletAdaptor adaptor = WCServletAdaptor.getInstance();
            if ( adaptor != null )
            {
                for ( Iterator i = adaptor.subsystems().iterator();
                      i.hasNext(); )
                {
                    FeatureDescriptor sd = (FeatureDescriptor)i.next();
                    if ( name.equals( sd.name() ) )
                    {
                        // found it!
                        descriptor = sd;
                        break;
                    }
                }
            }
            // Otherwise, try to create one directly from properties
            if ( descriptor == null )
            {
                descriptor = new FeatureDescriptor( name(),
                    Application.configurationProperties(), false );
            }
        }
        return descriptor;
    }


    // ----------------------------------------------------------
    /**
     * Get a list of names of the database tables required by
     * this subsystem.  Tables from the Core are not listed.  If no
     * tables from other subsystems are needed, this method returns
     * null.
     * 
     * @return The list of names, as strings
     */
    public NSArray requiredTableNames()
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Get a list of names of the database tables provided by
     * this subsystem.  If no tables are provided by EOModel(s) in
     * this subsystem, this method returns null.
     * 
     * @return The list of names, as strings
     */
    public NSArray providedTableNames()
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Get a list of WO components that should be instantiated and presented
     * on the front page.
     * 
     * @return The list of names, as strings
     */
    public NSArray frontPageStatusComponents()
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Get a list of in-jar paths of the EOModels contained in
     * this subsystem's jar file.  If no EOModel(s) are contained
     * in this subsystem, this method returns null.
     * 
     * @return The list of paths, as strings
     */
    public NSArray EOModelPathsInJar()
    {
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Initialize the subsystem-specific session data in a newly created
     * session object.  This method is called once by the core for
     * each newly created session object.
     * 
     * @param s The new session object
     */
    public void initializeSessionData( Session s )
    {
        // Subclasses should override this as necessary
    }


    // ----------------------------------------------------------
    /**
     * Generate the component definitions and bindings for a given
     * pre-defined information fragment, so that the result can be
     * plugged into other pages defined elsewhere in the system.
     * @param fragmentKey the identifier for the fragment to generate
     *        (see the keys defined in {@link SubsystemFragmentCollector}
     * @param htmlBuffer add the html template for the subsystem's fragment
     *        to this buffer
     * @param wodBuffer add the binding definitions (the .wod file contents)
     *        for the subsystem's fragment to this buffer
     */
    public void collectSubsystemFragments(
        String fragmentKey, StringBuffer htmlBuffer, StringBuffer wodBuffer )
    {
        // Subclasses should override this as necessary
    }


    // ----------------------------------------------------------
    /**
     * Handle a direct action request.  The user's login session will be
     * passed in as well.
     *
     * @param request the request to respond to
     * @param session the user's session
     * @param context the context for this request
     * @return The response page or contents
     */
    public WOActionResults handleDirectAction(
            WORequest request,
            Session   session,
            WOContext context )
    {
        throw new RuntimeException(
            "invalid subsystem direct action request: "
            + "\n---request---\n" + request
            + "\n\n---session---\n" + session
            + "\n\n---context---\n" + context
            );
    }


    //~ Instance/static variables .............................................

    private String name = getClass().getName();
    private FeatureDescriptor descriptor;
    static Logger log = Logger.getLogger( Subsystem.class );
}
