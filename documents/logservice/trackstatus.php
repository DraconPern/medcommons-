<?PHP

require "../dbparams.inc.php";
/*

trackstatus.php - set the status of a particular existing hipaa log entry

*/


// common subs for onecall

function cleanreq($fieldname)
{ 
// take an input field from the command line or POST 
// and clean it up before going any further
$value = $_REQUEST[$fieldname];
$value = htmlspecialchars($value);
return $value;
}

//
//xml support - the xml doc we are building is buffered until the very end
//
function xmlreply ($makexml)
{
	// if we wanted this in debug mode then don't generate xml headers
	// or, if we are dying and want to see what's going on, return it as plain
		
	if (($makexml==false) or ($GLOBALS['debug']== true))
	{
	 		echo ("Showing Reply as HTML instead of XML\n\r");
	}
	else {
	// generate headers
	$mimetype = 'text/xml';
	$charset = 'ISO-8859-1';
	header("Content-type: $mimetype; charset=$charset");
	echo ('<?xml version="1.0" ?>'."\n");
 	}
	echo $GLOBALS['xmlString']; // this is where we can trace
}
function xm($s)
{ $GLOBALS['xmlString'].= $s;}
//
//outer frame of XML document response is implemented by 
//   calling xmltop {calls to xm}  calling xmlend()
//
function xmltop($t1,$debug)
{
$GLOBALS['xmlString']="";
$GLOBALS['debug'] = $debug; // if set, it will go as html not xml
xm("<logservice>\n");//outer level 
$srva = $_SERVER['SERVER_ADDR'];
$srvp = $_SERVER['SERVER_PORT'];
$gmt = gmstrftime("%b %d %Y %H:%M:%S");
$uri = htmlspecialchars($_SERVER ['REQUEST_URI']);
xm("<details>$srva:$srvp $gmt GMT</details>");
xm("<referer>".htmlspecialchars($_SERVER ['HTTP_REFERER'])."</referer>\n");
xm("<requesturi>\n".$uri."</requesturi>\n");
}

function xmlend( $xml_status)
{
xm("<summary_status>".$xml_status."</summary_status>\n");
xm("</logservice>\n");//outer level 
xmlreply(true); // show its all good
exit;
}


	function z($l,$v){	if ($l[$v]!="") xm ("<$v>".$l[$v]."</$v>");}

function updatefield($mcid, $tracking,  $field, $value)
{ 
	
	$link=mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or xmlend ("can not connect to mysql");
	$db = $GLOBALS['DB_Database'];
	mysql_select_db($db,$link) or die ("can not connect to database $db");
	
	
//
//

$update = <<<VVV
	UPDATE hipaa 
		SET $field = '$value' , time=NOW()
		WHERE (tracking_number='$tracking')AND (a2='$mcid');
VVV;





	xm("<update>$update</update>");
	$result = mysql_query($update,$link) or
	xmlend ("updatefield failed : ".mysql_error($link));
	$num_rows = mysql_affected_rows($link);
	if ($num_rows==0) xmlend("failed - no rows updated");
	$query = <<<VVV
		SELECT * from hipaa WHERE (tracking_number='$tracking') AND (a2='$mcid')
VVV;
		xm("<query>$query</query>");

	$result = mysql_query ($query) or xmlend("can not query table hipaa - ".mysql_error());
 	
	if ($result=="") return false;
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
	
		$time = $l['creation_time'];
		$tracking = $l['tracking_number'];
		$hpin = $l['hpin'];
		$a1 = $l['a1'];
		$a2 = $l['a2'];
		$a3 = $l['a3'];
		$a4 = $l['a4'];
		$s1 = $l['s1'];
		$s2 = $l['s2'];
		$s3 = $l['s3'];
		$s4 = $l['s4'];
		
		$insert = <<<VVV
	
		INSERT INTO hipaatrace (creation_time,tracking_number,hpin,a1,a2,a3,a4,s1,s2,s3,s4)
		VALUES(NOW(),'$tracking','$hpin','$a1','$a2','$a3','$a4','$s1','$s2','$s3','$s4')
VVV;
		xm("<insert>$insert</insert>");

		$ans = mysql_query ($insert) or xmlend("can not insert table hipaatrace - ".mysql_error());

		
	}
	mysql_close($link);
}	
//
// main program - parse the incoming parameters, and reject ill formed requests, or bad templates
//
xmltop("hipaa" ,false); //not in debug mode

$mcid=cleanreq('mcid');
$tr=cleanreq('tr');
$status=cleanreq('status');



// if any new fields are specified then do the update
if ($status!="") updatefield ($mcid,$tr,"s1",$status); // if new password
else xmlend("new status must be non-null");
xmlend("success");
?>