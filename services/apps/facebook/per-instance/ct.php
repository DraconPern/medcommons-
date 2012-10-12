<?php
// careteam stuff - post entry points
require_once 'healthbook.inc.php';

// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit();
// *** end of basic start

$u = mustload($facebook,$user);


$page = $GLOBALS['facebook_application_url'];
$appname = $GLOBALS['healthbook_application_name'];
$dash = dashboard($user);
if (isset($_REQUEST['o'])) $op =  $_REQUEST['o']; else $op='';

if ($op=='r') // REMOVE
{
	$fbid  = $_REQUEST['id'];
	$giverfbid = $_REQUEST['gid'];
	$q = "Select * from users where fbid = '$fbid' ";
	$result = mysql_query($q) or die("Cant $q ".mysql_error());
	$r = mysql_fetch_object($result);
	if ($r) {
		//$q = "DELETE from careteams where mcid = '$r->mcid' and giverfbid='$giverfbid'";
		//mysql_query($q) or die("Cant $q ".mysql_error());
		/*
		set the caregiver so he is no longer viewing the target's records
		*/


		$q = "replace into users set  mcid='0', sponsorfbid='0', targetmcid='0',familyfbid='$user' ,fbid='$giverfbid',
		oauth_token='',oauth_secret='',applianceurl='',gw=''  ";
		mysql_query($q) or die("Cant $q ".mysql_error());
		logHBEvent($user,'careteamremove',"removed giver $giverfbid from $fbid care team");
		$markup =  "<fb:fbml version='1.1'>redirecting via facebook to $page". "<fb:redirect url='$page' /></fb:fbml>";
	}
}
/*
else if ($op=='b') // REMOVE this user from all care teams he is on
{

	$q = "Select * from users where fbid = '$user' ";
	$result = mysql_query($q) or die("Cant $q ".mysql_error());
	$r = mysql_fetch_object($result);
	if ($r) {
		$q = "replace into users set  mcid='0', sponsorfbid='0', targetmcid='0',familyfbid='$user' ,fbid='$user',
		oauth_token='',oauth_secret='',applianceurl='',gw=''  "; 
		mysql_query($q) or die("Cant $q ".mysql_error());
		logHBEvent($user,'careteamremove',"removed user $user from all care teams");
		$markup =  "<fb:fbml version='1.1'>redirecting via facebook to $page". "<fb:redirect url='$page' /></fb:fbml>";
	}
}
*/
else $markup = "Unknown op code $op";

echo $markup;
?>
