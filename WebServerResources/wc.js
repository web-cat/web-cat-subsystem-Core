// Should be from prototype.js, but we don't have that set up yet
var ie = false;
if (document.all) { ie = true; }

function $( id ) {
    if (ie) { return document.all[id]; } 
    else {	return document.getElementById(id);	}
}

function showHide( link, id )
{
    var img = link.getElementsByTagName("img")[0];
    var d = $(id);
    if (img.src.endsWith("expanded.gif"))
    {
        img.src = img.src.replace("expanded.gif", "collapsed.gif");
        d.style.display = "none";
    }
    else
    {
        img.src = img.src.replace("collapsed.gif", "expanded.gif");
        d.style.display = "block";
    }
    link.blur();
}
