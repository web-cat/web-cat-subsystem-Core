dojo.provide("webcat.core.AdvancedQueryAssistant");

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

    webcat.core.isEventKeyDead = function(event)
    {
        return (event.keyCode == null || (deadKeys[event.keyCode] || false));
    };

})();

dojo.declare("webcat.core.ContentAssistDataStore",
    dojo.data.api.Identity,
{
    url: "",
    entities: { },
    entityAttributeArrays: { },

    constructor: function(args)
    {
        dojo.mixin(this, args);

        dojo.xhrGet({
            url: this.url,
            handleAs: "json",
            load: dojo.hitch(this, this._processContentAssistResponse)
        });
    },

    _processContentAssistResponse: function(response, ioArgs)
    {
        // Massage the content assist info into something easier that we can
        // use.

        var entityArray = response.entities;

        dojo.forEach(entityArray, dojo.hitch(this, function(entity)
        {
            this.entities[entity.name] = { };
            this.entityAttributeArrays[entity.name] = { };

            this.entityAttributeArrays[entity.name] = entity.attributes;

            dojo.forEach(entity.attributes, dojo.hitch(this, function(attribute)
            {
                this.entities[entity.name][attribute.name] = attribute;
            }));
        }));

        return response;
    },

    getValue: function( /* item */ item,
                        /* attribute-name-string */ attribute,
                        /* value? */ defaultValue)
    {
        // The label, which gets displayed in the ComboBox's dropdown list,
        // should just be the last component of the key path represented by
        // the item.

        if (attribute == 'label')
        {
            var parts = item.split('.');
            return parts[parts.length - 1];
        }

        // Otherwise, just return the item.  (This implicitly means the "name"
        // attribute.)

        return item;
    },

    getAttributes: function(/* item */ item)
    {
        return [ 'name', 'label' ];
    },

    hasAttribute: function( /* item */ item,
                            /* attribute-name-string */ attribute)
    {
        return (attribute == 'name' || attribute == 'label');
    },

    isItem: function(/* anything */ something)
    {
        return typeof(something) == 'string';
    },

    isItemLoaded: function(/* anything */ something)
    {
        return this.isItem(something);
    },

    loadItem: function(/* object */ keywordArgs)
    {
    },

    _fetchItems: function( /* Object */ keywordArgs,
                           /* Function */ findCallback,
                           /* Function */ errorCallback)
    {
        var entityType = keywordArgs.query.rootType;
        var expr = keywordArgs.query.name;

        var components = expr.split('.');
        var keyPathSoFar = '';

        for (var i = 0; i < components.length - 1; i++)
        {
            var key = components[i];
            var attributes = this.entities[entityType];
            entityType = null;

            if (attributes)
            {
                var attribute = attributes[key];

                if (attribute)
                {
                    entityType = attribute.type;
                }
            }

            if (!entityType)
                break;

            keyPathSoFar += key + '.';
        }

        var items;

        if (entityType)
        {
            var matchString = components[components.length - 1];

            // TODO just assume "${0}*" for now
            var regex = new RegExp(
                matchString.substr(0, matchString.length - 1) + ".*");

            var attributes = this.entityAttributeArrays[entityType];
            attributes = dojo.filter(attributes, function(attribute) {
                return regex.test(attribute.name);
            });

            items = dojo.map(attributes,
                function(attribute) { return keyPathSoFar + attribute.name; });
        }
        else
        {
            items = [];
        }

        findCallback(items, keywordArgs);
    },

    close: function(/*dojo.data.api.Request || keywordArgs || null */ request)
    {
    },

    getLabel: function(/* item */ item)
    {
        return this.getValue(item, 'label');
    },

    getIdentity: function(/* item */ item)
    {
        return item;
    },

    fetchItemByIdentity: function(/* object */ keywordArgs)
    {
        keywordArgs.onItem(keywordArgs.item);
    }
});

//Mix in the simple fetch implementation to this class.
dojo.extend(webcat.core.ContentAssistDataStore, dojo.data.util.simpleFetch);


dojo.declare("webcat.core.AdvancedQueryAssistant", null,
{
    //~ Properties ...........................................................

    _updateRowTimeoutIds: [],
    _contentAssist: null,


    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    constructor: function(proxy, idPrefix, entityType, contentAssistUrl)
    {
        this.proxy = proxy;
        this.idPrefix = idPrefix;
        this.entityType = entityType;
    },


    // ----------------------------------------------------------
    _initContentAssistInfo: function(url)
    {
        dojo.xhrGet({
            url: url,
            handleAs: "json",
            load: dojo.hitch(this, function(response, ioArgs) {
                this._contentAssist =
                    new webcat.core.ContentAssistDataStore(response);
                return response;
            })
        });
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
    startBusyForRow: function(index)
    {
        this._dijitById('busy_' + index).start();
    },


    // ----------------------------------------------------------
    stopBusyForRow: function(index)
    {
        this._dijitById('busy_' + index).stop();
    },


    // ----------------------------------------------------------
    enqueueRowUpdateFromKeyUp: function(widget, event, index)
    {
        if (webcat.core.isEventKeyDead(event))
        {
            return;
        }

        this.enqueueRowUpdate(widget, widget.getValue(), index);
    },


    // ----------------------------------------------------------
    enqueueRowUpdate: function(widget, newValue, index)
    {
        this.startBusyForRow(index);

        if (this._updateRowTimeoutIds[index])
        {
           clearTimeout(this._updateRowTimeoutIds[index]);
        }

        this._updateRowTimeoutIds[index] = setTimeout(
            dojo.hitch(this, function() {
               this.proxy.immediatelySetKeyPathAtIndex(
                   dojo.hitch(this, function() {
                       var fn = this._completeId("updateRowAfterKeyPath_" + index);
                       eval(fn + "(dijit.byId('" + this._completeId("keyPath_" + index) + "'));");
                       delete this._updateRowTimeoutIds[index];
                   }),
                   newValue, index);
            }),
            500);
    },


    // ----------------------------------------------------------
    updateCastType: function(widget, event, index)
    {
        this.startBusyForRow(index);

        this.proxy.immediatelySetCastTypeAtIndex(
            dojo.hitch(this, function() {
                var fn = this._completeId("updateRowAfterCastType_" + index);
                eval(fn + "(dijit.byId('" + this._completeId("castType_" + index) + "'));");
            }),
            widget.getValue(), index);
    },


    // ----------------------------------------------------------
    updateComparison: function(widget, event, index)
    {
        this.startBusyForRow(index);

        var castTypeWidget = dijit.byId(this._completeId("castType_" + index));
        var castType = castTypeWidget ? castTypeWidget.getValue() : 0;

        this.proxy.immediatelySetComparisonAtIndex(
            dojo.hitch(this, function() {
                var fn = this._completeId("updateRowAfterComparison_" + index);
                eval(fn + "(dijit.byId('" + this._completeId("comparison_" + index) + "'));");
            }),
            widget.getValue(), castType, index);
    },


    // ----------------------------------------------------------
    updateComparandType: function(widget, event, index)
    {
        this.startBusyForRow(index);

        this.proxy.immediatelySetComparandTypeAtIndex(
            dojo.hitch(this, function() {
                var fn = this._completeId("updateRowAfterComparandType_" + index);
                eval(fn + "(dijit.byId('" + this._completeId("comparandType_" + index) + "'));");
            }),
            widget.getValue(), index);
    }
});
