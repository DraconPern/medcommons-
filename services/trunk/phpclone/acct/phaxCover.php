<?php
/*
 *
 * Generate Printable Fax Cover Sheet
 * 
 *  -- all of these cover sheets generate something that might only exist on a mobile device glass display which is then printed or scanned or faxed directly
 *  -- alternatively, sheets needing a signature can be printed, signed, and then 
 *
 * inputs -
 * 			mcid - of the account that is to receive the incoming faxes behind the generated sheet
 * 			groupid - of the MedCommons Group that is sponsoring the fax line
 * 			name - to print on the cover sheet - this might have nothing to do with the mcid
 * 			scale -  either .5 for iPhone or 1.2 for iPad or anything else needed in the future
 * 
 * optional input -
 * 			showconsent link - if set, a coversheet with an explicit place for signing forms is generated
 * outputs -
 * 			the fax cover sheet, encoded and rendered as html
 * 			if the mcid is not found, or the group is not in the groupfax table, a sheet is generated inviting the customer to turn on MedCommons Fax handling
 *
 * 
 *
 *  
--
-- Table structure for table 'groupfax'
--

DROP TABLE IF EXISTS groupfax;
CREATE TABLE IF NOT EXISTS groupfax (
  ind smallint(6) NOT NULL auto_increment,
  GroupId decimal(16,0) NOT NULL,
  CoverId varchar(255) NOT NULL,
  FaxNumber varchar(32) NOT NULL,
  BarCoderURL varchar(255) NOT NULL,
  FaxTo varchar(32) NOT NULL,
  MainBlurb tinytext NOT NULL,
  Salutation varchar(255) NOT NULL,
  ConsentBlurb tinytext NOT NULL,
  PRIMARY KEY  (ind)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

 */
require_once "urls.inc.php";
require_once 'settings.php';
require_once "utils.inc.php";
require_once "../acct/alib.inc.php";

function encode_fax($mcid, $n) {
	$binary = pack('NNN',
	intval(substr($mcid, 0, 8), 10),
	intval(substr($mcid, 8, 8), 10),
	$n);

	// Use 'url-safe' base64 encoding, cuz + and / interfere with URL encoding
	return str_replace(array('+', '/'), array('-', '_'),
	base64_encode($binary));
}


if (isset($_GET['showconsent'])) $showconsent = true; else $showconsent = false;
if (!isset($_GET['mcid']))die ("Must supply mcid");
if (!isset($_GET['name']))die ("Must supply name");
if (!isset($_GET['groupid']))die ("Must supply groupid");
if (!isset($_GET['scale']))die ("Must supply scale factor");


$name = $_GET['name'];
$mcid = $_GET['mcid'];



//
// find this group in the group fax table
//
$covers = pdo_query("select BarCoderURL,CoverId,FaxNumber,FaxTo,MainBlurb,Salutation,ConsentBlurb from groupfax where GroupId = ?",$_GET['groupid']);
// if we can't find anything specific for the group, maybe we can find a general entry for the whole appliance

if(count($covers)==0)     $covers = pdo_query("select BarCoderURL,CoverId,FaxNumber,FaxTo,MainBlurb,Salutation,ConsentBlurb from groupfax where GroupId = ?",0);

if(count($covers)==0)
{
	// not found, generate a nice looking error page
	header ('Content-type: text/html');

	// nothing in particular, return html

	$msg = <<<MSG

			<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
			<html>
			<head>
			<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
			<meta name="viewport" content="minimum-scale=1.0, width=320, maximum-scale=1.0, user-scalable=no">
    		<meta name="apple-mobile-web-app-capable" content="YES">
    	
			<title>MedCommons Service Response</title>
			</head>
			<body>
			Sorry, this group is not enabled for Fax.<br/>
			<p>
			Please contact your MedCommons System Administrator to enable this feature.

			</p>
</body>
</html>
MSG;


}
else
{
	// generate the coverPage

	$encryptedPin = '';
	$coverProviderCode = "{$covers[0]->CoverId}";
	$title = "Fax to {$covers[0]->FaxNumber}";
	$note = "Note to {$covers[0]->FaxTo}";
	$coverPin='';
	$coverNotifyEmail = "cso@medcommons.net";

	// Add row to fax cover table, most of this is not need for ipad/phone
	$coverId = pdo_execute("insert into cover (cover_id, cover_account_id, cover_notification, cover_encrypted_pin, cover_provider_code, cover_title, cover_note, cover_pin)
                              values (NULL, ?, ?, ?, ?,?,?,?)", 
	array($mcid,$coverNotifyEmail,$encryptedPin,$coverProviderCode,$title,$note,$coverPin));

	// Calculate the bar code url - dont interfere with current incoming handlers
	$barcode=encode_fax($mcid,$coverId);// always use this slot in the cover table

	$barImgUrl="{$covers[0]->BarCoderURL}?CODE_TYPE=DATAMATRIX&DM_DOT_PIXELS=8&BARCODE=$barcode";


	$instructions = '';//"instructions - place face down on copy machine and copy or send as fax";
	if (!$showconsent) $instructions = $instructions .
"  <a href='?showconsent&scale={$_GET['scale']}&groupid={$_GET['groupid']}&name=$name&mcid=$mcid' >show consent form</a> ";

	if (!$showconsent) $thankyou =<<<XXX
	<p>{$covers[0]->Salutation}
	<br/>

	$name
  <br/>
XXX;
	else $thankyou = '';
	$htmlconsent = <<<XXX
<p><i>You can include a message or comment here, or attach additional pages before faxing:</i></p>
XXX;

	if ($showconsent) $htmlconsent = <<<XXX
	<hr/>

	<div>
	{$covers[0]->ConsentBlurb}

	<p>
	{$covers[0]->Salutation}
	<br/>
	<br/>
	$name
  <br/>
  <div style="margin: 0px 0px 6px 0px;">Signed: &nbsp; ___________________________ </div>
    <div style="margin: 0px 0px 6px 0px;">Date: &nbsp; ___________________________ </div>
		</div>
XXX;

	$msg = <<<MSG
	<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
	<html>
	<head>
	<title>MedCommons Barcoded Fax Cover Sheet</title>
	<meta name="viewport" content="width=device-width; initial-scale={$_GET['scale']}; maximum-scale={$_GET['scale']}; user-scalable=0;"/>
	<meta name="format-detection" content="telephone=no">
	<style type="text/css" media="screen">
	body {
	margin: 0;
	font-family: Helvetica;
	background: #FFFFFF;
	color: #000000;
}
.importantnumber {font-size:.9em; bold}
.smallinstructions {font-size: .7em;}
#outer {padding:2px 2px 2px 2px;margin: 20px;width:600px;font-size:.8em}
</style>
</head>
<body>
<div id=outer>


<table><tr><td>
<div>

Fax to: {$covers[0]->CoverId}<br/>
Patient: {$name}<br/>
<img src='$barImgUrl' alt='missingbarcode' width=400px />
</div>
</td>
<td>
<div >

<div style="font-size:1.3em; font-weight: bold;">
<i>$instructions</i><br/><br/>
PRIVATE FAX<br/>
COVER SHEET<br/>

FAX Number:
<span >{$covers[0]->FaxNumber}&nbsp;</span>
</div>
</div>
<br/>Pages:  <br/><br/>
</td></tr></table>


<div >

<p>{$covers[0]->MainBlurb}</p>
$thankyou

<div>

<p class="smallinstructions"><b>PRACTICE ADMINISTRATOR NOTE:</b> MedCommons is a patient-centered, HIPAA
compliant, secure communications and personal health record service that we
hope will make your practice more efficient while providing valued patient
privacy and consumer empowerment features.  For more information and the current
Terms of Use, please visit www.medcommons.net.
</p>
</div>
$htmlconsent
		</div>
</body>
</html>
MSG;


	
}

//nothing really at this point but echo back the final html blob

echo $msg;
?>