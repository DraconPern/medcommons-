<?php
// show and control medcommons facebook tables
//
// installed as apps.facebook.com/blucare with a specific list of enabled developers
//
// does not use regular medcommons facebook libraries
//
//
require_once 'adminsetup.inc.php';
require 'facebook.php';
function fbstats ($facebook,$user)
{

	//time some api calls into facebook from here 
	$t0 = microtime(true);
	//test 1 - smallest possible call asks if I am app user
	$info=$facebook->api_client->fql_query("SELECT is_app_user FROM user WHERE uid=$user;");
	$t1 = microtime(true);	
	//test 2 - get all my groups
	$ret = $facebook->api_client->fql_query("SELECT gid,pic_small,name FROM group WHERE gid IN
	                                   (SELECT gid FROM group_member WHERE uid='$user') ");
	$t2 = microtime(true);

	$delta1 = round($t1-$t0,3);
	$delta2 = round($t2-$t1,3);
            $self = $_SERVER['HTTP_HOST'];	
            $gmt = gmstrftime("%b %d %Y %H:%M:%S");
	$out="<p>Running at $gmt GMT on $self";
	$out.="<p>Simplest FQL Api Call into Facebook returns in $delta1 seconds</p>";
	$out.="<p>Complex Api Call to get the group pics for all groups of which I am a member returns $delta2 seconds</p>";
	return $out;
}
function show_teams()
{
	$out="<table><tr><th>family team</th><th>facebook member</th><th>member since</th></tr>";
	// utab=Select * from users where fbid == '$user'
	$q = "select * from  teams order by accepttime desc";
	$result = mysql_query($q) or die("cant select from  $q ".mysql_error());
	while($r=mysql_fetch_object($result))
	{

		$family = "<fb:name uid='$r->teamfbid' useyou='false'/></fb:name>";
		$fbuser = "<fb:name uid='$r->userfbid' useyou='false'/></fb:name>";
		$accepted = strftime('%D',$r->accepttime);
		$out.="<tr><td>$family</td><td>$fbuser</td><td>$accepted</td></tr>";
	}
	$out.="</table>";
	return $out;
}

function show_account_admins($app,$fbidme)
{
	// everyone wih a family is an account admin, right now they require a medcommons account too

	$extra = '';
	$appliance = false;
	$mcid = false;
	$out="<table><tr><th>ops</th><th>fb user</th><th>fb family label</th><th>admin mcid</th><th>first</th><th>last</th><th>viewing family</th></tr>";
	// utab=Select * from users where fbid == '$user'
	$q = "select * from  users ";//where familyfbid = fbid";
	$result = mysql_query($q) or die("cant select from  $q ".mysql_error());
	while($u=mysql_fetch_object($result))
	{
		if ($u!==false){
			$mcid=$u->mcid; $label = $u->accountlabel;
			$appliance = $u->applianceurl;
			$fbid = $u->fbid;

			$family = "<fb:name uid='$u->familyfbid' useyou='false'/></fb:name>";
			$out.="<tr><td><a href='?log=$fbid' title='show log for this user' >l</a>
			<a href='$app->appcallbackurl/exporter.php?export=$fbid&appkey=$app->key&user=$fbidme' title='export fbdata' >e</a></td>
			<td title='fbid $u->fbid using app $u->app' ><fb:name uid='$u->fbid' useyou='false'/></fb:name></td><td>$label</td>
			<td title='appliance: $appliance'>$mcid</td>
			<td>$u->firstname</td><td>$u->lastname</td><td title='viewing family fbid $u->familyfbid' >$family</td></tr>";

		}
	}
	$out.="</table>";
	return $out;
}

function show_patient_accounts()
{
	// return the appliance and the medcommons user id
	$appliance = false;
	$mcid = false;
	$out="<table><tr><th>family</th><th>mcid</th><th>sponsor</th><th>first</th><th>last</th><th>sex</th></tr>";
	// utab=Select * from users where fbid == '$user'
	$q = "select * from  patients "; //ok
	$result = mysql_query($q) or die("cant select from  $q ".mysql_error());
	while($u=mysql_fetch_object($result))
	{
		if ($u!==false){
			$mcid=$u->mcid;
			$appliance = $u->applianceurl;
			$fbname = "<fb:name uid='$u->sponsorfbid' useyou='false'/></fb:name>";
			$family = "<fb:name uid='$u->familyfbid' useyou='false'/></fb:name>";
			$out.="<tr><td>$family</td><td title='appliance: $appliance'>$mcid</td><td>$fbname</td>
			<td>$u->firstname</td><td>$u->lastname</td><td>$u->sex</td></tr>";

		}
	}
	$out.="</table>";
	return $out;
}

function display_tables($facebook,$user,$app,$fbidme){
	$css = file_get_contents('fbadminstyle.css'); // borrow the stylesheet
	$perf = fbstats($facebook,$user);
	$showteams = show_teams();
	$table1a = show_account_admins($app,$fbidme);
	//$table1 = show_facebook_only();
	$table3 = show_patient_accounts();
	//$table2 = show_family_care_team_members();
	$markup = <<<XXX
	<fb:fbml version='1.1'><fb:title>$app->healthbook_application_name  Admin Console</fb:title>
	<style type='text/css'>
	$css
	</style>
	<fb:dashboard>$app->healthbook_application_name Admin Console
	<fb:action 	href='exporter.php?all&appkey=$app->key&user=$fbidme'  >export</fb:action>
		<fb:help href='emptydb.php' >empty db</fb:help>
	</fb:dashboard>
	<div class=fbtab><h1>MedCommons Facebook Admin Console</h1></div>
	<div class=fbbody >
	<div class=fbgreybox>
	<h2>$app->appstatus</h2>

	<small>connected to medcommons applications on same server as $app->appcallbackurl</small>
	<p>
	<a href='$app->appcallbackurl/exporter.php?all&appkey=$app->key&user=$fbidme' class=tinylink title='export fbdata' >export all users</a><br/>
	<a href='$app->appcallbackurl/emptydb.php' class=tinylink ><small>empty tables</small></a><br/>
	<div class=importform><small>
	<form enctype="multipart/form-data" action="$app->appcallbackurl/importer.php" method="POST">
	<input name="uploaded" type="file" size=50 value='' />&nbsp;
	<input type="submit" value="import family" />
	</form></small>
	</div>
	</p>
	</div>
	<div class=fbtab><h2>Performance of Facebook Servers</h2></div>
	<div class=fbgreybox>
	
	$perf
	</div>

	<div class=fbtab><h2>Facebook Users Using MedCommons Apps</h2></div>
	<div class=fbgreybox>
	
	$table1a
	</div>

	<div class=fbtab><h2>MedCommons Patient Accounts</h2></div>
	<div class=fbgreybox>
	
	$table3
	</div>
	
	
	<div class=fbtab><h2>Facebook Teams Table</h2></div>
	<div class=fbgreybox>
	$showteams
    </div>
    </div>
</fb:fbml>
XXX;

	return $markup;
}
function display_logs($fbid)
{
	$buf ='';
	$fbid = $_GET['fbid'];

	$markup = <<<XXX
	<fb:fbml version='1.1'><fb:title>MedCommons Admin Console</fb:title>
	<fb:dashboard>MedCommons Admin Console</fb:dashboard>
	<fb:explanation>
	<fb:message>MedCommons Facebook Log for User <fb:name uid=$fbid/></fb:message>
      <fb:wall>
XXX;
	$q = "select * from hblog where fbid='$fbid' order by ind desc limit 50";
	$result = mysql_query($q) or die("Cant $q ".mysql_error());
	while ($r = mysql_fetch_object($result))
	{//		<br/><span style="font-size:10px">$r->body</span>
		$markup .= <<<XXX
		<fb:wallpost linked=false uid="$fbid" t="$r->time" ><span style="font-size:12px">$r->title</span>

		</fb:wallpost>
XXX;
	}
	$markup .= <<<XXX
  </fb:wall></fb:explanation>
</fb:fbml>
XXX;
	echo $markup;

}


//**start here




$me = $_SERVER['PHP_SELF'];

$me = substr($me,0,strrpos($me,'/')+1);

// ssadedin: some sloppy code creates urls with // instead of /
// it would be nice to clean up the sloppy code, but for the sake
// of convenience we simply coalesce the doubled slashes together here
$me = str_replace("//", "/", $me);

$GLOBALS['app_url']='http://' . $_SERVER['HTTP_HOST'] . $me;

mysql_connect($GLOBALS['DB_Connection'], $GLOBALS['DB_User']) or die("facebook boostrap: error  connecting to database.");
$db = $GLOBALS['DB_Database'];
mysql_select_db($db) or die("can not connect to database $db");
$result = mysql_query("SELECT * FROM `fbapps` WHERE `key` = '$me' ") or die("$me in $db is not registered with medcommons as a healthbook application".mysql_error());
$app = mysql_fetch_object($result);
if ($app===false)  die("$me is not registered with medcommons as a healthbook application");
$facebook = new Facebook($app->appapikey, $app->appsecret);
$facebook->require_frame();
$user = $facebook->require_login();

if (isset($_GET['log']) ) display_logs($_GET['log']);



else echo display_tables($facebook,$user,$app,$user);
exit;
/*
 function show_facebook_only()
 {  return '';
 $extra = '';
 // return the appliance and the medcommons user id
 $appliance = false;
 $mcid = false;
 $out="<table><tr><th>user</th></tr>";
 // utab=Select * from users where fbid == '$user'
 $q = "select * from  users where mcid=0 ";
 $result = mysql_query($q) or die("cant select from  $q ".mysql_error());
 while($u=mysql_fetch_object($result))
 {
 if ($u!==false){
 $mcid=$u->mcid;
 $appliance = $u->applianceurl;
 $fbid = $u->fbid;

 $fbname = "<fb:name uid='$u->fbid' useyou='false'/></fb:name>"; //sponsor field should be retired
 $out.="<tr><td><a href='?fbid=$fbid' >$fbname</a></td></tr>";

 }
 }
 $out.="</table>";
 return $out;
 }
 */
/*
 function show_family_care_team_members()
 {
 // return the appliance and the medcommons user id
 $appliance = false;
 $mcid = false;
 $out="<table><tr><th>caregiver</th><th>family</th><th>patient mcid</th><th>mc first</th><th>mc last</th><th>mc sex</th></tr>";
 // utab=Select * from users where fbid == '$user'

 $q = "select * from  users where familyfbid != fbid";
 $result = mysql_query($q) or die("cant select from  $q ".mysql_error());
 while($u=mysql_fetch_object($result))
 {
 if ($u!==false){
 $mcid=$u->targetmcid;
 $appliance = $u->applianceurl;
 $family = "<fb:name uid='$u->familyfbid' useyou='false'/></fb:name>";
 $giver= "<fb:name uid='$u->fbid' useyou='false'/></fb:name>";
 $out.="<tr><td><a href='?fbid=$u->fbid' title='Show log for this user' >$giver</a></td><td>$family</td><td title='appliance: $appliance'>$mcid</td>
 <td>$u->firstname</td><td>$u->lastname</td><td>$u->sex</td></tr>";

 }
 }
 $out.="</table>";
 return $out;
 }
 */
?>
