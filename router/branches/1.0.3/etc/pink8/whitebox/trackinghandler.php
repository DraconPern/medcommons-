<?php

require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/displayhomepage.inc");
require_once("../whitebox/displaymyaccountpage.inc");

	
function wadodispatch
//($orderurl, $wado,$guid,$tracking,$address,$patient,$id,
//	$comments,$history,$datetime,$description,$status)
($gwurl, $guid, $tracking, $origin, $dest, $time, $status, $description,
				$patientname, $patientid, $modality, $series, $nimages)
				

{  
	
	$url = makewadourl ($gwurl,$tracking,$guid);
//	echo "made  wado url $url";

return $url;
}

readconfig();//hack
session_start();
$trackingbutton = cleanreq('trackingbutton');
$tracking = cleanreq ('tracking'); // passed thru
//check to see if it's a tracking request

	if ($_SESSION['user']==""){
		echo display_home_page(errortext("You must be logged in to track orders"),"","","");
		exit;}
//Ok, she's logged in and hit the go button, so prepare to dispatch the wado viewer
//		echo "looking for xds registry matches for this tracking number $tracking ";
//		echo "looking for tracking $tracking";
		$wadourl = getorderbytracking('wadodispatch',$tracking);
		if ($wadourl=="") {
				echo display_home_page(errortext(
					"There is a problem with your tracking number"),"","","");
				exit;
		}
		else {
			//redirect this page to the wado viewer
			
			 // Change to the URL you want to 
			header ("Location: $wadourl");
			 echo "Should be redirecting to $wadourl\r\n";
			 exit;
		}

?>
