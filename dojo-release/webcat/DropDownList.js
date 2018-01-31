/*==========================================================================*\
 |  $Id: DropDownList.js,v 1.1 2011/05/02 19:31:53 aallowat Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2011 Virginia Tech
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

dojo.provide("webcat.DropDownList");
dojo.require("webcat.global");


// --------------------------------------------------------------
webcat.dropDownList = {
    _onScrollHooks: { },
    _animations: { },
    _closeTimeouts: { }
};


//--------------------------------------------------------------
webcat.dropDownList._isNodeInListOrMenu = function(node)
{
    while (node.tagName != 'BODY')
    {
        if (dojo.hasClass(node, 'WCDropDownList')
                || dojo.hasClass(node, 'WCDropDownListMenu'))
        {
            return true;
        }

        node = node.parentNode;
    }

    return false;
};


//--------------------------------------------------------------
webcat.dropDownList._repositionMenu = function(dropDown, menu)
{
    var ddCoords = dojo.position(dropDown);
    var menuBox = dojo.marginBox(menu);

    var menuPos = { l: ddCoords.x, t: ddCoords.y + ddCoords.h };

    if (menuPos.l + menuBox.w > dijit.getViewport().w)
    {
        var newLeft = ddCoords.x + ddCoords.w - menuBox.w;

        if (newLeft >= 0)
        {
            menuPos.l = newLeft;
        }
    }

    if (menuPos.t + menuBox.h > dijit.getViewport().h)
    {
        var newTop = ddCoords.y - menuBox.h;

        if (newTop >= 0)
        {
            menuPos.t = newTop;
        }
    }

    dojo.marginBox(menu, menuPos);
};


//--------------------------------------------------------------
webcat.dropDownList._closeOpenMenu = function(quickly)
{
    var menuId = webcat.dropDownList._openMenu;
    if (!menuId) return;

    var menu = dojo.byId(menuId);

    dojo.disconnect(webcat.dropDownList._onScrollHooks[menuId]);

    if (quickly)
    {
        dojo.style(menu, 'display', 'none');
    }
    else
    {
        webcat.dropDownList._animations[menuId] =
            dojo.fadeOut({
                node: menu,
                duration: 150,
                onEnd: function() { dojo.style(menu, 'display', 'none'); }
            }).play();
    }

    delete webcat.dropDownList._onScrollHooks[menuId];

    if (webcat.dropDownList._closeTimeouts[menuId])
    {
        clearTimeout(webcat.dropDownList._closeTimeouts[menuId]);
        delete webcat.dropDownList._closeTimeouts[menuId];
    }

    delete webcat.dropDownList._openMenu;
};


//--------------------------------------------------------------
webcat.dropDownList.showDropDown = function(event, dropDownId, menuId)
{
    var dropDown = dojo.byId(dropDownId);
    var menu = dojo.byId(menuId);

    if (webcat.dropDownList._openMenu == menuId)
    {
        webcat.dropDownList._repositionMenu(dropDown, menu);
    }
    else
    {
        webcat.dropDownList._closeOpenMenu(true);

        var timeout = webcat.dropDownList._closeTimeouts[menuId];
        if (timeout)
        {
            clearTimeout(timeout);
            delete webcat.dropDownList._closeTimeouts[menuId];
        }

        var anim = webcat.dropDownList._animations[menuId];
        if (anim)
        {
            anim.stop();
            delete webcat.dropDownList._animations[menuId];
        }

        dojo.style(menu, 'opacity', '1');
        dojo.style(menu, 'display', 'block');

        webcat.dropDownList._openMenu = menuId;

        webcat.dropDownList._repositionMenu(dropDown, menu);

        webcat.dropDownList._onScrollHooks[menuId] =
            dojo.connect(window, "onscroll", null, function() {
                webcat.dropDownList._repositionMenu(dropDown, menu);
            });
    }
};


//--------------------------------------------------------------
webcat.dropDownList.hideDropDown = function(event, dropDownId, menuId, force)
{
    if (menuId != webcat.dropDownList._openMenu)
    {
        return;
    }

    var dropDown = dojo.byId(dropDownId);
    var menu = dojo.byId(menuId);

    var stayOpen;

    if (!event)
    {
        stayOpen = false;
    }
    else
    {
        var reltg = (event.relatedTarget) ? event.relatedTarget : event.toElement;
        stayOpen = webcat.dropDownList._isNodeInListOrMenu(reltg);
    }

    if (force)
    {
        webcat.dropDownList._closeOpenMenu();
    }
    else if (!stayOpen)
    {
        webcat.dropDownList._closeTimeouts[menuId] = setTimeout(
                webcat.dropDownList._closeOpenMenu, 250);
    }
};


//--------------------------------------------------------------
webcat.dropDownList.updateSelection = function(selectionId, selectedNode)
{
    dojo.byId(selectionId).innerHTML = selectedNode.innerHTML;
};
