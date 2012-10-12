<?PHP

require "../dbparams.inc.php";
/*

getmemberinfo.php - retrieve info associated with a particular user

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
xm("<memberservice>\n");//outer level 
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
xm("</memberservice>\n");//outer level 
xmlreply(true); // show its all good
exit;
}



	function z($l,$v){	if ($l[$v]!="") xm ("<$v>".$l[$v]."</$v>");}


	
//
// main program - parse the incoming parameters, and reject ill formed requests, or bad templates
//
xmltop("users" ,false); //not in debug mode


$username=cleanreq('username');
$hpass=cleanreq('hpass');


if ($username=="") xmlend("username must be non-null");

$t1="users";



	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or xmlend ("can not connect to mysql");
			
				$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");

 	 	 
 	$query = "SELECT * from $t1 WHERE (email='$username') and (hpass='$hpass')";
 	$result = mysql_query ($query) or xmlend("can not query table $t1 - ".mysql_error());
 	$count=0;
	if ($result=="") {xmlend("failure"); exit;}
	
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
			
		xm("<entry >");
	    z($l,'email'); z($l,'hpass');z($l,'name');
		z($l,'mcid'); z($l,'gateway1'); z($l,'gateway2');
		
		z($l,'serial'); z($l,'identityprovider'); z($l,'certurl');
		z($l,'certchecked');z($l,'status');
		xm("<username>".$l['name']."</username>"); // preserve compatibility
		
		xm("</entry>");
		$count++;
	
	}
	
	mysql_free_result($result);

 
mysql_close();
		if ($count==0) xmlend("failure"); else xmlend("success");
?>
