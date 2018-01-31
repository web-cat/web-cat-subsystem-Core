/*==========================================================================*\
 |  $Id: global.js,v 1.15 2010/11/01 17:04:05 aallowat Exp $
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
dojo.require("webcat.Blocker");


//----------------------------------------------------------
/**
 * A convenience method that parses its argument into one or more element IDs.
 * If passed an array, it will return the array. If passed a string, it will
 * split the string around commas and return the resulting array. If passed
 * anything else, it returns null.
 *
 * @param ids an object that is either a string or an array
 * @return an array of IDs
 */
webcat._parseIds = function(/* String|Array */ ids)
{
    if (dojo.isString(ids))
    {
        return ids.split(/\s*,\s*/);
    }
    else if (dojo.isArray(ids))
    {
        return ids;
    }
    else
    {
        return null;
    }
}


//----------------------------------------------------------
/**
 * A convenience method that calls the refresh() method on one or more
 * dijit.ContentPane elements.
 *
 * @param ids a string representing a single identifier to refresh, or an
 *     array of identifiers
 * @param onAfterRefresh a function to be executed after the refresh has
 *     completed and the new content has loaded
 */
webcat.refresh = function(/* String|Array */ ids, /*Function?*/ onAfterRefresh)
{
    var idArray = webcat._parseIds(ids);
    var deferredArray = [];

    dojo.forEach(idArray, function(id) {
        var widget = dijit.byId(id);
        if (widget && widget.refresh)
        {
            var deferred = widget.refresh();
            deferredArray.push(deferred);
        }
    });

    if (deferredArray.length > 0 && onAfterRefresh)
    {
        var dl = new dojo.DeferredList(deferredArray);
        dl.addCallback(function(res) { onAfterRefresh(); });
    }
};

// Old name for backwards compatibility
webcat.refreshContentPanes = webcat.refresh;


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
webcat.fullSubmit = function(/*String*/ formName, /*String*/ fieldName)
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

// Old name for backwards compatibility
webcat.fakeFullSubmit = webcat.fullSubmit;


//----------------------------------------------------------
/**
 * Serializes any form elements that are children of the specified DOM node.
 * This function is adapted from the dojo.formToObject function, which only
 * supports serializing an entire form.
 *
 * @param node the DOM node containing the elements to serialize
 * @return a Javascript object containing the values of the elements
 */
webcat.serializeChildren = function(/*DOMNode|String*/ node)
{
    var _setValue = function(/*Object*/obj, /*String*/name, /*String*/value)
    {
        if (value === null)
        {
            return;
        }

        var val = obj[name];
        if (typeof val == "string")  // inline'd type check
        {
            obj[name] = [val, value];
        }
        else if (dojo.isArray(val))
        {
            val.push(value);
        }
        else
        {
            obj[name] = value;
        }
    };

    var ret = {};
    var exclude = "file|submit|image|reset|button|";

    dojo.forEach(dojo.byId(node).getElementsByTagName('*'), function(item) {
        var _in = item.name;
        var type = (item.type || "").toLowerCase();
        if(_in && type && exclude.indexOf(type) == -1 && !item.disabled)
        {
            _setValue(ret, _in, dojo.fieldToObject(item));
            if(type == "image")
            {
                ret[_in + ".x"] = ret[_in + ".y"] = ret[_in].x = ret[_in].y = 0;
            }
        }
    });

    return ret; // Object
};


//----------------------------------------------------------
/**
 * webcat.isEventKeyDead is used in onKeyUp handlers that are attached to
 * controls like search fields. The function is called to check if the key
 * should be ignored (so that, for example, pressing a cursor key does not
 * cause an unnecessary refresh when the actual field content hasn't changed).
 */
(function() {

    var deadKeys = {};
    deadKeys[dojo.keys.TAB] = true;
    deadKeys[dojo.keys.ENTER] = true;
    deadKeys[dojo.keys.ESCAPE] = true;
    deadKeys[dojo.keys.LEFT_ARROW] = true;
    deadKeys[dojo.keys.RIGHT_ARROW] = true;
    deadKeys[dojo.keys.UP_ARROW] = true;
    deadKeys[dojo.keys.DOWN_ARROW] = true;
    deadKeys[dojo.keys.HOME] = true;
    deadKeys[dojo.keys.END] = true;
    deadKeys[dojo.keys.PAGE_UP] = true;
    deadKeys[dojo.keys.PAGE_DOWN] = true;
    deadKeys[dojo.keys.SHIFT] = true;
    deadKeys[dojo.keys.CTRL] = true;
    deadKeys[dojo.keys.ALT] = true;

    webcat.isEventKeyDead = function(event)
    {
        return (event.keyCode == null || (deadKeys[event.keyCode] || false));
    };

})();


//----------------------------------------------------------
/**
 * webcat.pushBusyCursor() and webcat.popBusyCursor() can be used to change the
 * mouse cursor to a "progress" indicator for indicating that an Ajax request
 * is occurring in the background. These functions should be used in pairs
 * because they stack.
 */
(function() {
    var _busyCursorCount = 0;

    dojo.mixin(webcat,
    {
        pushBusyCursor: function() {
            _busyCursorCount++;
            dojo.addClass(dojo.body(), 'showprogress');
        },

        popBusyCursor: function() {
            if (_busyCursorCount > 0)
            {
                _busyCursorCount--;

                if (_busyCursorCount == 0)
                {
                    dojo.removeClass(dojo.body(), 'showprogress');
                }
            }
        }
    });
})();


// ----------------------------------------------------------
/**
 * Invokes the action represented by the specified URL as an Ajax request, and
 * ensures that the appropriate callback handlers are called.
 *
 * @param widget a widget that contains the callbacks (onRemoteLoad/Error/End)
 *     that should be called when the request is completed; can be null
 * @param options a hash containing options that are passed to the XHR, as
 *     well as the following additional option:
 *
 *     submit: a DOM node (or id) of a node that is the parent of the fields
 *         that you wish to submit; if omitted, the entire form (options.form)
 *         will be submitted; if explicitly null, nothing will be submitted
 *
 * If options.url is supplied, the request will be sent to that URL. If
 * options.form is supplied but options.url is not, then the request will be
 * sent to the form's action URL.
 */
webcat.remoteSubmit = function(/*_Widget*/ widget, /*Object*/ options)
{
    var evalAttributeFunction = function(code) {
        return eval('__evalAttributeFunction__temp__ = ' + code);
    };

    options.content = options.content || {};

    // Use the form's action URL if one wasn't already provided.

    if (options.form)
    {
        var actionUrl = options.form.getAttribute('action');
        actionUrl = actionUrl.replace('/wo/', '/ajax/');
        options.url = actionUrl;
    }

    if (!options.suppressBusyCursor)
    {
        webcat.pushBusyCursor();
    }

    // If this is a partial submit, serialize the subset of fields manually
    // and then clear out options.form so that Dojo's XHR does not
    // serialize the form again.

    if (options.submit)
    {
        dojo.forEach(webcat._parseIds(options.submit), function(node) {
            var serialized = webcat.serializeChildren(node);
            dojo.mixin(options.content, serialized);
        });

        options.content['webcat.wasPartialSubmit'] = true;

        delete options.submit;
        delete options.form;
    }
    else if (options.submit === null)
    {
        options.content['webcat.wasPartialSubmit'] = true;

        delete options.submit;
        delete options.form;
    }

    if (options.sender)
    {
        options.content['AJAX_SUBMIT_BUTTON_NAME'] = options.sender;
        delete options.sender;
    }
    else if (widget)
    {
        if (widget.name)
        {
            options.content['AJAX_SUBMIT_BUTTON_NAME'] = widget.name;
        }
        else if (widget.attr && widget.attr('name'))
        {
            options.content['AJAX_SUBMIT_BUTTON_NAME'] = widget.attr('name');
        }
    }

    options.content['WOIsmapCoords'] = new Date().getTime();

    // Set up the event handlers.
    var xhrOpts = {
        load: function(response, ioArgs) {
            var handler;

            if (widget && widget.onRemoteLoad)
            {
                handler = widget.onRemoteLoad;
            }
            else if (widget && widget.getAttribute &&
                widget.getAttribute('onRemoteLoad'))
            {
                handler = evalAttributeFunction(
                    widget.getAttribute('onRemoteLoad'));
            }

            return handler ? handler(response, ioArgs) : response;
        },

        error: function(response, ioArgs) {
            var handler;

            if (widget && widget.onRemoteError)
            {
                handler = widget.onRemoteError;
            }
            else if (widget && widget.getAttribute &&
                widget.getAttribute('onRemoteError'))
            {
                handler = evalAttributeFunction(
                    widget.getAttribute('onRemoteError'));
            }

            return handler ? handler(response, ioArgs) : response;
        },

        handle: function(response, ioArgs) {
            var handler;

            if (!options.suppressBusyCursor)
            {
                webcat.popBusyCursor();
            }

            if (widget && widget.onRemoteEnd)
            {
                handler = widget.onRemoteEnd;
            }
            else if (widget && widget.getAttribute &&
                widget.getAttribute('onRemoteEnd'))
            {
                handler = evalAttributeFunction(
                    widget.getAttribute('onRemoteEnd'));
            }

            return handler ? handler(response, ioArgs) : response;
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

    var vp = dijit.getViewport();

    var contentDiv = dojo.create('div', {
        style: {
            width: (vp.w * 0.5) + 'px'
        }
    });

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

    var vp = dijit.getViewport();

    var contentDiv = dojo.create('div', {
        style: {
            width: (vp.w * 0.5) + 'px'
        }
    });

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
