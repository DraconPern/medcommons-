<?php
/*

// w l Donner

File: gupload.php
Abstract: MedCommons Image Portal Uploader


Disclaimer: IMPORTANT:  This MedCommons software is supplied to you by MedCommons Inc.
("MedCommons") in consideration of your agreement to the following terms, and your
use, installation, modification or redistribution of this MedCommons software
constitutes acceptance of these terms.  If you do not agree with these terms,
please do not use, install, modify or redistribute this MedCommons software.

In consideration of your agreement to abide by the following terms, and subject
to these terms, MedCommons grants you a personal, non-exclusive license, under
MedCommons's copyrights in this original MedCommons software (the "MedCommons Software"), to
use, reproduce, modify and redistribute the MedCommons Software, with or without
modifications, in source and/or binary forms; provided that if you redistribute
the MedCommons Software in its entirety and without modifications, you must retain
this notice and the following text and disclaimers in all such redistributions
of the MedCommons Software.
Neither the name, trademarks, service marks or logos of MedCommons Inc. may be used
to endorse or promote products derived from the MedCommons Software without specific
prior written permission from MedCommons.  Except as expressly stated in this notice,
no other rights or licenses, express or implied, are granted by MedCommons herein,
including but not limited to any patent rights that may be infringed by your
derivative works or by other works in which the MedCommons Software may be
incorporated.

The MedCommons Software is provided by MedCommons on an "AS IS" basis.  MEDCOMMONS MAKES NO
WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE IMPLIED
WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE, REGARDING THE MedCommons SOFTWARE OR ITS USE AND OPERATION ALONE OR IN
COMBINATION WITH YOUR PRODUCTS.

IN NO EVENT SHALL MEDCOMMONS BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION, MODIFICATION AND/OR
DISTRIBUTION OF THE MEDCOMMONS SOFTWARE, HOWEVER CAUSED AND WHETHER UNDER THEORY OF
CONTRACT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF
MedCommons HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Copyright (C) 2010 MedCommons Inc. All Rights Reserved.

*/

require_once "OAuth.php";





////////////////

function vtmdpost ($fields,$action)
{

	//echo "curl post to ".$action;

	$headers  =  array( "Content-type: application/x-www-form-urlencoded" );

	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, $action);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_TIMEOUT, 20);
	curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
	//curl_setopt($ch, CURLOPT_USERPWD, $your_username.':'.$your_password);
	curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);

	$data = curl_exec($ch);

	if (curl_errno($ch))print curl_error($ch);
	else curl_close($ch);

	return $data;
}

function sanitize($s)
{
	if (isset ($_POST[$s]))
	$s = mysql_real_escape_string($_POST[$s]);
	else
	$s ="--missing $s--";
	return $s;
}
//
// build html snippets for different files, add more as necessary
//
function build_patientid($patientid)
{
	return "<span class=vlabel>$patientid</span>";
}
function build_patientname($patientname)
{
	return "<span class=vlabel>$patientname</span>";
}
function build_orderstatuslink($orderstatuslink,$orderstatus)
{
	if (!$orderstatuslink)
	return "<span class=.vlabel>&nbsp;</span>"; else
	return "<a target='_evenanother' class=vhyperlink href='$orderstatuslink'>$orderstatus</a>";
}
function build_healthurl($healthurl)
{
	if (!$healthurl)
	return "<span class=.vlabel>&nbsp;</span>"; else
	return "<a class=vhyperlink href='$healthurl' target='_yetanother'>$healthurl</a>";
}
function build_orderref($requirelogin,$patientid,$hospital, $provider,$callback,$time,$customfields, $email)
{
// these are the basic order parameters as dragged thru from TIMC and with the "Custom Fields" and the new "showXXX" flags
	$h = z('h');
	$p = z('p'); 
	
	
	list($hn,$hlogo,$progind,$vn,$servicename,$labels,$groupAccountId) = ctx($h);
	
	$unext = $_SERVER['SCRIPT_URI'].("?home&h=$h");
	
	$emails = array();
	if($email) 
	    $emails[]=$email;
	if($provider->email)
	    $emails[]=$provider->email;
	
	$orderfields = array (
'next'=>$unext,
'respond'=>'XML',
'timestamp'=>$time,
'callers_order_reference'=>"ord$time",
'status_callback'=>$callback,
'patient_id'=>$patientid,
'protocol_id'=>'YF2',
'modality'=>'CT',
'due_date'=>'03/06/2009',
'due_time'=>'14:23',
'scan_date'=>'03/04/2009',
'scan_time'=>'09:00',
//'email'=>'N',
'baseline'=>'Y',
'source'=>'MedCommons',
'groupAccountId'=>$groupAccountId,
'upload_notification_email'=>implode(",",$emails)
	//
	);
	// add in any custom fields
	$customfieldnum = 0;
	foreach ($customfields as $cf)
	{
		$orderfields ["custom_0{$customfieldnum}"] = $cf;
		$customfieldnum++;
	}	
//

///'groupAccountId'=>'9092031177683982',	$l = array(0=>"Referring Hospital",1=>"Referring Physician",2=>"Referring Physician Email",3=>"Web/Video Conference",4=>"Referring Physician Phone",6=>"Clinical History",
//	                 8=>"Hospital/Practice Name",9=>"Consultant");
//$lsports = array(0=>"Imaging Center",1=>"Sender's Name",2=>"Sender's Email",3=>"Web/Video Conference",4=>"Contact Phone",6=>"Clinical History",
//	                 8=>"Sutton Team Portal",9=>"ATC");
	                 
//Referring Hospital,Referring Physician,Referring Physician Email,Web/Video Conference,Referring Physician Phone,,Clinical History,,Hospital/Practice Name,Consultant            
	                 
//Imaging Center,Sender's Name,Sender's Email,Web/Video Conference,Contact Phone,,Clinical History,,Sutton Team Portal,ATC                
	                 
	                 
	                 
	                 
	$l = explode(',',$labels); 

	if (0!=$hospital->showRefHospital)
	{
	    $orderfields["custom_00_label"]=$l[0];
	}
	if (0!=$hospital->showRefPhys)
	{
	    $orderfields["custom_01_label"]=$l[1];
	}

	if (0!=$hospital->showRefEmail)
	{
	    $orderfields["custom_02_label"]=$l[2];
	}
	if (0!=$hospital->showRefWebConf)
	{
	    $orderfields["custom_03_label"]=$l[3];
	}
	if (0!=$hospital->showRefPhone)
	{
	    $orderfields["custom_04_label"]=$l[4];
	}
	if (0!=$hospital->showClinicalHistory)
	{
	    $orderfields["custom_06_label"]=$l[6];
	}
	if (0!=$hospital->showPracticeName)
	{
		$orderfields["custom_08"]=$hn; 
	    $orderfields["custom_08_label"]=$l[8];
	}
//	if (0!=$hospital->showService)
//	{
//		$orderfields["custom_09"]=$servicename; 
//	    $orderfields["custom_09_label"]="Service";
//	}
	if (0!=$hospital->showConsultant)
	{
		$orderfields["custom_09"]=$provider->provider; 
	    $orderfields["custom_09_label"]=$l[9];
	}


		$upload = 'http://'.$_SERVER['HTTP_HOST']. '/orders/order'; // reset this for signed order
	// now build up the actual Order URL depending on whether signing or not
	if ($requirelogin)
	{
		global $consumer,$acc_token;  // these are common to each of the requests
		// Consumer - enter your application's token and secret here
		$consumer  = new OAuthConsumer("270ebdf10b9bb9dd957a4a14833367183a196da7", "72c25b142d4dd453213b586fc1278afc446b1d89", NULL);
		// Access Token - enter your the Access Token for the user you are calling here
		$acc_token = new OAuthToken("970efdf18b9bb9dd957a4a14833367283a116d37", "", 1);

		$req = OAuthRequest::from_consumer_and_token($consumer,$acc_token, "GET", $upload, $orderfields);
		$req->sign_request(new OAuthSignatureMethod_HMAC_SHA1(), $consumer, $acc_token);
		$url_target = $req->to_url();  // get the nicely signed URL
	}
	else
	{
		
		$args = '?args';
		foreach ($orderfields as $key=>$value) { $value= urlencode($value); $args .= "&$key=$value"; }
		$url_target = $upload.$args;
	}


	// return the URL as a link!, another choice would bne to just do it

	return $url_target;
}
// custom email notifications sent at end of upload process when 'done' button clicked

function send_upload_confirm_to_consultant($consultantemail)
{
{
			// send actual email
			//if(!($me=Gtestif_logged_in())) die("You must be logged in to perform this function"); else
			//list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;
		
	$h = z('h');
	
	$pname=  z('pname');
	

	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);

			//send a cheap and cheerful email right from here

			$message = "You have received a request for a consultation for Patient $pname\r\nLogin to your portal to view this case";
			// In case any of our lines are larger than 70 characters, we should use wordwrap()
			$message = wordwrap($message, 70);
			$headers = "From: public CD uploader" . "\r\n" .
			"BCC: cso@medcommons.net";


	$badge ='';//
	//"<span id=provider>$email&nbsp;&nbsp;<a id=settings href='settings.php?h=$h' >settings</a>&nbsp;&nbsp;<a id=logout href='glogout.php?h=$h' >logout</a></span>";// intended for inside a span
	
				$out =<<<XXX
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MedCommons CD Upload Complete - Consultant Notification</title>
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
			if (mail($uploadersemail, "You have received a consultation request for $pname", $message,$headers))			
			$out .= email_notice($email,$provider->email,$message,$pr);

 else $out .= "Could not send email re $pname to $targetemail<br/>"; 

 $out .="</div></body></html>";
			return $out;
}
	}
function send_upload_confirm_to_uploader($h,$pname,$uploadersemail)
{

	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);

			//send a cheap and cheerful email right from here

			$message = "Your Upload for Patient $pname is complete\r\n";
			// In case any of our lines are larger than 70 characters, we should use wordwrap()
			$message = wordwrap($message, 70);
			$headers = "From: public CD uploader" . "\r\n" .
			"BCC: cso@medcommons.net";


	$badge ='';//
	//"<span id=provider>$email&nbsp;&nbsp;<a id=settings href='settings.php?h=$h' >settings</a>&nbsp;&nbsp;<a id=logout href='glogout.php?h=$h' >logout</a></span>";// intended for inside a span
	
				$out =<<<XXX
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MedCommons CD Upload Complete - Notification to Uploader</title>
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
			if (mail($uploadersemail, "You have received a consultation request for $pname", $message,$headers))			
			$out .= email_notice($email,$provider->email,$message,$pr);

 else $out .= "Could not send email re $pname to $targetemail<br/>"; 

 $out .="</div></body></html>";
			return $out;
}



/**
 * ********REFERRING PHYSICIAN CONTACT AND UPLOAD PAGE
 *
 * This prompts for all the fields prior to uploading to MedCommons
 */
function contact_and_upload_handler()
{

	$h = z('h');
	$p = z('p');
	$refhospital = z('Hospital'); // these are the uploader's input fields
	$refphys = z('Ref');
	$videourl = z('Video');
	$phone = z('Phone');
	$pref = z('radio_call');
	$clinical = z('Clinical');
	$email= z('Email');
	

	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);

	$providers = dosql("select * from  aProviders p  where p.ind='$p'");
	
	if (!($provider = mysql_fetch_object($providers))) return false;
	
	$hospitals = dosql("select * from  aHospitals p  where p.ind='$h'");
	
	if (!($hospital =mysql_fetch_object($hospitals))) return false;

	$orderref = build_orderref (($progind==3),'BILLDONNER',$hospital,$provider,"doPosts",time(), array(
	'custom_00'=>$refhospital,
	'custom_01'=>$refphys,
		'custom_02'=>$email,
		'custom_03'=>$videourl,
		'custom_04'=>$phone,
		'custom_05'=>$pref,
		'custom_06'=>$clinical,
		'custom_07'=>$provider->mcid,
	
	), $email);

	$pn = $provider->provider;
	$defaultfields = array (
"Text"=>"checked='checked'",
"Skype"=>"checked='checked'",
"Ref"=>"value=''");
$crumbs = //"<a href='?home&h=$h'>admin home</a> >".
	" <a href='?home&h=$h'>Directory</a> > Order";
	$substitutions = array (
	'$$$programind$$$'=>$p, // pass into the callback handler	
	'$$$practiceind$$$'=>$h, // pass into the callback handler
	'$$$buttonlabel$$$'=>"Confirm Upload to $pn",
	'$$$uploadlink$$$'=>$orderref,
	'$$$consultant$$$'=>$pn,
	'$$$logourl$$$'=>$hlogo,
	'$$$practice$$$'=>$hn,
	'$$$program$$$'=>$vn,
	'$$$crumbs$$$'=>$crumbs,
	);

	$errdecorators = array (
'_errormaintext'=>"Pl",
'_errormainclass'=>'hide', //class=hideerror to hide error field
	); // or class=hideerror to hide error field

	
  if($progind==4) { 
    'htm/guploadconfirmsports.htm';

    // bill 19 mar 10 - when Done! clicked go to upload_done page instead of signin
    $page = ($progind==4)?'htm/guploadconfirmsports.htm':'htm/guploadconfirm.htm';
    return loadpage($page, "?handler=upload_done&h=$h&email=$email&p=$p&pname=$pn", $defaultfields, $substitutions, $errdecorators);
  }
  else {
      header("Location: $orderref"); 
  }
}
function upload_done_handler()
{
	$h = z('h');
	

		
	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	


	$uploadersemail= z('email');
	
	if (isset($_REQUEST['p'])) // if provider was supplied then 
	{
	$p = z('p');
	$providers = dosql("select * from  aProviders p  where p.ind='$p'");
	
	if (($provider = mysql_fetch_object($providers))) 
	$providersemail = $provider->email;
	}
	else $providersemail = '';
	
	
	// if we have email address for the consultant then send him an email
if ($providersemail!='')
	echo send_upload_confirm_to_consultant($providersemail);
	
	// if we have email address for the uploader then send him an email
if ($uploadersemail!='')
	echo 	send_upload_confirm_to_uploader($uploadersemail);
		
	// now go to the public page
	echo "done email would redirect to h $h";
	exit;
	header ("Location: ?public&h=$h");
}

function contact_and_upload_page($errdecorators=array())
{
		$defaultfields = array (
"Text"=>"checked='checked'",
"Skype"=>"checked='checked'",
"Ref"=>"value=''");
		
	//return array($hospital->hospital,$hospital->logourl,$hospital->indprogram,$hospital->program,$hospital->headers,$hospital->labels,$hospital->groupAccountID);
	$h = z('h');
	list($hn,$hlogo,$progind,$vn,$headers,$labels,$groupaccid) = ctx($h);
		
	$p = z('p');
	$providers = dosql("select * from  aProviders p  where p.ind='$p' ");
	if (!($provider =mysql_fetch_object($providers))) return false;
	$pn = $provider->provider;

	
	if ($progind==3) // restricted template requires login
	{
	if(!($me=Gtestif_logged_in())) please_login(); else
	list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;

	$itsme = dosql ("Select * from aProviders where mcid='$accid' and hind='$h' "); // find self as provider
	if (!($r = mysql_fetch_object($itsme))) die ("MedCommons Imaging Portal cant find MedCommons ID $accid");

$crumbs = //"<a href='?home&h=$h'>admin home</a> >".
	" <a href='?home&h=$h'>Directory</a> > Order";
	$substitutions = array (
	'$$$programind$$$'=>$p, // pass into the callback handler	
	'$$$practiceind$$$'=>$h, // pass into the callback handler
	'$$$buttonlabel$$$'=>"Request Consultation with $pn",
	'$$$consultant$$$'=>$pn,
	'$$$logourl$$$'=>$hlogo,
	'$$$practice$$$'=>$hn,
	'$$$program$$$'=>$vn,
	'$$$crumbs$$$'=>$crumbs,
	'$$$defaultHospital$$$'=>$r->defaultHospital,
	'$$$defaultRefPhys$$$'=>$r->defaultRefPhys,
	'$$$defaultEmailContact$$$'=>$r->defaultEmailContact,	
	'$$$defaultPhone$$$'=>$r->defaultPhone,
	'$$$defaultVideoURL$$$'=>$r->defaultVideoURL,
	
	);
	}
	else
	{ // other templates have no login
$crumbs = //"<a href='?home&h=$h'>admin home</a> >".
	" <a href='?home&h=$h'>Directory</a> > Order";
	$substitutions = array (
	'$$$programind$$$'=>$p, // pass into the callback handler	
	'$$$practiceind$$$'=>$h, // pass into the callback handler
	'$$$buttonlabel$$$'=>"Request Consultation with $pn",
	'$$$consultant$$$'=>$pn,
	'$$$logourl$$$'=>$hlogo,
	'$$$practice$$$'=>$hn,
	'$$$program$$$'=>$vn,
	'$$$crumbs$$$'=>$crumbs,
	'$$$defaultHospital$$$'=>'',
	'$$$defaultRefPhys$$$'=>'',
	'$$$defaultEmailContact$$$'=>'',	
	'$$$defaultPhone$$$'=>'',
	'$$$defaultVideoURL$$$'=>'',
	
	);	
	}
	
$page = ($progind==4)?'htm/guploadsports.htm':'htm/gupload.htm';
	$errdecorators = array (
'_errormaintext'=>"",
'_errormainclass'=>'hideerror', 
	); // or class=hideerror to hide error field
	return loadpage($page, "?handler=contact_and_upload_handler",
	$defaultfields, $substitutions, $errdecorators);
}

function contact_and_upload()
{
	$h = z('h');
	echo contact_and_upload_page($h);
}


?>