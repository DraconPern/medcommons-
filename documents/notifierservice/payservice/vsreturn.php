<?php

require "../../dbparamsmcextio.inc.php";
function logon_redirect($returl, $err)
{
	$url = "$returl?err=$err";
	$x=<<<XXX
<html><head><title>Please Logon to MedCommons</title>
<meta http-equiv="REFRESH" content="0;url=$url"></HEAD>
</html>
XXX;
	echo $x;
	exit;
}

$custid=$_POST['CUSTID'];
$user1 = $_POST['USER1'];
$returl = $_POST['USER2'];
$pnref= $_POST['PNREF'];
$result = $_POST['RESULT'];
$avsdata = $_POST ['AVSDATA'];
$respmsg = $_POST ['RESPMSG'];
$authcode = $_POST ['AUTHCODE'];

if ($result == 0) logon_redirect ($returl,"Successfully Established Your Account - Please Log On");
else logon_redirect ($returl,"Error processing your credit card  - $respmsg")
?>