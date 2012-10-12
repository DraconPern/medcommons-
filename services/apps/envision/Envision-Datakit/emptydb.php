<?php


require_once "setup.inc.php";
require_once "is.inc.php";
function empty_table ($table)
{
	global $GRID,$ebuf;
		$q = "delete from  $table where grid='$GRID' ";
	$result = mysql_query($q) or die("cant $q ".mysql_error());
	$ebuf.= "<p>Emptied: $table </p>";
}
global $GRID,$PROPS, $ebuf;
$ebuf = '';


$r = user_record();
if ($r===false) die("Must be logged in");

$GRID = $r->grid;
$PROPS = get_properties();
mysql_connect($GLOBALS['DB_Connection'], $GLOBALS['DB_User']) or die("facebook boostrap: error  connecting to database.");
$db = $GLOBALS['DB_Database'];
mysql_select_db($db) or die("can not connect to database $db");

$ebuf .= "<h3>Emptied all records for {$PROPS['servicename']} that are imported from Simtrak PC</h3>";

empty_table ('alerts');
empty_table ('islog');
empty_table ('leagues');
empty_table ('leagueteams');
empty_table ('players');
empty_table ('teamplayers');
empty_table ('teams');

empty_table ('FACILITY');
empty_table ('FACTRN');
empty_table ('INJURY');
empty_table ('PERSON');
empty_table ('PHYSIC');
empty_table ('PROGRS');
empty_table ('TRTMNT');
empty_table ('WEIGHT');

echo envision_page_shell ($ebuf);


?>