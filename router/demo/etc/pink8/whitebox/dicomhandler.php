<?PHP
require_once("../whitebox/wbsubs.inc");
require_once("../whitebox/displaydicomconfig.inc");
require_once("../whitebox/displaydialogcomplete.inc");

function b($s)
{
	return $_REQUEST[$s];
	//	echo "$s is ".$$s."\n\r";
}

// handle completiton of dicom editing function
//
// there are two key variables that determine the flow here insertnew and rowid
//
// if insertnew is set then add a completely new dicom element to vrdt
// 		in this case the key variable is the name of the new element to add
//		verify uniquess of the new display name and insert the record into vrdt table
//			ifnot unique then give it up with an error 
// 
// if insertnew is not set then we are going to update an existing dicom element
//		in this case the key variable is the 'new name' for the element and oldkey is the old
//		if the two are equal then this is an easy update - just change the fields as indicated with a simple update of the table emtry
//		otherwise check to see that this new key is unique
//			ifso then insert a new dicom element to vrdt as above and set flag to say primary key was changed
//			ifnot give it up with an error
// 
// ---- vrcp table fixup processing
//
// if primary key was changed above then 
//				go thru the vrcp table and fix up all the entries to reflect the keychange
//
// if rowid is non-null then this adjustment to the dicom column should happen on that row of the vrcp table 
// otherwise, a new row is added to the bottom
//
// if the wado flag hsa changed state then the wado fields in vrcp table are patched up
// 
//

readconfig(); // get reconnected to database
session_start();

sqltraceon(); // make sure we get it all
// get all of the fields from the form

//these are passed in from vrcp and are immutable
$submit = b("submit");
$gateway = b("gateway");
$user = b("user");
$rowid = b("rowid");
$insertnew = b("insertnew");
$readonly = (b("readonly")=="yes");
$key = b("displayname");
$oldkey = b("olddisplayname");
$showmenus=  (b("showinmenus")!="")?"1":"0";
sqt("dicomhandler $gateway $user rowid $rowid submit $submit insertnew $insertnew key $key oldkey $oldkey showmenus $showinmenus");

// these fields are specific to this dicom handler
//+
$aetitle = b("dicomaetitle");
$dicomport = b("dicomport");
$dicomipaddr = b("dicomipaddress");
$comment = b("comment");
//-

// check for errors, post them and re-display if necessary
//+
$key_error="";
$aetitle_error="";
$dicomport_error="";
$dicomipaddr_error="";
$comment_error="";

// check out all of the errors
if ($key =="") $key_error = errortext("** please enter a name **");

if ($aetitle =="") $aetitle_error = errortext("**you must supply an aetitle**");

// if there is a problem with a field then clear it out

if ($key_error!="")$key="";
if ($aetitle_error!="") $aetitle="";
if ($dicomport_error!="") $dicomport="";
if ($dicomipaddr_error!="") $dicomipaddr="";
if ($comment_error!="") $comment = "";

// if any errors put the thing up again
if (($key_error!="")
or ($aetitle_error!="")
or ($dicomport_error!="")
or ($dicomipaddr_error!="")
or ($comment_error!="")){

echo display_dicom_config(
	$rowid,	$readonly,	$key,	$user,
	$gateway,	$aetitle,	$showmenus,	$dicomipaddress,
	$dicomport,	$comment,	$key_error,	$gateway_error,
	$aetitle_error,	$dicomipaddr_error,	$dicomport_error,	$comment_error);
exit;
}
// end of error checking and handling of the incoming  arguments
//-


// we only get here on good news from the input form fields
// form was good, adjust database
if ($oldkey=="") $primarykeychanged=false; else
$primarykeychanged = ($key!=$oldkey)?true:false;
$wadochanged = false; // set after reading the record later

if ($submit =='DELETE')
{
//handle delete	
$success = getdicomdetails($key,$user,$gateway,$oldaetitle,$oldshowmenus,$olddicomipaddress,$olddicomport,$oldcomment);
if ($success==false) exit_with_message("No such Dicom device");
$success = deletedicom($key,$user,$gateway);
if ($success==false) exit_with_message("There was a problem deleting dicom device $key");
exit_with_message("Dicom device $key was deleted");
}
if ($insertnew=='insert')

{
sqt("dicomhandler case  insert");
	
// insert a new record
//  before we insert, make sure the record is not already there
$success = getdicomdetails($key,$user,$gateway,$oldaetitle,$oldshowmenus,$olddicomipaddress,$olddicomport,$oldcomment);
// if it's already there, let's suggest that the user try again
if ($success==true) exit_with_message("The DICOM device $key is already defined".
							" - click on an existing entry to modify");
//otherwise insert this as a new record
sqt("dicomhandler case fresh insert");
 patchandexit($rowid,$key,$user,$gateway,$aetitle,$showmenus,$dicomipaddress,$dicomport,$comment,false,
 	"A new DICOM record for $key was added");
}
else
{
// alright, this is the code to handle an update
// first, separate out the new vs old case
$success = getdicomdetails($key,$user,$gateway,$oldaetitle,$oldshowmenus,$olddicomipaddress,$olddicomport,$oldcomment);

if ($primarykeychanged==true) {
// if changing primary key then make sure nothing else is named similarly
		if ($success==true) exit_with_message("the DICOM device $key is already defined".
									"- click on an existing entry to modify");
// alright, we changed to a new key, it's almost like an insert
sqt ("case dicomhandler changed key");	
$wadochanged=($oldshowmenus!=$showmenus)?true:false;	
	 patchandexit($rowid,$key,$user,$gateway,$aetitle,$showmenus,$dicomipaddress,$dicomport,$comment,$wadochanged
	 ,"DICOM device $key was added to your configuration");
}
// this is a clean update, with no key change
sqt ("dicomhandler case straight update");
$success = updatedicomdetails(	$key,$user,	$gateway,$aetitle,	$showmenus,	$dicomipaddress,$dicomport,	$comment);
if ($success==false) exit_with_message("could not update vrdt dicom entry");
$wadochanged=($oldshowmenus!=$showmenus)?true:false;	
$success = vrcpcleanup($rowid,$key,$user,$gateway,$oldkey,$showmenus,$wadochanged);
if ($success==true) exit_with_message("The DICOM device characteristics for $key were updated");
	 else exit_with_message ("problem in vrcpcleanup for dicomhandler");
//that's it should never get here	

}//end of code to handle update

// vrcp cleanup processing
function vrcpcleanup ($rowid,$key,$user, $gateway,$oldkey,$showmenus,$wadochanged)
{
	sqt( "vrcpcleanup rowid $rowid showmenus $showmenus wadochange $wadochanged key $key oldkey $oldkey");
	//  patch will either insert or update depending on row id
	 $success =	patchvcrpdicomrow($rowid,$key,$user, $gateway);

	// if we changed the primary key then we need to fix the vrcp table
		if (($oldkey!="") and ($key!=$oldkey))
		$success=	updatevrcpdicomkeychange($oldkey,$key,
		                                          $user, $gateway);
	// if changing the wado flag setting then we must update the vrcp table as well
		if ($success==true)
		{//updating wado flags if needed
		//	if ($wadochanged==true)
			$success =  updatevrcpdicomwadoflags($showmenus,$user, $gateway,$key);
		 //echo "updatedvrcpdicomwadflags $success oldshow $oldshowmenus newsho $showmenus";
		} 
		return $success;
} 
function patchandexit($rowid,$key,$user,$gateway,$aetitle,$showmenus,$dicomipaddress,$dicomport,$comment,$wadochanged,$mess)
{
$success =  insertdicomdetails($key,$user,$gateway,$aetitle,$showmenus,$dicomipaddress,$dicomport,$comment);
//now fix up the vrcp table
if ($success==true) $success = vrcpcleanup ($rowid,$key,$user, $gateway,"",$showmenus,$wadochanged);
if ($success==true) exit_with_message($mess);
else exit_with_message("internal processing error in DICOM device $key - please contact the call center");
// end of the insert case, all processing stops here
} 
function exit_with_message ($s)
{	sqltracedump();
	echo display_dialog_complete("<p>$s</p>"); 
	exit;}

?> 