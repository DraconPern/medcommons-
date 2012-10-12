<?php
require_once "DB.inc.php";
require_once "../acct/alib.inc.php";

////this sucks, must fix
$ROOT = "https://portal.medcommons.net/";
$UploadHandler = $ROOT.'p/uh.php';
$PLForm = '/p/plemc.php';

/**
 * Login Service show patientlist
 *
 * rewritten to run inline in the gdashboard and not go thru remote json webservice, which will be reserved for now for the iphone.
 */



// this is very slow for just one group
function group_query($mcid,$maxgroups=20)
{

	$db = DB::get();

	$row = $db->first_row("select * from users where mcid = '$mcid'  " );
	if(!$row)
	die("No such user on re-read of ".$mcid);

	$info = new stdClass;
	$info->accid=$row->mcid;
	$info->fn=$row->first_name;
	$info->ln = $row->last_name;
	$info->email = $row->email;
	$info->idp = '';


	$info = get_full_account_info($info);
	// What other groups does this user have?
	$info->groups = $db->query("select gi.accid,gi.name, gi.parentid,gi.logo_url,gi.grouptypeid, gm.comment 
	                              from groupinstances gi, groupmembers gm
                                    where gm.memberaccid = ? and gm.groupinstanceid = gi.groupinstanceid",
	                                     array($info->accid));
	$xml->result =  $info;
	$xml->status = "ok";
	return $xml;
}



function makeUploadLink ($g)
{
	//
	// we have incorporated most of the fuf.php utility right into here, but its never called more than once in this page
	//
	global $ROOT,$UploadHandler;
	
	$GroupUploadURL = $ROOT.$g->accid.'/upload';
	
	$arg = base64_encode ($g->name.'|'.$g->name.'|'.$g->accid.'|'
	.$GroupUploadURL.'|'.$g->logo_url.'|'.$UploadHandler);
	
if ($g->grouptypeid	!=0) 
	$UploadForm = $ROOT."p/uploader1.php"; else $UploadForm = $ROOT."p/uploader0.php";
	$href = "$UploadForm?a=$arg";
	$link = "<a  title='{$g->accid} {$g->parentid} {$g->name}' href='$href' >Upload Form</a>
";
	return $link;
}



function compute_password($mcid,$pw1) {
	$sha1 = strtoupper(hash('SHA1', 'medcommons.net' . $mcid . $pw1));
	return $sha1;
}

/**
 * Performs query on patient list for specified practice.
 *
 * @param int $practiceId             practice id to query
 * @param int $limit                  max number of rows to return
 * @param int  $start                 row to start at
 * @param String $whereclause         optional where clause to filter results
 * @param String $viewStatusClause    optional viewstatus clause to filter results
 */
function my_query_patient_list($practiceId, $limit, $start=0, $whereclause="", $viewStatusClause=" AND e.ViewStatus = 'Visible' ") {

	$db = DB::get();
	//  echo "my query patient list with";print_r($practiceId);
	$practice = $db->first_row("select * from practice where practiceid=?",array($practiceId));

	$select = "SELECT e.*, wia.wi_id as wi_available_id, wid.wi_id as wi_downloaded_id, c.couponum,
	c.status as couponstatus, c.voucherid as voucherid, do.ddl_status as order_status, do.callers_order_reference as order_reference,
	do.custom_00, 	do.custom_01, 	do.custom_02, 	do.custom_03, 	do.custom_04, 	do.custom_05, 	do.custom_06, 	do.custom_07, 	do.custom_08, 	do.custom_09,
	do.ddl_status as dicom_order_status, u.photoUrl
	FROM practice p, practiceccrevents e
	LEFT JOIN workflow_item wia ON e.PatientIdentifier = wia.wi_target_account_id AND wia.wi_type = 'Download Status' AND wia.wi_active_status = 'Active' and wia.wi_status = 'Available' AND wia.wi_source_account_id = $practice->accid
	LEFT JOIN workflow_item wid ON e.PatientIdentifier = wid.wi_target_account_id AND wid.wi_type = 'Download Status' AND wid.wi_active_status = 'Active' and wid.wi_status = 'Downloaded' AND wid.wi_source_account_id =$practice->accid
	LEFT JOIN modcoupons c on c.mcid = e.PatientIdentifier
	LEFT JOIN dicom_order do on do.mcid = e.PatientIdentifier
	LEFT JOIN users u  on u.mcid = e.PatientIdentifier
	WHERE e.practiceid = '$practiceId' AND e.practiceid = p.practiceid
	AND ((p.accid = wia.wi_source_account_id) OR (wia.wi_source_account_id is NULL))
	AND ((p.accid = wid.wi_source_account_id) OR (wid.wi_source_account_id is NULL))
	$whereclause $viewStatusClause
	GROUP BY e.PatientGivenName, e.PatientFamilyName, e.Guid
	ORDER BY e.CreationDateTime DESC LIMIT $start,$limit";


	return $db->query($select);
}

function my_resolveEmail($email,$password) {
	//	echo "in my resolveEmail";
	$db = DB::get();
	$users = $db->query("SELECT users.mcid, users.sha1,
                           users.first_name, users.last_name,
                           users.email, users.acctype
                           FROM users
                           WHERE users.email = ?
                           ORDER BY users.since desc", array($email));

	foreach($users as $u) {
		$sha1 = compute_password($u->mcid, $password);
		dbg("Computed password hash for ".$u->mcid." on password ".$password." = ".$sha1);
		if($u->sha1 == $sha1)
		return $u;
	}
	return false;
}

function patients_query($mcid,$groupid,$maxpatients=200)
{
	$db = DB::get();


	$row = $db->first_row("select * from users where mcid = '$mcid'  " );
	if(!$row)
	die("No such user on re-read of ".$mcid);

	$info = new stdClass;
	$info->accid=$row->mcid;
	$info->fn=$row->first_name;
	$info->ln = $row->last_name;
	$info->email = $row->email;
	$info->idp = '';


	$info = get_full_account_info($info);
	// What other groups does this user have?
	$info->groups = $db->query("select gi.accid,gi.name, gi.parentid from groupinstances gi, groupmembers gm
                                    where gm.memberaccid = ? and gm.groupinstanceid = gi.groupinstanceid", array($info->accid));
	$patients = array();
	$allgroups = array();

	//$practiceIds = array();
	foreach($info->groups as $g)
	if ($g->accid == $groupid)  // only return from the current practice id
	{
		$practiceIds=$g->parentid;
		$g->patients = array();
		$allgroups[$g->parentid] = $g;
			
		$patients = my_query_patient_list($practiceIds, 200,0, "", " AND e.ViewStatus in ('Visible','Hidden')");
		break;
	}

	$info->practice->patients = $patients;
	//	print_r ($info);
	$xml->result =  $info;
	$xml->status = "ok";
	return $xml;
}





function makeGLink ($g)
{
	global $PLForm;

	$arg = base64_encode ($g->name.'|'.$g->name.'|'.$g->accid.'|'
	.'dummy'.'|'.$g->logo_url.'|'.'dummy');
	$href = "$PLForm?a=$arg";
	$link = "<li class=grouplinks ><a href='$href' title='{$g->name}'>{$g->name}</a> {$g->comment}</li>
";
	return $link;
}
function get_group_list_entries ($accid)
{

	try {
		$result =  group_query($accid);
		if(!$result)
		throw new Exception("Unable to decode JSON returned from URL ".$remoteurl.": ".$file);
		if($result->status != "ok")
		throw new Exception("Bad status '".$result->status."' error='".$result->error."' returned from JSON call ".$remoteurl);
		return $result;
	}
	catch(Exception $ex) {
		die("Unsuccessful test completion ". $ex->getMessage());
	}
	return false;
}



function Gtestif_logged_in()
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
function get_patient_list_entries ($mcid,$gid)
{

	try {
		$result =  patients_query($mcid,$gid);
		if(!$result)
		throw new Exception("Unable to decode JSON returned from URL ".$remoteurl.": ".$file);
		if($result->status != "ok")
		throw new Exception("Bad status '".$result->status."' error='".$result->error."' returned from JSON call ".$remoteurl);
		return $result;
	}
	catch(Exception $ex) {
		die("Unsuccessful test completion ". $ex->getMessage());
	}
	return false;
}
function getPL($accid,$groupid)
{

	global $ROOT,$info;

	$metaboard = get_patient_list_entries($accid,$groupid);
	//	print_r($metaboard);

	$practice = $metaboard->result->practice;
	$practicename = $practice->practicename;
	$practiceaccid = $practice->accid;
	$patients = $practice->patients;
	// do this twice to separate into different piles
	$out1 =<<<XXX
<div class=toppart>
	<table>
	<tbody>

XXX;

	$counter = 0;

	foreach ($patients as $patient)
	{

		if (($patient->ViewStatus!='Hidden'))
		{
			
			$name = trim ("$patient->PatientGivenName $patient->PatientFamilyName");
			$when = strftime('%D %T', $patient->CreationDateTime);
			$date = substr($when,0,8);
			$time = substr($when,9,5);
			$hospital = $patient->custom_00;
			$sender = $patient->custom_01;
			$viewlink = "http://{$_SERVER['HTTP_HOST']}/router/viewer/{$patient->PatientIdentifier}?auth={$info->auth}";
			// when running on the pad, we need special links
			//				$viewlink = "x-medpad://viewer?auth={$info->auth}&id={$patient->PatientIdentifier}".
			//				"&fn={$patient->PatientGivenName}&ln={$patient->PatientFamilyName}&date={$date}&time={$time}"
			//				."&healthURL=http://{$_SERVER['HTTP_HOST']}/router/viewer/{$patient->PatientIdentifier}"; // back to real link
			//echo "viewerlink <a href='$viewlink' >$viewlink</a><br/>";
			$toconsultant  = $patient->custom_07;
			$patientid = $patient->PatientIdentifier;
			$comment = $patient->custom_06;
			$oref = $patient->order_reference;


			$out1 .= "
			<tr><td><a href='$viewlink'>
			<div class=line1>
			<span class='stime' >$time $date</span>
			<span class='spatient'  >$name</span>
			<span class='shospital' >$hospital</span>
			<span class='sname'  >$sender</span>
			<span class='slabel'  >$comment</span>
			<span class='slabel'  >$toconsultant</span>
			</div>

			</a></td></tr>";

			$counter++;
		}

	}// if for

	if ($counter == 0)
	$out1 .= <<<XXX
	</tbody>
</table>
</center>
<p>No entries</p>
</div>
XXX;

	else
	$out1 .= <<<XXX
	</tbody>
</table>
</center>
</div>
XXX;

	
		$out2 =<<<XXX

<div class=picpart>

XXX;

	$counter = 0;

	foreach ($patients as $patient)
	{

		
		if (($patient->ViewStatus=='Hidden'))
		{
			$photourl = $patient ->photoUrl;
			if ($photourl=='') $photourl = $ROOT.'/images/unknown-user.png';
			$name = trim ("$patient->PatientGivenName $patient->PatientFamilyName");
			$when = strftime('%D %T', $patient->CreationDateTime);
			$date = substr($when,0,8);
			$time = substr($when,9,5);
			$hospital = $patient->custom_00;
			$sender = $patient->custom_01;
			$viewlink = "http://{$_SERVER['HTTP_HOST']}/router/viewer/{$patient->PatientIdentifier}?auth={$info->auth}";
			// when running on the pad, we need special links
			//				$viewlink = "x-medpad://viewer?auth={$info->auth}&id={$patient->PatientIdentifier}".
			//				"&fn={$patient->PatientGivenName}&ln={$patient->PatientFamilyName}&date={$date}&time={$time}"
			//				."&healthURL=http://{$_SERVER['HTTP_HOST']}/router/viewer/{$patient->PatientIdentifier}"; // back to real link
			//echo "viewerlink <a href='$viewlink' >$viewlink</a><br/>";
			$toconsultant  = $patient->custom_07;
			$patientid = $patient->PatientIdentifier;
			$comment = $patient->custom_06;
			$oref = $patient->order_reference;


			$out2 .= "
			<div class=outerblock title ='$practiceaccid $accid $groupid' ><a href='$viewlink'>
			<div class=photoblock>
			<img width=100px src='$photourl' />
			<div class='stime' >$time $date</div>
			<div class='spatient'  >$name</div>

			</div>

			</a></div>";

			$counter++;
		}

	}// if for

	if ($counter == 0)
	$out2 .= <<<XXX


<p>No entries</p>
</div>
XXX;

	else
	$out2 .= <<<XXX

</div>
XXX;
	return array($out1,$out2);
}




// main starts here

//	echo "getting consultants dashboard";
$db = DB::get();

// show the consultants filtered dashboard
if(!($me=Gtestif_logged_in())) die ("Please first <a href='../acct/login.php'>login</a> to MedCommons");


else
list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;


	$info = get_account_info();
	if(!$info)
	return die ("cant find account $accid but we are logged on");

$menutop = "<div id=grouplinks><ul>";

$result  = get_group_list_entries($accid);
$groups = $result->result->groups;
$firstg = false;

foreach ($groups as $g) {
	if (!$firstg) $firstg = $g;
	$menutop .=makeGLink($g);
}


$menutop .="</ul></div>
";
if (count($groups) <=1) $menutop = ''; // dont bother anyone with menu

if (!isset($_GET['a']))
{
	$ateam = $firstg -> name;
	$ShortName= $firstg -> name;
	$LongName= $firstg -> name;
	$GroupID= $firstg -> accid;
	$GroupLogo= $firstg -> logo_url;
	$Unused= '';
}
else
{

	$arg = $_GET['a'];
	$args = base64_decode($arg);
	list($ShortName,$LongName,$GroupID,$Unused,$GroupLogo,$Unused)=explode('|',$args);

}
list($patientlistbodyinbox,$patientlistbodygroup)=getPL($accid,$GroupID); // get the patient list



if ($GroupLogo=='') $GroupLogo='http://medcommons.net/images/logoHeader.gif';

$bluehref =  "https://portal.medcommons.net/router/PersonalBackup?storageId=$accid&auth={$info->auth}"; ///// careful here  <===========================


// generate the link



$uploadlist = "
<a>";

$result  = get_group_list_entries($accid);
$groups = $result->result->groups;
$firstg = false;
$m = 'not found';
foreach ($groups as $g) {
	if( $GroupID == $g->accid ) // filter down to just us 
	{
		$uploadlist .=makeUploadLink($g);
		$m ="{$g->accid} {$g->parentid} {$g->name}";
	
	break;
	}
}


$uploadlist .="</a>
";


?>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MedCommons Inbox for <?= $bluehref ?></title>
<link rel="shortcut icon" href="../acct/images/favicon.gif" type="image/gif"/>
<style>
body {
	font-family: Verdana, Tahoma;
	padding: 0; margins: 0;
}

ul {
	display: inline;
	margin-left:0px;
	padding-left:0px;
	padding-bottom: 20px;
}

ul li {
	display: inline;
	list-style: none;
	padding-right: 20px;
}

a {
	text-decoration: none;
	color: #777;
}

a:hover {
	text-decoration: underline
}
.photoblock {
	font-size: 10px;
	color:black;
	}
.outerblock {
	display: inline;
	float: left;
	padding: 10px;
	}
.bottompart {
	clear: both;
	display: block;
	}
</style>
</head>
<body>

<img onClick="" src='<?= $GroupLogo ?>' />
<h3><?= $LongName ?> Inbox</h3>
<div class='hideerror'></div>
<?= $patientlistbodyinbox ?>
<br/>
<br/>
<h3><?= $LongName ?> Group</h3>
<?= $patientlistbodygroup ?>
<br/>
<br/>
<div class=bottompart>
 <?= $uploadlist ?>&nbsp; <a href=''>Add Member</a>
<br/>
<br/>
<a title='press the blue button for USHSS approved PHR evacuation' href='<?=$bluehref?>' >
<img width=70px src='http://t2.gstatic.com/images?q=tbn:wVt2pfmv5R2qTM:http://www.clker.com/cliparts/a/9/3/e/1194984754884631372button-blue_benji_park_01.svg.hi.png' /></a>
<br/>
</div>
<hr />
<?= $menutop ?>
<br/>
Logged in as <?= $fn ?> <?= $ln ?> <?= $email ?> <a title='<?= $m ?>' href='/acct/settings.php'>settings</a> <a href='/acct/logout.php'>logout</a><br/>
<small>powered by <a href='http://www.medcommons.net' />MedCommons</a></small>
</body>
</html>