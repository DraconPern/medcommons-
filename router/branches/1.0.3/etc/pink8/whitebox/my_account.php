<?php

require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/displayhomepage.inc");
require_once("../whitebox/displaymyaccountpage.inc");

function ahref($l,$s)
{return "<a href=$s>$l</a>";}




readconfig();//hack	
session_start();

//we got here just because the user clicked on a hyperlink to get here
//check that we are logged in

$userid = $_SESSION['user'];
$gateway = 'mcpurple01'; //temp
if ($userid=="")
{//not logged in, send him back with penalties
display_home_page(errortext("You must be logged in to view your account"),'',$userid,
			errortext("**please login**"));
}
else {

display_my_account_page($userid,$gateway);

}
?>
