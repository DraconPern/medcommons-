<?php
require_once("../lib/config.inc");
require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/displayhomepage.inc");

// this code is utilized for two different pages - a Read/Only Display and an Editor

function makevrcprow ($a,$b,$c,$extra)
{
	if ($GLOBALS['aedit']==true) {
	return "<tr><td>$a</td><td>$b</td><td>$c</td><td>$extra</td></tr>";} else 
	return makerow($a,$b,$c);
}

function makevrcpactiverow ($a,$b,$c,$rowid)
{	
	$dupe = zlink("2",$GLOBALS['vrcputils']."&rowid=$rowid&op=2",false,false);
	$del  = zlink ("X", $GLOBALS['vrcputils']."&rowid=$rowid&op=3",false,false);
	$extra = $dupe." ".$del; // set to two urls for duping and deleting

	
$s = makevrcprow ($a,$b,$c,$extra);

return $s;

}

function uniquewado($x)
{
	for ($k=0; $k<count($GLOBALS['uwado']); $k++)
	if ($x==$GLOBALS['uwado'][$k]) return false;
	$GLOBALS['uwado'][]=$x;
	return true;
}
 function	buildwado ($dicomdev,$dicomwado,$mcdestdev,$mcdestdevwado)
 { //
 	if ($dicomwado==1)
 	{	if (uniquewado($dicomdev)==true)
 		$GLOBALS['wado'].=option($dicomdev);
 	};
 	if ($mcdestdevwado==1)
 	{	if (uniquewado($mcdestdev)==true)
 		$GLOBALS['wado'].=option($mcdestdev);
 	}
 }
 
 function buildactiondropdown($action)
{
	// build the action list dropdown
	// if the supplied action matches anything on our list, make that the default
	// and don't include the duplicate
	// if it doesn't match then put * around the entry *
	// if action is $GLOBALS['action'][0] then include that entry as well
	$i=-1;
	for ($k=0; $k<count($GLOBALS['action']); $k++)
	{if ($action==$GLOBALS['action'][$k]) {//found on list, just get out
			$i=$k; break;}
	}
	
//	echo "in buildactiondropdown ".$i;
			
	if ($i==-1) {
		$action = "*".$action."*"; // reflect small error	
		$GLOBALS['action'][]=$action; // put at the end
		$i=count($GLOBALS['action'])-1; // now it looks like it was found
	}

	// now go build the dropdown
	$dropdown = "";
	$count = count($GLOBALS['action']);
	$start = ($GLOBALS['action'][0]!=$action)?1:0; // skip the noaction entry
//	echo "start ".$start."count ".$count." imatch ".$i;
		for ($j=$start; $j<$count; $j++)
	{ //	echo "j is ".$j;
		if ($j>10) exit;
		if($i==$j)$dropdown .= option($GLOBALS['action'][$j],true); else
				$dropdown .= option($GLOBALS['action'][$j]);
	}
	$dropdown = selectlist('actions',$dropdown);// finishit off
	return $dropdown;
}


 
 	
 function buildrorow($rowid, $dicomdev,$dicomwado,$action,$filter,$mcdestdev,
 											$mcdestdevwado,$description)
 {  //this runs for the read-only version
  //echo "buildrorow $rowid";

 
 buildwado ($dicomdev,$dicomwado,$mcdestdev,$mcdestdevwado);
 	 	$extraparams = "&rowid=$rowid&action=$action&dicom=$dicomdev&mcdest=$mcdestdev";       
      

 	
 	if ($dicomdev!=""){
 	$arga = ($dicomwado==1)?wadolink($dicomdev,$GLOBALS['dicomconfig'].$extraparams): 
 							zlink($dicomdev,$GLOBALS['dicomconfig'].$extraparams);
 	}
 	else {
 	$arga = "(unassigned)";
 	}

 	if ($mcdestdev!=""){	
 	$argc = ($mcdestdevwado==1)?wadolink($mcdestdev,$GLOBALS['mcconfig'].$extraparams):
 								zlink($mcdestdev,$GLOBALS['mcconfig'].$extraparams);
 	}
 	else $argc = "(unassigned)";
 	
 	if ($action!=-1)	
 			$argb= $GLOBALS['action'][$action]; else
 	$argb =  "(unassigned)";
 	
 	$row= makevrcprow($arga,$argb,$argc,"?????");
 	
 	//echo $row;
 	
 	return $row;

 }


function buildmodifiablerow($rowid, $dicomdev,$dicomwado,$action,$filter,$mcdestdev,
 											$mcdestdevwado,$description)
 {  // build WADO dropdownlist dynamically from those that have the wado flag set
    // echo "buildmodifiablerow $rowid";
 	buildwado ($dicomdev,$dicomwado,$mcdestdev,$mcdestdevwado);
 	
 	// if the dicom device says 'none' then put in the dropdown we've built thusfar
 		$extraparams = "&rowid=$rowid&action=$action&dicom=$dicomdev&mcdest=$mcdestdev";       
 	 if ($dicomdev=="")  $dicomdev ="(editor)";
 		    
 	$arga = ($dicomwado==1)?wadolink($dicomdev,$GLOBALS['dicomconfig'].$extraparams): 
 							zlink($dicomdev,$GLOBALS['dicomconfig'].$extraparams);
 			
 	//$argb = buildactiondropdown($action); //zlink($action,"vcmod.php",false,false);
	if ($action==-1) $link ="(editor)";	else $link= $GLOBALS['action'][$action];
 	$argb= zlink($link,$GLOBALS['actionconfig'].$extraparams); 
 			
 	if ($mcdestdev=="") $mcdestdev="(editor)";					
 	$argc = ($mcdestdevwado==1)?wadolink($mcdestdev,$GLOBALS['mcconfig'].$extraparams):
 								zlink($mcdestdev,$GLOBALS['mcconfig'].$extraparams);
 	
	
 	$row= makevrcpactiverow($arga,$argb,$argc,$rowid);
 	
// 	echo $row;
 	
 	return $row;

 }

require_once ('actions.inc');// sets up globals
 
readconfig(); //get reconnected
session_start(); // get userid
sqltraceon();

$userid = $_SESSION['user'];
if ($userid=="")
{//not logged in, send him back with penalties
display_home_page(errortext("You must be logged in to manage your vrcp configuation"),'',$userid,
			errortext("**please login**"));
}
//figure out how we were started, and whether we are readonly
$isreadonly =(cleanreq('edit')=='no');
$advancededit = (cleanreq('mods')=='yes');
$user=$_SESSION['user'];
//take the gateway on the arglist line, provide a default
$gateway = cleanreq('gateway');
if ($gateway=="") 
//if no gateway specified on command line, see if we have a session variable
{
	$gateway = $_SESSION['vrcpgateway'];
	if ($gateway == "") $gateway='unknown';
}
// if any dirty cleanup work for duping or removing rows
$rowid = cleanreq('rowid');
$op = cleanreq('op');
if ($op==3) deletevrcprow($rowid);
elseif ($op ==2) dupevrcprow($rowid);
//we re-invoked ourselves
$_SESSION['vrcpgateway']=$gateway; // remember for next time
//we have some helpful lists here we are going to build incrementally

//make links and buttons
$userzlink = zlink($user,"my_account.php?user=$user&gateway=$gateway",true,false);
$gatewayzlink = $gateway; //zlink($gateway);
$GLOBALS['wado']=""; 
$GLOBALS['dicom']="";
$GLOBALS['aedit']=$advancededit;

//setup the standard urls for the editors we need 
//pass tell them whether we are read-only or not
$GLOBALS['dicomconfig'] = "dicomconfig.php?user=$user&gateway=$gateway";
if ($isreadonly==true) $GLOBALS['dicomconfig'].="&ro=yes";
$GLOBALS['mcconfig']="mcconfig.php?user=$user&gateway=$gateway";
if ($isreadonly==true) $GLOBALS['mcconfig'].="&ro=yes";
$GLOBALS['actionconfig']="actionconfig.php?user=$user&gateway=$gateway";
if ($isreadonly==true) $GLOBALS['actionconfig'].="&ro=yes";
$GLOBALS['gatewayconfig'] = "gatewayconfig.php?user=$user&gateway=$gateway";
if ($isreadonly==true) $GLOBALS['gatewayconfig'].="&ro=yes";

//point the utility switch back to us
$GLOBALS['vrcputils'] = "vrcp.php?user=$user&gateway=$gateway";
if ($advancededit==true) $GLOBALS['vrcputils'].="&mods=yes";


$cancel = "vrcp.php?edit=no";
$cancelbutton = zlink ("lock",$cancel,false,false);

$edit = "vrcp.php";
$editbutton = zlink("unlock",$edit,false,false);					
$inprogress = zlink($gateway,$GLOBALS["dicomconfig"],true);
$flipflop = ($isreadonly==true)?"$editbutton":"$cancelbutton";

$importexport = ($isreadonly==true)?"":
        zlink("import","../whitebox/import.php",false)." ".
      	zlink("export","../whitebox/vrcpexport.php?user=$user&gateway=$gateway",false)." ".
      	(($advancededit==true)?     	
      	zlink("basic","../whitebox/vrcp.php?user=$user&gateway=$gateway",false,false):
      	zlink("mods","../whitebox/vrcp.php?mods=yes&user=$user&gateway=$gateway",false,false))
       	;
      	
      	
// build those parts of the screen that are conditionally present based on whether we are readonly or not

$callback = ($isreadonly==true)?'buildrorow':'buildmodifiablerow';
$tot = 'MedCommons Virtual Radiology (TM)';
if ($isreadonly!=true) $tot .= (($advancededit ==true)?  ' Advanced Editor':' Editor');
$tail = (($advancededit ==false)?'':'  choose 2 to Dupe Row, X to Delete Row');
$wbh = wbheader("VRCP",$tot);
$vcr = getvrcpentries($callback,$user,$gateway);//adds to wado

//  build those things that are constructed incrementally and need to come last
$GLOBALS['wado'].=option("Inbox",true);
$wado = selectlist("menu",$GLOBALS['wado']);//must come after get vrcpentries

 // the dicom list is built on the fly and finished off in 'onerow' 	
 // the action dropdowns are built onthe fly
 
$x=<<<XXX
$wbh
<h4>$tot</h4>
<form>
user $userzlink gateway $gatewayzlink menu $wado $flipflop&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;$importexport
<br><br><br>
<table border="1">
XXX;
$x.=makevrcprow("----Local Dicom Device----",
  			"--------------Action--------------","---------MedCommons Destination---------",
  							"---");
$x.=makevrcprow(zlink($gateway,$GLOBALS['gatewayconfig'],false),
				zlink("(no action)",$GLOBALS['actionconfig'],false),
		   wadolink ("Inbox","selectionscreen.php",false,false),"   ");
$x.=$vcr; 

if ($isreadonly==false)
$x.=makevrcprow(zlink("(editor)",$GLOBALS['dicomconfig'],false),
				zlink("(editor)",$GLOBALS['actionconfig'],false),
		    zlink ("(editor)",$GLOBALS['mcconfig'],false), 
		    "   ");
else $x.="<p> </p>";

$x.=<<<ZZZ
</table>

</form>
$tail
</body>
</html>
ZZZ;
echo $x;
sqltracedump();

?>