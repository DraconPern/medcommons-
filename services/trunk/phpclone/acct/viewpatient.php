<?php
$cookie = $_COOKIE['mc_cursor'];
//// parse existing cookie
list($group,$groupid,$patient,$hurl)=explode(',',$cookie);
list($key,$healthurl) = explode('=',$hurl);
header("Location: $healthurl");
echo "going to $healthurl";
?>