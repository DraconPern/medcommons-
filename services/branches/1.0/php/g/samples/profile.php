<?php
// wraps an image into html
$name = $_GET['c'];
$photo = $_GET['ph'];
$mx = <<<XXX
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MedCommons iPad Test Page for profile $name photo $photo</title>
<meta name="viewport" content="minimum-scale=1.0, width=device-width, maximum-scale=1.0, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="YES">
    <link rel="apple-touch-icon" href="mcportal.png">
    <style>
    html, body, h1, form, fieldset, legend, ol, li {
	margin: 0;
	padding: 0;
	}

body {
	background: #ffffff;
	color: #111111;
		font-family: Arial;
	padding: 20px;
	}
img .branded { max-width:60px;}
#hospital,#program {font-size:1.2em;}
.show {display:inline; color:red}
.hide {display:inline; color:blue}
.portalsplash {width:700px}

a {color:#154b8c; text-decoration:none; }
a:hover {text-decoration:underline; }
td {padding:0 10px 0 10px}

	h1 {
		font-size: 28px;
		margin-bottom: 20px;
		}
    </style>
</head>
<body>
<h2>Profile page for $name</h2>
<img src='$photo' alt='missing $photo' />
<h2>How Am I Related to $name?</h2>
<p>You are $name's Primary Care Physician</p>
<h2>What Can I Do Now?<h2>
<h3>Share</h3>
<h3>Send Fax Cover Sheets</h3>
<h3>Suggest Jane Visit ASAP</h3>
<h2><a href='https://healthurl.medcommons.net/1013062431111407' >Old School Viewer</a></h2>
</body>
</html>
XXX;

echo $mx;

?>