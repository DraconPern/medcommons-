<?php
$mode =  $_POST['mode'];
list($group,$groupid)=explode(',',$mode);
$value = "groupName=$group,groupID=$groupid,patientName=SamplePatient,patientID=0";
// send a cookie that expires in 24 hours
setcookie("mc_cursor",$value, time()+3600*24,'/');
// redirect back to inbox which should now have the proper name
header("Location: /acct/index.php");
die("How did we get here from switchgroups?");
?>