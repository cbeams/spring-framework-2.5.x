/* Script assembled and modified by Jean-Pierre PAWLAK - jp.pawlak@tiscali.fr */

/*
	tableruler()
	written by Chris Heilmann for alistapart.
	adapted by Jean-Pierre Pawlak
	enables a rollover of rows for each table with the classname "ruler"
*/
function tableruler() {
  if (document.getElementById && document.createTextNode) {
    var tables=document.getElementsByTagName("table");
    for (var i=0;i<tables.length;i++) {
      if(tables[i].className.indexOf("ruler") >= 0) {
        var tbodies = tables[i].getElementsByTagName("TBODY");
        for (var j = 0; j < tbodies.length; j++) {
          var trs = tbodies[j].getElementsByTagName("tr");
          for(var k=0;k<trs.length;k++) {
            trs[k].onmouseover=function(){this.className+=" ruled";return false}
            trs[k].onmouseout=function(){this.className=this.className.replace(" ruled","");return false}
          }
        }
      }
    }
  }
}

/*
	tablestripe()
	from alistapart, adapted by Jean-Pierre Pawlak
	enables alterning styles for <tr> tags in body of each table whith the classname "stripe"
*/
function tablestripe() {
  if (document.getElementById && document.createTextNode) {
    var tables=document.getElementsByTagName("table");
    for (var i=0;i<tables.length;i++) {
      if(tables[i].className.indexOf("stripe") >= 0) {
        var tbodies = tables[i].getElementsByTagName("tbody");
        for (var j = 0; j < tbodies.length; j++) {
          var trs = tbodies[j].getElementsByTagName("tr");
          var even = false;
          for (var k = 0; k < trs.length; k++) {
            if (trs[k].className) {
              trs[k].className += (even ? " even" : " odd");
            } else {
              trs[k].className = (even ? "even" : "odd");
            }
            even =  ! even;
          }
        }
      }
    }
  }
}

