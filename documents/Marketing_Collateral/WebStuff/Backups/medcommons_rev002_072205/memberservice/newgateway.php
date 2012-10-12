<?PHP

require "../dbparams.inc.php";
/*
add a new gateway
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

function generate_tracking() {
//set the random id length 
return rand(100000,999999).rand(100000,999999);
}

	function z($l,$v){	if ($l[$v]!="") xm ("<$v>".$l[$v]."</$v>");}

	
//
// main program - parse the incoming parameters, and reject ill formed requests, or bad templates
//
xmltop($t1 ,false); //not in debug mode

$nickrname=cleanreq('nickname');
$description=cleanreq('description');
$gateway=cleanreq('gateway');
$status=cleanreq('status');
$egroup=cleanreq('egroup');


if ($gateway=="") xmlend("gateway must be non-null");


$t1="gateways";



	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or xmlend ("can not connect to mysql");
	$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");

 	 	 

    // now write an entry in the mysql database

	$insert="INSERT INTO $t1 (nickname, gateway, description,status,egroup)".
				"VALUES('$nickname','$gateway','$description','$status'.'$egroup')";
	mysql_query($insert) or xmlend("can not insert into table $t1 - ".mysql_error());

 
mysql_close();
xmlend("success");
?>
