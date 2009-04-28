/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech
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

dojo.provide("webcat.TitlePane");

dojo.require("dijit.TitlePane");

// ------------------------------------------------------------------------
/**
 * See the webcat.ContentPane class for an explanation of these overrides. This
 * class is slightly different in order to handle the expansion capabilities of
 * the Dijit TitlePane, but the concept is the same.
 *
 * @author Tony Allevato
 * @version $Id$
 */
dojo.declare("webcat.TitlePane", dijit.TitlePane,
{
    //~ Properties ............................................................
    
    /* Set to true when the pane is initially starting up, otherwise false. */
    initialStartup: true,

    /* Unlike webcat.ContentPane, which is intended to be used as low-level
       layout containers for controls that should appear to update "instantly"
       and therefore do not show a loading message by default when they are
       refreshed, the webcat.TitlePane does show the loading message by default
       when it is updated. Set this attribute to false to change this
       behavior. */
    showsLoadingMessageOnRefresh: true,

    /* Set to true only when the title pane is toggling between opened and
       closed states. */
    isToggling: false,


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    startup: function()
    {
        this.initialStartup = true;
        this.inherited(arguments);
        this.initialStartup = false;

        // If the title pane started in an open state, then we go ahead and
        // set the isLoaded attribute to true because the pane will have been
        // "loaded" with component content. This prevents a situation where
        // the pane starts out open with content, the user closes and reopens
        // the pane, and the pane reloads the content from the server because
        // the initial content never marked the pane as actually "loaded".

        if (this.open)
        {
            this.isLoaded = true;
        }
    },
    
    
    // ----------------------------------------------------------
    _loadCheck: function(/* Boolean */ forceLoad)
    {
        if (!this.initialStartup)
        {
            this.inherited(arguments);
        }
    },


    // ----------------------------------------------------------   
    toggle: function()
    {
        this.isToggling = true;
        this.inherited(arguments);
        this.isToggling = false;
    },


    // ----------------------------------------------------------   
    _setContent: function(content, isFakeContent)
    {
        // "Fake content" refers to a "Loading..." message that is inserted
        // into the pane when the Ajax request begins. We only want to allow
        // content to pass through in the Web-CAT version of the pane if it's
        // real content, or if it's fake content AND the pane is toggling to
        // an open state. (In other words, we ignore an attempt to set fake
        // content when the pane is open upon page load because it would
        // overwrite the component content that is already there.)

        if (!isFakeContent || 
            (isFakeContent &&
                (this.showsLoadingMessageOnRefresh || this.isToggling)))
        {
            this.inherited(arguments);
        }
    }
});
