<?PHP
function send_new_password ($towhom,$uid,$password)
{
	$srv = "http://".$_SERVER['SERVER_NAME']."/"
	     .$GLOBALS['DB_Database'].
	     "/whitebox/index.php?user=".$towhom;
$message = <<<XXX
<HTML><HEAD><TITLE>MedCommons New Password Confirmation for $towhom</TITLE>
<META http-equiv=Content-Type content="text/html; charset=iso-8859-1">
</HEAD>
<BODY>
<p>
--------------------------------------------------------------------------------
<br><br>
Thank you for using MedCommons.

<p>You may login as $uid
<br>Your new password is $password
<br>
<br><a href=$srv>login to MedCommons</a>
</BODY>
</HTML>
XXX;


$stat = @mail($towhom, "MedCommons New Password Confirmation for ".$towhom, 
		$message,
//		"Mime-Version: 1.0\r\n".
     "From: MedCommons@{$_SERVER['SERVER_NAME']}\r\n" .
     "Reply-To: cmo.medcommons.org\r\n" .
     "bcc: cmo@medcommons.org\r\n".
//     "X-Mailer: PHP/" . phpversion().
     "Content-Type: text/html; charset= iso-8859-1;\r\n"//must come last
     );
     
     return $stat;
}

function send_invite_registration ($towhom,$uid,$password,$tracking)
{
	$srv = "http://".$_SERVER['SERVER_NAME']."/"
	     .$GLOBALS['DB_Database'].
	     "/whitebox/index.php?user=".$uid.
	     	     "&amp;tracking=$tracking&amp;password=$password;&amp;autolog=true";
$message = <<<XXX
<HTML><HEAD><TITLE>MedCommons Registration Confirmation for $towhom</TITLE>
<META http-equiv=Content-Type content="text/html; charset=iso-8859-1">
</HEAD>
<BODY>
<p>
--------------------------------------------------------------------------------
<br><br>
Thank you for registering with MedCommons.

<p>You may login as $uid
<br>Your password is $password
<br>
<br><a href=$srv>login to MedCommons</a>
</BODY>
</HTML>
XXX;


$stat = @mail($towhom, "MedCommons Registration Confirmation for ".$towhom, 
		$message,
//		"Mime-Version: 1.0\r\n".
     "From: MedCommons@{$_SERVER['SERVER_NAME']}\r\n" .
     "Reply-To: cmo.medcommons.org\r\n" .
     "bcc: cmo@medcommons.org\r\n".
//     "X-Mailer: PHP/" . phpversion().
     "Content-Type: text/html; charset= iso-8859-1;\r\n"//must come last
     );
     
     return $stat;
}
function send_invite_tracking($towhom,$fromwhom,$tracking)
{
	$trackingurl =	 "http://".$_SERVER['SERVER_NAME'].
			"/".$GLOBALS['DB_Database'].
			"/whitebox/index.php?tracking=".$tracking;

	
$message = <<<XXX
<HTML><HEAD><TITLE>MedCommons Review Request $tracking from $fromwhom</TITLE>
<META http-equiv=Content-Type content="text/html; charset=iso-8859-1">
</HEAD>
<BODY>
<p>
--------------------------------------------------------------------------------
<br><br>
Subject: MedCommons Review Request <a href=$trackingurl><b>$tracking</b></a> from $fromwhom
<br><br>

--------------------------------------------------------------------------------
<br>
Study <a href=$trackingurl><b>$tracking</b></a> is available at MedCommons at the request of $fromwhom.
<p>
History:

<p>    
HIPAA Security and Privacy Notice: The Study referenced in this 
invitation contains Protected Health Information (PHI) covered under 
the HEALTH INSURANCE PORTABILITY AND ACCOUNTABILITY ACT OF 1996 (HIPAA).
The MedCommons user sending this invitation has set the security 
requirements for your access to this study and you may be required to 
register with MedCommons prior to viewing the PHI. Your access to this 
Study will be logged and this log will be available for review by the 
sender of this invitation and authorized security administrators. 
For more information about MedCommons privacy and security policies, 
please visit <a href="http://www.medcommons.net" target=_blank>www.medcommons.net</a>

</BODY>
</HTML>
XXX;


$stat = @mail($towhom, "MedCommons Review Request from ".$fromwhom, 
		$message,
//		"Mime-Version: 1.0\r\n".
     "From: MedCommons@{$_SERVER['SERVER_NAME']}\r\n" .
     "Reply-To: cmo.medcommons.org\r\n" .
     "bcc: cmo@medcommons.org\r\n".
//     "X-Mailer: PHP/" . phpversion().
     "Content-Type: text/html; charset= iso-8859-1;\r\n"//must come last
     );
     
     return $stat;
}


?>