<?php

$imgsite = 'http://www.medcommons.net/img/';

require_once 'site_config.php';

//		list ($image,$prevurl,$nexturl)=explode('|',base64_decode($_GET['a'])); //if starting automagically

$imageset = array (
array('Health2_0_Switch.png','Appliance Switch',"The MedCommons Appliance is a Health 2.0 Switch"),
array('Switch_Anatomy.png','Switch Interfaces',"The Switch Supports All Important Health 2.0 Interfaces with more to come"),

array('Global_Services.png','Global Services',"All Appliances, Regardless of Hosting and Ownership, Are Inter-Operable"),
array('Distributed_Radiology.png','Distributed Imaging',"Distributed Radiology Services including DICOM on Demand, utilize a downloadable DDL component for Windows, Mac, Linux"),
);

$imageset_len = count($imageset);

$index = $_GET['a']; // start at this point

if (!is_numeric($index) || ($index<0) || ($index>$imageset_len-1)) die ("No slide with that id");

$image = $imgsite.$imageset[$index][0]; // specific place
$title = $imageset[$index][1]; // specific place
$blabber = $imageset[$index][2]; // specific place

if ($index<$imageset_len-1) {
	$nextimage = $imageset[$index+1][0]; $nextlabel = $imageset[$index+1][1];$ip1 = $index+1;
	$nextimage = "<img src='{$imgsite}{$nextimage}' class=navimage /><br/><a class=navimagelink href='?a={$ip1}' title='next image' >$nextlabel >></a>";
}
else $nextimage = '';

if ($index>0) {
	$previmage = $imageset[$index-1][0]; $prevlabel = $imageset[$index-1][1];$ip1 = $index-1;
	$previmage = "<img src='{$imgsite}{$previmage}' class=navimage /><br/><a class=navimagelink href='?a={$ip1}' title='previous image' ><< $prevlabel</a>";
}
else $previmage = '';

$content = <<<XXX
<div class=walkview>
<div class=walkviewimagebody title='$blabber'>
<img src='$image' alt='missing $image' />
</div>
<hr/>
<p class=p1>$blabber</p>
<div class=walkviewimagenav>
<span style='float:left' >$previmage</span><span style='float:right;text-align:right;'>$nextimage</span>
</div>
<br/>
</div>
<br/>

XXX;
$markup = "<!-- loading header from /var/www/html/htm/_header.htm -->".file_get_contents("htm/_header.htm").$content.
	"<!-- loading footer from /var/www/html/htm/_footer.htm -->".file_get_contents("htm/_footer.htm");


$names = array('$$$htmltitle$$$','$$$modcomments$$$');
$values = array("Walkthru Gallery - $title" ,'');

echo str_replace($names,$values,$markup);

?>