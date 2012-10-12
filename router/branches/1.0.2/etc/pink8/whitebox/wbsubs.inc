<?PHP
//wbsubs.inc - common whitebox code
//wld 21 sept 04
require_once("../dbconfig.inc");

require_once ("../lib/config.inc");

function makewadourl ($gwurl,$tracking,$guid)
{
$orderurl = urlencode( "http://".$_SERVER['SERVER_NAME']."/"
	     .$GLOBALS['DB_Database'].
	     "/whitebox/order.php".
	     "?tracking=$tracking"
	     );
					
// turn the tracking number into a proper URL and embed it
return "http://$gwurl/router/initViewer.do?guid=$guid&orderUrl=$orderurl";
}

function generate_password() {
$rnd_id = rand(1000,9999);
return $rnd_id;
}

function generate_tracking() {
//set the random id length 
$random_id_length = 12; 

//generate a random id encrypt it and store it in $rnd_id 
$rnd_id = crypt(uniqid(rand(),time())); 

//to remove any slashes that might have come 
$rnd_id = strip_tags(stripslashes($rnd_id)); 

//Removing any . or / and reversing the string 
$rnd_id = str_replace(".","",$rnd_id); 
$rnd_id = strrev(str_replace("/","",$rnd_id)); 

//finally I take the first 10 characters from the $rnd_id 
$rnd_id = substr($rnd_id,0,$random_id_length); 

//echo "Generate Password - Random Id: $rnd_id"; 
return $rnd_id;
}
 
function wbheader($mylabel,$tit,$popup=false)
{
	function mlink($page,$label)
	{

		if ($label==$GLOBALS['mylabel']) return "<span class='currentPage'>$label</span>";
		else return "<a href='$page'>$label</a>";
	}
	readconfig();		// get connected to the database
	session_start();	// allows us to remember if logged in or not
	$GLOBALS['mylabel']=$mylabel;

	$homepage = mlink('index.php','home');
	$registerpage = mlink ('register.php','register');
	$downloadpage = mlink('download.php','download');
	$contactpage = mlink('contact.php','contact');
	$myaccountpage = mlink('my_account.php','my account');
	
	$menu = "$homepage || $registerpage || $downloadpage || $contactpage || $myaccountpage";
	if ($popup==true) $menu = "print || fax || "."<a href='javascript:self.close()'>close";

	$blurb = loginblurb($popup);

	$x = <<<ZZZ
<html>
  <head>
    <title>$tit</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <meta name="robots" content="none">
    <link href="main.css" rel="stylesheet" type="text/css">
    <script language="JavaScript" src="form_functions.js"></script>
 <style>
<!--
 /* Style Definitions */
 p.MsoNormal, li.MsoNormal, div.MsoNormal
	{margin:0in;
	margin-bottom:.0001pt;
	font-size:12.0pt;
	font-family:"Times New Roman";}
@page Section1
	{size:8.5in 11.0in;
	margin:1.0in 1.25in 1.0in 1.25in;}
div.Section1
	{page:Section1;}
-->
</style>
  </head>
<body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0" >
  <table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tbody>
<tr>
<td bgcolor="#dff2f7">	
<table border="0" cellpadding="0" cellspacing="0" width="100%">
        <tbody><tr> 
            <td rowspan="2" width="256">
           <img src="logo_sm.gif" height="50" width="256"></td>
           <td align="right" valign="bottom" >$blurb</td>

            </tr>
        <tr>
          <td align="right" valign="bottom"> $menu
		</td>
        </tr>
  </tbody>
</table>
</td>
</tr>
</tbody>
</table>
ZZZ;

	return $x;
}

function loginblurb ($popup)
{
	if ($_SESSION['user'] =="") $loginblurb = "not logged in"; else 
	{$loginblurb=
	"logged in as ".$_SESSION['user'];
	 
	if ($popup==false) $loginblurb .= ' <a href="loggedout.php">logout</a>';}
	return $loginblurb;
}
function refreshparentanddie ()
{ return "'javascript:var URL = unescape(window.opener.location.pathname);".
"window.opener.location.href = URL; window.close();'";}

function gotopageonparentanddie ($url)
{ return "'javascript:window.opener.location.href = $url; window.close();'";}

function makenewwindow ($url)
{ return "javascript:NewWindow('$url','temp',540,760,'no')"; }

function suicide()
{ return "javascript:self.close();";}

function gotopage ($url)
{ return "javascript:window.location='$url'";}


function zlink($label,$url="nopane.php",$passname=true,$newwindow=true)
{	if ($passname==true)	$url .= "&name=".$label;
	if ($newwindow==true)
	$js = makenewwindow($url); else $js=$url;
	 	return '<a href="'.$js.'">'."$label</a>";
}
function wadolink($label,$url="nopane.php",$passname=true,$newwindow=true)
{	if ($passname==true)	$url .= "&name=".$label;
	if ($newwindow==true)
	$js = makenewwindow($url); else $js=$url;
	 	return '<a href="'.$js.'">'."!$label</a>";
}
function butt ($label,$url)
{
	return "<button onClick=$url>$label</button>";
};

function makerow ($a,$b,$c)
{
	return "<tr><td>$a</td><td>$b</td><td>$c</td></tr>";
}

function select()
{	$base = func_get_arg(0);
	$selname = func_get_arg(1);
	$zzz = "<select name='$selname'>";
    $numargs = func_num_args();
  
   $arg_list = func_get_args();
   for ($i = 2 ; $i < $numargs; $i++) {
   	$val = $base+$i-2;
	$zzz.=
	"<option value = $val>".$arg_list[$i]."\r\n";
   }
   $zzz .="</select>";
   return $zzz;
}


function selectlist($name,$body)
{
	return "<select name='$name'>$body</select>";
}

function option($label,$selected=false)
{	$sek= ((true==$selected)?" selected":""); 
	return "<option value='$label'>$label</option>";
}

function gatewaylist($user)
{
function buildgatewaylist($gw,$gwurl,$details,$lastheard) {
  		$GLOBALS['gateway'].=option($gw);
 }
$GLOBALS['gateway']="";
$callback ='buildgatewaylist';
$vcr = getgatewayentries($callback,$user);//adds to wado

$gateways = selectlist("gateway",$GLOBALS['gateway']);//must come after get vrcpentries
return $gateways;
}

function monthlist ()
{
	return select(0,"month","(all)","january","february","march","april","may","june","july","august","september","october","november","december");
}
function yearlist ()
{
	return select(0,"year","(all)","2001","2002","2003","2004","2005","2006");
}


function wadolist($user,$gateway)
{
function buildwadolist($rowid, $dicomdev,$dicomwado,$action,$filter,$mcdestdev,
 											$mcdestdevwado,$description)
 {  //this runs for the read-only version
  //echo "buildrorow $rowid";

 
 	if ($dicomwado==1)
 	{
 		$GLOBALS['wado'].=option($dicomdev);
 	};
 	if ($mcdestdevwado==1)
 	{
 		$GLOBALS['wado'].=option($mcdestdev);
 	}
 
 }
$GLOBALS['wado']="";
$callback ='buildwadolist';
$vcr = getvrcpentries($callback,$user,$gateway);//adds to wado
$GLOBALS['wado'].=option("Inbox",true);
$wado = selectlist("wado",$GLOBALS['wado']);//must come after get vrcpentries
return $wado;
}

?>