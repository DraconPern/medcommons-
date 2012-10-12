<?php

require 'healthbook.inc.php';

function settargets($user,$targetmcid)
{

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
//echo "user $user familyfbid $familyfbid mcid $mcid <br/>";
if ($mcid == '9999999999999999')
{
	$q = "update users set familyfbid ='$familyfbid' where fbid='$user'";// and mcid = '$mcid'";
	$result = mysql_query($q) or die("cant $q ".mysql_error());
	//$rows= mysql_affected_rows();
	require_once "familypage.inc.php";
	echo family_page($facebook,$user,$familyfbid);
	exit;
}
$q = "SELECT * from patients where mcid ='$mcid' and familyfbid = '$familyfbid' ";
$result = mysql_query($q) or die("cant $q ".mysql_error());
$r = mysql_fetch_object($result);
$q = "update users set targetmcid='$r->mcid' where fbid='$user'";// and mcid = '$mcid'";
$result = mysql_query($q) or die("cant   $q ".mysql_error());
// jumping off to outer space
require_once "collaboratepage.inc.php";
echo collaboration_page($facebook,$user);

?>
