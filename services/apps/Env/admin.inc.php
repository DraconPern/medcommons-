<?php

//
//
// handle postbacks from forms for Envision Administration
//
//


require_once "simtrak.inc.php";

// these routines are only used by these postbacks

function standard_toptoo ()
{
	global $serviceskin_;

	return <<<XXX
	<DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
	<html><head><meta http-equiv="content-type" content="text/html; charset=utf-8">	<title>MedCommons SimTrak Explorer V0.7</title>
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.6.0/build/autocomplete/assets/skins/sam/autocomplete.css" />
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.6.0/build/fonts/fonts-min.css" />
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.6.0/build/calendar/assets/skins/sam/calendar.css" />
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.6.0/build/treeview/assets/skins/sam/treeview.css" />
	<link rel="stylesheet" type="text/css" href="/yui/2.6.0/fonts/fonts-min.css" />
	<link rel="stylesheet" type="text/css" href="/yui/2.6.0/tabview/assets/skins/sam/tabview.css" />
	<link rel="stylesheet" type="text/css" href="/yui/2.6.0/datatable/assets/skins/sam/datatable.css" />
	<link  href="DataKit/inputex/0.2.0/inputex-min.css" rel="stylesheet" type="text/css" />
	<script type="text/javascript" src="/yui/2.6.0/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/element/element-beta-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/tabview/tabview-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/datatable/datatable-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/container/container-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/connection/connection-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/json/json-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/utilities/utilities.js"></script>
	<script src="DataKit/inputex/0.2.0/inputex-min.js" type="text/javascript"></script>
	<script type="text/javascript" src="http://yui.yahooapis.com/2.6.0/build/animation/animation-min.js"></script>
	<script type="text/javascript" src="http://yui.yahooapis.com/2.6.0/build/autocomplete/autocomplete-min.js"></script>
	<script type="text/javascript" src="http://yui.yahooapis.com/2.6.0/build/calendar/calendar-min.js"></script>
	<script type="text/javascript" src="http://yui.yahooapis.com/2.6.0/build/treeview/treeview-min.js"></script>


	<!---
	<script type="text/javascript" src="/yui/2.6.0/element/element-beta-debug.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/tabview/tabview-debug.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/datasource/datasource-debug.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/datatable/datatable-debug.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/container/container-debug.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/connection/connection-debug.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/json/json-min.js"></script>
	-->

	<style type="text/css">
	/*	margin and padding on body element can introduce errors in determining element position and are not recommended;
	we turn them off as a foundation for YUI CSS treatments. */
	body {	margin:0;	padding:0; }
	.simtrak_viewer {  padding: 5px 0 0 15px; width: 90%;}
	.pane {position: relative; border: 1px solid; margin: 15px; padding: 10px; background-color:#e3e3e3; }
	.paneline: {display: block;}
	.fieldprompt {padding: 10px 20px 10px 20px;font-weight: 600; }
	.valuef {padding: 2px;font-size:.9em;}
	#tab_INJURY {font-size:.85em}
	a.paneSaveImg {
	position: absolute;
	right: 60px;
	top: 10px;
}
input.modified {
background-color: #DEBFBF;
}
.invisible {
visibility: hidden;
}

table th .yui-dt-label a,
table th .yui-dt-label a.yui-dt-sortable {
white-space: normal !important;
}

.yui-skin-sam .yui-navset .yui-content {
background-color: #f8f8f8 !important;
padding: 10px;
}
.yui-skin-sam .yui-navset li a em {
color: #333;
}
.yui-nav {
border-color: #828076 !important;
}
.yui-skin-sam .yui-navset li.selected a em {
color: white;
font-weight: bold;
}
#wrapper .yui-navset a:hover
{
text-decoration: none;
}
div.simtrak_viewer a {
color: #5987AC;
text-decoration: none;
}
div.simtrak_viewer a:hover,  div.simtrak_viewer a:active  {
text-decoration: underline;
}
div.paneline {
width:  600px;
clear: both;
}
div.paneline span.fieldprompt {
float: left;
display: block;
width: 120px;
text-align: right;
position: relative;
top: -4px;
color: #3B5269;
}
h3 {
color: #3B5269;
}
.saveAllIcon {
position: absolute;
right: 30px;
top: 10px;
}
h3 img {border: 0}

.floatright { float:right;}
#myAutoComplete {
width:17em; /* set width here or else widget will expand to fit its container */
padding-bottom:1em;
}
#mySubmit {
margin-left:19em; /* place the button next to the input */
}
#myForm {margin-right:3em; }
h3 input {line-height: 1.1em; font-size: .9em;}

.ppic,.tpic,.lpic {border:0; width:50px; max-height:80px;}

.listinlinetiny li {font-size: .8em; display: inline;}
#navcontainer h4 {padding:3px 0 0 0; margin:0;}
#navcontainer h3 {padding:5px 0 0 0; margin:0;}
#navcontainer #navlist {padding: 0 0 15px 3px; margin:0;}
#adminstuff {font-size: 18px; margin-top:10px;}
#mystuff {font-size:18px; margin-botom:10px;}
a small { padding-left:2em; font-size:.7em; color: #bbb;}
</style>
<link rel="stylesheet" type="text/css" href="$serviceskin_" />
</head>
<body class=" yui-skin-sam">
<div class="simtrak_viewer">
	<img src='images/BluePoweredByMasterTrans250x50.png'/>
XXX;
}
function playerchoiceformtoo()
{
	return <<<XXX
	<form id="myForm" action="envision.php" >
    <div id="myAutoComplete">
    	<input id="myInput"  size=40 type="text"><input id="mySubmit" type="submit" value="go"> 
    	<div id="myContainer"></div>
    </div>
    <input id="myHidden" name="accid" type="hidden">
    <input id="myHidden2" name="admin" type="hidden">
</form>
XXX;
}
function trainerchooser($team){ return teamroleuserchooser($team,'team') ;}
function teamroleuserchooser($team,$role)
{
	// returns a big select statement
	$teamind=get_teamind($team);

	$outstr = "<select name='name'>";
	$result = dosql ("SELECT * from users  where teamind= '$teamind' and role='$role' ");


	while ($r2 = isdb_fetch_object($result))
	{

		//$ename = urlencode($name);
		$selected ='';// ($name == $player)?' selected ':'';
		$outstr .="<option value='$r2->email $r2->openid' $selected >$r2->openid</option>
		";

	}
	$outstr.="</select>";
	return $outstr;

}


function leagueadminchooser($leagueind){ return leagueroleuserchooser($leagueind,'league') ;}
function leagueroleuserchooser($leagueind,$role)
{


	$outstr = "<select name='name'>";
	$result = dosql ("SELECT * from users  where leagueind= '$leagueind' and role='$role' ");


	while ($r2 = isdb_fetch_object($result))
	{

		//$ename = urlencode($name);
		$selected ='';// ($name == $player)?' selected ':'';
		$outstr .="<option value='$r2->email $r2->openid' $selected >$r2->openid</option>
		";

	}
	$outstr.="</select>";
	return $outstr;

}

function isheader($title,$priv){		return standard_toptoo();}
function makeplayer ($healthurl, $ln,$fn,$dob,$sex,$img, $teamind, $status)
{
	dbg("making player with healthurl $healthurl");
	if (!$healthurl)
	{
		// if none supplied, then make one
		$remoteurl = $GLOBALS['appliance']."/router/NewPatient.action?familyName=$ln&givenName=$fn&dateOfBirth=$dob".
      "&sex=$sex".
      "&auth=".$GLOBALS['appliance_access_token'].
      "&oauth_consumer_key=".$GLOBALS['appliance_access_token']; 

		try {
			// consumer token when creating patient
			$file = get_url($remoteurl);
			$json = new Services_JSON();
			$result = $json->decode($file);
			if(!$result)
			throw new Exception("Unable decode JSON returned from URL ".$remoteurl.": ".$file);

			if($result->status != "ok")
			throw new Exception("Bad status '".$result->status."' error='".$result->error."' returned from JSON call ".$remoteurl);

			$mcid = $result->patientMedCommonsId;
			$auth = $result->auth;
			$secret = $result->secret;
			$healthurl = $GLOBALS['appliance'].$mcid;
		}
		catch(Exception $ex) {
			islog("?","Failed to create new patient.", $ex->getMessage());
			die("Unable to create new patient");
		}
		dbg("created healthurl $healthurl auth $auth secret $secret");
	}
	else {
		$auth = "";
		$secret = "";
	}
	$team = teamnamefromind($teamind);
	// lets be careful and make sure we always make new records
	$sql = "Insert into players set name='$fn $ln', team='$team', imageurl='$img', oauthtoken='$auth,$secret', born='$dob', status='$status',healthurl='$healthurl' ";
	$status =mysql_query($sql );
	if ($status == false ) return false;

	$playerind = isdb_insert_id(); // get last
	dosql("Insert into teamplayers set teamind='$teamind', playerind='$playerind' ");
	return $playerind;
}

function teamsetupform($league,$team,$teamerr, $hp,$hperr,$sc,$scerr,$news,$newserr,$logo,$logoerr)
{
	$form =<<<XXX
	<table>
	<tr><td class=prompt>league</td><td>$league</td><td></td></tr>
	<tr><td class=prompt>team name</td><td><input class=infield type=text name=team value='$team' /></td><td class=errfield>$teamerr</td></tr>
	<tr><td class=prompt>home page url</td><td><input class=infield type=text name=homepageurl value='$hp' /></td><td class=errfield>$hperr</td></tr>
	<tr><td class=prompt>schedule url</td><td><input class=infield type=text name=schedurl value='$sc' /></td><td class=errfield>$scerr</td></tr>
	<tr><td class=prompt>rss news url</td><td><input class=infield type=text name=newsurl value='$news' /></td><td class=errfield>$newserr</td></tr>
	<tr><td class=prompt>logo url</td><td><input class=infield type=text name=logourl value='$logo' /></td><td class=errfield>$logoerr</td></tr>

<tr><td></td><td></td><td></td></tr>
</table>
<input type=submit name=submit value=submit />
XXX;
	return $form;
}

function playersetupform($team,$fn,$fnerr,$gn,$gnerr,$dob,$doberr,$sex,$sexerr,$img,$imgerr,$hurl,$hurlerr,$oauth,$oautherr)
{
	//<tr><td class=prompt>oauth</td><td><input class=infield type=text name=oauth value='$oauth' /></td><td class=errfield>$oautherr</td></tr>
	$maleselected= ($sex=='M')?'selected':'';
	$femaleselected= ($sex=='F')?'selected':'';
	if ($doberr=='') $doberr="<small>e.g. 11/23/87</small>";
	if ($oautherr=='') $oautherr="<small>token,secret pair, leave blank to authorize after submission</small>";
	$form =<<<XXX
	<input type='hidden' name='oauth' value='$oauth'/>
	<h4>Create New Player</h4>
	<table>
	<tr><td class=prompt>family name</td><td><input class=infield type=text name=familyName value='$fn' /></td><td class=errfield>$fnerr</td></tr>
	<tr><td class=prompt>given name</td><td><input class=infield type=text name=givenName value='$gn' /></td><td class=errfield>$gnerr</td></tr>
	<tr><td class=prompt>date of birth</td><td><input class=infield type=text name=dateOfBirth value='$dob' /></td><td class=errfield>$doberr</td></tr>
	<tr><td class=prompt>image url</td><td><input class=infield type=text name=image value='$img' /></td><td class=errfield>$imgerr</td></tr>

	<tr><td class=prompt>sex</td><td><select  class=infield name=sex>
	<option value='M' $maleselected >male</option>
	<option value='F' $femaleselected >female</option>
	</td><td>$sexerr</td></tr>
	<tr><td></td><td><input type=submit name=addplayerpost value='Create Player'/></td><td></td></tr>
	</table>
	<h4>Import Existing HealthURL</h4>
	<table>
	<tr><td class=prompt>HealthURL</td><td><input class=infield type=text name=hurl size='50' value='$hurl' onchange='document.isform.oauth.value=""' /></td>
	<td class=errfield>$hurlerr</td></tr>
<tr><td>&nbsp;</td><td><input type='submit' name='importplayer' value='Import Player'/></td><td></td></tr>
</table>
</div>

XXX;
	return $form;
}

function trainersetupform($team,$email,$emailerr,$openid,$openiderr,$sms,$smserr){	return usersetupform('team',$team,$email,$emailerr,$openid,$openiderr,$sms,$smserr);}
function leagueadminsetupform($team,$email,$emailerr,$openid,$openiderr,$sms,$smserr){	return usersetupform('league',$team,$email,$emailerr,$openid,$openiderr,$sms,$smserr);
}
function usersetupform($role,$team,$email,$emailerr,$openid,$openiderr,$sms,$smserr)
{
	$form =<<<XXX
	<table>
	<tr><td class=prompt>role</td><td>$role</td><td></td></tr>
	<tr><td class=prompt>team</td><td>$team</td><td></td></tr>
	<tr><td class=prompt>email</td><td><input class=infield type=text name=email value='$email' /></td><td class=errfield>$emailerr</td></tr>
	<tr><td class=prompt>medcommons id</td><td><input class=infield type=text name=openid value='$openid' /></td><td class=errfield>$openiderr</td></tr>
	<tr><td class=prompt>sms</td><td><input class=infield type=text name=sms value='$sms' /></td><td class=errfield>$smserr</td></tr>
<tr><td></td><td></td><td></td></tr>
</table>
<input type=submit name=submit value='Setup Trainer' />
XXX;
	return $form;
}

function fullteamchooser($id)
{
	// returns a big select statement
	$outstr = "<select $id name='teamind'>";
	$result = dosql ("SELECT t.name,t.teamind,l.name from teams t, leagueteams lt, leagues l
	                                         where  lt.teamind = t.teamind and lt.leagueind=l.ind
	                                                               order by l.name, t.name");
	$first = true;
	while ($r2 = isdb_fetch_array($result))
	{
		$team = $r2[0]; $teamind = $r2[1]; $league = $r2[2];
		//$ename = urlencode($name);
		$selected = ($first)?' selected ':'';
		$outstr .="<option value='$teamind' $selected >$league:$team</option>";
		$first = false;
	}
	$outstr.="</select>";
	return $outstr;
}

//*************
//
// main dispatcher loop parses incoming args and branches
//
//*************


$reportchooser = '';

if (isset($_REQUEST['priv'])){
	$playerind = $_REQUEST['playerind'];
	$player = getplayerbyind($playerind);
	$reportchooser = player_report_chooser('is',$player->team ,$player->name,-1);
	$header = isheader('Privileged functions for IS only' ,true);

	if (isset($_REQUEST['report']))
	{          $rpt = $_REQUEST['report'];

	list ($a,$b) =is_report_section('is',$player->team,$player->name,"_alerts_$player->name",$rpt,
	'ggg','fff','ttt','nnn');

	$body = " $reportchooser $a $b";
	}
	else $body = $reportchooser;
	$markup = <<<XXX
	$header
	<div id='is_body'><h5>Choose HealthURL function for Player $player->name</h5>
	<div class=ispanel>
	$body
</div>
<div id='is_footer'>
</div>
</div>
</body>
XXX;
	echo $markup;
	exit;
}
if (isset($_REQUEST['addtrainerpost']))
{
	$any=false;
	$teamind = $_REQUEST['teamind'];
	$role = $_REQUEST['role'];
	$openid = $_REQUEST['openid']; $fnerr='';
	$email = $_REQUEST['email']; $gnerr='';
	$sms = $_REQUEST['sms']; $smserr='';
	$team = teamnamefromind($teamind);
	// edit check all the fields
	//if (substr($openid,0,7)!='http://')  {$fnerr = "real openid with http: must be specified"; $any=true;} else
	//if (strpos($openid,"'")) {$fnerr = "no quotes allowed in openid"; $any=true;} else
	//if (substr($openid,strlen($openid)-1,1)!='/') {$fnerr = "last char in openid must be /"; $any=true;}


	if (strlen($email)<10) {$gnerr = "real email must be specified";$any=true;}

	if ($any) {
		//addplayerpost
		$header = isheader('Error adding new trainer',true);
		$formbody = usersetupform($role,$team, $email,$gnerr,$openid,$fnerr,$sms,$smserr);
		$markup = <<<XXX
		$header
		<div id='is_body'><h5>Please correct these errors to add a trainer (role: $role)</h5>
		<div class=ispanel>
		<form action="?" method=post>
		<input type=hidden name=addtrainerpost value=addtrainerrpost />
		<input type=hidden name=teamind value=$teamind />
		<input type=hidden  name=role value=$role />
		$formbody
</form>
</div>
<div id='is_footer'>
</div>
</div>
</body>
XXX;
		echo $markup;
		exit;
	}

	$result =mysql_query("Insert into users set email='$email',sms='$sms', openid='$openid',teamind='$teamind',role='$role' ");
	if ($result == false) $loc = "is.php?err=duplicateUser"; else $loc ="is.php?err=completedok";
	header ("Location: $loc");
	echo "Redirecting to $loc";
	exit;
} else
if (isset($_REQUEST['deltrainerpost']))
{
	$name = $_REQUEST['name'];
	$teamind = $_REQUEST['teamind'];
	$team=teamnamefromind($teamind);
	dosql("DELETE from users where email='$name' and teamind='$teamind' and role='team' ");
	$header = isheader("Removed trainer $name ",true);
	$markup = <<<XXX
	$header
	<div id='is_body'>
	<h5>Trainer $name was removed from  Team $team</h5>
</div>
<div id='is_footer'>
</div>
</div>
</body>
XXX;
	echo $markup;
	exit;
} else
if (isset($_REQUEST['delplayerpost']))
{
	$name = $_REQUEST['name'];
	$team = $_REQUEST['team'];
	// remove links into here
	$playerind =get_playerind($name);
	$teamind = get_teamind($team);
	dosql("DELETE FROM teamplayers where playerind = '$playerind' and teamind = '$teamind'");
	$result = dosql("Select healthurl from players where playerind='$playerind' ");
	$r=isdb_fetch_object($result);
	$healthurl ="<a target='_new' href='$r->healthurl' title='this healthurl is in MedCommons and is always accessible to qualified users'>$r->healthurl</a>";
	dosql("DELETE from players where playerind='$playerind' and team='$team' ");

	$header = isheader("Removed player $name (was on $team)",true);
	$markup = <<<XXX
	$header
	<div id='is_body'><h5>Player $name was removed from Informed Sports</h5>
	<div class=ispanel>
	<p>The associated healthurl $healthurl is still viable and can be utilized again if you choose to add the player at a later date</p>
</div>
<div id='is_footer'>
</div>
</div>
</body>
XXX;
	echo $markup;
	exit;
} else

if (isset($_REQUEST['moveplayerpost']))
{
	$name = $_REQUEST['name'];
	$fromteam = $_REQUEST['fromteam'];
	$toteamind = $_REQUEST['teamind'];
	$toteam = teamnamefromind($toteamind);
	// remove links into here
	$playerind =get_playerind($name);
	$fromteamind = get_teamind($fromteam);
	dosql("Update  teamplayers set teamind='$toteamind'  where playerind = '$playerind' and teamind = '$fromteamind'");
	$result = dosql("Select healthurl from players where playerind='$playerind' ");
	$r=isdb_fetch_object($result);
	$healthurl ="<a target='_new' href='$r->healthurl' title='this healthurl is in MedCommons and is always accessible to qualified users'>$r->healthurl</a>";
	dosql("Update players set team='$toteam'  where playerind='$playerind' and team='$fromteam' ");
	$header = isheader("Moved player $name  from $fromteam to $toteam",true);
	$markup = <<<XXX
	$header
	<div id='is_body'>
	<h5>Player $name was Moved from $fromteam to $toteam </h5>
	<div class=ispanel>
	<p>The associated healthurl $healthurl is still viable and associated with $name</p>
</div>
<div id='is_footer'>
</div>
</div>
</body>
XXX;
	echo $markup;
	exit;
} else
if (isset($_REQUEST['importplayer']))
{
	try {
		$hurl = $_REQUEST['hurl']; $hurlerr='';
		$teamind = $_REQUEST['teamind'];
		$team = teamnamefromind($teamind);
		if($team === false)
		throw new Exception("Unable to determine team name for team $teamind");

		$callback = get_trust_root()."is.php?authorize_player";
		list($req_token,$url)= ApplianceApi::authorize($GLOBALS['appliance_access_token'],$GLOBALS['appliance_access_secret'],$hurl,$callback);

		// set cookie with token and secret
		setcookie('oauth', $req_token->key.",".$req_token->secret.",".$hurl.",".$teamind, time()+300); // expire after 300 seconds


		// Add on team name as realm
		// TODO: what is the real realm???
		$url.="&realm=".urlencode($team);

		header("Location: $url");
		exit;
	}
	catch(Exception $e) {
		die(isheader('Error adding new player',true)."<p>An error occurred while attempting to authorize the HealthURL you entered.</p><pre>{$e->getMessage()}</pre>");
	}

	exit;

} else
if (isset($_REQUEST['addplayerpost']))
{
	$any=false;
	$teamind = $_REQUEST['teamind'];
	$fn = $_REQUEST['familyName']; $fnerr='';
	$gn = $_REQUEST['givenName']; $gnerr='';
	$dob = $_REQUEST['dateOfBirth']; $doberr='';

	$sex = $_REQUEST['sex']; $sexerr='';
	$img = $_REQUEST['image']; $imgerr='';
	$hurl = $_REQUEST['hurl']; $hurlerr='';
	$oauth = $_REQUEST['oauth']; $oautherr='';
	$team = teamnamefromind($teamind);
	// edit check all the fields
	if (strpos($fn,"'")) {$fnerr = "no quotes allowed in family name"; $any=true;}
	if (strpos($gn,"'")) {$gnerr = "no quotes allowed in given name";$any=true;}
	if ($hurl!='') if ($oauth=='') {$oautherr="Please authorize this HealthURL"; $any=true;}

	if ($any) {
		//addplayerpost
		$header = isheader('Error adding new player',true);
		$formbody = playersetupform($team, $fn,$fnerr,$gn,$gnerr,$dob,$doberr,$sex,$sexerr,$img,$imgerr,$hurl,$hurlerr,$oauth,$oautherr);
		$markup = <<<XXX
		$header
		<div id='is_body'>
		<h5>please correct these errors to add a player</h5>
		<div class=ispanel>
		<form name='isform' action="?" method=post>
		<input type=hidden name=teamind value=$teamind />
		$formbody
</form>
</div>
<div id='is_footer'>
</div>
</div>
</body>
XXX;
		echo $markup;
		exit;
	}
	// otherwise create the healthurl and then create the player in our tables
	//echo "making healthurl and then player $fn $gn";
	$playerind = makeplayer ($hurl, $fn,$gn,$dob,$sex,$img,$teamind,'test');
	if ($playerind == false) {
		dbg("dupe player");
		$loc = "is.php?addplayer=add&teamind=$teamind&err=".urlencode("Duplicate Player");
	}
	else  {  // success
		$loc ="p.php?playerind=$playerind";
	}

	dbg("redirecting to $loc");
	header ("Location: $loc");
	echo "Redirecting to $loc";
	exit;

} else
if (isset($_REQUEST['authorize_player'])) { // Appliance callback for successful authorization

	dbg("successful return from authorization call");

	if(!isset($_COOKIE['oauth']))
	die(isheader('Error adding new player',true)."<p>An error occurred while attempting to authorize the HealthURL you entered - missing cookie</p>");

	$oauth = explode(",",$_COOKIE['oauth']);
	$hurl = $oauth[2];
	$teamind = $oauth[3];

	dbg("access token from cookie ".$oauth[0]." / ".$oauth[1]);

	try {
		$api = ApplianceApi::confirm_authorization($GLOBALS['appliance_access_token'],$GLOBALS['appliance_access_secret'],$oauth[0], $oauth[1],$hurl);

		$access_token = "{$api->access_token->key},{$api->access_token->secret}";

		dbg("access token: $access_token");

		list($base_url,$accid) = $api->parse_health_url($hurl);

		// Now we have the gateway, get the CCR
		$ccr = $api->get_ccr($accid);

		// Got the CCR
		// Get the important details of this patient
		// We have to iterate all the actors looking for the patient
		$patientActorID = $ccr->patient->actorID;
		foreach($ccr->actors->actor as $a) {
			if($a->actorObjectID == $patientActorID) {
				$given = $a->person->name->currentName->given;
				$family = $a->person->name->currentName->family;
				$dob = $a->person->dateOfBirth;

				if(isset($dob->exactDateTime)) {
					$age = (int)((time() - strtotime($dob->exactDateTime)) /  ( 365 * 24 * 60 * 60 ));
				}
				else
				if(isset($dob->age))
				$age = (int)$dob->age->value;

				if(isset($a->person->gender)) {
					$gender = $a->person->gender->text;
				}

				// Found patient, we're done
				break;
			}
		}

		$fmtDob = $dob->exactDateTime ? date("m/d/Y",strtotime($dob->exactDateTime)) : "";
		if($gender == "Female")
		$genderIndex = 1;
		else
		if($gender == "Male")
		$genderIndex = 0;
		else
		$genderIndex = -1;
	}
	catch(Exception $ex) {
		error_log("failed to initialize player from health url: ".$ex->getMessage());
		die(isheader('Error adding new player',true)."<p>An error occurred while attempting to access the HealthURL you entered.</p>");
	}

	// create the player in our tables
	//echo "making healthurl and then player $fn $gn";
	$playerind = makeplayer ($base_url.$accid, $family,$given,$fmtDob,"",null,$teamind,'test');
	if ($playerind == false) {
		dbg("dupe player");
		$loc = "is.php?addplayer=add&teamind=$teamind&err=".urlencode("Duplicate Player");
	}
	else  {  // success
		// Lazy but easier than refactoring the makeplayer code
		$result = dosql("update players set oauthtoken = '$access_token' where playerind = $playerind");
		if(!$result) {
			$loc = "is.php?addplayer=add&teamind=$teamind&err=".urlencode("Failed to set authentication token");
		}
		else
		$loc ="p.php?playerind=$playerind";
	}

	dbg("redirecting to $loc");
	header ("Location: $loc");
	echo "Redirecting to $loc";
	exit;
} else
if (isset($_REQUEST['delteampost']))
{

	$team = $_REQUEST['team'];
	// might leave inaccessible players

	$teamind = get_teamind($team);
	dosql("DELETE FROM teamplayers where  teamind = '$teamind'");
	dosql("DELETE FROM leagueteams where  teamind = '$teamind'");
	dosql("DELETE from teams where teamind='$teamind' ");

	$header = isheader("Removed team $team",true);
	$markup = <<<XXX
	$header
	<div id='is_body'>
	<h5>Team $team  was removed from Informed Sports</h5>
<div class=ispanel>
<p>The associated healthurls of any players are  still viable and can be utilized again if you choose to add the player to another team at a later date</p>
</div>
<div id='is_footer'>
</div>
</div>
</body>
XXX;
	echo $markup;
	exit;
} else

if (isset($_REQUEST['addteampost']))
{
	$any=false;
	$team = $_REQUEST['team'];$teamerr='';
	$league = $_REQUEST['league'];
	$leagueind= getleagueind($league);
	$hp = $_REQUEST['homepageurl']; $hperr='';
	$sc = $_REQUEST['schedurl']; $scerr='';
	$news = $_REQUEST['newsurl']; $newserr='';
	$logo = $_REQUEST['logourl']; $logoerr='';
	// edit check all the fields
	if (strpos($team,"'")) {$teamerr = "no quotes allowed in team name"; $any=true;}
	if ($any) {

		$header = isheader('Error adding new team',true);
		$formbody = teamsetupform($league,$team, $teamerr,$hp,$hperr,$sc,$scerr,$news,$newserr,$logo,$logoerr);
		$markup = <<<XXX
		$header
		<div id='is_body'>
		<h5>please correct these errors to add a team</h5>
		<div class=ispanel>
		<form action="?" method=post>
		<input type=hidden name=addteampost value=addteampost />
		<input type=hidden name=team value=$team />
		<input type=hidden name=league value=$league />
		$formbody
</form></div></div>
<div id='is_footer'>
</div>
</body>
XXX;
		echo $markup;
		exit;
	}
	dosql("Insert into teams set name='$team',homepageurl='$hp',schedurl='$sc',newsurl='$news',logourl='$logo' ");
	$teamind = isdb_insert_id(); // get it
	dosql("Insert into leagueteams set teamind='$teamind', leagueind='$leagueind' ");

	// no errors, add new team and go to the new page
	$loc ="t.php?teamind=$teamind";
	header ("Location: $loc");
	echo "Redirecting to $loc";
	exit;
} else if (isset($_REQUEST['addplayer']))
{
	$footer=userpagefooter();
	$teamind = $_REQUEST['teamind'];
	$teamname=teamnamefromind($teamind);
	$formbody = playersetupform($teamname,'','','','','','','','','','','','','',''); // put up a blank form
	$header = isheader("Add Player to $teamname",true);
	$err = @$_REQUEST['err'];
	if($err) {
		$err="<p style='color: red;'>".htmlspecialchars($err)."</p>";
	}
	$markup = <<<XXX
	$header
	<div id='is_body'>
	<h5>Add a Player to $teamname</h5>
	<div class=ispanel>
	$err
	<form name='isform' action=is.php method=post>
	<input type=hidden name=addplayerpost value=addplayerpost />
	<input type=hidden name=teamind value=$teamind />
	$formbody
	</form>
	</div>
	<div id='is_footer'>
	$footer
</div></div>
</div>
</body>
XXX;
	echo $markup;
	exit;
}
else if (isset($_REQUEST['moveplayer']))
{
	$footer=userpagefooter();
	$teamind = $_REQUEST['teamind'];
	$teamname=teamnamefromind($teamind);
	$league = getLeague($teamname);
	$playerchooser =playerchooserquiet($teamname, ''); // get all players on team none is special

	$teamchooser =teamchooserindquiet($league->ind,'') ;// get all players on team none is special
	$header = isheader("Move Player from $teamname to another Team",true);
	$markup = <<<XXX
	$header
	<div id='is_body'>
	<h5>Move a Player from $teamname to Another Team</h5>
	<div class=ispanel>
	<form action=is.php method=post>
	<input type=hidden name=moveplayerpost value=moveplayerpost />
	<input type=hidden name=fromteam value=$teamname />
	<p>Choose a player to move from this team and a team to move to. The player's healthurl will not be affected</p>
	<table>
	<tr><td class=prompt>Move Player</td><td class=infield>$playerchooser </td><td></td></tr>
	<tr><td class=prompt>To Team</td><td class=infield>$teamchooser </td><td></td></tr>
	</table>
	<input type=submit name=submit value='Move Player' />
	</form>
	</div></div>
	<div id='is_footer'>
	$footer
	</div>
	</div>
</body>
XXX;
	echo $markup;
	exit;
}
else if (isset($_REQUEST['delplayer']))
{
	$footer=userpagefooter();
	$teamind = $_REQUEST['teamind'];
	$teamname=teamnamefromind($teamind);
	$playerchooser =playerchooserquiet($teamname, ''); // get all players on team none is special
	$header = isheader("Remove Player from $teamname",true);
	$markup = <<<XXX
	$header
	<div id='is_body'>
	<h5>Remove a Player from $teamname</h5>
	<div class=ispanel>
	<form action=is.php method=post>
	<input type=hidden name=delplayerpost value=delplayerpost />
	<input type=hidden name=team value=$teamname />
	<p>Choose a player to remove from this team. The player's healthurl will not be affected</p>
	<table>
	<tr><td class=prompt>Remove Player</td><td class=infield>$playerchooser </td><td></td></tr>
	</table>
	<input type=submit name=submit value='Remove Player' />
	</form>
	</div></div>
	<div id='is_footer'>
	$footer
	</div>
	</div>
</body>
XXX;
	echo $markup;
	exit;
}
else if (isset($_REQUEST['addleagueadmin']))
{
	if (isset($_REQUEST['role'])) $role = $_REQUEST['role']; else $role='league';
	$footer=userpagefooter();
	$leagueind = $_REQUEST['leagueind'];
	$leaguename=getleaguebyind($leagueind)->name;
	$formbody =leagueadminsetupform($leaguename, '','','','','','');
	$header = isheader("Add League Admin (Role: $role) to $leaguename",true);
	$markup = <<<XXX
	$header
	<div id='is_body'>
	<h5>Add a League Admin to $leaguename</h5>
	<div class=ispanel>
	<form action=is.php method=post>
	<input type=hidden name=addleagueadminpost value=addleagueadminrpost />
	<input type=hidden name=leagueind value=$leagueind />
	<input type=hidden name=role value=$role />
	$formbody
	</form>
	</div></div>
	<div id='is_footer'>
	$footer
</div>
</body>
XXX;
	echo $markup;
	exit;
}
else if (isset($_REQUEST['delleagueadmin']))
{
	$footer=userpagefooter();
	$leagueind = $_REQUEST['leagueind'];
	$leaguename=getleaguebyind($leagueind)->name;
	$leagueadminchooser =leagueadminchooser($leagueind, ''); // get all players on team none is special
	$header = isheader("Remove League Admin from $leaguename",true);
	$markup = <<<XXX
	$header
	<div id='is_body'>
	<h5>Remove League Admin from $leaguename</h5>
	<div class=ispanel>
	<form action=is.php method=post>
	<input type=hidden name=delleagueadminrpost value=deltrainerpost />
	<input type=hidden name=leagueind value=$leagueind />
	<p>Choose a league administrator to remove</p>
	<table>
	<tr><td class=prompt>Remove League Admin</td><td class=infield>$leagueadminchooser </td><td></td></tr>
	</table>
	<input type=submit name=submit value='Remove League Admin' />
	</form>
	</div></div>
	<div id='is_footer'>
	$footer
	</div>
	</div>
</body>
XXX;
	echo $markup;
	exit;
}
else if (isset($_REQUEST['addtrainer']))
{
	if (isset($_REQUEST['role'])) $role = $_REQUEST['role']; else $role='team';
	$footer=userpagefooter();
	$teamind = $_REQUEST['teamind'];
	$teamname=teamnamefromind($teamind);
	$formbody =trainersetupform($teamname, '','','','','','');
	$header = isheader("Add Trainer (Role: $role) to $teamname",true);
	$markup = <<<XXX
	$header
	<div id='is_body'>
	<h5>Add a Trainer to $teamname</h5>
	<div class=ispanel>
	<form action=is.php method=post>
	<input type=hidden name=addtrainerpost value=addtrainerpost />
	<input type=hidden name=teamind value=$teamind />
	<input type=hidden name=role value=$role />
	$formbody
	</form>
	</div></div>
	<div id='is_footer'>
	$footer
</div>
</body>
XXX;
	echo $markup;
	exit;
}
else if (isset($_REQUEST['deltrainer']))
{
	$footer=userpagefooter();
	$teamind = $_REQUEST['teamind'];
	$teamname=teamnamefromind($teamind);
	$trainerchooser =trainerchooser($teamname, ''); // get all players on team none is special
	$header = isheader("Remove Trainer from $teamname",true);
	$markup = <<<XXX
	$header
	<div id='is_body'>
	<h5>Remove Trainer from $teamname</h5>
	<div class=ispanel>
	<form action=is.php method=post>
	<input type=hidden name=deltrainerpost value=deltrainerpost />
	<input type=hidden name=teamind value=$teamind />
	<p>Choose a trainer to remove from this team. </p>
	<table>
	<tr><td class=prompt>Remove Trainer</td><td class=infield>$trainerchooser </td><td></td></tr>
	</table>
	<input type=submit name=submit value='Remove Trainer' />
	</form>
	</div></div>
	<div id='is_footer'>
	$footer
	</div>
	</div>
</body>
XXX;
	echo $markup;
	exit;
}
else if (isset($_REQUEST['publishteam']))
{
	$teamind = $_POST['teamind'];
	$content = mysql_real_escape_string($_POST['content']);
	dosql("Update teams set teaminfo = '$content' where teamind='$teamind' ");
	header ("Location: t.php?teamind=$teamind");
	echo "Successfully set teaminfo for $teamind";
	exit;
}
else if (isset($_REQUEST['publishleague']))
{
	$leagueind = $_POST['leagueind'];
	$content = mysql_real_escape_string($_POST['content']);
	dosql("Update leagues set leagueinfo = '$content' where ind='$leagueind' ");
	header ("Location:l.php?leagueind=$leagueind");
	echo "Successfully set leagueinfo for $leagueind";
	exit;
}

else if (isset($_REQUEST['addteam']))
{
	$footer=userpagefooter();
	$leagueind = $_REQUEST['leagueind'];
	$league= getleaguebyind($leagueind)->name;
	$formbody =teamsetupform($league,'','', '','','','','','','','');
	$header = isheader("Add Team to  $league",true);
	$markup = <<<XXX
	$header
	<div id='is_body'>
	<h5>Add a Team to $league</h5>
	<div class=ispanel>
	<form action=is.php method=post>
	<input type=hidden name=addteampost value=addteampost />
	<input type=hidden name=league value=$league />
	$formbody
	</form>
	</div></div>
	<div id='is_footer'>
	$footer
	</div></div>
</body>
XXX;
	echo $markup;
	exit;
}
else if (isset($_REQUEST['delteam']))
{
	$footer=userpagefooter();
	$leagueind = $_REQUEST['leagueind'];
	$league= getleaguebyind($leagueind)->name;
	$teamchooser=teamchooser($leagueind,'','');
	$header = isheader("Remove Team from $league",true);
	$markup = <<<XXX
	$header
	<div id='is_body'>
	<h5>Remove a Team from $league</h5>
	<div class=ispanel>
	<form action=is.php method=post>
	<input type=hidden name=delteampost value=delteampost />

	<input type=hidden name=league value=$league />
	<p>Choose a team to remove from this league. The various player's healthurl will not be affected</p>
	<table>
	<tr><td class=prompt>Remove Team</td><td class=infield>$teamchooser </td><td></td></tr>
	</table>
	<input type=submit name=submit value='Remove Team' />
	</form>
	</div>
	</div>
	<div id='is_footer'>
	$footer
	</div></div>

XXX;
	echo $markup;
	exit;
}


function envision_admin_page(){
	//
	// generate general welcome page
	//
	//
	function get_properties()
	{
		$props = array();
		$result = dosql("select * from properties");
		while ($r=mysql_fetch_array($result)) $props[$r['propertyname']]=$r;
		return $props;
	}
	function playernav1()
	{  // build the list needed for autocomplete AND a nav widget

		$r = user_record();
		if ($r===false) return false; else
		{
			switch ($r->role)
			{
				case 'is':
					{$qq ="";break;}
				case 'league':
					{$qq =" and lt.leagueind='$r->leagueind' ";break;}
				case 'team':
					{$qq =" and p.teamind='$r->teamind' ";break;}
				default :{return false;}
			}
		}
		$lastplayer=$lastteam=$lastleague='';
		$tv = <<<XXX
 {type:'Text', label:'Click to see your players', editable:false,expanded:true, children:[
XXX;
		$q ="select p.name,p.team,l.name,p.mcid,p.healthurl,p.playingstatus,p.oauthtoken,p.simtrakid,p.imageurl,t.logourl,l.logourl from players p,teams t ,leagueteams lt, leagues l
		where p.teamind = t.teamind and t.teamind=lt.teamind and l.ind=lt.leagueind and p.mcid!='' $qq
		order by l.name,p.team,p.name ";
		$result = dosql($q);
		while ($rr=mysql_fetch_array($result))
		{
			$player=$rr[0]; $team=$rr[1]; $league=$rr[2]; $mcid = $rr[3]; $healthurl = $rr[4];
			$playingstatus = ($rr[5]!='')?$rr[5]:'active';
			$oauthtoken = $rr[6]; $simtrakid=$rr[7];$imageurl = $rr[8];$timageurl = $rr[9]; $limageurl = $rr[10];
			if ($league!=$lastleague) {
				if ($lastleague!='') $tv.= "                        ]}
		                      ]}
		]},
			"; // close last if any

				if ($limageurl=='') $limageurl = "http://www.medcommons.net/images/unknown-user.png";
				$tv .= <<<XXX
				{type:'HTML',html:'<img class=lpic src=$limageurl alt="?:-(" > $league ', title:'$league is under your control', expanded:true,editable:false, children: [
XXX;
				$lastleague=$league;
				$lastteam=$lastplayer=''; // when moving to a new league, force new team
			}
			if ($team!=$lastteam) {
				if ($lastteam!='') $tv.= "]}
		]},
			"; // close last player and team if any

				if ($timageurl=='') $timageurl = "http://www.medcommons.net/images/unknown-user.png";
				$tv .= <<<XXX
				{type:'HTML',html:'<img class=tpic src=$timageurl alt="?:-(" > $team ', title:'$team is under your control', expanded:false,editable:false, children: [
XXX;
				$lastteam = $team;
				$lastplayer=''; // when moving to a new team, force display of new player
			}
			if ($player!=$lastplayer) {
				if ($lastplayer!='') $tv.= "]},
			"; // close last if any
				$activityUrl = false;
				if ($healthurl!='') {
					// Sign the health url so that the appliance will accept it
					// without challenge
					$health_url_parts = ApplianceApi::parse_health_url($healthurl);
					$api = get_appliance_api($health_url_parts[0], $oauthtoken);
					$healthurl = $api->sign($healthurl);
					$activityUrl = $health_url_parts[0]
					."/acct/cccrredir.php?accid=".$health_url_parts[1]
					."&auth=".$api->access_token->key
					."&widget=true"
					."&dest=CurrentCCRWidget.action%3Fcombined%26margin%3D10";

				}
				else
				$hurl = "no healthURL ";
				$injuryurl = "stviewer.php?admin&accid=$mcid&tab=tab_A";
//	{type:'Text',label:'$player Health Records', href:'$healthurl', target:'YAHOO\'s home page'}
				if ($imageurl=='') $imageurl = "http://www.medcommons.net/images/unknown-user.png";
				$tv .= <<<XXX
				{type:'HTML',html:'<a href="$healthurl" ><img class=ppic src=$imageurl alt="?:-(" > $player Health Records</a> ', title:'Simtrak ID: $simtrakid MCID: $mcid status: $playingstatus', editable:false, children: [

			

XXX;
				$lastplayer = $player;
			}

		}

		$tv .=<<<XXX
			]}
		]}
	]}
]}
	
XXX;
		return $tv;
	}
	function xxplayernav2()
	{  // careful - this is subtly different from nav1

		$r = user_record();
		if ($r===false) return false; else
		{
			switch ($r->role)
			{
				case 'is':
					{$qq ="";break;}
				case 'league':
					{$qq =" and lt.leagueind='$r->leagueind' ";break;}
				case 'team':
					{$qq =" and p.teamind='$r->teamind' ";break;}
				default :{return false;}
			}
		}
		$lastplayer=$lastteam=$lastleague='';//<a href="#"><small>add league</small></a>
		$tv = <<<XXX
 {type:'HTML', html:'Click to see your players ', editable:false,expanded:true, children:[
XXX;
		$q ="select p.name,p.team,l.name,p.mcid,p.healthurl,p.playingstatus,p.oauthtoken,p.simtrakid,p.imageurl,t.logourl,l.logourl from players p,teams t ,leagueteams lt, leagues l
		where p.teamind = t.teamind and t.teamind=lt.teamind and l.ind=lt.leagueind and p.mcid!='' $qq
		order by l.name,p.team,p.name ";
		$result = dosql($q);
		while ($rr=mysql_fetch_array($result))
		{
			$player=$rr[0]; $team=$rr[1]; $league=$rr[2]; $mcid = $rr[3]; $healthurl = $rr[4];
			$playingstatus = ($rr[5]!='')?$rr[5]:'active';
			$oauthtoken = $rr[6]; $simtrakid=$rr[7];$imageurl = $rr[8];$timageurl = $rr[9]; $limageurl = $rr[10];
			if ($league!=$lastleague) {
				if ($lastleague!='') $tv.= "                        }
		                      ]}
		]},
			"; // close last if any

				if ($limageurl=='') $limageurl = "http://www.medcommons.net/images/unknown-user.png";//<a href="#"><small>add team</small></a> <a href="#"><small>delete league</small></a>
				$tv .= <<<XXX
				{type:'HTML',html:'league: $league ', title:'$league is under your control', expanded:true,editable:false, children: [
XXX;
				$lastleague=$league;
				$lastteam=$lastplayer=''; // when moving to a new league, force new team
			}
			if ($team!=$lastteam) {
				if ($lastteam!='') $tv.= "}
		]},
			"; // close last player and team if any

				if ($timageurl=='') $timageurl = "http://www.medcommons.net/images/unknown-user.png";//<a href="#"><small>add player</small></a> <a href="#"><small>delete team</small></a>
				$tv .= <<<XXX
				{type:'HTML',html:'team: $team ', title:'$team is under your control', expanded:false,editable:false, children: [
XXX;
				$lastteam = $team;
				$lastplayer=''; // when moving to a new team, force display of new player
			}
			if ($player!=$lastplayer) {
				if ($lastplayer!='') $tv.= "},
			"; // close last if any
				$activityUrl = false;
				if ($healthurl!='') {
					// Sign the health url so that the appliance will accept it
					// without challenge
					$health_url_parts = ApplianceApi::parse_health_url($healthurl);
					$api = get_appliance_api($health_url_parts[0], $oauthtoken);
					$healthurl = $api->sign($healthurl);
					$activityUrl = $health_url_parts[0]
					."/acct/cccrredir.php?accid=".$health_url_parts[1]
					."&auth=".$api->access_token->key
					."&widget=true"
					."&dest=CurrentCCRWidget.action%3Fcombined%26margin%3D10";

				}
				else
				$hurl = "no healthURL ";
				$injuryurl = "envision.php?admin&accid=$mcid&tab=tab_A";

				if ($imageurl=='') $imageurl = "http://www.medcommons.net/images/unknown-user.png";//<a href=$injuryurl><small>simtrak</small></a>&nbsp;<a href="#"><small>move</small></a>&nbsp;<a href="#"><small>delete</small></a>
				$tv .= <<<XXX
				{type:'HTML',html:'<a href=$healthurl >player: $player </a> ',
				title:'Simtrak ID: $simtrakid MCID: $mcid status: $playingstatus', editable:false
	
XXX;
				$lastplayer = $player;
			}

		}

		$tv .=<<<XXX
			}
		]}
	]}
]}
	
XXX;
		return $tv;
	}
	function plslogontoo () {
		header ("Location: /acct/login.php"); // redirect to medcommons screen
		die ("<h2>Please signon to a Simtrak-enabled MedCommons Account</h2>");
	}
	//***************
	//
	//
	// Envision Admin Page starts here
	//
	//
	//***************
	global $allplayers_,$mcid_,$jsonstuff_,$playerind_,$serviceskin_;
	$props = get_properties();
	$servicename = $props ['ServiceName']['value'];

	$servicelinks = $props ['ServiceLinks']['value'];

	$serviceskin_ = $props ['ServiceCSS']['value'];

	$r = user_record();
	if ($r===false) plslogontoo(); // does not return
	$tv1_ = playernav1();

	$js = <<<XXX
	<script type='text/javascript' >
	tree1 = new YAHOO.widget.TreeView("treeView1", [
	$tv1_
	]);
	

// render it now
tree1.render();

</script>
XXX;


	// nav and admin adds tabs
	// is addministrationpage
	list($lc,$tc,$pc,$hu,$tr,$lm,$us,$al) = getstats();
	$userpagefooter = userpagefooter();
	$teamchooser = allteamchooser('');
	$fullteamchooser = fullteamchooser('');

	$leaguechooser=leaguechooser();
	$leaguechooserquiet=leaguechooserquiet();

	$appl = $GLOBALS["appliance"];
	$server = $_SERVER['SCRIPT_URI'];
	$pos = strrpos($server,'/');
	$server = substr($server,0,$pos);
	$db = $GLOBALS['DB_Database'] ;

	$playerchoiceform = playerchoiceformtoo();
	$markup = <<<XXX

	<div id='is_body'>
	<div id="adminstuff" class="yui-navset">
	<ul class="yui-nav">
	<li  ><a href='#tab_mysearch'><em>Search</em></a></li>
	<li  ><a href='#tab_import'><em>Import</em></a></li>
	<li  ><a href='#tab_export'><em>Export</em></a></li>
	<li class=selected ><a href='#tab_info'><em>$servicename</em></a></li>
	<li ><a href='#tab_leagues'><em>Leagues</em></a></li>
	<li  ><a href='#tab_teams'><em>Teams</em></a></li>
	<li  ><a href='#tab_players'><em>Players</em></a></li>
	</ul>




	<div class="yui-content">
	<div id="tab_mysearch">
	<p>Please choose a player
	$playerchoiceform
	</p>
	</div>
	<div id="tab_import">
	<h4>Import from Simtrak Mobility into Envision</h4>
	<p>
	<a href='ebfmappings.xml' target='_new' title='mapping from simtrak to teams and leagues'>team mappings</a>
	</p>
	<p>
	<form enctype="multipart/form-data" action="importzipcsv.php" method="POST">
	Please choose a Simtrak Zipped CSV file: <input name="uploaded" type="file" /><br />
	<input type="submit" size=50 value="Upload" />
	</form>
	</p>
	<h4>Import from a Saved Envision XML File into Envision</h4>

	<p>
	<form enctype="multipart/form-data" action="importxml.php" method="POST">
	Please choose an Envision XML File: <input name="uploaded" type="file" /><br />
	<input type="submit" size=50 value="Upload" />
	</form>
	</p>
	</div>

	<div id="tab_export">
	<h4>Export from MedCommons Simtrak</h4>
	<p>
	If you want to put data back into Simtrak PC format, select Zipped Simtrak CSV format.
	</p>
	<p>
	<form action="exportcsvzip.php" method="POST">
	<input type="submit" value="Download Zipped Envision CSV File" />
	</form>
	</p>
	<p>
	If you want to data mine using Business Objects or Powerbuilder,  select MySQL format.
	</p>
	<p>
	<form action="exportmysql.php" method="POST">
	<input type="submit" value="Download Envision MySQL File" />
	</form>
	</p>
	<p>
	If you want to export data to another Envision system, select XML format
	</p>
	<p>
	<form action="exportxml.php" method="POST">
	<input type="submit" value="Download Envision XML File" />
	</form>
	</p>
	</div>

	<div id="tab_info">
	<h4>Information About $servicename</h4>
	<p>This service is supporting $lc leagues, $tc teams, $pc players, $tr trainers, and $lm league managers.</p>
	<p>This service is run by $us individuals. A total of $al alerts have been generated. </p>
	<p>The service is running on $server and the database is "$db" </p>
	<p>We are currently creating new HealthURLs on $appl; $hu have been created for these players.</p>
	<br/>
	$servicelinks
	</div>
	<div id='tab_leagues'   >
	<h4>Operate on Leagues</h4>
	<p>You can add and remove league administrators for any league.</p>
	<table>
	<tr><td class=prompt><span>Add League Administrator  to</span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=addleagueadmin value=add />
	$leaguechooserquiet <input type=submit value=go name=go  />
	</form>
	</td></tr>
	<tr><td class=prompt><span>Remove League Administrator from</span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=delleagueadmin value=del />
	$leaguechooserquiet <input type=submit value=go name=go  />
	</form>
	</td></tr>
	</table>

	<p>You can publish HTML Marquee content that will be seen by the league administrators</p>
	<table>
	<tr><td class=prompt><span>Publish Content to League</span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=publishleague value=add />
	$leaguechooserquiet<br/>
	paste in the html you want to publish<br/> <textarea rows=4   cols=60 name=content ></textarea><br><input type=submit value=go name=go  />
	</form>
	</td></tr>
	</table>
	</div>
	<div id='tab_teams'    >
	<h4>Operate on Teams</h4>
	<table>
	<tr><td class=prompt><span>Add Team to</span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=addteam value=add />
	$leaguechooserquiet <input type=submit value=go name=go  />
	</form>
	</td></tr>
	<tr><td class=prompt><span>Remove Team  from</span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=delteam value=del />
	$leaguechooserquiet <input type=submit value=go name=go  />
	</form>
	</td></tr>
	</table>
	<p>You can publish an HTML Marquee to the top of any Team's Pages</p>
	<table>
	<tr><td class=prompt><span>Publish HTML Marquee to </span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=publishteam  value=add />
	$teamchooser <br/>

	paste in the html you want to publish<br/> <textarea rows=4 cols=60 name=content ></textarea><br>
	<input type=submit value=go name=go  />
	</form>
	</td></tr>

	</table>

	<p>You can add and remove trainers from any team you have access to.</p>
	<table>
	<tr><td class=prompt><span>Add Trainer to</span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=addtrainer value=add />
	$teamchooser <input type=submit value=go name=go  />
	</form>
	</td></tr>
	<tr><td class=prompt><span>Remove Trainer from</span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=deltrainer value=del />
	$fullteamchooser <input type=submit value=go name=go  />
	</form>
	</td></tr>
	</table>
	</div>
	<div id='tab_players'    >
	<h4>Operate on Players</h4>
	<p>Players do not have direct access to their records unless you create MedCommons Consents</p>
	<table>
	<tr><td class=prompt><span>Add Player to</span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=addplayer value=add />
	$teamchooser <input type=submit value=go name=go  />
	</form>
	</td><td></td></tr>
	<tr><td class=prompt><span>Remove Player from</span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=delplayer value=del />
	$fullteamchooser <input type=submit value=go name=go  />
	</form>
	</td><td></td></tr>
	<tr><td class=prompt><span>Move Player from</span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=moveplayer value=del />
	$fullteamchooser <input type=submit value=go name=go  />
	</form>
	</td><td></td></tr>
	</table>
	</div>
	</div>	
	</div>
	
	<script type="text/javascript">
	var tabView = new YAHOO.widget.TabView('adminstuff');
	</script>
	</div>
XXX;
	$nav1 = <<<XXX
	<div id="treeView1" style="background-color:white"></div>
    <div id="msg">&nbsp;</div>
XXX;



	if ($r->role=='is') $main = <<<XXX


	You have administrative privilege.

XXX;

	else
	if ($r->role=='team') {
		$theteam = teamnamefromind($r->teamind);

		$main = <<<XXX
		<p>
		You have privilege for team: $theteam.</p>

XXX;
	}
	else
	if ($r->role=='league') {
		$theleague = leaguenamefromind($r->leagueind);


		$main = <<<XXX
		<p>
		You have privilege for league: $theleague. </p>


XXX;
	}
	else  $main='';


	$pagetop = standard_toptoo(). <<<XXX
	<a class=floatright href='/acct/home.php'><img alt='' border='0' id='stamp' src='/acct/stamp.php' /></a>
	<div style='font-size:18px;'>	<div id="navcontainer">
	<h3>$servicename</h3>
	<h4>Connected. Secure. Complete.</h4>
	<ul id="navlist" class=listinlinetiny ><li><a class=menu_how href="/help.php">Help</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
	<li><a class=menu_dashboard href="/acct/home.php">Dashboard</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
	<li><a class=menu_settings href="/acct/settings.php">Settings</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
	<li ><a class=menu_nil  href="/acct/logout.php" >Logout</a></li></ul>
	</div>

	<div class="yui-content">
	<div id="tab_myplayers1">
	<p>
	$nav1
	</p>
	</div>


	


</div>

</div>


XXX;

	$pagefoot = page_foot().$js;
	// cin gere bitg wats
	$body = <<<XXX
	$pagetop

	$markup
	<p>
	<a href='m/'>MedCommons Simtrak Mobile Explorer</a> simulator
	</p>
	</div>
	$pagefoot
XXX;

	echo $body;

}

?>