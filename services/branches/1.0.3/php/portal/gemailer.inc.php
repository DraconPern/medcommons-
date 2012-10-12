<?php
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

	$providers = dosql("select * from  aHospitals, aServices s,aJoined j,aProviders p  where p.ind='$p' and j.hospitalind='$h'
	and j.providerind=p.ind and j.serviceind=s.ind ");
	if (!($provider =mysql_fetch_object($providers))) return false;
		
 $pi = $targetid;
$pr = $provider->provider;
			//send a cheap and cheerful email right from here

			$message = "$pr\r\nYour colleague  $colleague at $hn forwarded a MedCommons HealthURL for Patient $pname at http://ci.myhealthespace.com/$pi\r\n";

			// In case any of our lines are larger than 70 characters, we should use wordwrap()
			$message = wordwrap($message, 70);
			$headers = "From: $email" . "\r\n" .
			"BCC: cso@medcommons.net";
			// Send and append a success message
			if (mail($provider->email, "Forward of case $pname", $message,$headers))



			$out = <<<XXX
			<div id=email_sent_screen>

			<p>Case $pname  was forwarded to $pr {$provider->email}</p>
		
</div>
XXX;
 else $out = "Could not send email re $pname to $pr {$provider->email}<br/>"; 
			
			return $out;
}
?>