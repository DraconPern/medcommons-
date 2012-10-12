<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");
function shturl($bar,$foo)//must come  before subsequent include
{ return "<p><a href='../xmlws/wsshowxmltable.php?gateway=".$GLOBALS['gw'].
			"&table=".$bar."'> "
				.$foo." </a></p> ";
} 
function testxmlwebservices()
{
	
	


	$rec= cleanreq('recording');
	$gw = cleanreq('gateway');
	htmltop("Test XML Web Services from $gw",
				SIMULATOR_STYLE);

	if ($rec!="") $gw.="&recording=".$rec;
	$GLOBALS['gw']= $gw; //to pass into shturl

    include_once("../lib/visibletables.inc");
 
	htmlbody(
		hlevel(4,"XML Data Dumper -responses in XML").
					visibletables());		
	echo htmlfooter();
}
testxmlwebservices();
?>




