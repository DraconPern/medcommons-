<?PHP
require_once "version.inc.php";
require "../dbparams.inc.php";  // should go to the main database, not the mcback database
/*

returns xml status about the central system

*/



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
xm("<mcstatus>\n");//outer level 

xm("<requesturi>\n".$uri."</requesturi>\n");
}

function xmlend( $xml_status)
{
xm("<summary_status>".$xml_status."</summary_status>\n");
xm("</mcstatus>\n");//outer level 
xmlreply(true); // show its all good
exit;
}





function rowcount ($table)
{ 	$query = "SELECT COUNT(*) from $table";
 	$result = mysql_query ($query) or xmlend("can not query table $table ".mysql_error());
 	if ($result=="") {xmlend("failure"); exit;}
	$l = mysql_fetch_array($result,MYSQL_NUM);
	mysql_free_result($result);
	return $l[0];
}

function z($table){
			$x=rowcount($table);
			xm('<table name="'.$table.'" rowcount="'.$x.'" errors="0" />');
	}
	

function p($tag,$value){
				xm("<$tag>$value</$tag>");
}

				
	
//
// main program - parse the incoming parameters, and reject ill formed requests, or bad templates
//
	xmltop("mcstatus" ,false); //not in debug mode

	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or xmlend ("can not connect to mysql");
			
	$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or die ("can not connect to database $db");

	// get general info about this instance of central
		xm("<generalinfo>");
		p("name",$_SERVER['SERVER_NAME']);
		p("ip_addr",$_SERVER['SERVER_ADDR'].":".$_SERVER['SERVER_PORT']);
		p("host", $_ENV['HOSTNAME']);
		p("certauth",$_SERVER['SSL_SERVER_S_DN_OU']);
		p("referer",$_SERVER ['HTTP_REFERER']);
		p("time",gmstrftime("%b %d %Y %H:%M:%S")." GMT");
		p("apache_admin",$_SERVER['SERVER_ADMIN']);		
		xm("</generalinfo>");

		// get medcommmons parameters about this instance of central
		xm("<mcinfo>");
			p("sw_version",$GLOBALS["SW_Version"]);
				p("sw_revision",$GLOBALS["SW_Revision"]);
		p("db_connection",$GLOBALS["DB_Connection"]);
		p("db_database",$GLOBALS["DB_Database"]);
		p("default_repository", $GLOBALS['Default_Repository']);
		xm("</mcinfo>");
	
 		// get record counts from interesting tables	 	 			
		xm("<tableinfo>");
		z("hipaa");
		z("hipaa_trace");
		z("user");
// moved these tables to another database, really should reconnect to the other db to get them		
		
//		z("faxstatus");
//		z("ccstatus");
//		z("emailstatus");
	
	xm("</tableinfo>");
		$count++;

	
	

 
mysql_close();
		if ($count==0) xmlend("failure"); else xmlend("success");
?>
