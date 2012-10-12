<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertorderinfo.php
require_once ("../lib/htmlsubs.inc");

function insertorder()
{
	htmltop("Insert Order",SIMULATOR_STYLE);


       
	htmlbody(
	
		formheader("../xmlws/wsinsertorder.php",
		"New fields on 20Oct04").
		
	ft("Tracking", "tracking").	
	ft("Order Guid", "orderGuid").
	ft("Description" ,"description" ).
	ft("Origin vAETitle", "vAETitleOrigin" ).
	ft("Destination vAETitle", "vAETitleDest" ).
	ft("Patient Name", "patientName" ).
	ft("Patient Id","patientId" ).
	ft("Modality","modality").
	ft("Initial Status","status" ).

		formsubmit("Submit Order").
		formend()
		);			
	echo htmlfooter();
}
insertorder();
?>
