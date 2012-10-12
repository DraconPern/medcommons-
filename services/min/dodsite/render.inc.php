<?php


function renderas_webpage($contents=false,$vars=array())
{
	$script = $_SERVER["SCRIPT_NAME"];
	$break = explode('/', $script);
	$pagename=str_replace('.php','.htm',$break[count($break) - 1]);
	if ($contents===false)  $contents = file_get_contents('htm/'.$pagename);
	$maintitle = "NO TITLE";

	$pos = strpos($contents,'mainTitle=');
	if ($pos!==false)
	{ $npos = $pos+strlen('mainTitle=');	$c = substr($contents,$npos,1); $pos2 = strpos($contents,$c,$npos+2);
	if ($pos2!==false) $maintitle = substr($contents,$npos+1,$pos2-$npos-1);	}
	
	$host=$_SERVER['HTTP_HOST'];
	$time = gmdate ("M d Y H:i:s");
	$comments = <<<XXX

<!-- 
Rendering this DOD website page $pagename from server $host script $script at gmt $time

-->	

XXX;
	$markup = "<!-- loading header from /var/www/html/htm/_header.htm -->".file_get_contents("htm/_header.htm").$contents.
	"<!-- loading footer from /var/www/html/htm/_footer.htm -->".file_get_contents("htm/_footer.htm");

	$topright =  "<span id='visi' class=right > </span>";

	// build nav differently based on whether we see a cookie or not
	// and whether running as a Website or as an appliance site
	$GLOBALS['footerlogin'] ='';

		$navcontainer = <<<XXX
XXX;
$names = array('$$$htmltitle$$$','$$$navcontainer$$$','$$$topright$$$',  '$$$pageid$$$',
'$$$modcomments$$$','$$$locallogin$$$');
$values = array($maintitle,$navcontainer,$topright,  $pagename,$comments
,$GLOBALS['footerlogin'] );
foreach($vars as $n => $v) {
	$names[]='$$$'.$n.'$$$';
	$values[]=$v;
}
echo str_replace($names,$values,$markup);
}
?>
