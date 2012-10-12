<?php
require_once  "../dbparams.inc.php";

// show status of the network

$db=$GLOBALS['DB_Database'];
$a = $GLOBALS['DB_Connection'];
$r = $GLOBALS['Default_Repository'];

$srva = $_SERVER['SERVER_ADDR'];
$srvp = $_SERVER['SERVER_PORT'];
$gmt = gmstrftime("%b %d %Y %H:%M:%S")." GMT";
$uri = htmlspecialchars($_SERVER ['REQUEST_URI']);

//main
// get a select list of all gatways
$x=<<<xxx
<html><head><title>MedCommons Network Status Check for $db</title><meta http-equiv="refresh" content="50"></head><body>
<h4>MedCommons Network Status for $db</h4>
<p>Central Database $db on $srva:$srvp is operational at $gmt<br>
Default Repository is $r<br>
Request URI is $uri
</p>

<small>Background poller runs here - it must run for alert notifications to work:</small><br>
<iframe src=gwprober.php height=100 width=800></iframe>
xxx;

 
	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or die ("can not connect to mysql");
				$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");
 	 	 
 	$query = "SELECT * from gateways";

 	$result = mysql_query ($query) or die("can not query table gateways - ".mysql_error());
 	$count=0;
	if ($result=="") {echo "?no gateways defined?"; exit;}
	
	echo $x;// if here, we have a good database
	echo "<table><tr><td><b>Gateway<b></td><td><b>Status<b></td><td><b>Description<b></td></tr>";
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
			
		$gateway = $l['gateway'];
		$nickname = $l['nickname'];
		$description = $l['description'];
		$status=$l['status'];
		$egroup = $l['egroup'];
		$count++;
		echo "<tr><td>$gateway</td><td>$status</td><td>$description</td></tr>";

        }
	mysql_free_result($result);
	echo "</table>";
	
	//
	
	$query = "SELECT * from alerted";

 	$result = mysql_query ($query) or die("can not query table alerted - ".mysql_error());
 	$count=0;
	if ($result=="") {echo "?no parties to alert"; exit;}
	echo "<br><small>the parties listed below will receive alerts</small>";
	echo "<table><tr><td><b>Email<b></td><td><b>Frequency (mins)<b></td></tr>";
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
			
		$email = $l['email'];
		$last = $l['last'];
		$frequency = $l['frequency'];

		$count++;
		echo "<tr><td>$email</td><td>$frequency</td></tr>";

        }
	mysql_free_result($result);
	mysql_close();
$x=<<<xxx
</table></body></html>
xxx;

exit;

?>