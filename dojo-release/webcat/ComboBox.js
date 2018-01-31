/*==========================================================================*\
 |  $Id: ComboBox.js,v 1.6 2010/01/23 02:32:41 aallowat Exp $
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
 * item that it contains, and supports HTML markup in items when constructed
 * from div tags instead of a select tag.
 */
dojo.declare("webcat.ComboBoxMixin", null,
{
    //~ Properties ............................................................

    /**
     * True to disable the auto-resizing functionality, otherwise false.
     */
    fixedSize: false,

    /**
     * The minimum width that the control may take when auto-resizing is
     * enabled.
     */
    minimumWidth: 48,

    /**
     * The maximum width that the control may take when auto-resizing is
     * enabled.
     */
    maximumWidth: Number.MAX_VALUE,

    useDividers: false,


    //~ Private variables .....................................................

    /* The computed ideal width of the widget. */
    _idealWidth: 0,

    /* The HTML of the items contained in the list. */
    _itemCache: [],


    // ----------------------------------------------------------
    _cacheItems: function()
    {
        this._itemCache = [];

        var selector = "> " + (this.isSelectNode ? "option" : "span");

        dojo.query(selector, this.srcNodeRef).forEach(
                dojo.hitch(this, function(item)
        {
            this._itemCache.push(
                dojo.attr(item, "searchKey") || item.innerText || item.textContent);
        }));
    },


    // ----------------------------------------------------------
    _computeIdealWidth: function()
    {
        var maxWidth = 0;
        var parentNode = this.domNode.parentNode;

        // A dummy element is added to the parent of this widget so that
        // whatever styling applies to the widget also applies to the span
        // that we're getting the size of.
        var el = dojo.create("span", { }, parentNode);

        dojo.forEach(this._itemCache, function(item)
        {
            el.innerHTML = item;
            maxWidth = Math.max(maxWidth, dojo.marginBox(el).w);
        });

        dojo.destroy(el);

        this._idealWidth = Math.max(this.minimumWidth, maxWidth);

        if (maxWidth)
        {
            this._resizeToIdealWidth();
        }
    },


    // ----------------------------------------------------------
    /**
     * Borrowed from dijit.form._ComboBoxMixin. Make sure to keep in sync with
     * Dojo updates.
     */
    postMixInProperties: function()
    {
        if (!this.store)
        {
            var srcNodeRef = this.srcNodeRef;

            this.isSelectNode =
                (this.srcNodeRef.tagName.toLowerCase() == "select");

            // Apply the custom webcat ComboBox data store before the
            // superclass gets the opportunity. The superclass does the
            // same conditional as above, so our store will take
            // priority if the user didn't provide one.

            this.store = new webcat._ComboBoxDataStore(srcNodeRef);

            // if there is no value set and there is an option list, set
            // the value to the first value to be consistent with native
            // select tag.

            // Firefox and Safari set 'value'
            // IE6 and Opera set 'selectedIndex', which is automatically set
            // by the 'selected' attribute of an option tag
            // IE6 does not set 'value', Opera sets 'value = selectedIndex'

            var needsSelection;

            if (this.isSelectNode)
            {
                needsSelection = !this.value ||
                    ((typeof srcNodeRef.selectedIndex == "number") &&
                        srcNodeRef.selectedIndex.toString() === this.value);
            }
            else
            {
                needsSelection = !this.value;
            }

            if (needsSelection)
            {
                var item = this.store.fetchSelectedItem();

                if (item)
                {
                    this.value = this.store.getValue(item,
                            this._getValueField());
                }
            }
        }

        this.inherited(arguments);
    },


    // ----------------------------------------------------------
    _startSearch: function(/*String*/ key)
    {
        // Create the popup widget with our special subtype before the
        // base widget gets a chance.

        if (!this._popupWidget)
        {
            var popupId = this.id + "_popup";
            this._popupWidget = new webcat._ComboBoxMenu({
                onChange: dojo.hitch(this, this._selectOption),
                id: popupId,
                useDividers: this.useDividers
            });

            dijit.removeWaiState(this.focusNode,"activedescendant");
            dijit.setWaiState(this.textbox,"owns",popupId);
        }

        this.inherited(arguments);
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
            var dialog = this._getContainingTooltipDialog();
            if (dialog)
            {
                dojo.connect(dialog, "onOpen", this, this._computeIdealWidth);
            }
        }
    },


    // ----------------------------------------------------------
    _getContainingTooltipDialog: function()
    {
        var parents = dojo.query(this.domNode).parents(".dijitTooltipDialog");
        var dialog = null;
        if (parents.length)
        {
            dialog = dijit.byNode(parents[0]);
        }

        return dialog;
    },


    // ----------------------------------------------------------
    _getContainingPopup: function()
    {
        var parents = dojo.query(this.domNode).parents(".dijitPopup");
        var popup = null;
        if (parents.length)
        {
            popup = parents[0];
        }

        return popup;
    },


    // ----------------------------------------------------------
    _resizeToIdealWidth: function()
    {
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
});


//=========================================================================
/**
 * A subclass of dijit.form._ComboBoxMenu that adds functionality that the
 * Web-CAT version of the widget uses.
 */
dojo.declare("webcat._ComboBoxMenu", dijit.form._ComboBoxMenu,
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    _createOption: function(/*Object*/ item, labelFunc)
    {
        var menuitem = webcat._ComboBoxMenu.superclass._createOption.call(
            this, item, labelFunc);

        dojo.addClass(menuitem, "webcatComboBoxMenuItem");

        if (this.useDividers)
        {
            dojo.addClass(menuitem, "webcatComboBoxMenuItemDivider");
        }

        return menuitem;
    },


    // ----------------------------------------------------------
    createOptions: function(results, dataObject, labelFunc){
        // summary:
        //		Fills in the items in the drop down list
        // results:
        //		Array of dojo.data items
        // dataObject:
        //		dojo.data store
        // labelFunc:
        //		Function to produce a label in the drop down list from a dojo.data item

        //this._dataObject=dataObject;
        //this._dataObject.onComplete=dojo.hitch(comboBox, comboBox._openResultList);
        // display "Previous . . ." button
        this.previousButton.style.display = (dataObject.start == 0) ? "none" : "";
        dojo.attr(this.previousButton, "id", this.id + "_prev");
        // create options using _createOption function defined by parent
        // ComboBox (or FilteringSelect) class
        // #2309:
        //		iterate over cache nondestructively
        dojo.forEach(results, function(item, i){
            var menuitem = this._createOption(item, labelFunc);
// BEGIN WEBCAT CHANGES
            dojo.addClass(menuitem, "dijitReset");
            dojo.addClass(menuitem, "dijitMenuItem");
// END WEBCAT CHANGES
            dojo.attr(menuitem, "id", this.id + i);
            this.domNode.insertBefore(menuitem, this.nextButton);
        }, this);
        // display "Next . . ." button
        var displayMore = false;
        //Try to determine if we should show 'more'...
        if(dataObject._maxOptions && dataObject._maxOptions != -1){
            if((dataObject.start + dataObject.count) < dataObject._maxOptions){
                displayMore = true;
            }else if((dataObject.start + dataObject.count) > (dataObject._maxOptions - 1)){
                //Weird return from a datastore, where a start + count > maxOptions
                // implies maxOptions isn't really valid and we have to go into faking it.
                //And more or less assume more if count == results.length
                if(dataObject.count == results.length){
                    displayMore = true;
                }
            }
        }else if(dataObject.count == results.length){
            //Don't know the size, so we do the best we can based off count alone.
            //So, if we have an exact match to count, assume more.
            displayMore = true;
        }

        this.nextButton.style.display = displayMore ? "" : "none";
        dojo.attr(this.nextButton,"id", this.id + "_next");
        return this.domNode.childNodes;
    },
});


//=========================================================================
/**
 * A subclass of dijit.form.FilteringSelect that mixes in the auto-resizing
 * implementation above.
 */
dojo.declare("webcat.FilteringSelect",
    [dijit.form.FilteringSelect, webcat.ComboBoxMixin],
{
    //~ Methods ...............................................................

    // ----------------------------------------------------------
    postCreate: function()
    {
        dijit.form.FilteringSelect.prototype.postCreate.apply(this, arguments);
        webcat.ComboBoxMixin.prototype._postCreate.apply(this, arguments);
    },


    // ----------------------------------------------------------
    startup: function()
    {
        dijit.form.FilteringSelect.prototype.startup.apply(this, arguments);
        webcat.ComboBoxMixin.prototype._startup.apply(this, arguments);
    }
});


//=========================================================================
/**
 * A small data store for inlined combo box data, designed similarly to
 * dijit.form._ComboBoxDataStore. This class handles HTML content in item
 * labels, so it supports a control in markup either as a <select> tag like
 * the standard combo box:
 *
 * <select><option value="AL">Alabama</option>...
 *
 * or as nested <div> tags, since <option> tags cannot contain HTML markup:
 *
 * <div dojoType="webcat.FilteringSelect>
 *    <div value="AL"><span style="color: red">Alabama</span></div>
 *    ...
 *
 * The "items" in this store are simply the DomNodes of the options in the
 * list; that is, either <option> tags or <div> tags.
 */
dojo.declare("webcat._ComboBoxDataStore", null,
{
    // ----------------------------------------------------------
    constructor: function(/*DomNode*/ root)
    {
        this.root = root;

        this.isSelectNode = (root.tagName.toLowerCase() == "select");
        this.optionElementName = (this.isSelectNode ? "option" : "span");

        var selector = "> " + this.optionElementName;
        dojo.query(selector, root).forEach(function(node)
        {
            node.innerHTML = dojo.trim(node.innerHTML);
        });

    },


    // ----------------------------------------------------------
    _getSearchKeyForItem: function(/*item*/ item)
    {
        return dojo.attr(item, 'searchKey') ||
            item.innerText || item.textContent;
    },


    // ----------------------------------------------------------
    getValue: function(/*item*/ item,
                       /*attribute-name-string */ attribute,
                       /*value?*/ defaultValue)
    {
        if (attribute == "value")
        {
            return this.isSelectNode ? item.value : dojo.attr(item, 'value');
        }
        else if (attribute == "label")
        {
            return item.innerHTML || '';
        }
        else
        {
            return this._getSearchKeyForItem(item) || '';
        }
    },


    // ----------------------------------------------------------
    isItemLoaded: function(/*anything*/ something)
    {
        return true;
    },


    // ----------------------------------------------------------
    getFeatures: function()
    {
        return {
            "dojo.data.api.Read": true,
            "dojo.data.api.Identity": true
        };
    },


    // ----------------------------------------------------------
    _fetchItems: function(/*Object*/ args,
                          /*Function*/ findCallback,
                          /*Function*/ errorCallback)
    {
        if (!args.query)
        {
            args.query = {};
        }

        if (!args.query.name)
        {
            args.query.name = "";
        }

        if (!args.queryOptions)
        {
            args.queryOptions = {};
        }

        var self = this;
        var selector = "> " + this.optionElementName;

        var matcher = dojo.data.util.filter.patternToRegExp(
            args.query.name, args.queryOptions.ignoreCase);

        var items = dojo.query(selector, this.root).filter(function(option)
            {
                return (self._getSearchKeyForItem(option) || '').match(matcher);
            });

        if(args.sort)
        {
            items.sort(dojo.data.util.sorter.createSortFunction(
                args.sort, this));
        }

        findCallback(items, args);
    },


    // ----------------------------------------------------------
    close: function(/*dojo.data.api.Request || args || null*/ request)
    {
    },


    // ----------------------------------------------------------
    getLabel: function(/*item*/ item)
    {
        return item.innerHTML;
    },


    // ----------------------------------------------------------
    getIdentity: function(/*item*/ item)
    {
        return dojo.attr(item, "value");
    },


    // ----------------------------------------------------------
    fetchItemByIdentity: function(/*Object*/ args)
    {
        // The identity of an item is its value attribute.

        var selector = this.optionElementName +
            "[value='" + args.identity + "']";

        var item = dojo.query(selector, this.root)[0];
        args.onItem(item);
    },


    // ----------------------------------------------------------
    fetchSelectedItem: function()
    {
        var root = this.root;

        if (this.isSelectNode)
        {
            // If the control is a <select> tag, we can get the selected
            // item by simply accessing its selectedIndex property, and
            // then using a CSS selector to quickly grab the item at
            // that index.

            var si = root.selectedIndex;
            var selector = "> " + this.optionElementName + ":nth-child(" +
                        (si != -1 ? si + 1 : 1) + ")";
            return dojo.query(selector, root)[0];
        }
        else
        {
            // If the control is a <div> tag, then there is no selectedIndex
            // property, so the best we can do is cycle through each child
            // until we find one with its "selected" attribute set.

            var selector = "> " + this.optionElementName;
            var items = dojo.query(selector, root);

            var selectedItem = null;
            var isSelected = dojo.some(items, function(item) {
                selectedItem = item;
                return dojo.attr(item, "selected");
            });

            return isSelected ? selectedItem : items[0];
        }
    }
});
//Mix in the simple fetch implementation to this class.
dojo.extend(webcat._ComboBoxDataStore, dojo.data.util.simpleFetch);
