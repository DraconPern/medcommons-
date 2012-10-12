<?php
require_once 'mc_oauth_client.php';
require_once 'testlib.inc.php';


function one_call ($appliance,$token,$ln,$fn,$dob,$sex,$img) // new args, only validated from importzipcsv
{


	$remoteurl = $appliance."/probe/ws/ijsprobernil.php";

	try {
		
		$file = file_get_contents($remoteurl);
		$json = new Services_JSON();
		$result = $json->decode($file);
		if(!$result)
		throw new Exception("Unable to decode JSON returned from URL ".$remoteurl.": ".$file);
		if($result->status != "ok")
		throw new Exception("Bad status '".$result->status."' error='".$result->error."' returned from JSON call ".$remoteurl);
		return true;
	}
	catch(Exception $ex) {
		die("Unsuccessful test completion ". $ex->getMessage());
	}

}


if (!isset($_GET['count'])) die ("Must supply ?count=");

if (!isset($_GET['appliance'])) die ("Must supply ?appliance=");

//main
$testname = "Niltest";
run_test ($testname,$_GET['count'],$_GET['appliance']);
?>