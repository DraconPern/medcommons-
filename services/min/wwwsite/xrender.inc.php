<?php

$WEBSITE = 'www.medcommons.net';  // no not include http or s
$GLOBALREDIRECTOR = 'https://www.medcommons.net'; // should always go to s

function select_random_appliance() {
  // Hack for simon's sake
  if(strpos($_SERVER['SCRIPT_URI'],"mc:7080") !== false) {
    return "http://mc:7080";
  } 
 // return "https://".'000'.rand(0,1).".myhealthespace.com";
 
  return "https://tenth.medcommons.net"; //where to allocate urls
}
function testif_logged_in()
{
	if (!isset($_COOKIE['mc'])) //wld 10 sep 06 strict type checking
	return false;
	$mc = $_COOKIE['mc'];

	$accid=""; $fn=""; $ln = ""; $email = ""; $idp = ""; $auth="";
	if ($mc!='')
	{
		$accid=""; $fn=""; $ln = ""; $email = ""; $idp = ""; $auth="";
		$props = explode(',',$mc);
		for ($i=0; $i<count($props); $i++) {
			list($prop,$val)= explode('=',$props[$i]);
			switch($prop)
			{
				case 'mcid': $accid=$val; break;
				case 'fn': $fn = $val; break;
				case 'ln': $ln = $val; break;
				case 'email'; $email = $val; break;
				case 'from'; $idp = stripslashes($val); break;
				case 'auth'; $auth = $val; break;
			}
		}
	}
	return array($accid,$fn,$ln,$email,$idp,$mc,$auth);
}
function renderas_webpage($contents=false,$vars=array())
{
	global $WEBSITE,$GLOBALREDIRECTOR;
	
	$break = explode('/', $_SERVER["SCRIPT_NAME"]);
	if ($contents===false)
	$contents = file_get_contents('htm/'.str_replace('.php','.htm',$break[count($break) - 1]));
	$maintitle = "NO TITLE FOR THIS PAGE";
	$pos = strpos($contents,'mainTitle=');
	if ($pos!==false)
	{ $npos = $pos+strlen('mainTitle=');	$c = substr($contents,$npos,1); $pos2 = strpos($contents,$c,$npos+2);
	if ($pos2!==false) $maintitle = substr($contents,$npos+1,$pos2-$npos-1);	}
	$markup = file_get_contents("htm/_header.htm").$contents.file_get_contents("htm/_footer.htm");	
	
	$topright =  "<span id='visi' class=right > </span>";
	
  // build nav differently based on whether we see a cookie or not
  // and whether running as a Website or as an appliance site
  
  	$on_appliance = ($WEBSITE!=$_SERVER['HTTP_HOST']);
           $logged_in = testif_logged_in();
           if ($logged_in === false)
           { 
	if ($on_appliance) 
	/*container for on appliance but not logged on*/
$navcontainer = <<<XXX
<li><a class=menu_how href="help.php">Help</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
<li ><a class=menu_nil  href="https://$WEBSITE/personal.php" >Sign In</a></li>
XXX;
else /* on website not logged on */
$navcontainer = <<<XXX
<li><a class=menu_how href="/help.php">Help</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
<li ><a class=menu_nil  href="/personal.php" >Sign In</a></li>
XXX;
           }
           else 
                    { 
                    	list ($accid,$fn,$ln,$email,$idp,$mc,$auth) = $logged_in;
                    
	if ($on_appliance)  /*on appliance and logged on*/
	$navcontainer = <<<XXX
<li><a class=menu_how href="help.php">Help</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
<li><a class=menu_list  href="/acct/home.php ">My Account</a></li>
XXX;
else /*container for logged on website users*/
$navcontainer = <<<XXX
<li><a class=menu_how href="help.php">Help</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
<li><a class=menu_list  href="$GLOBALREDIRECTOR/login/?q=$accid ">My Account</a></li>
XXX;

           }
	
	$navcontainer = '<ul id="navlist" class="listinlinetiny" >'.$navcontainer.'</ul>';
	$names = array('$$$htmltitle$$$','$$$navcontainer$$$','$$$topright$$$', '$$$globalredirector$$$', '$$$randomappliance$$$');
  	$values = array($maintitle,$navcontainer,$topright, $GLOBALREDIRECTOR,select_random_appliance());
  foreach($vars as $n => $v) {
    $names[]='$$$'.$n.'$$$';
    $values[]=$v;
  }
	echo str_replace($names,$values,$markup);
}
?>
