<?php
// careteam stuff
require_once 'healthbook.inc.php';
function  careteam_notify_list ($user,$facebook)
{ // return a string which is an array delimited list of facebook ids
$counter = 0; $outstr=array();
$q = "select * from  users f  where f.fbid = '$user' ";
$result = mysql_query($q) or die("cant  $q ".mysql_error());
while($u=mysql_fetch_object($result))
{
	$outstr[] = $u->fbid;
	$counter++;
}
mysql_free_result($result);
return $outstr;
}

// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit();
// *** end of basic start

$u = mustload($facebook,$user);


$page = $GLOBALS['facebook_application_url'];
$appname = $GLOBALS['healthbook_application_name'];
$dash = dashboard($user);
if (isset($_REQUEST['o'])) $op =  $_REQUEST['o']; else $op='';


if (isset($_REQUEST['send']))  //SEND THE EMAIL
{	// send
// send a real email
$subject = $_REQUEST['subject'];
$body = $_REQUEST['body'];
$notification = " on $appname says $subject $body";
$emailSubject = "<fb:notif-subject>$subject</fb:notif-subject>";
$email = "<html>$body</html>".$emailSubject;
$uid = careteam_notify_list($user,$facebook);
$sendmail = $facebook->api_client->notifications_send($uid, $notification,'user_to_user');
$markup = <<<XXX
<fb:fbml version='1.1'>
$dash
<fb:explanation>
<fb:message>$appname -- Your Care Team Was Notified</fb:message>
<p>They each received the message $subject $body</p>
<p>Your friends will be receiving both emails and facebook notifications</p>
<p>$appname will never add anyone to your CareTeam without mutual consent.</p>
       <fb:editor action="index.php" labelwidth="100">
       <fb:editor-buttonset>
       <fb:editor-button value="OK"/>
     </fb:editor-buttonset>
  </fb:editor>
 </fb:explanation>
</fb:fbml>
XXX;


}
else
{
	//
	$dash = dashboard($user);
	$markup = <<<XXX
	<fb:fbml version='1.1'>
	$dash
	<fb:explanation>
	<fb:message>Send a Message to All Family CareTeam Members</fb:message>
	<p>The message will be sent via facebook to all Care Team members.</p>
	<fb:editor action="notify.php" labelwidth="100">
	<fb:editor-text name="subject" label="subject" value=""/>
	<input type=hidden name=send value=send>
	<fb:editor-custom label="message">
	<textarea rows=5 cols=50 name='body'></textarea>
	</fb:editor-custom>
	<fb:editor-buttonset>
	<fb:editor-button value="Notify $appname CareTeam"/>
	</fb:editor-buttonset>
	</fb:editor>
	<p>$appname will never add anyone to any CareTeam without mutual consent.</p>
 </fb:explanation>
</fb:fbml>
XXX;
}

echo $markup;
?>
