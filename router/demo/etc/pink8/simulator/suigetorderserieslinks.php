  
<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");

function getorderdataitems()
{
	htmltop("Get Order Data Series Items",
				SIMULATOR_STYLE);
       
	htmlbody(
	
		formheader("../xmlws/wsgetorderserieslinks.php",
		"Not sure if all these fields are right").
			

	ft("Order Guid", "OrderGuid" ).


		formsubmit("Get Items").
		formend()
		);			
	echo htmlfooter();
}
getorderdataitems();
?>



