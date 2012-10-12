<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");


function test()
{    
	 htmltop("Test Harness - Counter Test",
				PINKBOX_STYLE);
     $v = $GLOBALS['TraceLevel'];
     htmlbody(
     "value of ONE is ".bump_counter('ONE',"fresh one").eol().
     "value of TWO is ".bump_counter('TWO',"fresh two").eol().
     "value of ONE is ".bump_counter('ONE',"should be ignored")
     )
     
     ;
      	  
	echo htmlfooter();
}
test();
?>

