  
<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");

function getroutingqueueitems()
{
	htmltop("Get Routing Queue Items",
				SIMULATOR_STYLE);
       
	htmlbody(
	
		formheader("../xmlws/wsgetroutingqueueitems.php",
		"Not sure if all these fields are right").

		ft("Target vAETitle", "destvAETitle" ).

		formsubmit("Get Items").
		formend()
		);			
	echo htmlfooter();
}
getroutingqueueitems();
?>


