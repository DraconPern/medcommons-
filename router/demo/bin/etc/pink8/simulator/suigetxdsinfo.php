<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suigetxdsinfo.php
require_once ("../lib/htmlsubs.inc");

function xdsinfo()
{
	htmltop("Get XDS Registry Items",
				SIMULATOR_STYLE);
       
	htmlbody(
	
		formheader("../xmlws/wsgetxdsinfo.php",
		"Get XDS Registry Info").
			

	ft("StudyGuid", "studyGuid" ).


		formsubmit("Get XDS Info").
		formend()
		);			
	echo htmlfooter();
}
xdsinfo();
?>



