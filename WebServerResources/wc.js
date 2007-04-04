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
    if (img.src.indexOf("expanded.gif") > 0 )
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

function expandAll()
{
    var anchors = document.getElementsByTagName("a");
//    var anchors = document.anchors;
    for ( var i = 0; i < anchors.length; i++ )
    {
    	var a = anchors[i];
        if ( a.title.match(/show|hide/i) )
        {
            var img = a.firstChild;
	if ( img && img.nodeName.toLowerCase() == "img" )
        {
//            var img = a.firstChild;
            if ( img.src.indexOf( "collapsed.gif" ) > 0 )
            {
//            	eval( a.onClick );
		a.onclick();
            }
        }
        }
    }
}
