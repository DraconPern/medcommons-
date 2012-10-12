<?php
require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/mailsubs.inc");
require_once("../whitebox/displaychangepw.inc");
require_once("../whitebox/displayhomepage.inc");
session_start();
readconfig();
$userid = $_SESSION['user'];

$oldpass = $_REQUEST['oldpass'];
$newpass1 = $_REQUEST['newpass1'];
$newpass2 = $_REQUEST['newpass2'];

$error = "";
if ($newpass1!=$newpass2) $error = "***new passwords must match***";
else { $success = validuseridpin($userid, $oldpass);
	   if ($success == false) $error = "***password is invalid***";
	   else  updateuseridpassword($userid,$newpass1);}
	
if ($error == "") echo display_home_page("your password has been changed","","","","");
else echo display_change_pw(errortext($error));
?>
