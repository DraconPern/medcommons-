<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertorderinfo.php
require_once ("../lib/htmlsubs.inc");

function updateorderstatus()
{
	htmltop("Update Order Status",SIMULATOR_STYLE);


       
	htmlbody(
	
		formheader("../xmlws/wsupdateorderstatus.php",
		"Not sure if all these fields are right").
		
		
	ft("Order Guid", "OrderGuid").
	ft("Order Status","OrderStatus" ).
	ft("Number series","series").
	ft("Number of images","images").

		formsubmit("Update Status").
		formend()
		);			
	echo htmlfooter();
}
updateorderstatus();
?>
