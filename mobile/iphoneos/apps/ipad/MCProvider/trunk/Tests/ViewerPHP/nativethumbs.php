<?php
// generate HTML/JS test files for iPad Native-HTML interface
// bill - aug 18 2010

require ("common.php");

//////////////////////////// MAIN /////////////////////////////

global $buf, $jsbuf;

$background='gray'; $color='white';
if (isset($_GET['invert'])) { $background='white'; $color='black'; }
$jsonload = "onLoad='iFrameSetup(); return false;'";
if (isset($_GET['manual'])) $jsonload = "";

$buf = '';
$jsbuf = "";

setScrubberComponentVisibility ('hide');
setThumbsComponentVisibility ('show');
setThumbsComponentZoom ('max');
setToolsComponentVisibility ('hide');
setMainComponentVisibility ('hide');

echo "
<html><head>
<title>Native to Web iPad Tester for MedCommons App</title>
<meta name='viewport' content='width=device-width' />
<style>
body { background: $background ; color: $color; font-family: Arial,Tahoma; }
h1 {font-size: 8em}
a { color: lightgray; text-decoration:none; font-size:5em;}
ul {list-style: none}
ul li { padding:.3em; margin:.3em; }
#iframeStuff {display:none;}
</style>
<script type='text/javascript' src='common.js'/>
<body  >
<div style='border: 1px solid red'>
This should never really be rendered, instead the Full Screen Native Thumbnails View should appear
</div>
	<div id='iframeStuff' >7777</div>
	
	<script type='text/javascript'>$jsbuf</script>
</body>
</html>
"
?>
