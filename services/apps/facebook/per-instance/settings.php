<?php

require_once 'healthbook.inc.php';
require_once 'mc_oauth_client.php';
function insertSponsoredUser($user,$firstName,$lastName, $sex,$city,$state,$country,$photoUrl,$mcid,$product,$ghUrl,$hvUrl) {

	global $oauth_consumer_key;
	$appname = $GLOBALS['healthbook_application_name'];
	// this is domain where we are going to allocate facebook accounts
	$applianceUrl = new_account_factory_appliance();
	// set the sponsorAccountId to the mcid of the caller
	$remoteurl =$applianceUrl."/router/NewPatient.action?".
	"familyName=$lastName&givenName=".urlencode($firstName).
	"&sex=".urlencode($sex).
	"&city=".urlencode($city).
	"&state=".urlencode($state).
	"&country=".urlencode($country).
	"&auth=".urlencode($oauth_consumer_key).
	"&oauth_consumer_key=".urlencode($oauth_consumer_key).
	"&photoUrl=".urlencode($photoUrl).
	"&sponsorAccountId=".urlencode($mcid).
	"&activationProductCode=".urlencode($product);

	error_log("creating new patient with url ".$remoteurl);

	// if a support group is defined for this app, set it as the sponor so that
	// support team has access to this new patient's account
	if(isset($GLOBALS['new_account_support_group_mcid']))
	$remoteurl .= "&sponsorAccountId=".urlencode($GLOBALS['new_account_support_group_mcid']);

	//  new web service call to make the account
	//echo "creating $firstName $lastName from $city<br/>";
	$result = file_get_contents($remoteurl);

	//parse the return looking for mcid
	$json = new Services_JSON();
	$patient_info = $json->decode($result);

	if(!$patient_info) {
		error_log("Failed to create new storage account - unable to decode patient info: $result");
		return false;
	}

	if($patient_info->status !== "ok") {
		error_log("Failed to create new subordinate storage account - NewAccount service returned failure status with error: ".$patient_info->error);
		return false;
	}

	$mcid = $patient_info->patientMedCommonsId;
	$auth = $patient_info->auth;
	$secret = $patient_info->secret;
	$appname = $GLOBALS['healthbook_application_name'];
	$firstName = mysql_real_escape_string($firstName);
	$lastName = mysql_real_escape_string($lastName);
	//	dbg("Received auth $auth and secret $secret for new patient $mcid");
	$now = time();
	logHBEvent($user ,'addsub',"$now - added subordinate usermcid $mcid $firstName $lastName");
	mysql_query("REPLACE INTO patients (mcid,applianceurl,sponsorfbid,familyfbid, groupid,oauth_token,oauth_secret,firstname,lastname,sex,photoURL,ghUrl,hvUrl)
	VALUES ('$mcid','$applianceUrl','$user','$user',NULL,'$auth','$secret','$firstName','$lastName','$sex','$photoUrl'
	,'$ghUrl','$hvUrl')") or die("error inserting into patients: ".mysql_error());
	//	$q = "REPLACE INTO careteams set mcid = '$mcid', giverfbid='$user',giverrole='4' ";
	//	mysql_query($q) or die ("Cant $q");
	$q = "REPLACE INTO carewalls set wallmcid = '$mcid',  wallfbid='$user',authorfbid='$user',msg='I created this account for $firstName $lastName',time=$now ";
	mysql_query($q) or die ("Cant $q");

	return $mcid;
}
function upgradeUser($fbuid,$firstName,$lastName, $sex,$city,$state,$country,$photoUrl,$accountlabel,$accountpic,$key,$product) {

	global $oauth_consumer_key;

	$appname = $GLOBALS['healthbook_application_name'];
	$key=0; $product=0;




	// this is domain where we are going to allocate facebook accounts
	$applianceUrl = new_account_factory_appliance();
	$remoteurl =$applianceUrl."/router/NewPatient.action?".
	"familyName=$lastName&givenName=".urlencode($firstName).
	"&sex=".urlencode($sex).
	"&city=".urlencode($city).
	"&state=".urlencode($state).
	"&country=".urlencode($country).
	"&auth=".urlencode($oauth_consumer_key).
	"&oauth_consumer_key=".urlencode($oauth_consumer_key).
	"&photoUrl=".urlencode($photoUrl);
	"&activationKey=".urlencode($key).
	"&activationProductCode=".urlencode($product);

	error_log("creating new patient with url ".$remoteurl);

	// if a support group is defined for this app, set it as the sponor so that
	// support team has access to this new patient's account
	if(isset($GLOBALS['new_account_support_group_mcid']))
	$remoteurl .= "&sponsorAccountId=".urlencode($GLOBALS['new_account_support_group_mcid']);

	//  new web service call to make the account
	//echo "creating $firstName $lastName from $city<br/>";
	$result = file_get_contents($remoteurl);

	//parse the return looking for mcid
	$json = new Services_JSON();
	$patient_info = $json->decode($result);

	if(!$patient_info) {
		error_log("Failed to create new storage account - unable to decode patient info: $result");

		logHBEvent($fbuid ,'bad added',"Failed to create new storage account - unable to decode patient info: $result");
		return false;
	}

	if($patient_info->status !== "ok") {
		error_log("Failed to create new storage account - NewAccount service returned failure status with error: ".$patient_info->error);
		logHBEvent($fbuid ,'bad added',"Failed to create new storage account - NewAccount service returned failure status with error: ".$patient_info->error);
		return false;
	}

	$mcid = $patient_info->patientMedCommonsId;
	$auth = $patient_info->auth;
	$secret = $patient_info->secret;
	$appname = $GLOBALS['healthbook_application_name'];
	$firstName = mysql_real_escape_string($firstName);
	$lastName = mysql_real_escape_string($lastName);
	$now = time();
	//	dbg("Received auth $auth and secret $secret for new patient $mcid");
	logHBEvent($fbuid ,'added',"Self added user fbid $fbuid mcid $mcid $firstName $lastName");

	//  now must carefully add this
	mysql_query("REPLACE INTO users (fbid,mcid,applianceurl,sponsorfbid,familyfbid,targetmcid,groupid,oauth_token,oauth_secret,firstname,lastname,sex,photoURL,
	accountlabel,accountpic)
	VALUES ('$fbuid','$mcid','$applianceUrl','$fbuid','$fbuid','$mcid',NULL,'$auth','$secret','$firstName','$lastName','$sex','$photoUrl','$accountlabel','$accountpic')") or die("error inserting into users: ".mysql_error());


	return $mcid;
}


function create_subordinate_account($facebook, $u, $user,$supmcid)
{
	
	if ($u->mcid==0)
	{
		// IF THERE IS NO FAMILY ADMINISTRATIVE ACCOUNT THEN PROMPT FOR FIELDS THEN MAKE SURE APP IS ADDED
		$key = 0;
		$product = 0; // just bypass amazon for now
		$mcid = upgradeUser($user,$u->firstname,$u->lastname, '','','','',$u->photoUrl,$u->accountlabel,$u->accountpic,$key,$product) ;

		if($mcid === false) {
		$dash = dashboard($user);
			$out="<fb:fbml version='1.1'>
			$dash
			<fb:error message='Error Occurred'>
			A system error occurred while creating your long term storage account and
			connecting it to your Facebook account.
			</fb:error>
			</fb:fbml>";
			echo $out;

			exit;
		}
		$uber = $GLOBALS['uber'];
		$hurl = "$uber/$mcid";
		opsMailBody(  "Facebook account $user $u->firstname $u->lastname created medcommons account $mcid",
	"<br>A MedCommons Account was created for facebook user $u->firstname $u->lastname</br>".
	"<br>You can access the user's facebook profile at http://www.facebook.com/profile.php?id=$user</br>".
	"<br>You can attempt to access the user's healthurl at $hurl</br>");
		$notice = "<p>We created a basic administrative account for you";
		$supmcid = $mcid;
		

	}
	// subordinate account, no fb_user, does not come from amazon, must figure how to get superiors keys
	$key = '**notyet**'; //$_POST['ActivationKey'];
	$product = '**notyet**'; //$_POST['ProductCode'];
	$fn =  (isset($_REQUEST['fname']))?$_REQUEST['fname']:'--please set first name--';
	$ln = (isset($_REQUEST['lname']))?$_REQUEST['lname']:'--please set last name--';
	$sx = (isset($_REQUEST['sex']))?$_REQUEST['sex']:'--please set sex--';
	$pic = (isset($_REQUEST['pic']))?$_REQUEST['pic']:'--please set pic url--';
	$hvUrl = (isset($_REQUEST['hvUrl']))?$_REQUEST['hvUrl']:'';
	$ghUrl = (isset($_REQUEST['ghUrl']))?$_REQUEST['ghUrl']:'';
	$mcid = insertSponsoredUser($user,$fn,$ln,$sx,'','','',$pic,$supmcid,$product,$ghUrl,$hvUrl);

	if($mcid === false) {
		$dash = dashboard($user);
		$out="<fb:fbml version='1.1'>
		$dash
		<fb:error message='Error Occurred'>
		A system error occurred while creating a subordinate account for mcid $supmcid
		</fb:error>
		</fb:fbml>";
		echo $out;
		logHBEvent($user,'sub',"could not make subordinate account for mcid $supmcid");
		exit;
	}

	$hurl = "$uber/$mcid";
	opsMailBody(  "$appname says facebook account $user $fn $ln created medcommons account $mcid",
	"<br>A subordinate MedCommons Account was created for facebook user $fn $ln</br>".
	"<br>You can access the user's facebook profile at http://www.facebook.com/profile.php?id=$user</br>".
	"<br>You can attempt to access the user's healthurl at $hurl</br>");

	//logHBEvent($user,'sub',"back from sub acct creation healthurl is $hurl");
	$redir = $GLOBALS['facebook_application_url']."index.php";
	echo "<fb:fbml version='1.1'><fb:redirect url='$redir' /></fb:fbml>";
}

function settings_page($facebook, $user,$u)
{

	function  elders($facebook,$familyfbid,$u)
	{

		function addelder_form($facebook,$user,$u)
		{

      $newAcctAppliance=rtrim($GLOBALS['new_account_appliance'],'/');
      $appname = $GLOBALS['healthbook_application_name'];

			$msg = <<<XXX

<script>
  var connectBox = document.getElementById('connectExisting'); 
  var connectNewBox = document.getElementById('connectNew'); 
  function connect_existing() {
    connectBox.setStyle('display','block');
    return false;
  }
  function connect_new() {
    connectNewBox.setStyle('display','block');
    return false;
  }
</script>

<p>You can <a href='#' onclick='connect_new(); return false;'>add</a> a new Elder to your family or, if they have an existing HealthURL, <a href='#' onclick='connect_existing(); return false;'>connect</a> their existing account.</p>
<div id='connectNew' style='display: none;'>
  <fb:editor action="settings.php?newelder" labelwidth="100">  
    <fb:editor-text label="First Name" name="fname" value=""/>  
    <fb:editor-text label="Last Name" name="lname" value=""/>  

    <fb:editor-text label="Web Photo URL" name="pic" value=""/>  

    <fb:editor-text label="Google Health URL" name="ghUrl" value=""/>  
    <fb:editor-text label="Msoft Health Vault URL" name="hvUrl" value=""/>  
     
    <fb:editor-text label="Date of Birth" name="dob" value=""/>  
    <fb:editor-custom label="Sex"> 
      <select name="sex">  <option value="F" selected>Female</option>  <option value="M">Male</option></select> 
    </fb:editor-custom> 
    <fb:editor-buttonset>
      <fb:editor-button value="Add New Elder" onclick="this.disabled = true;" /> 
    </fb:editor-buttonset> 
  </fb:editor> 
</div>

<div id='connectExisting' style='display: none;'>
		<p>You can connect to an existing MedCommons Account by entiring its HealthURL here:</p>
		<fb:editor action="authorize_join.php" labelwidth="100">
		<fb:editor-text label="HealthURL" name="hurl" value="" style='font-size: 10px;'/>
		<fb:editor-buttonset>
		<fb:editor-button value="connect"/>
		</fb:editor-buttonset>
		</fb:editor>
    <p><b>Note:</b> You will be redirected to an authorization page to grant access.  You may
       be prompted to sign in, in which case you must log in with an account that has been 
       granted consent to access the account.</p>
</div>

XXX;
			return $msg;
		}

		$familyname = $GLOBALS['familyname'];//familyname($familyfbid);

		$outstr =<<<XXX
		<fb:explanation><fb:message>Elders in $u->accountlabel Care Team</fb:message>
      <div class=caregivee >
	<table><tr>
XXX;

		$counter = 0;
		$q = "select * from  patients  m where m.familyfbid = '$familyfbid' "; //ok

		$result = mysql_query($q) or die("cant  $q ".mysql_error());
		while($r=mysql_fetch_object($result))
		{

			// IF THIS ELDER IS THE ONE WE ARE VIEWING THEN ADD A PICTURE AND HEALTHURL LINK
			$remlink ="family.php?remelder=$r->mcid&familyid=$r->familyfbid";//"<a class='tinylink' href='#' clicktoshowdialog='remove_{$r->mcid}_dlg' title='Remove yourself from this Care Team'>remove yourself</a>";
      list($hurl,$xtra) = patient_hurl($r);
      if(!$r->photoUrl || ($r->photoUrl == ''))
        $r->photoUrl = $GLOBALS['app_url'].'/images/unknown-user.png';

			$outstr .= "
        <td width='70px'><a href='family.php?family={$r->familyfbid}'>
          <div class=pic ><img src='{$r->photoUrl}' alt='missing photo' /></div>
          <div class='txt'>{$r->firstname} {$r->lastname}</a></div>
          <br/>
          $xtra
          <br/>
          <div><a href=$remlink>Disconnect</a></div>
        </td>";
			$counter++;
		}
		mysql_free_result($result);

		if ($counter == 0) $outstr .="<td><h3>The $u->lastname Family Care Team is not currently caring for any family members.</h3></td>";
		$aeform=addelder_form($facebook,$familyfbid,$u);
		$outstr .= <<<XXX
		</tr></table></div>$aeform</fb:explanation>
XXX;

		return $outstr;
	}
	function manage($facebook,$user,$u)
	{
		function  siblings($facebook,$user)
		{

			$appUrl = $GLOBALS['app_url'];

			$outstr ="
	<div class=caregivee ><table><tr>
	"; 

			$counter = 0;
			// GO WIDE, PUT US FIRST
			$q = "select * from  users u where u.fbid = '$user' ";
			$result = mysql_query($q) or die("cant select from  $q ".mysql_error());
			$r2 = mysql_fetch_object($result);

      if(!$r2->photoUrl || ($r2->photoUrl == ''))
        $r2->photoUrl = $GLOBALS['app_url'].'/images/unknown-user.png';

			$outstr .= <<<XXX
			<td width=70px ><a href='family.php?family=$r2->fbid'><div class=pic ><img src=$r2->photoUrl alt='missing photo' /></div>
			<div class='txt' >$r2->firstname $r2->lastname<br/>(manager)</div></a> </td>
XXX;



			$q = "select * from teams t, users f
			where t.teamfbid= '$user' and t.userfbid = f.fbid   "; //was f.familyfbid

			$result = mysql_query($q) or die("cant  $q ".mysql_error());
			while($r1=mysql_fetch_object($result))
			{
				$mod = $counter -  floor($counter/1)*1;
				if ($mod==0 && $counter!=0)$outstr.="<br/>";
				$outstr .= <<<XXX
				<td width=70px ><a href='family.php?family=$r1->fbid'><div class=pic ><img src=$r1->photoUrl alt='missing photo' /></div>
				<div class='txt' >$r1->firstname $r1->lastname</div></a> </td>
XXX;
				$counter++;
			}
			mysql_free_result($result);
			$outstr .=<<<XXX
	</tr></table></div>
XXX;
			return $outstr;
		}
		$appUrl = $GLOBALS['app_url'];
		$siblings = siblings($facebook,$user);
		$outstr =<<<XXX
		<fb:explanation><fb:message>Manage $u->accountlabel Care Team</fb:message>
		$siblings
		<p><img src='${appUrl}images/speech.png'/>&nbsp;You can <a href=invite.php>invite your facebook friends</a> and siblings to help care.</p>
		<p>
		<fb:editor action="settings.php?ctlabel" labelwidth="100">
		<fb:editor-text label="Care Team" name="label" value="$u->accountlabel"/>
   <fb:editor-buttonset>  <fb:editor-button value="Change" onclick="this.disabled = true;" /> 
     </fb:editor-buttonset> 
</fb:editor> 
</p>
<p>or <a href='?connect'>connect</a> an existing Facebook Group or Page</p>
	</fb:explanation>
XXX;
		return $outstr;
}

function  focus($user)
{

	$appUrl = $GLOBALS['app_url'];
	$outstr =<<<XXX
	<fb:explanation><fb:message>My Other Care Teams</fb:message>
	
	<div class=caregivee ><table><tr>

XXX;

	$counter = 0;
	$q = "select * from  teams t, users u where t.userfbid='$user' and u.fbid = t.teamfbid order by accepttime desc";
	$result = mysql_query($q) or die("cant select from  $q ".mysql_error());

	while($r1=mysql_fetch_object($result))
	{
		$outstr .= <<<XXX
		<td width=70px ><a href='family.php?family=$r1->fbid'><div class=pic ><img src=$r1->photoUrl alt='missing photo' /></div>
		<div class='txt' >$r1->lastname Family</a><br/>
		<a href='#'>Disconnect</a></div></td>
XXX;
		$counter++;
	}
	mysql_free_result($result);
	$outstr .=<<<XXX
	</tr></table>
	</div>	
	</fb:explanation>
XXX;
	if ($counter==0) $outstr='';
	return $outstr;
}
function mcinfo($facebook,$user,$u)
{
	$appUrl = $GLOBALS['app_url'];
	if ($u->mcid==0)
	$accountinfo = "<p><img src='${appUrl}images/speech.png'/>&nbsp;You dont have a MedCommons Account. One will be created for you when you create the first elder.</p>";
	else {
		list($hurl,$xtra) = admin_hurl($u);
		$accountinfo = "<p><img src='${appUrl}images/speech.png'/>&nbsp;$xtra </p>
		<p><img src='${appUrl}images/speech.png'/>&nbsp;This account manages the {$u->lastname} Family CareTeam. <a>Use a different existing MedCommons Account</a></p>
		<p><img src='${appUrl}images/speech.png'/>&nbsp;The account status is 'Waiting For Direct Access Verification'</p>
		";
	}
	$outstr =<<<XXX
	<fb:explanation><fb:message><fb:name uid=$user useyou=false possesive=true /> MedCommons Account</fb:message>
	$accountinfo
	</fb:explanation>
XXX;
	return $outstr;
}

$dash = dashboard($user,false,true); // side effects needed, must go first
$f = focus($user);
$g = manage($facebook,$user,$u);
$h = elders($facebook,$user,$u);
$jj = mcinfo ($facebook,$user,$u);
$markup = <<<XXX
<fb:fbml version='1.1'><fb:title>Settings</fb:title>
$dash
$jj
$h
$g
$f
</fb:fbml>
XXX;
return $markup;
}
function facebook_page_info($facebook,$user,$pid)
{
	$qqq="SELECT name,pic_small,pic_square,general_info FROM page   WHERE  page_id IN ($pid)";
	$ret = $facebook->api_client->fql_query($qqq);
	if (!$ret) {
		logHBEvent($user,'nouser',"Couldnt $qqq on $pid");
		$markup = <<<SSS
		<fb:fbml version='1.1'><fb:title>Facebook Error Returned/fb:title>
		<fb:explanation>
		<fb:message>Facebook did not deliver results for group information on <fb:group gid=$user />
	</fb:message>
	<p>We plan to restore operation shortly. Thank you for your patience.</p>
 </fb:explanation>
</fb:fbml>	
SSS;
		echo $markup;
		exit;
	}

	$fn = mysql_real_escape_string($ret[0]['name']);
	$ps = mysql_real_escape_string($ret[0]['pic_small']);
	$sx = mysql_real_escape_string($ret[0]['pic_square']);
	if ($sx=='') $sx=$ps;
	$url = mysql_real_escape_string($ret[0]['general_info']);
	return array($fn,$sx,$url);
}
function pconnect_fin($facebook,$user)
{
	$pid = $_REQUEST['pid']; // will be there
	list($pagename,$pic,$blabber ) = facebook_page_info($facebook,$user,$pid);
	logHBEvent($user,'group',"Connected to page $pagename - $blabber");
	$q = "update users set accountpic='$pic' where fbid='$user' ";
	mysql_query($q) or die("Cant $q ". mysql_error());
	$dash = dashboard($user,false,true); // side effects needed, must go first
	// come here to finally do the work
	logHBEvent($user,'group',"Connected to page $pagename - $blabber");
	$markup = <<<SSS
	<fb:fbml version='1.1'><fb:title>Connected to page $pagename </fb:title>
	$dash
	<fb:explanation>
	<fb:message>Connected to page $pagename and website $blabber
	</fb:message>
	<img title='$blabber' src=$pic />
</fb:explanation>
</fb:fbml>	
	
SSS;
	echo $markup;
	exit;


}
function pconnect_page($facebook, $user,$u)
{
	$dash = dashboard($user,false,true); // side effects needed, must go first
	$appUrl = $GLOBALS['app_url'];
	$markup =<<<XXX
	<fb:fbml version='1.1'><fb:title>Settings</fb:title>
	$dash
	<fb:explanation><fb:message>Connect $u->accountlabel Care Team to Facebook Page</fb:message>
	<p><img src='${appUrl}images/speech.png'/>&nbsp;This group picture and description will be utilized for your dashboard avatar:	</p>
	<fb:editor action="settings.php?pconnectfin" labelwidth="100">
	<fb:editor-text label="Facebook Page ID" name="pid" value=""/>
   <fb:editor-buttonset>  <fb:editor-button value="Connect to Facebook Page" onclick="this.disabled = true;" /> 
     </fb:editor-buttonset> 
</fb:editor> 
</p>
	</fb:explanation>
</fb:fbml>
XXX;
	return $markup;
}


function facebook_group_info($facebook,$gid){
	$qqq="SELECT pic_small,name,description FROM group   WHERE  gid IN ($gid)";
	$ret = $facebook->api_client->fql_query($qqq);
	if (!$ret) {
		logHBEvent($user,'nouser',"Couldnt $qqq on $user");
		$markup = <<<SSS
		<fb:fbml version='1.1'><fb:title>Facebook Error Returned/fb:title>
		<fb:explanation>
		<fb:message>Facebook did not deliver results for group information on <fb:group gid=$user />
	</fb:message>
	<p>We plan to restore operation shortly. Thank you for your patience.</p>
	</fb:explanation>
</fb:fbml>	
SSS;
		echo $markup;
		exit;
	}

	$name = mysql_real_escape_string($ret[0]['name']);
	$ps = mysql_real_escape_string($ret[0]['pic_small']);
	$sx = mysql_real_escape_string($ret[0]['description']);
	$url = "http://www.facebook.com/group.php?gid=$gid";
	return array($name,$ps,$sx,$url);
}
function gconnect_fin($facebook,$user)
{
	$gid = $_REQUEST['gid']; // will be there
	list($groupname,$pic,$description,$link ) = facebook_group_info($facebook,$gid);
	$q = "update users set accountpic='$pic' where fbid='$user' ";
	mysql_query($q) or die("Cant $q ". mysql_error());
	logHBEvent($user,'group',"Connected to group $groupname - $description");
	$dash = dashboard($user,false,true); // side effects needed, must go first
	$markup = <<<SSS
	<fb:fbml version='1.1'><fb:title>Connected to group $groupname </fb:title>
	$dash
	<fb:explanation>
	<fb:message>Connected to group $groupname
	</fb:message>
	<p> $description</p>
	<p>link <a href=$link>$link</a></p>
</fb:explanation>
</fb:fbml>	
	
SSS;
	echo $markup;
	exit;


}
function gconnect_page($facebook, $user,$u)
{
	$appUrl = $GLOBALS['app_url'];
	$gid = $_REQUEST['gid']; // will be there
	list($groupname,$pic,$description,$link ) = facebook_group_info($facebook,$gid);
	$dash = dashboard($user,false,true); // side effects needed, must go first
	$markup =<<<XXX
	<fb:fbml version='1.1'><fb:title>Settings</fb:title>
	$dash
	<fb:explanation><fb:message>Connect $u->accountlabel Care Team to Facebook Group</fb:message>
	<fb:if-is-group-member gid="$gid" >

	<p><img src='${appUrl}images/speech.png'/>&nbsp;This group picture and description will be utilized for your dashboard avatar:	</p>
	<p><a href='$link' title='$description'><img src=$pic /><br/>$groupname</a>	</p>

	<fb:editor action="settings.php?gconnectfin" labelwidth="100">
	<input type=hidden name=gid value ='$gid' />
	<fb:editor-buttonset>  <fb:editor-button value="Confirm Connect to Facebook Group" onclick="this.disabled = true;" />
	</fb:editor-buttonset>
	</fb:editor>
	</p>

  <fb:else><p>Sorry you are not a member of this group</fb:else>
</fb:if-is-group-member>
	</fb:explanation>
</fb:fbml>
XXX;
	return $markup;
}
function connect_page($facebook, $user,$u)
{
	$appUrl = $GLOBALS['app_url'];
	$dash = dashboard($user,false,true); // side effects needed, must go first
	$markup =<<<XXX
	<fb:fbml version='1.1'><fb:title>Settings</fb:title>
	$dash
	<fb:explanation><fb:message>Connect $u->accountlabel Care Team to Facebook Group</fb:message>
	<p><img src='${appUrl}images/speech.png'/>&nbsp;The group picture and description will be utilized for your dashboard avatar.</p>
	<p><img src='${appUrl}images/speech.png'/>&nbsp;You must be a member of the facebook group to connect to your Care Teamj</p>
	<p>
	<fb:editor action="settings.php?gconnect" labelwidth="100">
	<fb:editor-text label="Facebook Group ID" name="gid" value=""/>
	<fb:editor-buttonset>  <fb:editor-button value="Connect to Facebook Group" onclick="this.disabled = true;" />
	</fb:editor-buttonset>
	</fb:editor>
	</p>
	</fb:explanation>
	<fb:explanation><fb:message>Connect $u->accountlabel Care Team to Facebook Page</fb:message>
	<p><img src='${appUrl}images/speech.png'/>&nbsp;The Page picture and description will be utilized for your dashboard avatar.</p>
	<p><img src='${appUrl}images/speech.png'/>&nbsp;You must be a member of the facebook group to connect to your Care Teamj</p>
	<p>
	<fb:editor action="settings.php?pconnect" labelwidth="100">
	<fb:editor-text label="Facebook Page ID" name="pid" value=""/>
   <fb:editor-buttonset>  <fb:editor-button value="Connect to Facebook Page" onclick="this.disabled = true;" /> 
     </fb:editor-buttonset> 
</fb:editor> 
</p>
	</fb:explanation>
</fb:fbml>
XXX;
	return $markup;
}
// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit();
// *** end of basic start
$appname = $GLOBALS['healthbook_application_name'];
$apikey = $GLOBALS['appapikey'];
$uber = $GLOBALS['uber'];

$u =mustload($facebook,$user) ; // goes no further if not logged in

if (isset($_REQUEST['newelder'])) {create_subordinate_account($facebook, $u, $user,$u->mcid); exit;}/*else

/* whenever we run settings go get stuff from facebook servers  and patch into our own */

list($fn,$ln,$ps,$sx) = facebook_user_info($facebook,$user);
if (isset($_REQUEST['label']))
{
	//change the account label for this user

	$accountlabel = $_REQUEST['label'];
	$extra = ", accountlabel='$accountlabel' ";
}
else $extra='';

$q = "update users set firstname = '$fn', lastname = '$ln', photoUrl = '$ps', sex='$sx' $extra where fbid='$user' ";
$result2 = mysql_query($q) or die("cant update from  $q ".mysql_error());
$page = $GLOBALS['facebook_application_url'];


if (isset($_REQUEST['newacct']))
{
	// Send them off to amazon devpay
	/*	$returnUrl = $GLOBALS['facebook_application_url']."settings.php?paid_newacct=true";
	logHBEvent($user,'amz',"Off to amazon $returnUrl");  // was misspelled and spewing into log
	$page = $GLOBALS['devpay_redir_url'].'?src='.urlencode($returnUrl);
	echo "<fb:fbml version='1.1'>redirecting to $page<fb:redirect url='$page'/></fb:fbml>";
	exit;
	}
	else
	if(isset($_REQUEST['paid_newacct']))
	{
	// Get the amazon activation key
	$key = $_POST['ActivationKey'];
	$product = $_POST['ProductCode'];
	*/

	$key = 0;
	$product = 0; // just bypass amazon for now


	$mcid = insertUser($user,$fn,$ln,$sex,$city,$state,$country,$pic,$key,$product);

	if($mcid === false) {
		$dash = dashboard($user);
		$out="<fb:fbml version='1.1'>
		$dash
		<fb:error message='Error Occurred'>
		A system error occurred while creating your long term storage account and
		connecting it to your Facebook account.
		</fb:error>
		</fb:fbml>";
		echo $out;
		logHBEvent($user,'amz',"back from amazon could not make user account");
		return;
	}

	$hurl = "$uber/$mcid";
	opsMailBody(  "$appname says facebook account $user $fn $ln created medcommons account $mcid",
	"<br>A MedCommons Account was created for facebook user $fn $ln</br>".
	"<br>You can access the user's facebook profile at http://www.facebook.com/profile.php?id=$user</br>".
	"<br>You can attempt to access the user's healthurl at $hurl</br>");

	logHBEvent($user,'amz',"back from amazon healthurl is $hurl");
	$redir = $GLOBALS['facebook_application_url']."settings.php?newacct_done=true";
	echo "<fb:fbml version='1.1'><fb:redirect url='$redir' /></fb:fbml>";
	exit;
}
else
if(isset($_REQUEST['newacct_done'])) {
	$dash = dashboard($user);
	echo "
	<fb:fbml version='1.1'>
	$dash
	<fb:success>
	<fb:message>A Long Term Account was created for $fn $ln</fb:message>
	You can keep your personal records in HealthBook. You can also become a Care Giver.
	</fb:success>
  ";
	include "confirm_account_warning.php";
	echo "</fb:fbml>";
	exit;
}
else
if (isset($_REQUEST['discon']))
{
	$mcid = dissociateUser($user);
	if($mcid === false) {
		echo "<fb:fbml version='1.1'><fb:error>
      <fb:message>A Problem Occurred While Disconnecting Your Account</fb:message> 
      <p>A system error occurred while we were disconnecting your account.</p>
      <p>Your account has been disconnected, however you may find there are still
         consents relating to your Facebook account in your old MedCommons storage account.</p>
      </fb:error></fbml>";

		logHBEvent($user,'disc',"disconnect failure from healthurl");

		exit;
	}

	$hurl = "$uber/$mcid";

	logHBEvent($user,'disc',"disconnected from healthurl $hurl");
	opsMailBody(  "$appname says facebook account $user $fn $ln disconnected from  medcommons storage and services",
	"<br>Facebook user $fn $ln disconnected from medcommons account $mcid</br>".
	"<br>You can access the user's facebook profile at http://www.facebook.com/profile.php?id=$user</br>".
	"<br>You can attempt to access the user's healthurl in the disconnected account at at $hurl</br>");

	//republish_user_profile($user);

	$page = $GLOBALS['facebook_application_url'];
	$markup =  "<fb:fbml version='1.1'>redirecting via facebook to $page". "<fb:redirect url='$page' /></fb:fbml>";
	echo  $markup;
	exit;
}
else
if (isset($_REQUEST['connect']))
$markup= connect_page($facebook,$user,$u);//end of settings
else
if (isset($_REQUEST['pconnect']))
$markup= pconnect_page($facebook,$user,$u);//end of settings
else
if (isset($_REQUEST['pconnectfin']))
$markup= pconnect_fin($facebook,$user,$u);//end of settings
else
if (isset($_REQUEST['gconnect']))
$markup= gconnect_page($facebook,$user,$u);//end of settings
else
if (isset($_REQUEST['gconnectfin']))
$markup= gconnect_fin($facebook,$user,$u);//end of settings
else
$markup= settings_page($facebook,$user,$u);//end of settings


echo $markup;



// old stuff

function obsolete()
{

	function disconnection_settings($user,$u)
	{
		// if he wants settings, then if he has if a real account, force a disconnect
		$appname = $GLOBALS['healthbook_application_name'];
		$apikey = $GLOBALS['appapikey'];
		publish_info($user);


		$addprofile = <<<XXX
<fb:if-section-not-added section="profile">
    <fb:success>
      <fb:message>Add My Info to My Profile Box 
          <div style='margin: 10px 0px'><fb:add-section-button section="profile" /></div>
       </fb:message>
       <small>All of your friends will see this information, You can always remove this directly .</small>
      </fb:success>
</fb:if-section-not-added>
XXX;

		$mcid = $u->mcid;
		$appliance = $u->appliance;
		$hurl = rtrim($appliance,'/').'/'.$mcid;
		$markup= <<<XXX

		<fb:if-is-app-user>
		<fb:success>
		<fb:message>{$u->getFirstName()} {$u->getLastName()}'s Private HealthURL Storage Location is <br/>
		<div style='text-align: left; margin: 10px 0px;'>
		<a target='_new' href='$hurl' title='Open HealthURL in a new window' ><img src="http://www.medcommons.net/images/icon_healthURL.gif" /> $hurl</a>
		</div>
		</fb:message>

		Your Facebook login and Care Givers are connected for direct access to your
		Private HealthURL through the HealthBook Application. If you disconnect or
		remove the HealthBook Application from your Facebook profile, your Private
		HealthURL storage account will not be affected but your Care Givers will lose
		access. To restore, and to view your content, you will need to have the
		HealthBook application and connect to <a target='_new' href='$hurl' title='Open HealthURL in a new window' >
		<img src="http://www.medcommons.net/images/icon_healthURL.gif" />$hurl</a>

		<fb:dialog id="my_dialog" cancel_button=1>
		<fb:dialog-title>Disconnect from My MedCommons Account</fb:dialog-title>
		<fb:dialog-content><form id="my_form">Do you really want to disconnect from your MedCommons Account?</form></fb:dialog-content>
		<fb:dialog-button type="button" value="Yes" href="settings.php?discon" />
		</fb:dialog>
		<div style='text-align: center; margin: 10px 0px;'>
		<a href="#" clicktoshowdialog="my_dialog"><button class='confirmbuttonstyle'>Disconnect</button></a>
		</div>
		</fb:success>

		$addprofile

		<fb:success>
		<fb:message>Login to {$u->getFirstName()} {$u->getLastName()}'s Health URL Host</fb:message>
		If you are the patient, or Custodian, you can login directly to the MedCommons Account on $appliance
		<fb:editor action="$appliance/acct/login.php?mcid=$mcid" labelwidth="100">
		<fb:editor-buttonset>
		<fb:editor-button value='Sign In Directly to Your HealthURL' />
		</fb:editor-buttonset>
		</fb:success>
		<fb:explanation>
		<fb:message>{$u->getFirstName()} {$u->getLastName()}'s Health URL Host is $appliance</fb:message>

		Your HealthURL host may allow you to move account $mcid  to a different host. Please contact them directly for instructions on closing or moving an account. A directory of HealthURL hosting providers is available at MedCommons.
		<fb:editor action="{$u->gw}/PersonalBackup" labelwidth="100">
		<input type='hidden' name='storageId' value='$mcid' />
		<input type='hidden' name='auth' value='{$u->token}' />
		<fb:editor-buttonset>
		<fb:editor-button value="Download All Documents"/>
		</fb:editor-buttonset>
		</fb:explanation>
		<fb:else> <fb:error>
		<fb:message>You must add $appname to your facebook account to use long-term storage       <a class=applink href='http://www.facebook.com/add.php?api_key=$apikey&app_ref=discon' ><img src='http://photos-d.ak.facebook.com/photos-ak-sctm/v43/135/6471872199/app_2_6471872199_5603.gif' />add app</a></fb:message>
      </fb:error></fb:else>
</fb:if-is-app-user>
XXX;
		return $markup;
	}

	function noadmin_settings($fbid)
	{

		$markup = <<<xxx
		<fb:success><fb:message>You are a member of a Family Care Team</fb:message>
		<p>You can not create a personal medcommons account and remain a member of this teamn created by <fb:name uid=$fbid/></p>
   <p>You should remove yourself from the team and then try again</p>
</fb:success>
xxx;

		return $markup;
	}

	function connection_settings()
	{
		$appname = $GLOBALS['healthbook_application_name'];
		$apikey = $GLOBALS['appapikey'];
		$newAcctAppliance=rtrim($GLOBALS['new_account_appliance'],'/');
		$connectold = <<<xxx
		<fb:explanation><fb:message>Connect to an Existing Private HealthURL</fb:message>
		<p>You can connect to an existing MedCommons Account  or you can connect to the Jane H. demonstration account.</p>
		<fb:editor action="authorize_join.php" labelwidth="100">
		<fb:editor-text label="HealthURL" name="hurl" value=""/>
		</fb:editor-custom>
		<fb:editor-buttonset>
		<fb:editor-button value="connect"/>
		</fb:editor-buttonset>
		</fb:editor>
		<p>Note: You will be redirected to an authorization page to grant $appname access to your account. If you want to connect to a demo account you can use
		$newAcctAppliance/1013062431111407
  </p>
</fb:explanation>
xxx;

		require_once "noacct_blurb.inc.php";
		$markup = <<<XXX
		<fb:if-is-app-user>
		$noacct_blurb
		<fb:explanation>
		<fb:message>I Want to Create a New MedCommons Account</fb:message>
		<p>A new MedCommons account will be created and associated with your facebook account - use your facebook credentials to access healthbook</p>
		<fb:editor action='settings.php'>
		<input type=hidden value=newacct name=newacct />
		<fb:editor-buttonset>
		<fb:editor-button value="create account" />
		</fb:editor-buttonset>
		</fb:editor>
		<p>Note: you will be forwarded to Amazon Payments to purchase a MedCommons subscription.</p>
		</fb:explanation>
		$connectold
		<fb:else> <fb:error>
		<fb:message>You must add $appname to your facebook account to maintain health records <a class=applink href='http://www.facebook.com/add.php?api_key=$apikey&app_ref=posturl' ><img src='http://photos-d.ak.facebook.com/photos-ak-sctm/v43/135/6471872199/app_2_6471872199_5603.gif' />add app</a></fb:message> </fb:error></fb:else>
</fb:if-is-app-user>
XXX;
		return $markup;
	}

	function dissociateUser($user)
	{
		// keep the healthbook entry around, just take out the medcommons account
		// also blow away the careteam if any
		$q ="select mcid from users where fbid='$user'";
		$result = mysql_query($q) or die("cant   $q ".mysql_error());
		$r = mysql_fetch_array($result);
		$success = true;
		if ($r) {
			$mcid = $r[0];
			// also blow away the careteam but dont blow away caregiving  until we remove the app
			//	$q ="delete from careteams  where mcid='$mcid' "; // or giverfbid='$user' ";
			//	$result = mysql_query($q) or die("cant   $q ".mysql_error());


			// remove the consent
			try {
				$u = HealthBookUser::load($user);
				$api = $u->getOAuthAPI();
				if($api) {
					$api->destroy_token($u->token);
				}
			}
			catch(Exception $e) {
				error_log("Unable to delete user user token: ".$e->getMessage());
				$success = false;
			}

			$q = "update users set mcid='0',sponsorfbid='0',targetmcid='0', applianceurl = '', gw='', oauth_token = NULL, oauth_secret = NULL, storage_account_claimed = 0 where fbid='$user'";// and mcid = '$mcid'";
			$result = mysql_query($q) or die("cant   $q ".mysql_error());
			logHBEvent($user,'view',"Now viewing nothing");
			if($success)
			return $mcid;
			else
			return false;
		}
		return false;
	}
	function wantsnoacct($user)
	{
		$q = "replace into  users set mcid='0', sponsorfbid='0',  targetmcid='0',  fbid='$user'";// and mcid = '$mcid'";
		$result = mysql_query($q) or die("cant   $q ".mysql_error());
		logHBEvent($user,'view',"Turned off MedCommons Account");
	}

}

?>
