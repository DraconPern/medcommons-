<?PHP

require_once "../dbparams.inc.php";
//redirect a user after successful logon
function logon_redirect($g,$m,$username,$hpass,$email)
{//ok, this is working so set cookies and dispatch

setcookie("MCID",$m,time()+3600);
setcookie("MCGW",$g,time()+3600);
//temporarily abandonded
$url = "$g/logon.jsp?mcid=$m&username=$email&hpass=$hpass&email=$email";
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

$username=$_POST['userid'];
$hpass=trim($_POST['password']);

mysql_connect($GLOBALS['DB_Connection'],
$GLOBALS['DB_User'],
$GLOBALS['DB_Password']
) or die ("can not connect to mysql");

$db = $GLOBALS['DB_Database'];
mysql_select_db($db) or die ("can not connect to database $db");


$query = "SELECT * from user WHERE (email_address ='$username')";
//and (hpass='$hpass')

$result = mysql_query ($query) or error_redirect("can not query table users - ".mysql_error());
$count=0;
if ($result=="") {error_redirect("no such user"); exit;}

while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {

	$gateway1 = $l['gateway1'];
	$gateway2 = $l['gateway2'];
	$mcid = $l['medcommons_user_id'];
	$hpass2=trim($l['hpass']);
	$email = $l['email_address'];
	$name = $l['name'];
	$serial = $l['serial'];
	$wip = $l['wired_ipaddress'];
	$count++;

}
mysql_free_result($result);

/////////////////////////////////////////////////////
// check if we got a record
if ($email != $username) {error_redirect("there is a problem with your username or password."); exit;}
if ($wip!="") { // special admin accounts have wip=null
$ip = $_SERVER['REMOTE_ADDR'];
// either the ip address must agree with the registered address, or the cookie must match, otherwise, dont
///

if ($ip != $wip)
{ // ip address has changed, see if cookie is still present and valid
//echo "IP MISMATCH $ip REGISTERED $wip ";

$cookieval = $_COOKIE['mcserial'.$serial];

if ($email != $cookieval)
{error_redirect ("you appear to have switched computers and thus can not log on \r\nplease create a distinct account for each computer you are using");
exit;
}
// rewrite the ip address
//echo "SERIAL MATCH";

$update = "UPDATE user SET wired_ipaddress='$ip' WHERE (email_address='$email');";
	$result = mysql_query ($update)or error_redirect("can not update table users on ip rewrite- ".mysql_error());
	
     mysql_close();
//     exit;
}
else
	{
// ip address is ok, if the cookie has changed then rewrite it
//echo "IP OK ";
if ($email != ($_COOKIE["mcserial".$serial]))
{
		setcookie("mcserial".$serial,$email,time()+60*60*24*366*3);//,'/',"",TRUE);// expire in 3 years
//		echo "SETTING NEW COOKIE";
//		exit;
}
	}
}
$f=strcmp($hpass,$hpass2);
if (($f!=0) || ($count==0))
{
error_redirect("there is a problem with your username or password");}
else 

logon_redirect($gateway1,$mcid,$name,$hpass,$email);

?>