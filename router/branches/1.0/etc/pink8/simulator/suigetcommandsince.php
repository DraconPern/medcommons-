 
<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");

function getcommandsince()
{
	htmltop("Get Commands",
	SIMULATOR_STYLE);

	htmlbody(

	formheader("../xmlws/wsgetcommandsince.php",
	"Not sure if all these fields are right").


	ft("Since Time", "SinceTime" ).
	formsubmit("Get Command Since").
	formend()
	);
	echo htmlfooter();
}
getcommandsince();
?>




