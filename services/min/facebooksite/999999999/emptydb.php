<?php
// show and control medcommons facebook tables
//
// installed as apps.facebook.com/blucare with a specific list of enabled developers
//
// does not use regular medcommons facebook libraries
//
//
require_once 'adminsetup.inc.php';



$me = $_SERVER['PHP_SELF'];

$me = substr($me,0,strrpos($me,'/')+1);

// ssadedin: some sloppy code creates urls with // instead of /
// it would be nice to clean up the sloppy code, but for the sake
// of convenience we simply coalesce the doubled slashes together here
$me = str_replace("//", "/", $me);

$GLOBALS['app_url']='http://' . $_SERVER['HTTP_HOST'] . $me;

mysql_connect($GLOBALS['DB_Connection'], $GLOBALS['DB_User']) or die("facebook boostrap: error  connecting to database.");
$db = $GLOBALS['DB_Database'];
mysql_select_db($db) or die("can not connect to database $db");
$result = mysql_query("SELECT * FROM `fbapps` WHERE `key` = '$me' ") or die("$me in $db is not registered with medcommons as a healthbook application".mysql_error());
$r = mysql_fetch_object($result);
if ($r===false)  die("$me is not registered with medcommons as a healthbook application");

	$q = "delete from  users where fbid!=1107682260";
	$result = mysql_query($q) or die("cant $q ".mysql_error());
	
	$q = "delete from  patients where familyfbid!=1107682260";
	$result = mysql_query($q) or die("cant $q ".mysql_error());
	
	$q = "delete from teams";
	$result = mysql_query($q) or die("cant $q ".mysql_error());
	
	$q = "delete from  carewalls where  wallfbid!=1107682260";//wallmcid!=1013062431111407 or
	$result = mysql_query($q) or die("cant $q ".mysql_error());
	
	$q = "delete from  hblog where fbid!=1107682260";
	$result = mysql_query($q) or die("cant $q ".mysql_error());

	$buf = "
<div class=fbgreybox ><h2>Your tables have been reset.</h2><a href=index.php>back</a></div> ";
echo page_shell($buf);

?>
