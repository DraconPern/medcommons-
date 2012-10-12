<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertxdsinfo.php
require_once ("../lib/htmlsubs.inc");

function updatexdsstatus()
{
	htmltop("Update XDS Status",SIMULATOR_STYLE);


       
	htmlbody(
	
		formheader("../xmlws/wsupdatexdsstatus.php",
		"Update XDS Registry Item Status").
		
		
	ft("study Guid", "studyGuid").
	ft("status","status" ).

		formsubmit("Update Status").
		formend()
		);			
	echo htmlfooter();
}
updatexdsstatus();
?>