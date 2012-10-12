<?php

require_once "setup.inc.php";
require_once 'OAuth.php';
require_once 'utils.inc.php';
require_once 'mc_oauth_client.php';

global $LOGIN_COOKIE_SECRET;
$LOGIN_COOKIE_SECRET = 'isS3kr1t';

function envision_page_shell($stuff)
{
	$props = get_properties();
	
	$markup = <<<XXX
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
   <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
   <head>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
	<meta name="author" content="MedCommons, Inc." />
	<meta name="description" content="Envision Simtrak Injury Tracking Service for {$props['servicename']}" />
	<meta name="keywords" content="simtrak, medcommons, personal health records,ccr, phr, privacy, patient, health, records, medical records,emergencyccr"/>
	<meta name="robots" content="noindex,nofollow"/>
	<meta name="viewport" content="width=320" />
	<title>Envision for {$props['servicename']}</title>
	<link rel="shortcut icon" href="/images/favicon.gif" type="image/gif" />
	<link media="all"	href="{$props['servicecss']}" type="text/css" rel="stylesheet" />
   </head>
   <body >
	<a href='/envision.php'' ><img src='{$props['servicelogo']}'/></a>	
	<a class=floatright href='/acct/home.php'><img alt='' border='0' id='stamp' src='/acct/stamp.php' /></a>
	$stuff
  </body>
</html>
	
XXX;
	return $markup;
}
function get_properties()
	{
		global $GRID;
		$props = array();
		$result = dosql("select * from _g_properties where grid='$GRID' ");
		$r=mysql_fetch_array($result);
		return $r;
	}
function islog($type,$mcid,$blurb)
{ global $GRID;
	$time = time();
	$ip = $_SERVER['REMOTE_ADDR'].':'.$_SERVER['REMOTE_PORT'];
	dosql("Insert into islog set grid='$GRID', time='$time',ip='$ip',id='$mcid',type='$type', url='$blurb' ");
	dbg("$mcid: $type [ $blurb ]");
}

function isdb_fetch_object($result)
{
	return mysql_fetch_object($result);
}
function isdb_fetch_array($result)
{
	return mysql_fetch_array($result);
}

function  isdb_insert_id()
{
	return mysql_insert_id();
}

function dosql($q)
{
	if (!isset($GLOBALS['db_connected']) ){
		$GLOBALS['db_connected'] = mysql_connect($GLOBALS['DB_Connection'] ,$GLOBALS['DB_User'] );
		$db = $GLOBALS['DB_Database'];
		mysql_select_db($db) or die ("can not connect to database $db ".mysql_error());
	}
	$status = mysql_query($q);
	if (!$status) die ("dosql failed $q ".mysql_error());
	return $status;
}
function clean($s)
{
	return mysql_real_escape_string(trim($s));
}

function getplayerbyind ($playerind)
{
	$result = dosql("Select * from players where playerind='$playerind' AND grid='$GRID' ");
	$r=isdb_fetch_object($result);
	return $r;
}
function get_appl_record($table,$op, $ascending,$ind)
{           $desc = ($ascending) ?'':'desc';
$result = dosql( "select * from $table where ind $op '$ind' order by ind $desc limit 1 " ); // overspecifiy for safety
$r=isdb_fetch_object($result);
return $r;
}
function getteambyname($team){
	$result = dosql ("Select * from teams where name='$team'  AND grid='$GRID' ");
	$r=isdb_fetch_object($result);
	return $r;
}
function getleagueteambyteamind($teamind){
	$result = dosql ("Select * from leagueteams where teamind='$teamind'  AND grid='$GRID' ");
	$r=isdb_fetch_object($result);
	return $r;
}
function getallusers($clause)
{
	return dosql("Select * from _g_users where $clause  AND grid='$GRID' ") ;
}
/*
 function  fetch_alerts($minprio,$playerind,$teamind) {
 $qqq= "select * from alerts where playerind='$playerind' and teamind='$teamind' and  priority >= '$minprio' order by alertind desc limit 20 ";
 return dosql($qqq);
 }
 function  fetch_query_templates($leagueind,$plugid) {
 $qqq= "select l.name,a.* from leagues l,  qtemplates a,leagueteams lt where plugid='$plugid'
 and a.teamind=lt.teamind and lt.leagueind='$leagueind'
 and l.ind=lt.leagueind order by alertind desc limit 20 ";
 return dosql($qqq);
 }
 */
function getplayerbyname($player,$team)
{	global $GRID;$player = mysql_escape_string($player);
$r = dosql("SELECT * from players where name='$player' and team='$team' AND grid='$GRID' ");
$f = isdb_fetch_object($r);
return $f;
}
function get_appl_record_ind($table,$ind)
{global $GRID;
$result = dosql("Select * from $table where ind='$ind'  AND grid='$GRID' "); // reread the record we just inserted
$r=isdb_fetch_object($result);
return $r;
}


function playernamefromind ($ind)
{global $GRID;
$result = dosql("Select * from players where playerind = '$ind' AND grid='$GRID' ");
$rr = isdb_fetch_object($result);
if ($rr==false) return false; else return array($rr->name,$rr->team);
}
function teamnamefromind ($ind)
{global $GRID;
$result = dosql("Select * from teams where teamind = '$ind' AND grid='$GRID' ");
$rr = isdb_fetch_object($result);
if ($rr==false) return false; else return $rr->name;
}
function leaguenamefromteamind ($ind)
{global $GRID;
$result = dosql("Select name from leagues l ,leagueteams lt where lt.teamind = '$ind' and l.ind=lt.leagueind  AND l.grid='$GRID' ");
$rr = isdb_fetch_object($result);
if ($rr==false) return false; else return $rr->name;
}
function leaguenamefromind ($ind)
{global $GRID;
$result = dosql("Select * from leagues where ind = '$ind' AND grid='$GRID' ");
$rr = isdb_fetch_object($result);
if ($rr==false) return false; else return $rr->name;
}
function firstplayer($team)
{global $GRID;
$teamind =get_teamind($team);
$result = dosql("SELECT playerind from teamplayers where teamind='$teamind'  AND grid='$GRID'  limit 1");
$f = isdb_fetch_object($result);
return $f->playerind;
}
function get_playerind ($player)
{global $GRID;
$player=mysql_escape_string($player);
$result = dosql("Select * from players where name='$player'  AND grid='$GRID' ");
$r = isdb_fetch_object($result);
if ($r===false) return false; return $r->playerind;
}
function get_teamind ($team)
{global $GRID;
$result = dosql("Select * from teams where name='$team'  AND grid='$GRID' ");
$r = isdb_fetch_object($result);
if ($r===false) return false; return $r->teamind;
}
function getleagueind($league)
{global $GRID;
$result = dosql("Select ind from leagues where name='$league'  AND grid='$GRID' ");
$r = isdb_fetch_object($result);
if ($r==false) return false;
return $r->ind;
}
function getLeague($team)
{global $GRID;
$result= dosql("SELECT l.logourl,l.ind,l.name,l.showpics,l.customlinks
from teams t, leagueteams lt, leagues l
where t.name='$team' and t.teamind=lt.teamind and lt.leagueind=l.ind  AND t.grid='$GRID' ");
$f = isdb_fetch_object($result);
if (!$f) return false;
return $f;
}
function getleaguebyname($leaguename)
{global $GRID;
$result = dosql("Select * from  leagues where name='$leaguename'  AND grid='$GRID' ");
$rr = isdb_fetch_object($result);
return $rr;
}
function getleaguebyind($leagueind)

{global $GRID;
$result = dosql("Select * from  leagues where ind='$leagueind' AND grid='$GRID' ");
$rr = isdb_fetch_object($result);
return $rr;
}
/*
 function plugidFromReport($report)
 {
 return intval($report/3)+1;
 }
 */
// sql fence
function javascriptstuff()
{
	$javascriptstuff = <<<XXX
<script>
function showhide(id){
if (document.getElementById){
obj = document.getElementById(id);
if (obj.style.display == "none"){
obj.style.display = "";
} else {
obj.style.display = "none";
}
}
}
</script> 
XXX;
	return $javascriptstuff;
}
function makevarname($s)
{
	return str_replace(array(' ','/','$','-'),array('_','_','_','_'),$s);
}
function testif_logged_in()
{
	if (!isset($_COOKIE['mc'])) //wld 10 sep 06 strict type checking
	return false;
	$mc = $_COOKIE['mc'];

	$accid=""; $fn=""; $ln = ""; $email = ""; $idp = ""; $auth="";
	if ($mc!='')
	{
		$accid=""; $fn=""; $ln = ""; $email = ""; $idp = ""; $auth="";
		$props = explode(',',$mc);
		for ($i=0; $i<count($props); $i++) {
			list($prop,$val)= explode('=',$props[$i]);
			switch($prop)
			{
				case 'mcid': $accid=$val; break;
				case 'fn': $fn = $val; break;
				case 'ln': $ln = $val; break;
				case 'email'; $email = $val; break;
				case 'from'; $idp = stripslashes($val); break;
				case 'auth'; $auth = $val; break;
			}
		}
	}
	return array($accid,$fn,$ln,$email,$idp,$mc,$auth);
}
function my_identity()
{
	$f = testif_logged_in();
	if ($f===false) {
		//		echo "my identity no logged in <br/>";
		return false; //('Not logged in');
	}
	else {
		list($accid,$fn,$ln,$email,$idp,$mc,$auth)=$f;
		//	echo "my identity $accid<br/>";
	}
	return  $accid;
}

function my_role()
{

	$mcid = my_identity();
	if ($mcid===false) return $false;
	$result = dosql ("Select * from _g_users where mcid='$mcid'  ");

	$r=isdb_fetch_object($result);

	if (!$r)
	return false; else

	return $r->role;
}


function user_record()
{

	$mcid = my_identity();
	if ($mcid===false) return false;
	$result = dosql ("Select * from _g_users where mcid='$mcid'  ");

	$r=isdb_fetch_object($result);

	if (!$r)
	return false; else

	return $r;
}

function redirect($url)
{
	islog('redirect to',my_identity(),$url);
	header("Location: $url");
	exit;
}

function teamchooser($leagueind,$myteam,$id)
{  // the id tag for the select should probably be changed

// returns a big select statement
$outstr = <<<XXX
<select $id name='team' title='choose another team in this league' onchange="location = 't.php?teamind='+this.options[this.selectedIndex].value;">
XXX;
//$outstr = "<select name='team'>";
$result = dosql ("SELECT t.name,t.teamind from teams t, leagueteams lt where lt.leagueind='$leagueind'  and lt.teamind = t.teamind AND t.grid='$GRID'
order by t.name");

while ($r2 = isdb_fetch_object($result))
{
	$name = $r2->name;
	//$ename = urlencode($name);
	$selected = ($name == $myteam)?' selected ':'';
	$outstr .="<option value='$r2->teamind' $selected >$name</option>
		";
}
$outstr.="</select>";
return $outstr;

}

function teamchooserind($leagueind,$id)
{
	// returns a big select statement . adds  one extra choice
	$outstr = <<<XXX
	<select  $id name='teamind' title='choose another team in this league' onchange="location = 't.php?teamind='+this.options[this.selectedIndex].value;">
	<option value='-1' >-choose team-</option>
XXX;
	$result = dosql ("SELECT t.teamind,t.name from teams t, leagueteams lt where lt.leagueind='$leagueind'  and lt.teamind = t.teamind AND t.grid='$GRID'
	order by t.name");

	while ($r2 = isdb_fetch_object($result))
	{
		$tind = $r2->teamind;
		$name = $r2->name;
		//$ename = urlencode($name);

		$outstr .="<option value='$tind' >$name</option>
		";
	}
	$outstr.="</select>";
	return $outstr;

}
function visualteamchooser($leagueind)
{

	$outstr = <<<XXX
	<div  id='leagueroster'   title='choose teams' >
XXX;
	$result = dosql ("SELECT t.teamind,t.name,t.logourl from teams t, leagueteams lt where lt.leagueind='$leagueind'  and lt.teamind = t.teamind  AND t.grid='$GRID'   order by t.name");

	while ($r2 = isdb_fetch_object($result))
	{
		$tind = $r2->teamind;
		$name = $r2->name;
		$img  = $r2->logourl;
		if ($img =='')  $img= $GLOBALS['missing_image'];
		//$ename = urlencode($name);

		$outstr .="<div class='league_member'>
		<a href='t.php?teamind=$tind' title='open team roster page for $name' class='team_image' >
		<img src='$img' width='50' alt='no image for team' /></a>
		<a href='t.php?teamind=$tind' title='open team roster page for $name' class='team_name' >$name</a>
		</div>
		";
	}
	$outstr.="
	</div>
	";
	return $outstr;

}
function teamchooserindquiet($leagueind,$id)
{global $GRID;
	// returns a big select statement . adds  one extra choice
	$outstr = <<<XXX
	<select   $id name='teamind' title='choose another team in this league' >
	"<option value='-1' >-all teams-</option>
	"
XXX;
	$result = dosql ("SELECT t.teamind,t.name from teams t, leagueteams lt where lt.leagueind='$leagueind'  and lt.teamind = t.teamind AND t.grid='$GRID'
	order by t.name");

	while ($r2 = isdb_fetch_object($result))
	{
		$tind = $r2->teamind;
		$name = $r2->name;
		//$ename = urlencode($name);

		$outstr .="<option value='$tind' >$name</option>
		";
	}
	$outstr.="</select>";
	return $outstr;

}

function leaguechooser()
{global $GRID;
// returns a big select statement

$outstr = <<<XXX
	<select name='leagueind' title='choose another league on this informed sports service' onchange="location = 'l.php?leagueind='+this.options[this.selectedIndex].value;">
XXX;

$result = dosql ("SELECT name, ind from leagues where grid='$GRID' order by name");


while ($r2=isdb_fetch_object($result))

{
	$tind = $r2->ind;
	$name = $r2->name;
	//$ename = urlencode($name);

	$outstr .="<option value='$tind' >$name</option>";
}
$outstr.="</select>";
return $outstr;

}

function leaguechooserquiet()
{global $GRID;
// returns a big select statement

$outstr = <<<XXX
	<select name='leagueind' title='choose another league '>
XXX;

$result = dosql ("SELECT name, ind from leagues where grid='$GRID' order by name");


while ($r2=isdb_fetch_object($result))

{
	$tind = $r2->ind;
	$name = $r2->name;
	//$ename = urlencode($name);

	$outstr .="<option value='$tind' >$name</option>";
}
$outstr.="</select>";
return $outstr;

}
function playerchooser($team, $player)
{global $GRID;
// returns a big select statement
$outstr = <<<XXX
<div class=playerselect> <select name='name' title='choose another player on $team' onchange="location = 'p.php?name='+this.options[this.selectedIndex].value;">
XXX;
$result = dosql ("SELECT * from players  where team = '$team'  AND grid='$GRID' ");
$eteam = urlencode ($team);
while ($r2 = isdb_fetch_object($result))
{
	$name = $r2->name;
	//$ename = urlencode($name);
	$selected = ($name == $player)?' selected ':'';
	$outstr .="<option value='$name' $selected >$name</option>
		";
}
$outstr.="</select></div>";
return $outstr;

}function playerchooserquiet($team, $player)
{global $GRID;
// returns a big select statement
$outstr = <<<XXX
<div class=playerselect> <select name='name' title='choose  player on $team' >
XXX;
$result = dosql ("SELECT * from players  where team = '$team'  AND grid='$GRID' ");
$eteam = urlencode ($team);
while ($r2 = isdb_fetch_object($result))
{
	$name = $r2->name;
	//$ename = urlencode($name);
	$selected = ($name == $player)?' selected ':'';
	$outstr .="<option value='$name' $selected >$name</option>
		";
}
$outstr.="</select></div>";
return $outstr;

}
function playerchooserind($team, $player)
{global $GRID;
// returns a big select statement
$outstr = <<<XXX
<div class=playerselect> <select  name='playerind' title='choose another player on $team' onchange="location = 'p.php?playerind='+this.options[this.selectedIndex].value;">
XXX;
$result = dosql ("SELECT * from players  where team = '$team'  AND grid='$GRID' ");

$eteam = urlencode ($team);
while ($r2 = isdb_fetch_object($result))
{

	$name = $r2->name;
	//$ename = urlencode($name);
	$selected = ($name == $player)?' selected ':'';
	$outstr .="<option value='$r2->playerind' $selected >$name</option>
		";

}
$outstr.="</select></div>";
return $outstr;

}

function clean1($s)
{

	$s = preg_split("/[\s,]+/",$s);
	return clean($s[0]);
}
function page_header($title)
{
	return str_replace(array('$$$title$$$'),array($title),file_get_contents("_header.html"));
}




function istoday($time)
{
	$now = date('Y-m-d h:i A');
	//echo "Now $now     time $time<br/>";
	return (substr($time,0,10)==substr($now,0,10));
}
function isyesterday($time)
{
	$now = time();
	$yesterday =
	date('Y-m-d h:i A',$now- (24 * 60 * 60));
	return (substr($time,0,10)==substr($yesterday,0,10));
}
function nicetime ($time)
{

	if (istoday($time)) return substr($time,11,8);
	else
	if (isyesterday($time)) return "yesterday";
	else return substr($time,5,2).'/'.substr($time,8,2).'/'.substr($time,2,2);

}

function allteamchooser($id)
{global $GRID;
// returns a big select statement
$outstr = "<select $id name='teamind'>
	";
$result = dosql ("SELECT t.name,t.teamind,l.name from teams t, leagueteams lt, leagues l where  lt.teamind = t.teamind and lt.leagueind=l.ind  AND t.grid='$GRID'
order by l.name, t.name");
$first = true;
while ($r2 = isdb_fetch_array($result))
{
	$team = $r2[0]; $teamind = $r2[1]; $league = $r2[2];
	//$ename = urlencode($name);
	$selected = ($first)?' selected ':'';
	$outstr .="<option value='$teamind' $selected >$league:$team</option>
		";
	$first = false;
}
$outstr.="</select>";
return $outstr;
}


function countof($q)
{
	$result = dosql("Select count(*) from $q  ");
	$r = isdb_fetch_array($result);
	if (!$r) return -1;
	return $r[0];
}

function getstats()
{
	global $GRID;
	$lc = countof ("leagues where  grid='$GRID'");
	$tc = countof ("teams where  grid='$GRID'");
	$pc = countof ("players where  grid='$GRID'");
	$hu = countof ("players where grid='$GRID' and healthURL!='' ");
	$tr = countof ("_g_users where  grid='$GRID' and role='team' ");
	$lm = countof ("_g_users where grid='$GRID' and role='league' ");
	$us = countof ("_g_users where  grid='$GRID' and role='is' ");
	$al = countof ("alerts where  grid='$GRID'");
	return array($lc,$tc,$pc,$hu,$tr,$lm,$us,$al);
}

/*
 function  getAllPluginInfo($leagueind)
 {
 $plugins = dosql("select * from plugins,leagueplugins where plugins.ind=leagueplugins.plugin and showonmenu='1' and leagueind='$leagueind' ");
 return $plugins;
 }
 function  getPluginInfo($id)
 {
 $plugin = dosql("select * from plugins where plugins.ind='$id' ");
 return $plugin;
 }

 function blurb ($r,$blurbdef)
 {
 $blurb = '';
 if (isset($r->Background_Info_short_case_description)) $blurb.="$r->Background_Info_short_case_description ";
 if (isset($r->Predicted_Outlook_)) $blurb.="$r->Predicted_Outlook_ ";
 $blurb = trim($blurb);	if ($blurb == '') $blurb = $blurbdef;
 return $blurb;
 }
 */

/**
 * Create and return an appliance API object initialized
 * for the given oauth access token for the given appliance.
 *
 * @param appliance - the base url of the appliance
 * @param oauth_token - <key>,<secret> of access token
 */
function get_appliance_api($appliance,$oauth_token) {
	global $PROPS;
	$token_parts = explode(",",$oauth_token);
	if(count($token_parts)!=2)
	throw new Exception("token $oauth_token is in an invalid format");
	return new ApplianceApi($PROPS['token'],$PROPS['secret'],$appliance, $token_parts[0], $token_parts[1]);
}

function get_request_token($appliance,$accid) {
	global $PROPS;
	$api = new ApplianceApi($PROPS['token'],$PROPS['secret'],$appliance);
	return $api->get_request_token($accid);
}

/**
 * Return a version of $x escaped for javascript
 */
function jsesc($x) {
	return preg_replace("/\n/","\\n",addslashes($x));
}
?>
