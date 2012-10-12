<?php

function sendemail($email,$blurb)
{   
 	$message = <<<XXX

<HTML><HEAD><TITLE>MedCommons Status Alert Notification for $email</TITLE>
<META http-equiv=Content-Type content="text/html; charset=iso-8859-1">
</HEAD>
<BODY>
<p><small>Something is apparently wrong with our service<br>
----------------------------------------------------------------------------
<br></small>$blurb<br>
<br>
Please get over to <a href=https://secure.medcommons.net/memberservice/netstat.php>https://secure.medcommons.net/memberservice/netstat.php</a><br>
</BODY>
</HTML>
XXX;
				 	
							 
				 
$stat = @mail($email, "Status Alert From MedCommons", 
		$message,
     "From: MedCommons@{$_SERVER['SERVER_NAME']}\r\n" .
     "Reply-To: cmo.medcommons.net\r\n" .
     "bcc: cmo@medcommons.net\r\n".
     "Content-Type: text/html; charset= iso-8859-1;\r\n"
     );
if($stat) return "SENTALERT email to $email <br>"; else return "email send failure to $email <br> $message";
}

function alupdatefield($email)
{ 
$t=time();
$update = <<<VVV
	UPDATE alerted 
		SET last = '$t'
		WHERE (email = '$email')
VVV;


	$result = mysql_query($update) or
	die("alupdatefield failed : ".mysql_error());

}
	// if any of the gateways are not in perfect shape, then check to see if anyone needs alerting
	if ($errcount>0){
		$query = "SELECT * from alerted";
		
 	$result = mysql_query ($query) or die("can not query table alerted - ".mysql_error());
 	$count=0;
	if ($result=="") {echo "?no parties to alert"; exit;}
	
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
			
		$email = $l['email'];
		$last = $l['last'];
		$frequency = $l['frequency'];
		$count++;
		$now=time(); //time now in seconds
//		echo "checking ".$last." ".$frequency." ".$now."<br>";
		if (($last+60*$frequency)<=$now){ //if long ago then
		//RENOTIFY
		echo sendemail($email,$blurb);
		alupdatefield($email);;
		}
        }
	mysql_free_result($result);
	}
	
	?>