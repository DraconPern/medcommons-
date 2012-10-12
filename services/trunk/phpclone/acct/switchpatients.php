<?php
//$cookie = $_COOKIE['mc_cursor'];
//// parse existing cookie
//list($group,$groupid,$x1,$x2)=explode(',',$cookie);
$group = $_GET['g'];
$groupid = $_GET['gid'];
// merge in new values
$patient = $_GET['n'];
$patientID = $_GET['id'];
$value = //urlencode(
"groupName=$group,groupID=$groupid,patientName=$patient,patientID=$patientID"
//)
;
// send a cookie that expires in 24 hours
setcookie("mc_cursor",$value, time()+3600*24,'/');
// redirect back to the healthurl
header("Location: $patientID");
die("Should never get here in switchpatients.php");
?>