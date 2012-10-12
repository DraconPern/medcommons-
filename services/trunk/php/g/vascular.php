<?php
/*

// w l Donner

File: vascular.php -- intended to be installed as .../vascular/index.php

Abstract: Custom Upload Form and Dispatcher for Individual Case Access


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
	$s = htmlentities($_POST[$s]);
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
function build_orderref($requirelogin,$patientid,$hospital, $provider,$callback,$time,$customfields, $emails)
{

$unext ='backtomedcommons.html'; //urlencode ("http://www.medcommons.net");


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
'groupAccountId'=>'9244287615706818', //$groupAccountId,
'upload_notification_email'=>implode(",",$emails)
	//
	);
	// add in any custom fields
	$customfieldnum = 0;
	foreach ($customfields as $key=>$value)
	{
		$orderfields [$key] = $value;
		$customfieldnum++;
	}


	$orderfields["custom_00_label"]='Facility';

	$orderfields["custom_01_label"]='Physician Name';

	$orderfields["custom_02_label"]='Physician Email';
	 
	$orderfields["custom_06_label"]='Notes'; // tinkered to match other portals so emails are good without tweaking

	$orderfields["custom_05_label"]='Name';

	$orderfields["custom_07_label"]='Email';


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

				


function handleform()
{
	
	$fromname = sanitize('fromname');
	$fromfacility = sanitize('fromfacility');
	$fromemail = sanitize('fromemail');
	$fromnotes = sanitize('fromnotes');
	$toname = sanitize('toname');
	$toemail = sanitize('toemail');

	$orderref = build_orderref (false,'BILLDONNER',$fromfacility,$fromname,"doPosts",time(), array(
	    'custom_00'=>$fromfacility,
	    'custom_01'=>$fromname,
		'custom_02'=>$fromemail,
	    'custom_05'=>$toname,
		'custom_07'=>$toemail,
		'custom_06'=>$fromnotes   // clinical field elsewhere
	),
	array($fromemail,$toemail));


	$defaultfields = array (
	);



	$errdecorators = array (
'_errormaintext'=>"Pl",
'_errormainclass'=>'hide', //class=hideerror to hide error field
	); // or class=hideerror to hide error field




	$html = <<<XXX

	<!DOCTYPE HTML>
	<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>MedCommons Vascular  Cloud Portal Confirm Upload</title>
	<link rel="stylesheet" type="text/css"  href="css/gstyle.css"/>
	<link rel="shortcut icon" href="images/favicon.gif" type="image/gif"/>
	<meta name="viewport" content="minimum-scale=0.6, width=device-width, maximum-scale=1.6, user-scalable=yes">
	<meta name="apple-mobile-web-app-capable" content="YES">
	<link rel="apple-touch-icon" href="mcportal.png">

	</head>
	<body>
	<img onClick="window.location='?home&h=30'" src='http://www.medcommons.net/images/logoHeader.gif' />
	<div><span id=hospital>Vascular  Cloud Portal</span></div>
	<br/>
	<button style="width:65;height:55" onClick="window.location='?cancelled"><b>Cancel</b></button>&nbsp;&nbsp;&nbsp;&nbsp;
	<button style=" width:65;height:95" onClick="window.location='$orderref' "><b>Upload CD to Vascular Trials Cloud Portal</b></button>

	<p>
The order we are placing is precisely:
</p>
	$orderref
	<p>
This will be removed when we are all done.
</p>
	</pre>

</body>
</html>




XXX;


		

	echo $html;
}


function contact_and_upload_page()
{

	$html = <<<XXX
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Welcome to MedCommons Vascular  Cloud Portal</title>
 <link rel="stylesheet" type="text/css"  href="css/gstyle.css"/>
        <link rel="shortcut icon" href="images/favicon.gif" type="image/gif"/>
<meta name="viewport" content="minimum-scale=0.6, width=device-width, maximum-scale=1.6, user-scalable=yes">
    <meta name="apple-mobile-web-app-capable" content="YES">
    <link rel="apple-touch-icon" href="mcportal.png">

</head>
<body>
<img onClick="window.location='?home&h=30'" src='http://www.medcommons.net/images/logoHeader.gif' />
<div><span id=hospital>Vascular  Cloud Portal</span></div>
<br/>
<form id=payment method=post action='?handleform'>
<input type=hidden value=5 name=p />
<input type=hidden value=30 name=h />
	<div class='_errormainclass' ></div> 
	<fieldset>
		<legend>Welcome to MedCommons Vascular  Cloud Portal</legend>
		<p>Please fill out the following information, insert CD and push CONTINUE button.</p><p> Thank you!. </p>
	</fieldset>
	<fieldset>
		<legend>This scan is being sent by:</legend>
		<ol>
		
			<li>
				<label for=fromname>Physician's Name</label>
				<input id=fromname name=fromname type=text >
			</li>
			<li>
				<label for=fromfacility>Facility</label>
				<input id=fromfacility name=fromfacility type=text  >
			</li>
		
			<li>
				<label for=fromemail>Email</label>
				<input id=fromemail name=fromemail type=email >
			</li>
			
			<li>
			
				<label for=fromnotes>Notes (optional) </label>
					<textarea rows=6 cols=40  name="fromnotes" id="fromnotes" /></textarea>
			</li>
	
			</ol>
			</fieldset>
	

	<fieldset>
		<legend>Please upload this scan to: </legend>
		<ol>
				<li>
				<label for=toname>Name</label>
				<input id=toname name=toname type=text >				
				</li>
				<li>
				<label for=toemail>Email</label>
				<input id=toemail name=toemail type=email >				
				</li>
					</ol>
				</fieldset>
			</li>
			
		</ol>
	</fieldset>
	<fieldset>
		<legend>Please Insert CD and click CONTINUE</legend>
		
	</fieldset>
	

	<fieldset>
		<button type=submit>CONTINUE</button>
	</fieldset>
</form>
</body>
</html>

XXX;

	return $html;

}

if (isset($_REQUEST['handleform'])) handleform(); else
echo contact_and_upload_page();


?>