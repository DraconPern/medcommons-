<?php
require_once 'mc_oauth_client.php';




function make_patient ($appliance,$token,$ln,$fn,$dob,$sex,$img) // new args, only validated from importzipcsv
{

	// if none supplied, then make one
	$remoteurl = $appliance."/router/NewPatient.action?familyName=$ln&givenName=$fn&dateOfBirth=$dob".
      "&sex=$sex".
      "&auth=".$token.
      "&oauth_consumer_key=".$token;

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

if (!isset($_COOKIE['mc'])) die("Must be logged in");


$myserver = $_SERVER ['SERVER_NAME'];
$fprefix='';
$lprefix='';
$count = $_GET['count'];
$first = $_GET['first'];
$last = $_GET['last'];
$token =$_GET['token'];
$appliance = $_GET['appliance'];

for ($j=0; $j<$count; $j++)
{
	$fn = $fprefix.$first.$t1.$j;
	$ln = $lprefix.$last.$t1.$j;
	$mcid = make_patient($appliance,$token,$ln,$fn,"010101","M",'test.jpg');
}

$t2 = microtime(true);


$delta2 = round($t2-$t1,3);
echo "<h4>Created $count new MedCommons Accounts on $appliance from $myserver</h4>";
echo "HealthURL of last created: <a href='$appliance$mcid' >$mcid</a><br/>";
echo  "Time: $delta2 ";


?>