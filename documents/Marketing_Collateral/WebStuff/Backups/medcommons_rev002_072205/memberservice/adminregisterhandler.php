<?php
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


function generate_mcid() {
//set the random id length 
return rand(1000,9999).rand(1000,9999).rand(1000,9999).rand(1000,9999);
}


function error_redirect($err)
{
	
	$url = "adminregister.php?err=$err";
	$x=<<<XXX
<html><head><title>Please Logon to MedCommons</title>
<meta http-equiv="REFRESH" content="0;url=$url"></HEAD>
</html>
XXX;
	echo $x;
}


function logon_redirect($err)
{
	$url = "logon.php?err=$err";
	$x=<<<XXX
<html><head><title>Please Logon to MedCommons</title>
<meta http-equiv="REFRESH" content="0;url=$url"></HEAD>
</html>
XXX;
	echo $x;
}
function newmember ($email,$status,$hpass)
{
	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or die ("can not connect to mysql");

			$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");
	 	 
	// generate a medcommons id
	$mcid = generate_mcid();
	$gateway1 = $GLOBALS['Default_Repository'];
	$wip = "";// no ip adddress makes this shared
	$wuserag = $_SERVER['HTTP_USER_AGENT'];
    // now write an entry in the mysql database
	$certURL="";
	$name = $email;
	$serial ="0123456789ABCDEF";
	$identityprovider = "MedCommons Admin";
	
	$insert="INSERT INTO users (mcid, name, hpass, gateway1, gateway2, certurl,serial,identityprovider,email,status,certchecked,
								wiredipaddress,wireduseragent)".
				"VALUES('$mcid','$name','$hpass','$gateway1','$gateway2',
				'$certUrl','$serial','$identityprovider','$email','$status',NOW(),'$wip','$wuserag'
				)";
	mysql_query($insert) or error_redirect("that account appears to already exist");
		// do not set the cookie that binds us 
		//setcookie("mcserial".$serial,$email,time()+60*60*24*366*3);//,'/',"",TRUE);// expire in 3 years



 
mysql_close();
	
}


$GLOBALS['buf']="";
$email = $_POST ['email'];
$hpass1 = $_POST['hpassword1'];
$hpass2 = $_POST['hpassword2'];

if ($hpass1!=$hpass2) error_redirect( "Sorry, the passwords don't match, you are not registered on MedCommons");
else {
 (newmember ($email,$status,$hpass1));
}
logon_redirect("Your account was created, please log on");
?>