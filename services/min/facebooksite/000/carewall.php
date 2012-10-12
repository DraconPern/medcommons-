<?php
// careteam stuff
require_once 'healthbook.inc.php';
require_once 'collaboratepage.inc.php';

// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit();

$u = mustload($facebook,$user);
// *** end of basic start

$page = $GLOBALS['facebook_application_url'];

$appname = $GLOBALS['healthbook_application_name'];


if (isset($_REQUEST['wallmcid']))
{
	// wall handler completion
	$wallmcid = $_REQUEST['wallmcid'];
	$wallfbid = ($_REQUEST['wallfbid']);
	$body = $_REQUEST['body'];
	if (isset($_REQUEST['info'])) $severity=1; else $severity=0;
	$rbody = mysql_escape_string($_REQUEST['body']);
	$now = time();
	$q = "REPLACE INTO carewalls set wallmcid = '$wallmcid', wallfbid='$wallfbid', severity='$severity', authorfbid='$user',time='$now',msg='$rbody' ";
	mysql_query($q) or die ("Cant $q");
// jumping off to outer space
$markup = collaboration_page($facebook,$user);
}
else if (isset($_REQUEST['removepost']))
{

	$dbrecid= $_REQUEST['id'];
	$time=$_REQUEST['removepost'];
	$q = "Delete from carewalls where id='$dbrecid' and time='$time'  ";
	mysql_query($q) or die ("Cant $q");

// jumping off to outer space
$markup = collaboration_page($facebook,$user);
}

else
{
	// put up a form
	$dash = dashboard($user);

	$wallmcid = $u->targetmcid;
	$wallfbid = $u->familyfbid;
		
	$q = "Select * from mcaccounts where familyfbid='$wallfbid' and mcid='$wallmcid' ";
	$result = mysql_query($q) or die("Cant $q ".mysql_error());
	$r = mysql_fetch_object($result);
	$markup = <<<XXX
<fb:fbml version='1.1'>
	$dash
	<fb:explanation>
	<fb:message>Write to $r->firstname $r->lastname's CareWall</fb:message>
	
	<fb:editor action="carewall.php" labelwidth="100">
	<fb:editor-custom label="message">
	<textarea rows=5 cols=50 name='body'></textarea>
	<input type=hidden name=wallmcid value=$wallmcid />
	<input type=hidden name=wallfbid value=$wallfbid />
	</fb:editor-custom>
	<fb:editor-buttonset>
	<fb:editor-button name=wall value="Write to CareWall"/>
      <fb:editor-cancel value="Cancel" href="dispatcher.php?xmcid=$wallmcid+$wallfbid" />
	</fb:editor-buttonset>
	</fb:editor>
	<p>Only $u->accountlabel CareTeam members can view this CareWall.</p>
  </fb:explanation>
</fb:fbml>
XXX;

}

echo $markup;
?>
