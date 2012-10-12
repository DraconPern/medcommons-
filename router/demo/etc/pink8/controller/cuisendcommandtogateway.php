
<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");

function sendcommand()
{
	htmltop("Send Command to Gateway",
				PINKBOX_STYLE);
       
	htmlbody(
	
		formheader("../utils/sendcommandutil.php",
		"Not sure if all these fields are right").
		ft("Gateway", "Purple_gw" ).
		ft("CommandCode","CommandCode").	
		ft("CommandString","CommandString").

		formsubmit("Send Command to Gateway").
		formend()
		);			
	echo htmlfooter();
}
sendcommand();
?>

