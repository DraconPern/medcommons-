<?php
/*

// w l Donner

File: up.php
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

function z($x)
{
	if (!isset($_REQUEST[$x])) die ("Must supply $x");
	return ($_REQUEST[$x]);
}

function build_orderref($args,$withOauth)
{
	// these are the basic order parameters as dragged thru from TIMC and with the "Custom Fields" and the new "showXXX" flags

	$time = time();
	


	$orderfields = array (

	'timestamp'=>$time,
	'callers_order_reference'=>"ord$time",

	//
	);
	// add in any custom fields

	foreach ($args as $key=>$value)
	{
		$orderfields [$key] = $value;
	}

	$upload = 'http://'.$_SERVER['HTTP_HOST']. '/orders/order'; // reset this for signed order
	// now build up the actual Order URL depending on whether signing or not
	if ($withOauth)
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


/**
 *
 *
 * Grabs all fields from a standard order form and Posts to the MedCommons Portal Upload Service
 */
function makeUploadRequest($withOauth)
{

	$uploadURL = z('UploadURL');
	$groupID = z('GroupID');
	$consultantID = z('ConsultantID');
	$consultantEmail = z('ConsultantEmail');
	$facility = z('Facility'); // these are the uploader's input fields
	$videourl = z('VideoURL');
	$phone = z('SenderPhone');
	$sms = z('SenderSMS');
	$clinical = z('Clinical');
	$email= z('SenderEmail');
	$name = z('SenderName');
	$consultantid = z('ConsultantID');
	$patientid = z('PatientID');
	$next = 'http://www.medcommons.net';
	if(isset($_REQUEST['next']))
    	$next = z('next');
	    
	$emailist = array();
	if ($consultantEmail!='') $emailist [] = $consultantEmail;
	if ($email!='') $emailist [] = $email;
	$arglist = array(

		'custom_00'=>$facility,
		'custom_01'=>$name,
		'custom_02'=>$email,
		'custom_03'=>$videourl,
		'custom_04'=>$phone,
		'custom_05'=>$sms,
		'custom_06'=>$clinical,
		'custom_07'=>$consultantid,

		'Facility'=>$facility,
		'SenderName'=>$name,
		'SenderEmail'=>$email,
		'VideoURL'=>$videourl,
		'SenderPhone'=>$phone,
		'SenderSMS'=>$sms,
		'Clinical'=>$clinical,
		'ConsultantID'=>$consultantid,


		'patient_id'=>$patientid,
		'groupAccountId'=>$groupID,

     	'next'=>$next,
     	'respond'=>'XML',
		'status_callback'=>'',
		'protocol_id'=>'YF2',
		'modality'=>'CT',
		'due_date'=>'03/06/2009',
		'due_time'=>'14:23',
		'scan_date'=>'03/04/2009',
		'scan_time'=>'09:00',
	//'email'=>'N',
		'baseline'=>'Y',
		'source'=>'MedCommons',
		'upload_notification_email'=> implode(',',$emailist),


	);

	$orderref = build_orderref($arglist,$withOauth)


	;


	header("Location: $orderref");
	//echo $orderref;

}


makeUploadRequest(false);

?>
