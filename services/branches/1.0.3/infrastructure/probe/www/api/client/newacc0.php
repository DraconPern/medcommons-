<?php
require_once 'mc_oauth_client.php';




function make_patient ($appliance,$token,$ln,$fn,$dob,$sex,$img) // new args, only validated from importzipcsv
{


	$remoteurl = $appliance."/ijs/ijsprobernil.php";
      //"&auth=".$token.
      //"&oauth_consumer_key=".$token;
	

	try {
		// consumer token when creating patient
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
		die("Unable to create new patient ". $ex->getMessage());
	}

}


//main
global $PROPS;

$t1 = microtime(true);	

//if (!isset($_COOKIE['mc'])) die("Must be logged in");


$myserver = $_SERVER ['SERVER_NAME'];
$fprefix='';
$lprefix='';
$count = $_GET['count'];
$appliance = $_GET['appliance'];
$time = time();



echo "<h4>$time Started at Probing $count times with no MySql calls on $appliance from $myserver</h4>";

for ($j=0; $j<$count; $j++)
{

	$mcid = make_patient($appliance,'','','',"010101","M",'test.jpg');
	//echo "Made new patient $fn $ln $mcid on $appliance<br/>"; ob_flush(); flush(); // for fun
	
}

$t2 = microtime(true);



$delta2 = round($t2-$t1,3);
$persecond = $delta2/$count;
$etime =round((1/$persecond),3);
$time = time();
echo "<h4>$time Finished Probing  $count times with no  Mysql calls on $appliance from $myserver in $delta2 seconds</h4>";

echo "<h4>The rate per second is $etime and the time for each is $persecond seconds</h4>";



?>