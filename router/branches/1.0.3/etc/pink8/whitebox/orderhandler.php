<?php
require_once('../whitebox/wbsubs.inc');
require_once('../whitebox/displayorderform.inc');
require_once('../whitebox/displayhomepage.inc');
require_once('../whitebox/mailsubs.inc');

function sendevite ($email,$fromparty,$tracking)
{
	if ($email=="") return;
$status = send_invite_tracking ($email ,$fromparty , $tracking); 
$dt = date('r'); //get fancy date;

     if ($status ==true )
     echo "<br>An invitation was sent on behalf of $fromparty on $dt via email to $email";
      else echo "<br>There was a problem sending mail to ".$email." please contact customer support";
}


if('submit' == cleanreq('submit')){
//ok, update everything and send the emails
$tracking = cleanreq('trackingNumber');
$email1 = cleanreq('email1');
$email2 = cleanreq('email2');
$email3 = cleanreq('email3');
//make sure we are good to go
if ($email1=="") { display_order_form(
$trackingNumber,
$guid,
$accountName,
$address,
$history='',
$comments='',
$partnererror='',
$mcerror=errortext('** you must include at least one email **'));
return;
}
wbheader('order',"Order Completion",true);//get a cheap header

$fromparty = $_SESSION['user'];

if ($fromparty =="") $fromparty = "Testman";

sendevite($email1,$fromparty,$tracking);
sendevite($email2,$fromparty,$tracking);
sendevite($email3,$fromparty,$tracking);

echo "<br>please close this window to continue";
}

?>
