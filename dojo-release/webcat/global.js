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

dojo.provide("webcat.global");

// ----------------------------------------------------------
/**
 * A convenience method that calls the refresh() method on one or more
 * dijit.ContentPane elements.
 *
 * @param ids a string representing a single identifier to refresh, or an
 *     array of identifiers 
 */
webcat.refreshContentPanes = function(/* String|Array */ ids)
{
	var idArray;

	if (dojo.isString(ids))
	{
		idArray = [ ids ];
	}
	else if (dojo.isArray(ids))
	{
		idArray = ids;
	}

	dojo.forEach(idArray, function(id) {
		dijit.byId(id).refresh();
	});
};


// ----------------------------------------------------------
webcat.partialSubmit = function(widget, scriptComponentName,
    options, refreshIds)
{
    var actionUrl = options.form.getAttribute("action");
    actionUrl = actionUrl.replace('/wo/', '/ajax/');

    if (!options.content)
    {
        options.content = {};
    }

    options.url = actionUrl;
    options.content["AJAX_SUBMIT_BUTTON_NAME"] = scriptComponentName;
    options.content["_partialSubmitID"] = widget.name;
    options.content["WOIsmapCoords"] = new Date().getTime();
    
    webcat.invokeRemoteAction(widget, options, refreshIds);
};


// ----------------------------------------------------------
/**
 * Invokes the action represented by the specified URL as an Ajax request, and
 * ensures that the appropriate callback handlers are called.
 *
 * @param widget
 * @param options
 * @param refreshIds
 */
webcat.invokeRemoteAction = function(/* _Widget */ widget,
	/* Object */ options, /* Any */ refreshIds)
{
	var evalAttributeFunction = function(code) {
		return eval("__evalAttributeFunction__temp__ = " + code);
	};

	// Set up the event handlers.
	var xhrOpts = {
		load: function(response, ioArgs) {
			if (refreshIds)
			{
				webcat.refreshContentPanes(refreshIds);
			}

			var handler;

			if (widget.onRemoteLoad)
			{
				handler = widget.onRemoteLoad;
			}
			else if (widget.getAttribute('onRemoteLoad'))
			{
				handler = evalAttributeFunction(
					widget.getAttribute('onRemoteLoad'));
			}
				
			return handler(response, ioArgs);
		},

		error: function(response, ioArgs) {
			var handler;

			if (widget.onRemoteError)
			{
				handler = widget.onRemoteError;
			}
			else if (widget.getAttribute('onRemoteError'))
			{
				handler = evalAttributeFunction(
					widget.getAttribute('onRemoteError'));
			}
				
			return handler(response, ioArgs);
		},

		handle: function(response, ioArgs) {
			var handler;

			if (widget.onRemoteEnd)
			{
				handler = widget.onRemoteEnd;
			}
			else if (widget.getAttribute('onRemoteEnd'))
			{
				handler = evalAttributeFunction(
					widget.getAttribute('onRemoteEnd'));
			}
				
			return handler(response, ioArgs);
		}
	};
	
	// Copy remaining options that were passed in by the component.
	for (var key in options)
	{
		xhrOpts[key] = options[key];
	}
	
	dojo.xhrPost(xhrOpts);
};
