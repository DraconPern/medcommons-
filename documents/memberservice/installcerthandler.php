<?php
require_once "getcertinfo.inc.php";
require_once "../dbparams.inc.php";

function emit($s)
{
	$GLOBALS['buf'].=$s;
};

function by($s)
{   $count = strlen ($s);
echo "bytecount ".strlen($s)." ";
for ($i=0; $i<$count; $i++)
echo $i." ".substr($s,$i,1)."*";
}

function dump($certUrl,	$serial, $identityprovider,$name,$email,$status)
{

	emit ("serial: $serial <br>");
	emit ("identityprovider: $identityprovider <br>");
	emit ("name: $name <br>");
	emit ("email: $email <br>");
	emit ("status: $status <br><br>");
	if ($status!='Valid') emit ("<b>==>invalid certificate status $status</b><br>");
}

function newmember ($certUrl,$serial,$identityprovider,$name,$email,$status,$hpass)
{
	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or die ("can not connect to mysql");
	$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");
	 	 
	// generate a medcommons id
	$mcid = $_REQUEST['mcid'];
	$gateway1 = $GLOBALS['Default_Repository'];
	$wip = $_SERVER['REMOTE_ADDR'];
	$wuserag = $_SERVER['HTTP_USER_AGENT'];
    // now write an entry in the mysql database

	$insert="INSERT INTO user (medcommons_user_id, name, hpass, gateway1, gateway2, cert_url,serial,identity_provider,email_address,status,cert_checked,
								wired_ipaddress,wired_useragent)".
				"VALUES('$mcid','$name','$hpass','$gateway1','$gateway2',
				'$certUrl','$serial','$identityprovider','$email','$status',NOW(),'$wip','$wuserag'
				)";
	mysql_query($insert) or die("can not insert into table users - ".mysql_error());
		setcookie("mcserial".$serial,$email,time()+60*60*24*366*3);//,'/',"",TRUE);// expire in 3 years



 
mysql_close();
	
}


$GLOBALS['buf']="";
$certUrl=$_POST['certurl'];
$hpass1 = $_POST['hpassword1'];
$hpass2 = $_POST['hpassword2'];

if ($hpass1!=$hpass2) emit( "Sorry, the passwords don't match, you are not registered on MedCommons");
else {
	if (FALSE==getcertinfo($certUrl, // get certinfo loads all these fields
	$serial, $identityprovider,$name,$email,$status)) emit ("cant reach $certUrl; you are not registered<br>");
	else emit (newmember ($certUrl,$serial,$identityprovider,$name,$email,$status,$hpass1));
	dump($certUrl,	$serial, $identityprovider,$name,$email,$status);
}

$outstring = $GLOBALS['buf'];

$x = <<<XXX
<html>
<BODY BGCOLOR="ffffff" LINK="000066">
<TABLE BORDER=0 WIDTH=500 CELLPADDING=4 CELLSPACING=0>
<tr><td><FONT COLOR="0000cc" FACE="verdana, arial, helvetica" ><b>$outstring</b></font></td></tr>
</table>
</body>
</html>
XXX;

echo $x;
?>