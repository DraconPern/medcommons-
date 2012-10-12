<?php

require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/displayhomepage.inc");
require_once("../whitebox/displaymyaccountpage.inc");

function ahref($l,$s)
{return "<a href=$s>$l</a>";}
//there's two ways to get here


readconfig();//hack
session_start();
sqltraceon();
$login = cleanreq('login');
$userid = cleanreq('userid');
$password = cleanreq('password');
$tracking = cleanreq ('tracking'); // passed thru

	
//check username and password
if ($login == 'login') {
	//just make sure we are handling the login
	// if already logged in , give an error message and suggest she og out

	if ($_SESSION['user']!="")
	{display_home_page(errortext("You are already logged in as ".$_SESSION['user']),"",'',"");
	exit;
	}

	  if (false == validuseridpin($userid,$password)) {
		//bad password, put the home page up again, this calls wbheader itself
		display_home_page(errortext("Please check your userid and password"),
		$userid,'',
		errortext("*** Your userid or password is incorrect ***".
		ahref("Obtain New Password",
		"../whitebox/newpw.php")));
		sqltracedump();
		exit;

	}
	else
	{
		// ok we can do our thing because the username and password check out

		$_SESSION['user']=$userid;

//		display_home_page("(You are logged in as $userid)",$userid,$tracking,'');
	    display_my_account_page($userid,'mcpurple01');
	}


}
else echo "Shouldnt be here in loginhandler";

?>
