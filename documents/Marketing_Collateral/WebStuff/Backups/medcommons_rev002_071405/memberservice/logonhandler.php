<?PHP

require_once "../dbparams.inc.php";
//redirect a user after successful logon
function logon_redirect($g,$m,$username,$hpass,$email)
{//ok, this is working so set cookies and dispatch

setcookie("MCID",$m,time()+3600);
setcookie("MCGW",$g,time()+3600);

	$url = "$g/logon.jsp?mcid=$m&username=$username&hpass=$hpass&email=$email";
	$x=<<<XXX
<html><head><title>Redirecting to MedCommons Repository Gateway</title>
<meta http-equiv="REFRESH" content="0;url=$url"></HEAD>
<body >
<p>
Please wait while we redirect you to $url ...
</p>
</body>
</html>
XXX;
echo $x;
}

function error_redirect($err)
{
	$url = "logon.php?err=$err";
	$x=<<<XXX
<html><head><title>Please Logon to MedCommons</title>
<meta http-equiv="REFRESH" content="0;url=$url"></HEAD>
</html>
XXX;
echo $x;
}
// see if the username and password combo exist
$t1="users";
$username=$_POST['userid'];
$hpass=trim($_POST['password']);

	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or die ("can not connect to mysql");

				$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");

 
 	$query = "SELECT * from users WHERE (email='$username')";
 //and (hpass='$hpass')

 	$result = mysql_query ($query) or error_redirect("can not query table users - ".mysql_error());
 	$count=0;
	if ($result=="") {error_redirect("no such user"); exit;}
	
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
			
		$gateway1 = $l['gateway1'];
		$gateway2 = $l['gateway2'];
		$mcid = $l['mcid'];
		$hpass2=trim($l['hpass']);
		$email = $l['email'];
		$name = $l['name'];
		$count++;
	
	}
	mysql_free_result($result);
mysql_close();
///////////////////////////////////////////////////// 
$f=strcmp($hpass,$hpass2);
if (($f!=0) || ($count==0))
{echo "password or username problem $f";
exit;
error_redirect("there is a problem with your username or password");}
else 
logon_redirect($gateway1,$mcid,$name,$hpass,$email);

?>