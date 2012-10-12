<?php
require_once "ns.inc.php";
class erefnotifier extends notifier {

	// send an eref notification, conform to old argument list

	function send_message
	( &$message,
	$mcid,
	$template,
	$recipient,
	$subjectline,
	$a,$b,$c,$d,$e,$f,$g
	)
	{
		$trackingnum = $a; // must be first if present
		$homepageurl = $GLOBALS['Homepage_Url'];

		$homepagehtml= "<a href=$homepageurl>$homepageurl</a>";
		
		if ($f!="") $trackingurl =$f; else
		$trackingurl = $GLOBALS['Tracking_Url'];
		
		$prettytracking = $this->pretty_tracking($trackingnum);
		$merge = $this->merge_tracking_mcid($trackingnum,$mcid);
		$trackinghtml = "<a href=$trackingurl?a=$merge>$prettytracking</a>";
		$mcidhtml = $this->pretty_mcid($mcid);

		$message = <<<XXX

		
<HTML><HEAD><TITLE>eReferral $trackingnum</TITLE>
<META http-equiv=Content-Type content="text/html; charset=iso-8859-1">
</HEAD>
<BODY>
<img src=http://www.medcommons.net/images/smallwhitelogo.gif />
<p>
eReferral $trackinghtml is available at MedCommons. Please click on the tracking number link to access. Be prepared to supply a PIN or a valid user registration to access patient information.
<p>
Sender Comment:
<br>
<p>
$b
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
please visit $homepagehtml
</BODY>
</HTML>
XXX;
		$srv = $_SERVER['SERVER_NAME'];
		$stat = @mail($recipient, $subjectline,
		$message,
		"From: MedCommons@{$srv}\r\n" .
		"Reply-To: cmo.medcommons.net\r\n" .
		"bcc: cmo@medcommons.net\r\n".
		"Content-Type: text/html; charset= iso-8859-1;\r\n"
		);
		if($stat) return "ok $srv"; else return "send mail failure from $srv";
	}

}

//main program
$e = new erefnotifier();
echo $e->notify();
?>