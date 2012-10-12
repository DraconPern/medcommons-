<?php
$mode =  $_POST['mode'];
$cookie = $_COOKIE['mc_cursor'];
// parse existing cookie
list($group,$groupid,$patient,$patientID)=explode(',',$cookie);
// overlay new group values and re-write
list($group,$groupid)=explode(',',$mode);
$value = "groupName=$group,groupID=$groupid,patientName=$patient,patientID=$patientID";
// send a cookie that expires in 24 hours
setcookie("mc_cursor",$value, time()+3600*24,'/');
// redirect back to inbox which should now have the proper name
header("Location: /acct/index.php");
die("How did we get here from switchgroups?");
?>