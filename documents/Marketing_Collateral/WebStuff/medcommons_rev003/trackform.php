<html>
<head>
<link href="css/main.css" rel="stylesheet" type="text/css">
<style type="text/css">
<!--
body {
	overflow:hidden;
	margin:0; padding:0;
	border:0;
	}
#trkformbox {
	width:200px;
	height:100px;
	padding:2px;
	background-image: url(images/tracknumblock_01.gif);
	background-color:#527463;
	background-attachment:scroll;
	background-repeat:no-repeat;
	}
#trknumfield {
	width:180px;
	border:1px solid #000;
	background-color:#FFFFFF;
	margin: 30px 0px 0px 10px;
	}
#gobutton {
	position:relative;
	margin: 3px 0px 0px 120px;
	filter:alpha(opacity=80);
	-moz-opacity:0.8;
	}
#gobutton:hover {
	filter:alpha(opacity=100);
	-moz-opacity:1.0;	
	}
	
-->
</style>
</head>
<body>
<div id="trkformbox">
<form id="trkform" name="trackingForm" method="post" action="logservice/trackinghandler.php" target="_top">
<input id="trknumfield" type="text" name="trackingNumber" class="AcctNumBox">
<input id="gobutton" type="image" src="images/gobutton.gif" width="45" height="35" border="0"></form></div>
</body></html>