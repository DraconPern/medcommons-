<?php
// wraps an image into html
$file = $_GET['p'];
if (!isset($_GET['c'])) $color='black'; else
$color = $_GET['c'];
$mx = <<<XXX
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MedCommons iPad Test Page for image $file</title>
<meta name="viewport" content="minimum-scale=1.0, width=device-width, maximum-scale=1.0, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="YES">
    <link rel="apple-touch-icon" href="mcportal.png">
    <style>
    body {margin: 0; width:320px; padding:0;background:$color;}
    img { border:0px; padding:0; margin: 0;}
    
    </style>
</head>
<body>
<img src='$file' alt='missing $file' />
</body>
</html>
XXX;

echo $mx;

?>