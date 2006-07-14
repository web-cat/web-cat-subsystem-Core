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
import er.extensions.ERXValueUtilities;
import org.apache.log4j.Logger;

// -------------------------------------------------------------------------
/**
 *  This component presents a panel for editing a set of options, such as
 *  configuration options.  The surrounding page should be a {@link WizardPage}
 *  that defines {@link WizardPage#defaultAction()} to return null so that the
 *  current page will be reloaded on basic form submissions.
 *
 *  @author  stedwar2
 *  @version $Id$
 */
public class OptionSetEditor
    extends WCComponent
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new OptionSetEditor object.
     *
     * @param context The context to use
     */
    public OptionSetEditor( WOContext context )
    {
        super( context );
    }


    //~ KVC Attributes (must be public) .......................................

    // For clients to configure this component
    public NSArray             options;
    public NSMutableDictionary optionValues;
    public NSArray             categories;
    public String              verboseOptionsKey = "verboseOptions";
    public String              browsePageName;
    public java.io.File        base;

    // For communicating with subcomponents ...
    public NSDictionary        option;
    public String              category;
    public String              chosenCategory;
    public String              displayedCategory;


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public void appendToResponse( WOResponse response, WOContext context )
    {
        log.debug( "appendToResponse()" );
        if ( isFirstView
             && categories != null
             && categories.count() > 0 )
        {
            chosenCategory = (String)categories.objectAtIndex( 0 );
        }
        isFirstView = false;
        terse = null;
        displayedCategory = chosenCategory;
        super.appendToResponse( response, context );
    }


    // ----------------------------------------------------------
    /**
     * Determine if the current option should be shown, because it is part
     * of the currently selected category.  Intended for use via KVC in
     * generating this page's HTML.
     * @return true if this option should be shown
     */
    public boolean showThisOption()
    {
        return displayedCategory == null
           || displayedCategory.equals( option.valueForKey( "category" ) );
    }


    // ----------------------------------------------------------
    /**
     * Toggle whether or not the user wants verbose descriptions of options
     * to be shown or hidden.  The setting is stored in the user's preferences
     * under the key specified by the verboseOptionsKey, and will be
     * permanently saved the next time the session's local changes are saved.
     */
    public void toggleVerboseOptions()
    {
        boolean verboseOptions = ERXValueUtilities.booleanValue(
            wcSession().userPreferences.objectForKey( verboseOptionsKey ) );
        verboseOptions = !verboseOptions;
        wcSession().userPreferences.setObjectForKey(
            Boolean.valueOf( verboseOptions ), verboseOptionsKey );
    }


    // ----------------------------------------------------------
    /**
     * Look up the user's preferences and determine whether or not to show
     * verbose option descriptions in this component.
     * @return true if verbose descriptions should be hidden, or false if
     * they should be shown
     */
    public Boolean terse()
    {
        if ( terse == null )
        {
            terse = ERXValueUtilities.booleanValue(
                wcSession().userPreferences.objectForKey( verboseOptionsKey ) )
                ? Boolean.TRUE : Boolean.FALSE;
        }
        return terse;
    }


    //~ Instance/static variables .............................................

    private Boolean terse;
    private boolean isFirstView = true;
    static Logger log = Logger.getLogger( OptionSetEditor.class );
}
