<?php

require_once "simtrak.inc.php";
require_once "stview.inc.php";
require_once "viewer.inc.php";

// start here
//decode our intentions$mcid_=0;
$standalone=isset($_REQUEST['admin']);
$admin = ($standalone)?'?admin&':'?';

if (!isset($_GET['layout'])) { $backlink = $admin; $layout_='Demographics'; }
else	{$layout_ = $_GET['layout']; $backlink= $backlink."accid=$mcid_";}
	
if (!isset($_GET['tab'])) $frontab_='tab_A';else	$frontab_ = $_GET['tab'];

if (isset($_GET['accid'])) $mcid_  = $_GET['accid']; else $mcid_ = 0;





viewer_plugin($standalone); // mcid is hidden variable

?>

