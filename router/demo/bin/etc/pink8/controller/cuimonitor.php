<?php
require_once ("../lib/htmlsubs.inc");

//pink/box home page
// (c) 2004 MedCommons, Inc.
// wld 8/28/04

function shturl($bar,$foo)
{ return "<br><a href='../utils/showtableutil.php?table=".$bar."'>".$foo." </a>";
} 


function pinkboxmonitor()
{
	htmltop($GLOBALS['Partner']." Pink Box Monitor",
					PINKBOX_STYLE);
       
	htmlbody(
		"Welcome to the MedCommons Pink Box &nbsp;".eol().
		hlevel(2,"Pink Box Controller Panel").eol().
			alink("../controller/cuisendcommandtogateway.php","Send Command").
		"to any purple box - these are not 'Medical Commands'".eol().
			alink("../controller/cuishowgatewaystatus.php","Show State")."of any purple box".eol().
		hlevel(3,"Dump Database Tables"));
		//	alink("../controller/cuishowTable.php","Show Table")." as HTML ( ");

		include_once("../lib/visibletables.inc");
 		htmlbody( visibletables());
 echo htmlfooter();
}
 
 pinkboxmonitor();
?>