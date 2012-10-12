<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");

function insertroutinginfo()
{
	htmltop("Insert Routing Info",
				SIMULATOR_STYLE);
       
	htmlbody(
	
		formheader("../xmlws/wsinsertroutingqueueinfo.php",
		"Not sure if all these fields are right").
		
	ft("Request Id", "requestID").
	ft("Order Guid", "orderGuid").
	ft("Data Series Guid" ,"dataGuid" ).
	ft("Origin vAETitle", "vAETitleOrigin" ).
	ft("Destination vAETitle", "vAETitleDest" ).
	ft("Protocol", "protocol" ).
	ft("Status","globalStatus" ).
	ft("Item Type","itemType" ).

		formsubmit("Order Button").
		formend()
		);			
	echo htmlfooter();
}
insertroutinginfo();
?>
