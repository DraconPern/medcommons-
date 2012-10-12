<?php
require_once "Envision-DataKit/is.inc.php";


global $GRID;
function simtrak_error_page($err)
{
	echo "<h3>There is a problem with your use of Simtrak Envision</h3><p>$err</p>";
	exit;
}


function check_errs()
{
if (isset($_REQUEST['err']))
$err=$_REQUEST['err'];
else $err='';
if ($err=='plslogontomedcommons') $err = "Please Sign On To MedCommons"; else
if ($err=='notsimtrakuser') $err ="You are not enabled for Simtrak Access";
if ($err!='') simtrak_error_page($err);
}

function check_login($roles,$blurb)
{
	$time = time();
	$r = user_record();
	if (!$r)
	{
		simtrak_error_page("Please contact your Simtrak Envision Administrator for a Username and Password");
	}

	$urole = $r->role;
	$role = explode(',',$roles);
	$count = count($role);
	for ($j=0; $j<$count; $j++)
	if ($role[$j] == $urole)
	{
		//islog('view',$r->mcid,$blurb);
		return $urole;
	}

	simtrak_error_page("You do not have privilege to access this page. Please contact your Simtrak Envision Administrator");


	// islog('userfound',$mcid, "role::$r->role");

}

function userpagefooter()
{ // starts with tail end of the linkarea section

$time = strftime('%D %T');
$myid = my_identity();
$my_role = my_role();
	"you are signed on as $myid role $my_role"
;
$ret = <<<XXX
<div id='footer'>$time - you are signed on as $myid; your role is: $my_role<br/><a href="signon.php" title="MedCommons Simtrak Navigator">Simtrak Navigator</a>
 is built on the <a href='https://www.medcommons.net/'>MedCommons Appliance</a>
<br/>
</div>
XXX;
return $ret;
}
function teamfooter($team,$search='',$extra='')
{
	$teamind = get_teamind($team);
	if ($search=='') $search = "<a href='search.php?teamind=$teamind' >search</a>";

	$league = getLEague($team);
	$userpagefooter=userpagefooter();
	$result = dosql ("Select * from teams where teamind='$teamind'");
	$rr = isdb_fetch_object($result);
	if ($rr)
	{
		if ($rr->schedurl!='')$sched = "
		&nbsp;|&nbsp;<a target='_new' target='_new' href='$rr->schedurl' title='schedule  $team'>sched</a>";
		else $sched='';
		if ($rr->newsurl!='')$news = "
		&nbsp;|&nbsp;<a target='_new' target='_new' href='$rr->newsurl' title='news for  $team'>rss</a>";
		else $news='';
		$eteam=urlencode($team);

		$my_role = my_role();
		$leaguelink = '';
		if (($my_role=='is')||($my_role=='league')) $leaguelink =
		"<a href='l.php?leagueind=$league->ind' >league</a>&nbsp;|&nbsp;" ;

		$x=<<<XXX
		<div id='linkarea'>$extra $league->customlinks $leaguelink
		<a href='t.php?teamind=$teamind' >team</a>&nbsp;|&nbsp;
		$search $sched
		$news&nbsp;|&nbsp;<a target='_new' href='launchsf.html?team=$eteam' title='support on salesforce.com for $team in new window'>support</a>
		&nbsp;|&nbsp;<a href='/acct/logout.php?next=/simtrak/index.php' title='logout from simtrak navigator'>logout</a>
		</div>
		$userpagefooter
XXX;

		return $x;
	}
	return false;
}

function standard_top ($title='SimTrak Envision V0.8 - Powered by MedCommons')
{
	global $serviceskin_;

	return <<<XXX
	<DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
	<html><head><meta http-equiv="content-type" content="text/html; charset=utf-8">	
	<title>$title</title>
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.6.0/build/autocomplete/assets/skins/sam/autocomplete.css" />
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.6.0/build/fonts/fonts-min.css" />
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.6.0/build/calendar/assets/skins/sam/calendar.css" />
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.6.0/build/treeview/assets/skins/sam/treeview.css" />
	<link rel="stylesheet" type="text/css" href="/yui/2.6.0/fonts/fonts-min.css" />
	<link rel="stylesheet" type="text/css" href="/yui/2.6.0/tabview/assets/skins/sam/tabview.css" />
	<link rel="stylesheet" type="text/css" href="/yui/2.6.0/datatable/assets/skins/sam/datatable.css" />
	<link  href="Envision-DataKit/inputex/0.2.0/inputex-min.css" rel="stylesheet" type="text/css" />
	<script type="text/javascript" src="/yui/2.6.0/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/element/element-beta-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/tabview/tabview-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/datasource/datasource-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/datatable/datatable-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/container/container-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/connection/connection-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/json/json-min.js"></script>
	<script type="text/javascript" src="/yui/2.6.0/utilities/utilities.js"></script>
	<script src="Envision-DataKit/inputex/0.2.0/inputex-min.js" type="text/javascript"></script>
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

	
<link rel="stylesheet" type="text/css" href='Envision-css/style.css' />
<link rel="stylesheet" type="text/css" href="$serviceskin_" />
</head>
<body class=" yui-skin-sam">
<div class="simtrak_viewer">
<div class=top >
	<img id=logo src='Envision-images/BluePoweredByMasterTrans250x50.png'/>
XXX;
}
function playerchoiceform()
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
?>