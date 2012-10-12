<?PHP
require_once "../dbparamsmcextio.inc.php";

class notifier {

function send_message($message,$mcid, $t,$m,$blurb, $a,$b,$c,$d,$e,$f,$g)
{ return "should not get to notifier base class, must be overridden";}

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
		$this->xm("<notifierservice>\n");//outer level
		$srva = $_SERVER['SERVER_ADDR'];
		$srvp = $_SERVER['SERVER_PORT'];
		$gmt = gmstrftime("%b %d %Y %H:%M:%S");
		$uri = htmlspecialchars($_SERVER ['REQUEST_URI']);
		$this->xm("<details>$srva:$srvp $gmt GMT</details>");
		$this->xm("<referer>".htmlspecialchars($_SERVER ['HTTP_REFERER'])."</referer>\n");
		$this->xm("<requesturi>\n".$uri."</requesturi>\n");
	}

	function xmlend( $xml_status)
	{
		$this->xm("<summary_status>".$xml_status."</summary_status>\n");
		$this->xm("</notifierservice>\n");//outer level
		$this->xmlreply(true); // show its all good
		exit;
	}


	function pretty_tracking($tracking) {
		return substr($tracking,0,4)." ".substr($tracking,4,4)." ".substr($tracking,8,4);
	}
	function pretty_mcid($s)
	{
		return substr($s,0,4)." ".substr($s,4,4)." ".substr($s,8,4)." ".substr($s,12,4);
	}
	function merge_tracking_mcid($tracking,$s)
	{
		return substr($s,0,4).substr($tracking,8,4).substr($s,4,4).substr($tracking,4,4).
		substr($s,8,4).substr($tracking,0,4).substr($s,12,4);
	}
	function unmerge_tracking_mcid($num,&$t,&$m)
	{
		$m = substr($num,0,4).substr($num,8,4).substr($num,16,4).substr($num,24,4);
		$t = substr($num,20,4).substr($num,12,4).substr($num,4,4);
	}

	function generate_tracking() {
		//set the random id length
		return rand(10000,99999).rand(10000,99999).rand(10000,99999);
	}

	function z($l,$v){	if ($l[$v]!="") xm ("<$v>".$l[$v]."</$v>");}

	function cleanreq($fieldname)
	{
		// take an input field from the command line or POST
		// and clean it up before going any further
		$value = $_REQUEST[$fieldname];
		$value = htmlspecialchars($value);
		return $value;
	}
	function getparams($set,&$t,&$m,&$a,&$b,&$c,&$d,&$e,&$f,&$g)
	{

		$t = $this->cleanreq("t".$set);
		$m = $this->cleanreq("m".$set);
		$a = $this->cleanreq("a".$set);
		$b = $this->cleanreq("b".$set);
		$c = $this->cleanreq("c".$set);
		$d = $this->cleanreq("d".$set);
		$e = $this->cleanreq("e".$set);
		$f = $this->cleanreq("f".$set);
		$g = $this->cleanreq("g".$set);
	}

	//
	// main program - parse the incoming parameters, and reject ill formed requests, or bad templates
	//
	function notify ()
	{

		$timenow=time();  // get time for writing these
		$ref = substr(htmlspecialchars($_SERVER ['REQUEST_URI']),0,255); //get the uri



		$mcid = $this->cleanreq('mcid');
		$timenow=time();  // get time for writing these
		$ref = substr(htmlspecialchars($_SERVER ['REQUEST_URI']),0,255); //get the uri

		$this->xmltop("$sender mcid $mcid" ,false); //not in debug mode

		$this->getparams(1,$t, $m, $a,$b,$c,$d,$e,$f,$g);

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

			$this->xm("<request_fieldset1>t1=$t;a1=$a;b1=$b;c1=$c;d1=$d;e1=$e;f1=$f;g1=$g</request_fieldset1>");

			$retval = $this->send_message($message,$mcid, $t,$m,"MedCommons Notification for $m", $a,$b,$c,$d,$e,$f,$g);//call the routine to send the mail

			$this->xm("<request_status>".$retval."</request_status>\n");


			$insert="INSERT INTO emailstatus (time,requesturi,sendermcid,rcvremail,template,arga,argb,argc,argd,arge,argf,argg,message,status)".
			"VALUES(NOW(),'$ref','$mcid','$m','$t','$a','$b','$c','$d','$e','$f','$g','$message','$retval')";

			mysql_query($insert) or xmlend("can not insert into table emailstatus - ".mysql_error());

		}

		$this->getparams(2,$t,$m, $a,$b,$c,$d,$e,$f,$g);

		if (isset($t) && ($t!="")){

			$this->xm("<request_fieldset2>t2=$t;a2=$a;b2=$b;c2=$c;d2=$d;e2=$e;f2=$f;g2=$g</request_fieldset2>");
			// write a log entry of what we did
			$retval = $this->send_message($message,$mcid, $t,$m,"MedCommons Notification for $m", $a,$b,$c,$d,$e,$f,$g);//call the routine to send the mail

			$this->xm("<request_status>".$retval."</request_status>\n");
			// write a log entry of what we did<BR>

			$insert="INSERT INTO emailstatus (time,requesturi,sendermcid,rcvremail,template,arga,argb,argc,argd,arge,argf,argg,message,status)".
			"VALUES(NOW(),'$ref','$mcid','$m','$t','$a','$b','$c','$d','$e','$f','$g','$message','$retval')";
			mysql_query($insert) or xmlend("can not insert into table notifierlog - ".mysql_error());

		}


		$this->getparams(3,$t,$m,$a,$b,$c,$d,$e,$f,$g);
		if (isset($t) && ($t!="")){

			$this->xm("<request_fieldset3>t3=$t;a3=$a;b3=$b;c3=$c;d3=$d;e3=$e;f3=$f;g3=$g</request_fieldset3>");
			$retval = $this->send_message($message,$mcid, $t,$m,"MedCommons Notification for $m", $a,$b,$c,$d,$e,$f,$g);//call the routine to send the mail
			$this->xm("<request_status>".$retval."</request_status>\n");
			// write a log entry of what we did

			$insert="INSERT INTO emailstatus (time,requesturi,sendermcid,rcvremail,template,arga,argb,argc,argd,arge,argf,argg,message,status)".
			"VALUES(NOW(),'$ref','$mcid','$m','$t','$a','$b','$c','$d','$e','$f','$g','$message','$retval')";
			mysql_query($insert) or xmlend("can not insert into table notifierlog - ".mysql_error());

		}

		/*mysql_close();*/

		$this->xmlend("success");

	}

}
?>
