<?php
//require_once "mainlib.inc.php";

//
// CUSTOMIZED FOR SHARING VIA SMS
//
$thisURL = "smsshareiphone.php";
$mainTitle = "Share via SMS";
$msgType = "sms message";
$viaType = "SMS";
$toLabel = "to phone(s)";
$pickupURL = "https://medcommons.net/pickup.php";
$infieldAppleType = "tel";




	function xpost ($content)
	{
		global $pennysmsurl;
		$headers  =  array( "Content-type: text/xml" );
		$ch = curl_init();
		curl_setopt($ch, CURLOPT_URL, $pennysmsurl );
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($ch, CURLOPT_TIMEOUT, 20);
		curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $content);

		$data = curl_exec($ch);

		if (curl_errno($ch)) error_exit( curl_error($ch));
		else curl_close($ch);

		return $data;
	}
	


function dopost ($from,$to,$message,$refphys)
{
global $pickupURL,$viaType,$pennysmskey;
   //$from = str_replace('+','plus',$from);
	$body = <<<MSG
<?xml version="1.0"?>
<methodCall>
<methodName>send</methodName>
  <params>
	<param>
	<value><string>$pennysmskey</string></value>
	</param>
	<param>
	<value><string>cso@medcommons.net</string></value>
	</param>
	<param>
	<value><string>$to</string></value>
	</param>
	<param>
	<value><string>$message</string></value>
	</param>
  </params>
</methodCall>
MSG;
	// post out to penny sms and analyze what we got back
	$response = xpost($body); // just pass back whatever actually happens
	$pos1 = strpos($response,'<string>Error:');
	if ($pos1===false)
	{
		$msg = <<<XXX
		
<h2>You just sent an sms msg to referring physician $refphys</h2>
<p>From: $from</p>
<p>To: $to</p>
<p>Message: $message</p>

XXX;

	}

	else
	{
		$pos2 = strpos ($response,'</string>',$pos1);
		if ($pos2===false) error_exit('Invalid or Unterminated Error Msg from Remote Service');
		else
		$msg = substr ($response,$pos1+14,$pos2-$pos1-14);
		$msg = "<h2>An error occurred while sending the $viaType message to $from: </h2>	<div style='border:1px solid black; padding:10px'>
		<h2>$msg</h2>
		</div>";
	}

	$html = <<<XXX

	<body>
	<form id="settings" title="send sms message with sharing info" class="panel" selected="true" >
	$msg
</form></body></html>
XXX;
	return $html;

}


function sms_send_msg()
{
			// show the consultants filtered dashboard
	if(!($me=Gtestif_logged_in())) please_login(); else
	list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
	$h = z('h');
	
	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	
	global $pickupURL;
	$refphys = h('refphys');
	$from = h('from');
	$to = h('phone');
	$msg = "New report ".urldecode(h('pname')). " Tracking:12345678 PIN:11111 at $pickupURL";
	
    $body= dopost($from,$to,$msg,$refphys);
    
    	$data = file_get_contents("htm/gblankpage.htm");
    	
	$badge ="<span id=provider>$email&nbsp;<a id=settings href='settings.php?h=$h' >settings</a>&nbsp;&nbsp;<a id=logout href='glogout.php?h=$h' >logout</a></span>";// intended for inside a span
	
	$crumbs = "<span ><a href='?home&h=$h'>Home</a>";

	$body = str_replace(array('$$$body$$$','$$$practice$$$','$$$logourl$$$','$$$program$$$','$$$badge$$$','$$$crumbs$$$'),
	array($body,$hn,$hlogo,$vn,$badge,$crumbs),
	$data); // replace body of template


	return $body;

}

function tel_call_msg()
{
			// show the consultants filtered dashboard
	if(!($me=Gtestif_logged_in())) please_login(); else
	list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
	$h = z('h');
	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	global $pickupURL;
	$refphys = h('refphys');
	$from = h('from');
	$to = h('phone');
	$msg = "New report ".urldecode(h('pname')). " Tracking:12345678 PIN:11111 at $pickupURL";
	
    
    		$body = <<<XXX
		
<h2>You have been requested to call referring physician $refphys at $to</h2>

<p>Message: $msg</p>

XXX;

    
    	$data = file_get_contents("htm/gblankpage.htm");

	$badge ="<span id=provider>$email&nbsp;&nbsp;<a id=settings href='settings.php?h=$h' >settings</a>&nbsp;&nbsp;<a id=logout href='glogout.php?h=$h' >logout</a></span>";// intended for inside a span
	

	$body = str_replace(array('$$$body$$$','$$$practice$$$','$$$logourl$$$','$$$program$$$','$$$badge$$$'),
	array($body,$hn,$hlogo,$vn,$badge),$data); // replace body of template


	return $body;

}
?>