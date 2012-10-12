<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertorderinfo.php
require_once ("../lib/htmlsubs.inc");

function updateroutingelementinfo()
{
	htmltop("Update Routing Element Info ",SIMULATOR_STYLE);

	htmlbody(
	
		formheader("../xmlws/wsupdateroutingqueueinfo.php",
		"Not sure if all these fields are right").
		
	ft("Request Id", "requestID").
		
	ft("Order Guid", "orderGuid").
	ft("Data Guid","dataGuid" ).
	ft("Status", "globalStatus").
	ft("Bytes Total", "bytesTotal").
	ft("Bytes Transferred","bytesTransferred").
	ft("Restart Count","restartCount").
	ft("Time Started","timeStarted").
	ft("Time Completed","timeCompleted").
	

		formsubmit("Update Routing Elements").
		formend()
		);			
	echo htmlfooter();
}
updateroutingelementinfo();
?>

