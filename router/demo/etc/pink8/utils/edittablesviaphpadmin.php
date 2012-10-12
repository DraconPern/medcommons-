<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");
	
function shturl($bar,$foo)//used by visible tables to construct html

{ return "<p><a href='".$GLOBALS['phpMyAdminURL'].
"/sql.php?lang=en-iso-8859-1&server=1&db=".$GLOBALS['DB_Database']."&goto=db_details_structure.php&table=".
$bar."&sql_query=SELECT+%2A+FROM+%60".$bar."%60&pos=0'>".$foo." </a></p>";
}

function edittables()
{
	htmltop("Edit Tables via phpMyAdmin",
				PINKBOX_STYLE);
	include_once("../lib/visibletables.inc");
 
	htmlbody(
		hlevel(4,"Edit Tables via phpMyAdmin").
					visibletables());		
	echo htmlfooter();
}
edittables();
?>
