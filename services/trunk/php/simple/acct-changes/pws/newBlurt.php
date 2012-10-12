<?php
// write a bunch of fields to the iPhoneMeta blurt log
require 'settings.php';
require_once 'utils.inc.php';
require_once 'dbparams.inc.php';
require_once "JSON.php";
require_once "login.inc.php";


function dosql($q)
{
	if (!isset($GLOBALS['db_connected']) ){
		$GLOBALS['db_connected'] = mysql_connect($GLOBALS['DB_Connection'] ,$GLOBALS['DB_User'] );
		$db = $GLOBALS['DB_Database'];
		mysql_select_db($db) or die ("can not connect to database $db ".mysql_error());
	}
	$status = mysql_query($q);
	if (!$status) die ("dosql failed $q ".mysql_error());
	return $status;
}
function b($s)
{
	$ret = '';
	if (isset($_REQUEST["$s"]))
	$ret = $_REQUEST["$s"];
	return mysql_real_escape_string($ret);
}

// main starts here


		$reqid = req('reqid');
		if(!$reqid) $reqid = '-1';



$auth = req('auth');
if(!$auth)
throw new ValidationFailure('auth not provided');

$result = dosql ("Select * from authentication_token where at_token = '$auth' and at_priority='G'");
$token = mysql_fetch_object ($result);


//$token = pdo_first_row("select * from authentication_token where at_token = ? and at_priority = 'G'", array($auth));
if(!$token)
throw new Exception("Unknown auth token or not authorized for group.");

$groupAccountId = $token->at_account_id;

$result = dosql ("select * from groupinstances where accid = '$groupAccountId'");
$group = mysql_fetch_object ($result);
if(!$group)
throw new Exception("Unknown group");



if (!isset($_REQUEST['uid']))throw new Exception("Unique device id must be specified");
$uid = $_REQUEST['uid'];

//$userrec = User::from_auth_token($auth);
//$mc = $userrec -> mcid;

if (!isset($_REQUEST['pid']))throw new Exception("patient id must be specified");
$mc = $_REQUEST['pid'];


$fn = b('fn'); $ln = b('ln'); $dob = b('dob'); $sender = b('sender'); $comment = b('comment'); $series = b('series');
$soapS = b('soapS'); $soapA = b('soapA'); $soapP = b('soapP');$soapD = b('soapD');$soapC = b('soapC');




$time = time();
$pin = rand(10000,99999);
$vid = rand(1000000,9999999);
$url = '';//	'http://'.$_SERVER ['SERVER_NAME'].'/probe/gallery.php?vid='.$vid;
dosql ("insert into iphoneMeta set reqtime = '$time',  vid = '$vid', uid='$uid', pin='$pin',
fn = '$fn' , ln = '$ln', dob = '$dob', sender='$sender', comment='$comment', series='$series',mc='$mc',
soapS = '$soapS',soapA = '$soapA',soapP = '$soapP',soapD = '$soapD',soapC = '$soapC',
server = '{$_SERVER['SERVER_ADDR']}', remote = '{$_SERVER['REMOTE_ADDR']}',
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
$out->reqid = $reqid;
$out->noteid = mysql_insert_id();

// Because JSON encode sometimes emits warnings in the middle of output!
error_reporting(0);
echo $json->encode($out);

?>