<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");

function showpurplestatus()
{
	htmltop("Show Gateway Status",
				PINKBOX_STYLE);
       
	htmlbody(
	
		formheader("../utils/showpurplestatushtml.php",
		"Not sure if all these fields are right").
			

	ft("Gateway", "Gateway" ).


		formsubmit("Get Gateway Status").
		formend()
		);			
	echo htmlfooter();
}
showpurplestatus();
?>



