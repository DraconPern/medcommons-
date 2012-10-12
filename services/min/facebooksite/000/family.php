<?php

require 'healthbook.inc.php';
require_once "familypage.inc.php";

// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit(); 
// *** end of basic start

// IF FAMILY IS SET, THEN SWITCH TO NEW FAMILY BEFORE DISPLAYING

if (isset($_REQUEST['family']))
{
	$family = $_REQUEST['family'];
	$ismine = ($family==$user);
	if (!$ismine) {
		$q = "Select * from teams where teamfbid='$family' and userfbid='$user'";

		$result = mysql_query($q) or die ("Cant $q ".mysql_error());
		$r = mysql_fetch_object($result);
		if ($r) $ismine = true;
	}
	if ($ismine)
	{
		$q = "UPDATE users set familyfbid = '$family'  where fbid='$user'";
		mysql_query($q) or die ("Cant $q ".mysql_error($q));
		// SHOULD LOG THIS??
	}
	mysql_free_result($result);

	logHBEvent($user,'family',"$user  --  switching to family $family");
}
else
// get rid of this

if (isset($_REQUEST['remove']))
{
	$family = $_REQUEST['remove'];
	$ismine = ($family==$user);

	$q = "Delete from teams where teamfbid='$family' and userfbid='$user'";

	$result = mysql_query($q) or die ("Cant $q ".mysql_error());
	

	$q = "UPDATE users set familyfbid = '$user', targetmcid=0  where fbid='$user'";
	mysql_query($q) or die ("Cant $q ".mysql_error($q));


	logHBEvent($user,'family',"$user  --  removed connection to $family");
}
else
// get rid of this

if (isset($_REQUEST['remelder']))
{
	$patientmcid = $_REQUEST['remelder'];
	$familyid = $_REQUEST['familyid'];
	$ismine = ($familyid==$user);

	$q = "Delete from patients where familyfbid='$familyid' and mcid='$patientmcid'";

	$result = mysql_query($q) or die ("Cant $q ".mysql_error());
	

	$q = "UPDATE users set familyfbid = '$user', targetmcid = 0 where fbid='$user'"; // dont zero out mcid when I remove an elder
	mysql_query($q) or die ("Cant $q ".mysql_error($q));


	logHBEvent($user,'family',"$user  --  removed elder patient mcid $patientmcid family $familyid");
}
// jumping off to outer space
echo family_page($facebook,$user,$user);
?>