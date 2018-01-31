/*==========================================================================*\
 |  $Id: ValidationResults.js,v 1.2 2010/01/23 02:32:41 aallowat Exp $
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

dojo.provide("webcat.ValidationResults");

//=========================================================================
/**
 * TODO real description
 */
dojo.declare("webcat.ValidationResults", null,
{
    //~ Properties ............................................................

    /**
     * Stores the messages that resulted from server-side validation.
     */
    widgetMessages: { },


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    set: function(/*String*/ id, /*String*/ message)
    {
        if (message)
        {
            this.widgetMessages[id] = message;
        }
        else
        {
            delete this.widgetMessages[id];
        }
    },


    // ----------------------------------------------------------
    setAll: function(/*Object*/ results)
    {
        this.widgetMessages = results;
    },


    // ----------------------------------------------------------
    get: function(/*String*/ id)
    {
        return this.widgetMessages[id];
    },


    // ----------------------------------------------------------
    validateWidget: function(/*dijit.form.ValidationTextBox*/ widget,
            /*String*/ elementID, /*Object*/ value, /*Object*/ constraints)
    {
        var msg = null;

        if (widget.prevalidator)
        {
            msg = widget.prevalidator(value, constraints);
        }

        if (!msg)
        {
            msg = this.get(elementID);
        }

        if (msg)
        {
            widget.invalidMessage = msg;
            return false;
        }
        else
        {
            widget.invalidMessage = null;
            return true;
        }
    }
});
