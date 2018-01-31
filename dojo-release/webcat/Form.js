/*==========================================================================*\
 |  $Id: Form.js,v 1.1 2010/03/15 16:48:57 aallowat Exp $
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

dojo.provide("webcat.Form");

dojo.require("dijit.form.Form");

//------------------------------------------------------------------------
/**
 * An extension to dijit.form.Form that calls validate() on the form before it
 * is submitted (so that widget UIs are updated with error states) instead of
 * just calling isValid().
 *
 * @author Tony Allevato
 * @version $Id: Form.js,v 1.1 2010/03/15 16:48:57 aallowat Exp $
 */
dojo.declare("webcat.Form", dijit.form.Form,
{
    onSubmit: function(/*Event*/ e)
    {
        this.validate();
        return this.inherited(arguments);
    }
});
