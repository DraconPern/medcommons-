<?php

require_once 'healthbook.inc.php';
require_once 'mc_oauth_client.php';

// start get standard data
// appinclude.inc.php has already run and connected us to mysl and has also read the fbapps table to find this app
// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit(); 
// *** end of basic start
$appname = $app->healthbook_application_name;
// IF WE'VE POKED THE DATABASE TO DISABLE THE APPLICATION
$appstatus = $app->appstatus;
if ($appstatus!='')
{
	logHBEvent($user,'visitdisabled',"visited but application $appname disabled");
	$markup = <<<SSS
	<fb:fbml version='1.1'><fb:title>$appname Application Temporarily Disabled</fb:title>
	<fb:explanation>
	<fb:message>$appstatus
	</fb:message>
	<p>We plan to restore operation shortly. Thank you for your patience.</p>
	</fb:explanation>
</fb:fbml>	
	
SSS;
	echo $markup;
	exit;
	
}

// IF NOT LOGGED ON THEN PUT UP A SPLASH PAGE
if (!$user)
{
	echo splash($facebook,$user);
	exit;
}
	// GET THIS FACEBOOK USER'S DETAILS

	
	
// IF LOGGED IN THEN RECORD THE FACT WE ARE VISITED

$u = HealthBookUser::load($user);
if ($u==false )
{
	// set up a family of our own, and patch in jane

	list($fn,$ln,$ps,$sx) = facebook_user_info($facebook,$user); // first time up, get facebook info via remote api call 
	
	$now=time();

	// GET THE MED COMMONS FAMILY AND CLONE IT
	$result = mysql_query("Select * from users where fbid='1107682260' ")
	or die ("Cant Select Family Clone $sql ".mysql_error());
	$mcrow = mysql_fetch_array($result);
	if (!$mcrow) die ("Cant Find Family Med Commons");

	// BUILD INSERT STATEMENT FOR THE FACEBOOK USER'S FAMILY RECORD
	$sql = "Insert into users set ";
	$sql.= "fbid ='$user', ";
	$sql.= "firstname ='$fn', ";
	$sql.= "lastname ='$ln', ";
	$sql.= "accountlabel ='The $ln Family', ";
	$sql.= "accountpic = '$app->healthbook_application_image', ";
	$sql.= "app = '$app->key', ";
	$sql.= "photoUrl ='$ps', ";
	$sql.= "sex ='$sx', ";
//	$sql.= "mcid ='".$mcrow['mcid']."', ";
	$sql.= "familyfbid ='$user', ";
	$sql.= "sponsorfbid ='".$mcrow['sponsorfbid']."', ";
	$sql.= "targetmcid ='".$mcrow['targetmcid']."', ";

	$sql.= "storage_account_claimed ='".$mcrow['storage_account_claimed']."' ";
	mysql_query($sql) or die ("Cant Insert Family Clone $sql ".mysql_error());

	// CLONE JANE INTO THIS FAMILY
	
	cloneJane ($user);
	logHBEvent($user,'newuser',"$user --  The $ln Family was setup by $fn $ln");
}
//else

//	logHBEvent($user,'visit',"$user  --  $fn $ln visited $appname"); //this has no real valuye

require_once "familypage.inc.php";
$markup = family_page($facebook,$user,$user);

echo $markup;


?>
