dojo.provide("webcat.core.FilteringUserSelector");

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

    webcat.core.filteringUserSelector = { };

    webcat.core.filteringUserSelector.isEventKeyDead = function(event)
    {
        return (event.keyCode == null || (deadKeys[event.keyCode] || false));
    };

})();

dojo.declare("webcat.core.FilteringUserSelector", null,
{
    //~ Properties ...........................................................

    _updateFilterTimeoutId: null,

    proxy: null,
    idPrefix: "",

    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    constructor: function(/* Object */ args)
    {
        dojo.mixin(this, args);
    },


    // ----------------------------------------------------------
    _completeId: function(suffix)
    {
        return this.idPrefix + '_' + suffix;
    },


    // ----------------------------------------------------------
    _dojoById: function(suffix)
    {
        return dojo.byId(this._completeId(suffix));
    },


    // ----------------------------------------------------------
    _dijitById: function(suffix)
    {
        return dijit.byId(this._completeId(suffix));
    },


    // ----------------------------------------------------------
    _jsById: function(suffix)
    {
        return eval(this._completeId(suffix));
    },


    // ----------------------------------------------------------
    updateFilterFromKeyUp: function(widget, event)
    {
        if (webcat.core.filteringUserSelector.isEventKeyDead(event))
        {
            return;
        }

        this.updateFilter(widget, widget.getValue());
    },


    // ----------------------------------------------------------
    updateFilter: function(widget, newValue)
    {
        this._dijitById("searchBusyIndicator").start();

        //this.startBusyForRow(index);

        if (this._updateFilterTimeoutId)
        {
           clearTimeout(this._updateFilterTimeoutId);
        }

        this._updateFilterTimeoutId = setTimeout(
            dojo.hitch(this, function() {
               this.proxy.updateFilter(
                   dojo.hitch(this, function() {
                       this._dijitById("searchBusyIndicator").stop();
                       webcat.refresh(this._completeId("availableUsersPane"));
                       delete this._updateFilterTimeoutId;
                   }),
                   newValue);
            }),
            500);
    },


    // ----------------------------------------------------------
    addToSelectedUsers: function(widget)
    {
        var availableUsersTable = this._jsById("availableUsersTable");
        var selNodes = availableUsersTable.getSelectedNodes();

        var selIndices = dojo.map(selNodes, function(node) {
            return availableUsersTable.getAllNodes().indexOf(node);
        });

        this.proxy.addToSelectedUsers(
            dojo.hitch(this, function() {
                webcat.refresh(this._completeId("selectedUsersPane"));
            }),
            selIndices);
    },


    // ----------------------------------------------------------
    deleteFromSelectedUsers: function(widget)
    {
        var selectedUsersTable = this._jsById("selectedUsersTable");
        var selNodes = selectedUsersTable.getSelectedNodes();

        var selIndices = dojo.map(selNodes, function(node) {
            return selectedUsersTable.getAllNodes().indexOf(node);
        });

        this.proxy.deleteFromSelectedUsers(
            dojo.hitch(this, function() {
                webcat.refresh(this._completeId("selectedUsersPane"));
            }),
            selIndices);
    }
});
