/*==========================================================================*\
 |  $Id: TableSource.js,v 1.1 2009/11/05 20:25:25 aallowat Exp $
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

dojo.provide("webcat.TableSource");

dojo.require("dojox.data.dom");
dojo.require("dojo.dnd.Source");

// ========================================================================
/**
 * Extends the standard dojo.dnd.Source to add features that are appropriate
 * for Web-CAT table elements.
 */
dojo.declare("webcat.TableSource", dojo.dnd.Source,
{
    //~ Properties ............................................................

    /** The JSON RPC proxy to use to communicate with the component. */
    proxy: null,

    /** True if items can only be moved and not copied in this source/target. */
    moveOnly: false,

    _rowCssClasses: [ "o", "e", "oc", "ec", "oe", "ee" ],
    _nodesCreated: [],


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    constructor: function(/*DOMNode|String*/node, /*dojo.dnd.__SourceArgs?*/params)
    {
        dojo.mixin(this, dojo.mixin({}, params));
    },


    // ----------------------------------------------------------
    markupFactory: function(params, node)
    {
        params._skipStartup = true;
        return new webcat.TableSource(node, params);
    },


    // ----------------------------------------------------------
    copyState: function(keyPressed, self)
    {
        if (this.moveOnly)
        {
            return false;
        }
        else
        {
            return dojo.dnd.Source.prototype.copyState.call(
                    this, keyPressed, self);
        }
    },


    // ----------------------------------------------------------
    insertNodes: function(addSelected, data, before, anchor)
    {
        var oldCreator = this._normalizedCreator;
        var nodesCreated = this._nodesCreated;

        // Wrap the creator so that we can store the nodes that are created;
        // we'll need their indices later in the drop handler.

        this._normalizedCreator = function(node, hint) {
            var res = oldCreator.call(this, node, hint);
            nodesCreated.push(res.node);
            return res;
        };

        dojo.dnd.Source.prototype.insertNodes.call(
                this, addSelected, data, before, anchor);

        this._normalizedCreator = oldCreator;
    },


    // ----------------------------------------------------------
    onDndStart: function(/*Node*/ source, /*Array*/ nodes, /*Boolean*/ copy)
    {
        dojo.dnd.Source.prototype.onDndStart.call(
                this, source, nodes, copy);

        this.draggingIndices = dojo.map(nodes, function(node) {
            return source.getAllNodes().indexOf(node)
        });
    },


    // ----------------------------------------------------------
    onDndDrop: function(source, nodes, copy, target)
    {
        webcat.block(this.node);

        dojo.dnd.Source.prototype.onDndDrop.call(this,
                source, nodes, copy, target);

        this._updateRowCssClasses(target);

        this.dropIndices = dojo.map(this._nodesCreated, function(node) {
            return target.getAllNodes().indexOf(node);
        });

        this.proxy._itemsWereDropped(dojo.hitch(this, function(r) {
                webcat.unblock(this.node);
            }),
            source.node.id, this.draggingIndices,
            target.node.id, this.dropIndices, copy);

        this.draggingIndices = null;
        this.dropIndices = null;
        this._nodesCreated = [];
    },


    // ----------------------------------------------------------
    /**
     * Restores the CSS classes of the table rows after they have been dragged
     * so that the alternating color scheme is preserved.
     */
    _updateRowCssClasses: function(/*Node*/ source)
    {
        var srcnode = new dojo.NodeList(source.node);
        var index = 0;

        srcnode.query("tbody tr").forEach(dojo.hitch(this, function(node) {
            var oldClassName = null;
            dojo.some(this._rowCssClasses, dojo.hitch(this, function(className) {
                if (dojo.hasClass(node, className))
                {
                    oldClassName = className;
                    return true;
                }
                else
                {
                    return false;
                }
            }));

            if (oldClassName)
            {
                var tag = index % 2;
                if (oldClassName.length == 2 &&
                    oldClassName.charAt(1) == 'c')
                {
                    tag += 2;
                }
                else if (oldClassName.length == 2 &&
                         oldClassName.charAt(1) == 'e')
                {
                    tag += 4;
                }

                dojo.removeClass(node, oldClassName);
                dojo.addClass(node, this._rowCssClasses[tag]);
            }

            index++;
        }));
    }
});
