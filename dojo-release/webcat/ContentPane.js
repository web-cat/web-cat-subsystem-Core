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

dojo.provide("webcat.ContentPane");

dojo.require("dijit.layout.ContentPane");

// ------------------------------------------------------------------------
/**
 * The webcat.ContentPane is a minor hack to dijit.layout.ContentPane that
 * fixes an issue that helps it fit better into the WebObjects model. The
 * intention is that the content pane will be filled with component content
 * that is to be refreshed when the refresh() method is called. The refresh()
 * method requires that the "href" attribute be set on the pane (which is
 * obtained from the Wonder Ajax request handler), but doing so causes Dojo to
 * replace the pane's content with the content at the href when the page is
 * loaded (so essentially, the content is loaded twice). This subclass patches
 * this behavior so that the pane's content is NOT loaded from the remote URL
 * when it is initialized.
 *
 * @author Tony Allevato
 * @version $Id$
 */
dojo.declare("webcat.ContentPane", dijit.layout.ContentPane,
{
	//~ Properties ............................................................
	
	/* Set to true when the pane is initially starting up, otherwise false. */
	initialStartup: true,
	
	/* Web-CAT's content pane by default does NOT show a loading message when
	   it is being updated, to behave more like the old-style Prototype
	   AjaxUpdateContainers (that is, modifications are made in place and
	   appear instant). Set this attribute to true if you want to show a
	   loading message when this pane is refreshed (for example, to indicate
	   a long-running operation). */
	showsLoadingMessageOnRefresh: false,


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	startup: function()
	{
		this.initialStartup = true;
		this.inherited(arguments);
		this.initialStartup = false;
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
	_setContent: function(content, isFakeContent)
	{
		if (!isFakeContent ||
		    (isFakeContent && this.showsLoadingMessageOnRefresh))
		{
			this.inherited(arguments);
		}
	}
});
