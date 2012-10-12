<?php
function email_notice ($from,$to,$message,$cons)
{


		$msg = <<<XXX
<div id=email_sent_screen>		
<h2>You just sent an email msg to your fellow consultant $cons</h2>
<p>From: $from</p>
<p>To: $to</p>
<p>Message: $message</p>

XXX;

return $msg;

	}



function send_forward_page()
{
			// send actual email
			if(!($me=Gtestif_logged_in())) die("You must be logged in to perform this function"); else
			list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
		
	$h = z('h');
	$p = z('p');
	$targetid = z('t');
	$colleague = z('colleague');
	$pname=  z('pname');
	

	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);

	$providers = dosql("select * from  aProviders p  where p.ind='$p' ");
	if (!($provider =mysql_fetch_object($providers))) return false;
		
 $pi = $targetid;
$pr = $provider->provider;
			//send a cheap and cheerful email right from here

			$message = "$pr\r\nYour colleague  $colleague at $hn forwarded a MedCommons HealthURL for Patient $pname at http://{$_SERVER['HTTP_HOST']}/$pi\r\n";
			// In case any of our lines are larger than 70 characters, we should use wordwrap()
			$message = wordwrap($message, 70);
			$headers = "From: $email" . "\r\n" .
			"BCC: cso@medcommons.net";


	$badge ="<span id=provider>$email&nbsp;&nbsp;<a id=settings href='settings.php?h=$h' >settings</a>&nbsp;&nbsp;<a id=logout href='glogout.php?h=$h' >logout</a></span>";// intended for inside a span
	
				$out =<<<XXX
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MedCommons Imaging Portal Dashboard</title>
 <link rel="stylesheet" type="text/css"  href="css/gstyle.css"/>
        <link rel="shortcut icon" href="favicon.gif" type="image/gif"/>
<meta name="viewport" content="minimum-scale=0.6, width=device-width, maximum-scale=1.6, user-scalable=yes">
    <meta name="apple-mobile-web-app-capable" content="YES">
    <link rel="apple-touch-icon" href="mcportal.png">

</head>
<body>
<img src='$hlogo' alt='missing $hlogo' />
<div><span id=hospital>$hn</span>&nbsp;&nbsp;&nbsp;&nbsp;<span id=program>$vn</span>&nbsp;&nbsp;&nbsp;&nbsp;
		$badge</div>
		<br/>
XXX;

			// Send and append a success message
			if (mail($provider->email, "Forward of case $pname", $message,$headers))			
			$out .= email_notice($email,$provider->email,$message,$pr);

 else $out .= "Could not send email re $pname to $pr {$provider->email}<br/>"; 

 $out .="</div></body></html>";
			return $out;
}




function send_forward_page()
{
			// send actual email
			if(!($me=Gtestif_logged_in())) die("You must be logged in to perform this function"); else
			list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
		
	$h = z('h');
	$p = z('p');
	$targetid = z('t');
	$colleague = z('colleague');
	$pname=  z('pname');
	

	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);

	$providers = dosql("select * from  aProviders p  where p.ind='$p' ");
	if (!($provider =mysql_fetch_object($providers))) return false;
		
 $pi = $targetid;
$pr = $provider->provider;
			//send a cheap and cheerful email right from here

			$message = "$pr\r\nYour colleague  $colleague at $hn forwarded a MedCommons HealthURL for Patient $pname at http://{$_SERVER['HTTP_HOST']}/$pi\r\n";
			// In case any of our lines are larger than 70 characters, we should use wordwrap()
			$message = wordwrap($message, 70);
			$headers = "From: $email" . "\r\n" .
			"BCC: cso@medcommons.net";


	$badge ="<span id=provider>$email&nbsp;&nbsp;<a id=settings href='settings.php?h=$h' >settings</a>&nbsp;&nbsp;<a id=logout href='glogout.php?h=$h' >logout</a></span>";// intended for inside a span
	
				$out =<<<XXX
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MedCommons Imaging Portal Dashboard</title>
 <link rel="stylesheet" type="text/css"  href="css/gstyle.css"/>
        <link rel="shortcut icon" href="favicon.gif" type="image/gif"/>
<meta name="viewport" content="minimum-scale=0.6, width=device-width, maximum-scale=1.6, user-scalable=yes">
    <meta name="apple-mobile-web-app-capable" content="YES">
    <link rel="apple-touch-icon" href="mcportal.png">

</head>
<body>
<img src='$hlogo' alt='missing $hlogo' />
<div><span id=hospital>$hn</span>&nbsp;&nbsp;&nbsp;&nbsp;<span id=program>$vn</span>&nbsp;&nbsp;&nbsp;&nbsp;
		$badge</div>
		<br/>
XXX;

			// Send and append a success message
			if (mail($provider->email, "Forward of case $pname", $message,$headers))			
			$out .= email_notice($email,$provider->email,$message,$pr);

 else $out .= "Could not send email re $pname to $pr {$provider->email}<br/>"; 

 $out .="</div></body></html>";
			return $out;
}
?>