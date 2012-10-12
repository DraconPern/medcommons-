<?php

//
//
// handle postbacks from forms for Envision Administration
//
//


require_once "simtrak.inc.php";

// these routines are only used by these postbacks


function trainerchooser($team){ return teamroleuserchooser($team,'team') ;}
function teamroleuserchooser($team,$role)
{
	// returns a big select statement
	$teamind=get_teamind($team);

	$outstr = "<select name='name'>";
	$result = dosql ("SELECT * from _g_users  where teamind= '$teamind' and role='$role' ");


	while ($r2 = isdb_fetch_object($result))
	{

		//$ename = urlencode($name);
		$selected ='';// ($name == $player)?' selected ':'';
		$outstr .="<option value='$r2->email $r2->mcid' $selected >$r2->mcid</option>
		";

	}
	$outstr.="</select>";
	return $outstr;

}


function leagueadminchooser($leagueind){ return leagueroleuserchooser($leagueind,'league') ;}
function leagueroleuserchooser($leagueind,$role)
{


	$outstr = "<select name='name'>";
	$result = dosql ("SELECT * from _g_users  where leagueind= '$leagueind' and role='$role' ");


	while ($r2 = isdb_fetch_object($result))
	{

		//$ename = urlencode($name);
		$selected ='';// ($name == $player)?' selected ':'';
		$outstr .="<option value='$r2->email $r2->mcid' $selected >$r2->mcid</option>
		";

	}
	$outstr.="</select>";
	return $outstr;

}

function isheader($title,$priv){		return standard_top();}


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

function trainersetupform($team,$email,$emailerr,$mcid,$mciderr,$sms,$smserr){	return usersetupform('team',$team,$email,$emailerr,$mcid,$mciderr,$sms,$smserr);}
function leagueadminsetupform($team,$email,$emailerr,$mcid,$mciderr,$sms,$smserr){	return usersetupform('league',$team,$email,$emailerr,$mcid,$mciderr,$sms,$smserr);
}
function usersetupform($role,$team,$email,$emailerr,$mcid,$mciderr,$sms,$smserr)
{
	$form =<<<XXX
	<table>
	<tr><td class=prompt>role</td><td>$role</td><td></td></tr>
	<tr><td class=prompt>team</td><td>$team</td><td></td></tr>
	<tr><td class=prompt>email</td><td><input class=infield type=text name=email value='$email' /></td><td class=errfield>$emailerr</td></tr>
	<tr><td class=prompt>medcommons id</td><td><input class=infield type=text name=mcid value='$mcid' /></td><td class=errfield>$mciderr</td></tr>
	<tr><td class=prompt>sms</td><td><input class=infield type=text name=sms value='$sms' /></td><td class=errfield>$smserr</td></tr>
<tr><td></td><td></td><td></td></tr>
</table>
<input type=submit name=submit value='Setup Trainer' />
XXX;
	return $form;
}

function fullteamchooser($id)
{
	global $GRID;
	// returns a big select statement
	$outstr = "<select $id name='teamind'>";
	$result = dosql ("SELECT t.name,t.teamind,l.name from teams t, leagueteams lt, leagues l
	where  lt.teamind = t.teamind and lt.leagueind=l.ind  AND t.grid='$GRID'
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

global $GRID;

$reportchooser = '';$PROPS = get_properties();// touch datbase whether needed or not
/*
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
*/
if (isset($_REQUEST['addtrainerpost']))
{
	$any=false;
	$teamind = $_REQUEST['teamind'];
	$role = $_REQUEST['role'];
	$mcid = $_REQUEST['mcid']; $fnerr='';
	$email = $_REQUEST['email']; $gnerr='';
	$sms = $_REQUEST['sms']; $smserr='';
	$team = teamnamefromind($teamind);
	// edit check all the fields
	//if (substr($mcid,0,7)!='http://')  {$fnerr = "real mcid with http: must be specified"; $any=true;} else
	//if (strpos($mcid,"'")) {$fnerr = "no quotes allowed in mcid"; $any=true;} else
	//if (substr($mcid,strlen($mcid)-1,1)!='/') {$fnerr = "last char in mcid must be /"; $any=true;}


	if (strlen($email)<10) {$gnerr = "real email must be specified";$any=true;}

	if ($any) {
		//addplayerpost
		$header = isheader('Error adding new trainer',true);
		$formbody = usersetupform($role,$team, $email,$gnerr,$mcid,$fnerr,$sms,$smserr);
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

	$result =mysql_query("Insert into _g_users set email='$email',sms='$sms', mcid='$mcid',teamind='$teamind',role='$role' ");
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
	dosql("DELETE from _g_users where email='$name' and teamind='$teamind' and role='team' ");
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
/*
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
		list($req_token,$url)= ApplianceApi::authorize($PROPS['token'],$PROPS['secret'],$hurl,$callback);

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
	$playerind = makeplayer ('', $fn,$gn,$dob,$sex,$img,$teamind,'test');
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
		$api = ApplianceApi::confirm_authorization($PROPS['token'],$PROPS['secret'],$oauth[0], $oauth[1],$hurl);

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
/*
 if (isset($_REQUEST['delteampost']))
 {

 $team = $_REQUEST['team'];
 // might leave inaccessible players

 $teamind = get_teamind($team);
 dosql("DELETE FROM teamplayers where  teamind = '$teamind' and grid='$GRID'");
 dosql("DELETE FROM leagueteams where  teamind = '$teamind' and grid='$GRID'");
 dosql("DELETE from teams where teamind='$teamind' and grid='$GRID' ");

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
 dosql("Insert into teams set grid='$GRID',name='$team', leagueind='$leagueind',homepageurl='$hp',schedurl='$sc',newsurl='$news',logourl='$logo' ");
 $teamind = isdb_insert_id(); // get it
 dosql("Insert into leagueteams set grid='$GRID', teamind='$teamind', leagueind='$leagueind' ");

 // no errors, add new team and go to the new page
 $loc ="t.php?teamind=$teamind";
 header ("Location: $loc");
 echo "Redirecting to $loc";
 exit;
 } else
 */
/*
 *
 if (isset($_REQUEST['addplayer']))
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
 else
 */
/*if (isset($_REQUEST['moveplayer']))
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
 else

 if (isset($_REQUEST['delplayer']))
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
 else
 */
if (isset($_REQUEST['addleagueadmin']))
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
	header ("Location: envsion.php");
	//echo "Successfully set teaminfo for $teamind";
	exit;
}
else if (isset($_REQUEST['publishleague']))
{
	$leagueind = $_POST['leagueind'];
	$content = mysql_real_escape_string($_POST['content']);
	dosql("Update leagues set leagueinfo = '$content' where ind='$leagueind' ");
	header ("Location: envision.php");
	//echo "Successfully set leagueinfo for $leagueind";
	exit;
}
/*
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
 */



function envision_admin_page(){
	//
	// generate general welcome page
	//
	/*
	<tr><td class=prompt><span>Add Player to</span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=addplayer value=add />
	$teamchooser <input type=submit value=go name=go  />
	</form>
	</td><td></td></tr>
	*/
	//

	function playernav1()
	{  // build the list needed for autocomplete AND a nav widget
		global $GRID;
		$r = user_record();
		if ($r===false) return false; else
		{

			$GRID = $r->grid;
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
 {type:'Text', label:'Click to view your players ', editable:false,expanded:true, children:[
XXX;
		$q ="select p.name,p.team,l.name,p.mcid,p.healthurl,p.status,p.oauthtoken,p.simtrakid,p.imageurl,t.logourl,l.logourl,t.teaminfo,l.leagueinfo from players p,teams t ,leagueteams lt, leagues l
		where p.teamind = t.teamind and t.teamind=lt.teamind and l.ind=lt.leagueind and p.mcid!='' and t.grid='$GRID' $qq
		order by l.name,p.team,p.name ";
		$result = dosql($q);
		while ($rr=mysql_fetch_array($result))
		{
			$player=$rr[0]; $team=$rr[1]; $league=$rr[2]; $mcid = $rr[3]; $healthurl = $rr[4];
			$playingstatus = ($rr[5]!='')?$rr[5]:'active';
			$oauthtoken = $rr[6]; $simtrakid=$rr[7];$imageurl = $rr[8];$timageurl = $rr[9]; $limageurl = $rr[10]; $tinfo = $rr[11]; $linfo=$rr[12];
			if ($league!=$lastleague) {
				if ($lastleague!='') $tv.= "                        ]}
		                      ]}
		]},
			"; // close last if any

				if ($limageurl=='') $limageurl = "http://www.medcommons.net/images/unknown-user.png";
				$tv .= <<<XXX
				{type:'HTML',html:'<img class=lpic src=$limageurl alt="?:-(" > $league $linfo', title:'$league is under your control', expanded:true,editable:false, children: [
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
				{type:'HTML',html:'<img class=tpic src=$timageurl alt="?:-(" > $team $tinfo', title:'$team is under your control', expanded:false,editable:false, children: [
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
				{type:'HTML',html:'<a href="$healthurl" ><img class=ppic src=$imageurl alt="?:-(" > $player Health Records</a> 				<small><a href="moveplayer.php?id=$mcid" >[move player to another team]</a>&nbsp;			<a href="stviewer.php?accid=$mcid" >[test viewer]</a>&nbsp;				<a href="deleteplayer.php?id=$mcid" >[delete player from envision]</a></small> ', title:'Simtrak ID: $simtrakid MCID: $mcid status: $playingstatus', editable:false, children: [

			

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
	//<small>	<a href="moveplayer.php?id=$mcid" >move player to another team</a>&nbsp;<a href="deleteplayer.php?id=$mcid" >delete player</a></small>
	function plslogontoo () {
		header ("Location: /acct/login.php?next=http://simtrak.medcommons.net/envision.php"); // redirect to medcommons screen
		die ("<h2>Please signon to a Simtrak-enabled MedCommons Account</h2>");
	}
	//***************
	//
	//
	// Envision Admin Page starts here
	//
	//
	//***************
	global $GRID, $PROPS, $allplayers_,$mcid_,$jsonstuff_,$playerind_,$serviceskin_;
	$r = user_record();
	if ($r===false) plslogontoo(); // does not return
	$GRID = $r->grid;
	$PROPS = get_properties();
	$servicename = $PROPS ['servicename'];
	$serviceskin_ = $PROPS ['servicecss'];
	$servicelinks = $PROPS ['servicelinks'];
	$appl = $PROPS ['appliance'];

	$r = user_record();
	if ($r===false) plslogontoo(); // does not return
	$GRID = $r->grid;
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

	$server = $_SERVER['SCRIPT_URI'];
	$pos = strrpos($server,'/');
	$server = substr($server,0,$pos);
	$db = $GLOBALS['DB_Database'] ;

	$playerchoiceform = playerchoiceform();
	$markup = <<<XXX

	<div id='is_body'>
	<div id="adminstuff" class="yui-navset">
	<ul class="yui-nav">
	<li  ><a href='#tab_mysearch'><em>ATCs</em></a></li>
	<li  ><a href='#tab_import'><em>Import</em></a></li>
	<li  ><a href='#tab_export'><em>Export</em></a></li>
	<li class=selected ><a href='#tab_info'><em>$servicename</em></a></li>
	<li ><a href='#tab_leagues'><em>Leagues</em></a></li>
	<li  ><a href='#tab_teams'><em>Teams</em></a></li>
	<li  ><a href='#tab_players'><em>Players</em></a></li>
	</ul>




	<div class="yui-content">
	<div id="tab_mysearch">
	
	<p>You can add and remove ATCs from any team you have access to.</p>
	<table>
	<tr><td class=prompt><span>Add ATC to</span> </td><td class=infield>
	<form method=post action='addtrainer.php'>
	<input type=hidden name=addtrainer value=add />
	$teamchooser <input type=submit value=go name=go  />
	</form>
	</td></tr>
	<tr><td class=prompt><span>Remove ATC from</span> </td><td class=infield>
	<form method=post action='deletetrainer.php'>
	<input type=hidden name=deltrainer value=del />
	$fullteamchooser <input type=submit value=go name=go  />
	</form>
	</td></tr>
	</table>
	<hr/>
		<p>You can add and remove league administrators for any league in $servicename.</p>
	<table>
	<tr><td class=prompt><span>Add League Administrator  to</span> </td><td class=infield>
	<form method=post action='addleagueadmin.php'>
	<input type=hidden name=addleagueadmin value=add />
	$leaguechooserquiet <input type=submit value=go name=go  />
	</form>
	</td></tr>
	<tr><td class=prompt><span>Remove League Administrator from</span> </td><td class=infield>
	<form method=post action='deleteleagueadmin.php'>
	<input type=hidden name=delleagueadmin value=del />
	$leaguechooserquiet <input type=submit value=go name=go  />
	</form>
	</td></tr>
	</table>
		<hr/>
		<p>You can add and remove super-administrators for $servicename</p>
	<table>
	<tr><td class=prompt><span>Add Super Administrator </span> </td><td class=infield>
	<form method=post action='addsuperadmin.php'>
	<input type=hidden name=addsuperadmin value=add />
	<input type=submit value=go name=go  />
	</form>
	</td></tr>
	<tr><td class=prompt><span>Remove Super Administrator </span> </td><td class=infield>
	<form method=post action='deletesuperadmin.php'>
	<input type=hidden name=delsuperadmin value=del />
<input type=submit value=go name=go  />
	</form>
	</td></tr>
	</table>
	</div>
	<div id="tab_import">
	<h4>Import from Simtrak Mobility into Envision - $servicename</h4>
	<p>
	<a href='Envision-DataKit/emptydb.php'  title='clean out old data'>empty db</a>
	</p>
	<p>
	<form enctype="multipart/form-data" action="Envision-DataKit/importzipcsv.php" method="POST">
	Please choose a Simtrak Zipped CSV file: <input name="uploaded" type="file" /><br />
	<input type="submit" size=50 value="Upload" />
	</form>
	</p>
	<h4>Import from a Saved Envision XML File into Envision - $servicename</h4>

	<p>
	<form enctype="multipart/form-data" action="Envision-DataKit/importxml.php" method="POST">
	Please choose an Envision XML File: <input name="uploaded" type="file" /><br />
	<input type="submit" size=50 value="Upload" />
	</form>
	</p>
	</div>

	<div id="tab_export">
	<h4>Export from Envision - $servicename</h4>
	<p>
	If you want to put data back into Simtrak PC format, select Zipped Simtrak CSV format.
	</p>
	<p>
	<form action="Envision-DataKit/exportcsvzip.php" method="POST">
	<input type="submit" value="Download Zipped Envision CSV File" />
	</form>
	</p>
	<p>
	If you want to data mine using Business Objects or Powerbuilder,  select MySQL format.
	</p>
	<p>
	<form action="Envision-DataKit/exportmysql.php" method="POST">
	<input type="submit" value="Download Envision MySQL File" />
	</form>
	</p>
	<p>
	If you want to export data to another Envision system, select XML format
	</p>
	<p>
	<form action="Envision-DataKit/exportxml.php" method="POST">
	<input type="submit" value="Download Envision XML File" />
	</form>
	</p>
	</div>

	<div id="tab_info">
	<h4>Information About $servicename</h4>
	<p>This service is supporting $lc leagues, $tc teams, $pc players, $tr trainers, and $lm league managers.</p>
	<p>This service is run by $us individuals. A total of $al alerts have been generated. </p>
	<p>The service is running on $server and the database is "$db" the grid is $GRID </p>
	<p>We are currently creating new HealthURLs on $appl; $hu have been created for these players.</p>
	<br/>
	$servicelinks
	</div>
	<div id='tab_leagues'   >

	<table>
	<tr><td class=prompt><span>Add League to $servicename </span> </td><td class=infield>
	<form method=post action='addleague.php'>
	<input type=hidden name=addleague value=add />
	<input type=submit value=go name=go  />
	</form>
	</td></tr>
	<tr><td class=prompt><span>Remove League from $servicename </span> </td><td class=infield>
	<form method=post action='deleteleague.php'>
	<input type=hidden name=delleague value=del />
	$leaguechooserquiet<input type=submit value=go name=go  />
	</form>
	</td></tr>
	</table>
	<hr/>
	
	
	
	<p>You can publish content that will be shown alongside the league logos</p>
	<table>
	<tr><td class=prompt><span>Publish Content to League </span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=publishleague value=add />
	$leaguechooserquiet<br/>
	paste <br/> <textarea rows=4   cols=60 name=content ></textarea><br><input type=submit value=go name=go  />
	</form>
	</td></tr>
	</table>
	</div>
	<div id='tab_teams'    >

	<table>
	<tr><td class=prompt><span>Add Team to $servicename League </span> </td><td class=infield>
	<form method=post action='addteam.php'>
	<input type=hidden name=addteam value=add />
	$leaguechooserquiet <input type=submit value=go name=go  />
	</form>
	</td></tr>
	<tr><td class=prompt><span>Remove Team </span> </td><td class=infield>
	<form method=post action='deleteteam.php'>
	<input type=hidden name=delteam value=del />
	$teamchooser <input type=submit value=go name=go  />
	</form>
	</td></tr>
	</table>
	<hr/>
	<p>You can publish content that will be show alongside the team logos</p>
	<table>
	<tr><td class=prompt><span>Publish Content to team </span> </td><td class=infield>
	<form method=post action='envision.php'>
	<input type=hidden name=publishteam  value=add />
	$teamchooser <br/>
	paste <br/> <textarea rows=4 cols=60 name=content ></textarea><br>
	<input type=submit value=go name=go  />
	</form>
	</td></tr>

	</table>

	</div>
	<div id='tab_players'    >

	<div id="treeView1" style="background-color:white"></div>
	<div id="msg">&nbsp;</div>

	<p>Players are only created via the Import functions. Players do not have direct access to their records unless you create MedCommons Consents</p>


	</div>
	</div>
	</div>
	<script type="text/javascript">
	var tabView = new YAHOO.widget.TabView('adminstuff');
	</script>
	</div>
</div>
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


	$pagetop = standard_top($servicename.'- SimTrak Envision - Powered by MedCommons'). <<<XXX

	<a class=floatright href='/acct/home.php'><img alt='' border='0' id='stamp' src='/acct/stamp.php' /></a>
</div>
<div id="navcontainer">
	<ul id="navlist" class=listinlinetiny ><li><a class=menu_how href="/help.php">Help</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
	<li><a class=menu_dashboard href="/acct/home.php">Dashboard</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
	<li><a class=menu_settings href="/acct/settings.php">Settings</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
	<li ><a class=menu_nil  href="/acct/logout.php" >Logout</a></li></ul>
	</div>



XXX;

	$pagefoot = page_foot().$js;
	// cin gere bitg wats
	$body = <<<XXX
	$pagetop

	$markup

	$pagefoot
XXX;

	echo $body;

}

?>