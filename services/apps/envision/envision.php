<?php

require_once "simtrak.inc.php";
//require_once "stview.inc.php";
require_once "admin.inc.php";

function page_foot()
{
	// called from multiple spots
	global $playerind_,$jsonstuff_,$allplayers_,$tv_;

	$foot = <<<XXX
	</div>
	</div>
	</div>
	<script type="text/javascript">
	datax= { accounts: $allplayers_
};

mytabView = new YAHOO.widget.TabView('mystuff');

open_win = function()
{
window.open("p.php?playerind=$playerind_","_navigator","toolbar=no, location=no, directories=no, status=no, menubar=yes, scrollbars=yes, resizable=no, copyhistory=yes, width=320, height=700");
}

YAHOO.example.ItemSelectHandler = function() {
// Use a LocalDataSource
var oDS = new YAHOO.util.LocalDataSource(datax.accounts);
oDS.responseSchema = {fields : ["name", "id"]};

// Instantiate the AutoComplete
var oAC = new YAHOO.widget.AutoComplete("myInput", "myContainer", oDS);
oAC.resultTypeList = false;

// Define an event handler to populate a hidden form field
// when an item gets selected
var myHiddenField = YAHOO.util.Dom.get("myHidden");
var myHandler = function(sType, aArgs) {
var myAC = aArgs[0]; // reference back to the AC instance
var elLI = aArgs[1]; // reference to the selected LI element
var oData = aArgs[2]; // object literal of selected item's result data

// update hidden form field with the selected item's ID
myHiddenField.value = oData.id;
};
oAC.itemSelectEvent.subscribe(myHandler);

// Rather than submit the form,
// alert the stored ID instead
//var onFormSubmit = function(e, myForm) {
//YAHOO.util.Event.preventDefault(e);
//alert("MedCommons ID: " + myHiddenField.value);
//};
//YAHOO.util.Event.addListener(YAHOO.util.Dom.get("myForm"), "submit", onFormSubmit);

return {
oDS: oDS,
oAC: oAC
};
}();
</script>

<script type="text/javascript" src="Envision-DataKit/fieldedit.js"></script>

<!--=============================== -->


<!--MyBlogLog instrumentation-->
<script type="text/javascript" src="http://track2.mybloglog.com/js/jsserv.php?mblID=2007020704011645"></script>

</body>
</html>

<script type="text/javascript" src="http://us.js2.yimg.com/us.js.yimg.com/lib/rt/rto1_78.js"></script><script>var rt_page="792404008:FRTMA"; var rt_ip="72.89.255.17"; if ("function" == typeof(rt_AddVar) ){ rt_AddVar("ys", escape("F14C9345"));}</script><noscript><img src="http://rtb.pclick.yahoo.com/images/nojs.gif?p=792404008:FRTMA"></noscript><script language=javascript>
if(window.yzq_d==null)window.yzq_d=new Object();
window.yzq_d['ztTzZkLEYrM-']='&U=13esmj1e8%2fN%3dztTzZkLEYrM-%2fC%3d289534.9603437.10326224.9298098%2fD%3dFOOT%2fB%3d4123617%2fV%3d1';
</script><noscript><img width=1 height=1 alt="" src="http://us.bc.yahoo.com/b?P=jKiLskWTTNJXr_8pSKtM7wR2SFn_EUkKLNEABoBX&T=142mb4513%2fX%3d1225403601%2fE%3d792404008%2fR%3ddev_net%2fK%3d5%2fV%3d2.1%2fW%3dH%2fY%3dYAHOO%2fF%3d1646153475%2fQ%3d-1%2fS%3d1%2fJ%3dF14C9345&U=13esmj1e8%2fN%3dztTzZkLEYrM-%2fC%3d289534.9603437.10326224.9298098%2fD%3dFOOT%2fB%3d4123617%2fV%3d1"></noscript>
<!-- VER-548 -->
<script language=javascript>
if(window.yzq_p==null)document.write("<scr"+"ipt language=javascript src=http://l.yimg.com/us.js.yimg.com/lib/bc/bc_2.0.4.js></scr"+"ipt>");
</script><script language=javascript>
if(window.yzq_p)yzq_p('P=jKiLskWTTNJXr_8pSKtM7wR2SFn_EUkKLNEABoBX&T=13tqoqrct%2fX%3d1225403601%2fE%3d792404008%2fR%3ddev_net%2fK%3d5%2fV%3d1.1%2fW%3dJ%2fY%3dYAHOO%2fF%3d3173136303%2fS%3d1%2fJ%3dF14C9345');
if(window.yzq_s)yzq_s();
</script><noscript><img width=1 height=1 alt="" src="http://us.bc.yahoo.com/b?P=jKiLskWTTNJXr_8pSKtM7wR2SFn_EUkKLNEABoBX&T=142h28obv%2fX%3d1225403601%2fE%3d792404008%2fR%3ddev_net%2fK%3d5%2fV%3d3.1%2fW%3dJ%2fY%3dYAHOO%2fF%3d1257063833%2fQ%3d-1%2fS%3d1%2fJ%3dF14C9345"></noscript>
<!-- p2.ydn.re1.yahoo.com compressed/chunked Thu Oct 30 14:53:21 PDT 2008 -->

XXX;
return $foot;
}


$mcid_=0;
$admin=isset($_REQUEST['admin']);
//if (isset($_POST['myHidden'])) $mcid_=$_POST['myHidden']; else

	// only load all this extra stuff if we are admin
	envision_admin_page(); // this is url processing gets done for the variou7s postbacks


?>
