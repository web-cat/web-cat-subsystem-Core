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
/**
 * Fakes a full form submit (synchronous, not Ajax) by dynamically creating a
 * submit button as a child of the specified form and then calling its click()
 * method to initiate the submit. This is necessary when we want Dojo elements
 * such as menu items to be able to execute component or direct actions; since
 * Dojo's page parsing causes the element to be moved to the end of the body,
 * and thus outside of its form, we need to dynamically inject an element so
 * that the action executes properly.
 *
 * @param formName the name of the form into which the button should be
 *     injected
 * @param fieldName the name of the actual element that is requesting the
 *     submit
 */
webcat.fakeFullSubmit = function(/* String */ formName, /* String */ fieldName)
{
    var form = dojo.query('form[name=' + formName + ']')[0];
    var button = dojo.create('button', {
        type: 'submit',
        name: fieldName,
        value: '__shadow',
        style: { display: 'none' },
    }, form, 'last');

    button.click();
    
    // We could destroy the button here, but since the page is going to be
    // reloaded at this point anyway, it doesn't seem to matter.
};


// ----------------------------------------------------------
/**
 * Performs a partial form submit via Ajax.
 *
 * @param widget the widget whose value should be submitted
 * @param scriptComponentName the component that is instigating the submit
 *     (i.e., a button that was clicked, or the component name of an element
 *     that generates a script that calls an action)
 * @param options a hash that contains options that will be passed to the XHR
 * @param refreshIds a string containing the ID of the content pane to refresh,
 *     or an array of IDs
 */
webcat.partialSubmit = function(/* _Widget */ widget,
    /* String */ scriptComponentName, /* Object */  options,
    /* String|Array? */ refreshIds)
{
    var actionUrl = options.form.getAttribute('action');
    actionUrl = actionUrl.replace('/wo/', '/ajax/');

    if (!options.content)
    {
        options.content = {};
    }

    options.url = actionUrl;
    options.content['AJAX_SUBMIT_BUTTON_NAME'] = scriptComponentName;
    options.content['_partialSubmitID'] = widget.name;
    options.content['WOIsmapCoords'] = new Date().getTime();
    
    webcat.invokeRemoteAction(widget, options, refreshIds);
};


// ----------------------------------------------------------
/**
 * Invokes the action represented by the specified URL as an Ajax request, and
 * ensures that the appropriate callback handlers are called.
 *
 * @param widget the widget that contains the callbacks (onRemoteLoad/Error/End)
 *     that should be called when the request is completed
 * @param options a hash containing options that are passed to the XHR
 * @param refreshIds a string containing the ID of the content pane to refresh,
 *     or an array of IDs
 */
webcat.invokeRemoteAction = function(/* _Widget */ widget,
	/* Object */ options, /* String|Array? */ refreshIds)
{
	var evalAttributeFunction = function(code) {
		return eval('__evalAttributeFunction__temp__ = ' + code);
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


// ----------------------------------------------------------
/**
 * Displays an alert dialog that is Dojo-styled; that is, it is a modal
 * dialog that fades the background and uses Dojo widgets instead of the system
 * controls used by the global alert() function.
 *
 * @param options a hash that contains the following keys:
 *
 *     title (String, optional): the title for the dialog box; if omitted, will
 *         default to empty
 * 
 *     message (String, required): the message to appear in the alert box
 *
 *     okLabel (String, optional): the label for the OK button; if omitted,
 *         defaults to "OK"
 *
 *     onClose (Function, optional): the function to be called when the alert
 *         dialog is closed, either by using the OK button or the dialog's
 *         close widget; if omitted, no action is taken
 */
webcat.alert = function(/* Object */ options)
{
    var dialog;
    var dialogId = 'webcat_alert_dialog';
    var okButtonId = 'webcat_alert_dialog_ok';

    // Called when a button in the dialog is clicked. This handler passes
    // control to one of the callbacks given in the options hash, if they
    // exist.

    var dialogHandler = function(id) {
        dialog.hide();
        dialog.destroyRecursive();

        if (id == okButtonId)
        {
            if (options.onClose) options.onClose();
        }
    };
        
    dialog = new dijit.Dialog({
        id: dialogId,
        title: options.title,
        onCancel: function() {
            dialogHandler(okButtonId);
        }
    });

    var okLabel = options.okLabel || 'OK';

    // Create the dialog content nodes and widgets.

    var contentDiv = dojo.create('div');

    var messageDiv = dojo.create('div', {
        innerHTML: options.message,
    }, contentDiv);

    var buttonDiv = dojo.create('div', {
        className: 'center'
    }, contentDiv);

    var okButton = new dijit.form.Button({
        label: okLabel,
        id: okButtonId,
        onClick: function(evt) { dialogHandler(this.id); }
    });

    buttonDiv.appendChild(okButton.domNode);

    dialog.attr('content', contentDiv);
    dialog.show();
};


// ----------------------------------------------------------
/**
 * Displays a confirmation dialog that is Dojo-styled; that is, it is a modal
 * dialog that fades the background and uses Dojo widgets instead of the system
 * controls used by the global confirm() function.
 *
 * @param options a hash that contains the following keys:
 *
 *     title (String, optional): the title for the dialog box; if omitted, will
 *         default to empty
 * 
 *     message (String, required): the message to appear in the alert box
 *
 *     yesLabel (String, optional): the label for the yes button; if omitted,
 *         defaults to "Yes"
 *
 *     noLabel (String, optional): the label for the no button; if omitted,
 *         defaults to "No"
 *
 *     onYes (Function, optional): the function to be called when the yes
 *         button is clicked; if omitted, no action is taken
 *
 *     onNo (Function, optional): the function to be called when the no button
 *         is clicked; if omitted, no action is taken
 */
webcat.confirm = function(/* Object */ options)
{
    var dialog;
    var dialogId = 'webcat_confirm_dialog';
    var yesButtonId = 'webcat_confirm_dialog_yes';
    var noButtonId = 'webcat_confirm_dialog_no';

    // Called when a button in the dialog is clicked. This handler passes
    // control to one of the callbacks given in the options hash, if they
    // exist.

    var dialogHandler = function(id) {
        dialog.hide();
        dialog.destroyRecursive();

        if (id == yesButtonId)
        {
            if (options.onYes) options.onYes();
        }
        else
        {
            if (options.onNo) options.onNo();
        }
    };
        
    dialog = new dijit.Dialog({
        id: dialogId,
        title: options.title,
        onCancel: function() {
            dialogHandler(noButtonId);
        }
    });

    var yesLabel = options.yesLabel || 'Yes';
    var noLabel = options.noLabel || 'No';

    // Create the dialog content nodes and widgets.

    var contentDiv = dojo.create('div');

    var questionDiv = dojo.create('div', {
        innerHTML: options.message,
    }, contentDiv);

    var buttonDiv = dojo.create('div', {
        className: 'center'
    }, contentDiv);

    var yesButton = new dijit.form.Button({
        label: yesLabel,
        id: yesButtonId,
        onClick: function(evt) { dialogHandler(this.id); }
    });
    dojo.addClass(yesButton.domNode, 'pos');

    var noButton = new dijit.form.Button({
        label: noLabel,
        id: noButtonId,
        onClick: function(evt) { dialogHandler(this.id); }
    });
    dojo.addClass(noButton.domNode, 'neg');

    buttonDiv.appendChild(yesButton.domNode);
    buttonDiv.appendChild(noButton.domNode);

    dialog.attr('content', contentDiv);
    dialog.show();
};
