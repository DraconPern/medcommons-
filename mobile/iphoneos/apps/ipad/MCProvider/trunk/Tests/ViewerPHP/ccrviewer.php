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
	
	
	$doclist ='';
	if (isset($_GET['doclist'])) $doclist = $_GET['doclist'];
	
	
	$buf = '';
	$jsbuf = "";
	
	
	
	
	$ecount = 0;
	addEpisodeComponent ('me00', 'Current CCR', 'th304,th301,th105','episode_chosen_event', 'ccrviewer.php'); $ecount++;
	addEpisodeComponent ('me01', 'Episode-1 12/23/07', 'th404,th301,th105', 'episode_chosen_event', 'ccrviewer.php'); $ecount++;
	addEpisodeComponent ('me02', 'Episode-2 12/25/07', 'th204,th201,th105', 'episode_chosen_event', 'ccrviewer.php');	$ecount++;
	addEpisodeComponent ('me03', 'Episode-3 03/04/09','th104,th101,th105',  'episode_chosen_event', 'ccrviewer.php');$ecount++;
	
	$thcount=0;
	addThumbComponent ('th00', 'http://www.medcommons.net/images/ccrthumb.png', '0. Current CCR',         '1 doc',    'ccr_pressed_event', '00_ccr');$thcount++;
	
	
	addThumbComponent ('th01', 'http://mclinkmaker.limewebs.com/lmk/thumbnail01.png', '1. 3D Coronal',         '1 image',    'thumb_pressed_event', '01_thumb');$thcount++;
	addThumbComponent ('th02', 'http://mclinkmaker.limewebs.com/lmk/thumbnail02.png', '2. Supine w/ contrast', '67 images',  'thumb_pressed_event', '02_thumb');$thcount++;
	addThumbComponent ('th03', 'http://mclinkmaker.limewebs.com/lmk/thumbnail03.png', '3. Abd 3 hours XGA',    '277 images', 'thumb_pressed_event', '03_thumb');$thcount++;
	
	addThumbComponent ('th04', 'http://mclinkmaker.limewebs.com/lmk/thumbnail04.png', '4. 3D Coronal',         '1 image',    'thumb_pressed_event', '04_thumb');$thcount++;
	addThumbComponent ('th05', 'http://mclinkmaker.limewebs.com/lmk/thumbnail05.png', '5. Supine w/ contrast', '67 images',  'thumb_pressed_event', '05_thumb');$thcount++;
	addThumbComponent ('th06', 'http://mclinkmaker.limewebs.com/lmk/thumbnail06.png', '6. Abd 3 hours XGA',    '277 images', 'thumb_pressed_event', '06_thumb');$thcount++;
	
	
	
	
	
	setScrubberComponentVisibility ('hide');
	setThumbsComponentVisibility ('show');
	setThumbsComponentZoom ('norm');
	setToolsComponentVisibility ('hide');
	setMainComponentVisibility ('show');
	
	//max doesnt work
	setMainComponentZoom ('norm');
	
    
	echo "
	<html><head>
	<title>Native to Web iPad Tester for MedCommons App</title>
	<meta name='viewport' content='width=device-width' />
	<style>
	body { background: $background ; color: $color; font-family: Arial,Tahoma; }
	a { color: lightgray; text-decoration:none; font-size:1.3em;}
	ul {list-style: none}
	ul li { padding:.3em; margin:.3em; }
	#iframeStuff {display:none;}
	</style>
	<script type='text/javascript' src='common.js' >
	</script>
	<body  >
	<div style='border: 10px solid gray'>
	<h1>Current CCR $doclist</h1>
	<h3>Rendered by MedCommons CCRViewer 2.0</h3>
	<p><img src='lmk/CCRpic.png' width=400  alt='ccrpic' /></p>

	</div>
	
	<div id='iframeStuff' >7777</div>
	
	<script type='text/javascript'>$jsbuf</script>
	</body>
	</html>
	"
	?>
