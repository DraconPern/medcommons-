<?php
require_once "DataKit/is.inc.php";
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
		//islog('view',$r->openid,$blurb);
		return $urole;
	}

	simtrak_error_page("You do not have privilege to access this page. Please contact your Simtrak Envision Administrator");


	// islog('userfound',$openid, "role::$r->role");

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
?>