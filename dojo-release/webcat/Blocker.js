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

dojo.provide("webcat.Blocker");

dojo.declare("webcat.Blocker", null,
{
    duration: 400,

    opacity: 0.4,

    backgroundColor: "white",

    zIndex: 999,

    // ----------------------------------------------------------
    constructor: function(node, args)
    {
        dojo.mixin(this, args);
        this.node = dojo.byId(node);

        this.overlay = dojo.doc.createElement("div");
        dojo.query(this.overlay)
            .place(dojo.body(), "last")
            .addClass("dojoBlockOverlay")
            .style({
                backgroundColor: this.backgroundColor,
                position: "absolute",
                zIndex: this.zIndex,
                display: "none",
                opacity: this.opacity
            });
    },


    // ----------------------------------------------------------
    show: function()
    {
        var pos = dojo.coords(this.node, true);
        var ov = this.overlay;

        dojo.marginBox(ov, pos);
        dojo.style(ov, { opacity:0, display:"block" });
        dojo.anim(ov, { opacity: this.opacity }, this.duration);
    },


    // ----------------------------------------------------------
    hide: function()
    {
        dojo.fadeOut({
            node: this.overlay,
            duration: this.duration,
            onEnd: dojo.hitch(this, function() {
                    dojo.style(this.overlay, "display", "none");
            })
        }).play();
    }
});


(function(){

    var blockers = {};

    var id_count = 0;

    // ----------------------------------------------------------
    var _uniqueId = function(){
            var id_base = "webcat_blocked",
                    id;
            do{
                    id = id_base + "_" + (++id_count);
            }while(dojo.byId(id));
            return id;
    };

    dojo.mixin(webcat,
    {
    // ----------------------------------------------------------
    block: function(node, args) {
        var n = dojo.byId(node);
        var id = dojo.attr(n, "id");
        if(!id)
        {
            id = _uniqueId();
            dojo.attr(n, "id", id);
        }
        if(!blockers[id])
        {
            blockers[id] = new webcat.Blocker(n, args);
        }
        blockers[id].show();
        return blockers[id];
    },


    // ----------------------------------------------------------
    unblock: function(node, args)
    {
        var id = dojo.attr(node, "id");
        if(id && blockers[id])
        {
            blockers[id].hide();
        }
    }
    });
})();
