<?PHP

require "../dbparams.inc.php";


function tracking_redirect($g,$t)
{
	$url = "$g/tracking.jsp?tracking=$t";
	$x=<<<XXX
<html><head><title>Redirecting to MedCommons Repository Gateway $g</title>
<meta http-equiv="REFRESH" content="0;url='$url'"></HEAD>
<body >
<p>
Please wait...
</p>
</body>
</html>
XXX;
echo $x;
}
//pick a random entry from the hipaa log and go there
$t1 = "hipaa";



// lookup by tracking number 


mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or die ("can not connect to mysql");
	$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");
	$query = "SELECT * from $t1 WHERE (tracking_number = '202829973846') LIMIT 1";

 	$result = mysql_query ($query) or die("can not query table $t1 - ".mysql_error());
 	
	if ($result!="") {

	

	// if here, we have a record - redirect to that place
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
		$gatewayurl = $l['s4'];
		$tracking = $l['tracking'];
			mysql_free_result($result);

		tracking_redirect($gatewayurl,$tracking);
		exit;
	}
	}
	
	// if here we have no match
$x = <<<XXX
<html>
<head>
<title>MedCommons eReferral Redirect Error</title>
</head><body>
<p>
Regrettably, we are unable to locate CCR $tracking
</body>
</html>
XXX;
echo $x;
exit;

 

?>