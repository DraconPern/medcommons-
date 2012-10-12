<?php
require_once  "../dbparams.inc.php";



function contact($xmlFile){
	$str= @file($xmlFile);
	if ($str==FALSE) return FALSE;
	
	$count = count($str);
	echo "<br>".$xmlFile." returned ".$count."<br>";
	$blurb="";
	for ($i=0; $i<count($str); $i++){
		$blurb.=strip_tags($str[$i])." ";
 	print_r($str[$i])."<br>";
	};


$pos = strpos($blurb, 'all:');


// Note our use of ===.  Simply == would not work as expected
// because the position of 'a' was the 0th (first) character.
if ($pos === false) {
   $blurb = "{old gateway needs upgrade}"; 
} else {
     $blurb = substr($blurb,$pos);
}

//	echo $blurb."<br>";
	return $blurb;
}

function sendemail($email,$blurb)
{   
 	$message = <<<XXX

<HTML><HEAD><TITLE>MedCommons Status Alert Notification for $email</TITLE>
<META http-equiv=Content-Type content="text/html; charset=iso-8859-1">
</HEAD>
<BODY>
<p><small>Something is apparently wrong with our service<br>
----------------------------------------------------------------------------
<br></small>$blurb<br>
<br>
Please get over to <a href=https://secure.medcommons.net/memberservice/netstat.php>https://secure.medcommons.net/memberservice/netstat.php</a><br>
</BODY>
</HTML>
XXX;
				 	
							 
				 
$stat = @mail($email, "Status Alert From MedCommons", 
		$message,
     "From: MedCommons@{$_SERVER['SERVER_NAME']}\r\n" .
     "Reply-To: cmo.medcommons.net\r\n" .
     "bcc: cmo@medcommons.net\r\n".
     "Content-Type: text/html; charset= iso-8859-1;\r\n"
     );
if($stat) return "SENTALERT email to $email <br>"; else return "email send failure to $email <br> $message";
}

function alupdatefield($email)
{ 
$t=time();
$update = <<<VVV
	UPDATE alerted 
		SET last = '$t'
		WHERE (email = '$email')
VVV;


	$result = mysql_query($update) or
	die("alupdatefield failed : ".mysql_error());

}
function gwupdatefield($gw,  $field, $value)
{ 

$update = <<<VVV
	UPDATE gateways 
		SET $field = '$value' 
		WHERE (gateway = '$gw')
VVV;


	$result = mysql_query($update) or
	die("gwupdatefield failed : ".mysql_error());

}
//header('Content-Type: text/xml');

$db=$GLOBALS['DB_Database'];

//main
// get a select list of all gatways
$x=<<<xxx
<html><head><title> MedCommons Application Status Check for $db></title><meta http-equiv="refresh" content="20"></head><body><small>
xxx;
echo $x;
 
	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or die ("can not connect to mysql");
				$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");
 	 	 
 	$query = "SELECT * from gateways";

 	$result = mysql_query ($query) or die("can not query table gateways - ".mysql_error());
 	$errcount=0; $blurb = "";
	if ($result=="") {echo "?no gateways defined?"; exit;}
	
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
			
		$gateway = $l['gateway'];
		$nickname = $l['nickname'];
		$description = $l['description'];
		$status=$l['status'];
		$egroup = $l['egroup'];
		$gw = $gateway."status.do";
        $d = gmstrftime("%b %d %Y %H:%M:%S")." GMT";
		echo "CONTACT $d $gw<br>";
        $status = contact ($gw);
        $d = gmstrftime("%b %d %Y %H:%M:%S")." GMT";
        if ($status==FALSE) {gwupdatefield($gateway,"status","ER $d ");        	
        	$b= " FAILURE $d $gw could not be reached<br>";
         	echo $b;

        	//only report if egroup iw no5 null
        	if ($egroup!="")
        	{
        	$blurb.=$b;
        	$errcount++;}
        }
        else {
        $d = gmstrftime("%b %d %Y %H:%M:%S")." GMT";
        gwupdatefield($gateway,"status","OK $d <br>$status");
		echo " RUNNING $d $gw <br>$status<br>";
        }
	}
	mysql_free_result($result);
	
	// if any of the gateways are not in perfect shape, then check to see if anyone needs alerting
	if ($errcount>0){
		$query = "SELECT * from alerted";
		
 	$result = mysql_query ($query) or die("can not query table alerted - ".mysql_error());
 	$count=0;
	if ($result=="") {echo "?no parties to alert"; exit;}
	
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
			
		$email = $l['email'];
		$last = $l['last'];
		$frequency = $l['frequency'];
		$count++;
		$now=time(); //time now in seconds
//		echo "checking ".$last." ".$frequency." ".$now."<br>";
		if (($last+60*$frequency)<=$now){ //if long ago then
		//RENOTIFY
		echo sendemail($email,$blurb);
		alupdatefield($email);;
		}
        }
	mysql_free_result($result);
	}//errcount>0
mysql_close();
$x=<<<xxx
</body></html>
xxx;

echo $x;

?>