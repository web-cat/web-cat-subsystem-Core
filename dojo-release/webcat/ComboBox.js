dojo.provide("webcat.ComboBox");

dojo.require("dijit.form.FilteringSelect");

dojo.declare("webcat.ResizingComboBoxMixin", null,
{
    maximumWidth: Number.MAX_VALUE,

    resizeToFitOptions: function()
    {
        var maxWidth = 32;
        var tb = this.textbox;
        var dn = this.domNode;

        dojo.query("> option", this.srcNodeRef).forEach(function(node)
        {
            var attrs = {
                innerHTML: node.innerHTML,
                style: {
                    position: "absolute",
                    left: -10000,
                    top: -10000,
                    fontFamily: dojo.style(tb, "font-family"),
                    fontSize: dojo.style(tb, "font-size"),
                    padding: dojo.style(tb, "padding"),
                    margin: dojo.style(tb, "margin")
                }
            };

            var el = dojo.create("span", attrs, dojo.body());
            
            var w = dojo.marginBox(el).w;
            if (w > maxWidth) maxWidth = w;
            
            dojo.destroy(el);
        });

        var diff = dojo.marginBox(dn).w - dojo.marginBox(tb).w;
        
        var newWidth = maxWidth + diff + 4;
        if (newWidth > this.maximumWidth)
            newWidth = this.maximumWidth;

        dojo.marginBox(dn, { w: newWidth });
    },
    

    _postCreate: function()
    {
        this.resizeToFitOptions();
    }
});


dojo.declare("webcat.FilteringSelect",
    [dijit.form.FilteringSelect, webcat.ResizingComboBoxMixin],
{
    postCreate: function(){
        dijit.form.FilteringSelect.prototype.postCreate.apply(this, arguments);
        webcat.ResizingComboBoxMixin.prototype._postCreate.apply(this, arguments);
    }
});
