<?php
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
<head><title>BigApps Reporter Media Vault</title>
<meta name="viewport" content="minimum-scale=1.0, width=320, maximum-scale=1.6, user-scalable=yes">
    <meta name="apple-mobile-web-app-capable" content="YES">
    <link rel="apple-touch-icon" href="iphone.png">
<style>
body {
padding:0; margin:0;
    background-color:white;
	font-family: verdana;
	text-decoration: none;
	color: black;
	}
	a {color:white}
h5 {padding:0; margin:5px; font-size:18px;}
h4 {padding:0; margin:5px; color:gray}
.one_series {background-color:black; margin:5px;padding:5px; border: 1px solid #555}
.series_info {display:block; color:grey; font-size:.9em; border:0px solid grey; padding 10px;margin:10px;}
.tiny_num {font-size:.8em};
.datum {color:white};
.datum a {color: white; }
.subject_photo,.parts_photo,.photo-upload {display:inline; padding:10px; }
.subject_photo img { display:inline; margin:5px; padding: 5px; border: 5px solid #555; width:100px}
.parts_photo img { display:inline; margin:5px; padding: 5px; border: 5px solid #AAA; width:100px}

.solo_image {display:inline; padding: 0; border:none;}
.solo_image img {width:320px; padding: 0; border: none;}
.snaplink {padding-left:20px; font-size:.7em;}
.sbtitle {font-size:.8em}
</style>
</head>
<body>
SSS;
$counter = 0;
$lastiphonetime ='';
$mcturnaround = '';
$cook = (isset($_COOKIE['mc']));
if ($cook){


	$mc = $_COOKIE['mc'];

	$accid=""; $fn=""; $ln = ""; $email = ""; $idp = ""; $auth="";
	$props = explode(',',$mc);
	for ($i=0; $i<count($props); $i++) {
		list($prop,$val)= explode('=',$props[$i]);
		switch($prop)
		{
			case 'mcid': $accid=$val; break;
			case 'fn': $fn = $val; break;
			case 'ln': $ln = $val; break;
			case 'email'; $email = $val; break;
			case 'from'; $idp = stripslashes($val); break;
			case 'auth'; $auth = $val; break;
		}
	}
	$mcturnaround = "$accid|$email";


}
if (isset($_GET['image'])) 
$imagenum = ($_GET['image']); 
else
$imagenum = -1; // not interested in any image, show whole series

$noargs=false;
if ($cook)
$medcommons_dashboard_link="<a class=series_label href='m/index.html' ><img src='collateral/UIButtonBarAction.jpg' alt=nobut /></a>" ; else
$medcommons_dashboard_link = '';
	$shootpatientlink = "mcu://shootpatient?db=yes";
$out .="<table border=0 >
<tr>
<td>

<h4>BigApps Reporter Media Vault</h4>

<img src='http://www.medcommons.net/images/MEDcommons_logo_246x50_Tran.gif' width=120px>
<br>
<img src='collateral/dashesVertical.png' width='110' height='15'>
";
	$clause = ''; $sbtitle = "BigApps Reporter Media Series";

if (isset($_REQUEST['voucher']))
{
	$clause = "and m.vid = '{$_REQUEST['voucher']}' ";
	if ($imagenum>-1) 
	{
		$inum = $imagenum+1; $sbtitle .= "   Image $inum";
	}

} 
else  {
	$clause .= "and 1=0 "; $noargs = true;
}


if (isset($_REQUEST['pin']))
{
	$clause = "and m.pin = '{$_REQUEST['pin']}' ";
}
else { 
	$clause .= "and 1=0 "; 
	$noargs = true;
}

if ($noargs==true)
{
	$out .= <<<XXX
<p>
<form action = 'pickup.php' method = post>
<input type=text value ='' name=voucher size = 10>Voucher ID<br/>
<input type=text value='' name = pin size = 10>PIN<br/>
<br/>
<input type=submit value='Pickup' name=submit>
<br/>
</form>
</p>	
XXX;
	
}
else

{
$out .="

<div class=sbtitle>
<br/>
$sbtitle
</div>

";
$any = false;
$result = dosql("select * from  iphoneMeta m  where 1=1 $clause
order by m.iphonetime desc limit 10")       ;

while ($r = mysql_fetch_object($result))
{
	if (($r->mc!='')&& ($r->mc!='(null)'))$mcred = $r->mc;else $mcred = "no medcommons credentials";

	$shootpatientlink = "mcu://shootpatient?db=yes&first=$r->fn&last=$r->ln&dob=$r->dob";
	if ($cook) $shootpatientlink.="&mc=$mcturnaround";

	if ($r->series=='')

	{
		$serieslabel = "'unlabelled'";
		$seriestitle = "view whole series $r->vid";
	}

	else {
		$serieslabel = "<span class=datum>$r->series</span>";
		$seriestitle = "view whole series $r->vid";
	}
	$sender = $r->sender;
	if ($sender == "") $sender = "anonymous";
	if ($r->remote == $_SERVER['REMOTE_ADDR']) $remote = "This is your current IP"; else $remote = "This is NOT your current IP";
	$imagecount = -1;
	if ($counter>0) $out.="</div>"; // end previous one_series instance
		$itime = strftime("%D %T",$r->iphonetime);
	$any = true;
	$out .="<div class=one_series>
	<div class=series_info >
	Series <a class=series_label title='$seriestitle' href='pickup.php?voucher=$r->vid&pin=$r->pin' >$serieslabel</a>
	<div class='patient_demographics Patient datum' >$r->fn $r->ln $r->dob </div>
	<div>$mcred</div>
	<div class=shoot_time>Shot at $itime on iphone #:$r->uid </div>
	<div>By $sender</div>
	<div class='comment datum'>Comment $r->comment</div>
	</div>\n";
	$result2 = dosql("select * from iphoneBits b where b.iphonetime='{$r->iphonetime}' and b.uid='{$r->uid}'
	order by b.iphonetime desc, b.reqop desc, b.reqtime  ") ;
	while ($r2 = mysql_fetch_object($result2))
	{
		if ($r2->reqop == "subject_photo")
		{
			$imagecount++;
			//$shootpatientlink .= "&pic=http://ci.myhealthespace.com{$r2->reqdata}"; //put a more specific link together
			//$out .= "<a class=snaplink	href='$shootpatientlink'	><img src='collateral/UIButtonBarCamera.jpg' alt=nocam /></a> ";
			if($imagenum <0) // means showing small
			$out .= "<span class='{$r2->reqop} series_image'><a href='pickup.php?voucher=$r->vid&pin=$r->pin&image=$imagecount'>
			<img alt='{$r2->filesuffix}' src='{$r2->reqdata}' /></a></span>\n";
			else 
			if ($imagecount==$imagenum) // show just this one
			$out .= "<div class='solo_image'><a href='pickup.php?voucher=$r->vid&pin=$r->pin' >
			<img alt='{$r2->filesuffix}' src='{$r2->reqdata}' /></a></div>
			<a href= 'http://maps.google.com/maps?q={$r2->latitude},{$r2->longitude}' 
			title='lat: $r2->latitude long: $r2->longitude'>map</a>&nbsp;\n";
		}
		else
		if ($r2->reqop == "parts_photo")
		{
			$imagecount++;
			if($imagenum <0) // means showing small
			$out .= "<span class='{$r2->reqop} series_image'><a href='pickup.php?voucher=$r->vid&pin=$r->pin&image=$imagecount'>
			<img alt='{$r2->filesuffix}' src='{$r2->reqdata}' /></a></span>\n";
			else 
			if ($imagecount==$imagenum) // show just this one
			{
				$spec = $r2->reqdata;
				$spec = str_replace ('mcImage.png','mcVideo.MOV',$spec);
				if (file_exists("/var/www/html/$spec"))	 $detaillink = $spec;
				else $detaillink = "pickup.php?voucher=$r->vid&pin=$r->pin";
			
			$out .= "<div class='solo_image'><a href='$detaillink' ><img alt='{$r2->filesuffix}' src='{$r2->reqdata}' /></a></div>
			<a href= 'http://maps.google.com/maps?q={$r2->latitude},{$r2->longitude}' 
			title='lat: $r2->latitude long: $r2->longitude'>map</a>&nbsp;\n";
			}
		}
		else
		if ($r2->reqop == "video")
		{
			// nothing to do since the video link is under the corresponding parts_photo image
		}
	}
	//<img src='collateral/dashesVertical.png' width='110' height='15'>
			$out .= "<br>
<br>";


}


$out .= "
</td>

</tr>
</table>
</div>";

if (!$any)
$out .= "Sorry, there is nothing stored with that Voucher ID and PIN";
}
$out .="
</body></html>";
echo $out;
?>
