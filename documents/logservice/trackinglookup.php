<?PHP

require "../dbparams.inc.php";


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
xm("<logservice>\n<log table='$t1'>\n");//outer level 
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
xm("</log>\n</logservice>\n");//outer level 
xmlreply(true); // show its all good
exit;
}

	function z($l,$v){	if ($l[$v]!="") xm ("<$v>".$l[$v]."</$v>");}

	
//
// main program - parse the incoming parameters, and reject ill formed requests, or bad templates
//
$t1="hipaa";
$tracking = cleanreq('tracking');
$tracking = str_replace(array(' ','=','?',':','-'),
                   "",
                   $tracking);
$hpin = cleanreq('hpin'); // if present it is the hashed pin value


xmltop($t1 ,false); //not in debug mode

	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or xmlend ("can not connect to mysql");
			
	$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");
// a1 must be present to get anything done at all
	$query = "SELECT * from $t1 WHERE (tracking_number = '$tracking') and (hpin = '$hpin')";
 	$result = mysql_query ($query) or xmlend("can not query table $t1 - ".mysql_error());
 	$count = 0; 
	if ($result!="") {
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
		$time = $l[' creation_time'];
		
		xm("<entry time='$time'>");
	    z($l,'tracking'); z($l,'hpin');
		z($l,'a1'); z($l,'a2'); z($l,'a3'); z($l,'a4');
		z($l,'s1'); z($l,'s2'); z($l,'s3'); z($l,'s4');
		xm("</entry>");
		$count++;
	
	}
	
	}
	mysql_free_result($result);
 
mysql_close();
xmlend(($count==0)?"failure":"success");

?>