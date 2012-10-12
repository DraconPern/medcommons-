<?PHP
//handles tracking coming in via email referrals
require "../dbparams.inc.php";


function error_redirect($err)
{
	$url = "../memberservice/logon.php?err=$err";
	$x=<<<XXX
<html><head><title>Please Logon to MedCommons</title>
<meta http-equiv="REFRESH" content="0;url=$url"></HEAD>
</html>
XXX;
echo $x;
}

function logon_redirect($g)
{

		$url = "trackhack.php";

	$x=<<<XXX
<html><head><title>Redirecting to MedCommons Repository Gateway</title>
<meta http-equiv="REFRESH" content="0;url=$url"></HEAD>
<body >
<p>
Please wait while we redirect to $url ...
</p>
</body>
</html>
XXX;
echo $x;
exit;
}
function tracking_redirect($g,$t,$pin)
{
	$url = "$g/tracking.jsp?tracking=$t&hpin=$pin";
	$x=<<<XXX
<html><head><title>Redirecting to MedCommons Repository Gateway</title>
<meta http-equiv="REFRESH" content="0;url=$url"></HEAD>
<body >
<p>
Please wait while we redirect to $url ...
</p>
</body>
</html>
XXX;
echo $x;
}
$tracking = $_POST['trackingNumber'];

$tracking = str_replace(array(' ','=','?',':','-'),
                   "",
                   $tracking);
                   
$pin = $_POST['hpin']; //just get hashed value



if ($tracking == "666") logon_redirect($GLOBALS['Default_Repository']);

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
		$pin2 = $l['hpin'];
		
		mysql_free_result($result);
		
//		if (strcmp($pin,$pin2)!=0) error_redirect("invalid pin or tracking number"); else
		tracking_redirect($gatewayurl,$tracking,$pin); //send original pin
		exit;
	}
	}
	
	// if here we have no match
error_redirect("Regrettably, we are unable to locate CCR $tracking");
 

?>