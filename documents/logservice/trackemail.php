<?PHP

require "../dbparams.inc.php";

function unmerge_tracking_mcid($num,&$t,&$m)
{
	$m = substr($num,0,4).substr($num,8,4).substr($num,16,4).substr($num,24,4);
	$t = substr($num,20,4).substr($num,12,4).substr($num,4,4);
}
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

// look up the tracking number to 
$a = $_REQUEST['a'];


unmerge_tracking_mcid($a,$tracking,$mcid);//reconstitute

// lookup by tracking number 


mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or die ("can not connect to mysql");
	$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");
	$query = "SELECT * from hipaa WHERE (tracking_number = '$tracking')";
 	$result = mysql_query ($query) or die("can not query table hipaa - ".mysql_error());
 	
	if ($result!="") {

	

	// if here, we have a record - redirect to that place
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
		$gatewayurl = $l['s4'];
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