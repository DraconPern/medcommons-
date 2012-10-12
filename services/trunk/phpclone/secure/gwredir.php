<?php
//  handling for ?a opcode args - all go to tracking.jsp
require "dbparams.inc.php";
require "session.inc.php";
require "securelib.inc.php";

function xexec ($s, $p)
{
	$result = mysql_query($s) or die("Can not query in xexec $p ".mysql_error());
	if ($result=="") {exit;}
	return $result;
}
// start here

dbconnect();
if (isset($_REQUEST['a']))$op=$_REQUEST['a']; // get operation code
else $op = ''; // presumably will be handled by default case below

// if logged on, pass that infor along

$accid=""; $email = ""; $idp = "";  $auth="";
;
if (isset($_COOKIE['mc']))
{
    $info = get_account_info();
    $accid = $info->accid;
    $auth = $info->auth;
    $email = $info->email;
}

if(!isset($accid) || ($accid==="")) 
  $accid='0000000000000000';

if (!isset($idp)) $idp='pops';

$page="tracking.jsp";
$extraparams="";
$storageId = $accid;
if(isset($_REQUEST['storageId'])) {
  $storageId = $_REQUEST['storageId'];
}

// fix opcode to conform to simon's implementation
switch ($op) {
	case 'CreateCCR' : {$op = 'new'; if ($email!='') $op.="&from=".urlencode($email);break;}
	case 'ImportCCR' : {$op = 'import'; break;}
	case 'OpenCCR':    {$op = 'saved'; break;}
	case 'AddDocument':{$op = 'addDocument'; $page="accountDocument.jsp"; $extraparams="&returnUrl=".urlencode($GLOBALS['Accounts_Url'])."/addDocument.php"; break;}
  default:	{
    $op =$_REQUEST['tracking'];
    if (isset($_REQUEST['pin']))
    $pin = "&p=".$_REQUEST['pin']; 
    break;
  }
}

if(isset($_COOKIE['mode'])) {
  $extraparams.="&am=".urlencode($_COOKIE['mode']);
}

dbg("allocating node for storage id = $storageId");
$n = allocate_node($storageId);
if($n === false) { // Failed to allocate gateway!
  error_log("Unable to allocate gateway for operation ".$op);
  die("Internal error: Unable to locate storage - please contact support or your administrator.");
}

$gw = $n->hostname;

if (!isset($pin))$pin='';
if (!isset($idp))$idp='';
$url = "$gw/$page?tracking=$op&accid=$accid&auth=$auth&idp=$idp$pin$extraparams";
$terryUrl = $url;
//$terryUrl = strong_url($url);
	$x=<<<XXX
<html><head><title>Redirecting to MedCommons Repository Gateway Op $op via $url</title>
<meta http-equiv="REFRESH" content="0;url='$terryUrl'"></HEAD>
<body >
<p>
Please wait while we connect to the PHR Record Repository...
</p>
<img border=0 src = "/images/progressbar.gif" alt='thank you for your patience...' />
</body>
</html>
XXX;
	echo $x;

?>
