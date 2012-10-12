<?php

// keep $user,$gateway in 
//config.inc
// (c) 2004 MedCommons, Inc.
// wld 8/30/04

define("SWMajorVersion", 7);
define("SWMinorVersion", 5);
define ("ERROR_COLOR","#FF0000");

require_once ('../lib/dblib.inc');
function generate_unique() {
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

////echo "Generate Password - Random Id: $rnd_id"; 
return $rnd_id;
}


function dupevrcprow($rowid)

{	sqt("dubvrcprow");
 	//find this users config on this gateway, and call the callback once for each record found
 	//concatenating the results into one string
 	$lookup = <<<TTT
	SELECT * FROM vrcp WHERE VC_ID='$rowid'
TTT;


	$result = db_query($lookup,	$GLOBALS['DB_Link']); 
	if (db_num_rows($result)==0) return false;
	//echo "in getvrcpentries got ".db_num_rows($result);
	$l = db_fetch_array_assoc($result);
    $createtime = $l['VC_CREATETIME'];
	$dicomdev = $l['VC_DICOM'];
	$dicomwado = $l['VC_DICOMWADO'];
	$action= $l['VC_ACTION'];
	$filter=$l['VC_FILTER'];
	$mcdestdev = $l['VC_MCDEST'];
	$mcdestdevwado = $l['VC_MCDESTWADO'];
	$description = $l['VC_DESCRIPTION'];
	$user = $l['VC_USER'];
	$gateway = $l['VC_GATEWAY'];
	db_free_result($result);

	$uniq = uniqid();
	$insert = <<<ZZZ

INSERT INTO vrcp (VC_ID,VC_GATEWAY,VC_USER,VC_DICOM,VC_DICOMWADO,VC_ACTION,VC_FILTER, VC_MCDEST,
				   VC_MCDESTWADO, VC_DESCRIPTION)
				   VALUES('$uniq','$gateway','$user','$dicomdev','$dicomwado',"$action","$filter",
				   "$mcdestdev",'$mcdestdevwado',"$description"
				   )
ZZZ;


$result = db_query($insert,$GLOBALS['DB_Link']) or
	exit("Cant do dupevrcprow insert table".db_error($GLOBALS['DB_Link']));



//	db_free_result($result);
	return true;
}

function deletevrcprow($rowid)
{echo "delete $rowid";
		$insert = <<<ZZZ
		DELETE FROM vrcp WHERE  VC_ID='$rowid'
ZZZ;

$result = db_query($insert,$GLOBALS['DB_Link']) or
	exit("Cant do deletevrcprow ".db_error($GLOBALS['DB_Link']));
return true;
}


function patchvcrpactionrow($rowid,$display,$user,$gateway)
{ sqt ('patchvcrpactionrow');

if ($rowid=="") 
{$id=generate_unique();
$insert = <<<ZZZ

INSERT INTO vrcp (VC_ID,VC_GATEWAY, VC_USER,VC_DICOM,VC_DICOMWADO,VC_ACTION,VC_FILTER, VC_MCDEST,
				   VC_MCDESTWADO, VC_DESCRIPTION)
				   VALUES('$id','$gateway','$user',"",'0','$display',"","",'0',"(action generated)"
				   )
ZZZ;

}
else
$insert = <<<ZZZ
UPDATE vrcp SET VC_ACTION = '$display' WHERE VC_ID = '$rowid'
ZZZ;

$result = db_query($insert,$GLOBALS['DB_Link']) or
	exit("Cant do patchvrcpactionrow vrdt table".db_error($GLOBALS['DB_Link']));
return true;
}
function patchvcrpdicomrow($rowid,$display,$user,$gateway)
{ sqt ('patchvcrpdicomrow');
	//echo "patch dicom $display rowid $rowid";


//make up a number for now, we'll worry about this later
if ($rowid=="") 
{   $id=generate_unique();

$insert=<<<ZZZ

INSERT INTO vrcp (VC_ID,VC_GATEWAY,VC_USER,VC_DICOM,VC_DICOMWADO,VC_ACTION,VC_FILTER, VC_MCDEST,
				   VC_MCDESTWADO, VC_DESCRIPTION)
				   VALUES('$id','$gateway','$user','$display','0',"-1","","",'0',"(dicom generated)"
				   )
ZZZ;
}

else
$insert = <<<ZZZ
UPDATE vrcp SET VC_DICOM = '$display' WHERE VC_ID = '$rowid'
ZZZ;
$result = db_query($insert,$GLOBALS['DB_Link']) or
	exit("Cant do patchvrcpdicomrow vrdt table".db_error($GLOBALS['DB_Link']));
return true;
}

function patchvcrpmcrow($rowid,$display,$user,$gateway)
{ sqt ('patchvcrpmcrow');

		//echo "patch mc $display rowid $rowid";

 //make up a number for now, we'll worry about this later
 

if ($rowid=="") 

{$id=generate_unique();

$insert = <<<AAA
INSERT INTO vrcp (VC_ID,VC_GATEWAY,VC_USER,VC_DICOM,VC_DICOMWADO,VC_ACTION,VC_FILTER, VC_MCDEST,
				   VC_MCDESTWADO, VC_DESCRIPTION)
				   VALUES('$id','$gateway','$user',"",'0',"-1","",'$display','0',"(mcdest generated)"
				   )
AAA;
}

else
$insert = <<<ZZZ
UPDATE vrcp SET VC_MCDEST = '$display' WHERE VC_ID = '$rowid'
ZZZ;

$result = db_query($insert,$GLOBALS['DB_Link']) or
	exit("Cant do patchvrcpmcrow vrdt table".db_error($GLOBALS['DB_Link']));
return true;
}

function	updatevrcpdicomwadoflags($displaywado,$user,$gateway,$displayname)
{ sqt('updatevrcpdicomwadoflags');
	
	
	$update =<<<XXX
UPDATE vrcp 
	SET VC_DICOMWADO = '$displaywado'
		WHERE VC_DICOM='$displayname' 
	AND VC_GATEWAY='$gateway' AND VC_USER='$user'
XXX;
$result = db_query($update,$GLOBALS['DB_Link']) or
	exit("Cant do updatevrcpdicomwadoflags".db_error($GLOBALS['DB_Link']));
	return true;
}
          
function	
            updatevrcpmcwadoflags($showmenus,$user,$gateway,$displayname)
{ sqt('updatevrcpmcwadoflags');
	$update =<<<XXX
UPDATE vrcp 
	SET VC_MCDESTWADO = '$showmenus'
		WHERE VC_MCDEST ='$displayname' 
	AND VC_GATEWAY='$gateway' AND VC_USER='$user'
XXX;
///echo "updatervccomkeychange ".$update;
$result = db_query($update,$GLOBALS['DB_Link']) or
	exit("Cant do updatevrcpmcwadoflags".db_error($GLOBALS['DB_Link']));
	return true;
}				
			
function	updatevrcpmckeychange($olddisplayname,$displayname,
													$user,$gateway)
{ sqt('updatevrcpmckeychange');
	
	$update =<<<XXX
UPDATE vrcp 
	SET VC_MCDEST ='$displayname'
	WHERE VC_MCDEST ='$olddisplayname' 
	AND VC_GATEWAY='$gateway' AND VC_USER='$user'
XXX;
$result = db_query($update,$GLOBALS['DB_Link']) or
	exit("Cant do updatevrcpmckeychange".db_error($GLOBALS['DB_Link']));

	return true;
}
function	updatevrcpdicomkeychange($olddisplayname,$displayname,$user,$gateway)
{ sqt('updatevrcpdicomkeychange');
	$update =<<<XXX
UPDATE vrcp 
	SET VC_DICOM='$displayname'
	WHERE VC_DICOM='$olddisplayname' 
	AND VC_GATEWAY='$gateway' AND VC_USER='$user'
XXX;
$result = db_query($update,$GLOBALS['DB_Link']) or
	exit("Cant do updatevrcpdicomkeychange".db_error($GLOBALS['DB_Link']));

	return true;
}
function updateactioncode($user,$gateway,$displayname,
                           $oldaction,$mcdest,$actioncode)
{ 
	sqt('updateactioncode');
	$update=<<<ZZZ
UPDATE vrcp
	SET VC_ACTION='$actioncode'
	WHERE VC_GATEWAY='$gateway' AND
	VC_USER='$user' AND
	VC_DICOM='$displayname' AND
	VC_ACTION='$oldaction' AND
	VC_MCDEST='$mcdest'
ZZZ;
echo $update;
$result = db_query($update,$GLOBALS['DB_Link']) or
	exit("Cant do updateactioncode".db_error($GLOBALS['DB_Link']));

return true;
}

function  updatedicomdetails( 

   $displayname,
   $user,   
   $gateway,
   $aetitle, 
   $showmenus, 
   $dicomipaddress, 
   $dicomport, 
   $comment)
   {
   	sqt('updatedicomdetails');	
	$update = <<<EXX
	UPDATE vrdt  
				SET
				VD_AETITLE='$aetitle',
				VD_SHOWMENUS='$showmenus',
				VD_DICOMIPADDR='$dicomipaddress',
				VD_DICOMPORT='$dicomport',
				VD_COMMENT= '$comment'
				WHERE VD_DISPLAYNAME ='$displayname' AND
				VD_USER='$user' AND
				VD_GATEWAY='$gateway'		
   
EXX;
	$result = db_query($update,$GLOBALS['DB_Link']) or
	exit("Cant do update on vrdt table".db_error($GLOBALS['DB_Link']));

	return true;
}
function  insertdicomdetails( 
   $displayname,
   $user,   
   $gateway,
   $aetitle, 
   $showmenus, 
   $dicomipaddress, 
   $dicomport, 
   $comment)
   { sqt('insertdicomdetails');	
	$insert = <<<EXX
INSERT INTO vrdt (VD_DISPLAYNAME,VD_USER,VD_GATEWAY,VD_AETITLE,
				VD_SHOWMENUS,VD_DICOMIPADDR,VD_DICOMPORT,VD_COMMENT)			
VALUES (  
   
   '$displayname',   
   '$user',
   '$gateway', 
   '$aetitle', 
   '$showmenus', 
   '$dicomipaddress', 
   '$dicomport',
   '$comment'
   )
EXX;
	$result = db_query($insert,$GLOBALS['DB_Link']) or
	exit("Cant do insert on vrdt table".db_error($GLOBALS['DB_Link']));

	return true;
}

	function deletedicom($key,$user,$gateway)
	{sqt("deletedicom $key $user $gateway");
		//delete record from vrmd 
			$delete=<<<DDD
		DELETE FROM vrdt WHERE  VM_DISPLAYNAME='$key' AND 
   				VM_USER ='$user' AND VM_GATEWAY='$gateway'
DDD;
		$result = db_query($delete,	$GLOBALS['DB_Link']); 
		//set all references to null
		$insert = <<<ZZZ
		UPDATE vrcp SET VC_DICOM = '' WHERE  VC_DICOM ='$key' AND 
   				VC_USER ='$user' AND VC_GATEWAY='$gateway'

ZZZ;
		$result = db_query($insert,$GLOBALS['DB_Link']) or
		exit("error in deletemc update of vrcp".db_error($GLOBALS['DB_Link']));
		//if we have any totally rows, then delete them
		//set all references to null
		$insert = <<<ZZZ
		DELETE FROM vrcp WHERE  VC_MCDEST='' AND VC_DICOM='' AND VC_ACTION='-1'
ZZZ;
		$result = db_query($insert,$GLOBALS['DB_Link']) or
		exit("error in deletedicom delete from vrcp".db_error($GLOBALS['DB_Link']));
		
		return true;
	}
	
	
function  getdicomdetails( 
   $displayname,
   $user,   
   $gateway,

   &$aetitle, 
   &$showmenus, 
   &$dicomipaddress, 
   &$dicomport, 
   &$comment)
   
   {sqt('getdicomdetails');
   	$lookup =<<<MMM
   	SELECT * FROM vrdt WHERE VD_DISPLAYNAME='$displayname' AND 
   				VD_USER ='$user' AND VD_GATEWAY='$gateway'
MMM;

$result = db_query($lookup,	$GLOBALS['DB_Link']); 
////echo "result ".$result;
	$num=db_num_rows($result);sqt ("dicom $num rows");
	if(0==db_num_rows($result)) return false;;
	
	$l = db_fetch_array_assoc($result);

	// for now, we just run thru the xdsregistry and display all the entries
	
	$aetitle=$l['VD_AETITLE'];
	$showmenus=$l['VD_SHOWMENUS'];
	$dicomipaddress=$l['VD_DICOMIPADDR'];
	$dicomport=$l['VD_DICOMPORT'];
	$comment=$l['VD_COMMENT'];
	db_free_result($result);
//	sqt("dicom $displayname aetitle $aetitle showmenus $showmenus");
	return true; 
	}
	
function  updatemcdetails( 
   $displayname,
   $user,   
   $gateway,
   $colleagues, 
   $destination,
   $showmenus, 
   $regusers, 
   $sendemail, 
   $email,
   $emailtype,
   $worklistonly,
   $templist
   )
   {	sqt('updatemcdetails');
	$update = <<<EXX
	UPDATE vrmd  
				SET
				VM_SHOWMENUS='$showmenus',
				VM_COLLEAGUES='$colleagues',
				VM_DESTINATION='$destination',
				VM_REGUSERS='$regusers',
				VM_SENDEMAIL='$sendemail',
				VM_EMAIL='$email',
				VM_EMAILTYPE='$emailtype',
				VM_WORKLISTONLY='$worklistonly',
				VM_TEMPLIST='$templist'
				WHERE VM_DISPLAYNAME ='$displayname' AND
				VM_USER='$user' AND
				VM_GATEWAY='$gateway'			
   
EXX;
	$result = db_query($update,$GLOBALS['DB_Link']) or
	exit("Cant do update on vrmd table".db_error($GLOBALS['DB_Link']));

	return true;
}
function  insertmcdetails( 

   $displayname,
   $user,   
   $gateway,
   $colleagues,
   $destination, 
   $showmenus, 
   $regusers, $sendemail,$email,$emailtype,$worklistonly,$templist
)
   {sqt('insertmcdetails');	
	$insert = <<<EXX
INSERT INTO vrmd (VM_DISPLAYNAME,VM_USER,VM_GATEWAY,VM_COLLEAGUES,VM_DESTINATION,
				VM_SHOWMENUS,VM_REGUSERS,VM_SENDEMAIL,VM_EMAIL,VM_EMAILTYPE,
				VM_WORKLISTONLY,VM_TEMPLIST)
				VALUES (
    '$displayname',  '$user','$gateway', 
   '$colleagues', '$destination',
   '$showmenus', 
   '$regusers', 
   '$sendemail',
   '$email',
   '$emailtype','$worklistonly','$templist'
   )
EXX;
	$result = db_query($insert,$GLOBALS['DB_Link']) or
	exit("Cant do insert on vrmd table".db_error($GLOBALS['DB_Link']));

	return true;
}

function  getmcdetails( 
   $displayname,  $user,   $gateway,  &$colleagues, &$destination, &$showmenus, &$regusers, 
   &$sendemail,&$email,&$emailtype,&$worklistonly,&$templist)
   {sqt('getmcdetails');
   
   	$lookup =<<<MMM
   	SELECT * FROM vrmd WHERE VM_DISPLAYNAME='$displayname' AND 
   				VM_USER ='$user' AND VM_GATEWAY='$gateway'
MMM;
	//echo "getmcdetails: ".$lookup;
$result = db_query($lookup,	$GLOBALS['DB_Link']); 
//echo " result ".$result;
	if(0==db_num_rows($result)) return false;;

$l = db_fetch_array_assoc($result);
	$colleagues=$l['VM_COLLEAGUES'];
	$destination=$l['VM_DESTINATION'];
//	//echo "in getmcm aetitle is",$aetitle;
	$showmenus=$l['VM_SHOWMENUS'];
	$regusers=$l['VM_REGUSERS'];
	$sendemail=$l['VM_SENDEMAIL'];
	$email=$l['VM_EMAIL'];
	$emailtype=$l['VM_EMAILTYPE'];
	$worklistonly=$l['VM_WORKLISTONLY'];
	$templist = $l['VM_TEMPLIST'];
	db_free_result($result);
	return true; 
	}
	
	function deletemc($key,$user,$gateway)
	{sqt("deletemc $key $user $gateway");
		//delete record from vrmd 
			$delete=<<<DDD
		DELETE FROM vrmd WHERE  VM_DISPLAYNAME='$key' AND 
   				VM_USER ='$user' AND VM_GATEWAY='$gateway'
DDD;
		$result = db_query($delete,	$GLOBALS['DB_Link']); 
		//set all references to null
		$insert = <<<ZZZ
		UPDATE vrcp SET VC_MCDEST = '' WHERE  VC_MCDEST='$key' AND 
   				VC_USER ='$user' AND VC_GATEWAY='$gateway'

ZZZ;
		$result = db_query($insert,$GLOBALS['DB_Link']) or
		exit("error in deletemc update of vrcp".db_error($GLOBALS['DB_Link']));
		//if we have any totally rows, then delete them
		//set all references to null
		$insert = <<<ZZZ
		DELETE FROM vrcp WHERE  VC_MCDEST='' AND VC_DICOM='' AND VC_ACTION='-1'
ZZZ;
		$result = db_query($insert,$GLOBALS['DB_Link']) or
		exit("error in deletemc delete from vrcp".db_error($GLOBALS['DB_Link']));
		
		return true;
	}
	
  
   
    	
function getspeciallinks($tocall,$user)
{
	$lookup = <<<TTT
	SELECT US_RL_ROLE FROM users WHERE US_USERID='$user'
TTT;

	$result = db_query($lookup,	$GLOBALS['DB_Link']); 
	if ($result=="") return false;
	$l = db_fetch_array_assoc($result);
	// for now, we just run thru the xdsregistry and display all the entries
    $role = $l['US_RL_ROLE'];
//  //echo "role is ".$role;
	db_free_result($result);
	
	$lookup = <<<TTT
	SELECT * FROM special_links WHERE SL_ROLE='$role'
TTT;

	$result = db_query($lookup,	$GLOBALS['DB_Link']); 
		if(0==db_num_rows($result))return "";

	$retval="";
	
	while ($l = db_fetch_array_assoc($result)) {
	// for now, we just run thru the xdsregistry and display all the entries
    $link = $l['SL_URL'];
	$label = $l['SL_TEXT'];
	$retval .= $tocall($link,$label);
	}
	
	db_free_result($result);
	return $retval;
	
}

 function getvrcpentries($tocall,$userid,$gateway)
 {
 	//find this users config on this gateway, and call the callback once for each record found
 	//concatenating the results into one string
 	$lookup = <<<TTT
	SELECT * FROM vrcp WHERE VC_USER='$userid' AND VC_GATEWAY='$gateway'
TTT;
////echo $lookup;
	$result = db_query($lookup,	$GLOBALS['DB_Link']); 
////echo "result ".$result;
	if ($result=="") return false;
	//echo "in getvrcpentries got ".db_num_rows($result);
	$retval="";
	while ($l = db_fetch_array_assoc($result)) {
	$rowid = $l['VC_ID'];
	$dicomdev = $l['VC_DICOM'];
//	//echo $dicomdev;
	$dicomwado = $l['VC_DICOMWADO'];
	$action= $l['VC_ACTION'];
	
	$filter=$l['VC_CREATETIME'];
	$mcdestdev = $l['VC_MCDEST'];
	$mcdestdevwado = $l['VC_MCDESTWADO'];
	$description = $l['VC_DESCRIPTION'];
//	//echo "about to call onerow $dicomdev";
	$retval .= $tocall($rowid, $dicomdev,$dicomwado,$action,$filter,
							$mcdestdev,$mcdestdevwado,$description);

	}
	db_free_result($result);
	return $retval;
	
}

function getvrcpentry($rowid,&$userid,&$gateway,&$createtime,&$dicom,
					&$dicomwado,&$action,&$filter,&$mcdest,&$mcdestwado,&$description)
 {	sqt("getvrcentry");
 	//find this users config on this gateway, and call the callback once for each record found
 	//concatenating the results into one string
 	$lookup = <<<TTT
	SELECT * FROM vrcp WHERE VC_ID='$rowid'
TTT;
	$result = db_query($lookup,	$GLOBALS['DB_Link']); 
	if (db_num_rows($result)==0) return false;
	//echo "in getvrcpentries got ".db_num_rows($result);
	$l = db_fetch_array_assoc($result);
    $createtime = $l['VC_CREATETIME'];
	$dicomdev = $l['VC_DICOM'];
	$dicomwado = $l['VC_DICOMWADO'];
	$action= $l['VC_ACTION'];
	$filter=$l['VC_CREATETIME'];
	$mcdestdev = $l['VC_MCDEST'];
	$mcdestdevwado = $l['VC_MCDESTWADO'];
	$description = $l['VC_DESCRIPTION'];

	db_free_result($result);
	return true;
}

function getgatewayinfo ($gw,&$desc, &$lastheard)
{	$query = "SELECT * FROM gateways WHERE GW_VR_vAETitle = '$gw'";
//	echo "query in getgatewayinfo is $query";
	$result = db_query($query,	$GLOBALS['DB_Link']); 

	if ($result=="") return "????";
    $totrecs = db_num_rows($result);
 //   echo "torecs in getgatewayinfo is $totrecs";
    if ($totrecs==0) return "";

    $l = db_fetch_array_assoc($result);
	$gwurl=$l['GW_URL'];
	$desc = $l['GW_DETAILS'];
	$lastheard = $l['GW_LASTHEARD'];

//	echo ("gwurl is $gwurl");
	return $gwurl;
}
function getgatewayentries ($tocall,$userid)
{	$query = "SELECT * FROM gateways"; // will need to filter by userid
	$result = db_query($query,	$GLOBALS['DB_Link']); 
	if ($result=="") return "????";
    $totrecs = db_num_rows($result);
    if ($totrecs==0) return "";
	if ($result=="") return "";
	$retval="";
	while ($l = db_fetch_array_assoc($result)) {

	$gw = $l['GW_VR_VAETITLE'];
	$gwurl=$l['GW_URL'];
	$desc = $l['GW_DETAILS'];
	$lastheard = $l['GW_LASTHEARD'];
	
	$tocall ($gw,$gwurl, $desc,$lastheard);
	}		

	db_free_result($result);
	return $retval;
	
//	

//	echo ("gwurl is $gwurl");
	return $gwurl;
}
function getorderbytracking($tocall,$tracking)
{// call a routine for each 'match' in the orders table
sqt('getorderbytracking');
// 	INNER JOIN gateways ON orders.OR_VR_VAETITLEORIGIN = gateways.GW_VR_VAETITLE

$lookup = <<<TTT
 	SELECT orders.* FROM orders WHERE orders.OR_TRACKING = '$tracking'
 	LIMIT 1
TTT;
//echo ("query is $lookup");

////echo $lookup;
	$result = db_query($lookup,	$GLOBALS['DB_Link']); 
////echo "result ".$result;
	if ($result=="") return "";
    $totrecs = db_num_rows($result);
  ///  echo "torecs in getorderbytracking is $totrecs";
    if ($totrecs==0) return "";
	$retval="";
	$l = db_fetch_array_assoc($result); 
		
$gwurl=getgatewayinfo ($l['OR_VR_VAETITLEORIGIN'],$desc,$lastheard);
if ($gwurl=="") return "";
$guid = $l['OR_ORDERGUID'];
$tracking = $l['OR_TRACKING'];
$origin = $l['OR_VR_VAETITLEORIGIN'];
$dest = $l['OR_VR_VAETITLEDEST'];
$time = $l['OR_TIME'];
$status = $l['OR_GLOBALSTATUS'];
$description = $l['OR_DESCRIPTION'];
$patientname = $l['OR_PATIENTNAME'];
$patientid = $l['OR_PATIENTID'];
$modality = $l['OR_MODALITY'];
$series = $l['OR_SERIES'];
$nimages = $l['OR_IMAGES'];


$retval .= $tocall ($gwurl, $guid, $tracking, $origin, $dest, $time, $status, $description,
				$patientname, $patientid, $modality, $series, $nimages);
				

	db_free_result($result);
	return $retval;
	
}				
		




function getordermatches($tocall,$limit=60,$lowdate, $highdate , &$totrecs)
{// call a routine for each 'match' in the orders table
sqt('getordermatches');
$totrecs = 0;
$countit = <<<XXX

SELECT  * FROM orders
XXX;

$result = db_query($countit, $GLOBALS['DB_Link']);
if ($result =="") {$totrecs = 0; return false;}
    $totrecs = db_num_rows($result);

$lookup = <<<TTT
 	SELECT orders.*,gateways.GW_URL FROM orders 

 	INNER JOIN gateways ON orders.OR_VR_VAETITLEORIGIN = gateways.GW_VR_VAETITLE
WHERE (orders.OR_TIME >= $lowdate) and (orders.OR_TIME<=$highdate)

 	ORDER BY orders.OR_TIME DESC
TTT;

$lookup .= " 	LIMIT $limit"; // add limit clause regardless

////echo $lookup;
	$result = db_query($lookup,	$GLOBALS['DB_Link']); 
////echo "result ".$result;
	if ($result=="") return "";
	$retval="";
	while ($l = db_fetch_array_assoc($result)) {
		
$gwurl=$l['GW_URL'];
$guid = $l['OR_ORDERGUID'];
$tracking = $l['OR_TRACKING'];
$origin = $l['OR_VR_VAETITLEORIGIN'];
$dest = $l['OR_VR_VAETITLEDEST'];
$time = $l['OR_TIME'];
$status = $l['OR_GLOBALSTATUS'];
$description = $l['OR_DESCRIPTION'];
$patientname = $l['OR_PATIENTNAME'];
$patientid = $l['OR_PATIENTID'];
$modality = $l['OR_MODALITY'];
$series = $l['OR_SERIES'];
$nimages = $l['OR_IMAGES'];


$retval .= $tocall ($gwurl, $guid, $tracking, $origin, $dest, $time, $status, $description,
				$patientname, $patientid, $modality, $series, $nimages);
				
	}
	db_free_result($result);
	return $retval;
	
}				
		

function getregistrymatches($tocall,$trackingfilter="")
{ // call a routine for each 'match' in the xds registry

	$lookup = <<<TTT
	SELECT * FROM xdsregistry 
TTT;
	if ($trackingfilter!="")$lookup .=" WHERE XD_TRACKING = '$trackingfilter'";
////echo $lookup;
	$result = db_query($lookup,	$GLOBALS['DB_Link']); 
////echo "result ".$result;
	if ($result=="") return false;
	$retval="";
	while ($l = db_fetch_array_assoc($result)) {

	// for now, we just run thru the xdsregistry and display all the entries
    $gw =$l['XD_VR_VAETITLE'];
	$guid= $l['XD_OS_STUDYGUID'];
	$tracking=$l['XD_TRACKING'];
	$address=$l['XD_PATIENTADDR'];
	$patient=$l['XD_PATIENT'];
	$id=$l['XD_MODALITY'];
	$comments=$l['XD_COMMENTS'];
	$history=$l['XD_UNKNOWN'];
	$datetime =$l['XD_STUDYTIME'];
	$description = $l['XD_STUDYDESCRIPTION'];
	$status = $l['XD_STATUS'];
	$retval .=$tocall ($gw,$guid,$tracking,$address,$patient,$id,
						$comments,$history,$datetime,$description,$status);
	}
	db_free_result($result);
	return $retval;
	
}
function getxdsinfo($guid,&$tracking,&$account,&$address,&$history,&$comments)
{
		$lookup = <<<TTT
	SELECT * FROM xdsregistry WHERE XD_OS_STUDYGUID = '$guid';
TTT;

	$result = db_query($lookup,	$GLOBALS['DB_Link']); 
////echo "result ".$result;
	if ($result=="") return false;
	$retval="";
	while ($l = db_fetch_array_assoc($result)) {

	// for now, we just run thru the xdsregistry and display all the entries
    $wado =$l['XD_VR_VAETITLE'];
	$guid= $l['XD_OS_STUDYGUID'];
	$tracking=$l['XD_TRACKING'];
	$address=$l['XD_PATIENTADDR'];
	$patient=$l['XD_PATIENT'];
	$id=$l['XD_MODALITY'];
	$comments=$l['XD_COMMENTS'];
	$history=$l['XD_UNKNOWN'];
	$datetime =$l['XD_STUDYTIME'];
	$description = $l['XD_STUDYDESCRIPTION'];
	$status = $l['XD_STATUS'];
	db_free_result($result);
	return $retval;
	}
	
}	
function readconfig()
{
	//open the main db connection and proceed to read config records

	$db = $GLOBALS['DB_Database'];
	$link = db_connect($GLOBALS['DB_Connection'],// open as persistent, why not?
	$GLOBALS['DB_User'], $GLOBALS['DB_Password'])
	or die("Could not connect in readconfig : " . db_error($link));
	db_select($db,$link) or die("Could not select database in readconfig");

	$GLOBALS['DB_Link']=$link; //preserve for use everywhere subsequently
	$query = <<<EOQ
SELECT * FROM params;
EOQ;

	$result = db_query($query,$link ) or die("ReadConfig Query failed : " . db_error($link));

	$params = db_fetch_array_assoc($result); //only gets the pin and role

	db_free_result($result);

	$query = <<<EOQ
SELECT * FROM config WHERE CN_TYPE ="MASTER"
EOQ;

	$result = db_query($query,$link ) or die("Readconfig Query failed : " . db_error($link));

	$config = db_fetch_array_assoc($result); //only gets the pin and role

	db_free_result($result);

	//set up new session variables for these config parameters
	$GLOBALS['Partner']=$config['CN_PARTNER'];
	$GLOBALS['ShortName']=$config['CN_SHORTNAME'];
	$GLOBALS['StyleSheet']=$config['CN_STYLESHEET'];
	$GLOBALS['PurpleStyleSheet']=$config['CN_PURPLESTYLESHEET'];
	$GLOBALS['RootURL']=$config['CN_ROOTURL'];
	$GLOBALS['HomePage']=$config['CN_HOMEPAGE'];
	$GLOBALS['phpMyAdminURL']=$config['CN_PHPMYADMINURL'];
	$GLOBALS['SW_MajorVersion'] = SWMajorVersion;
	$GLOBALS['SW_MinorVersion'] = SWMinorVersion;

	// get these parameters, and then check to see if we are compatible with what we are expecting


	$GLOBALS['SchemaMajorVersion']=$params['PA_MAJOR'];
	$GLOBALS['SchemaMinorVersion']=$params['PA_MINOR'];
	$GLOBALS['TraceLevel']=    $params['PA_TRACELEVEL'];
	$GLOBALS['SchemaComment'] = $params['PA_COMMENT'];
	$GLOBALS['SchemaDate'] = $params['PA_CREATETIME'];

	if ($GLOBALS['SchemaMajorVersion'] != SWMajorVersion)
	$str =  "Schema mismatch with software\r\n";
	elseif ($GLOBALS ['SchemaMinorVersion'] > SWMinorVersion)
	$str =  "Software has not been upgraded to correspond to new schema level\r\n";
	elseif ($GLOBALS ['SchemaMinorVersion']< SWMinorVersion)
	$str = "Schema has not been upgraded to correspond to new software\r\n";
	else $str = "Schema and Software match perfectly\r\n";
	$GLOBALS['SoftwareStatus']=$str;

}//readConfig


function insertreplay ($gateway, $time, $uri)
{
	//		$towrite = str_replace("'","''",$uri);//make sql happy

	$insert = <<<EZZ
	INSERT INTO replay (RP_GATEWAY, RP_TIMESTAMP, RP_WEBSERVICECOMMAND)
	VALUES ('$gateway','$time','$uri')
EZZ;

	$result = db_query($insert,$GLOBALS["DB_Link"])

	or
	exit ("Cant do insert on replay:".db_error($GLOBALS['DB_Link']));

}
function validuseridemail($userid,$email)
{
	// verify the email and email are in the database
////echo "looking for ".$userid. " email ".$email;	
	$lookup = <<<TTT
	SELECT US_EMAIL FROM users WHERE US_USERID='$userid' AND US_EMAIL = '$email'
TTT;
////echo $lookup;
	$result = db_query($lookup,
	$GLOBALS['DB_Link']); 
////echo "result ".$result;
	if ($result=="") return false;
	$line = db_fetch_array_assoc($result); //should only be one
//s//echo $line['US_EMAIL'];
	if ($line=="") return false;
//	//echo "email is".$line['US_email'];
	return ($email==$line['US_EMAIL']);
}
function validuseridpin($userid,$pin)
{//if freelogin, just require a valid userid
	$freelogin = $_SESSION['freelogin'];
	sqt("validuserpin userid $userid freelogin $freelogin");	
	if ($freelogin==true)
$lookup = <<<TTT
	SELECT US_PIN FROM users WHERE US_USERID='$userid'
TTT;
else		

$lookup = <<<TTT
	SELECT US_PIN FROM users WHERE US_USERID='$userid' AND US_PIN = '$pin'
TTT;
////echo $lookup;
	$result = db_query($lookup,
	$GLOBALS['DB_Link']); 
////echo "result ".$result;
	if ($result=="") return false;
	$line = db_fetch_array_assoc($result); //should only be one
//	//echo $line;
	if ($line=="") return false;
//	//echo "pin is".$line['US_PIN'];
	if($freelogin==true) return true; else
	return ($pin==$line['US_PIN']);
}

function insertuserdetails(
   $userid,   
   $name,
   $title, 
   $addr1, 
   $addr2, 
   $city, 
   $state,
   $zip,
   $email,
   $dob, 
   $sex, 
   $phone, 
   $company, 
   $healthcareSpecialist, 
   $stateLicense,
   $pin)
{
	// check to ensure no one is using this email in the database
	// ifnot, then create it
	
//	//echo "insertemailpin:".$email." ".$pin;
	
	$lookup = <<<TTT
	SELECT US_PIN FROM users WHERE US_USERID='$userid'
TTT;
 	
	$result = db_query($lookup,
	$GLOBALS['DB_Link']) or
	die("Lookup Pin from Email in insertuseridpinfailed : " . db_error());
	
	$line = db_fetch_array_assoc($result); //should only be one
	if ($line!="") return false;// if already there, forget it
//	//echo " not found, inserting";
	$insert = <<<EXX
INSERT INTO users (US_USERID,US_NAME,US_TITLE,US_ADDR1,US_ADDR2,US_CITY,
			US_STATE,US_ZIP,US_EMAIL,US_DOB,US_SEX,US_PHONE,US_COMPANY,
			US_HEALTHCARESPECIALIST,US_STATELICENSE,US_PIN)
			
VALUES (  
   '$userid',   
   '$name',
   '$title', 
   '$addr1', 
   '$addr2', 
   '$city', 
   '$state',
   '$zip',
   '$email',
   '$dob', 
   '$sex', 
   '$phone', 
   '$company', 
   '$healthcareSpecialist', 
   '$stateLicense',
   '$pin'
   )
EXX;
	$result = db_query($insert,$GLOBALS['DB_Link']) or
	exit("Cant do insert on user table".db_error($GLOBALS['DB_Link']));

	return true;
}

function getuserdetails(
  &$userid,   
   &$name,
   &$title, 
   &$addr1, 
   &$addr2, 
   &$city, 
   &$state,
   &$zip,
   &$email,
   &$dob, 
   &$sex, 
   &$phone, 
   &$company, 
   &$healthcareSpecialist, 
   &$stateLicense,
   &$pin,
   &$defaultworklist,
   &$defaultgateway,
   &$backupgateways)
   {
   		$lookup = <<<TTT
	SELECT * FROM users WHERE US_USERID='$userid'
TTT;

	$result = db_query($lookup,	$GLOBALS['DB_Link']); 
	if ($result=="") return false;

	$line = db_fetch_array_assoc($result); //should only be one
	if ($line=="") return false;
	
$userid = $line['US_USERID'];
$name= $line['US_NAME'];
$title = $line['US_TITLE'];
$addr1 = $line['US_ADDR1'];
$addr2 = $line['US_ADDR2'];
$city = $line['US_CITY'];
$state = $line['US_STATE'];
$zip = $line['US_ZIP'];
$email= $line['US_EMAIL'];
$dob= $line['US_DOB'];
$sex= $line['US_SEX'];
$phone= $line['US_PHONE'];
$company= $line['US_COMPANY'];
$healthcareSpecialist= $line['US_HEALTHCARESPECIALIST'];
$stateLicense=$line['US_STATELICENSE'];
$pin=$line['US_PIN'];
$defaultworklist=$line['US_DEFAULTWORKLIST'];
$defaultgateway=$line['US_DEFAULTGATEWAY'];
$backupgateways=$line['US_BACKUPGATEWAYS'];
	return true;
}

	
function tracelog ($owner, $code,$comment,$blob)
{	 $v = $GLOBALS['TraceLevel']; // write to the tracetable

if ($v==1)// only log on tracelevel==1, use separate connection
{
	$towrite = str_replace("'","''",$blob);//make sql happy

	$link = $GLOBALS['DB_Link']; //use existing connection
	$insert = <<<EXX
INSERT INTO trace (TR_OWNER,TR_CODE,TR_COMMENT,TR_BLOB)
VALUES ('$owner','$code','$comment','$towrite')
EXX;
	$result = db_query($insert,$GLOBALS['DB_Link']) or
	exit("Cant do insert on tracelog:".db_error($GLOBALS['DB_Link']));

	/*		$rows_affected = db_affected_rows($GLOBALS['DB_Link']);
	if ($rows_affected!=1) $ret = "INSERT FAILED"; else $ret = "INSERTED";
	*/
}
}

function db_sql_insert_into_remote_commands($gw,$ec,$ed,$st)
{/* Performing SQL INSERT
*/
$query = <<<EXX
INSERT INTO remote_commands (RC_VR_VAETITLE,RC_PINKBOXSTATUS,
        RC_REMOTECOMMAND,RC_REMOTESTATUS)
VALUES ('$gw','$ec','$ed', '$st')
EXX;

$result = db_query($query,$GLOBALS['DB_Link']) ;//or die("Query failed : " . db_error());
return $result;
}

function bump_counter($key,$desc)
{
	/*
	lookup and bump a counter in the db
	if it doesn't exist then create it and assign value of 1

	THIS WHOLE THING SHOULD REALLY BE RUN INSIDE A TRANSACTION

	*/

	$lookup = <<<TTT
	SELECT * FROM COUNTERS WHERE CN_KEY='$key'
TTT;

	$result = db_query($lookup,
	$GLOBALS['DB_Link']) or
	die("Counter Query failed : " . db_error());
	$line = db_fetch_array_assoc($result); //should only be one
	if ($line=="")
	{// didn't find the counter, so insert it

	$insert = <<<III
	
	INSERT INTO counters (CN_COUNTER, CN_KEY, CN_DESC)
					 VALUES('1','$key','$desc') ;
III;
	$result = db_query($insert,$GLOBALS['DB_Link']) or
	die("Counter Insert failed : ".db_error());
	return 1;


	}
	else
	{//  found counter, so bump it()
	$newcount = $line['CN_COUNTER']+1;
	$update = <<<VVV
	UPDATE counters SET CN_COUNTER = '$newcount' WHERE CN_KEY='$key';
VVV;

	$result = db_query($update,$GLOBALS['DB_Link']) or
	die ("Counter Update failed : ".db_error());

	return $newcount;

	}

}
function updateuseridpassword($userid,$password)
{
$update = <<<VVV
	UPDATE users SET US_PIN = '$password' WHERE US_USERID='$userid';
VVV;

	$result = db_query($update,$GLOBALS['DB_Link']) or
	die ("User password update failed : ".db_error());
}

function db_prepare_order_insert($id,$orderguid,$tracking,$origin,$dest,$description,
$patientname,$patientid,$modality)
{
	$insert = <<<EXX
INSERT INTO orders(OR_ID,OR_ORDERGUID, or_Tracking, OR_VR_vAETitleOrigin,
    OR_VR_vAETitleDest,or_description,or_patientName,or_patientID,or_modality ) 
VALUES ('$id','$orderguid','$tracking','$origin',
	'$dest','$description','$patientname','$patientid','$modality');
EXX;
	return $insert;
}
function db_prepare_xds_insert($id,$studyguid,$tracking,$vaetitle,$nimages,$nseries,$studydescription,$studytime,$comments,
$modaility,$affiliate,$history,$patient,$patientaddr,$status)
{
	$insert = <<<EXX
	INSERT INTO xdsregistry ( XD_ID , XD_OS_STUDYGUID , XD_TRACKING , XD_VR_VAETITLE , XD_NIMAGES , XD_NSERIES , 
	XD_STUDYDESCRIPTION , XD_STUDYTIME , XD_COMMENTS , XD_MODALITY , XD_TIMESTAMP , XD_AFFIILIATE , 
	XD_HISTORY , XD_PATIENT , XD_PATIENTADDR , XD_STATUS ) 
VALUES (
'$id', '$studyguid', '$tracking', '$vaetitle', '$nimages', '$nseries', '$studydescription', '$studytime', '$comments', 
'$modaility',NOW() , '$affiliate', '$history', '$patient', '$patientaddr', '$status'
);

EXX;
	return $insert;
}

function db_prepare_order_data_insert($orderguid,$seriesguid)
{
	$insert = <<<EXX
INSERT INTO order_data(OS_OR_orderGUID, OS_dataGUID) 
VALUES ('$orderguid','$seriesguid');
EXX;
	return $insert;
}

function db_prepare_routing_queue_insert($requestid,$orderguid,$mcguid,$dest,$origin,
$protocol,$globalstatus,$itemtype)
{
	$insert = <<<EXX
INSERT INTO routing_queue (RQ_ID,  RQ_OR_ORDERGUID, RQ_OS_DATAGUID,
		RQ_VR_DESTINATIONVAETITLE, RQ_VR_ORIGINVAETITLE,RQ_PROTOCOL,
		RQ_GLOBALSTATUS,RQ_ITEMTYPE) 
VALUES ('$requestid','$orderguid','$mcguid','$dest','$origin',
	'$protocol','$globalstatus','$itemtype');
EXX;
	return $insert;
}
function db_prepare_order_series_update($status,$og) {
	$query = "UPDATE order_series SET OS_status='".$status.
	"' WHERE OS_DataGuid='".$og."'";
	return $query;
}
function db_prepare_orders_update_status($status,$og,$nseries,$nimages){

	$query = "UPDATE orders SET OR_globalStatus='$status', OR_series = '$nseries'".
	", OR_images = '$nimages'".
	" WHERE OR_OrderGuid='".$og."'";

	return $query;
}
function db_prepare_xds_update_status($status,$og){

	$query = "UPDATE xdsregistry SET XD_STATUS='".$status.
	"' WHERE XD_OS_STUDYGUID='$og'";

	return $query;
}
function db_prepare_routing_queue_update ($globalStatus,$bytesTotal,$bytesTransferred,
$restartCount,$timeStarted,$timeCompleted,$requestid)
{

	$query = "UPDATE routing_queue SET ".
	"RQ_GLOBALSTATUS ='".$globalStatus."',".
	"RQ_BYTESTOTAL ='".$bytesTotal."',".
	"RQ_BYTESTRANSFERRED ='".$bytesTransferred."',".
	"RQ_RESTARTCOUNT ='".$restartCount."',".
	"RQ_TIMESTARTED ='".$timeStarted."',".
	"RQ_TIMECOMPLETED ='".$timeCompleted."'".
	"  WHERE RQ_ID ='".$requestid."'";

	return $query;
}

function db_prepare_select_general($table,$filter)
{
	$query = "SELECT * FROM ".$table;
	if ($filter!="") $query.=" WHERE ".$filter;
	$query .=  " LIMIT 0, 30";
	return $query;
}
function db_prepare_select_routing_queue($og)
{
	$query = "SELECT * FROM routing_queue
                 WHERE RQ_VR_destinationVAETitle='".$og."'";
	return $query;
}
function db_prepare_order_data_select ($og){
	$query = "SELECT * FROM order_data WHERE OS_OR_OrderGuid='".$og."'";
	return $query;
}
function db_prepare_order_select($og){

	$query = "SELECT * FROM orders WHERE OR_OrderGuid='".$og."'";
	return $query;
}

function db_prepare_xds_select($og){

	$query = "SELECT * FROM xdsregistry WHERE XD_OS_STUDYGUID='".$og."'";
	return $query;
}

function db_prepare_virtrad_select($gw){
	$table = "virtrad";
	$filter = "(vr_GW_Gateway ='".$gw."')";
	$query = "SELECT * FROM ".$table;
	if ($filter!="") $query.=" WHERE ".$filter;
	return $query;
}
function db_prepare_gateways_select($gw)
{

	$query = "SELECT * FROM gateways WHERE
                 (GW_VR_vAETitle = '".$gw."')";
	return $query;
}
function db_prepare_select($table)
{return
"SELECT * FROM $table";}

function db_prepare_replay_select($gw)
{
	$query = "SELECT * FROM replay WHERE RP_GATEWAY='$gw'";
	return $query;
};
function db_empty_table($tab)
{ 
	$del = "DELETE FROM $tab;";
	$result = db_query($del,$GLOBALS['DB_Link']) or 
      die(db_error($GLOBALS['DB_Link']));
      return $result;
}


// below this point are just general utility funcitons that probably should be elsewhere

function cleanreq($fieldname, $preserve='', $allowed_tags='')
{ // take an input field and clean it up before going any further
$value = $_REQUEST[$fieldname];
{if (empty($preserve))
{	$value = strip_tags($value,$allowed_tags);
}
$value = htmlspecialchars($value);
return $value;
}


}
function errortext($s)
{	$ec=ERROR_COLOR;
	return '<FONT COLOR="'.$ec.'">'.$s."</FONT>";
	
}

?>