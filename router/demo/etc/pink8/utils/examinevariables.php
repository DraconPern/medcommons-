<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");
function dop($s)
{	htmlbody ( $s."==".$GLOBALS[$s]."<br>");
}
function examinevariables()
{
	htmltop("Examine System Variables",
	PINKBOX_STYLE);
;
	htmlbody ("<br>***GLOBAL VARIABLES****<br>");


	dop('Partner');
	dop('ShortName');
	dop('StyleSheet');
	dop('PurpleStyleSheet');
	dop('RootURL');
	dop('HomePage');
	dop('phpMyAdminURL');


	// get these parameters, and then check to see if we are compatible with what we are expecting


	dop('SchemaMajorVersion');
	dop('SchemaMinorVersion');
	dop('TraceLevel');
	dop('SchemaComment');
	dop('SchemaDate');
	
	//
	
	dop ('DB_Connection');
	dop ('DB_Database');
	dop ('DB_User');
	dop ('SW_MajorVersion');
	dop ('SW_MinorVersion');
	dop ('SoftwareStatus');
	htmlflush();

	print ("<br>***SESSION VARIABLES***<br>");
	print_r($_SESSION);


	print ("<br>***FILES VARIABLES***<br>");
	print_r($_FILES);


	print ("<br>***REQUEST VARIABLES***<br>");
	print_r($_REQUEST);


	print ("<br>***ENV VARIABLES***<br>");
	print_r($_ENV);


	print ("<br>***SERVER VARIABLES***<br>");
	print_r($_SERVER);
	echo htmlfooter();
}
session_start();
examinevariables();
?>

