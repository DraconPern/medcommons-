<?php

require_once "dbparams.inc.php";

// keys for accessing external services

// sms service at 1cent/msg for outgoing sms
$pennysmskey = "d40bdebb-61da-4266-a045-27dfdb3c932f";

// Efax credentials for the account to use for incoming and outgoing fax
$faxoutuser ="bdonner5";
$faxoutpassword ="bdon23";
$faxoutid = "9175919352";
$faxoutpretty = '1-917-591-9352';

// MedCommons Amazon Credentials for the account to credit, 
$amzFpsAccessKey = "1J5AT4A5NMVAZZEHW3G2"; //"075Q8TW5Y9HFW4ZZAG02";// $accessKey =
$amzFpsSecretKey = "278H8f2VyZOtgV3FypZB2Xm+92iO6cqax8hw+fRW"; //"IMBRcy/Lb/uqrOLF7GTWI7emGKt120o+BDWgzcIa"; //$secretKey =
$amzFpsEmail = "billing-dev@medcommons.net";



function sign_medcommons_server_response($s)
{
	return "1202102994a";
}
function check_medcommons_client_request($sig,$sigtime)
{
	return ($sig=="13AB7Z589");
}

/*
 * function sign_medcommons_client_request($content)
 {
 return "13AB7Z589";
 }
 function check_medcommons_server_response($sig,$signature)
 {
 return ($sig == "1202102994a");
 }
 */
function verify_client_signature()
{
	if ((!isset($_POST['sig']))) $sig = ''; else $sig=$_POST['sig'];

	if ((!isset($_POST['time']))) $sigtime = ''; else $sigtime=$_POST['time'];
	if (!check_medcommons_client_request($sig,$sigtime)) server_response_msg (2,"Invalid Signature $sig $sigtime");
}
function server_response_msg ($arg1,$arg2=null,$arg3=null)
{
	// echo back a response msg
	$rsig = sign_medcommons_server_response ($arg1.$arg2.$arg3);
	$rsigtime = time();
	if (isset($_POST['respond']))
	$r = $_POST['respond'];
	else $r='';
	switch ($r)
	{
		case 'xml': {
			$msg = <<<MSG
			<MedCommons-Response-Container>
			<StandardInfo>
			<ErrorCode>
			$arg1
			</ErrorCode>
			<ErrorMessage>
			$arg2
			</ErrorMessage>
			</StandardInfo>
			$arg3
			<ResponseSignature>$rsig</ResponseSignature>
			<ResponseSignedTime>$rsigtime"</ResponseSignedTime>
			</MedCommons-Response-Container>
MSG;

			header ('Content-type:text/xml');
			echo $msg;
			exit;
		}
			
		case 'json': {

			header ('Content-type: text/json');
			$msg = <<<MSG

			{ "message" : "MedCommons-Response",
			"params"  : [

			StandardInfo : [

			"ErrorCode": "$arg1",
			"ErrorMessage": "$arg2",
			"ResponseSignature": "$rsig",
			"ResponseSignedTime": "$rsigtime"
			]
			]

			,

			$arg3
			}
 				
 				
MSG;

			echo $msg;
			exit;
		}
		case 'html':
		default: {
			header ('Content-type: text/html');

			// nothing in particular, return html

			$msg = <<<MSG

			<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
			<html>
			<head>
			<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
			<title>MedCommons SendFax Service Response</title>
			</head>
			<body>
			MedCommons Send Fax Service Reports :<br/>
			<p>
			"ErrorCode": "$arg1",
			"ErrorMessage": "$arg2"
			$arg3
			</p>
			-- response signed with $rsig at $rsigtime
</body>
</html>
MSG;

			echo $msg;
			exit;
		}
	}
}

?>