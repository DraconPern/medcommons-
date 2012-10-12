<?PHP
//
// htmlsubs.inc - routines to build HTML output strings
//   reads config to load globals
//   builds final result string in $GLOBAL['htmlString']
//
require_once ("../dbconfig.inc");
require_once('../lib/config.inc'); // read $GLOBALS

define ('SIMULATOR_STYLE',
				"../lib/purplestyles.css");
define ('PINKBOX_STYLE',
				"../lib/astyles.css");
				
function eol() {return "<br>";};

function htmlflush()
{
	//useful when calling a 3rd party function that builds its own html
	echo $GLOBALS['htmlString'];
	$GLOBALS['htmlString']="";
}

function htmlfooter()
{
$a = alink("../controller/cuimonitor.php","Pink Box Control Panel"); 
$GLOBALS['htmlString'] .= <<<EOT
<p>Return to $a </p>
</body>
</html>
EOT;
return $GLOBALS['htmlString'];
}

function htmlheader($page_title,$style_sheet,$refresh=-1)
{
	if ($refresh!=-1) $str = '
	<META HTTP-EQUIV=Refresh CONTENT=".$refresh.";>';
	else $str="";
	
$GLOBALS['htmlString'] = <<<EXX
<html>
<head> 
<meta name=ProgId content=MedCommons.PinkBox>
<meta name="ROBOTS" content="NOINDEX">
<meta name=Generator content="MedCommons 4.1">
$str
  <title>$page_title</title>
</head>
<body>
<link rel="stylesheet" type="text/css" 
	media="screen" title="Pink Box Style" 
	href="../lib/$style_sheet">

EXX;
}


function htmlbody($str)
{
	//appends input string to current body 
	$GLOBALS['htmlString'].=$str;
}

function alink($link,$str)
{
	return "<a href=$link>$str</a>\n\r";
}

function hlevel($level, $str)
{
	return "<h$level>$str</h$level>\n\r";
}


function formheader($target)
{$x=<<<xxx
<div>
Some words of wisdom should go here
<p>

<form action=$target method=Get enctype="application/x-www-form-urlencoded">
xxx;
return $x.fmemopropogate();
}

function formend()
{return "</form>
</div>";}

function ft($label,$fieldname)
{ $x=<<<xxx
<p>$label<INPUT TYPE="TEXT" 
NAME="$fieldname" VALUE=""></p>
xxx;
return $x;
}

function fhidden($fieldname,$value)
{ $x=<<<xxx
<INPUT TYPE="HIDDEN" 
NAME="$fieldname" VALUE="$value">
xxx;
return $x;
}

function fcheckbox($label,$fieldname,$default="on")
{ $x=<<<xxx
<p>$label<INPUT TYPE="CHECKBOX" 
NAME="$fieldname" VALUE="$default"></p>
xxx;
return $x;
}
function fmemopropogate (){
	//just pass this param down the chain
	$rets="";
if ($_REQUEST['gateway']!="")  $rets=fhidden("gateway",$_REQUEST['gateway']);
if ($_REQUEST['recording']=='on') $rets .= fhidden("recording",'on');
return $rets;
}


function fmemoask (){return ("<br><br>".
                         fcheckbox(" Record this Transaction", "fMemoize",'1'));
                         
                         

}
                         


function formsubmit($label)
{
	$x = <<<xxx
	<p><INPUT TYPE="SUBMIT" VALUE=$label></p>
xxx;
return $x;
}
//////////////////////


function htmltop($page_title, $ss ,$refresh = -1)
{
readconfig();//must come first

$db= $GLOBALS['DB_Database'];
$pma = $GLOBALS['phpMyAdminURL'];
$phpmyadminstart = $pma.
       "/db_details_structure.php?lang=en-iso-8859-1&server=1&db=".
            $GLOBALS['DB_Database'];
$adminusertable=
         $GLOBALS['phpMyAdminURL'].
 "/sql.php?lang=en-iso-8859-1&server=1&db=pink0&goto=db_details_structure.php".
       "&table=users&sql_query=SELECT+%2A+FROM+%60users%60&pos=0";
$dbu = $GLOBALS['DB_User'];
$partner = $GLOBALS['ShortName'];
$partnerhome = $GLOBALS['HomePage'];
$srva = $_SERVER['SERVER_ADDR'];
$srvp = $_SERVER['SERVER_PORT'];
$gmt = gmstrftime("%b %d %Y %H:%M:%S");
//$ss = $GLOBALS['PurpleStyleSheet'];
$major = $GLOBALS['SchemaMajorVersion'];
$minor = $GLOBALS['SchemaMinorVersion'];

htmlheader($page_title,$ss, $refresh);

htmlbody(

alink("../controller/cuimonitor.php",$dbu).
alink($partnerhome,$partner).
alink($phpmyadminstart,$db). 
"($major.$minor) $srva:$srvp $gmt".
alink("../utils/examinevariables.php","diagnose").
alink("http://www.php.net/manual/en/","phpDocs").
alink("../utils/runphpinfo.php","phpInfo").
alink("$pma","phpMyAdmin").
alink("//www.medcommons.net/","MedCommons").
alink("../controller/cuinetopspanel.php","NetOps").
hlevel(1, $page_title)

);
}

?>