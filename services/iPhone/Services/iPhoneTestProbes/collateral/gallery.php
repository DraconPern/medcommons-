<?php
// report status of medcommons appliance as JSON
require 'settings.php';
require_once 'utils.inc.php';
require_once 'dbparams.inc.php';
function dosql($q)
{
	if (!isset($GLOBALS['db_connected']) ){
		$GLOBALS['db_connected'] = mysql_connect($GLOBALS['DB_Connection'] ,$GLOBALS['DB_User'] );
		$db = $GLOBALS['DB_Database'];
		mysql_select_db($db) or die ("can not connect to database $db ".mysql_error());
	}
	$status = mysql_query($q);
	if (!$status) die ("dosql failed $q ".mysql_error());
	return $status;
}


// main starts here  $out = '';';
$out = <<<SSS
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<head><title>DICOM on Demand Photo Stream Gallery</title>
<meta name="viewport" content="minimum-scale=1.0, width=device-width, maximum-scale=1.0, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="YES">
    <link rel="apple-touch-icon" href="iphone.png">
<style>
.series {display:block; padding:20px; color:grey; font-size:.8em;}
.serial_num {font-size:.7em}
.series_image img {width:100px;}
.solo_image img {width:320px; padding: 0; border: none;}
.solo_image {display:inline; padding: 0; border:none;}
.patient_photo,.parts_photo,.photo-upload {display:inline; padding:20px; }
.patient_photo img { display:inline; padding: 5px; border: 5px solid blue; }
.parts_photo img { display:inline; padding: 5px; border: 5px solid red; }
</style>
</head>
<body>
SSS;
$counter = 0;
if (isset($_GET['image'])) $imagenum = ($_GET['image']); else
$imagenum = -1; // not interested in any image, show whole series
if (isset($_GET['all'])) { $clause = ''; $sbtitle = "Shoot the Patient DOD Global PhotoStream";}
else
if (isset($_GET['remote'])){
$clause = "and m.remote = '{$_GET['remote']}' "; $sbtitle = "All PhotoStreams from IP {$_GET['remote']}";}
else
if (isset($_GET['vid'])){
$clause = "and m.vid = '{$_GET['vid']}' "; $sbtitle = "Shoot the Patient Series# {$_GET['vid']}";}
else
if (isset($_GET['uid'])){
$clause = "and m.uid = '{$_GET['uid']}' "; $sbtitle = "All PhotoStreams from iPhone {$_GET['uid']}";}
else{
$clause = "and b.remote = '{$_SERVER['REMOTE_ADDR']}' "; $sbtitle = "All PhotoStreams from This IP {$_SERVER['REMOTE_ADDR']}";}

$result = dosql("select * from iphoneBits b, iphoneMeta m  where 1=1 $clause and b.iphonetime=m.iphonetime and b.remote=m.remote order by b.iphonetime desc, b.reqop desc, b.reqtime  ")       ;
$lastiphonetime ='';
$out .="<h3>$sbtitle</h3>";
$imagecount = -1;
$prestr = ($imagenum<0)?"Series":"Image $imagenum of:";
while ($r = mysql_fetch_object($result))
{
	if ($lastiphonetime !== $r->iphonetime)
	{
		$out .="<div class=series >
		        <div>$prestr <a href='gallery.php?vid=$r->vid' >$r->vid</a> 
		                     $r->series </div><div>$r->iphonetime </div>
		         <div>$r->sender (<a href='gallery.php?remote=$r->remote' >$r->remote</a>)</div>
		         <div class=serial_num>iphone serial#:<a href='gallery.php?uid=$r->uid' >$r->uid</a></div>
		         <div class=patient_demographics>$r->fn $r->ln $r->dob</div>
		         <div class=comment> $r->comment</div>
		         </div>\n";
		$lastiphonetime = $r->iphonetime;

		if ($counter++>10) {
			$out .= "</div></body></html>";
			echo $out;
			exit;
		}
	}
	$imagecount++;
	if (($imagenum < 0) )
	$out .= "<div class='{$r->reqop} series_image'><a href='gallery.php?vid=$r->vid&image=$imagecount'><img alt='{$r->filesuffix}' src='{$r->reqdata}' /></a></div>\n";
	else if (($imagenum == $imagecount ))
	$out .= "<div class='solo_image'><a href='gallery.php?vid=$r->vid&image=$imagecount'><img alt='{$r->filesuffix}' src='{$r->reqdata}' /></a></div>\n";
	
}
$out .= "</div></body></html>";
echo $out;
?>
