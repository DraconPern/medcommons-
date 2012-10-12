<?PHP
require_once "../dbparamsmcextio.inc.php";
require "generated-code/gencode.php.inc";

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
xm("<notifierservice>\n");//outer level 
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
xm("</notifierservice>\n");//outer level 
xmlreply(true); // show its all good
exit;
}




function generate_tracking() {
//set the random id length 
return rand(10000,99999).rand(10000,99999).rand(10000,99999);
}

	function z($l,$v){	if ($l[$v]!="") xm ("<$v>".$l[$v]."</$v>");}


	function getparams($set,&$t,&$m,&$a,&$b,&$c,&$d,&$e,&$f,&$g)
	{
		$t = cleanreq("t".$set);
		
		$m = cleanreq("m".$set);

		$a = cleanreq("a".$set);

		$b = cleanreq("b".$set);

		$c = cleanreq("c".$set);

		$d = cleanreq("d".$set);

		$e = cleanreq("e".$set);

		$f = cleanreq("f".$set);

		$g = cleanreq("g".$set);

	}
	
//
// main program - parse the incoming parameters, and reject ill formed requests, or bad templates
//


	$timenow=time();  // get time for writing these
	$ref = substr(htmlspecialchars($_SERVER ['REQUEST_URI']),0,255); //get the uri



   	$mcid = cleanreq('mcid');
	$timenow=time();  // get time for writing these
	$ref = substr(htmlspecialchars($_SERVER ['REQUEST_URI']),0,255); //get the uri

xmltop("sender mcid $mcid" ,false); //not in debug mode

getparams(1,$t, $m, $a,$b,$c,$d,$e,$f,$g);

if (isset($t) && ($t!="")){




		mysql_connect($GLOBALS['DB_Connection'],
			$GLOBALS['DB_User'],
			$GLOBALS['DB_Password']
			) or xmlend ("can not connect to mysql");
	mysql_select_db($GLOBALS['DB_Database']) or xmlend("can not connect to database emailstatus");

	$timenow=time();  // get time for writing these
	$ref = substr(htmlspecialchars($_SERVER ['REQUEST_URI']),0,255); //get the uri
//write the email on the way out with a status of TRYING	
		$insert="INSERT INTO emailstatus (time,requesturi,sendermcid,rcvremail,template,arga,argb,argc,argd,arge,argf,argg,message,status)".
				"VALUES(NOW(),'$ref','$mcid','$m','$t','$a','$b','$c','$d','$e','$f','$g','$message','TRYING')";
				
				mysql_query($insert) or xmlend("can not insert into table emailstatus - ".mysql_error());


xm("<request_fieldset1>t1=$t;a1=$a;b1=$b;c1=$c;d1=$d;e1=$e;f1=$f;g1=$g</request_fieldset1>");

	$str = "mail".$t;// put mail in front of template name
	if (!function_exists($str)) $retval="Unknown or uncompiled template"; else 
	$retval = $str($message,$mcid, $t,$m,"MedCommons Notification for $m", $a,$b,$c,$d,$e,$f,$g);//call the routine to send the mail

xm("<request_status>".$retval."</request_status>\n");

  
	$insert="INSERT INTO emailstatus (time,requesturi,sendermcid,rcvremail,template,arga,argb,argc,argd,arge,argf,argg,message,status)".
				"VALUES(NOW(),'$ref','$mcid','$m','$t','$a','$b','$c','$d','$e','$f','$g','$message','$retval')";
				
				mysql_query($insert) or xmlend("can not insert into table emailstatus - ".mysql_error());

}

getparams(2,$t,$m, $a,$b,$c,$d,$e,$f,$g);

if (isset($t) && ($t!="")){

xm("<request_fieldset2>t2=$t;a2=$a;b2=$b;c2=$c;d2=$d;e2=$e;f2=$f;g2=$g</request_fieldset2>");
   // write a log entry of what we did
	$str = "mail".$t;// put mail in front of template name
		if (!function_exists($str)) $retval="Unknown or uncompiled template"; else 

	$retval = $str($message, $mcid, $t,$m,"MedCommons Notification for $m",$a,$b,$c,$d,$e,$f,$g);//call the routine to send the mail
xm("<request_status>".$retval."</request_status>\n");
   // write a log entry of what we did<BR>

	$insert="INSERT INTO emailstatus (time,requesturi,sendermcid,rcvremail,template,arga,argb,argc,argd,arge,argf,argg,message,status)".
				"VALUES(NOW(),'$ref','$mcid','$m','$t','$a','$b','$c','$d','$e','$f','$g','$message','$retval')";
	mysql_query($insert) or xmlend("can not insert into table notifierlog - ".mysql_error());
	
}	


getparams(3,$t,$m,$a,$b,$c,$d,$e,$f,$g);
if (isset($t) && ($t!="")){

xm("<request_fieldset3>t3=$t;a3=$a;b3=$b;c3=$c;d3=$d;e3=$e;f3=$f;g3=$g</request_fieldset3>");
	$str = "mail".$t;// put mail in front of template name
		if (!function_exists($str)) $retval="Unknown or uncompiled template"; else 

	$retval = $str($message, $mcid, $t,$m,"MedCommons Notification for $m",$a,$b,$c,$d,$e,$f,$g);//call the routine to send the mail
xm("<request_status>".$retval."</request_status>\n");
   // write a log entry of what we did

	$insert="INSERT INTO emailstatus (time,requesturi,sendermcid,rcvremail,template,arga,argb,argc,argd,arge,argf,argg,message,status)".
				"VALUES(NOW(),'$ref','$mcid','$m','$t','$a','$b','$c','$d','$e','$f','$g','$message','$retval')";
	mysql_query($insert) or xmlend("can not insert into table notifierlog - ".mysql_error());

}

/*mysql_close();*/

xmlend("success");
?>
