<?php // vim: ts=4
require_once "session.inc.php";
require_once "./JSON.php";
require_once "fbuser.inc.php";
require_once "appinclude.php";  // required of all facebook apps put this last
require_once "utils.inc.php";

function fbinit($need_frame = true)
{
	// must be called at the start of each medcommons facebook program unless something wierd and special is needed

	$facebook = new Facebook($GLOBALS['appapikey'], $GLOBALS['appsecret']); // come from database via  appinclude.php
    if($need_frame) {
      $facebook->require_frame();
    }
	$user = $facebook->require_login();
	return array($facebook,$user);
}

function smallwall ($mcid,$familyfbid, $limit)
{
	$wallstuff='';
	$q = "select * from  carewalls where wallmcid = '$mcid' and wallfbid = '$familyfbid' order by time desc limit $limit ";
	$result = mysql_query($q) or die("cant  $q ".mysql_error());
	while($u=mysql_fetch_object($result))
	{
		$time = strftime ('%D',$u->time); $wallstuff.="$time: $u->msg\r\n";
	}
	if($wallstuff != '')
	$wallstuff .= "\r\n$wallstuff";

	mysql_free_result($result);

	return  $wallstuff;
}
function smallwallbr ($mcid,$familyfbid, $limit)
{

	$stuff=array();
	$q = "select * from  carewalls where wallmcid = '$mcid'  and wallfbid = '$familyfbid' order by time desc limit $limit ";
	$result = mysql_query($q) or die("cant  $q ".mysql_error());
	while($u=mysql_fetch_object($result))
	{
		$stuff  [] =  array($u->time, $u->authorfbid, $u->msg,$u->severity );
		//dbg("found carewall entry ".$u->msg);
		//$wallstuff.="$time: $u->msg<br/>";
	}
	mysql_free_result($result);
	//return  $wallstuff;
	return  $stuff;
}
function authorwallbr ($fbid,$limit)
{
	$stuff=array();
	$q = "select * from  carewalls c, patients m where c.authorfbid = '$fbid' and m.familyfbid = '$fbid' and c.wallmcid = m.mcid and c.wallfbid = m.familyfbid order by c.time desc limit $limit ";//ok 1/7/09
	$result = mysql_query($q) or die("cant  $q ".mysql_error());
	while($u=mysql_fetch_object($result))
	{
		$stuff  [] =  array($u->time, "$u->firstname $u->lastname", $u->msg );
		//dbg("found carewall entry ".$u->msg);
		//$wallstuff.="$time: $u->msg<br/>";
	}
	mysql_free_result($result);
	//return  $wallstuff;
	return  $stuff;
}
function publish_info($user)
{
	$appname = $GLOBALS['healthbook_application_name'];
	$q = "select applianceurl, mcid from  users  where fbid = '$user' ";
	$result = mysql_query($q) or die("cant  $q ".mysql_error());
	$u=mysql_fetch_array($result);
	if (!$u) return false;
	$mcid = $u[1];
	$hurl = $u[0].$u[1];


	$wallstuff = smallwall($mcid,$user,5);
	$carewall = array(      'field' => 'Carewall ',
	'items' =>array(array('label'=>$wallstuff,
	'description' => 'Recent lines ',
	'link'=>$hurl)));
	$info_fields = array(
	array('field' => 'In Case of Emergency Contact',
	'items' => array(array('label'=> '[replace with a friendly name and number]',
	'description'=>'The Mountain Goats is an urban folk band led by American singer-songwriter John Darnielle.'
	))),
	array(      'field' => 'Online Access URL ',
	'items' =>array(array('label'=>$hurl,
	'description' => 'My Health Record as stored in MedCommons.',
	'link'=>$hurl))),
	$carewall
	);
}


function fb_must_login($s,$t)
{
	$appname = $GLOBALS['healthbook_application_name'];
	$apikey = $GLOBALS['appapikey'];
	return <<<XXX
	<fb:explanation><fb:message>$s</fb:message><p>You must login to Facebook or even better, <a class=applink href='http://www.facebook.com/add.php?api_key=$apikey&app_ref=$t' >add $appname</a>
	</p></fb:explanation>
XXX;

}
function fb_must_add_app($s,$t)
{
	$appname = $GLOBALS['healthbook_application_name'];
	$apikey = $GLOBALS['appapikey'];
	return <<<XXX
	<fb:explanation><fb:message>$s</fb:message><p>You must <a class=applink href='http://www.facebook.com/add.php?api_key=$apikey&app_ref=$t' >add $appname</a> before you can perform this function.
	</p></fb:explanation>
XXX;

}
function republish_user_profile($user)
{            return;

}
function logHBEvent($user,$cat,$message)
{
    dbg("$cat : $message");
	$time=time();
	$message = mysql_escape_string($message);
	$q = "insert into hblog set fbid='$user',category='$cat',title='$message',body='$message',time='$time'";
	mysql_query($q) or die ("Cant $q ".mysql_error());
}
function logMiniHBEvent($user,$filter,$feed_title,$feed_body)
{
	$time=time();
	$message = mysql_escape_string($feed_body);
	$title = mysql_escape_string($feed_title);
	$q = "insert into hblog set fbid='$user',category='$filter',title='$title',body='$message',time='$time'";
	mysql_query($q) or die ("Cant $q ".mysql_error());
	try {
		$GLOBALS['facebook']->api_client->feed_publishActionOfUser($feed_title, $feed_body);
	}
	catch ( Exception $e ) { echo "Cant publish have exceed facebook daily limit for this user  $user"; }
}
function facebook_user_info($facebook,$user){
	$ret= ($facebook->api_client->users_getInfo($user,array('first_name','last_name','pic_small','sex'))); //sex
	if (!$ret) {
		logHBEvent($user,'nouser',"Couldnt call users_getInfo on $user");
		$markup = <<<SSS
		<fb:fbml version='1.1'><fb:title>Facebook Error Returned/fb:title>
		<fb:explanation>
		<fb:message>Facebook did not deliver results for demographic information on <fb:name useyou=false uid=$user />
	</fb:message>
	<p>We plan to restore operation shortly. Thank you for your patience.</p>
	</fb:explanation>
</fb:fbml>	
	
SSS;
		echo $markup;
		exit;
	}

	$fn = mysql_real_escape_string($ret[0]['first_name']);
	$ln = mysql_real_escape_string($ret[0]['last_name']);
	$ps = mysql_real_escape_string($ret[0]['pic_small']);
	$sx = mysql_real_escape_string($ret[0]['sex']);

	return array($fn,$ln,$ps,$sx);
}

function mustload($facebook, $user) {
	$u = HealthBookUser::load($user);
	if ($u===false){
		logHBEvent($user,'badload',"load healthbook user $user failed back to splash screen");
		echo splash($facebook,$user); exit;
	}
	return $u;
}
function mustloadtarget($facebook,$user)
{
	$u = HealthBookUser::load($user);
	if ($u===false){
		logHBEvent($user,'badload',"load healthbook user $user target failed back to splash screen");
		echo splash($facebook,$user); exit;
	}
	$t =HealthBookPatient::loadpatient($u->targetmcid,$u->familyfbid);
	if ($t===false||$t->mcid===false) {

		logHBEvent($user,'badloadtarget',"load healthbook user $user patient failed back to splash screen");
		echo splash($facebook,$user); exit;
	}
	return array($u,$t);
}
function splash($facebook, $user)
{

	if ($user) logHBEvent($user,'splash','should not be logged on here, but we are');

	$splash = <<<XXX
	
	<table class=splash><tr><td><img src='http://www.medcommons.net/images/splashscreens/Facebook%20Home.png' alt='splashpic1' ><br/>
	<span class=caption>Manage your health and your loved one's too!</span></td>
	<td><img src='http://www.medcommons.net/images/splashscreens/Facebook%20Settings.png' alt='splashpic2' ><br/>
	<span class=caption>Your Records are safely stored at Amazon S3</span></td></tr>
	<tr><td><img src='http://www.medcommons.net/images/splashscreens/HealthURL%20Privacy.png' alt='splashpic3' ><br/>
	<span class=caption>You control who can access your records and how</span></td>
	<td><img src='http://www.medcommons.net/images/splashscreens/HealthURL%20Viewer.png' alt='splashpic4' ><br/>
	<span class=caption>Get radiology, labs, fax into your account</span></td></tr></table>
XXX;

	$appname = $GLOBALS['healthbook_application_name'];
	$apikey = $GLOBALS['appapikey'];

	$markup = "<fb:fbml version='1.1'>
	<fb:is-logged-out>
	<fb:title>You are Not Logged On - MedCommons Facebook Home</fb:title>
	<fb:explanation>
	<fb:message>Please <a class=applink href='http://www.facebook.com/add.php?api_key=$apikey&app_ref=nlgisplash' >sign up</a> to use $appname </fb:message>
	$splash
	</fb:explanation>
	<fb:else>
	<fb:if-is-app-user>
	<fb:title>App Loaded - MedCommons Facebook Home</fb:title>
	<fb:explanation>
	<fb:message>If you want to keep your own records on MedCommons Facebook, go to <a class=applink href='settings.php' >settings</a>, or if you are just a Care Giver you can go <a class=applink href='familyteam.php'>home</a></fb:message>
	$splash
	</fb:explanation>
	<fb:else>
	<fb:title>App Not Loaded - MedCommons Facebook Home</fb:title>
	<fb:explanation>
	<fb:message>Please <a class=applink href='http://www.facebook.com/add.php?api_key=$apikey&app_ref=lgisplash' >sign up</a> to use $appname </fb:message>
	$splash
	</fb:explanation>
	</fb:if-is-app-user>
	</fb:is-logged-out></fb:fbml>";

	return $markup;
}

function fmcid ($fbid)
{
	$u = HealthBookUser::load($fbid);
	if ($u===false) {
		//echo "fmcid $fbid returns false";
		return false; }
		else  //bill dec 5
		return array($u->mcid,$u->appliance,$u->gw,0,$u->targetmcid); // target fbid no longer used
}

function opsMailBody($subject,$message)
{
	// $to= 'ssadedin@gmail.com';
	$to= 'billdonner@gmail.com,agropper@gmail.com';
	$appname = $GLOBALS['healthbook_application_name'];
	$headers = 'From: '.$appname.'@medcommons.net' . "\n" .
	'Reply-To:noreply-'.$appname.'@medcommons.net' . "\n" .
	'Content-Type: text/html; charset="iso-8859-1"';

	mail($to, $subject, $message, $headers);
	dbg("sent mail to $to with headers $headers");
}
function opsMail ($subject) { return opsMailBody($subject,$subject);}
function new_account_factory_appliance ()
{
	return $GLOBALS['new_account_appliance']; // someday this will be a fancy allocation policy machine
}
function css ()
{//border:1px solid #3B5998;vertical-align:middle;
$css = <<<CSS
<style type="text/css">
a { color:  #3B5998;}
a.xtinylink  {font-size:.3em; text-decoration:none; color: gray;}
a.tinylink  {font-size:xx-small; text-decoration:underline; color: gray;}
a.tinylink.embedded  {font-size:11px;}
a.tinylink img { position: relative; top: 0px; }
table #mugshots {border-width: 0px;	border-spacing: 5px;	border-style:solid; border-color: gray;	border-collapse: separate;	background-color: white;}
#mugshots tr.invisible {display:none;}
#mugshots td {	font-size: x-small;	width:110px;	border-width: 1px ;	padding: 3px;	border-style: solid;	border-color: gray;	background-color: rgb(255, 245, 238);}
#mugshots td.mugshotgiver {	font-size: x-small; width:55px;padding:  2px;  background-color: #f6f6f6;
  border: 1px solid #bedada;}
#mugshots td.mugshotrole{background-color: #f6f6f6;
  border: 1px solid #bedada;}
.topline { margin-top: 0px; height: 16px; padding:1px 5px 0px 2px;font-size: 1em; }
.floatleft {clear:both; float: left;display:inline; margin-left:13px; padding-top:3px;}
.floatright {float:right; display:inline;margin-right:14px; }
.viewas { font-size:1.0em; color:#3B5998;}
.viewasgo {font-size:.8em;}
.miniform, .miniform form {display:inline; padding:0; margin:0; }
.miniput {height:1.1em;padding:0;font-size:.9em;}
.xlink {width:60px}
td.logocaption {  padding-top:0px; padding-left: 10px; color: #444; }
.appnamebanner,.appversion { margin-top:-15px; padding-left: 36px; font-size:8px; font-weight: 400; font-family: verdana;}
.hurllinks {display:inline; border: 1px solid blue; padding: 0px;}
.bodypart {clear:both;}
.splash {background-color:#EEE}
.splash td {width:350px;}
.splash td img { padding: 20px; border: 1px solid; width: 260px;}
.splash td .caption { padding-left: 20px; font-size:.9em;}
.confirmbuttonstyle { font-weight: normal; margin-left: auto; margin-right:auto; width: 100px; padding: 3px; text-decoration:none; font-size: .8em;  color:#eee;  background-color:#3b5998;  border:1px solid #3b5998;}
#disconbutton {padding: 20px 0px 20px  260px;}
.caregivee { 
  background-color:white;
  border-bottom: 1px solid #e5e5e5;
  margin-bottom: 8px;
  padding: 5px 2px 0px 5px;
}
.smallwallcontainer { margin: 5px 0px; }
.pic img, .pichurl img {width:75px;}
 .caregivee .pic { max-width: 80px; padding: 0px 10px; }
.caregivee .txt { font-size: 1.1em; font-weight:700; margin: 10px 0px; vertical-align: middle; }

.caregivee .hurl { font-size: .7em; font-weight:300; margin: 10px 0px; vertical-align: middle; }
td.wall { vertical-align: top; font-weight: normal; }
td.wall img { position: relative;  top: 3px;}
#mcheader { border: 1px solid white; margin-left:10px; margin-right: 10px; margin-bottom: 5px; }
</style>
CSS;
return $css;
}


function nil_topright_menu()
{

	$outstr = <<<XXX
	<div class=miniform><form    name=mform action='dispatcher.php' method='get'>
	 <select name='xmcid' id='xmcid'	title='view another friends records (you must be a care giver)' class=viewas >
     <option value='-1' >+add new</option></select>
     <input type=image src='http://fb01.medcommons.net/facebook/000/images/magnifier.png' value='go' class=viewasgo /></span>
     </form></div>
XXX;
	return $outstr;
}

// For debugging
function backtrace()
{
	$bt = debug_backtrace();
	ob_start();

	echo("<br /><br />Backtrace (most recent call last):<br /><br />\n");
	for($i = 0; $i <= count($bt) - 1; $i++)
	{
		if(!isset($bt[$i]["file"]))
		echo("[PHP core called function]<br />");
		else
		echo("File: ".$bt[$i]["file"]."<br />");

		if(isset($bt[$i]["line"]))
		echo("&nbsp;&nbsp;&nbsp;&nbsp;line ".$bt[$i]["line"]."<br />");
		echo("&nbsp;&nbsp;&nbsp;&nbsp;function called: ".$bt[$i]["function"]);

		if($bt[$i]["args"])
		{
			echo("<br />&nbsp;&nbsp;&nbsp;&nbsp;args: ");
			for($j = 0; $j <= count($bt[$i]["args"]) - 1; $j++)
			{
				if(is_array($bt[$i]["args"][$j]))
				{
					print_r($bt[$i]["args"][$j]);
				}
				else
				echo($bt[$i]["args"][$j]);

				if($j != count($bt[$i]["args"]) - 1)
				echo(", ");
			}
		}
		echo("<br /><br />");
	}
	$result = ob_get_contents();
	ob_end_clean();
	return $result;
}
function standard_hurl_format ($hurl,$mcid){
	return "<a target='_new' title='Open HealthURL on MedCommons' href='$hurl'>
	<img src='http://www.medcommons.net/images/tinyhurl.png' alt=hurl />
	<span style='font-size:1.1em' >ht</span><span style='font-size:1.0em' >tp:</span><span style='font-size:.9em' >/</span><span style='font-size:.8em' >/</span><span style='font-size:.7em' >www</span><span style='font-size:.6em' >.medcommons</span><span style='font-size:.5em' >.net</span><br/><span style='font-size:.4em' >/$mcid</span>
	</a>";
}
	
function admin_hurl($u)
{
	// this runs down the target account for the current facebook user
	//x = new HealthBookUser($u);
	//$x = HealthBookUser::load($u->familyfbid,$u);
	$hurl = $u->authorize($u->appliance.$u->mcid,$u);//t_hurl();
	$xtra =standard_hurl_format($hurl,$u->mcid);
	return array($hurl,$xtra);
}
function patient_hurl($p)
{
	// this is for patients/elders when $p is a HealthBookPatient
	$x = HealthBookPatient::loadpatient($p->mcid,$p->familyfbid,$p);
	$hurl = $x->authorize($x->appliance.$x->mcid,$x);
	$xtra = standard_hurl_format ($hurl,$x->mcid);
	return array($hurl,$xtra);
}

function dashboard ($fbid,$showelder=false,$showself=false,$superuser=false)
{
	// displays dashboard for any family as given by fbid
	if (!$fbid) die ("In logged in dashboard with $fbid");
	if (!$superuser) $superuser=$fbid;
	$marqueefbml = '';//$GLOBALS['marqueefbml'];
	$appname = $GLOBALS['healthbook_application_name'];
	$hbappuser = $GLOBALS['healthbook_application_image'];
	$version = $GLOBALS['healthbook_application_version'];
	$publisher = @$GLOBALS['healthbook_application_publisher'];
	if (isset( $GLOBALS['healthbook_application_font_family']))
	$ffamily = "font-family: ".$GLOBALS['healthbook_application_font_family'].';'; else $ffamily='';
	$apikey = $GLOBALS['appapikey'];
	$appUrl = $GLOBALS['app_url'];
	$css = css();
	$my_viewing_friends=nil_topright_menu(); $melink='';  $viewing='';$tmcid=0;$mcid=0; $xtra ='';
	$domyself=false; $familybanner = ''; $createbutton='';	$color ='white';
	$tfbid = 0; // needs to work

	// 	GET THE LOGGED IN USER DETAILS BY CALLING LOAD, WHICH SHOULDN"T DO ANY UNNECESSARY WORK WE HOPE
	$r = HealthBookUser::load($fbid);
	if (!$r) die ("cant find facebook user $fbid in dashboard users");

	//  GET FAMILY DETAILS IF NEEDED OTHERWISE USE WHAT WE ALREADY HAVE
	if ($r->familyfbid==$r->fbid) $f=$r ;
	else
	{
		$q="SELECT * from users f where f.fbid='$r->familyfbid' ";
		$result = mysql_query($q) or die ("$q ".mysql_error());
		$f = mysql_fetch_object($result);
		if (!$f) die ("cant find user $r->familyfbid in dashboard users");
		mysql_free_result($result);
	}

//	if ($fbid == $r->familyfbid)
//	$addnew ="<option value='-1' >+new family member</option>";
//	else $addnew ='';

	$familybanner= $GLOBALS['familyname'] = "<span title='$appname-$version by $publisher' >$f->accountlabel Care Team</span>"; // pretty poor form, but don't want to select again

	if ($showself) $familybanner = "<span>Family Care Team Management</span>";

    if(!$f->accountpic || ($f->accountpic == ''))
      $f->accountpic = $GLOBALS['app_url'].'/images/unknown-user.png';

	$GLOBALS['familyphoto'] = $f->accountpic; // try this

	$viewing = '';
	// build select statement
	$counter = 0;
	$my_viewing_friends = <<<XXX
	<div class=miniform><form name=mform action='dispatcher.php' method='get'>
	<select name='xmcid' id='xmcid'	title='view family member records' class=viewas >
XXX;
	$isjane = false;

	//  GET EACH OF THE ELDERS
	$q="SELECT * from patients m where m.familyfbid='$f->familyfbid' ";
	$result = mysql_query($q) or die ("$q ".mysql_error());
	while ($m = mysql_fetch_object($result))
	{
		if (($m->mcid == $r->targetmcid)&&$showelder)
		{


			list($hurl,$xtra) = patient_hurl($m);

			$viewing = "<table class=pichurl><tr><td width=80px>
			<span style='font-size:1.2em'>$m->firstname</span><br/>
			<span style='font-size:1.2em'>$m->lastname</span>
			</td>
			<td  width='60px' ><img src='$m->photoUrl'  /></td></tr>
			</table>$xtra";

			$selected ='selected';
		}
		else $selected ='';

		if ($m->mcid == 1013062431111407 ) $isjane = true;

			$my_viewing_friends.= "<option value='$m->mcid+$m->familyfbid' >$m->firstname $m->lastname</option>"; //pass both mcid and family fbid
		//$foo = "<a href='dispatcher.php?xmid=$m->mcid+$m->familyfbid' >$m->firstname</a>&nbsp;&nbsp;";
	
	}
	//'1013062431111407+1107682260'
//	if (!$isjane) if ($fbid == $r->familyfbid) $my_viewing_friends .= "<option value='-2' >+add Jane Hernandez</option>"; //pass both mcid and family fbid
	// now get each of the families and put them up here too
	$my_viewing_friends .= "<option value='-3' >--------------------------</option>";
	
	$q = "select * from  teams t, users u where t.userfbid='$superuser' and u.fbid = t.teamfbid  order by accepttime desc"; //was u.fbid, check carefully
	$result = mysql_query($q) or die("cant select from  $q ".mysql_error());
	while($r1=mysql_fetch_object($result))
	{	// changed these from familyfbid
		$selected = ($r->fbid==$r1->fbid)?'selected':'';
		$my_viewing_friends .= "<option $selected value='9999999999999999+$r1->fbid' >$r1->accountlabel</option>"; 
		$counter++;
	}
	mysql_free_result($result);
	$selected = ($r->familyfbid==$fbid)?'selected':'';
	$my_viewing_friends .= "<option $selected value='9999999999999999+$r->fbid' >$r->accountlabel</option>"; // self?
	$my_viewing_friends .= "</select>&nbsp;<input type=submit value='go' class=viewasgo />
	</form></div>";
	// BUILD A TAGLINE THAT REFLECTS OUR PROVENANCE
	if (($fbid == $r->familyfbid)&&!$showself)
	$provenance = "<a href=settings.php>manage</a>"; else	$provenance = "";
	// NOW THAT ALL THE COMPONENTS ARE ASSEMBLED, BUILD THE DASHBOARD
	$xlink = "<a title='$f->accountpicdesc' href='$f->accountlink' ><img src='$f->accountpic' alt='missing account pic' /></a>";
	if ($GLOBALS['extgroupurl']!='') $xlink = "<a href='".$GLOBALS['extgroupurl']."' >$xlink</a>";
	$markup = <<<XXX
	$css<div id=mcheader style="$ffamily  background-color: $color"  >
	<div class=topline>
	<span class=floatleft >
	<fb:if-is-app-user>
	<a href="index.php">home</a> |
	<a href="settings.php" >settings</a> |
	</fb:if-is-app-user>
	<a href="http://www.facebook.com/apps/application.php?api_key=$apikey &app_ref=about">about</a>
	</span>
	<span class=floatright>
	<fb:if-is-app-user>$my_viewing_friends
	<fb:else>
	<a class=applink href='http://www.facebook.com/add.php?api_key=$apikey&app_ref=dash' >+add app</a>
	</fb:else>
	</fb:if-is-app-user>
	</span>
    </div><!-- top line -->
    <div style='clear: both;'>
        <div class=floatleft>
        <table><tr>
        <td class=xlink >$xlink</td>
        <td class='logocaption'>
        <h3>$familybanner</h3>
        <h4>$provenance</h4>
        </td>
        </tr></table>
        </div>
        <div class=floatright>
        $viewing
        </div>
        $marqueefbml
    </div>
<div class=bodypart>
XXX;
	return $markup;
}
function hurl_dashboard ($fbid, $kind,$showelder=true)
{
	$top = dashboard($fbid,$showelder);
	//
	$bottom = <<<XXX
	<fb:tabs>
	<fb:tab_item href='family.php' title='collaborate' />
	<fb:tab_item href='faxbarcode.php' title='fax' />
	<fb:tab_item href='sharebyemail.php' title='share by email' />
	<fb:tab_item href='sharebycell.php' title='share by cell' />
	<fb:tab_item href='healthurl.php?o=a' title='activity' />
	<fb:tab_item href='documents.php' title='documents' />
 </fb:tabs>
XXX;
	$needle = "title='$kind'";
	$ln = strlen($needle);
	$pos = strpos ($bottom,$needle);
	if ($pos!==false)
	{  // add selected item if we have a match
		$bottom = substr($bottom,0,$pos)." selected='true' ".
		substr ($bottom, $pos);
	}
	return $top.$bottom;
}

function cloneJane($familyfbid)
{
	// GET JANE'S PATIENT RECORD IN MED COMMONS FAMILY
	$now=time();
	$result = mysql_query("Select * from patients where familyfbid='1107682260' and mcid='1013062431111407'")
	or die ("Cant Find Jane in Med Commons".mysql_error());
	$jhrow = mysql_fetch_array($result);
	if (!$jhrow) die ("Cant Find Jane in Family Med Commons");

	$sql = "Insert into patients set ";
	$sql.= "familyfbid ='$familyfbid', ";
	$sql.= "sponsorfbid ='".$jhrow['sponsorfbid']."', ";
	$sql.= "oauth_token ='".$jhrow['oauth_token']."', ";
	$sql.= "oauth_secret ='".$jhrow['oauth_secret']."', ";
	$sql.= "groupid ='".$jhrow['groupid']."', ";

	$sql.= "mcid ='".$jhrow['mcid']."', ";
	$sql.= "applianceurl ='".$jhrow['applianceurl']."', ";
	$sql.= "gw ='".$jhrow['gw']."', ";

	$sql.= "firstname ='".$jhrow['firstname']."', ";
	$sql.= "lastname ='".$jhrow['lastname']."', ";
	$sql.= "sex ='".$jhrow['sex']."', ";
	$sql.= "photoUrl ='".$jhrow['photoUrl']."', ";
	$sql.= "bgcolor ='".$jhrow['bgcolor']."', ";
	$sql.= "storage_account_claimed ='1' ";
	mysql_query($sql) or die ("Cant Insert Jane Clone $sql ".mysql_error());

	// copy forward the carewall entry  if any
	$result = mysql_query("Select * from carewalls where wallfbid='1107682260' and wallmcid='1013062431111407'")
	or die ("Cant $q while cloning jane H".mysql_error());
	while ($r = mysql_fetch_array ($result))
	{

		$sql = "Insert into carewalls set ";
		$sql.= "wallfbid ='$familyfbid', ";
		$sql.= "wallmcid  ='1013062431111407' , ";
		$sql.= "authorfbid ='".$r['authorfbid']."', ";
		$sql.= "msg ='".$r['msg']."', ";
		$sql.= "severity ='".$r['severity']."', ";
		$sql.= "time='$now' ";
		mysql_query($sql) or die ("Cant Insert Jane Clone Carewall $sql ".mysql_error());
	}



}

?>
