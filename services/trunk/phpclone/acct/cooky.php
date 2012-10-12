<?php
$value = "groupName=SampleGroup";

// send a cookie that expires in 24 hours
setcookie("mc_cursor",$value, time()+3600*24,'/');
?>