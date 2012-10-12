
<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertorderseries.php
require_once ("../lib/htmlsubs.inc");

function insertdataseries()
{
	htmltop("Insert Data Series Into Order",SIMULATOR_STYLE);


       
	htmlbody(
	
		formheader("../xmlws/wsinsertorderseries.php",
		"Not sure if all these fields are right").
		ft("Order Guid", "orderGuid").
		ft("Data Guid" ,"dataGuid" ).
		formsubmit("Insert Data Series").
		formend()
		);			
	echo htmlfooter();
}
insertdataseries();
?>


