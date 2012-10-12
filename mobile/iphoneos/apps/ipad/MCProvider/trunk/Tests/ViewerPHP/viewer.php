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
	
	setScrubberComponentVisibility ('show');
	setThumbsComponentVisibility ('show');
	setThumbsComponentZoom ('norm');
	setToolsComponentVisibility ('show');
	setMainComponentVisibility ('show');
	setMainComponentZoom ('norm');
	
	
	
	
	$ecount = 0;
	h ('Episode Links'); // for now this is a simplistic, single ipad style list - not sure what entries shud be
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
	
	
	
	$url = $_GET['url'];
	$html = "<html><head><title>Image $url</title>
	<script type='text/javascript' src='common.js' >
	</script>
	</head><body><h3>MedCommons Viewer 2.0 Goes Here</h3><center><img src='$url' alt='no $url'/></center></body></html>";
	echo $html;
?>