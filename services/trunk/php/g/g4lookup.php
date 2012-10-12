<?php


//				$context = "$name | $patientname | $hospital | $sender | $videolink | $smslink | $emaillink | $chooselink
//				| $viewlink |$oref | $toconsultant | $comment ";
//				$base64 = base64_encode($context);

function a($label,$value)
{
	$value = trim($value);
	return ("<tr><td class=label >$label</td><td class=value >$value</tr>")	;
}


$arg = $_GET['a'];
$decoded = base64_decode($arg);
//echo $decoded;

$table = <<<XXX

	<!DOCTYPE HTML>
	<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>MedCommons Imaging Portal Dashboard for iPad</title>
	<link rel="stylesheet" type="text/css"  href="css/g4style.css"/>
	<meta name="viewport" content="minimum-scale=1.0, width=device-width, maximum-scale=1.0, user-scalable=no">
	</head>
	<body>
	<table>
	<tbody>

XXX;

// chooselink is quickforms
// patientname is timestamp
// to consultant should not be link


list ($name ,$timestamp  ,  $hospital  ,  $sender  ,  $videolink  ,  $smslink  ,  $emaillink  
,  $viewlink  , $oref  ,  $toconsultant  ,  $chooselink, $comment,$auth ) =explode('|',$decoded);
	
	
// lay down some pretty html


$table .= a('name',$name);
$viewlink = "<a href='x-medpad://viewer?healthURL=$viewlink'>$viewlink</a>"; // back to real link
$table .= a('healthURL',$viewlink);

$table .= a('time',$timestamp);


$table .= a('facility',$hospital);

$table .= a('sender',$sender);

if (substr($videolink,0,1)!='-')
$table .= a('videolink',$videolink);
if (substr($smslink,0,1)!='-')
$table .= a('smslink',$smslink);
if (substr($emaillink,0,1)!='-')
$table .= a('emaillink',$emaillink);
if (substr($chooselink,0,1)!='-')
$table .= a('quick forms',"<a href='$chooselink' >$chooselink</a>");


$oref = "<a href='http://portal.medcommons.net/orders/orderstatus?callers_order_reference=$oref'>$oref</a>";
$table .= a('order status',$oref);
if (strlen($toconsultant)>0)
$table .= a('consultant','consultant-'.$toconsultant);

$table .= a('auth','auth-'.$auth);
$table .= a('comment',$comment);


$table .=<<<XXX
</tbody>
</table>
</body>
</html>
XXX;


echo $table;








?>

