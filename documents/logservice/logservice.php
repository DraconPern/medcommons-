<?PHP

require "../dbparams.inc.php";
/*

15 july 2005 - write each record to hipaatrace table as well, all as inserts

16 May 2005 - if &tr is present, take that as tracking number
logservice.php - a web service for making entries in a generalized log
by: w l Donner

date: 26 April 005


To Make A Log Entry:

http://<service addr see above>/Secure/logservice.php?arg1=val1&arg2=val2&arg3=val3..

the following table elaborates the argument and value pairs for the MakeCall method which may be specified in any order

Argname	Argtype	Req/Optional	Meaning	Comments

t1  Alpha 32    R   Table Name of Log 
t2  Num 3       R	Records to return on each call, 0 keeps it quiet

a1	Alpha 32	O	Key argument 1	must be unique, Specific to your application, if missing, then only retrievals
a2	Alpha 32	O	Key argument 2	Specific to your application
a3	Alpha 32	O	Key argument 3	Specific to your application
a4	Alpha 32	O	Key argument 4	Specific to your application

s1	Alpha 255	O	Custom argument 1	Specific to your application
s2	Alpha 255	O	Custom argument 2	Specific to your application
s3	Alpha 255	O	Custom argument 3	Specific to your application
s4	Alpha 255	O	Custom argument 4	Specific to your application

q = 1,2,3 or 4 - if present, the retrieval will add a Where a1 = the command line
g
p = 1 - if set, the store will be done, otherwise nothing will go into the log

The key arguments are maintained as fixed size database fields for performance

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
function generate_mcid_temporary($t) { //changed by donner build temporary mcid 
//set the random id length 
return ("0000".$t);
}

	function z($l,$v){	if ($l[$v]!="") xm ("<$v>".$l[$v]."</$v>");}

	
//
// main program - parse the incoming parameters, and reject ill formed requests, or bad templates
//
$t1=cleanreq('t1');
$t2=cleanreq('t2');

$a1=cleanreq('a1');
$a2=cleanreq('a2');
$a3=cleanreq('a3');
$a4=cleanreq('a4');
$s1=cleanreq('s1');
$s2=cleanreq('s2');
$s3=cleanreq('s3');
$s4=cleanreq('s4');
$p=cleanreq ('p');
$q=cleanreq('q');//query param
$r=cleanreq('r');//stuff a new medcommonsid in this param
$tr=cleanreq('tr');//stuff a new medcommonsid in this param

$hpin=cleanreq('hpin'); // if present it is the hashed pin value


xmltop($t1 ,false); //not in debug mode
//xm("<request_fields>t1=$t1;t2=$t2;a1=$a1;a2=$a2;a3=$a3;a4=$a4;s1=$s1;s2=$s2;s3=$s3;s4=$s4;p=$p;q=$q;r=$r</request_fields>");


	mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or xmlend ("can not connect to mysql");
	$db = $GLOBALS['DB_Database'];
	mysql_select_db($db) or xmlend ("can not connect to database $db");
// a1 must be present to get anything done at all
 
 if ($p=="1") { //must be present to do any stores
    if ($tr=="")$tracking=generate_tracking(); else $tracking=$tr;
    xm("<tracking_number>$tracking</tracking_number>"); 
// if $r is non-zero then generate a medcommons id and store it
	if ($r!="")
 	 	 	{ $id = generate_mcid_temporary($tracking);  //build mcid from tracking number
 	 	 	   xm("<medcommons_id>$id</medcommons_id>");
 	 	 	}

 	if ($r=="1") $a1=$id;
 	 if ($r=="2") $a2=$id;
 	 if ($r=="3") $a3=$id;
     	if ($r=="4") $a4=$id;
	
 	 	 

    // now write an entry in the mysql database

	$timenow=time();
	$insert="INSERT INTO hipaa (creation_time,tracking_number,hpin,a1,a2,a3,a4,s1,s2,s3,s4) ".
				"VALUES(NOW(),'$tracking','$hpin','$a1','$a2','$a3','$a4','$s1','$s2','$s3','$s4')";
	mysql_query($insert) or xmlend("can not insert into table hipaa - ".mysql_error());
	
	
	
		$insert="INSERT INTO hipaatrace (creation_time,tracking_number,hpin,a1,a2,a3,a4,s1,s2,s3,s4) ".
				"VALUES(NOW(),'$tracking','$hpin','$a1','$a2','$a3','$a4','$s1','$s2','$s3','$s4')";
	mysql_query($insert) or xmlend("can not insert into table hipaatrace - ".mysql_error());

 }
 
 
 

 // if $t2 is present then return a bunch of records
 if ($t2!="") {
 	if ($t2==0)($t2=10);
 	$whereclause="";
 	if ($q=="1") $whereclause = "where (a1='$a1')";
 	
 	 	if ($q=="2") $whereclause = "where (a2='$a2')";

 	 	 	if ($q=="3") $whereclause = "where (a3='$a3')";
 	 	 	
 	 	 	 	if ($q=="4") $whereclause = "where (a4='$a4')";

 	$query = "SELECT * from hipaa $whereclause ORDER BY creation_time DESC LIMIT $t2";
 	$result = mysql_query ($query) or xmlend("can not query table hipaa - ".mysql_error());
 	
	if ($result=="") return false;
	while ($l = mysql_fetch_array($result,MYSQL_ASSOC)) {
		$time = $l['creation_time'];
		
		xm("<entry time='$time'>");
	    z($l,'tracking'); z($l,'hpin');
		z($l,'a1'); z($l,'a2'); z($l,'a3'); z($l,'a4');
		z($l,'s1'); z($l,'s2'); z($l,'s3'); z($l,'s4');
		
		xm("</entry>");
	
	}
	
	mysql_free_result($result);
 }
 
mysql_close();
xmlend("success");
?>
