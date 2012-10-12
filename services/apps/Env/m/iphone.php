<?php
require_once 'utils.inc.php';
require_once '../setup.inc.php';

function frontmatter()
{
	return <<<XXX

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="author" content="MedCommons, Inc." />
<meta name="description" content="Simtrak Mobile Explorer" />
<meta name="keywords"
	content="medcommons, personal health records,ccr, phr, privacy, patient, health, records, medical records,emergencyccr" />
<meta name="robots" content="all" />
<meta name="viewport" content="width=320" />
<style>
body {
	width: 320px;

	margin: 0;
	padding: 0;
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
</style>
<script type="text/javascript">
function build_confirm(op,mcid,bodyPart,injury,medication)
{
x =  'Are you sure you want to '+document.getElementById(op).value+' re: player '+document.getElementById(mcid).value+ ' '
+document.getElementById(bodyPart).value+' '+document.getElementById(injury).value+' '+document.getElementById(medication).value+'?';
return x;
}

function formSubmit()
{
document.getElementById("phoneform").submit();
return true;
}
</script>
</head>

<body>
<div class=phonebox>
<h3>MedCommons Simtrak Mobile Explorer</h3>

<form action="index.php" id='phoneform' name='phoneform' method=post >

XXX;

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
function myplayers()
{  // build the list needed for autocomplete AND a nav widget


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
function show_main()
{
	$allplayers = myplayers();


	$p0 = <<<XXX
	<div class=clump>
	<span class=oneline>
	$allplayers
&nbsp;&nbsp;&nbsp;&nbsp;
	<select id='op' name="op">
	<option value="add an injury">Add Injury</option>
	<option value="add a treatment">Add Treatment</option>
	<option value="show injuries">Show Injuries</option>
	<option value="show treatments">Show Treatments</option>
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
<input type=submit name=xsubmit id=xsubmit value=submit onclick="if (confirm(build_confirm('op','mcid','bodyPart','injury','medication') )) return formSubmit(); else return false;" />
</form>
</div>
</body>
</html>
XXX;
	echo frontmatter(),$p0,$p1,$p2,$p3,$tail;
}
function error ($e)
{
	$tail = <<<XXX
<input type=submit name=xsubmit id=xsubmit value=Continue />
</form>
</div>
</body>
</html>
XXX;
	echo "<h3>Error</h3> $e".$tail;
	exit;
}
function show_results()
{
	$tail = <<<XXX
<input type=submit name=xsubmit id=xsubmit value=Ok />
</form>
</div>
</body>
</html>
XXX;
	echo frontmatter()."<h3>Request</h3>";
	echo "<div class=results>".$_POST['op']." - ".$_POST['mcid'];
	if (isset($_POST['bodyPart'])&&($_POST['bodyPart']!=='')) echo "<br/>".$_POST['bodyPart'];
	if (isset($_POST['injury'])&&($_POST['injury']!=='')) echo "<br/>".$_POST['injury'];
	if(isset($_POST['medication'])&&($_POST['medication']!==''))echo "<br/>".$_POST['medication'];
	echo "<hr/>";
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
		echo "<p>updated $simtrakid with new injury data</p>";
		break;
		}
		case "add a treatment": { break;}
		case "show injuries": { break;}
		case "show treatments": { break;}
		default: { break;}
	}
	echo "<h3>Results</h3><p>go here</p>";

	echo $tail;
}

//start right here
if (isset($_POST['xsubmit'])&&($_POST['xsubmit']=='submit')) show_results();
else show_main();
?>

