<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");

function showphpinfo()
{
	   htmltop("Show PHP Info",
				PINKBOX_STYLE);
       htmlflush();
       
	   phpinfo();
	
	
	echo htmlfooter();
}
showphpinfo();
?>

