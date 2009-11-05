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

dojo.provide("webcat.ComboBox");

dojo.require("dijit.form.FilteringSelect");

//=========================================================================
/**
 * A mixin for combo-box widgets that autoresizes the widget to fit the largest
 * item that it contains.
 */
dojo.declare("webcat.ResizingComboBoxMixin", null,
{
    //~ Properties ............................................................

    /**
     * True to disable the auto-resizing functionality, otherwise false.
     */
    fixedSize: false,

    /**
     * The maximum width that the control may take.
     */
    maximumWidth: Number.MAX_VALUE,


    //~ Private variables .....................................................

    /* The computed ideal width of the widget. */
    _idealWidth: 0,

    /* The HTML of the items contained in the list. */
    _itemCache: [],


    // ----------------------------------------------------------
    _cacheItems: function()
    {
        this._itemCache = [];
        dojo.query("> option", this.srcNodeRef).forEach(
                dojo.hitch(this, function(node)
        {
            this._itemCache.push(node.innerHTML);
        }));
    },


    // ----------------------------------------------------------
    _computeIdealWidth: function()
    {
        var maxWidth = 32;
        var tb = this.textbox;
        var parentNode = this.domNode.parentNode;

        // Dummy element is added to the parent of this widget so that whatever
        // styling applies to the widget also applies to the span that we're
        // getting the size of.

        var el = dojo.create("span", { }, parentNode);

        dojo.forEach(this._itemCache, function(item)
        {
            el.innerHTML = item;
            var w = dojo.marginBox(el).w;

            if (w > maxWidth)
            {
                maxWidth = w;
            }
        });

        dojo.destroy(el);

        this._idealWidth = maxWidth;
    },


    // ----------------------------------------------------------
    _postCreate: function()
    {
        if (!this.fixedSize)
        {
            this._cacheItems();
            this._idealWidth = null;
        }
    },


    // ----------------------------------------------------------
    _startup: function()
    {
        if (!this.fixedSize)
        {
            if (!this._idealWidth)
            {
                this._computeIdealWidth();
            }

            var tb = this.textbox;
            var dn = this.domNode;
            var diff = dojo.marginBox(dn).w - dojo.marginBox(tb).w;
            var newWidth = this._idealWidth + diff + 4;

            if (newWidth > this.maximumWidth)
            {
                newWidth = this.maximumWidth;
            }

            dojo.marginBox(dn, { w: newWidth });
        }
    }
});


//=========================================================================
/**
 * A subclass of dijit.form.FilteringSelect that mixes in the auto-resizing
 * implementation above.
 */
dojo.declare("webcat.FilteringSelect",
    [dijit.form.FilteringSelect, webcat.ResizingComboBoxMixin],
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    postCreate: function()
    {
        dijit.form.FilteringSelect.prototype.postCreate.apply(this, arguments);
        webcat.ResizingComboBoxMixin.prototype._postCreate.apply(this, arguments);
    },


    // ----------------------------------------------------------
    startup: function()
    {
        dijit.form.FilteringSelect.prototype.startup.apply(this, arguments);
        webcat.ResizingComboBoxMixin.prototype._startup.apply(this, arguments);
    }
});
