
<?php
//replace portrait URL associated with one mcid
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


// main starts here

$auth = req('auth');
if(!$auth)
throw new ValidationFailure('auth not provided');

$result = dosql ("Select * from authentication_token where at_token = '$auth' and at_priority='G'");
$token = mysql_fetch_object ($result);


//$token = pdo_first_row("select * from authentication_token where at_token = ? and at_priority = 'G'", array($auth));
if(!$token)
throw new Exception("Unknown auth token or not authorized for group.");




if (!isset($_REQUEST['uid']))throw new Exception("Unique device id must be specified");
$uid = $_REQUEST['uid'];

//$userrec = User::from_auth_token($auth);
//$mcid = $userrec -> mcid;

if (!isset($_REQUEST['mcid']))throw new Exception("mcid must be specified");
$mcid = $_REQUEST['mcid'];

if (!isset($_REQUEST['url']))throw new Exception("photo url  must be specified");
$url = $_REQUEST['url'];

$reqid = req('reqid');
if(!$reqid) $reqid = '-1';

// now find all the records we need
// first make sure we have a record here
$status = "oK";
$last = "no mysql calls";
$results = dosql ("select * from  users where mcid='$mcid' ");
$obj = mysql_fetch_object($results);
if (!$obj)
{$status = "Couldnt find mcid $mcid";
$last  = mysql_error();
}
else
{
$results = dosql ("update users set photoUrl='$url' where mcid='$mcid' ");
$last  = mysql_error();
if ($last) $status = "Cant update users";
}

header ("Content-type: text/plain");
$json = new Services_JSON();
$out = new stdClass;
$out->status = $status;
$out->mysql = $last;


// Because JSON encode sometimes emits warnings in the middle of output!
error_reporting(0);
echo $json->encode($out);

?>