<?php
// store bits (mostly) in cloud (hopefully)
//
// this service can run standalone and has its own simple datbase
//
require_once 'dbcreds.inc.php';
require_once 'JSON.php';

function dosql($q)
{
	
	$status = mysql_query($q);
	if (!$status) die ("dosql failed $q ".mysql_error());
	return $status;
}           
              
function doit($field)
{
if (!isset($_REQUEST[$field])) return '';
return mysql_real_escape_string($_REQUEST[$field]);
}

// main starts here   -- first connect to database
if (!isset($GLOBALS['db_connected']) ){
		$GLOBALS['db_connected'] = mysql_connect($GLOBALS['DB_Connection'] ,$GLOBALS['DB_User'] );
		$db = $GLOBALS['DB_Database'];
		mysql_select_db($db) or die ("can not connect to database $db ".mysql_error());
	}
	
// main starts here - sanitize the incoming fields

$fields = array('photo_type','file_suffix','uid','iphone_time','latitude','longitude','verticalaccuracy','horizontalaccuracy');
foreach ($fields as $x)  $$x = doit($x);

$op = $photo_type;
$data = file_get_contents('php://input');
$time = time();
$pos = strpos($data,'bits=')+5;  // careful, we added &fin
$newstring = substr($data,$pos); // fix up the PNG stuff
$fs = $file_suffix;

$fn = 'pics/iphone.'.$uid.'.'.$fs;

file_put_contents($fn,$newstring);
$count = strlen($newstring);


$bfn = //'/probe/ws/'.
      '/midatastore/'.  $fn;

dosql ("insert into iphoneBits set reqtime = '$time',  reqop = '$op', reqlen = '$count',reqdata ='$bfn', iphonetime='$iphone_time',
latitude='$latitude',
longitude='$longitude',
verticalaccuracy='$verticalaccuracy',
horizontalaccuracy='$horizontalaccuracy',
uid='$uid',
server = '{$_SERVER['SERVER_ADDR']}',
remote = '{$_SERVER['REMOTE_ADDR']}', 
servername = '{$_SERVER ['SERVER_NAME']}',
filesuffix = '$fs'");

	
// Ensure the content type indicates javascript
header ("Content-type: text/plain");
$json = new Services_JSON();
$out = new stdClass;
$out->status = "ok";
$out->remotebytes = $count;
$out->remotetimestamp = time();
$out->remoteurl = 'http://'.$_SERVER['SERVER_NAME'].$bfn;
$out->remotesha1 =sha1($newstring);
// Because JSON encode sometimes emits warnings in the middle of output!
error_reporting(0);
echo $json->encode($out);
?>
