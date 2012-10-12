<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suigetorder.php
require_once ("../lib/htmlsubs.inc");

function getorderinfo()
{
	htmltop("Get Order Data Series Items",
				SIMULATOR_STYLE);
       
	htmlbody(
	
		formheader("../xmlws/wsgetorderinfo.php",
		"Get Order Info").
			

	ft("Order Guid", "OrderGuid" ).


		formsubmit("Get Order Info").
		formend()
		);			
	echo htmlfooter();
}
getorderinfo();
?>



