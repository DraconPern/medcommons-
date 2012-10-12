<?php
// careteam stuff
require_once 'healthbook.inc.php';
function handle_post($facebook,$family,$user)
{
	// is the sender of this invitation still around in our database
	// is this user already known to medcommons facebook?
	$q = "select * from  users  where  fbid='$family' ";
	$result = mysql_query($q) or die("Cant $q ".mysql_error());
	$r0 = mysql_fetch_object($result);
	if ($r0) {
		// is this user already known to medcommons facebook?
		$q = "select * from  users  where  fbid='$user' ";
		$result = mysql_query($q) or die("Cant $q ".mysql_error());
		$r1 = mysql_fetch_object($result);
		if ($r1==false) {
			// SENDER IS KNOWN, WE ARE UNKNOWN
				
			list($fn,$ln,$ps,$sx) = facebook_user_info($facebook,$user);

			$q = "replace into users set  mcid='$r0->mcid', sponsorfbid='$r0->sponsorfbid', targetmcid='$r0->targetmcid',familyfbid='$family' ,fbid='$user',
			firstname='$fn', lastname='$ln', sex='$sx',	photoUrl='$ps' , accountlabel='$r0->accountlabel',accountpic='$r0->accountpic',
			oauth_token='$r0->oauth_token',oauth_secret='$r0->oauth_secret',applianceurl='$r0->applianceurl',gw='$r0->gw' ";
			mysql_query($q) or die("Cant $q ".mysql_error());

		}
		
		else
		
		{ 
			// SENDER IS KNOWN, WE ARE ALREADY KNOWN
			
			
		}
		mysql_free_result($result);

		//ALREADY ON TEAM?
		$q = "select * from  teams  where teamfbid='$family' and userfbid='$user' ";
		$result = mysql_query($q) or die("Cant $q ".mysql_error());
		$r2 = mysql_fetch_object($result);
		if (!$r2){
			// HOOK INTO TEAMS Table
			$now = time();
			$q = "Replace into teams set teamfbid = '$family', userfbid='$user',accepttime='$now' ";
			mysql_query($q) or die("Cant $q ".mysql_error());

			$dash = dashboard($user);
			$markup = <<<XXX
			<fb:fbml version='1.1'>
			$dash
			<fb:success>
			<fb:message>You have joined the Family Careteam of <fb:name uid=$family /></fb:message>
 	<p>You can now help care for your family  </p>
       <fb:editor action="index.php" labelwidth="100">
     <fb:editor-buttonset>
          <fb:editor-button value="home"/>
     </fb:editor-buttonset>
</fb:editor>
  </fb:success>
</fb:fbml>
XXX;

			logHBEvent($user,'accepted',"$user accepted invite from $family");
		}
		else
		{
			// ALREADY ON TEAM
			$dash = dashboard($user);
			$markup = <<<XXX
			<fb:fbml version='1.1'>
			$dash
			<fb:success>
			<fb:message>You were already on the Family Careteam of <fb:name uid=$family /></fb:message>
       <fb:editor action="index.php" labelwidth="100">
     <fb:editor-buttonset>
          <fb:editor-button value="home"/>
     </fb:editor-buttonset>
</fb:editor>
  </fb:success>
</fb:fbml>
XXX;
		}
	}
	else
	{
		// this user has vaporized
		// ALREADY ON TEAM

		$markup = <<<XXX
		<fb:fbml version='1.1'>

		<fb:error>
		<fb:message>This invitation is no longer valid</fb:message>
       <fb:editor action="index.php" labelwidth="100">
       <p>We are sorry to inconvenience you, but this user has apparently removed the application after sending you an invitation</p>
     <fb:editor-buttonset>
          <fb:editor-button value="home"/>
     </fb:editor-buttonset>
</fb:editor>
  </fb:error>
</fb:fbml>
XXX;
	}
	echo $markup;
}
// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit(); 
// *** end of basic start
if (isset($_REQUEST['family'])){ handle_post($facebook,$_REQUEST['family'],$user); exit; }

$page = $GLOBALS['facebook_application_url'];
$appname = $GLOBALS['healthbook_application_name'];

$arFriends = "";
$q = "SELECT * from teams where invitetime!=0 and accepttime!=0 and teamfbid='$user' "; //bill = dont reinvite people with outstanding invites
$result = mysql_query($q) or die("cant  $q ".mysql_error());
while($u=mysql_fetch_object($result))
{
	if ( $arFriends != "" )
	$arFriends .= ",";
	$arFriends .= $u->userfbid;
}
mysql_free_result($result);

// Construct a next url for referrals $sNextUrl = urlencode("&refuid=".$user);
$sNextUrl =$GLOBALS['facebook_application_url']."invite.php?family=$user"; // this should be all that is needed for the hookup

//  Build your invite text
$invfbml = <<<FBML
Please help care for our family by joining our MedCommons Family CareTeam.
By accepting this invitation you will be able to share medical records and help other family members.  Thank you

<fb:req-choice url="$sNextUrl" label="Please Join My Family CareTeam" />
FBML;
$invfbml = htmlentities($invfbml);



$dash = dashboard($user);
$markup = <<<XXX
<fb:fbml version='1.1'><fb:title>Invite Friends to Join Family CareTeam</fb:title>
$dash
<fb:explanation>
<fb:message>Family CareTeam Invitations</fb:message>
<fb:request-form
action="index.php?ref=invitect"
method="POST"
invite="true"
type="Family CareTeam"
content="$invfbml">
<fb:multi-friend-selector max="20" actiontext="Invite friends to join your Family CareTeam"
showborder="true" rows="3" exclude_ids="$arFriends">
</fb:request-form>
<p>$appname will never add anyone to your Family CareTeam without mutual consent.</p>
  </fb:explanation>
</fb:fbml>
XXX;

echo $markup;
logHBEvent($user,'invite',"$user invited friends to $sNextUrl");




?>
