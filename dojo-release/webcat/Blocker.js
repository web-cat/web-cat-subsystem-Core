/*==========================================================================*\
 |  $Id: Blocker.js,v 1.4 2010/03/15 16:48:54 aallowat Exp $
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

dojo.provide("webcat.Blocker");
dojo.require("webcat.Spinner");

//=========================================================================
/**
 * The webcat.Blocker class represents a translucent overlay that can be placed
 * on top of a node in order to indicate a long-running action, and block input
 * to widgets on the node during that action.
 *
 * While this class is publicly accessible, it is easier to use the global
 * webcat.block() and webcat.unblock() functions below to manage blockers.
 */
dojo.declare("webcat.Blocker", null,
{
    /** The length of the fade-in/out animations, in milliseconds. */
    duration: 400,

    /** Overrides the opacity of the overlay. */
    opacity: 0.6,

    /** Overrides the color of the overlay. */
    color: null,

    /** Overrides the z-index at which the ovelay should appear. */
    zIndex: null,

    /**
     * Controls whether an animated progress spinner should be centered in
     * the overlay. Defaults to true.
     */
    usesSpinner: true,

    /** The Spinner widget used by this blocker, if there is one. */
    _spinner: null,

    // ----------------------------------------------------------
    constructor: function(node, args)
    {
        dojo.mixin(this, args);

        this.node = dojo.byId(node);

        var styleOverrides = { };
        if (this.color)
            styleOverrides.backgroundColor = this.color;
        if (this.zIndex)
            styleOverrides.zIndex = this.zIndex;
        if (this.opacity)
            styleOverrides.opacity = this.opacity;

        this.overlay = dojo.create("div", {
            "class": "webcatBlocker",
            style: styleOverrides
        }, dojo.body());
    },


    // ----------------------------------------------------------
    destroy: function()
    {
        dojo.destroy(this.overlay);
    },


    // ----------------------------------------------------------
    show: function()
    {
        var pos = dojo.coords(this.node, true);
        var ov = this.overlay;

        dojo.marginBox(ov, pos);

        if (this.usesSpinner)
        {
            var widgetSize = null;
            var LMARGIN = 32, MMARGIN = 16, SMARGIN = 8;
            if (pos.w >= 72+LMARGIN && pos.h >= 72+LMARGIN)
            {
                widgetSize = "large";
            }
            else if (pos.w >= 36+MMARGIN && pos.h >= 36+MMARGIN)
            {
                widgetSize = "medium";
            }
            else if (pos.w >= 18+SMARGIN && pos.h >= 18+SMARGIN)
            {
                widgetSize = "small";
            }

            if (widgetSize)
            {
                this._spinner = new webcat.Spinner({
                    size: widgetSize
                });
                this._spinner.startup();
                this._spinner.placeAt(ov, "last");
                dojo.style(this._spinner.domNode, "position", "absolute");

                var spinnerSize = this._spinner.getPixelSize();
                var spinnerBox = {
                    w: spinnerSize,
                    h: spinnerSize,
                    l: (pos.w - spinnerSize) / 2,
                    t: (pos.h - spinnerSize) / 2
                };

                dojo.marginBox(this._spinner.domNode, spinnerBox);

                this._spinner.start();
            }
        }

        dojo.style(ov, { opacity: 0, display: "block" });
        dojo.anim(ov, { opacity: this.opacity }, this.duration,
                null, null, 250);
    },


    // ----------------------------------------------------------
    hide: function(onEnd)
    {
        dojo.fadeOut({
            node: this.overlay,
            duration: this.duration,
            onEnd: dojo.hitch(this, function() {
                dojo.style(this.overlay, "display", "none");
                if (this._spinner)
                {
                    this._spinner.destroyRecursive();
                    this._spinner = null;
                }
                if (onEnd) onEnd(this);
            })
        }).play();
    }
});


(function()
{
    // Maintains the map of blockers currently in use.
    var blockers = { };
    var id_count = 0;

    // ----------------------------------------------------------
    /**
     * Gets a unique ID for the globally created blocker.
     */
    var _uniqueId = function()
    {
        var id_base = "webcat_blocked";
        var id;

        do
        {
            id = id_base + "_" + (++id_count);
        } while(dojo.byId(id));

        return id;
    };


    dojo.mixin(webcat,
    {
    // ----------------------------------------------------------
    /**
     * Creates a blocker over the specified node.
     */
    block: function(node, args)
    {
        node = dojo.byId(node);
        var id = dojo.attr(node, "id");

        if (!id)
        {
            id = _uniqueId();
            dojo.attr(node, "id", id);
        }

        if(!blockers[id])
        {
            blockers[id] = new webcat.Blocker(node, args);
            blockers[id].show();
        }

        return blockers[id];
    },


    // ----------------------------------------------------------
    /**
     * Removes the blocker from the specified node.
     */
    unblock: function(node)
    {
        node = dojo.byId(node);
        var id = dojo.attr(node, "id");

        if (id && blockers[id])
        {
            blockers[id].hide(function(blocker) { blocker.destroy(); });
            delete blockers[id];
        }
    }

    });
})();
