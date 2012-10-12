<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");

function showtable()
{
	htmltop("Show Table",
				PINKBOX_STYLE);
       
	htmlbody(
	
		formheader("../utils/showtableutil.php",
		"Not sure if all these fields are right").
		ft("table", "table" ).
		ft("filter","filter").
		formsubmit("Show Table").
		formend()
		);			
	echo htmlfooter();
}
showtable();
?>

