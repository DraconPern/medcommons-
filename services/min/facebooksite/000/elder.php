<?php

require 'healthbook.inc.php';

function settargets($user,$targetmcid)
{
	$app = $GLOBALS['healthbook_application_name'];

	$alink = $GLOBALS['facebook_application_url'];
	//	$q = "delete from users where fbid='$user'";// and mcid = '$mcid'";
	$q = "update users set targetmcid='$targetmcid' where fbid='$user'";// and mcid = '$mcid'";
	$result = mysql_query($q) or die("cant   $q ".mysql_error());
	$rows= mysql_affected_rows();
	return;
}


// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit(); 
// *** end of basic start

$page = $GLOBALS['facebook_application_url'];

// get incoming xmcid and parse
$xmcid = $_REQUEST['xmcid'];
if ($xmcid==-3)
{	//no-op
	echo "<fb:redirect url='".$page."index.php' />";
	exit;
} else
if ($xmcid==-1)
{
	echo "<fb:redirect url='".$page."settings.php' />";
	exit;
} else if ($xmcid==-2)
{
	// WANTS TO CLONE JANE, SO JUST PATCH IT IN HERE
	cloneJane($user); // put her back into our family
	// fall in to normal code with:
	$xmcid =  '1013062431111407+1107682260';
}
$mcid = substr($xmcid,0,16); // followed by a space before family fbid is there
$familyfbid = substr($xmcid,17);
if ($mcid == 9999999999999999)
{
	require_once "familypage.inc.php";
	echo family_page($familyfbid,$facebook);
	exit;
}
$q = "SELECT * from patients where mcid ='$mcid' and familyfbid = '$familyfbid' ";
$result = mysql_query($q) or die("cant $q ".mysql_error());

$r = mysql_fetch_object($result);
settargets ($user,$r->mcid);


// jumping off to outer space
require_once "collaboratepage.inc.php";
echo collaboration_page($facebook,$user);

?>
