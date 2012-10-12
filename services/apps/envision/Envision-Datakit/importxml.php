<?php
require_once "adminsetup.inc.php";

// Upload XML Simtrak Envision File

function clean($s)
{
	$s=  str_replace('"','',$s);
	return mysql_real_escape_string(trim($s));
}

function build_insert($label,$tree){
	if (!isset($GLOBALS['db_connected']) ){
		$GLOBALS['db_connected'] = mysql_connect($GLOBALS['DB_Connection'] ,$GLOBALS['DB_User'] );
		$db = $GLOBALS['DB_Database'];
		mysql_select_db($db) or die ("can not connect to database $db ".mysql_error());
	}
	$time1 = strftime('%D %T');

	$sql=$showsql ="Insert into $label set "; $pref='';
	$newline = 0;
	foreach($tree -> children() as $name => $value){
		$sql .= " $pref $name = '$value' ";
		$showsql .= " $pref $name = '$value' ";
		$pref = ','; if ($newline++ == 2) { $showsql .='<br/>  '; $newline=0;}
	}
	mysql_query($sql); // just do it
	$time2 = strftime('%D %T');
	$error = mysql_error();
	$boxclass = $error?'fberrorbox':'fbbluebox';
	$out = "<div class=$boxclass ><h3>execute sql statement at $time1 </h3>";
	$out .="<pre>
	$showsql
	</pre>";
	$out .="<h4>completed at $time2 with status $error</h4> </div>";

	return $out;
}
function build_multiple_inserts($label,$node)
{
	$out='';
	foreach ($node as $x) $out.= build_insert($label,$x);
	return $out;
}
//main


	$ret ='';
	$incoming = $_FILES['uploaded']['name'];
	//echo "Incoming Xmlfile:            " . $incoming . "\n<br/>";
	$zipfile = $_FILES['uploaded']['tmp_name'];
	//echo "Uploaded Zipfile:            " . $zipfile . "\n<br/>";
	$type = $_FILES['uploaded']['type'];
	//echo "File is of type ".$type."\n<br/>";
	if ( $type =='text/xml')
	{
		$zip = file_get_contents($zipfile);
		if ($zip)
		{	$buf = "
<div class=fbgreybox ><h2>Importing XML From Saved Envision File - $incoming</h2><a href=envision.php>back</a></div> ";
		$xml = simplexml_load_string($zip);
		$xfbid = $xml->exporter_fbid;
		foreach($xml->facebook_user_export as $fue)
		{
			$buf .= build_insert('users',$fue->user);
			$buf .= build_multiple_inserts('patients',$fue->patients->patient);
			$buf .= build_multiple_inserts('teams',$fue->familyteam->member);
			$buf .= build_multiple_inserts('teams',$fue->otherteams->giver);
		}
		//$buf .= "</div>";
		header ('Content-type:text/html');
		echo page_shell($buf); //
		}
		else echo "Cant open $zipfile \n<br/>";
	}
	else echo "Only Envision XML files can be uploaded\n<br/>";
	//echo $ret;

?>