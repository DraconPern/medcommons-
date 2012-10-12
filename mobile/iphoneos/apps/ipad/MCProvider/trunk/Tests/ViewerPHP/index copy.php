<?php
	
	
	
	// once only, top level page for the Viewer2.0 test rig

// generate HTML/JS test files for iPad Native-HTML interface
// bill - aug 18 2010

require ("common.php");

//////////////////////////// MAIN /////////////////////////////

global $buf, $jsbuf;

$background='black'; $color='white';
if (isset($_GET['invert'])) { $background='white'; $color='black'; }
$jsonload = "onLoad='iFrameSetup(); return false;'";
if (isset($_GET['manual'])) $jsonload = "";

$buf = '';
$jsbuf = "";

if (!isset($_GET['manual']))
out("<hr/><small>everything in this section is automatically invoked upon startup</small>");

h ('Tool Button Links'); // max 12, dynamic
addToolComponent ('tb01', 'arrow',       'button_pressed_event', 'arrow_button');
addToolComponent ('tb02', 'magnify',     'button_pressed_event', 'magnify_button');
addToolComponent ('tb03', 'pan',         'button_pressed_event', 'pan_button');
addToolComponent ('tb04', 'overlay',     'button_pressed_event', 'overlay_button');
addToolComponent ('tb05', 'wl',          'button_pressed_event', 'wl_button');
addToolComponent ('tb06', 'wlpreset',    'button_pressed_event', 'wlpreset_button');
addToolComponent ('tb07', 'prev_series', 'button_pressed_event', 'prev_series_button');
addToolComponent ('tb08', 'next_series', 'button_pressed_event', 'next_series_button');
addToolComponent ('tb09', 'prev_image',  'button_pressed_event', 'prev_image_button');
addToolComponent ('tb10', 'next_image',  'button_pressed_event', 'next_image_button');
addToolComponent ('tb11', 'reset',       'button_pressed_event', 'reset_button');
addToolComponent ('tb12', 'play_full',   'button_pressed_event', 'play_full_button');
	
	
	
	
	$thcount=0;
h ('Thumb Links - on the left'); // variable and dynamic  *** ADD THUMB COMPONENT IGNORES THE JSPARAM ARG AND SUBSTITUTES ITS OWN
	addThumbComponent ('th00', 'http://www.medcommons.net/images/ccrthumb.png', '0. Current CCR',         '1 doc',    'ccr_pressed_event', '00_ccr');$thcount++;
	
	
	addThumbComponent ('th01', 'http://mclinkmaker.limewebs.com/lmk/thumbnail01.png', '1. 3D Coronal',         '1 image',    'thumb_pressed_event', '01_thumb');$thcount++;
	addThumbComponent ('th02', 'http://mclinkmaker.limewebs.com/lmk/thumbnail02.png', '2. Supine w/ contrast', '67 images',  'thumb_pressed_event', '02_thumb');$thcount++;
	addThumbComponent ('th03', 'http://mclinkmaker.limewebs.com/lmk/thumbnail03.png', '3. Abd 3 hours XGA',    '277 images', 'thumb_pressed_event', '03_thumb');$thcount++;

	addThumbComponent ('th04', 'http://mclinkmaker.limewebs.com/lmk/thumbnail04.png', '4. 3D Coronal',         '1 image',    'thumb_pressed_event', '04_thumb');$thcount++;
	addThumbComponent ('th05', 'http://mclinkmaker.limewebs.com/lmk/thumbnail05.png', '5. Supine w/ contrast', '67 images',  'thumb_pressed_event', '05_thumb');$thcount++;
	addThumbComponent ('th06', 'http://mclinkmaker.limewebs.com/lmk/thumbnail06.png', '6. Abd 3 hours XGA',    '277 images', 'thumb_pressed_event', '06_thumb');$thcount++;

	addThumbComponent ('th101', 'http://mclinkmaker.limewebs.com/lmk/thumbnail101.png', '7. 3D Coronal',         '1 image',    'thumb_pressed_event', '07_thumb');$thcount++;
	addThumbComponent ('th102', 'http://mclinkmaker.limewebs.com/lmk/thumbnail102.png', '8. Supine w/ contrast', '67 images',  'thumb_pressed_event', '08_thumb');$thcount++;
	addThumbComponent ('th103', 'http://mclinkmaker.limewebs.com/lmk/thumbnail103.png', '9. Abd 3 hours XGA',    '277 images', 'thumb_pressed_event', '09_thumb');$thcount++;

	addThumbComponent ('th104', 'http://mclinkmaker.limewebs.com/lmk/thumbnail104.png', '10. 3D Coronal',         '1 image',    'thumb_pressed_event', '10_thumb');$thcount++;
	addThumbComponent ('th105', 'http://mclinkmaker.limewebs.com/lmk/thumbnail105.png', '11. Supine w/ contrast', '67 images',  'thumb_pressed_event', '11_thumb');$thcount++;
	addThumbComponent ('th106', 'http://mclinkmaker.limewebs.com/lmk/thumbnail106.png', '12. Abd 3 hours XGA',    '277 images', 'thumb_pressed_event', '12_thumb');$thcount++;

	addThumbComponent ('th201', 'http://mclinkmaker.limewebs.com/lmk/thumbnail201.png', '13. 3D Coronal',         '1 image',    'thumb_pressed_event', '13_thumb');$thcount++;
	addThumbComponent ('th202', 'http://mclinkmaker.limewebs.com/lmk/thumbnail202.png', '14. Supine w/ contrast', '67 images',  'thumb_pressed_event', '14_thumb');$thcount++;
	addThumbComponent ('th203', 'http://mclinkmaker.limewebs.com/lmk/thumbnail203.png', '15. Abd 3 hours XGA',    '277 images', 'thumb_pressed_event', '15_thumb');$thcount++;

	addThumbComponent ('th204', 'http://mclinkmaker.limewebs.com/lmk/thumbnail204.png', '16. 3D Coronal',         '1 image',    'thumb_pressed_event', '16_thumb');$thcount++;
	addThumbComponent ('th205', 'http://mclinkmaker.limewebs.com/lmk/thumbnail205.png', '17. Supine w/ contrast', '67 images',  'thumb_pressed_event', '17_thumb');$thcount++;
	addThumbComponent ('th206', 'http://mclinkmaker.limewebs.com/lmk/thumbnail206.png', '18. Abd 3 hours XGA',    '277 images', 'thumb_pressed_event', '18_thumb');$thcount++;

	addThumbComponent ('th301', 'http://mclinkmaker.limewebs.com/lmk/thumbnail301.png', '19. 3D Coronal',         '1 image',    'thumb_pressed_event', '19_thumb');$thcount++;
	addThumbComponent ('th302', 'http://mclinkmaker.limewebs.com/lmk/thumbnail302.png', '20. Supine w/ contrast', '67 images',  'thumb_pressed_event', '20_thumb');$thcount++;
	addThumbComponent ('th303', 'http://mclinkmaker.limewebs.com/lmk/thumbnail303.png', '21. Abd 3 hours XGA',    '277 images', 'thumb_pressed_event', '21_thumb');$thcount++;

	addThumbComponent ('th304', 'http://mclinkmaker.limewebs.com/lmk/thumbnail304.png', '22. 3D Coronal',         '1 image',    'thumb_pressed_event', '22_thumb');$thcount++;
	addThumbComponent ('th305', 'http://mclinkmaker.limewebs.com/lmk/thumbnail305.png', '23. Supine w/ contrast', '67 images',  'thumb_pressed_event', '23_thumb');$thcount++;
	addThumbComponent ('th306', 'http://mclinkmaker.limewebs.com/lmk/thumbnail306.png', '24. Abd 3 hours XGA',    '277 images', 'thumb_pressed_event', '24_thumb');$thcount++;

	addThumbComponent ('th401', 'http://mclinkmaker.limewebs.com/lmk/thumbnail401.png', '25. 3D Coronal',         '1 image',    'thumb_pressed_event', '25_thumb');$thcount++;
	addThumbComponent ('th402', 'http://mclinkmaker.limewebs.com/lmk/thumbnail402.png', '26. Supine w/ contrast', '67 images',  'thumb_pressed_event', '26_thumb');$thcount++;
	addThumbComponent ('th403', 'http://mclinkmaker.limewebs.com/lmk/thumbnail403.png', '27. Abd 3 hours XGA',    '277 images', 'thumb_pressed_event', '27_thumb');$thcount++;

	addThumbComponent ('th404', 'http://mclinkmaker.limewebs.com/lmk/thumbnail404.png', '28. 3D Coronal',         '1 image',    'thumb_pressed_event', '28_thumb');$thcount++;
	addThumbComponent ('th405', 'http://mclinkmaker.limewebs.com/lmk/thumbnail405.png', '29. Supine w/ contrast', '67 images',  'thumb_pressed_event', '29_thumb');$thcount++;
	addThumbComponent ('th406', 'http://mclinkmaker.limewebs.com/lmk/thumbnail406.png', '30. Abd 3 hours XGA',    '277 images', 'thumb_pressed_event', '30_thumb');$thcount++;

//h ('Scrubber Link - below the main webview'); // only one
//modifyScrubberComponent (1, 1, 67, 'scrubber_pressed_event', '1_scrubber');

h ('Share Links -  menu built in native code');

h ('Menu Links'); // for now this is a simplistic, single ipad style list - not sure what entries shud be
addMenuComponent ('mn01', 'Merge Episodes', 'menu_pressed_event', 'merge_selected');
addMenuComponent ('mn02', 'Copy Episode',  'menu_pressed_event', 'copy_selected');
	
	$ecount = 0;
h ('Episode Links'); // for now this is a simplistic, single ipad style list - not sure what entries shud be
addEpisodeComponent ('me00', 'Current CCR', 'th304,th301,th105','episode_chosen_event', 'ccrviewer.php'); $ecount++;
addEpisodeComponent ('me01', 'Episode-1 12/23/07', 'th404,th301,th105', 'episode_chosen_event', 'ccrviewer.php'); $ecount++;
addEpisodeComponent ('me02', 'Episode-2 12/25/07', 'th204,th201,th105', 'episode_chosen_event', 'ccrviewer.php');	$ecount++;
addEpisodeComponent ('me03', 'Episode-3 03/04/09','th104,th101,th105',  'episode_chosen_event', 'ccrviewer.php');$ecount++;

h ('Header Links - above the thumbnails');
modifyHeaderComponent ('title',     'Current CCR',  'button_pressed_event', 'title_button');
modifyHeaderComponent ('subtitle',  '4/15/2009',    'ignored',              'ignored');

setScrubberComponentVisibility ('show');
setThumbsComponentVisibility ('show');
setToolsComponentVisibility ('show');
// set this as late as possible
$jsonload = "onLoad='iFrameSetup($jsbuf); return false;'";

echo "
<html><head>
<title>Native to Web iPad Tester for MedCommons App</title>
<meta name='viewport' content='width=device-width' />
<style>
body { background: $background ; color: $color; font-family: Arial,Tahoma; }
a { color: #DDDD;  text-decoration:none; font-size:1.3em;}
ul {list-style: none}
ul li { padding:.3em; margin:.3em; }
#iframeStuff {display:none;}
</style>
	
	<script type='text/javascript' src='common.js' >
	</script>
<body >
<div style='border: none' >
<h1>Native to Web iPad Test Program Main Page</h1>
<small>version 1.6</small>

    <img src='lmk/thumbnail03.png' height=100
     alt='This Image Is Not Displayed in Straight Safari'>
<p>Warning - If you are not on an iPad with the MC app installed, every link will fail. On the iPad, under Safari Settings, you will want to enable the developer console</p>

<center><a href=ccrviewer.php><img src='http://28.media.tumblr.com/tumblr_l6kx2ylRIA1qd4q62o1_100.jpg'/></a></center>
<div style='display:none'  >
<ul>
";

echo $buf;

echo "
</ul>
</div>
</div>	
	<div id='iframeStuff' >7777</div>
	
	<script type='text/javascript'>$jsbuf</script>
</body>
</html>
"
?>
