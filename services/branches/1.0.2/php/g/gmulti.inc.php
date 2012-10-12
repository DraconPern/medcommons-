<?php
function sports_home($h)
{
$msg = <<<XXX
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MedCommons Imaging Portal Consultants Directory</title>
 <link rel="stylesheet" type="text/css"  href="css/gstyle.css"/>
        <link rel="shortcut icon" href="favicon.gif" type="image/gif"/>
<meta name="viewport" content="minimum-scale=0.6, width=device-width, maximum-scale=1.6, user-scalable=yes">
    <meta name="apple-mobile-web-app-capable" content="YES">
    <link rel="apple-touch-icon" href="mcportal.png">

</head>

<body>
<img src='images/sports.png' "/>
<div><span id=hospital>Facilities</span>&nbsp;<span id=program>Hutton Health MHL Sports Portal</span>&nbsp;&nbsp;&nbsp;&nbsp;</div>
<h3>Upload to Any MHL Team</h3>
<table><tbody>		
<tr><td><a href='http://www2.lsdiv.harvard.edu/labs/evans/'>New York Dangers</a></td><td>
<button style="width:65;height:85" onClick="window.location='?handler=uploadpage&h=5&p=2'"><b>Upload to Dangers</b></button></td></tr>		
<tr><td><a href='http://www.pronouncedhorhay.com/'>Boston Brooms</a></td><td>
<button style="width:65;height:85" onClick="window.location='?handler=uploadpage&h=6&p=3'"><b>Upload to Brooms</b></button></td></tr>		

</tbody></table>
<hr/><a href='g.php?login&h=5'>ATC login</a>
</body>

XXX;

return $msg;
}


?>