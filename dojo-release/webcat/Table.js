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

dojo.provide("webcat.Table");

// ------------------------------------------------------------------------
/**
 * This is not a true Dojo widget class. It is a wrapper class that wraps an
 * instance of the WCTable component based on its AjaxProxy and JavaScript id,
 * and provides functions that communicate back to the server through the
 * proxy.
 */
dojo.declare("webcat.Table", null,
{
    //~ Properties ............................................................

    // A reference to the JSON RPC bridge used to obtain data for the table.
    proxy: null,

    // The prefix used for all of the widget IDs in the table.
    idPrefix: "",
    
    // True if multiple selection is allowed.
    multiSelect: false,


    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    constructor: function(/* Object */ args)
    {
        dojo.mixin(this, args);

        this.selectionCheckboxName = this.idPrefix + "_selectionState";
        this.allSelectionCheckboxID = this.idPrefix + "_allSelectionState";
        this.updateContainerID = this.idPrefix + "_updateContainer";

        // Cache the checkbox widgets for easier access later.
        if (this.multiSelect)
        {
            this.selectionCheckboxes =
                dojo.query("*[name='" + this.selectionCheckboxName + "']").map(
                function(node) {
                    return dijit.getEnclosingWidget(node);
                });

            this.allSelectionCheckbox = dijit.byId(this.allSelectionCheckboxID);
        }
    },
    
    
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    selectAllObjectsInBatch: function(state)
    {
        this.selectionCheckboxes.forEach(function(widget) {
            widget.attr("checked", state);
        });
    
        this.proxy.selectAllObjectsInBatch(
            function() { }, state);
    },
    

    // ----------------------------------------------------------
    selectObjectAtIndexInBatch: function(index, state)
    {
        if (this.multiSelect)
        {
            var allChecked;

            allChecked = this.selectionCheckboxes.length > 0
                && this.selectionCheckboxes.every(function(widget) {
                    return widget.attr("checked");
                });
        
            this.allSelectionCheckbox.attr("checked", allChecked);
        }

        this.proxy.selectObjectAtIndexInBatch(
            function() { }, index, state);
    },
    

    // ----------------------------------------------------------
    selectOnlyObjectAtIndexInBatch: function(index, options)
    {
        options = typeof(options) != 'undefined' ? options : {};

        if (this.multiSelect)
        {
            this.selectionCheckboxes.forEach(function(widget, i) {
                widget.attr("checked", i == index);
            });

            this.allSelectionCheckbox.attr("checked", false);
        }

        if (options.synchronous)
        {
            this.proxy.selectOnlyObjectAtIndexInBatch(index);
        }
        else
        {
            this.proxy.selectOnlyObjectAtIndexInBatch(function() { }, index);
        }
    },


    // ----------------------------------------------------------
    performActionOnObjectAtIndexInBatch: function(index, columnIndex)
    {
        if (this.multiSelect)
        {
            this.selectionCheckboxes.forEach(function(widget, i) {
                widget.attr("checked", i == index);
            });

            this.allSelectionCheckbox.attr("checked", false);
        }

        this.proxy.performActionOnObjectAtIndexInBatch(
            this._updateTableGenerator(), index, columnIndex);
    },
    

    // ----------------------------------------------------------
    changeSortOrdering: function(index)
    {
        this.proxy.changeSortOrdering(
            this._updateTableGenerator(), index);
    },
    

    // ----------------------------------------------------------
    changeBatchSize: function(size)
    {
        this.proxy.changeBatchSize(
            this._updateTableGenerator(), size);
    },
    

    // ----------------------------------------------------------
    goToFirstBatch: function()
    {
        this.proxy.goToFirstBatch(this._updateTableGenerator());
    },


    // ----------------------------------------------------------
    goToPreviousBatch: function()
    {
        this.proxy.goToPreviousBatch(this._updateTableGenerator());
    },


    // ----------------------------------------------------------
    goToNextBatch: function()
    {
        this.proxy.goToNextBatch(this._updateTableGenerator());
    },


    // ----------------------------------------------------------
    goToLastBatch: function()
    {
        this.proxy.goToLastBatch(this._updateTableGenerator());
    },


    // ----------------------------------------------------------
    changeFilter: function(keyPath, changes)
    {
        this.proxy.changeFilter(
            this._updateTableGenerator(), keyPath, changes);
    },
    
    
    // ----------------------------------------------------------
    startFilterValueUpdateTimer: function(keyPath, widget, evt, proxy)
    {
        if(evt.keyCode == null)
        {
            return;
        }
        else
        {
            switch (evt.keyCode)
            {
                case Event.KEY_TAB:
                case Event.KEY_RETURN:
                case Event.KEY_ESC:
                case Event.KEY_LEFT:
                case Event.KEY_RIGHT:
                case Event.KEY_UP:
                case Event.KEY_DOWN:
                    return;
            }
        }

        if (this.timeoutId)
        {
            clearTimeout(this.timeoutId);
        }

        value = widget.id;
        script = "eval('" + proxy + ".changeFilter(\"" + keyPath +
            "\", { value: dijit.byId(\"" + value + "\").getValue() });');";
        this.timeoutId = setTimeout(
            script, 500
        );
    },


    // ----------------------------------------------------------
    _updateTableGenerator: function()
    {
        var ucid = this.updateContainerID;
        return function() {
            eval("dijit.byId('" + ucid + "').refresh();");
        };
    }
});
