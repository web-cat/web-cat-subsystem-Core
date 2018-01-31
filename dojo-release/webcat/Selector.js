/*==========================================================================*\
 |  $Id: Selector.js,v 1.4 2011/08/22 19:59:13 aallowat Exp $
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

dojo.provide("webcat.Selector");

dojo.require("dojo.dnd.Selector");

// ------------------------------------------------------------------------
/**
 * @author Tony Allevato
 * @version $Id: Selector.js,v 1.4 2011/08/22 19:59:13 aallowat Exp $
 */
dojo.declare("webcat.Selector", dojo.dnd.Selector,
{
    shadowSelectionField: '',
    onSelectionChanged: '',

    constructor: function(node, params)
    {
        this.onSelectionChanged = params.onSelectionChanged;
        this.onItemDoubleClicked = params.onItemDoubleClicked;
        this.shadowSelectionField = params.shadowSelectionField;
        this._firstTime = true;

        this.events.push(
            dojo.connect(this.node, "ondblclick", this, "onDoubleClick"));
    },

    markupFactory: function(params, node)
    {
        params._skipStartup = true;
        return new webcat.Selector(node, params);
    },

    onMouseDown: function(/*Event*/ e)
    {
        if (this._firstTime)
        {
            this.forInItems(function(data, id){
                if (dojo.hasClass(id, 'dojoDndItemSelected'))
                {
                    this.selection[id] = 1;
                }
            }, this);

            delete this._firstTime;
        }

        var oldSelection = dojo.clone(this.selection);
        this.inherited(arguments);

        var changed = false;
        var empty = dojo.dnd._empty;
        var shadowSelection = null;

        for (var i in this.selection)
        {
            if (i in empty)
            {
                continue;
            }

            if (shadowSelection)
            {
                shadowSelection += ',' + i;
            }
            else
            {
                shadowSelection = i;
            }

            if (this.selection[i] != oldSelection[i])
            {
                changed = true;
                break;
            }
        }

        if (this.shadowSelectionField)
        {
            dojo.byId(this.shadowSelectionField).value = shadowSelection;
        }

        if (changed)
        {
            eval(this.onSelectionChanged);
        }
    },

    onDoubleClick: function(/*Event*/ e)
    {
        if (this.current && dojo.hasClass(this.current, "dojoDndItem"))
        {
            eval(this.onItemDoubleClicked);
        }
    }
});
