/*==========================================================================*\
 |  $Id: FileInput.js,v 1.2 2011/11/08 14:04:35 aallowat Exp $
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

dojo.provide("webcat.FileInput");

dojo.require("dojox.form.FileInputAuto");
dojo.require("webcat.Spinner");

//------------------------------------------------------------------------
/**
 * An extension to the dojox.form.FileInputAuto control with some minor
 * modifications for layout.
 *
 * @author Tony Allevato
 * @version $Id: FileInput.js,v 1.2 2011/11/08 14:04:35 aallowat Exp $
 */
dojo.declare("webcat.FileInput", dojox.form.FileInputAuto,
{
    /**
     * If false, the filename of the uploaded file will remain in the input
     * field after the file is uploaded (best used when the control is intended
     * to upload a single file). If true, the input field will be cleared (best
     * used when the control is used to upload multiple files and other UI
     * feedback is provided). Defaults to false.
     */
    clearAfterUpload: false,

    blurDelay: 0,
    templateString: dojo.cache("webcat","templates/FileInput.html"),
    triggerEvent: "onchange",
    uploadMessage: "Uploading ${filename}...",
    widgetsInTemplate: true,

    _hasUploaded: false,
    _lastUploadedFilename: "",


    // ----------------------------------------------------------
    startup: function()
    {
        this.inherited(arguments);

        this.inputNode.validator = dojo.hitch(this, function(value, constraints) {
            return this._hasUploaded
                || (this.validator && this.validator(value, constraints));
        });

        dojo.style(this.messageNode, "vertical-align", "middle");

        this._resizeNode();
    },


    // ----------------------------------------------------------
    reset: function(e)
    {
        // summary: accomodate our extra focusListeners
        if(this._blurTimer){ clearTimeout(this._blurTimer); }

        this.disconnect(this._blurListener);
        this.disconnect(this._focusListener);

        this.fakeNodeHolder.style.display = "";

        // summary: on click of cancel button, since we can't clear the input because of
        // 	security reasons, we destroy it, and add a new one in it's place.
        this.disconnect(this._listener);
        this.disconnect(this._keyListener);
        if(this.fileInput){
            this.domNode.removeChild(this.fileInput);
        }

        // should we use cloneNode()? can we?
        this.fileInput = document.createElement('input');
        // dojo.attr(this.fileInput,{
        //	"type":"file", "id":this.id, "name": this.name
        //});
        this.fileInput.setAttribute("type","file");
        this.fileInput.setAttribute("id", this.id);
        this.fileInput.setAttribute("name", this.name);
        dojo.addClass(this.fileInput,"dijitFileInputReal");
        this.domNode.appendChild(this.fileInput);

        this._keyListener = this.connect(this.fileInput, "onkeyup", "_matchValue");
        this._listener = this.connect(this.fileInput, "onchange", "_matchValue");

        this.inputNode.attr('value',
            this.clearAfterUpload ? "" : this._lastUploadedFilename);

        if (this.messageNode.firstChild)
        {
            this.messageNode.removeChild(this.messageNode.firstChild);
        }

        this._sent = false;
        this._sending = false;
        this._blurListener = this.connect(this.fileInput, this.triggerEvent,"_onBlur");
        this._focusListener = this.connect(this.fileInput,"onfocus","_onFocus");

        this._resizeNode();
    },


    // ----------------------------------------------------------
    _getValueAttr: function()
    {
        return this._lastUploadedFilename;
    },


    // ----------------------------------------------------------
    _resizeNode: function()
    {
        var totalArea = dojo.marginBox(this.fakeNodeHolder);
        dojo.marginBox(this.domNode, { w: totalArea.w, h: totalArea.h });
        dojo.marginBox(this.fileInput, { w: totalArea.w, h: totalArea.h });
    },


    // ----------------------------------------------------------
    _matchValue: function()
    {
        var path = this.fileInput.value.replace(/^.*(\\|\/)/, '');

        this._lastUploadedFilename = path;
        this.inputNode.attr('value', path);
    },


    // ----------------------------------------------------------
    setMessage: function(/*String*/title)
    {
        // summary: set the text of the progressbar

        this.spinner.start();

        if (this.messageNode.firstChild)
        {
            this.messageNode.removeChild(this.messageNode.firstChild);
        }

        title = dojo.string.substitute(title, {
            filename: this.fileInput.value.replace(/^.*(\\|\/)/, '')
        });
        this.messageNode.appendChild(document.createTextNode(title));
    },


    // ----------------------------------------------------------
    _sendFile: function(/* Event */e)
    {
        // summary: triggers the chain of events needed to upload a file in the background.
        if(this._sent || this._sending || !this.fileInput.value){ return; }

        this._sending = true;

        dojo.style(this.fakeNodeHolder,"display","none");

        this.setMessage(this.uploadMessage);

        var _newForm;
        if(dojo.isIE){
            // just to reiterate, IE is a steaming pile of code.
            _newForm = document.createElement('<form enctype="multipart/form-data" method="post">');
            _newForm.encoding = "multipart/form-data";

        }else{
            // this is how all other sane browsers do it
            _newForm = document.createElement('form');
            _newForm.setAttribute("enctype","multipart/form-data");
        }
        _newForm.appendChild(this.fileInput);

        dojo.body().appendChild(_newForm);

        // For some reason if handleAs is "javascript", the handle callback
        // doesn't actually get called; so we just use "text" as the response
        // type and eval it ourselves in the callback.
        dojo.io.iframe.send({
            url: this.url,
            form: _newForm,
            handleAs: "text",
            handle: dojo.hitch(this, this._handleSend)
        });
    },


    // ----------------------------------------------------------
    _handleSend: function(data, ioArgs)
    {
        // summary: The callback to toggle the progressbar, and fire the user-defined callback

        dojo.eval(data);

        this._hasUploaded = true;
        this._sent = true;
        this._sending = false;

        this.spinner.stop();
        this.fileInput.style.display = "none";
        this.fakeNodeHolder.style.display = "none";

        this.disconnect(this._blurListener);
        this.disconnect(this._focusListener);

        //remove the form used to send the request
        dojo.body().removeChild(ioArgs.args.form);
        this.fileInput = null;
        this.inputNode.attr('value', null);

        this.reset(null);
    },


    // ----------------------------------------------------------
    isValid: function()
    {
        if (this.validator)
        {
            return this.validator(null, null);
        }
        else
        {
            return true;
        }
    },


    // ----------------------------------------------------------
    validate: function()
    {
        if (this.inputNode.validate())
        {
            this.inputNode.attr('value', '');
        }
        else
        {
            this.inputNode.attr('value', this.invalidMessage);
        }
    }
});
