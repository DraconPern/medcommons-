<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// cuisimulategateway
require_once ("../lib/htmlsubs.inc");

function simulategateway()
{
	htmltop("Construct Simulated Gateway",
				PINKBOX_STYLE);
       
	htmlbody(
	
		formheader("../simulator/suipurplebox.php",
		"Not sure if all these fields are right").
		ft("MedCommons Gateway", "gateway" ).
		fcheckbox("record transactions","recording","on").
		fcheckbox("replay recorded transactions","replay","replay").
		formsubmit("Simulate Gateway").
		formend()
		);			
	echo htmlfooter();
}
simulategateway();
?>

