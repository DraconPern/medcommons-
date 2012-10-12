 <?PHP
//xmlheader.inc - all xml based processing responses come thru here
// basic hedaer is put out
// get db parameters, we have no other context
require_once("../dbconfig.inc");
require_once("../lib/config.inc"); //not wild about this

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
	//$mimetype = 'text/xml';
	//$charset = 'ISO-8859-1';
	//header("Content-type: $mimetype; charset=$charset");
	echo ('<?xml version="1.0" ?>'."\n");
 	}

	echo $GLOBALS['xmlString']; // this is where we can trace

	
}


function xm($s)
{ $GLOBALS['xmlString'].= $s;}

function xmltop($req,$debug)
{
$GLOBALS['xmlString']="";
$GLOBALS['debug'] = $debug; // if set, it will go as html not xml
$time_start = microtime(true); // start time - finishes in xmlfooter

readconfig();

xm("<call_pink>\n");//outer level 
$srva = $_SERVER['SERVER_ADDR'];
$srvp = $_SERVER['SERVER_PORT'];
$gmt = gmstrftime("%b %d %Y %H:%M:%S");
$major = $GLOBALS['SchemaMajorVersion'];
$minor = $GLOBALS['SchemaMinorVersion'];
$uri = htmlspecialchars($_SERVER ['REQUEST_URI']);
xm("<gateway>".cleanreq('gateway')."</gateway>");
if (cleanreq('recording')=='on') 
		insertreplay(cleanreq('gateway'),time(),$uri);
xm("<details>(".$GLOBALS['SW_MajorVersion']."."
			.$GLOBALS['SW_MinorVersion'].") $srva:$srvp $gmt GMT</details>");
xm("<database>".$GLOBALS['DB_Connection']."-".$GLOBALS['DB_Database']."-".
		$GLOBALS['DB_User']."(".$major.".".$minor.")"."</database>\n");
xm("<referer>".htmlspecialchars($_SERVER ['HTTP_REFERER'])."</referer>\n");
xm("<requesturi>\n".$uri."</requesturi>\n");
xm("<sqlrequest>\n".htmlspecialchars($req)."</sqlrequest>\n");
xm("<response>\n");
return($time_start);
}

function xmlend($time_start, $xml_status)
{
$time_end = microtime(true);
$time_elapsed = $time_end - $time_start;
xm("<elapsed>".$time_elapsed."</elapsed>\n");
xm("<summary_status>".$xml_status."</summary_status>\n");
xm("</response>\n");
xm("</call_pink>\n");//outer level 
tracelog('FOO1',$xml_status,'things are looking better', 
                       $GLOBALS['xmlString']); //use a separate connection
xmlreply(true); // show its all good
db_close ($GLOBALS['DB_Link']);
}

function xmlrowcols($result)
{
$number_rows = db_num_rows($result);
$number_cols = db_num_fields($result);
/* Outputting results in XML */
xm("<rows>".$number_rows."</rows>\n");
xm("<cols>".$number_cols."</cols>\n");
}


function xmltabledump ($table,$result)
{
$number_cols = db_num_fields($result);
xm("<table>\n");
xm("<name>".$table."</name>\n");
xmlrowcols($result); //show rows and columns (why?)

//col headers
while ($line = db_fetch_array_index($result)) {
 xm("<row>\n");  
for ($i=0; $i<$number_cols;++$i){
	xm("<".db_field_name($result,$i).">\n");
	xm( htmlspecialchars($line[$i]),"\n");
	xm("</".db_field_name($result,$i).">\n");
	}
 xm("</row>\n"); 
}//while loop
xm("</table>\n");
}

function xmldie($mess)
{// very carefully, avoid recursive loops, don't go to the tracelog
xm("<status>666</status>\n");
xm("<sqlcode>".$mess."</sqlcode>\n");
$time_end = microtime(true);
$time_elapsed = $time_end - $time_start;
xm("<elapsed>".$time_elapsed."</elapsed>\n");
xm("<summary_status>"."665"."</summary_status>\n");
xm("</response>\n");
xm ("</call_pink>\n");//outer level 
xmlreply(false); //put this out onto the screen and not as xml
exit; //look into this
}

function xmlquery($table,$query,$debug)
{
// does a select and generates xml response
$time_start = xmltop ($query,$debug);

$result = db_query($query,$GLOBALS['DB_Link']) or 
        xmldie(db_error($GLOBALS['DB_Link']));

xmltabledump($table,$result);

/* Free resultset */
db_free_result($result);
//sql query finished ok
xmlend($time_start,"OK");

}


function xmlinsert($table,$insert,$debug)
{	
	// does an insert and generates xml response
$time_start = xmltop ($insert,$debug);

$result = db_query($insert,$GLOBALS['DB_Link']) or 
        xmldie(db_error($GLOBALS['DB_Link']));
                
$rows_affected = db_affected_rows();        

if ($rows_affected!=1) $ret = "INSERT FAILED"; else $ret = "INSERTED OK";

xmlend($time_start,$ret);
}

function xmlupdate($table,$update,$debug)
{	
	// does an insert and generates xml response
$time_start = xmltop ($update,$debug);


$result = db_query($update,$GLOBALS['DB_Link']) or 
        xmldie(db_error($GLOBALS['DB_Link']));
                
$rows_affected = db_affected_rows();        

if ($rows_affected==0) $ret = "UPDATE FAILED"; 
elseif ($rows_affected==1) $ret = "UPDATED";
else $ret = "UPDATED MULTIPLE ROWS - should be analyzed";

xmlend($time_start,$ret);
}

?>