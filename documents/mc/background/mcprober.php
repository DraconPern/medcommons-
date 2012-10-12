<?php
require_once  "../../dbparamsmcback.inc.php";
require_once "../version.inc.php";
function out($depth,$s,$val)
{
//	for ($i=0; $i<$depth;$i++) {echo "    ";};
//	echo $s." =>".$val."<br>";
}

function process_xml($ctprot,$cthost,$ctport,$ctfile,
$configuration)
{
	$fv = ""; // this goes in the middle of an UPDATE SQL statement we are



	out (0,"details ",$configuration->details);
	out (0,"requesturi ",$configuration->requesturi);

	$gi = $configuration->generalinfo;
	out (1,"name",$gi->name);
	$fv.="name = '$gi->name', ";
	out (1,"ip_addr",$gi->ip_addr);
	$fv.="ipaddr ='$gi->ip_addr', ";
	out (1,"host",$gi->host);
	$fv.="host ='$gi->host', ";
	out (1,"certauth",$gi->certauth);
	$fv.="certauth ='$gi->certauth', ";
	out (1,"time",$gi->time);
	$fv.="time ='$gi->time', ";
	out (1,"apache_admin",$gi->apache_admin);
	$fv.="apacheadmin ='$gi->apache_admin', ";

	$mc = $configuration->mcinfo;
	
		$fv.="swversion ='$mc->sw_version', ";
		
		$fv.="swrevision ='$mc->sw_revision', ";
		
		
		$fv.="dbconnection ='$mc->db_connection', ";

		$fv.="dbdatabase ='$mc->db_database', ";

		$fv.="defaultrepository ='$mc->default_repository', ";


	$ti = $configuration->tableinfo;
	foreach ($ti->table as $table) {
	//	out (2,"table name", $table['name']);
	//	out (2,"rowcount",$table['rowcount']);
		switch ($table['name'])

		{
				case "faxstatus":  
				$rc=$table['rowcount'];
				$re=$table['errors'];
				$fv.="ofaxcount ='$rc', ofaxerrs = '$re', ";
				break;	
				
					case "ccstatus":  
				$rc=$table['rowcount'];
				$re=$table['errors'];
				$fv.="opaycount ='$rc', opayerrs = '$re', ";
				break;	
				
					case "emailstatus":  
				$rc=$table['rowcount'];
				$re=$table['errors'];
				$fv.="oemailcount ='$rc', oemailerrs = '$re', ";
				break;	
			
			
			case "hipaa": 
				$rc=$table['rowcount'];
				$fv.="hipaacount ='$rc', ";
				break;
			case "hipaa_trace": 
				$rc=$table['rowcount'];
				$fv.="hipaatracecount ='$rc', ";
				break;
			case "users": 
				$rc=$table['rowcount'];
				$fv.="usercount ='$rc',";
				
		}


	}
	
	out (0,"summary_status ",$configuration->summary_status);
	$fv.="summarystatus = '$configuration->summary_status' ";


	$update = <<<VVV
	UPDATE centralprobes 
		SET $fv 
		WHERE (cthost = '$cthost') AND (ctprot = '$ctprot') AND (ctport ='$ctport') AND (ctfile='$ctfile')
VVV;

	$result = mysql_query($update) or
	die("ctupdatefield failed : ".mysql_error());
	$nr = mysql_affected_rows();
//	echo " rows $nr ";
}



function contact($xmlFile, $gwprot,$gwhost,$gwport,$gwfile){
//	echo "Getting $xmlFile";
	$str= @file_get_contents($xmlFile);//file2($gwprot,$gwhost,$gwport,$gwfile);//@file($xmlFile);@file($xmlFile);//
	if ($str===FALSE) {return FALSE;}

	$count = strlen($str);
//	echo "<br>".$xmlFile." returned ".$count."<br>";


	$xml = @simplexml_load_string($str);
	if ($xml===FALSE) {return FALSE;}

//	var_dump($xml);

	process_xml($gwprot,$gwhost,$gwport,$gwfile,$xml);

	// Note our use of ===.  Simply == would not work as expected
	/* because the position of 'a' was the 0th (first) character.
	if ($count === false) {
	return "{no xml returned}";
	} else {
	$str = substr($str,$pos);
	$blurb="";
	for ($i=0; $i<strlen($str); $i++){
	$blurb.=strip_tags($str[$i])." ";
	print_r($str[$i])."<br>";

	};*/


	//	echo $blurb."<br>";
	return "OK";// $blurb;
}


function ctupdatefield($ctprot,$cthost,$ctport,$ctfile,  $field, $value)
{

	$update = <<<VVV
	UPDATE centralprobes 
		SET $field = '$value' 
		WHERE (cthost = '$cthost') AND (ctprot = '$ctprot') AND (ctport ='$ctport') AND (ctfile='$ctfile')
VVV;


	$result = mysql_query($update) or
	die("ctupdatefield failed : ".mysql_error());

}
//header('Content-Type: text/xml');

$db=$GLOBALS['DB_Database'];

$ver=$GLOBALS['SW_Version']."-".$GLOBALS['SW_Revision'];


$x=<<<xxx
<html><head><title> MedCommons NETWORK Prober for $db></title><meta http-equiv="refresh" content="30"></head><body>
<h4>MC Central Prober for $db sw version $ver</h4><small>
xxx;
echo $x;

mysql_connect($GLOBALS['DB_Connection'],
$GLOBALS['DB_User'],
$GLOBALS['DB_Password']
) or die ("can not connect to mysql");
$db = $GLOBALS['DB_Database'];
mysql_select_db($db) or die ("can not connect to database $db");

$query = "SELECT * from centralprobes";

$result = mysql_query ($query) or die("can not query table centralprobes - ".mysql_error());
$errcount=0; $blurb = "";
if ($result=="") {echo "?no central sites defined?"; exit;}

while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
	$ctprot = $l['ctprot'];
	$cthost = $l['cthost'];
	$ctport= $l['ctport'];
	$ctfile = $l['ctfile'];
	$nickname = $l['nickname'];
	$description = $l['description'];
	$ct = $ctprot."://".$cthost./*":".$ctport.*/$ctfile;//wld now passed in from database
	$d = gmstrftime("%b %d %Y %H:%M:%S")." GMT";
	echo "<br>CONTACT $d $ct<br>";
	$status = contact ($ct,$ctprot,$cthost,$ctport,$ctfile);
	if ($status===FALSE) 
		{ctupdatefield($ctprot,$cthost,$ctport,$ctfile,"summarystatus","ER $d ");
		$b= " FAILURE $d $ct could not be reached<br>";
		echo $b;
		}
		else
		{
		$d = gmstrftime("%b %d %Y %H:%M:%S")." GMT";
		ctupdatefield($ctprot,$cthost,$ctport,$ctfile,"summarystatus","OK $d");
		echo " RUNNING $d $ct <br>$status<br>";
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