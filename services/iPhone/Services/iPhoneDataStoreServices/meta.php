<?php
// store metadata (mostly) in cloud (hopefully)
//
// this service can run stand alone and has its own single table database


require_once 'dbcreds.inc.php';
require_once "JSON.php";
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
	


$fields = array('fn','ln','dob','uid','sender','comment','series','mc','iphone_time','email','notes');
foreach ($fields as $x)  $$x = doit($x);
/*
$fn='';
if (isset($_POST['fn']))
$fn = $_POST['fn'];

$ln='';
  if (isset($_POST['ln']))
$ln = $_POST['ln'];

$dob ='';
if (isset($_POST['dob']))
$dob = $_POST['dob'];

$uid='';
if (isset($_POST['uid']))
$uid = $_POST['uid'];

$sender='';
  if (isset($_POST['sender']))
$sender = $_POST['sender'];

$comment ='';
  if (isset($_POST['comment']))
$comment = $_POST['comment'];

$series='';
if (isset($_POST['series']))
$series = $_POST['series'];


$mc = $_POST['mc'];
*/

$time = time();
//$data = '230038408bits=777772309840928340980923840982309840982309480';
$pin = rand(10000,99999);
//$vid = md5(rand(0,10000000000).$time);
$vid = rand(1000000,9999999);
//$url = 	'http://'.$_SERVER ['SERVER_NAME'].'/probe/gallery.php?vid='.$vid;    
dosql ("insert into iphoneMeta set reqtime = '$time',  vid = '$vid', uid='$uid', pin='$pin', 
			fn = '$fn' , ln = '$ln', dob = '$dob', sender='$sender', comment='$comment', series='$series',mc='$mc',
			email = '$email', notes='$notes',
			iphonetime='$iphone_time', 
			server = '{$_SERVER['SERVER_ADDR']}', 
			remote = '{$_SERVER['REMOTE_ADDR']}', 
			servername = '{$_SERVER ['SERVER_NAME']}'");
			
			      // Ensure the content type indicates javascript
			      
header ("Content-type: text/plain");
$json = new Services_JSON();
$out = new stdClass;
$out->status = "ok";
$out->voucherid = $vid;
$out->remotetimestamp = $time;
$out->remoteseriessha1 =sha1($fn.$ln.$dob.$uid.$sender.$comment.$series.$time);
$out->pin = $pin;
$out->pickupurl = 'http://'.$_SERVER['SERVER_NAME']."/midatastore/pickup.php";


// Because JSON encode sometimes emits warnings in the middle of output!
error_reporting(0);
echo $json->encode($out);

?>