<?php

require_once 'utils.inc.php';
require_once '../setup.inc.php';
function thispage($stuff)
{
	$pm = player_manifest();

	$top = <<<XXX
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>MedCommons Simtrak</title>
<meta name="viewport" content="width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;"/>
<style type="text/css" media="screen">@import "../css/iphonenav.css";</style>
<style type="text/css" >
 p
 {
    margin: 0;
    border-bottom: 1px solid #E0E0E0;
    padding: 10px;
    font-size: 20px;
    font-weight: bold;
    list-style: none;
}
input, select {
    box-sizing: border-box;
    width: 100%;
    margin: 6px 0 0 0;
    padding: 6px 6px 6px 44px;
    font-size: 16px;
    font-weight: normal;
}

.phonebox {
	border: 2px solid #515151;
	background-color: #515151;
	font-size: 12px;
	color: white;
	padding-bottom: 10px;
	height: 640px;
}
.phonebox h3 {padding-left: 15px; font-size:14px; font-family:Tahoma;}
.phonebox .clump {
	padding: 5px;
	margin: 10px;
}
#xsubmit {margin-left: 15px; font-size: 12px; }
.phonebox select {

	margin: 0;
	padding: 0;
}

.smallteam {padding-left:30px; font-size:.7em; font-weight:300}
</style>
<script type="application/x-javascript" src="iphonenav.js"></script>
</head>

<body>
    <h1 id="pageTitle"></h1>
    <a id="homeButton" class="button" href="#players">Players</a>

    <a class="button" href="#phoneform">Modify</a>
    

XXX;

	$end = <<<XXX
	<div id="player" class="panel" title="In Progress">
	<h2>If this were working you'd be seeing something...</h2>
	</div>
	<div id="searchResults" class="panel" title="Modify">
	<h2>The Simtrak database has been modified...</h2>
	</div>
	<form id="phoneform" name=phoneform class="dialog" action="index.php" method=post>
	<fieldset>
	<h1>Modify</h1>
	<a class="button toolButton goButton" href="#searchResults">Modify</a>
	</fieldset>

	$stuff

    </form>
    
<script type="text/javascript">
function build_confirm(op,mcid,bodyPart,injury,medication)
{
var x =  'Are you sure you want to '+document.getElementById(op).value+' re: player '+document.getElementById(mcid).value+ ' '
+document.getElementById(bodyPart).value+' '+document.getElementById(injury).value+' '+document.getElementById(medication).value+'?';
return x;
}

function formSubmit()
{

document.phoneform.submit();
return true;
}
</script>
</body>
</html>

XXX;
	return $top.$pm.$end;

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
function user_record()
{

	$openid = my_identity();
	if ($openid===false) return false;
	$result = dosql ("Select * from users where openid='$openid'  ");

	$r=mysql_fetch_object($result);

	if (!$r)
	return false; else

	return $r;
}

function plslogon () {
	die ("First, please signon to a Simtrak-enabled MedCommons Account");
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
		return ('Not logged in');
	}
	else {
		list($accid,$fn,$ln,$email,$idp,$mc,$auth)=$f;
		//	echo "my identity $accid<br/>";
	}
	return  $accid;
}

function my_role()
{

	$openid = my_identity();
	if ($openid===false) return $false;
	$result = dosql ("Select * from users where openid='$openid'  ");

	$r=mysql_fetch_object($result);

	if (!$r)
	return false; else

	return $r->role;
}

//
function select_field_values ($dbtable,$field)
{
	$dict = array();$last='';
	$result = dosql("Select distinct $field from $dbtable order by $field");
	while ($r=mysql_fetch_array($result))
	{	$trim = trim($r[0]);
	$trim = substr($trim,0,30);

	if ($trim!='')
	{
		if ($trim !=$last)
		{ $dict [$trim] = $trim; $last=$trim;}
	}
	}
	return $dict;
}
function islog($type,$openid,$blurb)
{
	$time = time();
	$ip = $_SERVER['REMOTE_ADDR'].':'.$_SERVER['REMOTE_PORT'];
	dosql("Insert into islog set time='$time',ip='$ip',id='$openid',type='$type', url='$blurb' ");
}
function buildselect ($name,$r,$default)
{
	$buf = "
	<select id = '$name' name='$name' >

	<option value=''>$default</option>
	";
	foreach ($r as $key=>$value)
	{
		$buf .="
		<option value='$key'>$value</option>";
	}
	$buf .= "
	</select>
	";
	return $buf;
}
function demog($simtrakid,$team)
{
	$buf = '';

	$result = dosql ("select * from PERSON where personid='$simtrakid' ");
	$r=mysql_fetch_array($result);
	if ($r===false) die("could not get person with simtrakid $simtrakid");

	$buf.="<p><small>name</small> {$r['FNAME']}&nbsp;{$r['MI']}&nbsp;{$r['LNAME']} <small>team</small> {$team}";
	if ($r['HEIGHT']!='') $buf.=" <small>height</small> {$r['HEIGHT']}";
	if ($r['WEIGHT']!='') $buf.=" <small>weight</small> {$r['WEIGHT']}</p>";
	$buf.="<p><small>sport</small> {$r['SPORT1']}";
	$buf.=" <small>position</small> {$r['POSITION1']}</p>";
			
	if ($r['PEMAIL']!='') $buf.=" <p><small>email</small> {$r['PEMAIL']}</p>";
	
	if ($r['PHPHONE']!='') $buf.="<p> <small>home phone</small> {$r['PHPHONE']}</p>";
	
	if ($r['PCELL']!='') $buf.="<p> <small>cell phone</small> {$r['PCELL']}</p>";
	
	if ($r['ALLERGIES']!='') $buf.="<p> <small>allergies</small> {$r['ALLERGIES']}</p>";
	
	if ($r['MEDALLERGY']!='') $buf.="<p> <small>med allergies</small> {$r['MEDALLERGY']}</p>";
	
	if ($r['SRSURGERY']!='') $buf.="<p> <small>sr surgeries</small> {$r['SRSURGERY']}</p>";
	
	if ($r['SRMEDICAL']!='') $buf.="<p> <small>sr medical</small> {$r['SRMEDICAL']}</p>";
	return $buf;
}
function injuries($simtrakid,$team)
{
	$buf = $bufz = '';

	
	$result = dosql ("select * from PERSON where personid='$simtrakid' ");
	$r=mysql_fetch_array($result);
	if ($r===false) die("could not get person with simtrakid $simtrakid");
	$buf.="<p><small>name</small> {$r['FNAME']}&nbsp;{$r['MI']}&nbsp;{$r['LNAME']} <small>team</small> {$team}";
	if ($r['HEIGHT']!='') $buf.=" <small>height</small> {$r['HEIGHT']}";
	if ($r['WEIGHT']!='') $buf.=" <small>weight</small> {$r['WEIGHT']}</p>";
	$buf.="<p><small>sport</small> {$r['SPORT1']}";
	$buf.=" <small>position</small> {$r['POSITION1']}</p>";
		 $bufcopy = str_pad($buf,strlen($buf)); //copy thusfar
	$result = dosql ("select * from INJURY where personid='$simtrakid' ");
	while ($r=mysql_fetch_array($result))
	{
		if ($r['INJURYDATE']!='') if ($r['INJURYDATE']!=' / /    ')
		$buf .=<<<XXX
	 <li><a href="#injury-{$r['INJURYID']} ">{$r['INJURYDATE']} {$r['INJURY']} {$r['BODYPART']} {$r['MEDICATION']}</a></li>
XXX;

		$bufz .=<<<XXX
	<ul id="injury-{$r['INJURYID']}" title="INJURY">
	$bufcopy 
	 <p><small>incident</small> {$r['INJURYDATE']} {$r['INJURY']} {$r['BODYPART']} {$r['MEDICATION']}</p>
	 
	 <p><small>missed</small> {$r['DAYMISSED']}</p>
XXX;
if ($r['COMMENT']!='') $bufz.=" <p><small>comment</small> {$r['COMMENT']}</p>";

if ($r['SUBJECTIVE']!='') $bufz.=" <p><small>subjective</small> {$r['SUBJECTIVE']}</p>";

if ($r['OBJECTIVE']!='') $bufz.=" <p><small>objective</small> {$r['OBJECTIVE']}</p>";

if ($r['ASSESSMENT']!='') $bufz.=" <p><small>assement</small> {$r['ASSESSMENT']}</p>";

if ($r['PLAN']!='') $bufz.=" <p><small>plan</small> {$r['PLAN']}</p>";
$bufz .=<<<XXX
	 </ul>	 
XXX;
		}
	$buf .='</ul>';
	return $buf.$bufz;
}
function treatments($simtrakid,$team)
{
	$buf = $bufz = '';
	$result = dosql ("select * from PERSON where personid='$simtrakid' ");
	$r=mysql_fetch_array($result);
	if ($r===false) die("could not get person with simtrakid $simtrakid");
	$buf.="<p><small>name</small> {$r['FNAME']}&nbsp;{$r['MI']}&nbsp;{$r['LNAME']} <small>team</small> {$team}";
	if ($r['HEIGHT']!='') $buf.=" <small>height</small> {$r['HEIGHT']}";
	if ($r['WEIGHT']!='') $buf.=" <small>weight</small> {$r['WEIGHT']}</p>";
	$buf.="<p><small>sport</small> {$r['SPORT1']}";
	$buf.=" <small>position</small> {$r['POSITION1']}</p>";
		 // prepare secondary pages
	 $bufcopy = str_pad($buf,strlen($buf)); //copy thusfar
	$result = dosql ("select * from TRTMNT where personid='$simtrakid' ");
	while ($r=mysql_fetch_array($result))
	{
		if ($r['TREATDATE']!='') if ($r['TREATDATE']!=' / /    ')
		$buf .=<<<XXX
	 <li><a href="#treatment-{$r['TREATMNTID']}">{$r['TREATDATE']} {$r['INJURY']} {$r['BODYPART']} {$r['MEDICATION']}</a></li>
	 
XXX;
	$bufz .=<<<XXX
	<ul id="treatment-{$r['TREATMNTID']}" title="TREATMENT">
	$bufcopy 
	 <p><small>incident</small> {$r['TREATDATE']} {$r['INJURY']} {$r['BODYPART']} {$r['MEDICATION']}</p>
	 
	 <p><small>missed</small> {$r['MISSEDDAY']}</p>
XXX;
if ($r['PROGRAM']!='') $bufz.=" <small>program</small> {$r['PROGRAM']}";
if ($r['SUBJECTIVE']!='') $bufz.=" <p><small>subjective</small> {$r['SUBJECTIVE']}</p>";

if ($r['OBJECTIVE']!='') $bufz.=" <p><small>objective</small> {$r['OBJECTIVE']}</p>";

if ($r['ASSESSMENT']!='') $bufz.=" <p><small>assement</small> {$r['ASSESSMENT']}</p>";

if ($r['PLAN']!='') $bufz.=" <p><small>plan</small> {$r['PLAN']}</p>";


if ($r['ACTIVE']!='') $bufz.=" <p><small>active</small> {$r['ACTIVE']}</p>";


if ($r['NONACTIVE']!='') $bufz.=" <p><small>nonactive</small> {$r['NONACTIVE']}</p>";
$bufz .=<<<XXX
	 </ul>	 
XXX;

	}
	$buf .='</ul>';
	return $buf.$bufz;
}
function weights($simtrakid,$team)
{
	$buf = '';

	$result = dosql ("select * from PERSON where personid='$simtrakid' ");
	$r=mysql_fetch_array($result);
	if ($r===false) die("could not get person with simtrakid $simtrakid");
	$buf.="<p><small>name</small> {$r['FNAME']}&nbsp;{$r['MI']}&nbsp;{$r['LNAME']} <small>team</small> {$team}";
	if ($r['HEIGHT']!='') $buf.=" <small>height</small> {$r['HEIGHT']}";
	if ($r['WEIGHT']!='') $buf.=" <small>weight</small> {$r['WEIGHT']}</p>";
	$buf.="<p><small>sport</small> {$r['SPORT1']}";
	$buf.=" <small>position</small> {$r['POSITION1']}</p>";
	
	$result = dosql ("select * from WEIGHT where personid='$simtrakid' ");
	while ($r=mysql_fetch_array($result))
	{
		
		if ($r['WEIGHTDATE']!='') if ($r['WEIGHTDATE']!=' / /    ')
		$buf .=<<<XXX
	 <p>{$r['WEIGHTDATE']} {$r['WEIGHT']} {$r['BODYFAT']}</p>
XXX;
	}
	$buf .='</ul>';
	return $buf;
}
function player_manifest()
{  // build the list needed for selecting a player


$r = user_record();
if ($r===false) plslogon(); // does not return else

switch ($r->role)
{
	case 'is':
		{$q ="select p.name,p.team,p.mcid,p.playerind,p.simtrakid from players p where p.mcid!=''
		order by p.team,p.name ";break;}
	case 'league':
		{$q ="select p.name,p.team,p.mcid,p.playerind,p.simtrakid   from players p,teams t ,leagueteams l where p.teamind = t.teamind and t.teamind=l.teamind and l.leagueind='$r->leagueind' and p.mcid!=''
		order by  p.team,p.name ";break;}
	case 'team':
		{$q ="select p.name,p.team,p.mcid,p.playerind,p.simtrakid   from players p where p.teamind='$r->teamind' and p.mcid!='' 
		order by p.team,p.name ";break;}
	default :{return false;}
}
//	echo "<p>$q</p>";
$buf='<ul id="players" title="Players" selected="true"> '; $bufa=$bufb=$bufc=$bufd=$bufe='';
$result = dosql($q);
while ($rr = mysql_fetch_object($result))
{
	$buf .=<<<XXX
	<li><a title='mcid $rr->mcid' href="#player{$rr->playerind}">$rr->name&nbsp;&nbsp;&nbsp; <span class=smallteam>$rr->team</span></a></li>

XXX;
    $demo = demog($rr->simtrakid,$rr->team);
	$bufa .= <<<XXX

	<ul id="player{$rr->playerind}" title="$rr->name">
	$demo
	<li><a href="#{$rr->playerind}-injrys">Injuries</a></li>
	<li><a href="#{$rr->playerind}-trtmnts">Treatments</a></li>
	<li><a href="#{$rr->playerind}-weights">Weight</a></li>
 </ul>

XXX;
	

	$injur = injuries($rr->simtrakid,$rr->team);
	$bufc .=<<<XXX
	<ul id="{$rr->playerind}-injrys" title="$rr->name- Injuries">
	$injur

XXX;
	$trtmnt = treatments($rr->simtrakid,$rr->team);
	$bufd .=<<<XXX
	<ul id="{$rr->playerind}-trtmnts" title="$rr->name- Treatments">
	$trtmnt

XXX;
	$weight = weights($rr->simtrakid,$rr->team);
	$bufe .=<<<XXX
	<ul id="{$rr->playerind}-weights" title="$rr->name- Weights">
	$weight

XXX;
}

$buf.='</ul>
';
return $buf.$bufa.$bufb.$bufc.$bufd.$bufe;
}
function myplayers()
{  // build the list needed for selecting a player


$r = user_record();
if ($r===false) plslogon(); // does not return else

switch ($r->role)
{
	case 'is':
		{$q ="select p.name,p.team,p.mcid from players p where p.mcid!=''";break;}
	case 'league':
		{$q ="select p.name,p.team,p.mcid  from players p,teams t ,leagueteams l where p.teamind = t.teamind and t.teamind=l.teamind and l.leagueind='$r->leagueind' and p.mcid!=''";break;}
	case 'team':
		{$q ="select p.name,p.team,p.mcid from players p where p.teamind='$r->teamind' and p.mcid!=''";break;}
	default :{return false;}
}
//	echo "<p>$q</p>";
$buf="<select id='mcid' name='mcid' >";
$result = dosql($q);
while ($rr = mysql_fetch_object($result))
{
	$buf .=<<<XXX
	<option value='$rr->mcid'>$rr->name</option>
XXX;
}
$buf.='</select>
';
return $buf;
}
function command_window()
{
	$allplayers = myplayers();
	$p0 = <<<XXX
	<div class=clump>
	<span class=oneline>
	$allplayers
	<select id='op' name="op">
	<option value="add an injury">Add Injury</option>
	<option value="add a treatment">Add Treatment</option>
	<option value="add a weight">Add Weight</option>
</select></span>
</div>
XXX;

	$r1 = select_field_values('TRTMNT','BODYPART');
	$r2 = select_field_values('INJURY','INJURY');
	$r3 = select_field_values ('TRTMNT','MEDICATION');
	$p1 = '<div class=clump><span class=oneline>'.buildselect('bodyPart',$r1,'-- unspecified bodypart --').'
</span></div>
';
	$p2 = '<div class=clump><span class=oneline>'.buildselect('injury',$r2,'-- unspecified injury --').'
</span></div>
';
	$p3 = '<div class=clump><span class=oneline>'.buildselect('medication',$r3,'-- unspecified medication --').'
</span></div>
';
	$tail = <<<XXX
<input type=submit name=xsubmit id=xsubmit value=submit onclick="if (confirm(build_confirm('op','mcid','bodyPart','injury','medication') )) return formSubmit();  else return false;" />
XXX;
	return $p0.$p1.$p2.$p3.$tail;
}
function error ($e)
{
	$tail = <<<XXX
<input type=submit name=err id=err value=Continue />
</form>
</div>
</body>
</html>
XXX;
	echo "<h3>Error</h3> $e".$tail;
	exit;
}
function command_handler()
{
	$tail = <<<XXX
<input type=submit name=xsubmit id=xsubmit value=Ok />
</div>
XXX;

	$buf =  "<h3>Request</h3>";
	$buf .= "<div class=results>".$_POST['op']." - ".$_POST['mcid'];
	if (isset($_POST['bodyPart'])&&($_POST['bodyPart']!=='')) $buf .= "<br/>".$_POST['bodyPart'];
	if (isset($_POST['injury'])&&($_POST['injury']!=='')) $buf .= "<br/>".$_POST['injury'];
	if(isset($_POST['medication'])&&($_POST['medication']!==''))$buf .=  "<br/>".$_POST['medication'];
	$buf .= "<hr/>";
	// analyze request
	$mcid = $_POST['mcid'];
	$result = dosql("Select simtrakid from players where mcid ='$mcid' ");
	if (!$result) die ("cant find player with mcid $mcid");
	$r = mysql_fetch_array($result);
	$simtrakid = $r[0];
	switch ($_POST['op'])
	{	//edit check based on args
		case "add an injury": { if (!isset($_POST['injury'])||$_POST['injury']=='') error("Must choose a specific Injury"); else
		if (!isset($_POST['bodyPart'])||$_POST['bodyPart']=='') error("Must choose a specific body part");

		// add injury bodypart {medication}
		$injury = clean($_POST['injury']);
		$bodyPart = clean($_POST['bodyPart']);
		if (isset($_POST['medication']) )$medication = clean($_POST['medication']); else $medication='';

		$date=strftime('%D');

		$q=  ("Insert into INJURY set PERSONID='$simtrakid', INJURY='$injury', INJURYDATE='$date', BODYPART='$bodyPart',  MEDICATION='$medication' ");
		dosql($q);

		$q = mysql_real_escape_string($q);
		islog('mupdate',$mcid, "$q");
		$buf .= "<p>updated $simtrakid with new injury data</p>";
		break;
		}
		case "add a treatment": { break;}
		case "show injuries": { break;}
		case "show treatments": { break;}
		default: { break;}
	}
	$buf .= 	 "<h3>Results</h3><p>go here</p>";

	$buf .=  $tail;
	return $buf;
}

//start right here
//print_r ($_POST);



//if (isset($_POST['op'])) echo thispage(command_handler());
//e/lse 
 echo thispage(command_window());
?>