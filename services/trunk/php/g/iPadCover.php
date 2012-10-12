<?php


function encode_fax($mcid, $n) {
  $binary = pack('NNN',
    intval(substr($mcid, 0, 8), 10),
    intval(substr($mcid, 8, 8), 10),
    $n);

  // Use 'url-safe' base64 encoding, cuz + and / interfere with URL encoding
  return str_replace(array('+', '/'), array('-', '_'),
		     base64_encode($binary));
}

function error_response_msg($arg1,$arg2)
{
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
			MedCommons Service Reports :<br/>
			<p>
			"ErrorCode": "$arg1",
			"ErrorMessage": "$arg2"

			</p>
</body>
</html>
MSG;

			echo $msg;
			exit;
		}
		
/////


//if (
////(!isset($_GET['switchB']))||
////(!isset($_GET['switchA']))||
if (isset($_GET['showconsent'])) $showconsent = true; else $showconsent = false;
if (!isset($_GET['coverid'])) $_GET['coverid'] ='NYMets';
if (!isset($_GET['from']))$_GET['from']='Dr B';
if (!isset($_GET['name']))$_GET['name']='Mike Jacobs';
if (!isset($_GET['mcid']))$_GET['mcid']='2032093209';

$coverid = $_GET['coverid'];
$name = $_GET['name'];
$mcid = $_GET['mcid'];
$from = $_GET['from'];

$tpextra = '';

    // Calculate the bar code url - dont interfere with current incoming handlers
    $barcode=encode_fax($mcid, $coverid);
    
    $barImgUrl="https://secure.efaxdeveloper.com/EFaxCreateBarCode.serv?BARCODE=$barcode&CODE_TYPE=DATAMATRIX&DM_DOT_PIXELS=8";
$instructions = "instructions - place face down on copy machine and copy or send as fax";
if (!$showconsent) $instructions = $instructions . 
"  <a href='?showconsent&coverid=$coverid&name=$name&mcid=$mcid&from=$from' >show consent form</a> ";

if (!$showconsent) $thankyou =<<<XXX
 <p>Thank you,<br/>
  <br/>
  
  Mike Jacobs
  <br/>
XXX;
  else $thankyou = '';
    $htmlconsent = <<<XXX
<p>Include a message or comment here, or attach additional pages before faxing:</p>
XXX;

    if ($showconsent) $htmlconsent = <<<XXX
   <hr/>	

<div>	  
  <p>Please print, sign, and save this request and consent in your files. 
  This EHR account accepts FAX and DICOM diagnostic images
  (see below).  You may use my MedCommons account as a HIPAA and FDA compliant
  secure communication system.</p>

  <p>Thank you,<br/>
  <br/>
  
  Mike Jacobs
  <br/>
  <div style="margin: 0px 0px 6px 0px;">Signed: &nbsp; ___________________________  &nbsp;&nbsp;&nbsp;_______________</div>
  <div style="float:left; width: 4em;">&nbsp;</div><div class="annotation" style="float: left;">Patient:</div><div class="importantinfo" style="float: left; width: 2in;">&nbsp;</div><span class="annotation">Date</span>
  <br style='clear: both;'/>
  <div style='height: 50px;'/>
</div>
		</div>
XXX;
 
    			$msg = <<<MSG
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>MedCommons Barcoded Fax Cover Sheet</title>
<meta name="viewport" content="width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;"/>
  <meta name="apple-touch-fullscreen" content="YES" />
<meta name="format-detection" content="telephone=no">
<style type="text/css" media="screen">
  body {
    margin: 0;
    font-family: Helvetica;
    background: #FFFFFF;
    color: #000000;
}

#outer {padding:5px 20px 0px 20px;margin: 20px;width:650px;}
  </style>
</head>
<body>
<div id=outer>
	<i>$instructions</i>

<table><tr><td>
 <div>
		<br/>
		Fax to: {$coverid}<br/>
		Patient: {$name}<br/>
		$tpextra	
		<img src='$barImgUrl' alt='missingbarcode' width=300px />
 </div>
 </td><td>
 <div >

    <div style="float: left; font-weight: bold;">
      PRIVATE FAX<br/>
      COVER SHEET<br/>
    </div>
      <div style="float: left; margin-left: 0.4in; height: 0.8in;">
        <br/>Number of Pages:  _______________ <br/><br/>
        <table style="width: 2.7in">
          <tr><td style="width:1in;">FAX TO:</td><td style="text-align: right;">
           <span class="importantnumber">1 (877) 717-7503&nbsp;</span>
          </td></tr>
        </table>
      </div>
      <div class="smallinstructions" style="width: 4.2in; clear: both;"><br/>FAX INSTRUCTIONS: This PHR account accepts FAX and converts them to PDF
      files.</div>
  </div>
</td></tr></table>

		
<div >
		      
  <p>The NY Mets maintain a standard Electronic Health Record (EHR) on MedCommons to help keep my care safe
  and effective. Your cooperation in using and updating my EHR is greatly
  appreciated. </p>
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

			echo $msg;
			exit;


?>