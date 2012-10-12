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
function build_orderref($requirelogin,$patientid,$hospital, $provider,$callback,$time,$customfields)
{
// these are the basic order parameters as dragged thru from TIMC and with the "Custom Fields" and the new "showXXX" flags
	$h = z('h');
	$p = z('p');
	
	
	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	$next = "http://ci.myhealthespace.com/acct/g.php?handler=orderdone&h=$h&p=$p";
	$orderfields = array (
'next'=>$next,
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
	);
	// add in any custom fields
	$customfieldnum = 0;
	foreach ($customfields as $cf)
	{
		$orderfields ["custom_0{$customfieldnum}"] = $cf;
		$customfieldnum++;
	}	
	if (0!=$hospital->showPracticeName)
	{
		$orderfields["custom_08"]=$hn; 
	    $orderfields["custom_08_label"]="Hospital/Practice Name";
	}
	if (0!=$hospital->showService)
	{
		$orderfields["custom_09"]=$servicename; 
	    $orderfields["custom_09_label"]="Service";
	}
	if (0!=$hospital->showConsultant)
	{
		$orderfields["custom_10"]=$provider->provider; 
	    $orderfields["custom_10_label"]="Consultant";
	}
	if (0!=$hospital->showRefHospital)
	{
		//$orderfields["custom_00"]=$refhospital; 
	    $orderfields["custom_00_label"]="Referring Hospital";
	}
	if (0!=$hospital->showRefPhys)
	{
		//$orderfields["custom_01"]=$refphys; 
	    $orderfields["custom_01_label"]="Referring Physician";
	}
	if (0!=$hospital->showRefPhone)
	{
		//$orderfields["custom_04"]=$hospital; 
	    $orderfields["custom_04_label"]="Referring Physician Phone";
	}
	if (0!=$hospital->showRefEmail)
	{
		//$orderfields["custom_02"]=$email; 
	    $orderfields["custom_02_label"]="Referring Physician Email";
	}
	if (0!=$hospital->showRefWebConf)
	{
		//$orderfields["custom_03"]=$hospital; 
	    $orderfields["custom_03_label"]="Web/Video Conference";
	}
	if (0!=$hospital->showClinicalHistory)
	{
		//$orderfields["custom_06"]=$hospital; 
	    $orderfields["custom_06_label"]="Clinical History";
	}



	// now build up the actual Order URL depending on whether signing or not
	if ($requirelogin)
	{
		global $consumer,$acc_token;  // these are common to each of the requests
		// Consumer - enter your application's token and secret here
		$consumer  = new OAuthConsumer("270ebdf10b9bb9dd957a4a14833367183a196da7", "72c25b142d4dd453213b586fc1278afc446b1d89", NULL);
		// Access Token - enter your the Access Token for the user you are calling here
		$acc_token = new OAuthToken("970efdf18b9bb9dd957a4a14833367283a116d37", "", 1);
		$upload = 'https://ci.myhealthespace.com/orders/order'; // reset this for signed order
		$req = OAuthRequest::from_consumer_and_token($consumer,$acc_token, "GET", $upload, $orderfields);
		$req->sign_request(new OAuthSignatureMethod_HMAC_SHA1(), $consumer, $acc_token);
		$url_target = $req->to_url();  // get the nicely signed URL
	}
	else
	{
		// no signing, just make a regular link fron the fields
		$upload = "http://ci.myhealthespace.com/9092031177683982/upload?accid=9092031177683982";//$provider->uploadformurl;
		$args = '?args';
		foreach ($orderfields as $key=>$value) { $value= urlencode($value); $args .= "&$key=$value"; }
		$url_target = $upload.$args;
	}


	// return the URL as a link!, another choice would bne to just do it

	echo "Orderref is $url_target<br/>";
	return $url_target;
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

	//$providers = dosql("select * from  aHospitals, aServices s,aJoined j,aProviders p  where p.ind='$p' and j.hospitalind='$h'and j.providerind=p.ind and j.serviceind=s.ind ");
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
	
	));

	$pn = $provider->provider;
	$defaultfields = array (
"Text"=>"checked='checked'",
"Skype"=>"checked='checked'",
"Ref"=>"value=''");
$crumbs = //"<a href='?handler=adminlogin&h=$h'>admin home</a> >".
	" <a href='?handler=adminrefphyspage&h=$h'>referring physicians directory</a> > upload";
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
'_errormaintext'=>"Please confirm you want to place your order to $pn",
'_errormainclass'=>'hide', //class=hideerror to hide error field
	); // or class=hideerror to hide error field



	return loadpage("guploadconfirm.htm", "", $defaultfields, $substitutions, $errdecorators);

}

function contact_and_upload_page($errdecorators=array())
{
	
	$h = z('h');
	$p = z('p');

	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
	
	if ($progind==3)
	{
		// require login for now
	if(!($me=Gtestif_logged_in())) please_login(); else
	list($accid,$fn,$ln,$email,$idp,$mc,$auth) =$me;

	$itsme = dosql ("Select * from aProviders where mcid='$accid' "); // find self as provider
	if (!($r = mysql_fetch_object($itsme))) die ("MedCommons Imaging Portal cant find MedCommons ID $accid");
	}
	

	$providers = dosql("select * from  aServices s,aJoined j,aProviders p  where p.ind='$p' and j.hospitalind='$h'
	and j.providerind=p.ind and j.serviceind=s.ind ");
	if (!($provider =mysql_fetch_object($providers))) return false;
	$pn = $provider->provider;
	$defaultfields = array (
"Text"=>"checked='checked'",
"Skype"=>"checked='checked'",
"Ref"=>"value=''");
	if ($progind==3)
	{
$crumbs = //"<a href='?handler=adminlogin&h=$h'>admin home</a> >".
	" <a href='?handler=refphyslogin&h=$h'>Consultants Directory</a> &gt; Upload";
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
	{

	$crumbs = //"<a href='?handler=adminlogin&h=$h'>admin home</a> >".
	" <a href='?handler=refphyslogin&h=$h'>Consultants Directory</a> &gt; Upload";
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
	

	$errdecorators = array (
'_errormaintext'=>"",
'_errormainclass'=>'hideerror', 
	); // or class=hideerror to hide error field
	return loadpage("gupload.htm", "?handler=contact_and_upload_handler",
	$defaultfields, $substitutions, $errdecorators);
}

?>