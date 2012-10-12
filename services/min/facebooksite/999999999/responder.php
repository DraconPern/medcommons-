<?php

require_once "appinclude.php";

function dosql($q)
{
	if (!isset($GLOBALS['db_connected']) ){ 
		$GLOBALS['db_connected'] =
		mysql_connect($GLOBALS['DB_Connection'] ,$GLOBALS['DB_User'] );
		$db = $GLOBALS['DB_Database'];
		mysql_select_db($db) or die ("can not connect to database $db ".mysql_error());
	}
	$status = mysql_query($q);
	if (!$status) die ("dosql failed $q ".mysql_error());
	return $status;
}

function countsql($q)
{
	$result = dosql($q);
	$r = mysql_fetch_array($result);
	if (!$r) return -1;
	return $r[0];
}
function countof($q)
{
	return countsql("Select count(*) from $q ");
}
function getstats()
{
	$user_count = countof ("users");
	$user_count_with_mcid = countof ("users where mcid!='0' ");

	return <<<XXX
$user_count facebook users have loaded this app, $user_count_with_mcid have created medcommons accounts<br/>
XXX;
}


$ob="<html><head><meta http-equiv='refresh' content='60'></head><body style='font-size:12px'>";
$client = $_SERVER['REMOTE_ADDR'].':'.$_SERVER['REMOTE_PORT'];
$server =  isset($_SERVER['SCRIPT_URI']) ? $_SERVER['SCRIPT_URI'] : $_SERVER['HTTP_HOST'];
$appurl = isset($GLOBALS['facebook_application_url'] ) ?  $GLOBALS['facebook_application_url']:'no facebook_application_url';
$appname = isset($GLOBALS['healthbook_application_name']) ? $GLOBALS['healthbook_application_name']:'no_healthbook_application_name';
$appversion = isset($GLOBALS['healthbook_application_version']) ? $GLOBALS['healthbook_application_version']:'no_healthbook_application_version';

$time = strftime ('%T %D');

$ob.= "$time - $server was contacted by $client <br/>";
$ob.= "$appurl - $appname- $appversion <br/>";
$ob.=getstats();
$ob.="</body></html>";
echo $ob;
?>