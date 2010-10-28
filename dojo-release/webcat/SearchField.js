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

dojo.provide("webcat.SearchField");
dojo.require("webcat.global");

webcat.searchField = { };

// --------------------------------------------------------------
webcat.searchField.handleKeyUp = function(widget, event, callback)
{
    if (webcat.isEventKeyDead(event))
    {
        return;
    }

    webcat.searchField.forceChange(widget, callback);
};

//--------------------------------------------------------------
webcat.searchField.forceChange = function(widget, callback)
{
    if (widget._webcat_searchField_updateFilterTimeoutId)
    {
       clearTimeout(widget._webcat_searchField_updateFilterTimeoutId);
    }

    widget._webcat_searchField_updateFilterTimeoutId = setTimeout(
        dojo.hitch(this, function() { callback(widget); }), 500);
};
