<?php
require_once  "../../dbparamsmcback.inc.php";
require_once "../version.inc.php";



function file2($prot, $host,$port,$file)
{

	$fp = @fsockopen($host,$port, $errno, $errstr, 30);
	if (!$fp) {
		echo "$errstr ($errno)<br />\n"; return FALSE;
	} else {
		$out = "GET $file HTTP/1.1\r\n";
		$out .= "Host: $host:$port\r\n";
		$out .= "Connection: Close\r\n\r\n";

		fputs($fp, $out);
		$ret="";
		while (!feof($fp)) {
			$ret.= fgets($fp, 128);
		}
		fclose($fp);
		return $ret;
	}
}
function contact($xmlFile, $gwprot,$gwhost,$gwport,$gwfile,&$version,&$revision)
{
	$str= file2($gwprot,$gwhost,$gwport,$gwfile);//@file($xmlFile);
	if ($str==FALSE) return FALSE;
// parse here*******************continue here ****
	$count = strlen($str);
	//	echo "<br>".$xmlFile." returneds ".$count."<br>";
	$posa = strpos($str,"<b>Version");
	if ($posa===false)
	{
		//xml response
			$posa = strpos($str,"<version>");

			$posb = strpos($str,"</version>",$posa);
	
	$posc = strpos($str,"<revision>");
	$posd = strpos($str,"</revision>",$posc);
	
	// Note our use of ===.  Simply == would not work as expected
	// because the position of 'a' was the 0th (first) character.
	if ($posa === false) {
		return "{old gateway needs upgrade}";
	} else {
		$version = substr($str,$posa+9,$posb-$posa-9);
				$revision = substr($str,$posc+10,$posd-$posc-10);
	}
	}
	else {
		//old form
	$posb = strpos($str,"<br/>",$posa);
	
	$posc = strpos($str,"<b>Revision");
	$posd = strpos($str,"<br/>",$posc);
	
	// Note our use of ===.  Simply == would not work as expected
	// because the position of 'a' was the 0th (first) character.
	if ($posa === false) {
		return "{old gateway needs upgrade}";
	} else {
		$version = substr($str,$posa+15,$posb-$posa-15);
				$revision = substr($str,$posc+18,$posd-$posc-18);

	}

}

//	echo $blurb."<br>";
return "OK";// $blurb;
	
}

function gwupdatefields($gwprot,$gwhost,$gwport,$gwfile, $status,$version,$revision)
{

	$update = <<<VVV
	UPDATE gwprobes 
		SET status = '$status' , swversion = '$version', swrevision = '$revision'
		WHERE (gwhost = '$gwhost') AND (gwprot = '$gwprot') AND (gwport ='$gwport') AND (gwfile='$gwfile')
VVV;


	$result = mysql_query($update) or
	die("gwupdatefield failed : ".mysql_error());

}
function gwupdatefieldstatus($gwprot,$gwhost,$gwport,$gwfile, $status)
{

	$update = <<<VVV
	UPDATE gwprobes 
		SET status = '$status'
		WHERE (gwhost = '$gwhost') AND (gwprot = '$gwprot') AND (gwport ='$gwport') AND (gwfile='$gwfile')
VVV;


	$result = mysql_query($update) or
	die("gwupdatefield failed : ".mysql_error());

}
//header('Content-Type: text/xml');

$db=$GLOBALS['DB_Database'];
$ver=$GLOBALS['SW_Version']."-".$GLOBALS['SW_Revision'];

//main
// get a select list of all gatways
$x=<<<xxx
<html><head><title> MedCommons Gateway Prober for $db></title><meta http-equiv="refresh" content="30"></head><body>
<h4>MC Gateway Prober for $db sw version $ver</h4><small><br>
xxx;
echo $x;

mysql_connect($GLOBALS['DB_Connection'],
$GLOBALS['DB_User'],
$GLOBALS['DB_Password']
) or die ("can not connect to mysql");
$db = $GLOBALS['DB_Database'];
mysql_select_db($db) or die ("can not connect to database $db");

$query = "SELECT * from gwprobes";

$result = mysql_query ($query) or die("can not query table gwprobes - ".mysql_error());
$errcount=0; $blurb = "";
if ($result=="") {echo "?no gateways defined?"; exit;}

while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
	$gwprot = $l['gwprot'];
	$gwhost = $l['gwhost'];
	$gwport= $l['gwport'];
	$gwfile = $l['gwfile'];
	$nickname = $l['nickname'];
	$description = $l['description'];
	$status=$l['status'];
	$egroup = $l['egroup'];
	$gw = $gwprot."://".$gwhost.":".$gwport.$gwfile;//wld now passed in from database
	$d = gmstrftime("%b %d %Y %H:%M:%S")." GMT";
	echo "CONTACT $d $gw<br>";
	$status = contact ($gw,$gwprot,$gwhost,$gwport,$gwfile,$version,$revision);
	$d = gmstrftime("%b %d %Y %H:%M:%S")." GMT";
	if ($status==FALSE) {gwupdatefieldstatus($gwprot,$gwhost,$gwport,$gwfile,"ER $d ");
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
		gwupdatefields($gwprot,$gwhost,$gwport,$gwfile,"$status $d",$version,$revision);
		echo " RUNNING $d $gw <br>$status<br>";
	}
}
mysql_free_result($result);

//errcount>0
mysql_close();
$x=<<<xxx
</body></html>
xxx;

echo $x;

?>