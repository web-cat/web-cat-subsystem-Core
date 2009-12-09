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

dojo.provide("webcat.Dialog");

dojo.require("dojo.dnd.move");
dojo.require("dojo.dnd.TimedMoveable");
dojo.require("dojo.fx");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form._FormMixin");
dojo.require("dijit._DialogMixin");
dojo.require("dijit.DialogUnderlay");
dojo.require("dijit.layout.ContentPane");
dojo.require("webcat.ContentPane");
dojo.requireLocalization("dijit", "common");

//------------------------------------------------------------------------
/**
 * A dialog that inherits from webcat.ContentPane (which in turn inherits
 * from dojox.layout.ContentPane), in order to support proper behavior during
 * initial page loads and execution of scripts after a refresh.
 *
 * THIS CLASS IS A STRAIGHT COPY OF dijit.Dialog WITH THE INHERITANCE
 * CHANGED FROM dijit.layout.ContentPane TO webcat.ContentPane. IT IS CURRENTLY
 * SYNCHRONIZED WITH
 *
 *     DOJO VERSION 1.3.1.
 *
 * WHEN YOU UPGRADE THE DOJO VERSION, BE SURE TO COPY THE LATEST VERSION OF
 * dijit.Dialog FROM IT BELOW.
 *
 * @author Tony Allevato
 * @version $Id$
 */
dojo.declare(
    "webcat.Dialog",
    [webcat.ContentPane, dijit._Templated, dijit.form._FormMixin, dijit._DialogMixin],
    {
        // summary:
        //		A modal dialog Widget
        //
        // description:
        //		Pops up a modal dialog window, blocking access to the screen
        //		and also graying out the screen Dialog is extended from
        //		ContentPane so it supports all the same parameters (href, etc.)
        //
        // example:
        // |	<div dojoType="dijit.Dialog" href="test.html"></div>
        //
        // example:
        // |	var foo = new dijit.Dialog({ title: "test dialog", content: "test content" };
        // |	dojo.body().appendChild(foo.domNode);
        // |	foo.startup();

        templateString: null,
        templatePath: dojo.moduleUrl("dijit", "templates/Dialog.html"),
        attributeMap: dojo.delegate(dijit._Widget.prototype.attributeMap, {
            title: [
                { node: "titleNode", type: "innerHTML" },
                { node: "titleBar", type: "attribute" }
            ]
        }),

        // open: Boolean
        //		True if Dialog is currently displayed on screen.
        open: false,

        // duration: Integer
        //		The time in milliseconds it takes the dialog to fade in and out
        duration: dijit.defaultDuration,

        // refocus: Boolean
        // 		A Toggle to modify the default focus behavior of a Dialog, which
        // 		is to re-focus the element which had focus before being opened.
        //		False will disable refocusing. Default: true
        refocus: true,

        // autofocus: Boolean
        // 		A Toggle to modify the default focus behavior of a Dialog, which
        // 		is to focus on the first dialog element after opening the dialog.
        //		False will disable autofocusing. Default: true
        autofocus: true,

        // _firstFocusItem: [private] [readonly] DomNode
        //		The pointer to the first focusable node in the dialog.
        //		Set by `dijit._DialogMixin._getFocusItems`.
        _firstFocusItem: null,

        // _lastFocusItem: [private] [readonly] DomNode
        //		The pointer to which node has focus prior to our dialog.
        //		Set by `dijit._DialogMixin._getFocusItems`.
        _lastFocusItem: null,

        // doLayout: [protected] Boolean
        //		Don't change this parameter from the default value.
        //		This ContentPane parameter doesn't make sense for Dialog, since Dialog
        //		is never a child of a layout container, nor can you specify the size of
        //		Dialog in order to control the size of an inner widget.
        doLayout: false,

        // draggable: Boolean
        //		Toggles the moveable aspect of the Dialog. If true, Dialog
        //		can be dragged by it's title. If false it will remain centered
        //		in the viewport.
        draggable: true,

        // _fixSizes: Boolean
        //		Does this Dialog attempt to restore the width and height after becoming too small?
        _fixSizes: true,

        postMixInProperties: function(){
            var _nlsResources = dojo.i18n.getLocalization("dijit", "common");
            dojo.mixin(this, _nlsResources);
            this.inherited(arguments);
        },

        postCreate: function(){
            dojo.style(this.domNode, {
                visibility:"hidden",
                position:"absolute",
                display:"",
                top:"-9999px"
            });
            dojo.body().appendChild(this.domNode);

            this.inherited(arguments);

            this.connect(this, "onExecute", "hide");
            this.connect(this, "onCancel", "hide");
            this._modalconnects = [];
        },

        onLoad: function(){
            // summary:
            //		Called when data has been loaded from an href.
            //		Unlike most other callbacks, this function can be connected to (via `dojo.connect`)
            //		but should *not* be overriden.
            // tags:
            //		callback

            // when href is specified we need to reposition the dialog after the data is loaded
            this._position();
            this.inherited(arguments);
        },

        _endDrag: function(e){
            // summary:
            //		Called after dragging the Dialog. Calculates the relative offset
            //		of the Dialog in relation to the viewport.
            // tags:
            //		private
            if(e && e.node && e.node === this.domNode){
                var vp = dijit.getViewport();
                var p = e._leftTop || dojo.coords(e.node,true);
                this._relativePosition = {
                    t: p.t - vp.t,
                    l: p.l - vp.l
                }
            }
        },

        _setup: function(){
            // summary:
            //		Stuff we need to do before showing the Dialog for the first
            //		time (but we defer it until right beforehand, for
            //		performance reasons).
            // tags:
            //		private

            var node = this.domNode;

            if(this.titleBar && this.draggable){
                this._moveable = (dojo.isIE == 6) ?
                    new dojo.dnd.TimedMoveable(node, { handle: this.titleBar }) :	// prevent overload, see #5285
                    new dojo.dnd.Moveable(node, { handle: this.titleBar, timeout: 0 });
                dojo.subscribe("/dnd/move/stop",this,"_endDrag");
            }else{
                dojo.addClass(node,"dijitDialogFixed");
            }

            var underlayAttrs = {
                dialogId: this.id,
                "class": dojo.map(this["class"].split(/\s/), function(s){ return s+"_underlay"; }).join(" ")
            };

            var underlay = dijit._underlay;
            if(!underlay){
                underlay = dijit._underlay = new dijit.DialogUnderlay(underlayAttrs);
            }

            this._fadeIn = dojo.fadeIn({
                node: node,
                duration: this.duration,
                beforeBegin: function(){
                    underlay.attr(underlayAttrs);
                    underlay.show();
                },
                onEnd:	dojo.hitch(this, function(){
                    if(this.autofocus){
                        // find focusable Items each time dialog is shown since if dialog contains a widget the
                        // first focusable items can change
                        this._getFocusItems(this.domNode);
                        dijit.focus(this._firstFocusItem);
                    }
                })
             });

            this._fadeOut = dojo.fadeOut({
                node: node,
                duration: this.duration,
                onEnd: function(){
                    node.style.visibility="hidden";
                    node.style.top = "-9999px";
                    dijit._underlay.hide();
                }
             });
        },

        uninitialize: function(){
            var wasPlaying = false;
            if(this._fadeIn && this._fadeIn.status() == "playing"){
                wasPlaying = true;
                this._fadeIn.stop();
            }
            if(this._fadeOut && this._fadeOut.status() == "playing"){
                wasPlaying = true;
                this._fadeOut.stop();
            }
            if(this.open || wasPlaying){
                dijit._underlay.hide();
            }
            if(this._moveable){
                this._moveable.destroy();
            }
        },

        _size: function(){
            // summary:
            // 		Make sure the dialog is small enough to fit in viewport.
            // tags:
            //		private

            var mb = dojo.marginBox(this.domNode);
            var viewport = dijit.getViewport();
            if(mb.w >= viewport.w || mb.h >= viewport.h){
                dojo.style(this.containerNode, {
                    width: Math.min(mb.w, Math.floor(viewport.w * 0.75))+"px",
                    height: Math.min(mb.h, Math.floor(viewport.h * 0.75))+"px",
                    overflow: "auto",
                    position: "relative"	// workaround IE bug moving scrollbar or dragging dialog
                });
            }
        },

        _position: function(){
            // summary:
            //		Position modal dialog in the viewport. If no relative offset
            //		in the viewport has been determined (by dragging, for instance),
            //		center the node. Otherwise, use the Dialog's stored relative offset,
            //		and position the node to top: left: values based on the viewport.
            // tags:
            //		private
            if(!dojo.hasClass(dojo.body(),"dojoMove")){
                var node = this.domNode;
                var viewport = dijit.getViewport();
                    var p = this._relativePosition;
                    var mb = p ? null : dojo.marginBox(node);
                    dojo.style(node,{
                        left: Math.floor(viewport.l + (p ? p.l : (viewport.w - mb.w) / 2)) + "px",
                        top: Math.floor(viewport.t + (p ? p.t : (viewport.h - mb.h) / 2)) + "px"
                    });
                }

        },

        _onKey: function(/*Event*/ evt){
            // summary:
            //		Handles the keyboard events for accessibility reasons
            // tags:
            //		private
            if(evt.charOrCode){
                var dk = dojo.keys;
                var node = evt.target;
                if (evt.charOrCode === dk.TAB){
                    this._getFocusItems(this.domNode);
                }
                var singleFocusItem = (this._firstFocusItem == this._lastFocusItem);
                // see if we are shift-tabbing from first focusable item on dialog
                if(node == this._firstFocusItem && evt.shiftKey && evt.charOrCode === dk.TAB){
                    if(!singleFocusItem){
                        dijit.focus(this._lastFocusItem); // send focus to last item in dialog
                    }
                    dojo.stopEvent(evt);
                }else if(node == this._lastFocusItem && evt.charOrCode === dk.TAB && !evt.shiftKey){
                    if (!singleFocusItem){
                        dijit.focus(this._firstFocusItem); // send focus to first item in dialog
                    }
                    dojo.stopEvent(evt);
                }else{
                    // see if the key is for the dialog
                    while(node){
                        if(node == this.domNode){
                            if(evt.charOrCode == dk.ESCAPE){
                                this.onCancel();
                            }else{
                                return; // just let it go
                            }
                        }
                        node = node.parentNode;
                    }
                    // this key is for the disabled document window
                    if(evt.charOrCode !== dk.TAB){ // allow tabbing into the dialog for a11y
                        dojo.stopEvent(evt);
                    // opera won't tab to a div
                    }else if(!dojo.isOpera){
                        try{
                            this._firstFocusItem.focus();
                        }catch(e){ /*squelch*/ }
                    }
                }
            }
        },

        show: function(){
            // summary:
            //		Display the dialog
            if(this.open){ return; }

            // first time we show the dialog, there's some initialization stuff to do
            if(!this._alreadyInitialized){
                this._setup();
                this._alreadyInitialized=true;
            }

            if(this._fadeOut.status() == "playing"){
                this._fadeOut.stop();
            }

            this._modalconnects.push(dojo.connect(window, "onscroll", this, "layout"));
            this._modalconnects.push(dojo.connect(window, "onresize", this, function(){
                // IE gives spurious resize events and can actually get stuck
                // in an infinite loop if we don't ignore them
                var viewport = dijit.getViewport();
                if(!this._oldViewport ||
                        viewport.h != this._oldViewport.h ||
                        viewport.w != this._oldViewport.w){
                    this.layout();
                    this._oldViewport = viewport;
                }
            }));
            this._modalconnects.push(dojo.connect(dojo.doc.documentElement, "onkeypress", this, "_onKey"));

            dojo.style(this.domNode, {
                opacity:0,
                visibility:""
            });

            if(this._fixSizes){
                dojo.style(this.containerNode, { // reset width and height so that _size():marginBox works correctly
                    width:"auto",
                    height:"auto"
                });
            }

            this.open = true;
            this._onShow(); // lazy load trigger

            this._size();
            this._position();

            this._fadeIn.play();

            this._savedFocus = dijit.getFocus(this);
        },

        hide: function(){
            // summary:
            //		Hide the dialog

            // if we haven't been initialized yet then we aren't showing and we can just return
            if(!this._alreadyInitialized){
                return;
            }

            if(this._fadeIn.status() == "playing"){
                this._fadeIn.stop();
            }
            this._fadeOut.play();

            if (this._scrollConnected){
                this._scrollConnected = false;
            }
            dojo.forEach(this._modalconnects, dojo.disconnect);
            this._modalconnects = [];
            if(this.refocus){
                this.connect(this._fadeOut,"onEnd",dojo.hitch(dijit,"focus",this._savedFocus));
            }
            if(this._relativePosition){
                delete this._relativePosition;
            }
            this.open = false;
        },

        layout: function() {
            // summary:
            //		Position the Dialog and the underlay
            // tags:
            //		private
            if(this.domNode.style.visibility != "hidden"){
                dijit._underlay.layout();
                this._position();
            }
        },

        destroy: function(){
            dojo.forEach(this._modalconnects, dojo.disconnect);
            if(this.refocus && this.open){
                setTimeout(dojo.hitch(dijit,"focus",this._savedFocus), 25);
            }
            this.inherited(arguments);
        },

        _onCloseEnter: function(){
            // summary:
            //		Called when user hovers over close icon
            // tags:
            //		private
            dojo.addClass(this.closeButtonNode, "dijitDialogCloseIcon-hover");
        },

        _onCloseLeave: function(){
            // summary:
            //		Called when user stops hovering over close icon
            // tags:
            //		private
            dojo.removeClass(this.closeButtonNode, "dijitDialogCloseIcon-hover");
        }
    }
);
