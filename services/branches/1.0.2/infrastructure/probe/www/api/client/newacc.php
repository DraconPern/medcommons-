<?php
require_once 'mc_oauth_client.php';




function make_patient ($appliance,$token,$ln,$fn,$dob,$sex,$img) // new args, only validated from importzipcsv
{

	// if none supplied, then make one
	$remoteurl = $appliance."/router/NewPatient.action?familyName=$ln&givenName=$fn&dateOfBirth=$dob".
      "&sex=$sex";//.
      //"&auth=".$token.
      //"&oauth_consumer_key=".$token;

	try {
		// consumer token when creating patient
		$file = get_url($remoteurl);
		$json = new Services_JSON();
		$result = $json->decode($file);
		if(!$result)
		throw new Exception("Unable decode JSON returned from URL ".$remoteurl.": ".$file);

		if($result->status != "ok")
		throw new Exception("Bad status '".$result->status."' error='".$result->error."' returned from JSON call ".$remoteurl);

		$mcid = $result->patientMedCommonsId;
		$auth = $result->auth;
		$secret = $result->secret;
		return $mcid;
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
$first = $_GET['first'];
$last = $_GET['last'];
$token ='';//$_GET['token'];
$appliance = $_GET['appliance'];
$time = time();


ob_start();
echo "<h4>$time Started at Creating $count new MedCommons Accounts on $appliance from $myserver</h4>";
ob_flush(); flush();
for ($j=0; $j<$count; $j++)
{
	$fn = $fprefix.$first.$t1.$j;
	$ln = $lprefix.$last.$t1.$j;
	$mcid = make_patient($appliance,$token,$ln,$fn,"010101","M",'test.jpg');
	//echo "Made new patient $fn $ln $mcid on $appliance<br/>"; ob_flush(); flush(); // for fun
	
}

$t2 = microtime(true);



$delta2 = round($t2-$t1,3);
$persecond = $delta2/$count;
$etime =round((1/$persecond),3);
$time = time();
echo "<h4>$time Finished Creating $count new MedCommons Accounts on $appliance from $myserver in $delta2 seconds</h4>";

echo "<h4>The rate per second is $etime and the time to create each account is $persecond seconds</h4>";
echo "For QA, the HealthURL of last patient created: <a target='_new' href='$appliance/$mcid' >https://$appliance/$mcid</a><br/>";



?>